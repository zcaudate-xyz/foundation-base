(ns code.manage.unit.factcheck-test
  (:require [code.manage.unit.factcheck :refer :all]
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
(fact "TODO")

^{:refer code.manage.unit.factcheck/fact-block? :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/top-level-entries :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/child-entries :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/entry-block :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/entry-col :added "4.1"}
(fact "TODO")

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
(fact "TODO")

^{:refer code.manage.unit.factcheck/trim-indent :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/normalise-block-string :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/render-form :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/fact-block-data :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/fact-line :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/render-fact :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/render-checkless-item :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/result-string :added "4.1"}
(fact "formats vector examples through `std.block`"
  (result-string '[{:alpha {:nested-key-one 1
                            :nested-key-two 2}}
                   {:beta [:first-value {:deep-key :deep-value}]}])
  => "[{:alpha {:nested-key-one 1
          :nested-key-two 2}}
 {:beta [:first-value {:deep-key :deep-value}]}]")

^{:refer code.manage.unit.factcheck/render-generated-item :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/factcheck-remove-form-string :added "4.1"}
(fact "removes `=>` expectations from a single fact form"
  (factcheck-remove-form-string (block/parse-first +factcheck-generated+))
  => +factcheck-removed+)

^{:refer code.manage.unit.factcheck/factcheck-remove-string :added "4.1"}
(fact "removes `=>` expectations from all fact forms in a file"
  (factcheck-remove-string +factcheck-generated-file+)
  => +factcheck-removed-file+)

^{:refer code.manage.unit.factcheck/fact-op-form :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/evaluate-fact-op :added "4.1"}
(fact "TODO")

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
(fact "TODO")

^{:refer code.manage.unit.factcheck/factcheck-remove :added "4.1"}
(fact "TODO")

^{:refer code.manage.unit.factcheck/factcheck-generate :added "4.1"}
(fact "TODO")