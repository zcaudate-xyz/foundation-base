(ns code.manage.unit.template-test
  (:use code.test)
  (:require [code.manage.unit.template :refer :all]
            [code.project :as project]))

^{:refer code.manage.unit.template/source-namespaces :added "3.0"}
(fact "returns all source namespaces"

  (count (source-namespaces {} (project/project)))
  ;; 358
  => number?)

^{:refer code.manage.unit.template/test-namespaces :added "3.0"}
(fact "returns all test namespaces"

  (count (test-namespaces {} (project/project)))
  ;;321
  => number?)

^{:refer code.manage.unit.template/empty-status :added "3.0"}
(fact "constructs a function that outputs on empty"

  ((empty-status :warn :empty) [])
  ;;#result.warn{:data :empty}
  => std.lib.result.Result)

^{:refer code.manage.unit.template/code-default-columns :added "3.0"}
(fact "creates columns for default code operations"
  (code-default-columns #{:red})
  => vector?)

^{:refer code.manage.unit.template/empty-result :added "3.0"}
(fact "constructs a function that outputs on an empty key"

  ((empty-result :data :warn :empty)
   {:data []})
  ;;#result.warn{:data :empty}
  => std.lib.result.Result)

^{:refer code.manage.unit.template/code-transform-result :added "3.0"}
(fact "creates result for code transform operations"
  (code-transform-result :k)
  => (contains {:keys (contains {:count fn? :functions :k})
                :ignore fn?}))

^{:refer code.manage.unit.template/code-transform-columns :added "3.0"}
(fact "creates columns for code transform operations"
  (code-transform-columns #{:red})
  => seq?)

^{:refer code.manage.unit.template/line-string :added "3.0"}
(fact "constructs a line string"
  (line-string [{:row 1 :end-row 2}])
  => ["(1-2)"])

^{:refer code.manage.unit.template/line-count :added "3.0"}
(fact "calculates number of lines"
  (line-count [{:row 1 :end-row 2}])
  => 2)

(comment
  (code.manage/import {:write true}))
