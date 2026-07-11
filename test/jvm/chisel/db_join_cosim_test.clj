(ns jvm.chisel.db-join-cosim-test
  "Value co-simulation of `jvm.chisel.db.join` (build + probe) against its Clojure
   reference models, using `jvm.chisel.testing` (Verilator). Skipped unless
   `verilator` is on PATH. Each `ct/simulate` recompiles the design, so each fact
   batches its whole oracle table inside one body."
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel.testing :as ct]
            [jvm.chisel.db.join :as j]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(def ^:private lanes 8)
(def ^:private width 8)
(def ^:private buckets 16)
(def ^:private k 0x9E)

(defn- rand-keys []
  (vec (repeatedly lanes #(rand-int (bit-shift-left 1 width)))))

(defn- b01 [b] (if b 1 0))

^{:refer jvm.chisel.db.join/join-build-module :added "4.1"}
(fact "build co-sim: tableValid and tableKeys match join-build-ref"
  (let [fixed [[0 1 2 3 4 5 6 7]
               [3 7 11 42 99 200 0 1]
               [255 0 255 0 255 0 255 0]
               [7 7 7 7 7 7 7 7]]
        cases (concat fixed (repeatedly 4 rand-keys))
        masks [0xFF 0x0F 0xF0 0xAA 0x55 0x00 0x01]]
    (ct/simulate
     (j/join-build-module {:lanes lanes :width width :buckets buckets :k k :name "JoinBuildCo"})
     (fn [ctx]
       (let [{:keys [port poke expect step]} ctx]
         (doseq [keys cases
                 mask masks
                 :let [t (j/join-build-ref keys mask width buckets k)]]
           (ct/poke-vec! ctx "keys" keys)
           (poke (port "validMask") (long mask))
           (step)
           (expect (port "tableValid") (long (:valid t)))
           (ct/expect-vec! ctx "tableKeys" (:keys t)))))))
  => nil)

^{:refer jvm.chisel.db.join/join-probe-module :added "4.1"}
(fact "probe co-sim: match matches join-probe-ref over inserted/absent/colliding keys"
  (let [build-keys [3 7 11 42 99 200 0 1]
        t          (j/join-build-ref build-keys 0xFF width buckets k)
        probes     (concat build-keys                 ;; inserted (some may be overwritten)
                           [60 128 255]               ;; absent / other buckets
                           (repeatedly 16 #(rand-int 256)))]
    (ct/simulate
     (j/join-probe-module {:width width :buckets buckets :k k :name "JoinProbeCo"})
     (fn [ctx]
       (let [{:keys [port poke expect step]} ctx]
         (poke (port "tableValid") (long (:valid t)))
         (ct/poke-vec! ctx "tableKeys" (:keys t))
         (doseq [pk probes
                 :let [want (j/join-probe-ref pk t width buckets k)]]
           (poke (port "key") (long pk))
           (step)
           (expect (port "match") (long (b01 want))))))))
  => nil)
