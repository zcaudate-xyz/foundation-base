# Foundation Base

**A Clojure-first toolkit for building, generating, testing, and operating polyglot systems.**

Foundation Base combines reusable Clojure libraries, developer tooling, language generation, and runtime integration in one repository. It is intended for developers who want one REPL-driven environment for working across application code, generated source, tests, documentation, and external runtimes.

The repository is large because it contains several related systems. You do not need to understand all of them before getting started.

## Choose your path

| I want to... | Start here |
|---|---|
| Use the standard Clojure libraries | [`std`](https://zcaudate.xyz/foundation-base/std/index.html) and the guides in [`guides/`](guides/) |
| Generate JavaScript, Lua, Python, Go, SQL, Solidity, or other code | [Hara introduction](https://zcaudate.xyz/foundation-base/hara/introduction.html) |
| Run generated code in external runtimes | [Hara runtimes](https://zcaudate.xyz/foundation-base/hara/hara-runtime.html) |
| Write tests using the fact-based test framework | [`code.test` guide](guides/code.test.md) |
| Analyse, query, or refactor Clojure source | [`code.manage`](guides/code.manage.md) and [`code.query`](guides/code.query.md) |
| Generate project documentation | [`code.doc`](https://zcaudate.xyz/foundation-base/code/code-doc.html) |
| Explore portable cross-target libraries | [`xt`](https://zcaudate.xyz/foundation-base/xt/index.html) |
| Contribute to the repository | [`CONTRIBUTING.md`](CONTRIBUTING.md) |

## What is in the repository?

Foundation Base is organised into four primary areas:

| Area | Purpose |
|---|---|
| `std.*` | Standard libraries and reusable infrastructure: collections, concurrency, filesystems, strings, time, tasks, scheduling, configuration, data handling, and system utilities |
| `code.*` | Developer tooling: testing, documentation, source queries, code management, project metadata, build tooling, and analysis |
| `hara.*` and `rt.*` | Language authoring, grammar-driven code generation, typing, and runtime adapters |
| `xt.*` | Portable libraries and application layers built on top of the language tooling |

Supporting directories include:

| Path | Purpose |
|---|---|
| `src/`, `src-lang/`, `src-extra/` | Main source trees |
| `test/`, `test-lang/` | Tests, mirroring the source structure |
| `src-build/` | Walkthroughs, demos, build definitions, and generated project examples |
| `src-doc/` | Source for the generated documentation site |
| `guides/` | Task-oriented Markdown guides |
| `config/publish/` | Documentation-site configuration |

## Quick start

### Prerequisites

- Java 21
- Leiningen
- Git

Some test groups and runtimes also require tools such as Node.js, Python, R, Docker, PostgreSQL, OpenResty, or language-specific compilers. You only need those dependencies for the corresponding subsystem.

### Clone and install locally

Clojars deployment is currently paused, so the simplest installation path is a local Maven install:

```bash
git clone git@github.com:zcaudate-xyz/foundation-base.git
cd foundation-base
lein install
```

Then add the current project version from [`project.clj`](project.clj) to another Leiningen project:

```clojure
[xyz.zcaudate/foundation-base "4.1.5"]
```

### Start a REPL

```bash
lein repl
```

Try a standard-library helper:

```clojure
(require '[std.lib :as h])

(h/time-ms)
(h/pl "Hello Foundation!")
```

## Generate target-language code

`hara.lang` is a language-oriented templating and code-generation system. Clojure forms are stored in a reusable intermediate representation and emitted through a target grammar.

```clojure
(require '[hara.lang :as l])

(l/emit-as :js '(+ 1 2 3))
;; => "1 + 2 + 3"
```

The same authoring model can target multiple languages:

```clojure
(l/emit-as :js
  '[(defn add [a b]
      (return (+ a b)))
    (add 1 2)])

(l/emit-as :lua
  '[(defn add [a b]
      (return (+ a b)))
    (add 1 2)])
```

Hara goes beyond printing syntax. Language books, grammars, modules, pointers, and runtime adapters make it possible to inspect, test, and execute generated code from the same Clojure workflow.

Start with:

- [Hara overview](https://zcaudate.xyz/foundation-base/hara/index.html)
- [Introduction](https://zcaudate.xyz/foundation-base/hara/introduction.html)
- [Basic walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-basic.html)
- [Multiple-language walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-multi.html)
- [Live runtime walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-live.html)

## Write and run tests

Foundation Base uses `code.test`, not `clojure.test`. Tests are written as facts using the `=>` assertion form.

```clojure
(ns example.core-test
  (:require [code.test :refer [fact =>]]))

(fact "addition works"
  (+ 1 2) => 3)
```

Run a targeted namespace while developing:

```bash
lein test :only std.lib.collection-test
```

Run tests matching a namespace prefix:

```bash
lein test :with std.lib
```

The complete suite covers many optional runtimes and external services, so targeted tests are the recommended starting point.

## Work with the repository

There are several useful ways to interact with Foundation Base:

### As a library consumer

Install the project locally, depend on the modules you need, and treat documented public namespaces as the supported entry points. Because the repository contains both mature and experimental areas, check the relevant guide and tests before relying on an unfamiliar subsystem.

### As an explorer

Start with one focused workflow:

1. emit a small JavaScript or Lua form;
2. run one `code.test` namespace;
3. inspect one generated project under `src-build/play`;
4. browse the matching generated documentation page.

### As a contributor

Read [`CONTRIBUTING.md`](CONTRIBUTING.md) for setup, repository conventions, testing expectations, documentation generation, and pull-request guidance.

### As a documentation author

The public site is generated from files in `src-doc/documentation/`. The repository homepage and `src-doc/documentation/main_index.clj` intentionally use the same project description and navigation model. When one changes, update the other in the same pull request.

## Guides

- [`code.test`](guides/code.test.md) — fact-based testing and checkers
- [`code.manage`](guides/code.manage.md) — source maintenance and test management
- [`code.query`](guides/code.query.md) — structural source queries and edits
- [`std.task`](guides/std.task.md) — task definition and execution
- [`std.block`](guides/std.block.md) — source block parsing and layout
- [`std.scheduler`](guides/std.scheduler.md) — concurrent scheduling
- [`std.timeseries`](guides/std.timeseries.md) — time-series data and aggregation
- [`xt.db.node.kernel-base`](guides/xt.db.node.adaptor_base.md) — database node bootstrap and RPC routing

## Project status

Foundation Base is an active codebase containing production-used libraries, evolving developer tooling, and experimental language/runtime integrations.

Documentation should use these maturity labels where possible:

- **Stable** — relied upon by production systems or other repositories
- **Usable** — functional and tested, but APIs may still change
- **Experimental** — research, prototypes, incomplete targets, or environment-specific integrations

The full test suite currently includes runtime-dependent groups and is not guaranteed to pass on a minimal workstation without additional services. See the open issues and [`CONTRIBUTING.md`](CONTRIBUTING.md) for current work.

## Documentation

Published documentation: <https://zcaudate.xyz/foundation-base/>

Documentation source: [`src-doc/documentation/`](src-doc/documentation/)

Generate documentation locally with:

```bash
lein publish
```

## License

Copyright © 2023 Chris Zheng. Distributed under the MIT License.
