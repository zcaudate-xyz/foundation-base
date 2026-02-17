# Getting Started with Foundation Base

This guide covers how to get up and running with `foundation-base` for both users (library consumers) and developers (contributors).

## For Users

### Installation

`foundation-base` is a Leiningen-based Clojure project. While Clojars deployment is currently paused, you can install the library locally to your `~/.m2` repository.

1.  **Clone the repository:**
    ```bash
    git clone git@github.com:zcaudate-xyz/foundation-base.git
    cd foundation-base
    ```

2.  **Install locally:**
    You must have [Leiningen](https://leiningen.org/) installed.
    ```bash
    lein install
    ```

### Basic Usage (REPL)

Once installed, you can include `[xyz.zcaudate/foundation-base "4.0.0"]` (check `project.clj` for the latest version) in your project's dependencies.

To try it out immediately within the repo:

1.  Start a REPL:
    ```bash
    lein repl
    ```

2.  Try out the `std.lib` utilities:
    ```clojure
    (require '[std.lib :as h])

    ;; Use the 'time' helper to get current timestamp
    (h/time-ms)

    ;; Use logging
    (h/pl "Hello Foundation!")
    ```

3.  Explore `std.lang` (the core feature):
    ```clojure
    (require '[std.lang :as l]
             '[std.lang.model.spec-js :as js])

    ;; Transpile a simple Clojure form to JavaScript
    (l/emit-as :js '(+ 1 2 3))
    ;; => "1 + 2 + 3"
    ```

## For Developers

### Prerequisites

The development environment requires:
*   **Java 21**: Verify with `java -version`.
*   **Leiningen**: The project relies heavily on `lein`. The `clojure` CLI tools are not used.

**Installing Leiningen (if missing):**
```bash
curl -O https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod +x lein
# Move to PATH or use ./lein locally
```

### Setup

1.  **Clone the repo:**
    ```bash
    git clone git@github.com:zcaudate-xyz/foundation-base.git
    cd foundation-base
    ```

2.  **Download dependencies:**
    ```bash
    lein deps
    ```

### Running Tests

The project uses a custom testing framework, `code.test`. **Do not use `clojure.test` directly.**

*   **Run all tests** (Warning: may take a long time):
    ```bash
    lein test
    ```

*   **Run a specific namespace:**
    This is the recommended workflow.
    ```bash
    lein test :only std.lib.collection-test
    ```

*   **Run tests matching a pattern:**
    ```bash
    lein test :with std.lib
    ```

### Project Structure

*   `src/`: Main source code.
    *   `src/std/`: Standard libraries (`lib`, `lang`, `task`, etc.).
    *   `src/code/`: Development tools (`test`, `manage`, `doc`).
    *   `src/rt/`: Runtimes for various languages.
*   `test/`: Test files, mirroring the `src` structure.
*   `plans/`: Documentation and architectural plans.
*   `project.clj`: Project configuration.

### Common Commands

*   `lein repl`: Start a development REPL.
*   `lein manage`: Access management tasks (scaffolding, analysis).
