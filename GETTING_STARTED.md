# Getting Started with Foundation Base

Foundation Base is a Clojure-first toolkit for reusable libraries, developer tooling, language generation, and runtime integration.

This guide covers the core setup shared by most workflows. Optional language targets and runtime adapters may require additional tools.

## Prerequisites

Install:

- Java 21
- Leiningen
- Git

Verify the environment:

```bash
java -version
lein version
git --version
```

## Clone the repository

```bash
git clone git@github.com:zcaudate-xyz/foundation-base.git
cd foundation-base
```

Download dependencies:

```bash
lein deps
```

## Install locally

Clojars deployment is currently paused. Install Foundation Base into your local Maven repository:

```bash
lein install
```

The current project version is declared in [`project.clj`](project.clj). At the time of this update it is:

```clojure
[xyz.zcaudate/foundation-base "4.1.5"]
```

Prefer checking `project.clj` rather than copying a version from an old document.

## Start a REPL

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

`hara.lang` emits Clojure forms through target-language grammars:

```clojure
(require '[hara.lang :as l])

(l/emit-as :js '(+ 1 2 3))
;; => "1 + 2 + 3"
```

Emit a small function:

```clojure
(l/emit-as :js
  '[(defn add [a b]
      (return (+ a b)))
    (add 1 2)])
```

Continue with the published Hara documentation:

- <https://zcaudate.xyz/foundation-base/hara/index.html>
- <https://zcaudate.xyz/foundation-base/hara/introduction.html>
- <https://zcaudate.xyz/foundation-base/hara/walkthrough-basic.html>

## Run tests

Foundation Base uses the custom `code.test` framework, not `clojure.test`.

Tests use `fact` and `=>`:

```clojure
(ns example.core-test
  (:require [code.test :refer [fact =>]]))

(fact "addition works"
  (+ 1 2) => 3)
```

Run one namespace:

```bash
lein test :only std.lib.collection-test
```

Run a namespace group:

```bash
lein test :with std.lib
```

Run the complete suite:

```bash
lein test
```

The complete suite exercises optional runtimes and services. A minimal workstation may not have every required program, database, browser, compiler, or container image. Targeted tests are the recommended development workflow.

## Generate documentation

Documentation source lives under:

```text
src-doc/documentation/
config/publish/
```

Generate configured sites:

```bash
lein publish
```

Serve generated documentation:

```bash
lein serve
```

The repository homepage and generated-documentation homepage intentionally mirror each other:

```text
README.md
src-doc/documentation/main_index.clj
```

Update both when changing the project's description, navigation, repository map, or onboarding paths.

## Repository map

| Path or namespace | Purpose |
|---|---|
| `std.*` | Standard libraries and reusable infrastructure |
| `code.*` | Testing, documentation, source management, queries, tooling, and analysis |
| `hara.*` | Language authoring, target models, typing, and code generation |
| `rt.*` | Runtime adapters and execution environments |
| `xt.*` | Portable libraries and cross-target application layers |
| `src-build/` | Walkthroughs, demos, build definitions, and generated projects |
| `src-doc/` | Documentation source |
| `guides/` | Task-oriented Markdown guides |

## Common commands

| Command | Purpose |
|---|---|
| `lein repl` | Start the development REPL |
| `lein test :only namespace-test` | Run one test namespace |
| `lein test :with namespace-prefix` | Run a related test group |
| `lein test` | Run the complete custom test suite |
| `lein manage` | Run code-management tasks |
| `lein publish` | Generate documentation |
| `lein serve` | Serve generated documentation |
| `lein install` | Install the project into the local Maven repository |

## Next steps

- Read [`README.md`](README.md) to choose an interaction path.
- Read [`CONTRIBUTING.md`](CONTRIBUTING.md) before proposing changes.
- Use the task-oriented guides under [`guides/`](guides/).
- Start with one subsystem rather than configuring every optional runtime.
