(ns jvm.chisel.internal-test
  (:use code.test)
  (:require [clojure.string :as str]
            [jvm.chisel.internal :as in]))

(defn- internal-child []
  (in/module "InternalChild"
    (fn []
      (let [io (in/io-data (in/bundle [[:in (in/input (in/uint 8))]
                                       [:out (in/output (in/uint 8))]]))]
        (in/register-io! in/*module* io "io")
        (in/connect! (in/field io :out) (in/field io :in))))))

(defn- internal-module []
  (in/module "InternalFixture"
    (fn []
      (let [io (in/io-data (in/bundle
                            [[:a (in/input (in/uint 8))]
                             [:b (in/input (in/uint 8))]
                             [:sel (in/input (in/bool))]
                             [:vecIdx (in/input (in/uint 2))]
                             [:bitIdx (in/input (in/uint 3))]
                             [:values (in/input (in/vec-type 4 (in/uint 8)))]
                             [:y (in/output (in/uint 8))]
                             [:wide (in/output (in/uint 16))]
                             [:flag (in/output (in/bool))]]))
            _ (in/register-io! in/*module* io "io")
            a (in/field io :a) b (in/field io :b)
            sel (in/field io :sel) vec-idx (in/field io :vecIdx)
            bit-idx (in/field io :bitIdx)
            w (in/wire (in/uint 8)) wb (in/wire (in/uint 8))
            r (in/reg (in/uint 8)) ri (in/reg-init (in/u 1 8))
            rn (in/reg-next a) rni (in/reg-next b (in/u 2 8))
            vi (in/vec-init [a b])]
        (in/suggest-name w "namedWire")
        (in/connect! w (in/add a b))
        (in/bulk-connect! wb b)
        (in/connect! r (in/sub a b))
        (in/connect! (in/field io :wide) (in/cat a b))
        (in/connect! (in/field io :flag)
                     (in/bor (in/eq a b)
                             (in/band (in/lt a b) (in/bnot (in/neq a b)))))
        (doseq [x [(in/mul a b) (in/lte a b) (in/gt a b) (in/gte a b)
                   (in/bxor a b) (in/shl a vec-idx) (in/shr a vec-idx)
                   (in/bits-at a 5 2) (in/mux sel a b)
                   (in/index (in/field io :values) vec-idx) (in/index a bit-idx)
                   (in/vec-as-uint [a b]) (in/index vi (in/u 1 1))
                   ri rn rni wb]] x)
        (in/connect! (in/field io :y) w)
        (in/when-context sel #(in/connect! (in/field io :y) a))
        (in/when-else sel
                      #(in/connect! (in/field io :y) a)
                      #(in/connect! (in/field io :y) b))))))

(defn- internal-parent []
  (in/module "InternalParent"
    (fn []
      (let [io (in/io-data (in/bundle [[:in (in/input (in/uint 8))]
                                       [:out (in/output (in/uint 8))]]))
            _ (in/register-io! in/*module* io "io")
            child (in/module-instance (internal-child))
            cio (. child io)]
        (in/connect! (in/field cio :in) (in/field io :in))
        (in/connect! (in/field io :out) (in/field cio :out))))))

(def ^:private fixture-fir (delay (in/emit-firrtl (internal-module))))
(def ^:private parent-fir (delay (in/emit-firrtl (internal-parent))))
(defn- has? [s] (str/includes? @fixture-fir s))

^{:refer jvm.chisel.internal/width :added "4.1"}
(fact "width converts an integer to a Chisel Width"
  (instance? chisel3.Width (in/width 8)) => true)

^{:refer jvm.chisel.internal/uint :added "4.1"}
(fact "uint builds sized and unsized UInt types"
  (instance? chisel3.UInt (in/uint 8)) => true
  (instance? chisel3.UInt (in/uint)) => true)

^{:refer jvm.chisel.internal/sint :added "4.1"}
(fact "sint builds a SInt type"
  (instance? chisel3.SInt (in/sint 8)) => true)

^{:refer jvm.chisel.internal/bool :added "4.1"}
(fact "bool builds a Bool type"
  (instance? chisel3.Bool (in/bool)) => true)

^{:refer jvm.chisel.internal/bits :added "4.1"}
(fact "bits builds a Bits type"
  (instance? chisel3.Bits (in/bits 8)) => true)

^{:refer jvm.chisel.internal/u :added "4.1"}
(fact "u builds an unsigned literal"
  (instance? chisel3.UInt (in/u 3 8)) => true)

^{:refer jvm.chisel.internal/s :added "4.1"}
(fact "s builds a signed literal"
  (instance? chisel3.SInt (in/s -3 8)) => true)

^{:refer jvm.chisel.internal/b :added "4.1"}
(fact "b builds a boolean literal"
  (instance? chisel3.Bool (in/b true)) => true)

^{:refer jvm.chisel.internal/input :added "4.1"}
(fact "input wraps a Data direction"
  (instance? chisel3.UInt (in/input (in/uint 8))) => true)

^{:refer jvm.chisel.internal/output :added "4.1"}
(fact "output wraps a Data direction"
  (instance? chisel3.UInt (in/output (in/uint 8))) => true)

^{:refer jvm.chisel.internal/vec-type :added "4.1"}
(fact "vec-type builds a Vec"
  (instance? chisel3.Vec (in/vec-type 4 (in/uint 8))) => true)

^{:refer jvm.chisel.internal/bundle :added "4.1"}
(fact "bundle builds a Record"
  (instance? chisel3.Record (in/bundle [[:a (in/uint 8)]])) => true)

^{:refer jvm.chisel.internal/field :added "4.1"}
(fact "field retrieves a Record member"
  (instance? chisel3.UInt (in/field (in/bundle [[:a (in/uint 8)]]) :a)) => true)

^{:refer jvm.chisel.internal/suggest-name :added "4.1"}
(fact "suggest-name controls emitted identifiers"
  (has? "namedWire") => true)

^{:refer jvm.chisel.internal/index :added "4.1"}
(fact "index supports Vec and Bits selection"
  (has? "dshr(") => true)

^{:refer jvm.chisel.internal/vec-as-uint :added "4.1"}
(fact "vec-as-uint packs values"
  (has? "cat(") => true)

^{:refer jvm.chisel.internal/vec-init :added "4.1"}
(fact "vec-init builds indexable aggregate"
  (has? "_WIRE") => true)

^{:refer jvm.chisel.internal/wire :added "4.1"}
(fact "wire elaborates Data"
  (has? "wire namedWire") => true)

^{:refer jvm.chisel.internal/reg :added "4.1"}
(fact "reg elaborates clocked state"
  (has? "reg REG") => true)

^{:refer jvm.chisel.internal/reg-init :added "4.1"}
(fact "reg-init elaborates reset state"
  (has? "regreset") => true)

^{:refer jvm.chisel.internal/reg-next :added "4.1"}
(fact "reg-next supports both forms"
  (has? "REG_3") => true)

^{:refer jvm.chisel.internal/add :added "4.1"}
(fact "add dispatches to Chisel addition"
  (has? "add(") => true)

^{:refer jvm.chisel.internal/sub :added "4.1"}
(fact "sub dispatches to Chisel subtraction"
  (has? "sub(") => true)

^{:refer jvm.chisel.internal/mul :added "4.1"}
(fact "mul dispatches to Chisel multiplication"
  (has? "mul(") => true)

^{:refer jvm.chisel.internal/eq :added "4.1"}
(fact "eq dispatches to equality"
  (has? "eq(") => true)

^{:refer jvm.chisel.internal/neq :added "4.1"}
(fact "neq dispatches to inequality"
  (has? "neq(") => true)

^{:refer jvm.chisel.internal/lt :added "4.1"}
(fact "lt dispatches to less-than"
  (has? "lt(") => true)

^{:refer jvm.chisel.internal/lte :added "4.1"}
(fact "lte dispatches to less-or-equal"
  (has? "leq(") => true)

^{:refer jvm.chisel.internal/gt :added "4.1"}
(fact "gt dispatches to greater-than"
  (has? "gt(") => true)

^{:refer jvm.chisel.internal/gte :added "4.1"}
(fact "gte dispatches to greater-or-equal"
  (has? "geq(") => true)

^{:refer jvm.chisel.internal/band :added "4.1"}
(fact "band dispatches to bitwise and"
  (has? "and(") => true)

^{:refer jvm.chisel.internal/bor :added "4.1"}
(fact "bor dispatches to bitwise or"
  (has? "or(") => true)

^{:refer jvm.chisel.internal/bxor :added "4.1"}
(fact "bxor dispatches to bitwise xor"
  (has? "xor(") => true)

^{:refer jvm.chisel.internal/bnot :added "4.1"}
(fact "bnot dispatches to inversion"
  (has? "not(") => true)

^{:refer jvm.chisel.internal/shl :added "4.1"}
(fact "shl dispatches to dynamic left shift"
  (has? "dshl(") => true)

^{:refer jvm.chisel.internal/shr :added "4.1"}
(fact "shr dispatches to dynamic right shift"
  (has? "dshr(") => true)

^{:refer jvm.chisel.internal/cat :added "4.1"}
(fact "cat dispatches to concatenation"
  (has? "cat(") => true)

^{:refer jvm.chisel.internal/bits-at :added "4.1"}
(fact "bits-at extracts an inclusive range"
  (has? "bits(io.a, 5, 2)") => true)

^{:refer jvm.chisel.internal/mux :added "4.1"}
(fact "mux dispatches conditional selection"
  (has? "mux(") => true)

^{:refer jvm.chisel.internal/connect! :added "4.1"}
(fact "connect! emits directed wiring"
  (has? "connect io.y") => true)

^{:refer jvm.chisel.internal/bulk-connect! :added "4.1"}
(fact "bulk-connect! connects compatible Data"
  (has? "_WIRE_1") => true)

^{:refer jvm.chisel.internal/when-context :added "4.1"}
(fact "when-context emits conditional wiring"
  (has? "when io.sel") => true)

^{:refer jvm.chisel.internal/when-else :added "4.1"}
(fact "when-else emits an otherwise branch"
  (has? "else :") => true)

^{:refer jvm.chisel.internal/register-io! :added "4.1"}
(fact "register-io! exposes the module IO"
  (has? "output io : {") => true)

^{:refer jvm.chisel.internal/io-data :added "4.1"}
(fact "io-data materializes the IO Record"
  (has? "flip a : UInt<8>") => true)

^{:refer jvm.chisel.internal/module :added "4.1"}
(fact "module returns a named builder"
  (fn? (internal-module)) => true
  (has? "module InternalFixture") => true)

^{:refer jvm.chisel.internal/raw-module :added "4.1"}
(fact "raw-module emits a named clockless module"
  (str/includes? (in/emit-firrtl (in/raw-module "InternalRaw" (fn [] nil)))
                 "module InternalRaw") => true)

^{:refer jvm.chisel.internal/module-instance :added "4.1"}
(fact "module-instance elaborates a child builder"
  (str/includes? @parent-fir "inst InternalChild of InternalChild") => true)

^{:refer jvm.chisel.internal/emit-firrtl :added "4.1"}
(fact "emit-firrtl returns named CHIRRTL"
  (has? "circuit InternalFixture") => true)

^{:refer jvm.chisel.internal/emit-system-verilog :added "4.1"}
(fact "emit-system-verilog returns named SystemVerilog"
  (str/includes? (in/emit-system-verilog (internal-child)) "module InternalChild") => true)
