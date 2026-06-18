(ns code.framework.test.fact-test
  (:require [code.framework :as framework]
            [code.framework.docstring :as docstring]
             [code.framework.test.fact :refer :all]
             [std.block.navigate :as nav]
             [std.lib.collection :as collection])
  (:use code.test))

^{:refer code.framework.test.fact/gather-fact-body :added "3.0"}
(fact "extracts and formats the body of a `fact` form into docstring notation, used as a helper for `gather-fact`"
  (-> "(\n  (+ 1 1) => 2\n  (long? 3) => true)"
      nav/parse-string
      nav/down
      (gather-fact-body)
      (docstring/->docstring))
  => "\n  (+ 1 1) => 2\n  (long? 3) => true")

^{:refer code.framework.test.fact/gather-fact :added "3.0"}
(fact "Make docstring notation out of fact form"
  (-> "^{:refer example/hello-world :added \"0.1\"}
       (fact \"Sample test program\"\n  (+ 1 1) => 2\n  (long? 3) => true)"
      (nav/parse-string)
      nav/down nav/right nav/down nav/right
      (gather-fact)
      (update-in [:test] docstring/->docstring))
  => (just-in {:form  'fact
               :ns    'example,
               :var   'hello-world,
               :refer 'example/hello-world
               :added "0.1",
               :line  {:row 2, :col 8, :end-row 4, :end-col 21}
               :intro "Sample test program",
               :sexp collection/form?
               :test  "\n  (+ 1 1) => 2\n  (long? 3) => true"}))

(fact "analyses metadata-wrapped facts after seedgen scaffolding"
  (let [code (str "(ns sample.generated-test\n"
                  "  (:require [hara.lang :as l])\n"
                  "  (:use code.test))\n\n"
                  "^{:seedgen/root {:all true}}\n"
                  "(l/script- :js {:runtime :basic})\n\n"
                  "(fact:global\n"
                  "  {:setup [(l/rt:restart)]\n"
                  "   :teardown [(l/rt:stop)]})\n\n"
                  "^{:refer sample.generated/foo :added \"1.0\"}\n"
                  "(fact \"keeps generated facts\"\n"
                  "  ^{:seedgen/base {:python {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}}}\n"
                  "  (!.js 1)\n"
                  "  => 1)\n")]
    (-> (framework/analyse-test-code code)
        (get-in '[sample.generated foo])))
  => (contains {:ns 'sample.generated
                :var 'foo
                :meta {:added "1.0"}
                :intro "keeps generated facts"
                :test map?}))


^{:refer code.framework.test.fact/top-level-fact-navs :added "4.1"}
(fact "collects top-level `fact` and `comment` navigators from a parsed root"
  (->> "^{:refer a/b :added \"1\"}
        (fact \"hi\" 1 => 1)
        (comment x y)
        (+ 1 2)
        (fact \"two\" 2 => 2)"
       nav/parse-root
       nav/down
       top-level-fact-navs
       (map nav/value))
  => '((fact "hi" 1 => 1)
       (comment x y)
       (fact "two" 2 => 2)))

^{:refer code.framework.test.fact/top-level-fact-navs :added "4.1"}
(fact "returns an empty vector for nil input"
  (top-level-fact-navs nil)
  => [])