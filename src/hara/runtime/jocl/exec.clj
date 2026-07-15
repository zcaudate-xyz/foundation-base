(ns hara.runtime.jocl.exec
  (:require [std.protocol.component :as protocol.component]
            [std.lib.encode :as encode]
            [hara.runtime.jocl.common :as common]
            [hara.runtime.jocl.meta :as meta]
            [hara.runtime.jocl.type :as type]
            [std.object :as object]
            [std.string :as str]
            [hara.lang.impl :as impl]
            [hara.lang.pointer :as ptr]
            [hara.common.util :as ut]
            [hara.model.spec-c :as c]
            [std.lib.collection :as coll]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.security :as security])
  (:import (org.jocl CL cl_event)
           (java.nio Buffer)))

(defn- exec-started?
  ([{:keys [state]}]
   (not (empty? @state))))

(defn- exec-stopped?
  ([{:keys [state]}]
   (empty? @state)))

(defn exec-prep
  "preps the source for the exec"
  {:added "3.0"}
  ([platform devices name code]
   (let [devices   (into-array (coll/seqify devices))
         context   (common/cl-context platform devices)
         queue     (common/cl-queue context)
         program   (common/cl-program context (first devices) code)
         kernel    (common/cl-kernel program name)]
     {:platform platform
      :devices devices
      :context context
      :queue queue
      :program program
      :kernel kernel})))

(defn exec-source
  "preps the source for the exec"
  {:added "3.0"}
  [source-ptr & [code namespace]]
  (let [code      (or code (impl/emit-script
                            (ut/sym-full source-ptr)
                            {:lang :c
                             :layout :flat
                             :namespace (or namespace (env/ns-sym))
                             :emit {:body {:suppress true}}}))
        ;; :flat layout emits declarations unmangled, so the kernel name is
        ;; just the entry id (see hara.model.spec-c/emit-defn).
        name      (c/c-sanitize (:id source-ptr))]
    [code name]))

(defn exec-start
  "starts the exec
 
   (def -e- 
     (exec-start (exec {:source sample
                        :worksize (fn [{:keys [a]}]
                                    [(count a)])})))
   (exec-stop -e-)"
  {:added "3.0"}
  ([{:keys [type source code args state] :as cl}]
   (if (empty? @state)
     (let [type (or type :gpu)
           [platform devices]  (case type ;; This can be expanded with more customisations
                                 :cpu [(meta/platform:default) (meta/device:cpu)]
                                 :gpu [(meta/platform:default) (meta/device:gpu)])
           [code name]  (exec-source source code)
           spec      (common/entry-spec (ptr/get-entry source) args)
           _  (reset! state (assoc (exec-prep platform devices name code)
                                   :spec spec
                                   :code code
                                   :sha  (security/sha1 code)))]))
   cl))

(defn exec-stop
  "stops the exec"
  {:added "3.0"}
  ([{:keys [type source options state] :as cl}]
   (when-not (empty? @state)
     (let [{:keys [kernal queue context program]} @state]
       (CL/clFinish queue)
       (CL/clReleaseKernel kernal)
       (CL/clReleaseProgram program)
       (CL/clReleaseCommandQueue queue)
       (CL/clReleaseContext context)
       (reset! state nil)))
   cl))

(defn exec-invoke:worksize
  "gets the worksize of the executable
   
   (vec (exec-invoke:worksize +exec+ (:spec @(:state +exec+))
                              [(float-array [10 10])]))
   => [2]"
  {:added "3.0"}
  ([{:keys [worksize] :as cl} spec args]
   (let [margs (zipmap (map :key spec) args)
         ws (if (or (fn? worksize)
                    (var? worksize))
              (worksize margs)
              worksize)]
     (long-array ws))))

(defn set-kernel-buffer
  "sets the kernel buffer
 
   (set-kernel-buffer (:context @(:state +exec+))
                      (:kernel  @(:state +exec+))
                      1
                      {:buffer true :const true :dsize 4}
                      {:length 10}
                      (float-array 10))
   => org.jocl.cl_mem"
  {:added "3.0"}
  ([context kernel i {:keys [dsize const input output] :as entry} {:keys [length]} arg]
   (let [flags  (if const
                  (bit-or CL/CL_MEM_READ_ONLY CL/CL_MEM_COPY_HOST_PTR)
                  CL/CL_MEM_READ_WRITE)
         total (* dsize length)
         ptr   (cond (or const input) (common/pointer-to arg)
                     
                     output nil
                     
                     :else (h/error "Invalid entry" {:entry entry}))
         buff  (common/with-error [err]
                 (CL/clCreateBuffer context
                                    flags
                                    total
                                    ptr
                                    err))]
     (CL/clSetKernelArg kernel i (:mem meta/+types+) (common/pointer-to buff))
     buff)))

(defn set-kernel-value
  "sets the kernel value
 
   ;; RUNNING THIS IN EMACS/REPL CAUSES A STACKFAULT
   ;; MOST LIKELY DUE TO THREAD SAFETLY ISSUES
   (set-kernel-value (:kernel @(:state +exec+))
                     1
                     {:dsize 8}
                     1)"
  {:added "3.0"}
  ([kernel i {:keys [dsize]} arg]
   (let [arr (type/to-array arg)]
     (CL/clSetKernelArg kernel i dsize (common/pointer-to arr))
     nil)))

(defn exec-invoke:setup
  "sets up the exec
   
   (exec-invoke:setup +exec+
                      (type/type-args (:spec @(:state +exec+))
                                      |args|)
                      |args|)
   => (contains [org.jocl.cl_mem
                 org.jocl.cl_mem
                 org.jocl.cl_mem])"
  {:added "3.0"}
  ([{:keys [options state] :as cl} targs args]
   (let [{:keys [spec context kernel]} @state
         buffs  (mapv (fn [i {:keys [buffer] :as entry} targ arg]
                        (if buffer
                          (set-kernel-buffer context kernel i entry targ arg)
                          ;; scalars must match the kernel's declared type
                          ;; exactly (strict drivers reject size mismatches
                          ;; with CL_INVALID_KERNEL_ARGS)
                          (set-kernel-value  kernel i entry
                                             (type/unit-coerce (:type entry) arg))))
                      (range) spec targs args)]
     buffs)))

(defn exec-invoke:process
  "enqueues the kernel call"
  {:added "3.0"}
  ([{:keys [options state] :as cl} worksize]
   (let [{:keys [queue kernel]} @state
         event (cl_event.)]
     (CL/clEnqueueNDRangeKernel queue kernel (count worksize)
                                nil
                                worksize
                                nil
                                0
                                nil
                                event)
     event)))

(defn exec-invoke:output
  "writes to output from buffer and release"
  {:added "3.0"}
  ([{:keys [options state] :as cl} targs args buffs]
   (let [{:keys [queue]} @state]
     (mapv (fn [i {:keys [length dsize output] :as targ} arg buff]
             (let [event (cl_event.)]
               (when output
                 (CL/clEnqueueReadBuffer queue buff CL/CL_TRUE 0 (* dsize length)
                                         (common/pointer-to arg) 0 nil event))
               (if buff
                 (CL/clReleaseMemObject buff))))
           (range) targs args buffs))))

(defn exec-invoke
  "main invoke function
   
   (seq (exec-invoke +exec+
                     (float-array (range 10))
                     (float-array (range 10))
                     (float-array 10)))
   => '(0.0 1.0 4.0 9.0 16.0 25.0 36.0 49.0 64.0 81.0)"
  {:added "3.0"}
  ([{:keys [state options] :as cl} & args]
   (let [_ (if (exec-stopped? cl) (exec-start cl))
         {:keys [spec]} @state
         targs     (type/type-args spec args)
         worksize  (exec-invoke:worksize cl spec args)
         buffs     (exec-invoke:setup cl targs args)
         _         (exec-invoke:process cl worksize)
         _         (exec-invoke:output cl targs args buffs)
         outputs   (->> (map (fn [{:keys [output]} arg]
                               (if output
                                 arg))
                             targs
                             args)
                        (filter identity))]
     (case (count outputs)
       0 (h/error "No output")
       1 (first outputs)
       outputs))))

(defn- exec-jocl-string [{:keys [type options source]}]
  (str "#exec.cl" type " " (if source
                             (ut/sym-full source))))

(defimpl ExecJocl [type options entry]
  :prefix "exec-"
  :invoke exec-invoke
  :string exec-jocl-string
  :protocols [protocol.component/IComponent
              :exclude [-kill]
              protocol.component/IComponentQuery
              :include [-started? -stopped?]])

(defn exec?
  "checks that object is of type exec
 
   (exec? +exec+)
   => true"
  {:added "3.0"}
  ([obj]
   (instance? ExecJocl obj)))

(defn exec
  "creates an opencl exec"
  {:added "3.0"}
  ([{:keys [type source] :as m
     :or {type :gpu}}]
   (let [{:rt/keys [kernel]} (ptr/get-entry source)]
     (-> (merge m (update kernel :worksize (fn eval-fn
                                             ([ws]
                                              (if (or (coll/form? ws)
                                                      (symbol? ws))
                                                (eval ws)
                                                ws)))))
         (assoc :type type :state (atom {}))
         (map->ExecJocl)))))
