(ns jvm.chisel-elaborate-test
  (:use code.test)
  (:require [jvm.chisel :as ch]))

(defn- pred-eq
  []
  (ch/module
   {:name "PredicateEq"}
   (fn []
     (let [io (ch/io
               (ch/bundle
                [[:values    (ch/input  (ch/vec 8 (ch/uint 8)))]
                 [:validMask (ch/input  (ch/uint 8))]
                 [:constant  (ch/input  (ch/uint 8))]
                 [:matchMask (ch/output (ch/uint 8))]]))
           matches (mapv (fn [i]
                           (ch/and (ch/index (ch/field io :validMask) i)
                                   (ch/eq (ch/index (ch/field io :values) i)
                                          (ch/field io :constant))))
                         (range 8))]
       (ch/connect! (ch/field io :matchMask)
                    (ch/vec-as-uint matches))))))

(defn- op-mod
  []
  (ch/module
   {:name "OpMod"}
   (fn []
     (let [io (ch/io
               (ch/bundle
                [[:a     (ch/input  (ch/uint 8))]
                 [:b     (ch/input  (ch/uint 8))]
                 [:yAdd  (ch/output (ch/uint 8))]
                 [:ySub  (ch/output (ch/uint 8))]
                 [:yMux  (ch/output (ch/uint 8))]
                 [:yCat  (ch/output (ch/uint 16))]
                 [:yWhen (ch/output (ch/uint 8))]]))]
       (ch/connect! (ch/field io :yAdd) (ch/add (ch/field io :a) (ch/field io :b)))
       (ch/connect! (ch/field io :ySub) (ch/sub (ch/field io :a) (ch/field io :b)))
       (ch/connect! (ch/field io :yMux) (ch/mux (ch/eq (ch/field io :a) (ch/field io :b))
                                                (ch/field io :a) (ch/field io :b)))
       (ch/connect! (ch/field io :yCat) (ch/cat (ch/field io :a) (ch/field io :b)))
       (ch/connect! (ch/field io :yWhen) (ch/field io :b))
       (ch/when (ch/eq (ch/field io :a) (ch/field io :b))
         (ch/connect! (ch/field io :yWhen) (ch/field io :a)))))))

^{:refer jvm.chisel/emit-firrtl :added "4.1"}
(fact "elaborates the PredicateEq scan block to FIRRTL"
  (let [fir (ch/emit-firrtl (pred-eq))]
    (.contains fir "module PredicateEq") => true
    (.contains fir "io : { matchMask : UInt<8>") => true
    (.contains fir "flip values : UInt<8>[8]") => true
    (count (re-seq #"eq\(io\.values\[" fir)) => 8
    (.contains fir "connect io.matchMask") => true))

^{:refer jvm.chisel/emit-system-verilog :added "4.1"}
(fact "emits SystemVerilog for PredicateEq (via the bundled firtool resolver)"
  (let [sv (ch/emit-system-verilog (pred-eq))]
    (.contains sv "module PredicateEq") => true
    (.contains sv "matchMask") => true))

^{:refer jvm.chisel/module :added "4.1"}
(fact "elaborates arithmetic, mux, cat and when inside a module"
  (let [fir (ch/emit-firrtl (op-mod))]
    (.contains fir "module OpMod") => true
    (.contains fir "add(") => true
    (.contains fir "sub(") => true
    (.contains fir "mux(") => true
    (.contains fir "cat(") => true
    (.contains fir "when ") => true))
