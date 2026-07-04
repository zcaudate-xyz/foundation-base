(ns hara.runtime.jocl.runtime-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [hara.lang :as l]
            [std.lib.component :as component]
            [std.lib.context.pointer :as cptr]
            [hara.runtime.jocl :as exec :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs exec? exec kernel? init-exec-jocl
                     init-ptr-jocl invoke-ptr-jocl stop-jocl jocl:create
                     jocl +exec+ +rt+)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

(l/script- :c)

(define.c MUL= [c a b i] (:= (. c [i]) (* (. a [i]) (. b [i]))))

(define.c G0  (get-global-id 0))

(defn.c ^{:- [:__kernel :void]
          :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}}
  sample
  ([:__global :const :float :* a
    :__global :const :float :* b
    :__global :float :* c]
   (var :int i := -/G0)
   (-/MUL= c a b i)))

(defonce +exec+
  (-> (exec {:source sample
             :worksize (fn [{:keys [a]}]
                         [(count a)])})
      (component/start)))

(defonce +rt+
  (component/start (jocl:create {})))

^{:refer hara.runtime.jocl.runtime/kernel? :added "3.0"}
(fact "check that a code entry "

  (kernel? @sample)
  => true)

^{:refer hara.runtime.jocl.runtime/init-exec-jocl :added "3.0"}
(fact "initialises the exec in the runtime"
  ^:hidden

  (init-exec-jocl +rt+
                  (cptr/pointer {:context :lang/c
                                 :lang :c
                                 :module 'hara.runtime.jocl.runtime-test
                                 :section :code
                                 :id 'sample})
                  @sample)
  => exec/exec?

  (-> ((get @(:state +rt+) `sample)
       (float-array (range 10))
       (float-array (range 10))
       (float-array 10))
      seq)
  =>  [0.0 1.0 4.0 9.0 16.0 25.0 36.0 49.0 64.0 81.0])

^{:refer hara.runtime.jocl.runtime/init-ptr-jocl :added "3.0"}
(fact "initialises the pointer")

^{:refer hara.runtime.jocl.runtime/invoke-ptr-jocl :added "3.0"}
(fact "invokes a jocl ptr (cached kernel)")

^{:refer hara.runtime.jocl.runtime/stop-jocl :added "3.0"}
(fact "stops the runtime")

^{:refer hara.runtime.jocl.runtime/jocl:create :added "3.0"}
(fact "creates a new runtime")

^{:refer hara.runtime.jocl.runtime/jocl :added "3.0"}
(fact "create and starts the runtime")
