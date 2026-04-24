(ns code.manage.unit.factcheck-test
  (:require [code.manage.unit.factcheck :refer :all]
            [code.test.base.runtime :as rt]
            [std.block :as block])
  (:use code.test))

(def +factcheck-generated+
  "^{:id factcheck-sample}
(fact \"sample generation fact\"

  (+ 1 1)
  => 2

  (mapv inc [1 2])
  => [2 3])")

(def +factcheck-removed+
  "^{:id factcheck-sample}
(fact \"sample generation fact\"

  (+ 1 1)

  (mapv inc [1 2]))")

(def +factcheck-generated-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +factcheck-generated+))

(def +factcheck-removed-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +factcheck-removed+))

^{:id factcheck-sample}
(fact "sample generation fact"

  (+ 1 1)
  => 2

  (mapv inc [1 2])
  => [2 3])

^{:refer code.manage.unit.factcheck/parse-body :added "4.1"}
(fact "parses checks only when an expectation is present"
  (->> (parse-body (->> (block/parse-first "(fact \"hello\" (+ 1 1) =>)")
                        child-entries
                        (drop 2)))
       (mapv (fn [{:keys [type expr expected]}]
               {:type type
                :expr (some-> expr entry-block block/string)
                :expected (some-> expected entry-block block/string)})))
  => [{:type :form
       :expr "(+ 1 1)"
       :expected nil}
      {:type :form
       :expr "=>"
       :expected nil}])

^{:refer code.manage.unit.factcheck/parse-body :added "4.1"}
(fact "does not treat nested `=>` symbols as top-level checks"
  (->> (parse-body (->> (block/parse-first "(fact \"hello\" (vector '=> 1) (+ 1 1))")
                        child-entries
                        (drop 2)))
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

^{:refer code.manage.unit.factcheck/factcheck-remove-form-string :added "4.1"}
(fact "removes `=>` expectations from a single fact form"
  (factcheck-remove-form-string (block/parse-first +factcheck-generated+))
  => +factcheck-removed+)

^{:refer code.manage.unit.factcheck/factcheck-remove-string :added "4.1"}
(fact "removes `=>` expectations from all fact forms in a file"
  (factcheck-remove-string +factcheck-generated-file+)
  => +factcheck-removed-file+)

^{:refer code.manage.unit.factcheck/fact-result-values :added "4.1"}
(fact "evaluates compiled fact ops sequentially"
  (fact-result-values (rt/get-fact 'code.manage.unit.factcheck-test 'factcheck-sample))
  => [2 [2 3]])

^{:refer code.manage.unit.factcheck/factcheck-generate-form-string :added "4.1"}
(fact "generates `=>` expectations for a single fact form"
  (factcheck-generate-form-string
   (block/parse-first +factcheck-removed+)
   (fact-result-values (rt/get-fact 'code.manage.unit.factcheck-test 'factcheck-sample)))
  => +factcheck-generated+)

^{:refer code.manage.unit.factcheck/factcheck-generate-string :added "4.1"}
(fact "generates `=>` expectations for all fact forms in a file"
  (factcheck-generate-string +factcheck-removed-file+
                             {5 [2 [2 3]]})
  => +factcheck-generated-file+)
