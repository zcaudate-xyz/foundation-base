(ns lib.oshi
  (:require [std.object :as object]
            [lib.oshi.interop :deps true])
  (:import (oshi.software.os OperatingSystem$ProcessSorting)))

(defonce ^:dynamic *raw* true)

(defn to-data
  "converts the object to a map provided *raw* is false"
  {:added "3.0"}
  ([obj]
   (if *raw*
     obj
     (object/to-data obj))))

(defn all
  "returns the entire map of the current system
 
   (all)
   ;; => <EVERYTHING>"
  {:added "3.0"}
  ([]
   (binding [*raw* false]
     (to-data (oshi.SystemInfo.)))))

(defn computer-system
  "returns specs of  the current system
 
   (computer-system)
   ;; => #sys{:baseboard {:manufacturer \"Apple Inc.\" :model \"SMC\"},
   ;;         :firmware {:description \"unknown\",
   ;;                    :manufacturer \"Apple Inc.\",
   ;;                    :name \"EFI\",
   ;;                    :version \"MBP114.0172.B13\"},
   ;;         :manufacturer \"Apple Inc.\",
   ;;         :model \"MacBook Pro (MacBookPro11,5)\", :serial-number \"C02RR24EG8WP\"}"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getComputerSystem)
       (to-data))))

(defn fs
  "returns disk store information
 
   (fs)
   ;; => (#disk{:writes 157554, :write-bytes 4209285632,
   ;;           :name \"disk0\", :transfer-time 8968912,
   ;;           :size 500277790720, :serial \"S29ANYAH561132\"
   ;;           :partitions [{:identification \"disk0s1\", :major 1, :minor 1,
   ;;                         :mount-point \"\", :name \"EFI\", :size 209715200,
   ;;                         :type \"EFI System Partition\"}]
   ;;           :time-stamp 1487147164390, :read-bytes 3176375808,
   ;;           :reads 132357, :model \"APPLE SSD SM0512G\"})"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getDiskStores)
       seq
       (to-data))))

(defn displays
  "returns display information
 
   (displays)
   ;; => (#display{:edid [0 -1 -1 -1 -1 -1 -1 0 6 16 46 -96 ...
   ;;                     ...
   ;;                     0 0 0 0 16 0 0 0 0 0 0 0 0 0 0 0 0
   ;;                     ...
   ;;                     0 0 0 -48]})"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getDisplays)
       seq
       (to-data))))

(defn memory
  "returns memory information
 
   (memory)
   ;; => #memory{:available 4792446976,
   ;;            :swap-total 0,
   ;;            :swap-used 0,
   ;;            :total 17179869184}"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getMemory)
       (to-data))))

(defn network-ifs
  "returns network interface information
 
   (network-ifs)
   ;; => (#net{:packets-recv 0, :mtu 1484, :speed 10000000,
   ;;          :macaddr \"52:33:aa:41:86:27\",
   ;;          :packets-sent 28, :name \"awdl0\", :ipv4addr [],
   ;;          :bytes-sent 6460, :out-errors 0, 
   ;;          :in-errors 0, :display-name \"awdl0\",
   ;;          :ipv6addr [\"fe80:0:0:0:5033:aaff:fe41:8627\"],
   ;;          :time-stamp 1487147416141, :bytes-recv 0})"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getNetworkIFs)
       seq
       (to-data))))

(defn power
  "returns power information
 
   (power)
   ;; => (#power{:name \"InternalBattery-0\",
   ;;            :remaining-capacity 1.0,
   ;;            :time-remaining -2.0})"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getPowerSources)
       seq
       (to-data))))

(defn cpu
  "returns cpu information
 
   (cpu)
   ;; => #cpu{:vendor \"GenuineIntel\",
   ;;         :physical-processor-count 4,
   ;;         :system-serial-number \"C02RR24EG8WP\",
   ;;         :system-cpu-load 0.03085702451764327, :family \"6\",
   ;;         :logical-processor-count 8,
   ;;         :name \"Intel(R) Core(TM) i7-4870HQ CPU @ 2.50GHz\",
   ;;         :system-cpu-load-between-ticks 0.04217839588279096,
   ;;         :cpu64bit? true,
   ;;         :identifier \"Intel64 Family 6 Model 70 Stepping 1\",
   ;;         :system-uptime 30980, :stepping \"1\",
   ;;         :system-load-average 1.19970703125,
   ;;         :vendor-freq 2500000000, :model \"70\"}"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getProcessor)
       (to-data))))

(defn sensors
  "returns sensor information
 
   (sensors)
   ;; => #sensors{:cpu-temperature 42.125,
   ;;             :cpu-voltage 173798.992,
   ;;             :fan-speeds [2163 1999]}"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getSensors)
       (to-data))))

(defn usb
  "returns usb information
 
   (usb)
   ;; => (#usb{:name \"Apple Internal Keyboard / Trackpad\",
   ;;          :product-id \"0274\"
   ;;          :vendor \"Apple Inc.\"}
   ;;     #usb{:name \"Bluetooth USB Host Controller\",
   ;;          :product-id \"8290\",
   ;;          :vendor \"Broadcom Corp.\"})"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getHardware)
       (.getUsbDevices false)
       seq
       (to-data))))

(defn os
  "returns operating system information
 
   (os)
   ;; => #os{:family \"macOS\",
   ;;        :file-system {:file-stores [{:description \"Local Disk\", :mount \"/\",
   ;;                                     :name \"Macintosh HD\",
   ;;                                     :total-space 499055067136,
   ;;                                     :type \"hfs\",
   ;;                                     :usable-space 458689961984,
   ;;                                     :volume \"/dev/disk1\"}],
   ;;                      :max-file-descriptors 12288,
   ;;                      :open-file-descriptors 3396},
   ;;        :manufacturer \"Apple\",
   ;;        :process-count 318,
   ;;        :process-id 9317,
   ;;        :thread-count 574,
   ;;        :version {:build-number \"16D32\"
   ;;                  :code-name \"Sierra\",
   ;;                  :osx-version-number 12,
   ;;                  :version \"10.12.3\"}}"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getOperatingSystem)
       (to-data))))

(defn process-id
  "returns the current process id
 
   (process-id)
   ;;=> 9317"
  {:added "3.0"}
  ([]
   (-> (oshi.SystemInfo.)
       (.getOperatingSystem)
       (.getProcessId))))

(defn process
  "returns information about a process given it's id
 
   (process (process-id))
   ;; => #process{:parent-process-id 9314,
   ;;             :path \"/Library/Java/JavaVirtualMachines/jdk1.8.0_112.jdk/Contents/Home/bin/java\",
   ;;             :kernel-time 7427,
   ;;             :resident-set-size 1756151808,
   ;;             :user-time 48685,
   ;;             :virtual-size 8653258752,
   ;;             :name \"java\",
   ;;             :start-time 1487140389429,
   ;;             :state \"WAITING\",
   ;;             :thread-count 41,
   ;;             :bytes-written 253952,
   ;;             :priority 31, 
   ;;             :bytes-read 16384,
   ;;             :up-time 7672182,
   ;;             :process-id 9317}"
  {:added "3.0"}
  ([id]
   (-> (oshi.SystemInfo.)
       (.getOperatingSystem)
       (.getProcess id))))

(defn list-processes
  "returns information about all running processes on the os
 
   ;; ordering options:
   #{:name :oldest :memory :pid :newest :cpu :parentpid}
 
   (list-processes (process-id) :name)
   ;; => (#process{:parent-process-id 1,
   ;;              :path \"/System/Library/Frameworks/Accounts.framework/Versions/A/Support/accountsd\",
   ;;              :name \"accountsd\",
   ;;              :bytes-read 4450304,
   ;;              :up-time 31524268,
   ;;              :process-id 1276}
   ;;     #process{:parent-process-id 1,
   ;;              :path \"/Applications/Utilities/Activity Monitor.app/Contents/MacOS/Activity Monitor\",
   ;;              :name \"Activity Monitor\",
   ;;              :bytes-read 61440,
   ;;              :up-time 31522999,
   ;;              :process-id 1360})"
  {:added "3.0"}
  ([] (list-processes 50 :cpu))
  ([limit] (list-processes limit :cpu))
  ([limit sort-key]
   (-> (oshi.SystemInfo.)
       (.getOperatingSystem)
       (.getProcesses)
       seq
       (to-data))))
