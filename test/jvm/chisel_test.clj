(ns jvm.chisel-test
  (:use code.test)
  (:require [clojure.string :as str]
            [jvm.chisel :as ch]))

(defn- child-module []
  (ch/module {:name "PublicChild"}
    (fn []
      (let [io (ch/io (ch/bundle [[:in (ch/input (ch/uint 8))]
                                  [:out (ch/output (ch/uint 8))]]))]
        (ch/connect! (ch/field io :out) (ch/field io :in))))))

(defn- public-module []
  (ch/module {:name "PublicFixture"}
    (fn []
      (let [io (ch/io (ch/bundle
                       [[:a (ch/input (ch/uint 8))]
                        [:b (ch/input (ch/uint 8))]
                        [:sel (ch/input (ch/bool))]
                        [:vecIdx (ch/input (ch/uint 2))]
                        [:bitIdx (ch/input (ch/uint 3))]
                        [:values (ch/input (ch/vec 4 (ch/uint 8)))]
                        [:yAdd (ch/output (ch/uint 8))]
                        [:ySub (ch/output (ch/uint 8))]
                        [:yMul (ch/output (ch/uint 16))]
                        [:yEq (ch/output (ch/bool))]
                        [:yNeq (ch/output (ch/bool))]
                        [:yLt (ch/output (ch/bool))]
                        [:yLte (ch/output (ch/bool))]
                        [:yGt (ch/output (ch/bool))]
                        [:yGte (ch/output (ch/bool))]
                        [:yAnd (ch/output (ch/uint 8))]
                        [:yOr (ch/output (ch/uint 8))]
                        [:yXor (ch/output (ch/uint 8))]
                        [:yNot (ch/output (ch/uint 8))]
                        [:yShl (ch/output (ch/uint 11))]
                        [:yShr (ch/output (ch/uint 8))]
                        [:yCat (ch/output (ch/uint 16))]
                        [:yBits (ch/output (ch/uint 4))]
                        [:yMux (ch/output (ch/uint 8))]
                        [:yIndex (ch/output (ch/uint 8))]
                        [:yBitIndex (ch/output (ch/bool))]
                        [:yVec (ch/output (ch/uint 16))]
                        [:yVecIndex (ch/output (ch/uint 8))]
                        [:yWire (ch/output (ch/uint 8))]
                        [:yBulk (ch/output (ch/uint 8))]
                        [:yReg (ch/output (ch/uint 8))]
                        [:yRegInit (ch/output (ch/uint 8))]
                        [:yRegNext (ch/output (ch/uint 8))]
                        [:yRegNextInit (ch/output (ch/uint 8))]
                        [:yWhen (ch/output (ch/uint 8))]
                        [:yWhenElse (ch/output (ch/uint 8))]]))
            a (ch/field io :a) b (ch/field io :b)
            sel (ch/field io :sel) vec-idx (ch/field io :vecIdx)
            bit-idx (ch/field io :bitIdx)
            w (ch/wire (ch/uint 8)) wb (ch/wire (ch/uint 8))
            r (ch/reg (ch/uint 8)) ri (ch/reg-init (ch/u 3 8))
            rn (ch/reg-next a) rni (ch/reg-next b (ch/u 4 8))
            vi (ch/vec-init [a b])]
        (ch/connect! w a)
        (ch/bulk-connect! wb b)
        (ch/connect! r a)
        (doseq [[k v] [[:yAdd (ch/add a b)] [:ySub (ch/sub a b)]
                       [:yMul (ch/mul a b)] [:yEq (ch/eq a b)]
                       [:yNeq (ch/neq a b)] [:yLt (ch/lt a b)]
                       [:yLte (ch/lte a b)] [:yGt (ch/gt a b)]
                       [:yGte (ch/gte a b)] [:yAnd (ch/and a b)]
                       [:yOr (ch/or a b)] [:yXor (ch/xor a b)]
                       [:yNot (ch/not a)] [:yShl (ch/shl a vec-idx)]
                       [:yShr (ch/shr a vec-idx)] [:yCat (ch/cat a b)]
                       [:yBits (ch/bits-at a 5 2)] [:yMux (ch/mux sel a b)]
                       [:yIndex (ch/index (ch/field io :values) vec-idx)]
                       [:yBitIndex (ch/index a bit-idx)]
                       [:yVec (ch/vec-as-uint [a b])]
                       [:yVecIndex (ch/index vi (ch/u 1 1))]
                       [:yWire w] [:yBulk wb] [:yReg r] [:yRegInit ri]
                       [:yRegNext rn] [:yRegNextInit rni]]]
          (ch/connect! (ch/field io k) v))
        (ch/connect! (ch/field io :yWhen) b)
        (ch/when sel (ch/connect! (ch/field io :yWhen) a))
        (ch/when-else sel
          (ch/connect! (ch/field io :yWhenElse) a)
          (ch/connect! (ch/field io :yWhenElse) b))))))

(defn- parent-module []
  (ch/module {:name "PublicParent"}
    (fn []
      (let [io (ch/io (ch/bundle [[:in (ch/input (ch/uint 8))]
                                  [:out (ch/output (ch/uint 8))]]))
            child (ch/module-instance (child-module))
            cio (. child io)]
        (ch/connect! (ch/field cio :in) (ch/field io :in))
        (ch/connect! (ch/field io :out) (ch/field cio :out))))))

(def ^:private fixture-fir (delay (ch/emit-firrtl (public-module))))
(def ^:private parent-fir (delay (ch/emit-firrtl (parent-module))))
(defn- has? [s] (str/includes? @fixture-fir s))

^{:refer jvm.chisel/uint :added "4.1"}
(fact "uint builds sized and unsized UInt types"
  (instance? chisel3.UInt (ch/uint 8)) => true
  (instance? chisel3.UInt (ch/uint)) => true)

^{:refer jvm.chisel/sint :added "4.1"}
(fact "sint builds a SInt type"
  (instance? chisel3.SInt (ch/sint 16)) => true)

^{:refer jvm.chisel/bool :added "4.1"}
(fact "bool builds a Bool type"
  (instance? chisel3.Bool (ch/bool)) => true)

^{:refer jvm.chisel/bits :added "4.1"}
(fact "bits builds a Bits type"
  (instance? chisel3.Bits (ch/bits 32)) => true)

^{:refer jvm.chisel/vec :added "4.1"}
(fact "vec builds a sized Vec type"
  (instance? chisel3.Vec (ch/vec 4 (ch/uint 8))) => true)

^{:refer jvm.chisel/bundle :added "4.1"}
(fact "bundle builds a Record"
  (instance? chisel3.Record (ch/bundle [[:a (ch/uint 8)]])) => true)

^{:refer jvm.chisel/u :added "4.1"}
(fact "u builds an unsigned literal"
  (instance? chisel3.UInt (ch/u 5 4)) => true)

^{:refer jvm.chisel/s :added "4.1"}
(fact "s builds a signed literal"
  (instance? chisel3.SInt (ch/s -3 8)) => true)

^{:refer jvm.chisel/b :added "4.1"}
(fact "b builds a boolean literal"
  (instance? chisel3.Bool (ch/b true))  => true
  (instance? chisel3.Bool (ch/b false)) => true)


^{:refer jvm.chisel/field :added "4.1"}
(fact "field retrieves a bundle member"
  (instance? chisel3.UInt (ch/field (ch/bundle [[:a (ch/uint 8)]]) :a)) => true)

^{:refer jvm.chisel/input :added "4.1"}
(fact "input produces flipped ports"
  (has? "flip a : UInt<8>") => true)

^{:refer jvm.chisel/output :added "4.1"}
(fact "output produces non-flipped ports"
  (has? "yAdd : UInt<8>") => true)

^{:refer jvm.chisel/io :added "4.1"}
(fact "io registers the top-level bundle"
  (has? "io : {") => true)

^{:refer jvm.chisel/wire :added "4.1"}
(fact "wire elaborates a UInt wire"
  (has? "wire _WIRE : UInt<8>") => true)

^{:refer jvm.chisel/reg :added "4.1"}
(fact "reg elaborates a clocked register"
  (has? "reg REG : UInt<8>") => true)

^{:refer jvm.chisel/reg-init :added "4.1"}
(fact "reg-init elaborates reset state"
  (has? "yRegInit") => true)

^{:refer jvm.chisel/reg-next :added "4.1"}
(fact "reg-next supports both arities"
  (has? "yRegNextInit") => true)

^{:refer jvm.chisel/add :added "4.1"}
(fact "add emits addition"
  (has? "add(") => true)

^{:refer jvm.chisel/sub :added "4.1"}
(fact "sub emits subtraction"
  (has? "sub(") => true)

^{:refer jvm.chisel/mul :added "4.1"}
(fact "mul emits multiplication"
  (has? "mul(") => true)

^{:refer jvm.chisel/eq :added "4.1"}
(fact "eq emits equality"
  (has? "eq(") => true)

^{:refer jvm.chisel/neq :added "4.1"}
(fact "neq emits inequality"
  (has? "neq(") => true)

^{:refer jvm.chisel/lt :added "4.1"}
(fact "lt emits less-than"
  (has? "lt(") => true)

^{:refer jvm.chisel/lte :added "4.1"}
(fact "lte emits less-or-equal"
  (has? "leq(") => true)

^{:refer jvm.chisel/gt :added "4.1"}
(fact "gt emits greater-than"
  (has? "gt(") => true)

^{:refer jvm.chisel/gte :added "4.1"}
(fact "gte emits greater-or-equal"
  (has? "geq(") => true)

^{:refer jvm.chisel/and :added "4.1"}
(fact "and emits conjunction"
  (has? "and(") => true)

^{:refer jvm.chisel/or :added "4.1"}
(fact "or emits disjunction"
  (has? "or(") => true)

^{:refer jvm.chisel/xor :added "4.1"}
(fact "xor emits exclusive-or"
  (has? "xor(") => true)

^{:refer jvm.chisel/not :added "4.1"}
(fact "not emits inversion"
  (has? "not(") => true)

^{:refer jvm.chisel/shl :added "4.1"}
(fact "shl emits dynamic left shift"
  (has? "dshl(") => true)

^{:refer jvm.chisel/shr :added "4.1"}
(fact "shr emits dynamic right shift"
  (has? "dshr(") => true)

^{:refer jvm.chisel/cat :added "4.1"}
(fact "cat emits concatenation"
  (has? "cat(") => true)

^{:refer jvm.chisel/bits-at :added "4.1"}
(fact "bits-at emits the requested range"
  (has? "bits(io.a, 5, 2)") => true)

^{:refer jvm.chisel/mux :added "4.1"}
(fact "mux emits conditional selection"
  (has? "mux(") => true)

^{:refer jvm.chisel/index :added "4.1"}
(fact "index supports dynamic Vec and Bits access"
  (has? "yBitIndex") => true)

^{:refer jvm.chisel/vec-as-uint :added "4.1"}
(fact "vec-as-uint packs elements"
  (has? "yVec : UInt<16>") => true)

^{:refer jvm.chisel/vec-init :added "4.1"}
(fact "vec-init supports dynamic indexing"
  (has? "yVecIndex") => true)

^{:refer jvm.chisel/connect! :added "4.1"}
(fact "connect! emits directed wiring"
  (has? "connect io.yWire") => true)

^{:refer jvm.chisel/bulk-connect! :added "4.1"}
(fact "bulk-connect! wires compatible data"
  (has? "yBulk") => true)

^{:refer jvm.chisel/when :added "4.1"}
(fact "when emits a conditional block"
  (has? "when io.sel") => true)

^{:refer jvm.chisel/when-else :added "4.1"}
(fact "when-else emits an else branch"
  (has? "else :") => true)

^{:refer jvm.chisel/module :added "4.1"}
(fact "module returns a named builder"
  (fn? (public-module)) => true
  (has? "module PublicFixture") => true)

^{:refer jvm.chisel/module-instance :added "4.1"}
(fact "module-instance elaborates a child"
  (str/includes? @parent-fir "module PublicChild") => true
  (str/includes? @parent-fir "inst PublicChild of PublicChild") => true)

^{:refer jvm.chisel/emit-firrtl :added "4.1"}
(fact "emit-firrtl returns named CHIRRTL"
  (has? "circuit PublicFixture") => true)

^{:refer jvm.chisel/emit-system-verilog :added "4.1"}
(fact "emit-system-verilog returns named SystemVerilog"
  (str/includes? (ch/emit-system-verilog (child-module)) "module PublicChild") => true)
