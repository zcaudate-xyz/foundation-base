(ns jvm.chisel.db.bloom-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.bloom :as bloom]))

(def ^:private ks [0x9E 0x3F 0x55])

^{:refer jvm.chisel.db.bloom/bloom-probe-ref :added "4.1"}
(fact "probe is always false on an empty filter"
  (bloom/bloom-probe-ref 123 0 8 64 ks) => false
  (bloom/bloom-probe-ref 0   0 8 64 ks) => false)

^{:refer jvm.chisel.db.bloom/bloom-insert-ref :added "4.1"}
(fact "a key always probes positive after it was inserted"
  (let [bits (bloom/bloom-insert-ref 123 0 8 64 ks)]
    (pos? bits) => true
    (bloom/bloom-probe-ref 123 bits 8 64 ks) => true
    (count (filter #(pos? (bit-and bits (bit-shift-left 1 %))) (range 64))) => 3))

^{:refer jvm.chisel.db.bloom/bloom-probe-module :added "4.1" :id test-bloom-probe-module-1}
(fact "bloom probe elaborates one multiply per hash and a per-bit neq"
  (let [fir (ch/emit-firrtl
             (bloom/bloom-probe-module {:width 8 :bits-count 64 :ks ks :name "BloomProbe8"}))]
    (.contains fir "module BloomProbe8") => true
    (.contains fir "hit : UInt<1>") => true
    (count (re-seq #"mul\(" fir)) => 3
    (.contains fir "neq(") => true))

^{:refer jvm.chisel.db.bloom/bloom-probe-module :added "4.1" :id test-bloom-probe-module-2}
(fact "bloom probe emits SystemVerilog"
  (let [sv (ch/emit-system-verilog
            (bloom/bloom-probe-module {:width 8 :bits-count 64 :ks ks :name "BloomProbe8SV"}))]
    (.contains sv "module BloomProbe8SV") => true))


(defn- bloom-fragment-module []
  (ch/module {:name "BloomFragments"}
    (fn []
      (let [io (ch/io (ch/bundle [[:key (ch/input (ch/uint 8))]
                                  [:bits (ch/input (ch/uint 16))]
                                  [:inserted (ch/output (ch/uint 16))]
                                  [:hit (ch/output (ch/bool))]]))]
        (ch/connect! (ch/field io :inserted)
                     (bloom/insert-data (ch/field io :key) (ch/field io :bits)
                                        8 16 [0x9E 0x5D]))
        (ch/connect! (ch/field io :hit)
                     (bloom/probe-data (ch/field io :key) (ch/field io :bits)
                                       8 16 [0x9E 0x5D]))))))

(def ^:private fragment-fir (delay (ch/emit-firrtl (bloom-fragment-module))))

^{:refer jvm.chisel.db.bloom/probe-data :added "4.1"}
(fact "probe-data combines every hashed membership test"
  (count (re-seq #"neq\(" @fragment-fir)) => 2
  (.contains @fragment-fir "connect io.hit") => true)

^{:refer jvm.chisel.db.bloom/insert-data :added "4.1"}
(fact "insert-data sets every hashed position while preserving existing bits"
  (count (re-seq #"dshl\(" @fragment-fir)) => 4
  (.contains @fragment-fir "connect io.inserted") => true)
