(ns std.lang.manage-test
  (:require [std.lang.manage :as manage]
            [std.lang.manage.xtalk-audit :as audit]
            [std.lang.manage.xtalk-ops :as xtalk-ops]
            [std.lang.manage.xtalk-scaffold :as scaffold]
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

^{:refer std.lang.manage.xtalk-audit/xtalk-op-map :added "4.1"}
(fact "audit functions can be used directly as 4-arity task fns"
  [(vector? (audit/xtalk-categories nil nil nil nil))
   (map? (audit/xtalk-op-map nil nil nil nil))
   (vector? (audit/xtalk-symbols nil nil nil nil))
   (vector? (audit/audit-languages nil {} nil nil))
   (map? (audit/support-matrix nil {} nil nil))
   (map? (audit/missing-by-language nil {} nil nil))
   (map? (audit/missing-by-feature nil {} nil nil))
   (string? (audit/visualize-support nil {} nil nil))]
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-ops/generate-xtalk-ops :added "4.1"}
(fact "ops generation supports direct task-style invocation"
  (map? (xtalk-ops/generate-xtalk-ops nil {:write false} nil nil))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/export-runtime-suite :added "4.1"}
(fact "scaffold functions remain directly available after wrapper cleanup"
  [(fn? scaffold/scaffold-xtalk-grammar-tests)
   (fn? scaffold/separate-runtime-tests)
   (fn? scaffold/scaffold-runtime-template)
   (fn? scaffold/export-runtime-suite)
   (fn? scaffold/compile-runtime-bulk)]
  => [true true true true true])

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

^{:refer std.lang.manage/runtime-template-matrix :added "4.1"}
(fact "runtime-template-matrix reports scaffold status for selected templates"
  (let [{:keys [templates languages status summary lang-summary scaffold-summary]}
        (manage/runtime-template-matrix {:nss '[xt.lang.common-iter-test
                                               xt.lang.common-string-test
                                               xt.lang.spec-base-test]
                                         :langs [:js :lua :python :dart]})]
    [(= (set templates)
        #{'xt.lang.common-iter-test
          'xt.lang.common-string-test
          'xt.lang.spec-base-test})
     (= languages [:dart :js :lua :python])
     (= #{:ready :unsupported}
        (set (for [source-ns templates
                   lang languages]
               (get-in status [source-ns lang :status]))))
     (= (set (keys summary))
        (set templates))
     (= {:full 4 :partial 0 :stub 0 :unwritten 0 :unsupported 0 :scaffold-error 0 :diagnose-error 0}
         (get summary 'xt.lang.common-iter-test))
     (= {:full 1 :partial 0 :stub 0 :unwritten 0 :unsupported 0 :scaffold-error 0 :diagnose-error 0}
         (get lang-summary :python))
     (= 0 (get-in scaffold-summary ['xt.lang.spec-base-test :ready]))])
  => [true true true true true true true])

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

^{:refer std.lang.manage/scaffold-xtalk-grammar-tests :added "4.1"}
(fact "scaffold-xtalk-grammar-tests task is available"
  (task/task? manage/scaffold-xtalk-grammar-tests)
  => true)

^{:refer std.lang.manage/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests task is available"
  (task/task? manage/separate-runtime-tests)
  => true)

^{:refer std.lang.manage/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template task is available"
  (task/task? manage/scaffold-runtime-template)
  => true)


^{:refer std.lang.manage/compile-runtime-bulk :added "4.1"}
(fact "compile-runtime-bulk is a task for compiling runtime EDN suites"
  (task/task? manage/compile-runtime-bulk)
  => true)

^{:refer std.lang.manage/-main :added "4.1"}
(fact "main entry point lists available tasks when called with no args"
  (string? (with-out-str (manage/-main)))
  => true)
