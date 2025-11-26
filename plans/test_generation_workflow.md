# Automatic Test Generation Workflow

This document outlines the workflow for automatically generating unit tests from runtime traces.

## 1. Prerequisites

You need a source file with functions for which you want to generate tests. For this example, we will use `src/code/manage/unit/example.clj`:

```clojure
(ns code.manage.unit.example)

(defn add [a b]
  (+ a b))
```

## 2. The `generate-tests` Task

The `generate-tests` task is used to generate the tests. It takes the following arguments:

*   **`ns`**: The namespace you want to generate tests for.
*   **`:form`**: A string containing a Clojure form to execute. The test generator will trace the execution of this form to generate the tests.

## 3. Generating the Tests

To generate tests for the `code.manage.unit.example` namespace, run the following command from the root of the project:

```bash
./lein run -m code.manage generate-tests code.manage.unit.example :form "'(do (use 'code.manage.unit.example) (add 1 2))'" :write true
```

This command will:

1.  Trace the execution of the `(add 1 2)` form.
2.  Generate a test `fact` for the `add` function with the captured input and output.
3.  Write the generated test to `test/code/manage/unit/example_test.clj`.

## 4. The Generated Test File

After running the command, the `test/code/manage/unit/example_test.clj` file will contain the following:

```clojure
(ns code.manage.unit.example-test
  (:use code.test)
  (:require [code.manage.unit.example :refer :all]))

^{:refer code.manage.unit.example/add :added " (generated)"}
(fact "TODO")

(fact
  (add 1 2) => 3)
```
