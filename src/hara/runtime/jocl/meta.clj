(ns hara.runtime.jocl.meta
  (:require [std.object :as object]
            [std.string :as str]
            [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.resource :as resource]
            [std.lib.template :as template])
  (:import (org.jocl CL
                     Pointer
                     Sizeof
                     cl_platform_id
                     cl_device_id
                     cl_command_queue
                     cl_context
                     cl_context_properties
                     cl_kernel
                     cl_mem
                     cl_program
                     cl_queue_properties)))

(declare cl-device-type
         cl-fp-config
         cl-exec-capabilities
         cl-cache-type
         cl-mem-type
         device-info)

(comment

  org.jocl.CL
  )

;;
;; Types
;;

(def +init+
  (CL/setExceptionsEnabled false))

(defn cl-size
  "helper for cl-types
 
   (cl-size \"float\")
   => '(org.jocl.Sizeof/float)"
  {:added "3.0"}
  ([name]
   `(~(symbol "org.jocl.Sizeof" name))))

(defmacro cl-types
  "creates a lookup of all standard cl sizes
 
   (cl-types)"
  {:added "3.0"}
  ([]
   (->> (object/query-class Sizeof [#"^cl_" :name])
        (coll/map-juxt [(comp keyword #(subs % 3))
                     cl-size]))))

(def +types+ (cl-types))

;;
;; Lookups                                        ;
;;

(def upper-str (comp str/upper-case str/snake-case name))

(defn lookup:fn
  "builds a lookup function
   
   ((lookup:fn {:a 1 :b 2}) 1)
   => :a"
  {:added "3.0"}
  ([entries]
   (let [m  (into {} entries)
         tm (coll/transpose m)
         lu (merge m tm)]
     (fn [val]
       (or (get lu val)
           (h/error "Cannot find value" {:input val
                                         :options (keys lu)}))))))

(defn lookup:flags
  "builds a flags function
 
   ((lookup:flags {:hello 1 :world 2 :foo 4 :bar 8}) 13)
   => #{:bar :hello :foo}"
  {:added "3.0"}
  ([entries]
   (let [m  (into {} entries)]
     (fn [val]
       (set (keys (coll/filter-vals #(pos? (bit-and % val))
                                m)))))))

(def cl-bool (comp pos? first))
         
(def +meta+
  '{:platform    {:name     {}
                  :profile  {}
                  :vendor   {}
                  :version  {}
                  :extensions {}}
    :context     {:reference-count {:arrayt :long}
                  :num-devices {:arrayt :long}
                  :properties  {:arrayt cl_context_properties}}
    :queue       {:context {:arrayt cl_context}
                  :device {:arrayt cl_device_id}
                  :reference-count {:arrayt :long}
                  :properties  {:arrayt :long}}  ;;cl_command_queue_properties
    :mem         {:type      {:arrayt :long} ;; cl_mem_object_type
                  :flags     {:arrayt :long} ;; cl_mem_flags
                  :size      {:arrayt :long}
                  :host-ptr  {:arrayt :long}
                  :map-count {:arrayt :long}
                  :reference-count {:arrayt :long}
                  :context   {:arrayt cl_context}
                  :associated-memobject {:arrayt cl_mem}
                  :offset    {:arrayt :long}}
    :kernel      {:function-name {:arrayt :byte :return h/string}
                  :num-args  {:arrayt :long}
                  :reference-count {:arrayt :long}
                  :context {:arrayt cl_context}
                  :program {:arrayt cl_program}}
    :program     {:reference-count {:arrayt :long}
                  :context {:arrayt cl_context}
                  :num-devices {:arrayt :long}
                  :devices {:arrayt cl_device_id :return vec}
                  :source {:arrayt :byte :return h/string}
                  :binary-sizes {:arrayt :long :return vec}
                  :binaries {:arrayt :byte :return identity}}
    :device      {:address-bits {:arrayt :long}
                  :available    {:arrayt :byte :return cl-bool}
                  :compiler-available {:arrayt :byte :return cl-bool}
                  :double-fp-config {:arrayt :long :return cl-fp-config}
                  :endian-little {:arrayt :byte :return cl-bool}
                  :error-correction-support {:arrayt :byte :return cl-bool}
                  :execution-capabilities {:arrayt :byte :return cl-exec-capabilities}
                  :extensions {:arrayt :byte :return h/string}
                  :global-mem-cache-type {:arrayt :byte :return cl-cache-type}
                  :global-mem-cacheline-size {:arrayt :int}
                  :global-mem-size {:arrayt :long}
                  :half-fp-config {:arrayt :long :return cl-fp-config}
                  :host-unified-memory {:arrayt :byte :return cl-bool}
                  :image-support {:arrayt :byte :return cl-bool}
                  :image2d-max-height {:arrayt :long}
                  :image2d-max-width {:arrayt :long}
                  :image3d-max-depth {:arrayt :long}
                  :image3d-max-height {:arrayt :long}
                  :image3d-max-width {:arrayt :long}
                  :local-mem-size {:arrayt :long}
                  :local-mem-type {:arrayt :long :return cl-mem-type}
                  :max-clock-frequency {:arrayt :long}
                  :max-compute-units {:arrayt :long}
                  :max-constant-args {:arrayt :long}
                  :max-constant-buffer-size {:arrayt :long}
                  :max-mem-alloc-size {:arrayt :long}
                  :max-parameter-size {:arrayt :long}
                  :max-read-image-args {:arrayt :long}
                  :max-samplers {:arrayt :long}
                  :max-work-group-size {:arrayt :long}
                  :max-work-item-dimensions {:arrayt :long :return vec}
                  :max-work-item-sizes {:arrayt :long}
                  :max-write-image-args {:arrayt :long}
                  :name {:arrayt :byte :return h/string}
                  :opencl-c-version {:arrayt :byte :return h/string}
                  :platform {:arrayt cl_platform_id}
                  :profile {:arrayt :byte :return h/string}
                  :queue-properties {:arrayt :long}
                  :single-fp-config {:arrayt :long :return cl-fp-config}
                  :type {:arrayt :long :return cl-device-type}
                  :vendor {:arrayt :byte :return h/string}
                  :vendor-id {:arrayt :long}
                  :version {:arrayt :byte :return h/string}}
    :enum        {:device-type       {:prefix "CL_DEVICE_TYPE"
                                      :keys [:cpu :gpu :accelerator :default :all]}
                  :fp-config         {:prefix "CL_FP"
                                      :flag true
                                      :keys [:denorm :inf-nan :fma
                                             :round-to-nearest :round-to-zero :round-to-inf
                                             :soft-float]}
                  :exec-capabilities {:prefix "CL_EXEC"
                                      :flag true
                                      :keys [:kernel :native-kernel]}
                  :cache-type        {:prefix "CL"
                                      :keys [:none :read-only-cache :read-write-cache]}
                  :mem-type          {:prefix "CL"
                                      :keys [:local :global]}}})

(def +device-ext+
  {:mem-base-addr-align {:arrayt :long}
   :min-data-type-align-size {:arrayt :long}
   :native-vector-width-char {:arrayt :long}
   :native-vector-width-short {:arrayt :long}
   :native-vector-width-int {:arrayt :long}
   :native-vector-width-long {:arrayt :long}
   :native-vector-width-float {:arrayt :long}
   :native-vector-width-double {:arrayt :long}
   :native-vector-width-half {:arrayt :long}
   :profiling-timer-resolution {:arrayt :long}
   :preferred-vector-width-char {:arrayt :long}
   :preferred-vector-width-short {:arrayt :long}
   :preferred-vector-width-int {:arrayt :long}
   :preferred-vector-width-long {:arrayt :long}
   :preferred-vector-width-float {:arrayt :long}
   :preferred-vector-width-double {:arrayt :long}
   :preferred-vector-width-half {:arrayt :long}})

(defn lookup:build
  "builds a lookup for the metadata
 
   ((lookup:build [:enum :device-type]) [4])
   => :gpu"
  {:added "3.0"}
  ([path]
   (let [{:keys [prefix flag keys]} (get-in +meta+ path)
         entries (map (fn [k]
                        [k (symbol (str "org.jocl.CL/" prefix "_" (upper-str k)))])
                      keys)
         entries (->> (eval `(h/template-entries [identity]
                               ~entries))
                      (into {}))
         build-fn (if flag
                    lookup:flags
                    lookup:fn)]
     (comp (build-fn entries) first))))

(def cl-device-type (lookup:build [:enum :device-type]))
(def cl-exec-capabilities (lookup:build [:enum :exec-capabilities]))
(def cl-fp-config (lookup:build [:enum :fp-config]))
(def cl-cache-type (lookup:build [:enum :cache-type]))
(def cl-mem-type (lookup:build [:enum :mem-type]))


;;
;; FUNCTIONS
;;

(def +id-fns+
  '{:platform-ids    {:fn CL/clGetPlatformIDs
                      :unit :array
                      :sizet  :int
                      :arrayt cl_platform_id
                      :return vec}
    :device-ids      {:fn CL/clGetDeviceIDs
                      :unit :array
                      :sizet  :int
                      :arrayt cl_device_id
                      :return vec}})

(def +info-fns+
  '[[:platform-info   {:arrayt :byte :return h/string
                       :prefix "CL_PLATFORM" :fn CL/clGetPlatformInfo
                       :key :platform :class cl_platform_id}]
    [:device-info     {:prefix "CL_DEVICE"   :fn CL/clGetDeviceInfo
                       :key :device :class cl_device_id}]
    [:context-info    {:prefix "CL_CONTEXT"  :fn CL/clGetContextInfo
                       :key :context :class cl_context}]
    [:mem-info        {:prefix "CL_MEM"      :fn CL/clGetMemObjectInfo
                       :key :mem :class cl_mem}]
    [:queue-info      {:prefix "CL_QUEUE"    :fn CL/clGetCommandQueueInfo
                       :key :queue :class cl_command_queue}]
    [:program-info    {:prefix "CL_PROGRAM"  :fn CL/clGetProgramInfo
                       :key :program :class cl_program :exclude [:devices :context]}]
    [:kernel-info     {:prefix "CL_KERNEL"   :fn CL/clGetKernelInfo
                       :key :kernel :class cl_kernel}]])

(def +fns+
  (->> (coll/map-entries (fn [[k m]]
                       [k (merge {:sizet :long :pointer true :return first} m)])
                      +info-fns+)
       (merge +id-fns+)))

(defn array-form
  "creates a form given inputs
 
   (array-form :int [1 2 3])
   => '(int-array [1 2 3])
 
   (array-form 'Object [1 2 3])
   => '(clojure.core/make-array Object [1 2 3])"
  {:added "3.0"}
  ([type & args]
   (if (keyword? type)
     `(~(symbol (str (name type) "-array")) ~@args)
     `(make-array ~type ~@args))))

(defn to-ptr
  "pointer for array inputs"
  {:added "3.0"}
  ([arr]
   (Pointer/to ^"[Lorg.jocl.NativePointerObject;" arr)))

(defmacro cl-call
  "creates a cl call given parameters"
  {:added "3.0"}
  ([{:keys [fn sizet arrayt pointer return unit]} & args]
   (let [data  (array-form arrayt 'size)
         sizeb (array-form sizet 1)]
     (template/$ (let [size  ~(if true
                         `(let [~'sizeb ~sizeb
                                ~'_     (~fn ~@args 0 nil ~'sizeb)]
                            (first ~'sizeb))
                         1)
                data  ~data
                _     (~fn ~@args size ~(cond (not pointer)
                                              'data
                                              
                                              (keyword? arrayt)
                                              `(Pointer/to ~'data)
                                              
                                              :else `(to-ptr ~'data))
                       nil)]
            ~(if return
               `(~return ~'data)
               'data))))))

(defmacro fn-call
  "creates a cl call given a lookup key"
  {:added "3.0"}
  [k opts & args]
  `(try (cl-call ~(merge (get +fns+ k) opts) ~@args)
        (catch Throwable ~'t)))

(defn info-template:defn
  "creates a property template"
  {:added "3.0"}
  ([tname prefix [k opts]]
   [k (template/$ (defn ~(symbol (str tname "-info:" (name k)))
             ([~(symbol tname)]
              (fn-call ~(keyword (str tname "-info")) ~opts
                       ~(symbol tname)
                       ~(symbol (str "CL/" prefix "_" (upper-str k)))))))]))

(defn info-template:print
  "creates a print template"
  {:added "3.0"}
  [type class exclude]
  `(defmethod print-method ~class
     ([~'obj ~(with-meta 'w {:tag 'java.io.Writer})]
      (.write ~'w (str "#cl." ~(name type) " "
                       (dissoc (~(symbol (str (name type) "-info")) ~'obj)
                               ~@exclude))))))

(defn info-template:gen
  "helper for `info-template`
 
   (info-template:gen :platform nil)"
  {:added "3.0"}
  ([type fn?]
   (let [tname   (name type)
         fields  (get +meta+ type)
         {:keys [prefix class exclude]}  (get +fns+ (keyword (str tname "-info")))
         forms   (mapv (partial info-template:defn tname prefix) fields)
         entries (coll/map-vals second forms)
         lu-sym  (symbol (str "+" (name type) "-info+"))]
     `[~forms
       (def ~lu-sym ~entries)
       ~(if fn?
          `(defn ~(symbol (str (name type) "-info"))
             ([~(symbol (name type))]
              (coll/map-vals (partial h/call ~(symbol (name type))) ~lu-sym))))
       ~(info-template:print tname class exclude)])))

(defmacro info-template
  "generates the info functions"
  {:added "3.0"}
  ([type]
   (info-template:gen type true))
  ([type gen-fn]
   (info-template:gen type gen-fn)))

(defmacro info-gen
  "generates multiple info functions"
  {:added "3.0"}
  ([ks]
   (mapv (fn [k]
           `(info-template ~k))
         ks)))

(info-template :device false)
(info-gen [:platform :context :mem :queue :kernel :program])

(def +error-codes+
  [[:success 0]
   [:device-not-found -1]
   [:device-not-available -2]
   [:compiler-not-available -3]
   [:mem-object-allocation-failure -4]
   [:out-of-resources -5]
   [:out-of-host-memory -6]
   [:profiling-info-not-available -7]
   [:mem-copy-overlap -8]
   [:image-format-mismatch -9]
   [:image-format-not-supported -10]
   [:build-program-failure -11]
   [:map-failure -12]
   [:misaligned-sub-buffer-offset -13]
   [:exec-status-error-for-events-in-wait-list -14]
   [:compile-program-failure -15]
   [:linker-not-available -16]
   [:link-program-failure -17]
   [:device-partition-failed -18]
   [:kernel-arg-info-not-available -19]
   [:invalid-value -30]
   [:invalid-device-type -31]
   [:invalid-platform -32]
   [:invalid-device -33]
   [:invalid-context -34]
   [:invalid-queue-properties -35]
   [:invalid-command-queue -36]
   [:invalid-host-ptr -37]
   [:invalid-mem-object -38]
   [:invalid-image-format-descriptor -39]
   [:invalid-image-size -40]
   [:invalid-sampler -41]
   [:invalid-binary -42]
   [:invalid-build-options -43]
   [:invalid-program -44]
   [:invalid-program-executable -45]
   [:invalid-kernel-name -46]
   [:invalid-kernel-definition -47]
   [:invalid-kernel -48]
   [:invalid-arg-index -49]
   [:invalid-arg-value -50]
   [:invalid-arg-size -51]
   [:invalid-kernel-args -52]
   [:invalid-work-dimension -53]
   [:invalid-work-group-size -54]
   [:invalid-work-item-size -55]
   [:invalid-global-offset -56]
   [:invalid-event-wait-list -57]
   [:invalid-event -58]
   [:invalid-operation -59]
   [:invalid-gl-object -60]
   [:invalid-buffer-size -61]
   [:invalid-mip-level -62]
   [:invalid-global-work-size -63]
   [:invalid-property -64]
   [:invalid-image-descriptor -65]
   [:invalid-compiler-options -66]
   [:invalid-linker-options -67]
   [:invalid-device-partition-count -68]])

(def error-lookup
  (->> (map (comp vec reverse) +error-codes+)
       (into {})))


;;
;; Platform
;;

(defn list-platforms
  "lists all platforms
 
   (list-platforms)"
  {:added "3.0"}
  ([] (fn-call :platform-ids {})))

(resource/res:spec-add
 {:type :hara/opencl.platform
  :instance {:create (fn [_] (first (list-platforms)))}})

(defn platform:default
  "gets the default platform
 
   (platform:default)"
  {:added "3.0"}
  ([]
   (resource/res :hara/opencl.platform)))

;;
;; Device
;;

(defn list-devices
  "lists all devices
 
   (list-devices (platform:default))"
  {:added "3.0"}
  ([platform-id]
   (list-devices platform-id :all))
  ([platform-id device-type]
   (fn-call :device-ids {}
            platform-id
            (or (cl-device-type [device-type])
                (h/error "Invalid type" {:input device-type})))))

(defn device-info
  "shows the device info"
  {:added "3.0"}
  ([device-id]
   (device-info device-id :summary))
  ([device-id show]
   (let [summary [:address-bits
                  :type
                  :name
                  :available
                  :global-mem-cache-type
                  :global-mem-cacheline-size
                  :global-mem-size
                  :host-unified-memory
                  :max-clock-frequency
                  :max-compute-units
                  :max-samplers
                  :max-work-group-size
                  :image2d-max-height
                  :image2d-max-width
                  :image3d-max-depth
                  :image3d-max-height
                  :image3d-max-width
                  :local-mem-size
                  :local-mem-type
                  :opencl-c-version:version
                  :version
                  :vendor]
         fns (case show
               :summary (select-keys +device-info+ summary)
               :all +device-info+
               (select-keys +device-info+ show))]
     (coll/map-vals (partial h/call device-id) fns))))

(resource/res:spec-add
 {:type :hara/opencl.cpu
  :instance {:create (fn [_]
                       (first (filter (comp #{:cpu} device-info:type)
                                      (list-devices (platform:default)))))}})

(resource/res:spec-add
 {:type :hara/opencl.gpu
  :instance {:create (fn [_]
                       (first (filter (comp #{:gpu} device-info:type)
                                      (list-devices (platform:default)))))}})

(defn device:cpu
  "gets the default cpu device"
  {:added "3.0"}
  ([]
   (resource/res :hara/opencl.cpu)))

(defn device:gpu
  "gets the default gpu device"
  {:added "3.0"}
  ([]
   (resource/res :hara/opencl.gpu)))

