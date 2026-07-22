# Foundation Base

**A Clojure-first toolkit for building, generating, testing, and operating polyglot systems.**

Foundation Base combines reusable Clojure libraries, developer tooling, language generation, and runtime integration in one repository. It is intended for developers who want one REPL-driven environment for working across application code, generated source, tests, documentation, and external runtimes.

The repository is large because it contains several related systems. You do not need to understand all of them before getting started.

## Choose your path

| I want to... | Start here |
|---|---|
| Install the project and run a first workflow | [`GETTING_STARTED.md`](GETTING_STARTED.md) |
| Use the standard Clojure libraries | [`std`](https://zcaudate.xyz/foundation-base/std/index.html), including the integrated narrative guides |
| Generate JavaScript, Lua, Python, Go, SQL, Solidity, or other code | [Hara introduction](https://zcaudate.xyz/foundation-base/hara/introduction.html) |
| Browse walkthroughs and generated projects | [`wiki/Examples.md`](wiki/Examples.md) and the [published examples page](https://zcaudate.xyz/foundation-base/examples.html) |
| Run generated code in external runtimes | [Hara runtimes](https://zcaudate.xyz/foundation-base/hara/hara-runtime.html) |
| Write tests using the fact-based test framework | [`code.test` guide](https://zcaudate.xyz/foundation-base/code/code-test.html) |
| Analyse, query, or refactor Clojure source | [`code.manage`](https://zcaudate.xyz/foundation-base/code/code-manage.html) and [`code.query`](https://zcaudate.xyz/foundation-base/code/code-query.html) |
| Generate project documentation | [`code.doc`](https://zcaudate.xyz/foundation-base/code-tools.html) |
| Explore portable cross-target libraries | [`xt`](https://zcaudate.xyz/foundation-base/xt/index.html) |
| Browse topic-oriented pages | [`wiki/Home.md`](wiki/Home.md) |
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
| `src-doc/` | Authored source for the generated documentation site, including narrative guides and API references |
| `wiki/` | GitHub Wiki-ready topic pages, kept in the main repository for review and versioning |
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

(l/emit-as :js '[(+ 1 2 3)])
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

### Build the standalone Hara CLI

Build an AOT-compiled uberjar from the Hara sources in the current checkout:

```bash
lein hara-uberjar
```

The artifact is written to
`.build/hara-uberjar/target/hara-4.1.5-standalone.jar`. It does not require
Leiningen or a Foundation Base checkout at runtime:

```bash
java -jar .build/hara-uberjar/target/hara-4.1.5-standalone.jar languages
java -jar .build/hara-uberjar/target/hara-4.1.5-standalone.jar \
  emit js '[(+ 1 2 3)]'
printf '[(* 6 7)]\n' | java -jar \
  .build/hara-uberjar/target/hara-4.1.5-standalone.jar emit lua -
```

Input is EDN and must be an outer sequential collection of forms. The CLI
supports `xtalk`, Bash, C, Dart, GLSL, JavaScript, Lua, Emacs Lisp, Scheme,
Python, SQL, and Oracle. Language specs remain lazy at runtime; the build
discovers and AOT-compiles their shared namespace closure for faster startup.

Start with:

- [Hara overview](https://zcaudate.xyz/foundation-base/hara/index.html)
- [Introduction](https://zcaudate.xyz/foundation-base/hara/introduction.html)
- [Basic walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-basic.html) — [source](src-doc/walkthrough/std_lang_00_basic.clj)
- [Multiple-language walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-multi.html) — [source](src-doc/walkthrough/std_lang_01_multi.clj)
- [Live runtime walkthrough](https://zcaudate.xyz/foundation-base/hara/walkthrough-live.html) — [source](src-doc/walkthrough/std_lang_02_live.clj)

## Examples

Examples should retain links to the authored source, project-generation definition, tests where available, generated output repository, and reproduction command.

| Example | Generated project | Authored source | Build definition |
|---|---|---|---|
| C pthreads hello | [`hoebat/play.c-000-pthreads-hello`](https://github.com/hoebat/play.c-000-pthreads-hello) | [`main.clj`](src-build/play/c_000_pthreads_hello/main.clj) | [`build.clj`](src-build/play/c_000_pthreads_hello/build.clj) |
| OpenResty hello | [`hoebat/play.ngx-000-hello`](https://github.com/hoebat/play.ngx-000-hello) | [`main.clj`](src-build/play/ngx_000_hello/main.clj) | [`build.clj`](src-build/play/ngx_000_hello/build.clj) |
| OpenResty live evaluation | [`hoebat/play.ngx-001-eval`](https://github.com/hoebat/play.ngx-001-eval) | [`main.clj`](src-build/play/ngx_001_eval/main.clj) | [`build.clj`](src-build/play/ngx_001_eval/build.clj) |
| TUI counter | [`hoebat/play.tui-000-counter`](https://github.com/hoebat/play.tui-000-counter) | [`main.clj`](src-build/play/tui_000_counter/main.clj) | [`build.clj`](src-build/play/tui_000_counter/build.clj) |
| TUI fetch | [`hoebat/play.tui-001-fetch`](https://github.com/hoebat/play.tui-001-fetch) | [`main.clj`](src-build/play/tui_001_fetch/main.clj) | [`build.clj`](src-build/play/tui_001_fetch/build.clj) |
| TUI Game of Life | [`zcaudate/play.tui-002-game-of-life`](https://github.com/zcaudate/play.tui-002-game-of-life) | [`main.clj`](src-build/play/tui_002_game_of_life/main.clj) | [`build.clj`](src-build/play/tui_002_game_of_life/build.clj) |
| React Native components | [`zcaudate/foundation.react-native`](https://github.com/zcaudate/foundation.react-native) | [`web_native_index.clj`](src-build/component/web_native_index.clj) | [`build_native_index.clj`](src-build/component/build_native_index.clj) |

Generate or push the existing examples with the project aliases:

```bash
lein push-c-000-pthreads
lein push-ngx-000-hello
lein push-ngx-001-eval
lein push-tui-000-counter
lein push-tui-001-fetch
lein push-tui-002-game-of-life
```

See [`wiki/Examples.md`](wiki/Examples.md) for the expanded examples index.

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

### As a library consumer

Install the project locally, depend on the modules you need, and treat documented public namespaces as the supported entry points. Because the repository contains both mature and experimental areas, check the relevant guide and tests before relying on an unfamiliar subsystem.

### As an explorer

Start with one focused workflow:

1. emit a small JavaScript or Lua form;
2. run one `code.test` namespace;
3. inspect one generated project under `src-build/play`;
4. browse the matching generated documentation and source links.

### As a contributor

Read [`CONTRIBUTING.md`](CONTRIBUTING.md) for setup, repository conventions, testing expectations, documentation generation, and pull-request guidance.

### As a documentation author

The public site is generated from files in `src-doc/documentation/`. The repository homepage and `src-doc/documentation/main_index.clj` intentionally use the same project description and navigation model. When one changes, update the other in the same pull request.

## Wiki pages

GitHub stores a repository Wiki in a separate Git repository named `foundation-base.wiki`. That Wiki has not yet been initialized, so the reviewable source pages currently live under [`wiki/`](wiki/).

The prepared pages include:

- [`Home`](wiki/Home.md)
- [`Getting Started`](wiki/Getting-Started.md)
- [`Repository Map`](wiki/Repository-Map.md)
- [`Hara Language Tooling`](wiki/Hara-Language-Tooling.md)
- [`Code Tools`](wiki/Code-Tools.md)
- [`Examples`](wiki/Examples.md)
- [`Contributing`](wiki/Contributing.md)

After the Wiki is enabled and its first page is created in GitHub, run:

```bash
bash bin/publish-wiki
```

This synchronizes the reviewed Markdown pages to the separate Wiki repository.

## Integrated guides

The explanatory guides now live beside the generated API sections in `src-doc/documentation/` and are published as part of the corresponding subsystem pages:

- [`code.test`](https://zcaudate.xyz/foundation-base/code/code-test.html) — fact-based testing and checkers
- [`code.manage`](https://zcaudate.xyz/foundation-base/code/code-manage.html) — source maintenance and test management
- [`code.query`](https://zcaudate.xyz/foundation-base/code/code-query.html) — structural source queries and edits
- [`std.task`](https://zcaudate.xyz/foundation-base/std/std-task.html) — task definition and execution
- [`std.block`](https://zcaudate.xyz/foundation-base/std/std-block.html) — source block parsing and layout
- [`std.scheduler`](https://zcaudate.xyz/foundation-base/std/std-scheduler.html) — concurrent scheduling
- [`std.timeseries`](https://zcaudate.xyz/foundation-base/std/std-timeseries.html) — time-series data and aggregation

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
