(ns hara.runtime.jocl-test
  (:use code.test)
  (:require [hara.runtime.jocl]
            [hara.lang :as l]))

(l/script- :c
  {:runtime :jocl})

(defn.c ^{:- [:__kernel :void]
          :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}}
  sample
  ([:__global :const :float :* a
    :__global :const :float :* b
    :__global :float :* c]
   (var :int i := (get-global-id 0))
   (:= (. c [i]) (* (. a [i]) (. b [i])))))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.jocl/CANARY :adopt true :added "4.0"
  :setup [(l/rt:restart :c)]}
(fact "Basic usage for JOCL"

  @(:state (l/rt :c))
  => nil
  
  (let [a (float-array [1 2 3])
        b (float-array [1 2 3])
        c (float-array 3)]
    (sample a b c)
    (vec c))
  => [1.0 4.0 9.0]

  @(:state (l/rt :c))
  => map?)


