(ns jvm.chisel.testing
  "Co-simulation harness for `jvm.chisel` modules using Chisel's native
   `chisel3.simulator` (peek/poke/expect/step) with the bundled svsim/Verilator
   backend.

   Requires an external simulator on PATH (Verilator, or VCS). Facts that call
   `simulate` should be gated with `(fact:global {:skip (not (env/program-exists?
   \"verilator\"))})` so they are reported skipped (not failed) when no backend
   is present.

   A module is simulated by passing its *builder thunk* (the value returned by
   `jvm.chisel/module`, or any of the `*-module` generators) and a body that
   drives it. Ports are addressed by their `io` field name (a string). For
   sequential modules the body also gets `step`/`reset!` (synchronous reset).

   Drive an entire oracle table inside one body: each `simulate` recompiles the
   design with Verilator (~seconds), so batch many stimuli per call."
  (:require [jvm.chisel :as ch]))

(defonce ^:private SIM (delay (eval 'chisel3.simulator.EphemeralSimulator$/MODULE$)))
(defonce ^:private SBI (delay (eval 'scala.math.BigInt$/MODULE$)))

(defn- ->sbi [x]
  (.apply ^scala.math.BigInt$ @SBI (biginteger x)))

(defn- fn0 [thunk]
  (proxy [scala.runtime.AbstractFunction0] [] (apply [] (thunk))))

(defn- fn1 [g]
  (proxy [scala.runtime.AbstractFunction1] [] (apply [x] (g x))))

(defn- elements [raw]
  ;; Chisel `Record.elements` is a Scala ListMap[String, Data]; route it through
  ;; a Java Map so Clojure can seq it.
  (let [m  (.elements (.io ^chisel3.Module raw))
        jm (.asJava (scala.collection.JavaConverters/mapAsJavaMapConverter m))]
    (into {} jm)))

(defn port-data
  "The Chisel `Data` for `io.<name>` of an elaborated `raw` module."
  [raw name]
  ((elements raw) name))

(defn vec-el
  "The `i`-th lane `Data` of a Chisel `Vec` port (static integer index)."
  [vec-port i]
  (.apply ^chisel3.Vec vec-port (int i)))

(defn poke-vec!
  "Poke each lane of the Vec port named `pname` with the corresponding value in
   `vals` (lane 0 = first element). `ctx` is the map passed to a `simulate` body."
  [ctx pname vals]
  (let [{:keys [port poke]} ctx
        v (port pname)]
    (doseq [[i x] (map-indexed vector vals)]
      (poke (vec-el v i) (long x)))))

(defn expect-vec!
  "Expect each lane of the Vec *output* port named `pname` to equal the
   corresponding value in `vals` (lane 0 = first element)."
  [ctx pname vals]
  (let [{:keys [port expect]} ctx
        v (port pname)]
    (doseq [[i x] (map-indexed vector vals)]
      (expect (vec-el v i) (long x)))))

(defn simulate
  "Elaborate and simulate `builder` (a `jvm.chisel/module` thunk), running
   `(f ctx)` inside the simulation context. `ctx` is a map:

     :port   (fn [name]      -> Data)        io field by string name
     :poke   (fn [port v]    -> nil)         v: long/boolean
     :expect (fn [port v]    -> nil)         throws FailedExpectation on mismatch
     :peek   (fn [port]      -> long)        current value as a long
     :step   (fn []          -> nil)         step the implicit clock once
     :reset! (fn []          -> nil)         assert reset for one clock, then release

   Returns whatever `f` returns. Recompiles the design with Verilator on every
   call, so batch stimuli inside one body."
  [builder f]
  (let [sim @SIM
        raw (atom nil)
        thunk (fn0 (fn [] (let [m (builder)] (reset! raw m) m)))
        pdata (fn [name] (port-data @raw name))
        poke  (fn [port v]
                (if (boolean? v)
                  (.poke (.testableData sim port) ^boolean v)
                  (.poke (.testableData sim port) (->sbi v))))
        expect (fn [port v]
                 (.expect (.testableUInt sim port) (->sbi v)))
        peekv (fn [port]
                (.toLong ^scala.math.BigInt (.litValue ^chisel3.Data (.peek (.testableUInt sim port)))))
        step  (fn [] (.step (.testableClock sim (.clock ^chisel3.Module @raw)) 1))
        reset! (fn [] (let [r (.reset ^chisel3.Module @raw)]
                        (poke r true) (step) (poke r false)))
        ctx {:port pdata :poke poke :expect expect :peek peekv :step step :reset! reset!}]
    (.simulate sim thunk (fn1 (fn [_] (f ctx))))))

;; bit-vector helpers --------------------------------------------------------

(defn pack
  "Pack a seq of 0/1 bits into an integer, element 0 at the least-significant
   bit (matches the lsb-first convention of `jvm.chisel.variant`, whose
   kernels now live in foundation-embed)."
  [bits]
  (reduce (fn [m i] (bit-or m (bit-shift-left (long (nth bits i)) i)))
          0 (range (count bits))))

(defn unpack
  "The inverse of `pack`: the low `n` bits of integer `x` as a vector of 0/1,
   element 0 = least-significant bit."
  [x n]
  (mapv (fn [i] (if (pos? (bit-and x (bit-shift-left 1 i))) 1 0)) (range n)))

(defn popcount-int
  "Popcount of an integer (Clojure), for oracles/invariants."
  [x]
  (Long/bitCount (long x)))
