(ns hara.runtime.jocl.exec-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [hara.lang :as l]
            [std.lib.component :as component]
            [hara.runtime.jocl :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs exec-source exec-prep exec-start exec-stop
                     exec-invoke:worksize set-kernel-buffer
                     set-kernel-value exec-invoke:setup exec-invoke:process
                     exec-invoke:output exec-invoke exec? exec type-args
                     platform:default device:gpu +exec+)

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

^{:refer hara.runtime.jocl.exec/CANARY :adopt true :added "3.0"}
(fact "play around with the spec"

  (meta #'sample)
  => (contains '{:- [:__kernel :void],
                 :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}})

  (:rt/kernel @sample)
  => '{:worksize (fn [{:keys [a]}] [(count a)])})

^{:refer hara.runtime.jocl.exec/exec-prep :added "3.0"}
(fact "preps the source for the exec"
  ^:hidden

  (exec-prep (platform:default)
             (device:gpu)
             (second (exec-source sample))
             (first (exec-source sample)))
  => map?)

^{:refer hara.runtime.jocl.exec/exec-source :added "3.0"}
(fact "preps the source for the exec"
  ^:hidden

  (def -e- 
    (exec {:source sample
           :worksize (fn [{:keys [a]}]
                       [(count a)])}))
  
  (exec-source sample)
  => [(std.string/|
       "#define MUL_eq(c,a,b,i) c[i] = (a[i] * b[i])"
       ""
       "#define G0 get_global_id(0)"
       ""
       "__kernel void hara_runtime_jocl_exec_test____sample(__global const float * a, __global const float * b, __global float * c) {"
       ""
       "  int i = G0;"
       "  MUL_eq(c,a,b,i);"
       "}")

      "hara_runtime_jocl_exec_test____sample"])

^{:refer hara.runtime.jocl.exec/exec-start :added "3.0"}
(fact "starts the exec"

  (def -e- 
    (exec-start (exec {:source sample
                       :worksize (fn [{:keys [a]}]
                                   [(count a)])})))
  (exec-stop -e-))

^{:refer hara.runtime.jocl.exec/exec-stop :added "3.0"}
(fact "stops the exec")

^{:refer hara.runtime.jocl.exec/exec-invoke:worksize :added "3.0"}
(fact "gets the worksize of the executable"
  
  (vec (exec-invoke:worksize +exec+ (:spec @(:state +exec+))
                             [(float-array [10 10])]))
  => [2])

^{:refer hara.runtime.jocl.exec/set-kernel-buffer :added "3.0"}
(fact "sets the kernel buffer"

  (set-kernel-buffer (:context @(:state +exec+))
                     (:kernel  @(:state +exec+))
                     1
                     {:buffer true :const true :dsize 4}
                     {:length 10}
                     (float-array 10))
  => org.jocl.cl_mem)

^{:refer hara.runtime.jocl.exec/set-kernel-value :added "3.0"}
(comment
  "sets the kernel value"

  ;; RUNNING THIS IN EMACS/REPL CAUSES A STACKFAULT
  ;; MOST LIKELY DUE TO THREAD SAFETLY ISSUES
  (set-kernel-value (:kernel @(:state +exec+))
                    1
                    {:dsize 8}
                    1))

(def +exec-args+
  [(float-array (range 10))
   (float-array (range 10))
   (float-array 10)])

^{:refer hara.runtime.jocl.exec/exec-invoke:setup :added "3.0"}
(fact "sets up the exec"
  
  (exec-invoke:setup +exec+
                     (type-args (:spec @(:state +exec+))
                                +exec-args+)
                     +exec-args+)
  => (contains [org.jocl.cl_mem
                org.jocl.cl_mem
                org.jocl.cl_mem]))

^{:refer hara.runtime.jocl.exec/exec-invoke:process :added "3.0"}
(fact "enqueues the kernel call")

^{:refer hara.runtime.jocl.exec/exec-invoke:output :added "3.0"}
(fact "writes to output from buffer and release")

^{:refer hara.runtime.jocl.exec/exec-invoke :added "3.0"}
(fact "main invoke function"
  
  (seq (exec-invoke +exec+
                    (float-array (range 10))
                    (float-array (range 10))
                    (float-array 10)))
  => '(0.0 1.0 4.0 9.0 16.0 25.0 36.0 49.0 64.0 81.0))

^{:refer hara.runtime.jocl.exec/exec? :added "3.0"}
(fact "checks that object is of type exec"

  (exec? +exec+)
  => true)

^{:refer hara.runtime.jocl.exec/exec :added "3.0"}
(fact "creates an opencl exec")
