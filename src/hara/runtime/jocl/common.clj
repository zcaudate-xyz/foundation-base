(ns hara.runtime.jocl.common
  (:require [hara.runtime.jocl.meta :as meta]
            [hara.common.emit-helper :as helper]
            [hara.model.spec-c :as c]
            [std.object :as object]
            [std.string :as str]
            [std.lib.foundation :as h]
            [std.lib.template :as template])
  (:import (org.jocl CL
                     Pointer
                     NativePointerObject
                     Sizeof
                     cl_context_properties
                     cl_device_id
                     cl_event)
           (java.nio Buffer)))

(def ^:dynamic *error* nil)

(def pointer-to
  (object/query-class Pointer ["to" :#]))

(defn parse-spec
  "parses a kernel arglist
 
   (parse-spec {:modifiers #{:__global :const :float :*}, :symbol 'a}
               '{c {:output true}})
   => '{:key :a, :symbol a, :type :float,
        :dsize 4, :buffer true, :const true, :input true}
   
   (parse-spec {:modifiers #{:__global :long :*}, :symbol 'c}
               '{c {:output true}})
   => '{:key :c, :symbol c, :type :long, :dsize 8,
        :buffer true, :const false, :output true}"
  {:added "3.0"}
  ([{:keys [symbol modifiers] :as arg} {:keys [output] :as optm}]
   (let [mod    (set modifiers)
         type   (or (first (filter meta/+types+ mod))
                    (h/error "Unable to recognise type" {:input modifiers}))
         const  (boolean (mod :const))
         buffer (boolean (mod :*))
         input  (or const (false? output))
         output (and (not const) (not (false? output)))]
     (merge (cond-> {:key    (keyword (name symbol))
                     :symbol symbol
                     :type   type
                     :dsize  (meta/+types+ type)
                     :buffer buffer
                     :const  const}
              input  (assoc :input true)
              output (assoc :output true))
            (get optm symbol)))))

(defn entry-spec
  "converts a form entry into a spec
   
   (entry-spec {:form '(defn sym [:__global :const :float :* a
                                  :__global :const :float :* b
                                  :__global :float :* c])}
               '{c {:output true}})
   => '({:key :a, :symbol a, :type :float, :dsize 4, :buffer true, :const true, :input true}
        {:key :b, :symbol b, :type :float, :dsize 4, :buffer true, :const true, :input true}
        {:key :c, :symbol c, :type :float, :dsize 4, :buffer true, :const false, :output true})"
  {:added "3.0"}
  ([entry optm]
   (let [args (-> (nth (:form entry) 2)
                  (helper/emit-typed-args c/+grammar+))]
     (map #(parse-spec % optm) args))))

(defmacro with-error
  "helper function for creation within context"
  {:added "3.0"}
  [[sym error] & body]
  (template/$ (let [~sym (if (nil? ~error)
                    (or ~`*error* (int-array [1]))
                    ~error)
             result (do ~@body)
             code (meta/error-lookup (first ~sym))]
         (if (not= code :success)
           (h/error "Error in processing" {:code code})
           result))))

;;
;; Context
;;

(defn cl-context
  "creates a cl context
 
   (cl-context)
   => org.jocl.cl_context"
  {:added "3.0"}
  ([]
   (cl-context (meta/platform:default) [(meta/device:gpu)]))
  ([platform devices]
   (with-error [err]
     (CL/clCreateContext 
      (doto (cl_context_properties.)
        (.addProperty CL/CL_CONTEXT_PLATFORM platform))
      1
      (into-array cl_device_id devices)
      nil
      nil
      err))))

(defn cl-queue
  "creates a command queue (within a context)
 
   (cl-queue (cl-context) (meta/device:gpu))
   => org.jocl.cl_command_queue"
  {:added "3.0"}
  ([context]
   (cl-queue context (meta/device:gpu)))
  ([context device]
   (with-error [err]
     (CL/clCreateCommandQueue context device 0 err))))


(defn cl-program
  "creates a program from source"
  {:added "3.0"}
  ([context device source]
   (let [prog (with-error [err]
                (CL/clCreateProgramWithSource context
                                              1
                                              (into-array [source])
                                              nil
                                              err))
         err    (CL/clBuildProgram prog 0 nil nil nil nil)
         [msg tag] (when (neg? err)
                  [(meta/cl-call {:fn CL/clGetProgramBuildInfo
                                  :sizet  :long
                                  :pointer true
                                  :arrayt :byte
                                  :return h/string}
                                 prog
                                 device
                                 CL/CL_PROGRAM_BUILD_LOG)
                   (meta/error-lookup err)])]
     (if tag (h/error msg {:error tag}))
     prog)))

(defn cl-kernel
  "creates a kernel from the program"
  {:added "3.0"}
  ([program name]
   (with-error [err]
     (CL/clCreateKernel program name err))))
