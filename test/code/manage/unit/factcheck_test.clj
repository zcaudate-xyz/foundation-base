(ns code.manage.unit.factcheck-test
  (:require [code.framework :as base]
            [code.manage.unit.factcheck :refer :all]
            [code.test.base.executive :as executive]
            [code.test.compile :as compile]
            [code.test.base.runtime :as rt]
            [std.block :as block])
  (:use code.test))

(def ^:private +fact-body-offset+
  2)

(def ^:private +sample-fact-id+
  'factcheck-sample-data)

(def ^:private +sample-fact-body+
  '((+ 1 1) => 2
    (mapv inc [1 2]) => [2 3]))

(def ^:private +factcheck-generated+
  "^{:id factcheck-sample}
(fact \"sample generation fact\"

  (+ 1 1)
  => 2

  (mapv inc [1 2])
  => [2 3])")

(def ^:private +factcheck-removed+
  "^{:id factcheck-sample}
(fact \"sample generation fact\"

  (+ 1 1)

  (mapv inc [1 2]))")

(def ^:private +factcheck-generated-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +factcheck-generated+))

(def ^:private +factcheck-removed-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +factcheck-removed+))

(defn with-sample-fpkg
  [test-fn]
  (let [fpkg (compile/create-fact {:ns 'code.manage.unit.factcheck-test
                                   :id +sample-fact-id+
                                   :path "test/code/manage/unit/factcheck_test.clj"
                                   :desc "sample generation fact"}
                                  +sample-fact-body+)]
    (rt/set-fact (:ns fpkg) (:id fpkg) fpkg)
    (try
      (test-fn fpkg)
      (finally
        (rt/remove-fact (:ns fpkg) (:id fpkg))))))

^{:refer code.manage.unit.factcheck/unwrap-fact-block :added "4.1"}
(fact "returns the metadata prefix and inner fact block"
  (let [{:keys [prefix block]} (unwrap-fact-block (block/parse-first +factcheck-generated+))]
    [prefix (block/string block)])
  => ["^{:id factcheck-sample}\n"
      "(fact \"sample generation fact\"\n\n  (+ 1 1)\n  => 2\n\n  (mapv inc [1 2])\n  => [2 3])"])

^{:refer code.manage.unit.factcheck/fact-block? :added "4.1"}
(fact "recognises fact forms and rejects other blocks"
  [(boolean (fact-block? (block/parse-first +factcheck-generated+)))
   (fact-block? (block/parse-first "(+ 1 1)"))
   (fact-block? (block/parse-first "[1 2 3]"))]
  => [true nil false])

^{:refer code.manage.unit.factcheck/top-level-entries :added "4.1"}
(fact "returns top-level blocks with line info"
  (->> (top-level-entries +factcheck-generated-file+)
       (mapv (juxt (comp block/string :block) :line)))
  => [["(ns example.core-test\n  (:use code.test))"
       {:row 1 :col 1 :end-row 2 :end-col 20}]
      [+factcheck-generated+
       {:row 4 :col 1 :end-row 11 :end-col 12}]])

^{:refer code.manage.unit.factcheck/child-entries :added "4.1"}
(fact "returns non-void children with their start column"
  (->> (-> (block/parse-first +factcheck-generated+)
           unwrap-fact-block
           :block
           child-entries)
       (mapv (fn [{:keys [block col]}]
               [(block/string block) col])))
  => [["fact" 2]
      ["\"sample generation fact\"" 7]
      ["(+ 1 1)" 3]
      ["=>" 3]
      ["2" 6]
      ["(mapv inc [1 2])" 3]
      ["=>" 3]
      ["[2 3]" 6]])

^{:refer code.manage.unit.factcheck/entry-block :added "4.1"}
(fact "extracts the block from an entry map or returns the value"
  (-> (entry-block {:block (block/parse-first "(+ 1 1)") :col 3})
      block/string)
  => "(+ 1 1)"
  
  (-> (entry-block (block/parse-first "(+ 1 1)"))
      block/string)
  => "(+ 1 1)")

^{:refer code.manage.unit.factcheck/entry-col :added "4.1"}
(fact "extracts the column from an entry map or defaults to 1"
  (entry-col {:block (block/parse-first "(+ 1 1)") :col 3})
  => 3
  
  (entry-col (block/parse-first "(+ 1 1)"))
  => 1)

^{:refer code.manage.unit.factcheck/parse-body :added "4.1"}
(fact "does not treat nested `=>` symbols as top-level checks"
  (->> (parse-body (->> (block/parse-first "(fact \"hello\" (vector '=> 1) (+ 1 1))")
                        child-entries
                        (drop +fact-body-offset+)))
       (mapv (fn [{:keys [type expr expected]}]
               {:type type
                :expr (some-> expr entry-block block/string)
                :expected (some-> expected entry-block block/string)})))
  => [{:type :form
       :expr "(vector '=> 1)"
       :expected nil}
      {:type :form
       :expr "(+ 1 1)"
       :expected nil}])

^{:refer code.manage.unit.factcheck/leading-indent :added "4.1"}
(fact "counts leading spaces and tabs"
  [(leading-indent "    hello")
   (leading-indent "\thello")
   (leading-indent "hello")
   (leading-indent "  world")]
  => [4 1 0 2])

^{:refer code.manage.unit.factcheck/trim-indent :added "4.1"}
(fact "removes up to n leading whitespace characters"
  [(trim-indent "    hello" 2)
   (trim-indent "  hello" 4)
   (trim-indent "hello" 2)
   (trim-indent "   hello" 3)]
  => ["  hello" "hello" "hello" "hello"])

^{:refer code.manage.unit.factcheck/normalise-block-string :added "4.1"}
(fact "normalises indentation of subsequent lines"
  (normalise-block-string "hello\n    world\n    foo" 2)
  => "hello\n  world\n  foo"
  
  (normalise-block-string "single")
  => "single"
  
  (normalise-block-string "a\n\n  b" 2)
  => "a\n\nb")

^{:refer code.manage.unit.factcheck/render-form :added "4.1"}
(fact "formats a block relative to its start column"
  (let [entry (->> (block/parse-first "(fact \"iterates arrays in order\"\n  (!.js\n    (var out [])\n    out))")
                   child-entries
                   (drop 2)
                   first)]
    (render-form entry))
  => "(!.js\n  (var out [])\n  out)"
  
  (->> (block/parse-first "(fact \"sample\"\n  (+ 1 1))")
       child-entries
       (drop 2)
       first
       render-form)
  => "(+ 1 1)")

^{:refer code.manage.unit.factcheck/fact-block-data :added "4.1"}
(fact "returns the logical structure of a fact form"
  (let [{:keys [prefix op intro items]} (fact-block-data +factcheck-generated+)]
    [prefix
     (block/string (entry-block op))
     (block/string (entry-block intro))
     (mapv (fn [{:keys [type expr expected]}]
             {:type type
              :expr (block/string (entry-block expr))
              :expected (block/string (entry-block expected))})
           items)])
  => ["^{:id factcheck-sample}\n"
      "fact"
      "\"sample generation fact\""
      [{:type :check :expr "(+ 1 1)" :expected "2"}
       {:type :check :expr "(mapv inc [1 2])" :expected "[2 3]"}]])

^{:refer code.manage.unit.factcheck/fact-line :added "4.1"}
(fact "returns the line where the inner fact form starts"
  (->> (top-level-entries +factcheck-generated-file+)
       (mapv fact-line))
  => [1 5])

^{:refer code.manage.unit.factcheck/render-fact :added "4.1"}
(fact "renders a fact with formatted body items"
  (let [form (block/parse-first +factcheck-generated+)
        items (-> form fact-block-data :items)]
    (render-fact form (mapv render-checkless-item items)))
  => +factcheck-removed+)

^{:refer code.manage.unit.factcheck/render-checkless-item :added "4.1"}
(fact "renders a fact item without its expectation"
  (let [item (-> (block/parse-first "(fact \"sample\"\n  (+ 1 1)\n  => 2)")
                 fact-block-data
                 :items
                 first)]
    (render-checkless-item item))
  => "  (+ 1 1)")

^{:refer code.manage.unit.factcheck/result-string :added "4.1"}
(fact "formats vector examples through `std.block`"
  (result-string '[{:alpha {:nested-key-one 1
                            :nested-key-two 2}}
                   {:beta [:first-value {:deep-key :deep-value}]}])
  => "[{:alpha {:nested-key-one 1\n          :nested-key-two 2}}\n {:beta [:first-value {:deep-key :deep-value}]}]")

^{:refer code.manage.unit.factcheck/render-generated-item :added "4.1"}
(fact "renders a fact item with its generated expectation"
  (let [item (-> (block/parse-first "(fact \"sample\"\n  (+ 1 1))")
                 fact-block-data
                 :items
                 first)]
    (render-generated-item item 2))
  => "  (+ 1 1)\n  => 2")

^{:refer code.manage.unit.factcheck/factcheck-remove-form-string :added "4.1"}
(fact "removes `=>` expectations from a single fact form"
  (factcheck-remove-form-string (block/parse-first +factcheck-generated+))
  => +factcheck-removed+)

^{:refer code.manage.unit.factcheck/factcheck-remove-string :added "4.1"}
(fact "removes `=>` expectations from all fact forms in a file"
  (factcheck-remove-string +factcheck-generated-file+)
  => +factcheck-removed-file+)

^{:refer code.manage.unit.factcheck/fact-op-form :added "4.1"}
(fact "returns the input form for supported op types"
  [(fact-op-form {:type :test-equal :input {:form '(+ 1 1)}})
   (fact-op-form {:type :form :form '(mapv inc [1 2])})]
  => ['(+ 1 1) '(mapv inc [1 2])])

^{:refer code.manage.unit.factcheck/evaluate-fact-op :added "4.1"}
(fact "evaluates a compiled fact op and returns its value"
  (evaluate-fact-op 'user :sample {:type :form :form '(+ 1 2 3) :meta {:ns 'user}})
  => 6)

^{:refer code.manage.unit.factcheck/fact-result-values :added "4.1"}
(fact "evaluates compiled fact ops sequentially"
  (with-sample-fpkg fact-result-values)
  => [2 [2 3]])

^{:refer code.manage.unit.factcheck/factcheck-generate-form-string :added "4.1"}
(fact "generates multiline expectations with `std.block` formatting"
  (factcheck-generate-form-string
   (block/parse-first "(fact \"sample\"\n\n  (identity :value))")
   ['[{:alpha {:nested-key-one 1
               :nested-key-two 2}}
      {:beta [:first-value {:deep-key :deep-value}]}]])
  => (str "(fact \"sample\"\n\n"
           "  (identity :value)\n"
           "  => [{:alpha {:nested-key-one 1\n"
           "               :nested-key-two 2}}\n"
           "      {:beta [:first-value {:deep-key :deep-value}]}])"))

^{:refer code.manage.unit.factcheck/factcheck-generate-string :added "4.1"}
(fact "generates `=>` expectations for all fact forms in a file"
  (factcheck-generate-string +factcheck-removed-file+
                             {5 [2 [2 3]]})
  => +factcheck-generated-file+)

^{:refer code.manage.unit.factcheck/fact-results-map :added "4.1"}
(fact "evaluates all facts in a namespace and indexes them by line"
  (let [fpkg (compile/create-fact {:ns 'user
                                   :id 'factcheck-results-map-sample
                                   :path "test.clj"
                                   :desc "sample"
                                   :line 7}
                                  '((+ 1 1) => 2))]
    (with-redefs [rt/get-global (fn [& _] nil)
                  rt/all-facts (fn [ns] {(:id fpkg) fpkg})]
      (fact-results-map 'user)))
  => {7 [2]})

^{:refer code.manage.unit.factcheck/factcheck-remove :added "4.1"}
(fact "delegates to transform-code with the remove transformer"
  (with-redefs [base/transform-code (fn [ns params lookup project]
                                      {:ns ns :transform (:transform params)})]
    (factcheck-remove 'code.manage.unit.factcheck
                      {}
                      (constantly "test/code/manage/unit/factcheck_test.clj")
                      {:root "."}))
  => {:ns 'code.manage.unit.factcheck-test
      :transform factcheck-remove-string})

^{:refer code.manage.unit.factcheck/factcheck-generate :added "4.1"}
(fact "delegates to transform-code with a generate transformer"
  (with-redefs [executive/load-namespace (fn [& _] nil)
                executive/unload-namespace (fn [& _] nil)
                fact-results-map (fn [test-ns] {5 [2 [2 3]]})
                base/transform-code (fn [ns params lookup project]
                                      {:ns ns
                                       :result ((:transform params) +factcheck-removed-file+)})]
    (factcheck-generate 'code.manage.unit.factcheck
                        {}
                        (constantly "test/code/manage/unit/factcheck_test.clj")
                        {:root "."}))
  => {:ns 'code.manage.unit.factcheck-test
      :result +factcheck-generated-file+})
