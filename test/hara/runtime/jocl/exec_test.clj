tahto/runtime/jocl/exec_test.clj:1:(ns tahto.runtime.jocl.exec-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [tahto.core :as l]
            [std.lib.component :as component]
tahto/runtime/jocl/exec_test.clj:6:            [tahto.runtime.jocl :refer :all]
tahto/runtime/jocl/exec_test.clj:7:            [tahto.runtime.jocl.env :as jocl-env]))

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

tahto/runtime/jocl/exec_test.clj:39:^{:refer tahto.runtime.jocl.exec/CANARY :adopt true :added "3.0"}
(fact "play around with the spec"

  (meta #'sample)
  => (contains '{:- [:__kernel :void],
                 :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}})

  (:rt/kernel @sample)
  => '{:worksize (fn [{:keys [a]}] [(count a)])})

tahto/runtime/jocl/exec_test.clj:49:^{:refer tahto.runtime.jocl.exec/exec-prep :added "3.0"}
(fact "preps the source for the exec"
  ^:hidden

  (exec-prep (platform:default)
             (device:gpu)
             (second (exec-source sample))
             (first (exec-source sample)))
  => map?)

tahto/runtime/jocl/exec_test.clj:59:^{:refer tahto.runtime.jocl.exec/exec-source :added "3.0"}
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
       "__kernel void sample(__global const float * a, __global const float * b, __global float * c) {"
       ""
       "  int i = G0;"
       "  MUL_eq(c,a,b,i);"
       "}")

      "sample"])

tahto/runtime/jocl/exec_test.clj:82:^{:refer tahto.runtime.jocl.exec/exec-start :added "3.0"}
(fact "starts the exec"

  (def -e- 
    (exec-start (exec {:source sample
                       :worksize (fn [{:keys [a]}]
                                   [(count a)])})))
  (exec-stop -e-))

tahto/runtime/jocl/exec_test.clj:91:^{:refer tahto.runtime.jocl.exec/exec-stop :added "3.0"}
(fact "stops the exec")

tahto/runtime/jocl/exec_test.clj:94:^{:refer tahto.runtime.jocl.exec/exec-invoke:worksize :added "3.0"}
(fact "gets the worksize of the executable"
  
  (vec (exec-invoke:worksize +exec+ (:spec @(:state +exec+))
                             [(float-array [10 10])]))
  => [2])

tahto/runtime/jocl/exec_test.clj:101:^{:refer tahto.runtime.jocl.exec/set-kernel-buffer :added "3.0"}
(fact "sets the kernel buffer"

  (set-kernel-buffer (:context @(:state +exec+))
                     (:kernel  @(:state +exec+))
                     1
                     {:buffer true :const true :dsize 4}
                     {:length 10}
                     (float-array 10))
  => org.jocl.cl_mem)

tahto/runtime/jocl/exec_test.clj:112:^{:refer tahto.runtime.jocl.exec/set-kernel-value :added "3.0"}
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

(defn.c ^{:- [:__kernel :void]
          :rt/kernel {:worksize (fn [{:keys [a]}] [(count a)])}}
  scale-by
  ([:__global :const :float :* a
    :__global :float :* c
    :int n]
   (var :int i := -/G0)
   (:= (. c [i]) (* (. a [i]) n))))

tahto/runtime/jocl/exec_test.clj:137:^{:refer tahto.runtime.jocl.exec/set-kernel-value :added "4.1" :id test-scalar-kernel-arg-coercion}
(fact "scalar args are coerced to the kernel's declared type"
  ;; Clojure literals are longs (8 bytes); the kernel declares `int n`
  ;; (4 bytes). Strict drivers (NVIDIA OpenCL 3.0 / CUDA 13) reject the
  ;; mismatch with CL_INVALID_KERNEL_ARGS and the output stays zero.
  (let [e (component/start
           (exec {:source scale-by
                  :worksize (fn [{:keys [a]}] [(count a)])}))
        out (float-array 10)]
    (try
      (seq (exec-invoke e (float-array (range 10)) out 3))
      (finally
        (component/stop e))))
  => '(0.0 3.0 6.0 9.0 12.0 15.0 18.0 21.0 24.0 27.0))

tahto/runtime/jocl/exec_test.clj:152:^{:refer tahto.runtime.jocl.exec/exec-invoke:setup :added "3.0"}
(fact "sets up the exec"
  
  (exec-invoke:setup +exec+
                     (type-args (:spec @(:state +exec+))
                                +exec-args+)
                     +exec-args+)
  => (contains [org.jocl.cl_mem
                org.jocl.cl_mem
                org.jocl.cl_mem]))

tahto/runtime/jocl/exec_test.clj:163:^{:refer tahto.runtime.jocl.exec/exec-invoke:process :added "3.0"}
(fact "enqueues the kernel call")

tahto/runtime/jocl/exec_test.clj:166:^{:refer tahto.runtime.jocl.exec/exec-invoke:output :added "3.0"}
(fact "writes to output from buffer and release")

tahto/runtime/jocl/exec_test.clj:169:^{:refer tahto.runtime.jocl.exec/exec-invoke :added "3.0"}
(fact "main invoke function"
  
  (seq (exec-invoke +exec+
                    (float-array (range 10))
                    (float-array (range 10))
                    (float-array 10)))
  => '(0.0 1.0 4.0 9.0 16.0 25.0 36.0 49.0 64.0 81.0))

tahto/runtime/jocl/exec_test.clj:178:^{:refer tahto.runtime.jocl.exec/exec? :added "3.0"}
(fact "checks that object is of type exec"

  (exec? +exec+)
  => true)

tahto/runtime/jocl/exec_test.clj:184:^{:refer tahto.runtime.jocl.exec/exec :added "3.0"}
(fact "creates an opencl exec")
