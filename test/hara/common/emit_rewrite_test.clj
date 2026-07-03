(ns hara.common.emit-rewrite-test
  (:use code.test)
  (:require [hara.common.emit-rewrite :refer :all]))

^{:refer hara.common.emit-rewrite/stage-transforms :added "4.1"}
(fact "returns the transforms registered for a stage"
  (stage-transforms {:rewrite {:staging [inc]
                               :emit    [dec]}}
                     :staging)
  => [inc]

  (stage-transforms {:rewrite {:emit [dec]}}
                     :staging)
  => [])

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
  => 11)