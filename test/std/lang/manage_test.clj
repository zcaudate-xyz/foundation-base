(ns std.lang.manage-test
  (:require [std.lang.manage :as manage]
            [std.task :as task])
  (:use code.test))

(fact "xtalk inventory interfaces return language keyed maps"
  [(map? (manage/xtalk-model-inventory))
   (map? (manage/xtalk-runtime-inventory))
   (map? (manage/xtalk-spec-inventory))
   (map? (manage/xtalk-test-inventory))
   (map? (manage/xtalk-language-status))
   (map? (manage/xtalk-coverage-summary))]
  => [true true true true true true])

(fact "xtalk manage exposes task-pluggable interfaces"
  [(task/task? manage/xtalk-status)
   (task/task? manage/xtalk-model-status)
   (task/task? manage/xtalk-runtime-status)
   (task/task? manage/xtalk-spec-status)
   (task/task? manage/xtalk-test-status)]
  => [true true true true true])

(fact "xtalk status tasks run through std.task"
  (let [result (manage/xtalk-status :all {:print {:item false
                                                   :result false
                                                   :summary false}})]
    (coll? result))
  => true)

^{:refer std.lang.manage/xtalk-model-inventory :added "4.1"}
(fact "returns model inventory map"
  (map? (manage/xtalk-model-inventory))
  => true)

^{:refer std.lang.manage/xtalk-test-inventory :added "4.1"}
(fact "returns test inventory map"
  (map? (manage/xtalk-test-inventory))
  => true)

^{:refer std.lang.manage/xtalk-runtime-inventory :added "4.1"}
(fact "returns runtime inventory map"
  (map? (manage/xtalk-runtime-inventory))
  => true

  (get-in (manage/xtalk-runtime-inventory {:langs [:php]})
          [:php :runtime-executable?])
  => true)

^{:refer std.lang.manage/xtalk-spec-inventory :added "4.1"}
(fact "returns spec inventory map"
  (map? (manage/xtalk-spec-inventory))
  => true)

^{:refer std.lang.manage/xtalk-language-status :added "4.1"}
(fact "returns merged language status map"
  (map? (manage/xtalk-language-status))
  => true)

^{:refer std.lang.manage/xtalk-coverage-summary :added "4.1"}
(fact "returns coverage summary map"
  (map? (manage/xtalk-coverage-summary))
  => true)

^{:refer std.lang.manage/xtalk-status :added "4.1"}
(fact "status task is invokable"
  (task/task? manage/xtalk-status)
  => true)

^{:refer std.lang.manage/xtalk-model-status :added "4.1"}
(fact "model status task is invokable"
  (task/task? manage/xtalk-model-status)
  => true)

^{:refer std.lang.manage/xtalk-runtime-status :added "4.1"}
(fact "runtime status task is invokable"
  (task/task? manage/xtalk-runtime-status)
  => true)

^{:refer std.lang.manage/xtalk-spec-status :added "4.1"}
(fact "spec status task is invokable"
  (task/task? manage/xtalk-spec-status)
  => true)

^{:refer std.lang.manage/xtalk-test-status :added "4.1"}
(fact "test status task is invokable"
  (task/task? manage/xtalk-test-status)
  => true)

^{:refer std.lang.manage/xtalk-categories :added "4.1"}
(fact "xtalk-categories task is available"
  (task/task? manage/xtalk-categories)
  => true)

^{:refer std.lang.manage/xtalk-op-map :added "4.1"}
(fact "xtalk-op-map task is available"
  (task/task? manage/xtalk-op-map)
  => true)

^{:refer std.lang.manage/xtalk-symbols :added "4.1"}
(fact "xtalk-symbols task is available"
  (task/task? manage/xtalk-symbols)
  => true)

^{:refer std.lang.manage/installed-languages :added "4.1"}
(fact "installed-languages task is available"
  (task/task? manage/installed-languages)
  => true)

^{:refer std.lang.manage/audit-languages :added "4.1"}
(fact "audit-languages task is available"
  (task/task? manage/audit-languages)
  => true)

^{:refer std.lang.manage/support-matrix :added "4.1"}
(fact "support-matrix task is available"
  (task/task? manage/support-matrix)
  => true)

^{:refer std.lang.manage/missing-by-language :added "4.1"}
(fact "missing-by-language task is available"
  (task/task? manage/missing-by-language)
  => true)

^{:refer std.lang.manage/missing-by-feature :added "4.1"}
(fact "missing-by-feature task is available"
  (task/task? manage/missing-by-feature)
  => true)

^{:refer std.lang.manage/visualize-support :added "4.1"}
(fact "visualize-support task is available"
  (task/task? manage/visualize-support)
  => true)

^{:refer std.lang.manage/generate-xtalk-ops :added "4.1"}
(fact "generate-xtalk-ops task is available"
  (task/task? manage/generate-xtalk-ops)
  => true)
