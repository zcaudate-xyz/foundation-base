# Getting Started

## Prerequisites

Install:

- Java 21
- Leiningen
- Git

Optional subsystems may also require Node.js, Python, R, Docker, PostgreSQL, OpenResty, native compilers, or browser tooling.

## Clone and install

```bash
git clone git@github.com:zcaudate-xyz/foundation-base.git
cd foundation-base
lein deps
lein install
lein repl
```

The current dependency version is declared in [`project.clj`](https://github.com/zcaudate-xyz/foundation-base/blob/main/project.clj).

## Try a standard-library helper

```clojure
(require '[std.lib :as h])

(h/time-ms)
(h/pl "Hello Foundation!")
```

## Emit JavaScript

```clojure
(require '[hara.lang :as l])

(l/emit-as :js '(+ 1 2 3))
;; => "1 + 2 + 3"
```

## Run one test namespace

```bash
lein test :only std.lib.collection-test
```

Foundation Base uses `code.test`, not `clojure.test`. Targeted test runs are recommended because the full suite includes optional runtimes and services.

## Choose the next page

- [Examples](Examples)
- [Hara Language Tooling](Hara-Language-Tooling)
- [Code Tools](Code-Tools)
- [Repository Map](Repository-Map)

See the full [GETTING_STARTED.md](https://github.com/zcaudate-xyz/foundation-base/blob/main/GETTING_STARTED.md) for more detail.
