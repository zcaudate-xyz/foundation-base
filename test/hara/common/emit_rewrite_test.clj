(ns hara.common.emit-rewrite-test
  (:use code.test)
  (:require [hara.common.emit :as emit]
            [hara.common.emit-rewrite :refer :all]
            [hara.typed.xtalk-analysis :as analysis]
            [hara.typed.xtalk-infer :as infer]))

^{:refer hara.common.emit-rewrite/stage-transforms :added "4.1"}
(fact "returns the transforms registered for a stage"
  (stage-transforms {:rewrite {:staging [inc]
                               :emit    [dec]}}
                     :staging)
  => [inc]

  (stage-transforms {:rewrite {:emit [dec]}}
                     :staging)
  => [])

^{:refer hara.common.emit-rewrite/canonical-stage :added "4.1"}
(fact "only applies typed canonical lowering when requested"
  [(canonical-stage '(. arr [i])
                    {:mopts {:hara/xtalk-context
                             {:infer infer/infer-type
                              :env '{arr {:kind :array
                                          :item {:kind :primitive :name :xt/int}}}}}})
   (canonical-stage '(. arr [i]) {:mopts {}})]
  => ['(x:get-idx arr i)
      '(. arr [i])])

^{:refer hara.common.emit-rewrite/canonical-stage :id canonical-stage-module-context :added "4.1"}
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

^{:refer hara.common.emit-rewrite/canonical-stage :id canonical-stage-xtalk-entry :added "4.1"}
(fact "runs canonical lowering for XTalk entries"
  (canonical-stage '(. value [key])
                   {:mopts {:entry {:lang :xtalk}}})
  => '(. value [key]))

^{:refer hara.common.emit-rewrite/rewrite-stage :added "4.1"}
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
