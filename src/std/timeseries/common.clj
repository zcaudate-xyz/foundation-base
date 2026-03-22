(ns std.timeseries.common
  (:require [std.lib.collection :as collection]
            [std.lib.foundation :as core]
            [std.lib.time :as time]
            [std.math :as math])
  (:import (java.text SimpleDateFormat)))

(defn linspace
  "takes the linear space of n samples
 
   (linspace (range 100) 10)
   => [0 11 22 33 44 55 66 77 88 99]"
  {:added "3.0"}
  ([arr ^long n]
   (let [len (count arr)]
     (if (<= len n)
       arr
       (case n
         1 [(last arr)]
         2 [(first arr) (last arr)]
         (let [len  (dec len)
               spc  (quot len (dec n))]
           (-> (take-nth spc arr)
               (butlast)
               (vec)
               (conj (last arr)))))))))

(defn cluster
  "clusters a space with an aggregate function
 
   (cluster (range 1 200) 10  math/mean)
   => [10 29 48 67 86 105 124 143 162 181]"
  {:added "3.0"}
  ([arr n f]
   (let [len   (count arr)
         squot (quot len n)
         squot (if (zero? squot) 1 squot)]
     (map f (partition squot arr)))))

(defn make-empty
  "creates an empty entry given structure
 
   (make-empty {:a 1 :b \"hello\" :c :world})
   => {:a 0, :b \"\", :c nil}"
  {:added "3.0"}
  ([entry]
   (collection/map-vals (fn [val]
                 (cond (number? val) 0
                       (string? val) ""
                       :else nil))
               entry)))

(defn raw-template
  "creates a template for nest/flat mapping
 
   (raw-template {:a {:b 1 :c \"2\"}})
   => {:nest {:a {:b \"{{a.b}}\",
                  :c \"{{a.c}}\"}},
       :flat {:a.b \"{{a.b}}\",
              :a.c \"{{a.c}}\"},
       :types {:a.b java.lang.Long,
               :a.c java.lang.String}
       :empty {:a.b 0, :a.c \"\"}}"
  {:added "3.0"}
  ([entry]
   (binding [core/*sep* "."]
     (let [dflat  (collection/tree-flatten entry)
           dtypes (collection/map-vals class dflat)
           tflat  (collection/map-entries (fn [[k v]]
                                   [k (str "{{" (core/strn k) "}}")]) dflat)
           tnest  (collection/tree-nestify tflat)]
       {:nest tnest :flat tflat :types dtypes :empty (make-empty dflat)}))))

(defn flat-fn
  "creates a nested to flat transform
 
   (def transform-fn (flat-fn (raw-template {:a {:b {:c 1}}})))
 
   (transform-fn {:a {:b {:c 10}}})
   => {:a.b.c 10}"
  {:added "3.0"}
  ([template]
   (collection/transform-fn template [:nest :flat])))

(defn nest-fn
  "creates a nested to flat transform
 
   (def transform-fn (nest-fn (raw-template {:a {:b {:c 1}}})))
 
   (transform-fn {:a.b.c 10})
   => {:a {:b {:c 10}}}"
  {:added "3.0"}
  ([template]
   (collection/transform-fn template [:flat :nest])))

(defn create-template
  "creates a template for entry"
  {:added "3.0"}
  ([entry]
   (let [tmpl    (raw-template entry)
         flat-fn (flat-fn tmpl)
         nest-fn (nest-fn tmpl)]
     {:raw tmpl :nest nest-fn :flat flat-fn})))

(defn order-flip
  "returns the opposite of the order
 
   (order-flip :asc)
   => :desc"
  {:added "3.0"}
  ([order]
   (case order :asc :desc :desc :asc)))

(defn order-fn
  "creates a function that returns or flips the input
 
   (mapv (order-fn [:up :down])
         [:asc :desc]
         [false true])
   => [:up :up]"
  {:added "3.0"}
  ([[asc desc]]
   (fn order-function
     ([order]
      (order-function order false))
     ([order flip]
      (cond-> order
        flip  (order-flip)
        :then (case :asc asc :desc desc))))))

(def order-comp    (order-fn [< >]))
(def order-comp-eq (order-fn [<= >=]))
(def order-op      (order-fn [+ -]))

(defn order-fns
  "returns commonly used functions
 
   (order-fns :asc)
   => {:comp-fn <, :comp-eq-fn <=, :op-fn +}"
  {:added "3.0"}
  ([order]
   (order-fns order false))
  ([order flip]
   {:comp-fn    (order-comp order flip)
    :comp-eq-fn (order-comp-eq order flip)
    :op-fn (order-op order flip)}))

(def +default+
  {:time {:key identity :unit :ms :order :asc}
   :interval [:start :end]
   :sample   [100 :even]})

(defn from-ms
  "converts ms time into a given unit
 
   (from-ms 100 :ns)
   => 100000000"
  {:added "3.0"}
  (^long [ms to]
   (case to
     :ns (* ms 1000000)
     :us (* ms 1000)
     :ms ms
     :s  (quot ms 1000)
     :m  (quot ms (* 60 1000))
     :h  (quot ms (* 60 60 1000))
     :d  (quot ms (* 24 60 60 1000)))))

(defn to-ms
  "converts a time to ms time
 
   (to-ms 10000 :ns)
   => 0
 
   (to-ms 10000 :s)
   => 10000000"
  {:added "3.0"}
  (^long [ms from]
   (case from
     :ns (quot ms 1000000)
     :us (quot ms 1000)
     :ms ms
     :s  (* ms 1000)
     :m  (* ms 60 1000)
     :h  (* ms 60 60 1000)
     :d  (* ms 24 60 60 1000))))

(defn from-ns
  "converts a time from ns"
  {:added "3.0"}
  (^long [ns to]
   (case to
     :ns ns
     :us (quot ns 1000)
     (from-ms (to-ms ns :ns) to))))

(defn duration
  "calculates the duration from array and options
 
   (duration [1 2 3 4] {:key identity :unit :m})
   => 180000"
  {:added "3.0"}
  ([arr {:keys [key unit order]}]
   (let [t0 (key (first arr))
         t1 (key (last arr))]
     (to-ms (math/abs (- t0 t1)) unit))))

(defn parse-time
  "parses a string or keyword time representation
 
   (parse-time :0.1m :us)
   => 6000000
 
   (parse-time :0.5ms :us)
   => 500"
  {:added "3.0"}
  ([s]
   (parse-time s :ms))
  ([s unit]
   (let [s (core/strn s)]
     (from-ms (or (if-let [t (time/parse-ns s)]
                    (/ t 1000000))
                  (time/parse-ms s))
              unit))))

(defn parse-time-expr
  "parses a time expression
 
   (parse-time-expr {:interval \"0.5ms\"})
   => {:key identity, :unit :ms, :order :asc, :interval 0}"
  {:added "3.0"}
  ([m]
   (let [{:keys [unit interval] :as m} (merge (:time +default+) m)
         interval (cond (number? interval)
                        interval

                        (or (string? interval)
                            (keyword? interval))
                        (parse-time interval unit))]
     (assoc m :interval interval))))

(defmulti sampling-fn
  "extensible sampling function"
  {:added "3.0"}
  identity)

(defmulti sampling-parser
  "extensible parser function"
  {:added "3.0"}
  identity)

(defmethod sampling-parser :default
  ([_]
   nil))

(defn parse-sample-expr
  "parses a sample expression
 
   (parse-sample-expr [10] {})
   => {:size 10, :strategy :linear}"
  {:added "3.0"}
  ([sample time-opts]
   (let [opts (cond (map? sample) sample

                    (vector? sample)
                    (if (or (keyword? (first sample))
                            (string? (first sample)))
                      {:size [0 sample] :strategy :range}
                      (let [[size strategy m] sample]
                        (merge {:size size :strategy (or strategy :linear)}
                               m)))

                    (integer? sample)
                    (parse-sample-expr [sample] time-opts)

                    (or (keyword? sample)
                        (string? sample))
                    {:size sample :strategy :interval :default {:aggregate :first}})
         parser (sampling-parser (:strategy opts))]
     (if parser
       (parser opts time-opts)
       opts))))

(defn process-sample
  "process sample given sampling function
 
   (process-sample (range 100)
                   (parse-sample-expr [10 :random] {})
                   {:key identity :order :asc})
   ;; (4 20 39 51 51 60 70 72 89 96)
   => coll?
 
   ;; extended sampling function from range
   (process-sample (range 100)
                   (parse-sample-expr [[0 :10ms] :range] {:key identity :order :asc :unit :ms})
                   {:key identity :order :asc :unit :ms})
   => [0 1 2 3 4 5 6 7 8 9 10]"
  {:added "3.0"}
  ([arr {:keys [size strategy every] :as m} time-opts]
   (if (nil? m)
     arr
     (let [{:keys [order key sort]} time-opts
           comp-fn (order-comp order)
           samples (case strategy
                     :linear (linspace arr size)
                     :random (take size (repeatedly #(rand-nth arr)))
                     :start  (take size (take-nth (or every 1) arr))
                     :end    (take size (take-nth (or every 1) (reverse arr)))
                     (let [f (sampling-fn strategy)]
                       (f arr m time-opts)))]
       (if (false? sort)
         samples
         (sort-by key comp-fn samples))))))

