(ns jvm.chisel.internal
  "JVM interop boundary for `jvm.chisel`.

   This is the only namespace that talks to Chisel/Scala. It centralises the
   three awkward parts of calling Chisel from Clojure:

     * Chisel's public operators are Scala macros that expand to `do_<mangled>`
       methods taking an explicit `chisel3.experimental.SourceInfo`. We pass a
       single `UnlocatableSourceInfo` everywhere and reach the `do_*` methods
       through `clojure.lang.Reflector` (the receiver type varies per Data
       subtype, so a single reflective dispatch is the clean option).

     * Companion-object factories (`UInt`, `Wire`, `Mux`, ...) and package-object
       helpers (`fromIntToWidth`, `fromIntToLiteral`, ...) are fixed targets and
       are type-hinted.

     * Module bodies must run inside the module constructor, which Clojure
       cannot inject into. A tiny Java base (`xt.chisel.DynModule`) runs a
       callback in its constructor; `module` binds `*module*` around it so that
       `io` can register itself for port naming.

   Reflection warnings are disabled for this namespace only: this is the one
   sanctioned reflective boundary."
  (:import [chisel3 Data Bits UInt SInt Bool]
           [chisel3.experimental SourceInfo]
           [clojure.lang Reflector])
  (:refer-clojure :exclude [rem cat]))

(set! *warn-on-reflection* false)

;; ---------------------------------------------------------------------------
;; singletons / companions
;; ---------------------------------------------------------------------------

(def ^:private pkg   chisel3.package$/MODULE$)
(def ^SourceInfo si  chisel3.experimental.UnlocatableSourceInfo$/MODULE$)
(def ^:private stage circt.stage.ChiselStage$/MODULE$)

(defn- fn0 [f] (reify scala.Function0 (apply [_] (f))))
(defn- fn1 [f] (reify scala.Function1 (apply [_ x] (f x))))

(defn- ->seq
  "Clojure collection -> scala.collection.immutable.Seq (a List)."
  [coll]
  (.toList (scala.jdk.javaapi.CollectionConverters/asScala
            (java.util.ArrayList. ^java.util.Collection (vec coll)))))

(defn- ->listmap
  "Ordered map of String->Data -> scala ListMap (for Record elements)."
  [m]
  (.apply ^scala.collection.immutable.ListMap$ scala.collection.immutable.ListMap$/MODULE$
          (->seq (map (fn [[k v]] (scala.Tuple2. (name k) v)) m))))

;; ---------------------------------------------------------------------------
;; widths, types, literals
;; ---------------------------------------------------------------------------

(defn width
  "integer width -> chisel Width"
  [n]
  (.W (.fromIntToWidth pkg (int n))))

(defn uint
  ([] (.apply ^chisel3.package$UInt$ chisel3.package$UInt$/MODULE$))
  ([n] (.apply ^chisel3.package$UInt$ chisel3.package$UInt$/MODULE$ (width n))))

(defn sint
  ([] (.apply ^chisel3.package$SInt$ chisel3.package$SInt$/MODULE$))
  ([n] (.apply ^chisel3.package$SInt$ chisel3.package$SInt$/MODULE$ (width n))))

(defn bool
  []
  (.apply ^chisel3.package$Bool$ chisel3.package$Bool$/MODULE$))

(defn bits
  ([] (.apply ^chisel3.package$Bits$ chisel3.package$Bits$/MODULE$))
  ([n] (.apply ^chisel3.package$Bits$ chisel3.package$Bits$/MODULE$ (width n))))

(defn u
  "unsigned literal: (u value width) -> value.U(width.W)"
  [value n]
  (.U (.fromIntToLiteral pkg (int value)) (width n)))

(defn s
  "signed literal: (s value width) -> value.S(width.W)"
  [value n]
  (.S (.fromIntToLiteral pkg (int value)) (width n)))

(defn b
  "boolean literal: (b true|false) -> true.B / false.B"
  [value]
  (.B (.fromBooleanToLiteral pkg (boolean value))))

;; ---------------------------------------------------------------------------
;; ports
;; ---------------------------------------------------------------------------

(defn input  [t] (.apply ^chisel3.Input$  chisel3.Input$/MODULE$  (fn0 (fn [] t))))
(defn output [t] (.apply ^chisel3.Output$ chisel3.Output$/MODULE$ (fn0 (fn [] t))))

(defn suggest-name
  [^Data d n]
  (.suggestName d (fn0 (fn [] (str n)))))

;; ---------------------------------------------------------------------------
;; aggregates: vec and bundle (Record)
;; ---------------------------------------------------------------------------

(defn vec-type
  "Vec type constructor: (vec-type n t) -> Vec(n, t)"
  [n t]
  (.apply ^chisel3.package$Vec$ chisel3.package$Vec$/MODULE$ (int n) ^Data t si))

(defn- record-proxy
  [elts]
  (proxy [chisel3.Record] []
    (elements [] (->listmap elts))
    (cloneType [] (record-proxy elts))))

(defn bundle
  "Bundle from an ordered map/seq of [name Data] pairs.
   Field access uses `field`; keyword lookup is intentionally not provided
   (a Chisel Record cannot also be a Clojure ILookup)."
  [elts]
  (record-proxy (into [] elts)))

(defn field
  "access a bundle/record field by name"
  [^chisel3.Record rec k]
  (.apply ^scala.collection.immutable.ListMap (.elements rec) (name k)))

(defn index
  "index a Vec (static int or dynamic UInt) or bit-index a Bits value."
  [coll i]
  (cond
    (instance? chisel3.Vec coll)
    (if (integer? i)
      (.apply ^chisel3.Vec coll (int i))
      (.do_apply ^chisel3.Vec coll ^Data i si))

    (integer? i)
    (.do_apply ^Bits coll (int i) si)

    :else
    (.do_apply ^Bits coll ^Data i si)))

(defn vec-as-uint
  "VecInit(coll).asUInt — pack a seq of Bool/Bits into a UInt (lsb first)."
  [coll]
  (let [v (.do_apply ^chisel3.VecInit$ chisel3.VecInit$/MODULE$ (->seq coll) si)]
    (.do_asUInt ^Bits v si)))

;; ---------------------------------------------------------------------------
;; construction: wire / reg
;; ---------------------------------------------------------------------------

(defn wire      [t]   (.apply ^chisel3.Wire$    chisel3.Wire$/MODULE$    (fn0 (fn [] t)) si))
(defn reg       [t]   (.apply ^chisel3.Reg$     chisel3.Reg$/MODULE$     (fn0 (fn [] t)) si))
(defn reg-init  [t]   (.apply ^chisel3.RegInit$ chisel3.RegInit$/MODULE$ ^Data t si))
(defn reg-next
  ([t]       (.apply ^chisel3.RegNext$ chisel3.RegNext$/MODULE$ ^Data t si))
  ([t init]  (.apply ^chisel3.RegNext$ chisel3.RegNext$/MODULE$ ^Data t ^Data init si)))

;; ---------------------------------------------------------------------------
;; operators (single reflective dispatch over the varying Data subtype)
;; ---------------------------------------------------------------------------

(defn- op2
  [m a b]
  (Reflector/invokeInstanceMethod
   ^Object a ^String (name m) ^"[Ljava.lang.Object;" (object-array [b si])))

(defn- op1
  [m a]
  (Reflector/invokeInstanceMethod
   ^Object a ^String (name m) ^"[Ljava.lang.Object;" (object-array [si])))

(defn add [a b] (op2 "do_$plus" a b))
(defn sub [a b] (op2 "do_$minus" a b))
(defn mul [a b] (op2 "do_$times" a b))

(defn eq  [a b] (op2 "do_$eq$eq$eq" a b))
(defn neq [a b] (op2 "do_$eq$div$eq" a b))
(defn lt  [a b] (op2 "do_$less" a b))
(defn lte [a b] (op2 "do_$less$eq" a b))
(defn gt  [a b] (op2 "do_$greater" a b))
(defn gte [a b] (op2 "do_$greater$eq" a b))

(defn band [a b] (op2 "do_$amp" a b))
(defn bor  [a b] (op2 "do_$bar" a b))
(defn bxor [a b] (op2 "do_$up" a b))
(defn bnot [a]   (op1 "do_unary_$tilde" a))

(defn shl [a n] (op2 "do_$less$less" a n))
(defn shr [a n] (op2 "do_$greater$greater" a n))

(defn cat
  "concatenate (a ## b ## ...) lsb-first by pairwise do_##"
  ([a] a)
  ([a b] (op2 "do_$hash$hash" a b))
  ([a b & more] (reduce cat (cat a b) more)))

(defn bits-at
  "bit range extract a(hi, lo)"
  [a hi lo]
  (.do_apply ^Bits a (int hi) (int lo) si))

(defn mux
  [c t f]
  (.do_apply ^chisel3.Mux$ chisel3.Mux$/MODULE$ c ^Data t ^Data f si))

;; ---------------------------------------------------------------------------
;; wiring
;; ---------------------------------------------------------------------------

(defn connect!
  [sink source]
  (.connect ^Data sink ^Data source si))

(defn bulk-connect!
  [left right]
  (.bulkConnect ^Data left ^Data right si))

;; ---------------------------------------------------------------------------
;; control: when / when-else
;; ---------------------------------------------------------------------------

(defn when-context
  [condition body-fn]
  (.apply ^chisel3.when$ chisel3.when$/MODULE$
          (fn0 (fn [] condition))
          (fn0 body-fn)
          si))

(defn when-else
  [condition then-fn else-fn]
  (let [ctx (when-context condition then-fn)]
    (.otherwise ^chisel3.WhenContext ctx (fn0 else-fn) si)))

;; ---------------------------------------------------------------------------
;; modules + emission
;; ---------------------------------------------------------------------------

(def ^:dynamic *module*
  "the DynModule currently being constructed (bound by `module`)" nil)

(defn register-io!
  "Called by `io`: name the IO and expose it on the module for port discovery."
  [^xt.chisel.DynModule m data name]
  (suggest-name data name)
  (set! (. m io) data)
  data)

(defn io-data
  [t]
  (.apply ^chisel3.IO$ chisel3.IO$/MODULE$ (fn0 (fn [] t)) si))

(defn module
  "Return a zero-arg builder thunk for a module named `name`. `body-fn` is a
   no-arg fn that constructs io/wiring. The module is only elaborated once the
   thunk is invoked inside ChiselStage (i.e. by `emit-*` or `module-instance`),
   because a module can only be built within a Chisel Builder context."
  [name body-fn]
  (fn [] (xt.chisel.DynModule. (str name)
                               (fn1 (fn [m] (binding [*module* m] (body-fn)))))))

(defn raw-module
  [name body-fn]
  (fn [] (xt.chisel.DynRawModule. (str name)
                                  (fn1 (fn [m] (binding [*module* m] (body-fn)))))))

(defn- builder->fn0
  "Accept either a builder thunk (a 0-arg fn) or an already-built module."
  [builder]
  (if (fn? builder) (fn0 builder) (fn0 (fn [] builder))))

(defn module-instance
  "Instantiate a module, i.e. Module(new Child(...)). `build-thunk` is the
   zero-arg builder returned by `module`."
  [build-thunk]
  (.do_apply ^chisel3.Module$ chisel3.Module$/MODULE$ (builder->fn0 build-thunk) si))

(defn emit-firrtl
  [builder]
  (.emitCHIRRTL stage (builder->fn0 builder) (into-array String [])))

(defn emit-system-verilog
  ([builder]
   (emit-system-verilog builder [] []))
  ([builder chisel-args firtool-opts]
   (.emitSystemVerilog stage (builder->fn0 builder)
                       (into-array String chisel-args)
                       (into-array String firtool-opts))))
