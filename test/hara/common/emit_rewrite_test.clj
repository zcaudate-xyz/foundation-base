tahto/common/emit_rewrite_test.clj:1:(ns tahto.common.emit-rewrite-test
  (:use code.test)
tahto/common/emit_rewrite_test.clj:3:  (:require [tahto.common.emit :as emit]
tahto/common/emit_rewrite_test.clj:4:            [tahto.common.emit-rewrite :refer :all]
tahto/common/emit_rewrite_test.clj:5:            [tahto.typed.xtalk-analysis :as analysis]
tahto/common/emit_rewrite_test.clj:6:            [tahto.typed.xtalk-infer :as infer]))

tahto/common/emit_rewrite_test.clj:8:^{:refer tahto.common.emit-rewrite/stage-transforms :added "4.1"}
(fact "returns the transforms registered for a stage"
  (stage-transforms {:rewrite {:staging [inc]
                               :emit    [dec]}}
                     :staging)
  => [inc]

  (stage-transforms {:rewrite {:emit [dec]}}
                     :staging)
  => [])

tahto/common/emit_rewrite_test.clj:19:^{:refer tahto.common.emit-rewrite/canonical-stage :added "4.1"}
(fact "only applies typed canonical lowering when requested"
  [(canonical-stage '(. arr [i])
tahto/common/emit_rewrite_test.clj:22:                    {:mopts {:tahto/xtalk-context
                             {:infer infer/infer-type
                              :env '{arr {:kind :array
                                          :item {:kind :primitive :name :xt/int}}}}}})
   (canonical-stage '(. arr [i]) {:mopts {}})]
  => ['(x:get-idx arr i)
      '(. arr [i])])

tahto/common/emit_rewrite_test.clj:30:^{:refer tahto.common.emit-rewrite/canonical-stage :id canonical-stage-module-context :added "4.1"}
(fact "infers local bindings before lowering module forms"
  (analysis/analyze-and-register! 'xt.event.base-route)
  (canonical-stage
   '(do
      (var r (route/make-route))
      (. (. r ["listeners"]) ["a1"]))
   {:mopts {:lang :python
            :module {:id 'xt.event.base-route-test
                     :alias {'route 'xt.event.base-route}}}})
  => '(do
       (var r (xt.event.base-route/make-route))
       (x:get-key (x:get-key r "listeners") "a1")))

tahto/common/emit_rewrite_test.clj:44:^{:refer tahto.common.emit-rewrite/canonical-stage :id canonical-stage-xtalk-entry :added "4.1"}
(fact "runs canonical lowering for XTalk entries"
  (canonical-stage '(. value [key])
                   {:mopts {:entry {:lang :xtalk}}})
  => '(. value [key]))

tahto/common/emit_rewrite_test.clj:50:^{:refer tahto.common.emit-rewrite/rewrite-stage :added "4.1"}
(fact "applies each transform for a stage to the form"
  (rewrite-stage :staging
                 1
                 {:rewrite {:staging [(fn [form _] (inc form))
                                      (fn [form _] (inc form))]}}
                 {})
  => 3

  (rewrite-stage :emit
                 5
                 {:rewrite {:emit [(fn [form _] (* form 2))
                                   (fn [form _] (+ form 1))]}}
                 {})
  => 11

  (contains? (set (stage-transforms (emit/default-grammar) :canonical))
             #'canonical-stage)
  => true)
