(ns std.lib.stream
  (:require [std.lib.generate :as gen]
            [std.lib.stream.iter :as i]
            [std.protocol.stream :as protocol.stream])
  (:import (clojure.lang IPersistentCollection
                         IPersistentList
                         ITransientCollection)
           (java.util.function Supplier)
           (java.util Collection
                      Spliterator)
           (java.util.stream Stream
                             StreamSupport
                             Collector
                             Collectors)
           (java.util.concurrent Callable
                                 Future)
           (hara.lib.stream ISeqSpliterator)))

(defn produce
  "produces a seq from an object
 
   (produce (range 5))
   => '(0 1 2 3 4)"
  {:added "3.0"}
  ([obj]
   (protocol.stream/-produce obj)))

(defn collect
  "collection function given a seq supply
 
   (collect [] (range 5))
   => [0 1 2 3 4]"
  {:added "3.0"}
  ([sink supply]
   (collect sink identity supply))
  ([sink xf supply]
   (protocol.stream/-collect sink xf supply)))

(defn seqiter
  "creates an non-chunking iterator from a transducer (sink)
 
   (seqiter (map inc)
            (range 5))
   => (range 1 6)"
  {:added "3.0"}
  ([coll] coll)
  ([xform coll]
   (xform coll)))

(defn unit
  "applies a transducer to single input
 
   (unit (map inc) 1)
   => 2"
  {:added "3.0"}
  ([input] input)
  ([xform input]
   (first (seqiter xform [input]))))

(defn extend-stream-form
  "extend protocols for a type
 
   (extend-stream-form [nil '{:produce 'produce-nil
                              :collect 'collect-nil}])"
  {:added "3.0"}
  ([[type {:keys [produce collect]}]]
   (let [type (if (string? type)
                `(Class/forName ~type)
                type)]
     `(do (extend-type ~type
            ~@(if produce
                `[std.protocol.stream/ISource
                  (-produce [~'source] (~produce ~'source))])
            ~@(if collect
                `[std.protocol.stream/ISink
                  (-collect [~'sink ~'xf ~'supply] (~collect ~'sink ~'xf ~'supply))]))
          ~type))))

(defmacro extend-stream
  "extends the stream protocol for a map of types"
  {:added "3.0"}
  ([all]
   (mapv extend-stream-form (cond (symbol? all)
                                  @(resolve all)

                                  :else all))))

;;
;; Implementation
;;

(defonce ^:dynamic *transforms*
  (atom {:map    i/i:map
         :map-indexed i/i:map-indexed
         :keep   i/i:keep
         :keep-indexed i/i:keep-indexed
         :filter  i/i:filter
         :remove   i/i:remove
         :take     i/i:take
         :take-nth i/i:take-nth
         :drop     i/i:drop
         :drop-last i/i:drop-last
         :butlast i/i:butlast
         :peek   i/i:peek
         :prn    i/i:prn
         :mapcat  i/i:mapcat
         :delay   i/i:delay
         :dedupe   i/i:dedupe
         :partition-all i/i:partition-all
         :partition-by i/i:partition-by
         :random-sample i/i:random-sample
         :sort     i/i:sort
         :sort-by  i/i:sort-by
         :reductions i/i:reductions
         :some   i/i:some
         :count  i/i:count
         :reduce i/i:reduce
         :max      i/i:max
         :min      i/i:min
         :mean     i/i:mean
         :stdev    i/i:stdev
         :last     i/i:last
         :str      i/i:str}))

(defn add-transforms
  "adds a transform to the list
 
   (add-transforms :map x/x:map)
   => '(:map)"
  {:added "3.0"}
  ([key xform & more]
   (let [args (apply vector key xform more)]
     (apply swap! *transforms* assoc args)
     (take-nth 2 args))))

(defn pipeline-transform
  "transforms a pipeline to transducer form
 
   (pipeline-transform [[:map inc]
                        [:map inc]
                        [:window 5]])
   => (contains [fn? fn? fn?])"
  {:added "3.0"}
  ([stages]
   (map (fn [xf]
          (cond (vector? xf)
                (let [[k & args] xf
                      f (or (get @*transforms* k)
                            (throw (ex-info "Invalid key" {:key k})))]
                  (apply f args))

                :else xf))
        stages)))

(defn pipeline
  "creates a transducer pipeline
 
   (pipeline [[:map inc]
              [:map inc]
              [:window 5]])
   => fn?"
  {:added "3.0"}
  ([stages]
   (apply comp (reverse (pipeline-transform stages)))))

(defn pipe
  "pipes data from one source into another
 
   (pipe (range 5)
         [[:map inc]
          [:map inc]]
         ())
   => '(6 5 4 3 2)"
  {:added "3.0"}
  ([source stages sink]
   (let [xforms (pipeline stages)
         supply (produce source)]
     (collect sink xforms supply))))

(defn producer
  "creates a source seq
 
   (producer (range 5)
             [[:map inc]])
   => seq?"
  {:added "3.0"}
  ([source stages]
   (let [xforms (pipeline stages)]
     (seqiter xforms (produce source)))))

(defn collector
  "creates a collection function
 
   (collector [[:map inc]]
              [])
   => fn?"
  {:added "3.0"}
  ([stages sink]
   (let [xforms (pipeline stages)]
     (fn
       ([source]
        (collect sink xforms (produce source)))
       ([stages source]
        (collect sink
                 (comp (pipeline stages)
                       xforms)
                 (produce source)))))))

(defmacro <*>
  "denoting a missing connector
 
   (<*>) => :stream/<*>"
  {:added "3.0"}
  ([] :stream/<*>))

(defn stream
  "constructs a stream operation"
  {:added "3.0"}
  ([source] source)
  ([source & args]
   (let [[stages sink] [(butlast args) (last args)]
         source (if (= :<*> source) :stream/<*> source)
         sink   (if (= :<*> sink) :stream/<*> sink)]
     (cond (and (= :stream/<*> source)
                (= :stream/<*> sink))
           (pipeline stages)

           (= :stream/<*> sink)
           (producer source stages)

           (= :stream/<*> source)
           (collector stages sink)

           :else
           (pipe source stages sink)))))

(defmacro *>
  "shortcut for `stream`
 
   (*> (range 5)
       (x/x:map inc)
       [])
   => [1 2 3 4 5]"
  {:added "3.0"}
  ([& args]
   `(stream ~@args)))

;;
;; Java and Clojure Functions
;;

(defn produce-nil
  "produce for nil
 
   (produce-nil nil) => ()"
  {:added "3.0"}
  ([_]
   (list)))

(defn produce-object
  "default implementation of produce
 
   (produce-object 1) => '(1)"
  {:added "3.0"}
  ([obj]
   (list obj)))

(defn produce-ifn
  "produce for functions
 
   (produce-ifn (fn [] 1)) => '(1)
 
   (produce-ifn (fn [] (range 5)))
   => '(0 1 2 3 4)"
  {:added "3.0"}
  ([^clojure.lang.IFn f]
   (produce (.invoke f))))

(defn collect-nil
  "collect for nil
 
   (collect-nil nil (map inc)
                (range 5))
   => seq?"
  {:added "3.0"}
  ([_ xf supply]
   (seqiter xf supply)))

(defn collect-ifn
  "collect outputs to a source
 
   (-> (collect-ifn int-array (map inc)
                    (range 5))
       seq)
   => '(1 2 3 4 5)"
  {:added "3.0"}
  ([^clojure.lang.Fn f xf supply]
   (let [s (seqiter xf supply)]
     (f s))))

(defn produce-callable
  "produce for callable
 
   (produce-callable (fn [] 1))
   => '(1)"
  {:added "3.0"}
  ([^Callable f]
   (produce (.call f))))

(defn produce-supplier
  "produce for supplier
 
   (produce-supplier (fn/fn:supplier [] 1))
   => 1"
  {:added "3.0"}
  ([^Supplier f]
   (.get f)))

(def +stream:fn+
  '{nil
    {:produce produce-nil :collect collect-nil}
    Object
    {:produce produce-object}
    clojure.lang.Fn
    {:produce produce-ifn :collect collect-ifn}
    Callable
    {:produce produce-callable}
    Supplier
    {:produce produce-supplier}})

(extend-stream +stream:fn+)

;;
;; Primitives
;;

(def +stream:primitive+
  '{"[Z"
    {:produce seq :collect collect-booleans}
    "[B"
    {:produce seq :collect collect-bytes}
    "[C"
    {:produce seq :collect collect-chars}
    "[S"
    {:produce seq :collect collect-shorts}
    "[I"
    {:produce seq :collect collect-ints}
    "[J"
    {:produce seq :collect collect-longs}
    "[F"
    {:produce seq :collect collect-floats}
    "[D"
    {:produce seq :collect collect-doubles}})

(defn primitive-form
  "creates the primitive forms
 
   (primitive-form '[\"[Z\"
                     {:produce seq :collect collect-booleans}])"
  {:added "3.0"}
  ([[class {:keys [collect]}]]
   (let [fn-sym   collect
         sym-name (name collect)
         sym-name (subs sym-name (count "collect-") (dec (count sym-name)))
         aset-sym (symbol (str "aset-" sym-name))]
     `(defn ~fn-sym
        ([~'sink ~'xf ~'supply]
         (let [len#  (count ~'sink)
               xs#  (sequence ~'xf ~'supply)]
           (loop [i#   0
                  x#  (first xs#)
                  xs# (rest  xs#)]
             (if-not x#
               ~'sink
               (do (~aset-sym ~'sink i# x#)
                   (recur (inc i#) (first xs#) (rest xs#)))))))))))

(defmacro gen:primitives
  "generate primitive forms
 
   (produce (byte-array 5))
   => '(0 0 0 0 0)"
  {:added "3.0"}
  ([]
   (mapv primitive-form +stream:primitive+)))

(gen:primitives)
(extend-stream +stream:primitive+)

;;
;; Java and Clojure Collections
;;

(defn collect-transient
  "collect for transients
 
   (-> (collect-transient (transient [])
                          (map inc)
                          (range 5))
       (persistent!))
   => [1 2 3 4 5]"
  {:added "3.0"}
  ([^ITransientCollection sink xf supply]
   (reduce conj! sink (xf supply))))

(defn collect-collection
  "collect for java collections
 
   (-> (collect-collection (java.util.ArrayList.)
                           (map inc)
                           (range 5)))
   => [1 2 3 4 5]"
  {:added "3.0"}
  ([^Collection sink xf supply]
   (reduce (fn [^Collection c e]
             (doto c (.add e)))
           sink
           (xf supply))))

(def +stream:java+
  '{java.lang.Iterable
    {:produce iterator-seq}
    java.util.Collection
    {:produce seq}
    java.util.List
    {:produce seq}
    java.util.ArrayList
    {:produce seq :collect collect-collection}
    java.util.LinkedList
    {:produce seq :collect collect-collection}})

(extend-stream +stream:java+)

(defn collect-persistent
  [sink xf supply]
  (into sink (xf supply)))

(def +stream:clojure+
  (merge '{clojure.lang.ITransientCollection
           {:produce persistent! :collect collect-transient}
           clojure.lang.ISeq
           {:produce identity}}
         (zipmap '[clojure.lang.APersistentMap
                   clojure.lang.APersistentSet
                   clojure.lang.APersistentVector
                   clojure.lang.ASeq
                   clojure.lang.PersistentList
                   clojure.lang.PersistentList$EmptyList]
                 (repeat  {:produce identity :collect collect-persistent}))))

(extend-stream +stream:clojure+)

;;
;; Streams
;;

(defn to-stream
  "converts a seq to a stream"
  {:added "3.0"}
  [arr]
  (ISeqSpliterator/stream (seq arr)))

(defn produce-stream
  "produces values from a stream
 
   (->> (to-stream (range 5))
        (produce-stream)
        (map inc))
   => '(1 2 3 4 5)"
  {:added "3.0"}
  ([^Stream source]
   (-> source .iterator iterator-seq)))

(defn collect-collector
  "collects values from seq given a collector
 
   (collect-collector (Collectors/toList)
                      (map inc)
                      (range 5))
   => [1 2 3 4 5]"
  {:added "3.0"}
  ([^Collector sink xf supply]
   (-> (ISeqSpliterator/stream (seqiter xf supply))
       (.collect sink))))

(def +stream:base+
  '{java.util.stream.Stream
    {:produce produce-stream}
    java.util.stream.Collector
    {:collect collect-collector}})

(extend-stream +stream:base+)

;;
;; Streams
;;

(defn produce-deref
  "produces from a deref
 
   (produce-deref (volatile! 1))
   => '(1)"
  {:added "3.0"}
  ([source]
   (produce (deref source))))

(defn collect-promise
  "collects given a promise
 
   @(doto (promise)
      (collect (map inc) (range 5)))
   => 1"
  {:added "3.0"}
  ([sink xf supply]
   (deliver sink (first (seqiter xf supply)))))

(def +stream:promise+
  {(symbol (.getName ^Class (type (promise))))
   '{:produce produce-deref :collect collect-promise}})

(extend-stream +stream:promise+)

(def +stream:future+
  '{Future
    {:produce produce-deref}
    clojure.lang.IPending
    {:produce produce-deref}
    clojure.lang.IDeref
    {:produce produce-deref}})

(extend-stream +stream:future+)

;;
;; Sources
;;

(defn atom-seq
  "constructs an atom-seq
 
   (take 5 (atom-seq (atom -1) inc))
   => '(0 1 2 3 4)"
  {:added "3.0"}
  ([atm f]
   (gen/gen (loop []
              (let [val (swap! atm f)]
                (gen/yield val)
                (recur))))))

(defn object-seq
  "constructs an object-seq
 
   (take 5 (object-seq (volatile! -1)
                       #(vswap! % inc)))
   => '(0 1 2 3 4)"
  {:added "3.0"}
  ([obj f]
   (gen/gen (loop []
              (let [e (f obj)]
                (gen/yield e)
                (recur))))))

