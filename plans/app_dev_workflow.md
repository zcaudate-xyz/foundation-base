# Application Development Workflow

This plan outlines the workflow for developing applications using `foundation-base` tools (`std.make` and `code.dev`).

## 1. Overview

The project provides custom tooling for building, testing, and managing the development lifecycle.
*   **`std.make`**: Build automation, project management, and task execution (similar to Makefiles or Leiningen tasks but in Clojure).
*   **`code.dev`**: Development utilities, test runners, and refactoring helpers.

## 2. Using `std.make` (Build & Tasks)

The `std.make` namespace is the entry point for build tasks. It exposes functionality typically found in CI/CD pipelines or local build scripts.

### 2.1. Key Functions
*   `make/build`: Compiles the project or specific modules.
*   `make/run`: Runs specific tasks or targets.
*   `make/project`: Manages project definitions (`def.make`).
*   `make/gh-*`: GitHub integration (clone, commit, push, release).

### 2.2. Common Workflows
*   **Building**: `(make/build)` or `(make/build-all PROJECT)`
*   **Running Tests**: `(make/run:test)`
*   **Starting Dev Server**: `(make/run:dev)`
*   **Releasing**: `(make/run:release)`

*Note*: These functions are likely designed to be called from a REPL or a script runner (like `lein exec` or `clj -M`).

## 3. Defining Projects with `def.make`

Projects are defined using the `def.make` macro (typically from `std.make`). This macro creates a configuration object that `std.make` uses to execute tasks.

### 3.1. Structure of `def.make`

```clojure
(def.make PROJECT-NAME
  {:tag      "project-tag"         ;; Tag used for identification
   :build    ".build/output-dir"   ;; Output directory for build artifacts
   :github   {:repo "user/repo"}   ;; GitHub repository info

   ;; Configuration Sections
   :sections {:common [...]        ;; Shared configurations (e.g., makefiles, workflows)
              :node   [...]        ;; Node.js specific config (package.json, gitignore)
              :setup  [...]}       ;; Setup steps

   ;; Default Build Target
   :default  [{:type   :module.single  ;; Build type (single module, graph, etc.)
               :lang   :js             ;; Target language
               :main   'my.namespace   ;; Entry point namespace
               :file   "src/main.js"   ;; Output file
               :emit   {...}}]})       ;; Emission options
```

### 3.2. Example Config Types
*   **:makefile**: Defines a `Makefile` content.
*   **:package.json**: Defines `package.json` content (dependencies, scripts).
*   **:gitignore**: Defines `.gitignore` content.
*   **:module.single**: Transpiles a single module/namespace.
*   **:module.graph**: Transpiles a module and its dependencies.

### 3.3. Triggering Builds
You can associate namespaces with a project so that changes trigger a rebuild:
```clojure
(make/triggers-set PROJECT-NAME #{'my.namespace 'other.namespace})
```

## 4. Using `code.dev` (Development & Testing)

`code.dev` provides helpers for day-to-day development, particularly focused on testing and code organization.

### 4.1. Test Groups
Instead of running all tests, `code.dev` defines groups for faster feedback:
*   `dev/test:base`: Core libraries (`std`, `math`).
*   `dev/test:framework`: Framework components (`code`, `jvm`, `script`).
*   `dev/test:infra`: Infrastructure (`platform`, `kmi`, `rt`).
*   `dev/test:app`: Application level (`fx`, `js`).
*   `dev/test:all`: Everything.

### 4.2. Refactoring Tools
*   `dev/fix-tests`: Moves test files to the correct path based on their namespace.
*   `dev/rename-tests`: Renames test namespaces and moves files.
*   `dev/rename-test-var`: Renames specific test vars/refer metadata.

## 5. Proposed Workflow for the App

1.  **Start REPL**: `./lein repl`
2.  **Load Tools**:
    ```clojure
    (require '[std.make :as make]
             '[code.dev :as dev])
    ```
3.  **Find Project Definition**: Locate the `def.make` form in your component (e.g., `src-build/play/.../build.clj`).
4.  **Run Build**:
    ```clojure
    (require '[my.project.build :as build])
    (make/build-all build/PROJECT)
    ```
5.  **Run Dev Server** (if defined):
    ```clojure
    (make/run:dev build/PROJECT)
    ```

## 6. Next Steps for Developer

1.  Identify the specific "app" component (e.g., `src-build/play/...`).
2.  Check for a `build.clj` or `def.make` in that component.
3.  Use `std.make` to target that build.
