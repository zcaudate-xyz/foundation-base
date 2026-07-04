(ns hara.runtime.jocl-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.jocl :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

;; Stubs that keep the file loadable when the native OpenCL library is
;; not installed.  When OpenCL is present these macros expand to nothing.
(jocl-env/with-stubs sample)
(jocl-env/with-script-stubs)

;; The script runtime and kernel definition cannot be compiled without
;; OpenCL, so we only emit them when it is available.
(jocl-env/when-available
  (l/script- :c
    {:runtime :jocl
     :test-mode true})

  (defn.c ^{:- [:__kernel :void]
            :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}}
    sample
    ([:__global :const :float :* a
      :__global :const :float :* b
      :__global :float :* c]
     (var :int i := (get-global-id 0))
     (:= (. c [i]) (* (. a [i]) (. b [i]))))))

(fact:global
 {:skip (not (jocl-env/opencl-available?))
  :setup [(l/rt:restart)]
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
