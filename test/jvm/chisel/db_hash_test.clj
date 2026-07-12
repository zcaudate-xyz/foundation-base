(ns jvm.chisel.db-hash-test
  (:use code.test)
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.hash :as hash]))

^{:refer jvm.chisel.db.hash/hash-ref :added "4.1"}
(fact "hash-ref takes the high log-n bits of key*K"
  ;; width 8, K 0x9E=158, 8 buckets -> log-n 3 -> high 3 bits of 16-bit product
  (hash/hash-ref 1   8 0x9E 3) => 0
  (hash/hash-ref 100 8 0x9E 3) => 1
  (hash/hash-ref 255 8 0x9E 3) => 4)

^{:refer jvm.chisel.db.hash/hash-module :added "4.1" :id test-hash-module-1}
(fact "hash-module elaborates with one multiply and a high-bits extract"
  (let [fir (ch/emit-firrtl
             (hash/hash-module {:width 8 :buckets 8 :k 0x9E :name "Hash8"}))]
    (.contains fir "module Hash8") => true
    (.contains fir "bucket : UInt<3>") => true
    (count (re-seq #"mul\(" fir)) => 1
    (.contains fir "bits(_T, 15, 13)") => true))

^{:refer jvm.chisel.db.hash/hash-module :added "4.1" :id test-hash-module-2}
(fact "hash-module emits SystemVerilog"
  (let [sv (ch/emit-system-verilog
            (hash/hash-module {:width 8 :buckets 16 :k 0x9E :name "Hash16"}))]
    (.contains sv "module Hash16") => true))
