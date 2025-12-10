(ns code.tool.measure-test
  (:use code.test)
  (:require [code.tool.measure :as measure]
            [code.tool.measure.algo-clojure :as clj]
            [code.tool.translate.js-ast :as build-ast]
            [std.make :as make]
            [std.lib :as h]))

(fact "should score code structure invariantly (JS)"
  :setup [(make/build-all build-ast/BUILD_AST) (build-ast/initialise)] ;; Skipped to avoid npm path issues in test runner
  (let [code-original "function add(a, b) { return a + b; }"
        code-renamed  "function sum(x, y) { return x + y; }"
        code-spaced   "function  add  ( a ,  b )  { \n return a + b ; \n }"
        score-orig    (measure/generate-score code-original)
        score-renamed (measure/generate-score code-renamed)
        score-spaced  (measure/generate-score code-spaced)]

    score-orig => number?
    score-orig => score-renamed
    score-orig => score-spaced))

(fact "should score control flow higher (JS)"
  :setup [(make/build-all build-ast/BUILD_AST) (build-ast/initialise)]
  (let [code-linear "function f() { var a = 1; var b = 2; }"
        code-branch "function f() { if (true) { var a = 1; } else { var b = 2; } }"
        score-linear (measure/generate-score code-linear)
        score-branch (measure/generate-score code-branch)]
    (> score-branch score-linear) => true))

(fact "should score code structure invariantly (CLJ)"
  (let [code-original "(defn add [a b] (+ a b))"
        code-renamed  "(defn sum [x y] (+ x y))"
        m-orig    (clj/generate-metrics code-original)
        m-renamed (clj/generate-metrics code-renamed)]

    (:complexity m-orig) => (:complexity m-renamed)
    (:surface m-orig) => (:surface m-renamed)))

(fact "should score control flow higher (CLJ)"
  (let [code-linear "(do (def a 1) (def b 2))"
        code-branch "(if true (def a 1) (def b 2))"
        m-linear (clj/generate-metrics code-linear)
        m-branch (clj/generate-metrics code-branch)]
    ;; `if` is in *control-flow-types*, `do` is not.
    (> (:complexity m-branch) (:complexity m-linear)) => true))

(fact "should calculate surface area (CLJ)"
  (let [code-small "(def a 1)"
        code-large "(def a 1) (def b 2) (def c 3)"
        m-small (clj/generate-metrics code-small)
        m-large (clj/generate-metrics code-large)]

    (> (:surface m-large) (:surface m-small)) => true))

^{:refer code.tool.measure/generate-score :added "4.1"}
(fact "generates score for code"
  (measure/generate-score "var a = 1;") => number?)

^{:refer code.tool.measure/generate-metrics :added "4.1"}
(fact "generates metrics map"
  (keys (measure/generate-metrics "var a = 1;")) => (contains [:complexity :surface] :in-any-order))

^{:refer code.tool.measure/detect-type :added "4.1"}
(fact "detects file type from extension"
  (measure/detect-type "foo.js") => :js
  (measure/detect-type "foo.clj") => :clj
  (measure/detect-type "foo.txt") => nil)

^{:refer code.tool.measure/filter-supported-files :added "4.1"}
(fact "filters supported files"
  (measure/filter-supported-files ["a.js" "b.txt" "c.clj"]) => ["a.js" "c.clj"])

^{:refer code.tool.measure/analyse-file :added "4.1"}
(fact "analyses a single file"
  (with-redefs [code.tool.measure.common/get-file-content (constantly "var a = 1;")]
    (measure/analyse-file "." "sha" "test.js"))
  => (contains {:complexity number? :surface number?}))

^{:refer code.tool.measure/analyse-commit :added "4.1"}
(fact "analyses a commit"
  (with-redefs [code.tool.measure.common/list-files (constantly ["test.js"])
                code.tool.measure.common/get-file-content (constantly "var a = 1;")]
    (measure/analyse-commit "." "sha"))
  => (contains {:complexity number? :surface number?}))

^{:refer code.tool.measure/measure-history :added "4.1"}
(fact "measures history of commits"
  (with-redefs [code.tool.measure.common/list-commits (constantly [{:sha "123" :date "2023-01-01"}])
                code.tool.measure.common/list-files (constantly ["test.js"])
                code.tool.measure.common/get-file-content (constantly "var a = 1;")]
    (let [res (measure/measure-history ".")]
      res => vector?
      (first res) => (contains {:sha "123" :date "2023-01-01" :complexity number? :surface number?})))
  )
