(ns jvm.chisel.db-test
  (:use code.test)
  (:require [clojure.string :as str]
            [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(defn- fragment-module []
  (ch/module {:name "DbFragments"}
    (fn []
      (let [io (ch/io (ch/bundle [[:values (ch/input (ch/vec 4 (ch/uint 8)))]
                                  [:mask (ch/input (ch/uint 4))]
                                  [:key (ch/input (ch/uint 8))]
                                  [:idx (ch/input (ch/uint 2))]
                                  [:cmp (ch/output (ch/uint 4))]
                                  [:pop (ch/output (ch/uint 3))]
                                  [:gated (ch/output (ch/vec 4 (ch/uint 8)))]
                                  [:hash (ch/output (ch/uint 2))]
                                  [:oneHot (ch/output (ch/uint 4))]]))
            values (ch/field io :values)
            mask (ch/field io :mask)
            cmps (db/cmp-vec values (ch/u 3 8) ch/gte 4)
            gated (db/gated values mask 4 (ch/u 0 8))]
        (ch/connect! (ch/field io :cmp) (db/mask-pack cmps))
        (ch/connect! (ch/field io :pop) (db/popcount mask 4))
        (doseq [i (range 4)]
          (ch/connect! (ch/index (ch/field io :gated) i) (gated i)))
        (ch/connect! (ch/field io :hash) (db/mhash (ch/field io :key) 8 0x9E 2))
        (ch/connect! (ch/field io :oneHot) (db/one-hot (ch/field io :idx) 4))))))

(def ^:private fragment-fir (delay (ch/emit-firrtl (fragment-module))))
(defn- has? [s] (str/includes? @fragment-fir s))

^{:refer jvm.chisel.db/cmp-vec :added "4.1"}
(fact "cmp-vec emits one comparison per lane"
  (count (re-seq #"geq\(" @fragment-fir)) => 4)

^{:refer jvm.chisel.db/mask-pack :added "4.1"}
(fact "mask-pack emits a four-bit lane mask"
  (has? "cmp : UInt<4>") => true)

^{:refer jvm.chisel.db/tree-reduce :added "4.1"}
(fact "tree-reduce preserves its identity and combines every element"
  (db/tree-reduce + 0 []) => 0
  (db/tree-reduce + 0 [1 2 3 4 5]) => 15
  (db/tree-reduce vector :zero [:a]) => :a)

^{:refer jvm.chisel.db/popcount :added "4.1"}
(fact "popcount emits a correctly widened adder tree"
  (has? "pop : UInt<3>") => true
  (count (re-seq #"add\(" @fragment-fir)) => 3)

^{:refer jvm.chisel.db/gated :added "4.1"}
(fact "gated emits one mask-controlled mux per lane"
  (count (re-seq #"mux\(" @fragment-fir)) => 4
  (has? "gated : UInt<8>[4]") => true)

^{:refer jvm.chisel.db/mhash :added "4.1"}
(fact "mhash multiplies and extracts the requested high bits"
  (has? "mul(io.key") => true
  (has? "hash : UInt<2>") => true)

^{:refer jvm.chisel.db/one-hot :added "4.1"}
(fact "one-hot emits a dynamic shift into the requested width"
  (has? "dshl(") => true
  (has? "oneHot : UInt<4>") => true)

^{:refer jvm.chisel.db/log2-ceil :added "4.1"}
(fact "log2-ceil sizes inclusive counts"
  (mapv db/log2-ceil [0 1 2 3 7 8]) => [0 1 2 2 3 4])

^{:refer jvm.chisel.db/log2 :added "4.1"}
(fact "log2 returns reduction-tree depth"
  (mapv db/log2 [1 2 3 4 8]) => [0 1 2 2 3])
