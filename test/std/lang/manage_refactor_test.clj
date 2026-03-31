(ns std.lang.manage-refactor-test
  (:require [std.lang.manage :as manage]
            [std.lang.manage.xtalk-audit :as audit]
            [std.lang.manage.xtalk-ops :as xtalk-ops]
            [std.lang.manage.xtalk-scaffold :as scaffold])
  (:use code.test))

^{:refer std.lang.manage.xtalk-audit/xtalk-op-map :added "4.1"}
(fact "direct audit functions support task-style invocation without std.lang.manage wrappers"
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
(fact "direct ops and scaffold functions remain callable in task-style arity"
  [(map? (xtalk-ops/generate-xtalk-ops nil {:write false} nil nil))
   (fn? scaffold/scaffold-xtalk-grammar-tests)
   (fn? scaffold/separate-runtime-tests)
   (fn? scaffold/scaffold-runtime-template)
   (fn? scaffold/export-runtime-suite)
   (fn? scaffold/compile-runtime-bulk)]
  => [true true true true true true])

^{:refer std.lang.manage/xtalk-op-map :added "4.1"}
(fact "std.lang.manage tasks still run after wrapper cleanup"
  [(map? (manage/xtalk-op-map {:print {:item false
                                       :result false
                                       :summary false}}))
   (map? (manage/generate-xtalk-ops {:print {:item false
                                             :result false
                                             :summary false}
                                     :write false}))]
  => [true true])
