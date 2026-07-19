# Agent Instructions for Foundation Base

This document provides essential context for AI agents working with this codebase.

## Testing Framework

**IMPORTANT:** This project uses a custom testing framework (`code.test`), NOT the standard `clojure.test`.

### How Tests Work

- The `lein test` command is aliased in `project.clj` to run `code.test` (via `:aliases {"test" ["run" "-m" "code.test"]}`)
- This is NOT the standard Leiningen test runner - it's a custom test framework with different behavior
- Tests are defined using `(fact ...)` macros and `=>` assertions, not `deftest` and `is`

### Running Tests

```bash
# Run all tests (Warning: ~930 tests, may take several minutes)
lein test

# Run a specific test namespace (RECOMMENDED)
lein test :only code.doc-test
lein test :only std.lib.collection-test

# Run tests matching a namespace pattern
lein test :with code.doc
lein test :with std.lib
```

### Test Syntax

Tests use the `fact` macro and `=>` arrow assertions:

```clojure
^{:refer my.namespace/function-name :added "3.0"}
(fact "description of what this tests"
  (function-name arg1 arg2)
  => expected-result)
```

### Fact IDs: one `:refer` per namespace, or use `:id`

A fact's id is derived from its `:refer` metadata (`fact-id` in
`code.test.base.runtime`). **Two facts in the same namespace with the same
`:refer` collide: the later one overwrites the earlier one in the registry,
and the earlier fact never runs** — the suite still reports green, just with
fewer checks. `install-fact` now prints a `WARNING code.test: fact ... will
NOT run` line at load time when this happens; never let those warnings stand
in a namespace you own (many pre-existing ones remain elsewhere in the tree).

- Keep to one fact per `:refer` per namespace (the existing convention), or
- give every fact a unique explicit id: `^{:refer my.ns/fn :id test-my-fn-2}`.

To prove a test file's assertions actually execute, temporarily break one
expectation and confirm the run fails (the summary line shows
`FAILED L:<line>`), then revert. Don't trust the passed-check count alone.

### Assert the intended result

Tests must prove the function's semantic contract, not merely that execution
did not throw or that a delivery mechanism returned a success status.

- Test a transport or handler delivery mechanism once. Tests for functions
  reached through it should then assert each function's actual return value or
  resulting state.
- Preserve stable domain expectations exactly. For example, if an RPC is
  expected to return `"pong"`, assert `"pong"`; do not substitute a different
  RPC or weaken the expectation to a status, type, or non-nil check.
- Use `promise-catch` as the asserted path only for intentional negative tests.
  Include an explicit rejection marker and assert the relevant error fields so
  that an accidentally resolved promise cannot satisfy the test.
- A catch may help diagnose a positive flow, but catching an exception must
  never turn that positive test into a pass.
- A status-only assertion is appropriate only when the function's documented
  domain result genuinely is status-only; say so in the fact description.
- Seedgen transforms must preserve these semantic assertions across target
  languages. Prefer a language transform over suppressing or weakening a fact.

### Conditional Namespace Skipping

Use `fact:global` with `:skip` to skip every fact in a namespace when a runtime condition is not met (for example, an external OS program is missing). Skipped facts are reported as skipped and their bodies are not executed.

```clojure
(fact:global
 {:skip (not (std.lib.env/program-exists? "tcc"))})

^{:refer my.namespace/function-name :added "3.0"}
(fact "tests functionality requiring tcc"
  ...
  => expected-result)
```

When `:skip` is truthy, namespace setup and teardown are also bypassed, so missing dependencies cannot cause setup failures.

Version-based skips are also supported using `program-version` and `version>=`:

```clojure
(fact:global
 {:skip (not (env/version>= (env/program-version "docker") "24.0"))})
```

## code.doc - Documentation System

`code.doc` is the documentation generation system. It generates HTML documentation from Clojure source files.

### Architecture

```
config/publish.edn          # Main config, lists sites
config/publish/std.lib.edn  # Site config (pages, theme, output)
src-doc/documentation/*.clj # Documentation source files
public/                     # Generated HTML output
```

### Configuration Files

**Main Config** (`config/publish.edn`):
```clojure
{:template {...}
 :snippets "config/snippets"
 :sites {:core    [:include "config/publish/foundation.core.edn"]
         :std.lib [:include "config/publish/std.lib.edn"]}}
```

**Site Config** (`config/publish/std.lib.edn`):
```clojure
{:theme  "bolton"    ; Theme name (bolton/stark)
 :output "public"    ; Output directory
 :pages  {page-key   ; Page identifier
          {:input    "src-doc/documentation/file.clj"
           :title    "Page Title"
           :subtitle "Description"}}}
```

### Documentation Source Syntax

Documentation files are Clojure files in `src-doc/documentation/`:

```clojure
(ns documentation.my-page
  (:use code.test))

;; Chapter/Section headers
[[:chapter {:title "Introduction"}]]
[[:section {:title "Subsection"}]]

;; Markdown paragraphs (just strings)
"This is **markdown** text."

;; API documentation - auto-generated from namespace
[[:api {:namespace "std.lib.collection"}]]

;; API with filters
[[:api {:namespace "std.lib.collection"
        :only ["map-keys" "map-vals"]
        :exclude ["internal-fn"]}]]

;; Code examples (using fact macro)
(fact "map-keys example"
  (map-keys inc {0 :a 1 :b})
  => {1 :a 2 :b})

;; Non-running code examples
(comment
  (this-wont-run-but-will-be-displayed))

;; Reference source code
[[:reference {:refer "std.lib.collection/map-keys"}]]

;; Reference tests
[[:reference {:refer "std.lib.collection/map-keys" :mode :test}]]
```

### Generating Documentation

```bash
# Generate all docs for a site
lein exec -ep "(use 'code.doc) (publish '[std.lib] {:write true})"

# Generate specific page
lein exec -ep "(use 'code.doc) (publish '[std.lib/my-page] {:write true})"

# Generate without writing (dry run)
lein exec -ep "(use 'code.doc) (publish '[std.lib])"
```

Note: The page key uses the format `[site-key/page-key]` where page-key matches
the key defined in the site config (with hyphens instead of underscores).

### API resolution notes

`[[:api {:namespace "..."}]]` elements resolve aggregate namespaces built with
`f/intern-in` / `f/intern-all` (e.g. `std.block`, `std.lib`, `code.test`), including
`[dst src]` alias vectors and one level of nesting (`std.block` → `std.block.heal`
→ `std.block.heal.core`). Analysis for docs also indexes `def`, `defonce`, `defn-`,
`definvoke` and `impl/defimpl` forms (the latter with generated `map->`/`->`
constructor entries) — scoped to code.doc via `code.doc.collect.reference/*doc-toplevel-forms*`,
so `code.manage` analysis is unaffected. Vars generated by macros at runtime
(e.g. `std.lib.bin` buffers) cannot be resolved statically; `code.doc/check` reports
them as `missing-source`.

### Checking Documentation

`code.doc/check` validates pages before publishing: missing `:api` namespaces,
unresolved `:reference` targets, api entries with no source or example
(rendered as "source not found" / "example not found"), typo'd `:only` vars,
unknown `[[:related]]`/`[[:links]]` data groups, and page load/parse errors.
With `:eval true` it also executes page facts and reports failing sections.

```bash
# Check all sites
lein exec -ep "(use 'code.doc) (check :all)"

# Check one site, evaluating page facts
lein exec -ep "(use 'code.doc) (check '[core] {:eval true})"

# CI-friendly: exits non-zero when any issue is found
lein doc-check
```

### Available Elements

| Element | Purpose |
|---------|---------|
| `[[:chapter {:title "..."}]]` | Top-level section |
| `[[:section {:title "..."}]]` | Subsection |
| `[[:subsection {:title "..."}]]` | Lower-level section |
| `[[:api {:namespace "..."}]]` | Auto-generate API docs |
| `[[:reference {:refer "ns/fn"}]]` | Include source code |
| `[[:image {:src "..." :title "..."}]]` | Embed image |
| `[[:file {:src "..."}]]` | Include another file |
| `[[:code {:lang "python"} "..."]]` | Code in other languages |
| `[[:related {:group "..."}]]` | Related-libraries table from shared data |
| `[[:links {:group "..."}]]` | Link chips from shared data |

### Data-driven related and links sections

`config/publish/related.edn` (path set by the top-level `:data` key in
`config/publish.edn`) holds shared entries: `:libraries` (name, href,
description, comparison, group), `:links` (label, href, group), and
`:namespaces` (curated extra entries per documented namespace).

- `[[:related {...}]]` renders a comparison table from `:libraries`;
  `[[:links {...}]]` renders link chips from `:links`. Both honour `:group`,
  `:only` and `:exclude` filters.
- Pages **without** an explicit `[[:related]]` element get one appended
  automatically: sibling pages documenting namespaces in the same group
  (e.g. other `jvm.*` pages on `std/jvm-monitor.html`) plus curated
  `:namespaces` entries. Home pages (`home.html` base) and pages with no
  documented namespaces are skipped.

### Codox API reference

The project is codox-compatible. A `:codox` lein profile carries the codox
dependency so the normal build is untouched; the `codox` alias runs
`code.doc.codox/-main`, which exits the JVM explicitly (the plugin task hangs
on non-daemon threads after generation):

```bash
# Generate codox API docs into public/api
lein codox
```

The `Publish Documentation` workflow runs this after code.doc publishing, so
`public/api` ships with the GitHub Pages artifact. Known limitation: codox
only analyses core `def`/`defn` forms — vars defined via custom forms
(`defn.js`, `invoke/definvoke`, `deftask`, ...) and aggregate namespaces built
with `f/intern-all` (`std.lib`, `hara.lang`, `code.test`) do not appear in
codox output; use code.doc for those.

### Page Key Mapping

In config: `:my-page` → In command: `[site/my-page]`

Example: `std-lib-collection` key in config → `[std.lib/std-lib-collection]` in command.

## Key Project Conventions

### Namespaces

- `std.*` - Standard libraries (core functionality)
- `code.*` - Development tools (test, manage, doc, framework)
- `rt.*` - Language runtimes

### Common Commands

```bash
# Start REPL
lein repl

# Run tests (custom framework)
lein test

# Run management tasks
lein manage

# Publish documentation (code.doc)
lein publish

# Check documentation pages (missing namespaces/refs, failing sections)
lein doc-check

# Generate codox API reference into public/api
lein codox

# Install to local maven repo
lein install
```

### Project Structure

- `src/` - Main source code
- `test/` - Test files (mirror src structure)
- `src-doc/` - Documentation site source
- `public/` - Generated documentation output
- `config/` - Configuration files

## Important Notes

1. **Do not assume standard Clojure test conventions** - This project predates or diverges from common patterns
2. **The test framework modifies files** - Some tests (like `grep-replace`) can modify source files; the framework shows diffs but doesn't write unless explicitly told to
3. **Large codebase** - 930+ tests across many namespaces; prefer targeted test runs

## Seedgen and xtbench authoring

`test-lang/xt/**/*.clj` contains the **canonical seed tests**. Each seed is written once for the `:js` runtime and then generated into target-language bench files under `test-lang/xtbench/<lang>/...`. The generated namespaces are named `xtbench.<lang>.<path>-test`.

The goal is maximum parity: the same facts, assertions, and test flow should run on every target. Use the `:seedgen/*` metadata to adapt the seed, not to remove coverage.

### Core principles

1. **Keep the canonical `:js` seed intact.** Do not remove the `(l/script- :js ...)` form or `:js` checks to make another target pass. Fix the target implementation, the seed, or the metadata, then regenerate.
2. **Do not edit generated `test-lang/xtbench/**` files by hand.** They are derived output. Change the seed or the generator and regenerate.
3. **Prefer portable XT APIs first.** Only introduce per-language adapters when the API really differs.
4. **Prefer `:transform` over `:suppress`.** If a target needs a different function name or shape, use `:seedgen/base {:<lang> {:transform ...}}`. Reserve `:suppress` for capabilities that truly cannot be expressed on a target, and document why.
5. **Regenerate and run both sides after a seed change.** Run the canonical source test (`lein test :only xt.<path>-test`) and the generated target test (`lein test :only xtbench.<lang>.<path>-test`).

### Common commands

```bash
# Add/update generated bench files for one or more languages
lein seedgen benchadd '[xt.lang] :lang [:lua :python] :write true'

# Remove generated bench files for a language
lein seedgen benchremove '[xt.lang] :lang python :write true'

# Run generation + tests for selected languages
lein seedgen test :all :with [dart python]

# List compatible namespaces for a language
lein seedgen compatible '[xt.lang]'

# Find facts missing coverage for the canonical runtime
lein seedgen incomplete '[xt.lang]'
```

### `:seedgen/*` metadata reference

#### `:seedgen/root` — declare a seed and its targets

Attach to the canonical `(l/script- :js ...)` form. It declares which languages to generate and which adapter requires to inject per target.

```clojure
^{:seedgen/root {:all true
                 :langs [:lua :python]
                 :lua     {:extra [[xt.lang.common-promise :as lua-promise]]}
                 :python  {:extra [[xt.lang.common-promise :as py-promise]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             ...
             ^{:seedgen/extra true}
             [js.net.http-fetch :as js-fetch]]})
```

- `:all true` — generate for every registered language.
- `:langs [...]` — restrict generation to those languages. The root language (`:js`) should not be listed here unless you want a bench copy of the canonical seed.
- `:<lang> {:extra [...]}` — inject adapter requires into the generated target script.
- `^{:seedgen/extra true}` on a require inside the root script — marks the canonical adapter; it is kept only for matching languages.

Good examples: `test-lang/xt/lang/spec_base_test.clj:8`, `test-lang/xt/net/http_fetch_test.clj:26`, `test-lang/xt/db/system/impl_sqlite_test.clj:6`.

#### `:seedgen/extra` — tag canonical adapter requires

Inside the root script, mark the canonical adapter so the generator knows to drop it for non-matching targets:

```clojure
(l/script- :js
  {:require [[xt.net.http-fetch :as fetch]
             ^{:seedgen/extra true}
             [js.net.http-fetch :as js-fetch]]})
```

Each target's adapter is declared in `:seedgen/root :<lang> :extra`.

#### `:seedgen/scaffold` — harness-only blocks

Use when a runtime form is needed to run the Clojure-hosted source test but is **not** part of the canonical seed or a derived runtime seed.

```clojure
^{:seedgen/scaffold true}
(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v1 :as scratch]]})
  ...)
```

Examples: `test-lang/xt/net/http_fetch_test.clj:7`, `test-lang/xt/db/text/sql_call_test.clj:7`, `test-lang/xt/event/base_model_test.clj:6`.

#### `:seedgen/skip` — exclude a file from generation

```clojure
^{:seedgen/skip true}
(ns xt.substrate.transport-browser-test ...)
```

Use for language-specific files that should never be generated. Examples: `test-lang/xt/substrate/transport_browser_test.clj:1`, `test-lang/xt/substrate/walkthrough_js/*`.

#### `:seedgen/base` — per-language overrides

Attach to a `fact`, a check clause, or a setup/teardown item. It is a map from language keyword to an override map. Use `:all` to apply to every target language. Specific language entries win over `:all`.

Supported override keys:

##### `:suppress`

Skip this clause/fact/setup for the given language.

```clojure
;; Suppress on lua only
^{:refer xt.event.util-validate/validate-fields-loop :added "4.1"
  :seedgen/base {:lua {:suppress true}}}
(fact "..." ...)

;; Suppress everywhere except lua and python
^{:seedgen/base {:all    {:suppress true}
                 :lua    {:suppress false}
                 :python {:suppress false}}}
(!.js ...)
```

**Use `:suppress` only when a capability cannot be expressed or supported on the target.** Document the reason near the suppressed form and keep the remaining parity coverage.

Examples: `test-lang/xt/event/util_validate_test.clj:224`, `test-lang/xt/lang/spec_base_test.clj:137`, `test-lang/xt/lang/spec_primitive_test.clj:57`, `test-lang/xt/lang/common_data_test.clj:820`.

##### `:expect`

Replace the generated expected value for the target language. The value is printed as-is into the generated `=>` clause.

```clojure
^{:seedgen/base {:all {:expect [1 4]}}}
(!.js
  [(xtm/mod 10 3)
   (xtm/mod -1 5)])
=> [1 -1]
```

Examples: `test-lang/xt/lang/common_math_test.clj:241`, `test-lang/xt/event/base_model_test.clj:355`, `test-lang/xt/lang/spec_base_test.clj:2568`.

##### `:transform`

Rewrite the generated expression text before it is emitted. This is the preferred way to handle target-specific symbol or shape differences.

```clojure
;; Symbol substitution
^{:seedgen/base {:dart {:transform '{js-ws/create dart-ws/create}}}}
(notify/wait-on [:js 5000] ...)

;; Form substitution
^{:seedgen/base {:lua {:transform {xt/x:iter-from xt/x:iter-from-arr}}}}
(!.js
  (xt/for:iter [e (xt/x:iter-from [2 4 6])] ...))
```

Examples: `test-lang/xt/lang/spec_base_test.clj:2463`, `test-lang/xt/lang/spec_base_test.clj:2701`, `test-lang/xt/lang/common_lib_test.clj:682`, `test-lang/xt/db/text/sql_view_test.clj:34`, `test-lang/xt/db/text/sql_call_test.clj:75`.

##### `:input`

Replace the entire generated input expression for a target language. This is the most invasive override; use it sparingly.

#### `:seedgen/derived`, `:seedgen/lang`, `:seedgen/check` — legacy/merged keys

- `:seedgen/derived` explicitly classifies a runtime form as a derived runtime seed. It is legacy and unused in current tests.
- `:seedgen/lang` and `:seedgen/check` are legacy synonyms that are merged into the `:seedgen/base` config by the generator. Prefer `:seedgen/base` in new tests.

### Decision order for adapting a seed

When a target fails against a canonical seed, consider options in this order:

1. **Fix the target implementation** so it matches the canonical behavior.
2. **Use a portable XT API** instead of a language-specific one.
3. **Inject a per-language adapter** with `:seedgen/root :<lang> :extra` and tag the canonical adapter require with `^{:seedgen/extra true}`.
4. **Substitute symbols or shape** with `:seedgen/base :<lang> :transform` while keeping the same fact and assertions.
5. **Adjust only the expected value** with `:seedgen/base :<lang> :expect` when the result differs but the expression is fine.
6. **Suppress the clause or fact** with `:seedgen/base :<lang> :suppress` only when the capability truly cannot be expressed. Leave a comment explaining why.
7. **Replace the input expression** with `:seedgen/base :<lang> :input` only as a last resort.

### Good vs bad patterns

**Good — adapter injection + transform preserves parity:**

`test-lang/xt/substrate/walkthrough/s07_wsserver_test.clj:55-75`

```clojure
^{:seedgen/root {:all true :dart {:extra [[dart.net.ws-native :as dart-ws]]}}}
(l/script- :js
  {:require [...
             ^{:seedgen/extra true}
             [js.net.ws-native :as js-ws]
             ...]})

^{:seedgen/base {:dart {:transform '{js-ws/create dart-ws/create}}}}
(notify/wait-on [:js 5000] ...)
```

**Good — suppress only where unavoidable, with a reason:**

`test-lang/xt/lang/spec_base_test.clj:137`

```clojure
^{:seedgen/base {:all    {:suppress true}
                 :lua    {:suppress false}
                 :python {:suppress false}}}
(!.js ...)
```

**Avoid — using `:suppress` where a `:transform` would preserve parity.** Instead of suppressing a whole fact because one function name differs, substitute that symbol with `:transform` as in `test-lang/xt/lang/spec_base_test.clj:2463`.

**Avoid — stale generated files.** After changing a seed, regenerate the affected bench and run both the canonical source test and the generated target test.
