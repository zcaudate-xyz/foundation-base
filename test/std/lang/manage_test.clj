(ns std.lang.manage-test
  (:require [std.lang.manage :as manage])
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
  [(fn? manage/xtalk-status)
   (fn? manage/xtalk-model-status)
   (fn? manage/xtalk-runtime-status)
   (fn? manage/xtalk-spec-status)
   (fn? manage/xtalk-test-status)]
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

^{:refer std.lang.manage/xtalk-status-fn :added "4.1"}
(fact "status fn returns entry by language"
  (manage/xtalk-status-fn :js nil {:js {:lang :js}} nil)
  => {:lang :js})

^{:refer std.lang.manage/xtalk-model-status-fn :added "4.1"}
(fact "model status fn filters model keys"
  (manage/xtalk-model-status-fn :js nil {:js {:lang :js :model-count 1 :model-forms [:fn] :model-files ["a"] :x 1}} nil)
  => {:lang :js :model-count 1 :model-forms [:fn] :model-files ["a"]})

^{:refer std.lang.manage/xtalk-runtime-status-fn :added "4.1"}
(fact "runtime status fn filters runtime keys"
  (manage/xtalk-runtime-status-fn :js nil {:js {:lang :js :script :js :dispatch '!.js :suffix "js" :runtime-installed? true :runtime-executable? true :x 1}} nil)
  => {:lang :js :script :js :dispatch '!.js :suffix "js" :runtime-installed? true :runtime-executable? true})

^{:refer std.lang.manage/xtalk-spec-status-fn :added "4.1"}
(fact "spec status fn filters spec keys"
  (manage/xtalk-spec-status-fn :js nil {:js {:lang :js :spec-feature-count 10 :spec-implemented 8 :spec-abstract 1 :spec-missing 1 :x 1}} nil)
  => {:lang :js :spec-feature-count 10 :spec-implemented 8 :spec-abstract 1 :spec-missing 1})

^{:refer std.lang.manage/xtalk-test-status-fn :added "4.1"}
(fact "test status fn filters test keys"
  (manage/xtalk-test-status-fn :js nil {:js {:lang :js :test-count 4 :test-forms [:fn] :test-files ["a"] :coverage 1.0 :ready? true :x 1}} nil)
  => {:lang :js :test-count 4 :test-forms [:fn] :test-files ["a"] :coverage 1.0 :ready? true})

^{:refer std.lang.manage/xtalk-status :added "4.1"}
(fact "status task is invokable"
  (fn? manage/xtalk-status)
  => true)

^{:refer std.lang.manage/xtalk-model-status :added "4.1"}
(fact "model status task is invokable"
  (fn? manage/xtalk-model-status)
  => true)

^{:refer std.lang.manage/xtalk-runtime-status :added "4.1"}
(fact "runtime status task is invokable"
  (fn? manage/xtalk-runtime-status)
  => true)

^{:refer std.lang.manage/xtalk-spec-status :added "4.1"}
(fact "spec status task is invokable"
  (fn? manage/xtalk-spec-status)
  => true)

^{:refer std.lang.manage/xtalk-test-status :added "4.1"}
(fact "test status task is invokable"
  (fn? manage/xtalk-test-status)
  => true)

^{:refer std.lang.manage/xtalk-categories-fn :added "4.1"}
(fact "categories fn returns vector"
  (vector? (manage/xtalk-categories-fn nil nil nil nil))
  => true)

^{:refer std.lang.manage/xtalk-op-map-fn :added "4.1"}
(fact "op-map fn returns map"
  (map? (manage/xtalk-op-map-fn nil nil nil nil))
  => true)

^{:refer std.lang.manage/xtalk-symbols-fn :added "4.1"}
(fact "symbols fn returns vector"
  (vector? (manage/xtalk-symbols-fn nil nil nil nil))
  => true)

^{:refer std.lang.manage/installed-languages-fn :added "4.1"}
(fact "installed languages fn returns vector"
  (vector? (manage/installed-languages-fn nil nil nil nil))
  => true)

^{:refer std.lang.manage/audit-languages-fn :added "4.1"}
(fact "audit languages fn returns vector"
  (vector? (manage/audit-languages-fn nil {} nil nil))
  => true)

^{:refer std.lang.manage/support-matrix-fn :added "4.1"}
(fact "support matrix fn returns map"
  (map? (manage/support-matrix-fn nil {} nil nil))
  => true)

^{:refer std.lang.manage/missing-by-language-fn :added "4.1"}
(fact "missing-by-language fn returns map"
  (map? (manage/missing-by-language-fn nil {} nil nil))
  => true)

^{:refer std.lang.manage/missing-by-feature-fn :added "4.1"}
(fact "missing-by-feature fn returns map"
  (map? (manage/missing-by-feature-fn nil {} nil nil))
  => true)

^{:refer std.lang.manage/visualize-support-fn :added "4.1"}
(fact "visualize support fn returns string"
  (string? (manage/visualize-support-fn nil {} nil nil))
  => true)

^{:refer std.lang.manage/generate-xtalk-ops-fn :added "4.1"}
(fact "generate xtalk ops fn is available"
  (fn? manage/generate-xtalk-ops-fn)
  => true)

^{:refer std.lang.manage/scaffold-xtalk-grammar-tests-fn :added "4.1"}
(fact "scaffold grammar tests fn is available"
  (fn? manage/scaffold-xtalk-grammar-tests-fn)
  => true)

^{:refer std.lang.manage/separate-runtime-tests-fn :added "4.1"}
(fact "separate runtime tests fn is available"
  (fn? manage/separate-runtime-tests-fn)
  => true)

^{:refer std.lang.manage/scaffold-runtime-template-fn :added "4.1"}
(fact "scaffold runtime template fn is available"
  (fn? manage/scaffold-runtime-template-fn)
  => true)

^{:refer std.lang.manage/xtalk-categories :added "4.1"}
(fact "xtalk-categories task is available"
  (fn? manage/xtalk-categories)
  => true)

^{:refer std.lang.manage/xtalk-op-map :added "4.1"}
(fact "xtalk-op-map task is available"
  (fn? manage/xtalk-op-map)
  => true)

^{:refer std.lang.manage/xtalk-symbols :added "4.1"}
(fact "xtalk-symbols task is available"
  (fn? manage/xtalk-symbols)
  => true)

^{:refer std.lang.manage/installed-languages :added "4.1"}
(fact "installed-languages task is available"
  (fn? manage/installed-languages)
  => true)

^{:refer std.lang.manage/audit-languages :added "4.1"}
(fact "audit-languages task is available"
  (fn? manage/audit-languages)
  => true)

^{:refer std.lang.manage/support-matrix :added "4.1"}
(fact "support-matrix task is available"
  (fn? manage/support-matrix)
  => true)

^{:refer std.lang.manage/missing-by-language :added "4.1"}
(fact "missing-by-language task is available"
  (fn? manage/missing-by-language)
  => true)

^{:refer std.lang.manage/missing-by-feature :added "4.1"}
(fact "missing-by-feature task is available"
  (fn? manage/missing-by-feature)
  => true)

^{:refer std.lang.manage/visualize-support :added "4.1"}
(fact "visualize-support task is available"
  (fn? manage/visualize-support)
  => true)

^{:refer std.lang.manage/generate-xtalk-ops :added "4.1"}
(fact "generate-xtalk-ops task is available"
  (fn? manage/generate-xtalk-ops)
  => true)

^{:refer std.lang.manage/scaffold-xtalk-grammar-tests :added "4.1"}
(fact "scaffold-xtalk-grammar-tests task is available"
  (fn? manage/scaffold-xtalk-grammar-tests)
  => true)

^{:refer std.lang.manage/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests task is available"
  (fn? manage/separate-runtime-tests)
  => true)

^{:refer std.lang.manage/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template task is available"
  (fn? manage/scaffold-runtime-template)
  => true)
