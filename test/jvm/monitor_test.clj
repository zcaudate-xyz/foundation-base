(ns jvm.monitor-test
  (:require [jvm.monitor :refer :all]
            [std.object :as object])
  (:use code.test))

^{:refer jvm.monitor/class-loading-bean :added "3.0"}
(fact "gives information about class loading"

  (object/to-map (class-loading-bean))
  => (contains {:loaded-class-count number?
                :total-loaded-class-count number?
                :unloaded-class-count number?}))

^{:refer jvm.monitor/compilation-bean :added "3.0"}
(fact "gives information about compilation"

  (object/to-map (compilation-bean))
  => (contains {:name string?
                :total-compilation-time number?}))

^{:refer jvm.monitor/gc-bean :added "3.0"}
(fact "gives information about the garbage collector"

  (object/to-map (first (gc-bean)))
  => (contains {:collection-count number?
                :collection-time number?}))

^{:refer jvm.monitor/memory-bean :added "3.0"}
(fact "gives information about the system memory"

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
                   :object-pending-finalization-count number?}))

^{:refer jvm.monitor/memory-manager-bean :added "3.0"}
(fact "gives information about the memory manager"

  (object/to-data (memory-manager-bean))
  => (any (contains-in [{:memory-pool-names [string?],
                         :name string?}])
          map?))

^{:refer jvm.monitor/memory-pool-bean :added "3.0"}
(fact "gives information about the memory pool"

  (object/to-map (first (memory-pool-bean))))

^{:refer jvm.monitor/os-bean :added "3.0"}
(fact "gives information about the operating system"

  (object/to-map (os-bean))
  => (contains {:arch string?
                :available-processors number?
                :name string?
                :system-load-average number?
                :version string?}))

^{:refer jvm.monitor/runtime-bean :added "3.0"}
(comment "gives information about the system runtime"

  (object/to-map (runtime-bean)))

^{:refer jvm.monitor/thread-bean :added "3.0"}
(fact "gives information about the current thread executor"

  (object/to-map (thread-bean)))

^{:refer jvm.monitor/jvm :added "3.0"}
(fact "Access to all `java.lang.management.ManagementFactory` MXBean methods"

  (jvm)
  => [:class-loading :compilation :gc :memory :memory-manager :memory-pool :os :runtime :thread]

  (jvm :gc)
  ;; => [{:collection-count 33, :collection-time 1089}
  ;;     {:collection-count 4, :collection-time 585}]

  (jvm :all)
  ;; => <All MX Results> 
  )

(comment
  (code.manage/import)
  (require '[code.test.common :as common])
  (require '[code.project :as project])
  (def project (project/project))
  (def all-files (project/all-files (:test-paths common/*settings*)
                                    {}
                                    project))
  (doseq [n (keys all-files)]
    (code.test/run-namespace n)))
