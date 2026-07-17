(ns jvm.chisel.db.join-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.join :as j]
            [jvm.chisel.db.hash :as h]))

(def ^:private width 8)
(def ^:private buckets 16)
(def ^:private k 0x9E)
(defn- bkt [x] (h/hash-ref x width k 4))

^{:refer jvm.chisel.db.join/join-build-ref :added "4.1" :id test-join-build-ref-1}
(fact "build-ref: table length and valid-bitmask reflect occupied buckets"
  (let [keys [3 7 11 42 99 200]
        t    (j/join-build-ref keys 2r11111111 width buckets k)]
    (count (:keys t))            => buckets
    (Long/bitCount (:valid t))   => (count (set (map bkt keys)))))

^{:refer jvm.chisel.db.join/join-probe-ref :added "4.1" :id test-join-probe-ref-1}
(fact "probe hits exactly when the key is the last writer in its bucket"
  (let [keys (vec (range 0 256 17))                 ;; 16 widely-spread keys
        mask (dec (bit-shift-left 1 (count keys)))
        t    (j/join-build-ref keys mask width buckets k)]
    (every? (fn [x]
              (= (j/join-probe-ref x t width buckets k)
                 (= x (nth (:keys t) (bkt x)))))
            keys)
    => true))

^{:refer jvm.chisel.db.join/join-build-ref :added "4.1" :id test-join-build-ref-2}
(fact "last-writer-wins on collision: probing the overwritten key misses"
  ;; keys 0 and 1 both hash to bucket 0 with k=0x9E, width 8
  (bkt 0) => (bkt 1)
  (let [t (j/join-build-ref [0 1] 2r11 width buckets k)]
    (nth (:keys t) (bkt 0))                 => 1     ;; lane 1 survives
    (j/join-probe-ref 1 t width buckets k)  => true  ;; survivor hits
    (j/join-probe-ref 0 t width buckets k)  => false ;; overwritten misses
    (Long/bitCount (:valid t))              => 1))   ;; single occupied bucket

^{:refer jvm.chisel.db.join/join-probe-ref :added "4.1" :id test-join-probe-ref-2}
(fact "probe into an empty bucket misses"
  (let [t (j/join-build-ref [0 1] 2r11 width buckets k)]   ;; only bucket 0 occupied
    (bkt 60)                               => 2       ;; key 60 maps to bucket 2
    (j/join-probe-ref 60 t width buckets k) => false))  ;; bucket 2 empty -> miss

^{:refer jvm.chisel.db.join/join-build-ref :added "4.1" :id test-join-build-ref-3}
(fact "valid-mask gates which lanes are inserted"
  (let [t (j/join-build-ref [0 1] 2r01 width buckets k)]   ;; only lane 0 inserted
    (nth (:keys t) (bkt 0))                 => 0
    (j/join-probe-ref 0 t width buckets k)  => true
    (j/join-probe-ref 1 t width buckets k)  => false))  ;; lane 1 was masked out


(def ^:private build-fir
  (delay (ch/emit-firrtl
          (j/join-build-module {:lanes 4 :width width :buckets 4 :k k
                                :name "JoinBuildUnit"}))))
(def ^:private probe-fir
  (delay (ch/emit-firrtl
          (j/join-probe-module {:width width :buckets 4 :k k
                                :name "JoinProbeUnit"}))))

^{:refer jvm.chisel.db.join/build-table-data :added "4.1"}
(fact "build-table-data emits one key and valid bit per bucket"
  (.contains @build-fir "tableValid : UInt<4>") => true
  (.contains @build-fir "tableKeys : UInt<8>[4]") => true
  (count (re-seq #"mux\(" @build-fir)) => 16)

^{:refer jvm.chisel.db.join/join-probe-data :added "4.1"}
(fact "join-probe-data dynamically selects and validates a bucket"
  (.contains @probe-fir "tableKeys[io.tableValid") => false
  (.contains @probe-fir "connect io.match") => true
  (.contains @probe-fir "eq(") => true)

^{:refer jvm.chisel.db.join/join-build-module :added "4.1"}
(fact "join-build-module elaborates its direct-mapped table interface"
  (.contains @build-fir "module JoinBuildUnit") => true)

^{:refer jvm.chisel.db.join/join-probe-module :added "4.1"}
(fact "join-probe-module elaborates its lookup interface"
  (.contains @probe-fir "module JoinProbeUnit") => true
  (.contains @probe-fir "match : UInt<1>") => true)
