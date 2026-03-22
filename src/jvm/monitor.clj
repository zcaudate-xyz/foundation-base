(ns jvm.monitor
  (:require [std.lib.collection :as collection]
            [std.object :as object])
  (:import (java.lang.management ClassLoadingMXBean CompilationMXBean GarbageCollectorMXBean ManagementFactory MemoryMXBean MemoryManagerMXBean MemoryPoolMXBean MemoryUsage OperatingSystemMXBean RuntimeMXBean ThreadMXBean)))

(object/map-like

 ClassLoadingMXBean
 {:tag "class-loading"}
 CompilationMXBean
 {:tag "compilation"}
 GarbageCollectorMXBean
 {:tag "gc"}
 MemoryMXBean
 {:tag "memory"}
 MemoryPoolMXBean
 {:tag "memory-pool"
  :exclude [:collection-usage-threshold-exceeded?
            :collection-usage-threshold-count
            :collection-usage-threshold
            :collection-usage
            :usage-threshold
            :usage-threshold-count
            :usage-threshold-exceeded?]}
 MemoryManagerMXBean
 {:tag "memory-manager"}
 OperatingSystemMXBean
 {:tag "os"}
 RuntimeMXBean
 {:tag "runtime"
  :exclude [:boot-class-path]}
 ThreadMXBean
 {:tag "thread"}
 MemoryUsage
 {:tag "usage"})

(object/map-like
 sun.management.RuntimeImpl
 {:tag "runtime"
  :exclude [:boot-class-path]})

(defn class-loading-bean
  "gives information about class loading
 
   (object/to-map (class-loading-bean))
   => (contains {:loaded-class-count number?
                 :total-loaded-class-count number?
                 :unloaded-class-count number?})"
  {:added "3.0"}
  []
  (ManagementFactory/getClassLoadingMXBean))

(defn compilation-bean
  "gives information about compilation
 
   (object/to-map (compilation-bean))
   => (contains {:name string?
                 :total-compilation-time number?})"
  {:added "3.0"}
  []
  (ManagementFactory/getCompilationMXBean))

(defn gc-bean
  "gives information about the garbage collector
 
   (object/to-map (first (gc-bean)))
   => (contains {:collection-count number?
                 :collection-time number?})"
  {:added "3.0"}
  []
  (ManagementFactory/getGarbageCollectorMXBeans))

(defn memory-bean
  "gives information about the system memory
 
   (object/to-map (memory-bean))
   => (contains-in {:heap-memory-usage
                    {:committed number?
                     :init number?
                     :max number?
                     :used number?}
                    :non-heap-memory-usage
                    {:committed  number?
                    :init number?
                     :max number?
                     :used number?}
                    :object-pending-finalization-count number?})"
  {:added "3.0"}
  []
  (ManagementFactory/getMemoryMXBean))

(defn memory-manager-bean
  "gives information about the memory manager
 
   (object/to-data (memory-manager-bean))
   => (any (contains-in [{:memory-pool-names [string?],
                          :name string?}])
           map?)"
  {:added "3.0"}
  []
  (ManagementFactory/getMemoryManagerMXBeans))

(defn memory-pool-bean
  "gives information about the memory pool
 
   (object/to-map (first (memory-pool-bean)))"
  {:added "3.0"}
  []
  (ManagementFactory/getMemoryPoolMXBeans))

(defn os-bean
  "gives information about the operating system
 
   (object/to-map (os-bean))
   => (contains {:arch string?
                 :available-processors number?
                 :name string?
                 :system-load-average number?
                 :version string?})"
  {:added "3.0"}
  []
  (ManagementFactory/getOperatingSystemMXBean))

(defn runtime-bean
  "gives information about the system runtime
 
   (object/to-map (runtime-bean))"
  {:added "3.0"}
  []
  (ManagementFactory/getRuntimeMXBean))

(defn thread-bean
  "gives information about the current thread executor
 
   (object/to-map (thread-bean))"
  {:added "3.0"}
  []
  (ManagementFactory/getThreadMXBean))

(def jvm-map
  {:class-loading class-loading-bean
   :compilation compilation-bean
   :gc gc-bean
   :memory memory-bean
   :memory-manager memory-manager-bean
   :memory-pool memory-pool-bean
   :os os-bean
   :runtime runtime-bean
   :thread thread-bean})

(defn jvm
  "Access to all `java.lang.management.ManagementFactory` MXBean methods
 
   (jvm)
   => [:class-loading :compilation :gc :memory :memory-manager :memory-pool :os :runtime :thread]
 
   (jvm :gc)
   ;; => [{:collection-count 33, :collection-time 1089}
   ;;     {:collection-count 4, :collection-time 585}]
 
   (jvm :all)
   ;; => <All MX Results>"
  {:added "3.0"}
  ([]
   (sort (keys jvm-map)))
  ([k]
   (cond (= k :all)
         (->> jvm-map
              (collection/map-vals (fn [f]
                            (object/to-data (f)))))

         (jvm-map k)
         (object/to-data ((jvm-map k)))

         :else
         (jvm))))
