(ns code.ai.measure-test
  (:use code.test)
  (:require [code.ai.measure :as measure]
            [indigo.build.build-ast :as build-ast]
            [std.lib :as h]))

(fact "should score code structure invariantly"
  :setup [(build-ast/initialise)] ;; Ensure build environment is ready
  (let [code-original "function add(a, b) { return a + b; }"
        code-renamed  "function sum(x, y) { return x + y; }"
        code-spaced   "function  add  ( a ,  b )  { \n return a + b ; \n }"
        score-orig    (measure/generate-score code-original)
        score-renamed (measure/generate-score code-renamed)
        score-spaced  (measure/generate-score code-spaced)]

    ;; Scores should be identical regardless of naming and whitespace
    score-orig => number?
    score-orig => score-renamed
    score-orig => score-spaced))

(fact "should score control flow higher"
  :setup [(build-ast/initialise)]
  (let [code-linear "function f() { var a = 1; var b = 2; }"
        code-branch "function f() { if (true) { var a = 1; } else { var b = 2; } }"
        score-linear (measure/generate-score code-linear)
        score-branch (measure/generate-score code-branch)]
    (> score-branch score-linear) => true))

(fact "should calculate surface area"
  :setup [(build-ast/initialise)]
  (let [code-small "var a = 1;"
        code-large "var a = 1; var b = 2; var c = 3;" ;; Larger base
        code-deep  "if (true) { if (true) { var a = 1; } }" ;; Higher complexity

        m-small (measure/generate-metrics code-small)
        m-large (measure/generate-metrics code-large)
        m-deep  (measure/generate-metrics code-deep)]

    ;; Surface should increase with code size (base)
    (> (:surface m-large) (:surface m-small)) => true

    ;; Surface should increase with complexity (height)
    (> (:surface m-deep) (:surface m-small)) => true

    ;; Verify structure
    m-small => (contains {:complexity number? :surface number?})))
