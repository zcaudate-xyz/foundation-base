(ns jvm.chisel
  "A thin, direct mapping of Chisel primitives into Clojure.

   Every public function/macro maps straight onto the corresponding Chisel
   primitive and Chisel performs the elaboration; there is no intermediate
   hardware representation. The only non-trivial machinery (implicits, symbolic
   JVM method names, module constructor scope, emission) lives in
   `jvm.chisel.internal`.

       Clojure function  ->  corresponding Chisel primitive  ->  Chisel elaboration

   Example: see `src-lang/chisel/examples/predicate_eq.clj`."
  (:require [jvm.chisel.internal :as in])
  (:refer-clojure :exclude [and or not vec when cat]))

;; types ---------------------------------------------------------------------

(defn uint
  "UInt type. (uint) or (uint width) -> UInt / UInt(width.W)"
  ([] (in/uint)) ([w] (in/uint w)))

(defn sint
  "SInt type. (sint) or (sint width)"
  ([] (in/sint)) ([w] (in/sint w)))

(defn bool
  "Bool type."
  [] (in/bool))

(defn bits
  "Bits type. (bits) or (bits width)"
  ([] (in/bits)) ([w] (in/bits w)))

(defn vec
  "Vec type. (vec n element-type) -> Vec(n, t)"
  [n t] (in/vec-type n t))

(defn bundle
  "Bundle (Record) from an ordered map/seq of [name Data] pairs.
   Read fields with `field`."
  [elts] (in/bundle elts))

(defn field
  "access a bundle field by name, e.g. (field io :values)"
  [rec k] (in/field rec k))

;; literals ------------------------------------------------------------------

(defn u "unsigned literal (u value width)"  [value w] (in/u value w))
(defn s "signed literal   (s value width)"  [value w] (in/s value w))
(defn b "boolean literal  (b true|false)"   [value]   (in/b value))

;; ports ---------------------------------------------------------------------

(defn input  [t] (in/input t))
(defn output [t] (in/output t))

(defn io
  "Top-level module IO. When called inside `module`, registers itself as the
   module's IO (named \"io\") so Chisel can discover and name the ports."
  [t]
  (let [d (in/io-data t)]
    (clojure.core/when in/*module*
      (in/register-io! in/*module* d "io"))
    d))

;; construction --------------------------------------------------------------

(defn wire      [t]      (in/wire t))
(defn reg       [t]      (in/reg t))
(defn reg-init  [t]      (in/reg-init t))
(defn reg-next
  "RegNext. (reg-next t) or (reg-next t reset-value)"
  ([t] (in/reg-next t))
  ([t reset] (in/reg-next t reset)))

;; operators -----------------------------------------------------------------

(defn add [a b] (in/add a b))
(defn sub [a b] (in/sub a b))
(defn mul [a b] (in/mul a b))

(defn eq  [a b] (in/eq a b))
(defn neq [a b] (in/neq a b))
(defn lt  [a b] (in/lt a b))
(defn lte [a b] (in/lte a b))
(defn gt  [a b] (in/gt a b))
(defn gte [a b] (in/gte a b))

(defn and "bitwise/logical AND" [a b] (in/band a b))
(defn or  "bitwise/logical OR"  [a b] (in/bor a b))
(defn xor "bitwise XOR"         [a b] (in/bxor a b))
(defn not "bitwise/logical NOT" [a]   (in/bnot a))

(defn shl [a n] (in/shl a n))
(defn shr [a n] (in/shr a n))
(defn cat
  "concatenate (a ## b ## ...)"
  ([a] a)
  ([a b] (in/cat a b))
  ([a b & more] (apply in/cat a b more)))

(defn bits-at
  "bit range extract a(hi, lo)"
  [a hi lo] (in/bits-at a hi lo))

(defn mux
  "(mux condition true-value false-value)"
  [c t f] (in/mux c t f))

(defn index
  "index a Vec or bit-index a Bits value (static int or dynamic UInt)"
  [coll i] (in/index coll i))

(defn vec-as-uint
  "pack a seq of Bool/Bits into a UInt (VecInit(...).asUInt), lsb first"
  [coll] (in/vec-as-uint coll))

;; wiring --------------------------------------------------------------------

(defn connect!
  "(connect! sink source) -> sink := source"
  [sink source] (in/connect! sink source))

(defn bulk-connect!
  "(bulk-connect! left right) -> left <> right"
  [left right] (in/bulk-connect! left right))

;; control -------------------------------------------------------------------

(defmacro when
  "Chisel `when` conditional construction. Body runs in the `when` context."
  [condition & body]
  `(in/when-context ~condition (fn [] ~@body)))

(defmacro when-else
  "Chisel `when`/`otherwise`. Single-form then and else bodies."
  [condition then else]
  `(in/when-else ~condition (fn [] ~then) (fn [] ~else)))

;; modules -------------------------------------------------------------------

(defn module
  "Define a Chisel module. `opts` carries :name (and :raw? for an explicit
   clock/reset RawModule). `body-fn` is a no-arg fn that builds io/wiring.
   Returns the module instance (pass to `emit-system-verilog`)."
  [opts body-fn]
  (if (:raw? opts)
    (in/raw-module (:name opts) body-fn)
    (in/module     (:name opts) body-fn)))

(defn module-instance
  "Instantiate a module, i.e. Module(new Child(...)). `build-thunk` returns the
   module (e.g. (fn [] (module ...)))."
  [build-thunk]
  (in/module-instance build-thunk))

;; output --------------------------------------------------------------------

(defn emit-firrtl
  "Elaborate and emit FIRRTL (no external toolchain required)."
  [mod] (in/emit-firrtl mod))

(defn emit-system-verilog
  "Elaborate and emit SystemVerilog. Requires a version-matched `firtool`
   (CIRCT); Chisel bundles a resolver that fetches one if absent."
  ([mod] (in/emit-system-verilog mod))
  ([mod chisel-args firtool-opts] (in/emit-system-verilog mod chisel-args firtool-opts)))
