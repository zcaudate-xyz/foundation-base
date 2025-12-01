(ns std.lib.stream.iter)

(defn i:map
  "iterator for map"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (cons (f (first s)) ((i:map f) (rest s)))))))
  ([f & colls]
   (fn [coll]
     (lazy-seq
      (let [s (map seq (cons coll colls))]
        (when (every? identity s)
          (let [next-s (map rest s)]
            (cons (apply f (map first s))
                  ((apply i:map f (rest next-s)) (first next-s))))))))))

(defn i:map-indexed
  "iterator for map-indexed"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (letfn [(mapi [idx coll]
               (lazy-seq
                (when-let [s (seq coll)]
                  (cons (f idx (first s)) (mapi (inc idx) (rest s))))))]
       (mapi 0 coll)))))

(defn i:filter
  "iterator for filter"
  {:added "4.1"}
  ([pred]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (let [f (first s) r (rest s)]
          (if (pred f)
            (cons f ((i:filter pred) r))
            ((i:filter pred) r))))))))

(defn i:remove
  "iterator for remove"
  {:added "4.1"}
  ([pred]
   (i:filter (complement pred))))

(defn i:keep
  "iterator for keep"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (let [x (f (first s))]
          (if (nil? x)
            ((i:keep f) (rest s))
            (cons x ((i:keep f) (rest s))))))))))

(defn i:keep-indexed
  "iterator for keep-indexed
   ((i:keep-indexed (fn [i v] (if (odd? i) v))) [:a :b :c :d])
   => '(:b :d)"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (letfn [(keepi [idx coll]
               (lazy-seq
                (when-let [s (seq coll)]
                  (let [x (f idx (first s))]
                    (if (nil? x)
                      (keepi (inc idx) (rest s))
                      (cons x (keepi (inc idx) (rest s))))))))]
       (keepi 0 coll)))))

(defn i:take
  "iterator for take"
  {:added "4.1"}
  ([n]
   (fn [coll]
     (lazy-seq
      (when (and (pos? n) (seq coll))
        (cons (first coll) ((i:take (dec n)) (rest coll))))))))

(defn i:drop
  "iterator for drop"
  {:added "4.1"}
  ([n]
   (fn [coll]
     (lazy-seq
      (if (and (pos? n) (seq coll))
        ((i:drop (dec n)) (rest coll))
        coll)))))

(defn i:take-nth
  "iterator for take-nth"
  {:added "4.1"}
  ([n]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (cons (first s)
              ((i:take-nth n) (drop (dec n) (rest s)))))))))

(defn i:drop-last
  "iterator for drop-last"
  {:added "4.1"}
  ([n]
   (fn [coll]
     (map (fn [_ val] val) (drop n coll) coll))))

(defn i:butlast
  "iterator for butlast"
  {:added "4.1"}
  []
  (i:drop-last 1))

(defn i:peek
  "iterator for peek"
  {:added "4.1"}
  ([f]
   (i:map (fn [v] (doto v f)))))

(defn i:prn
  "iterator for prn"
  {:added "4.1"}
  ([f]
   (i:map (fn [v] (doto v (-> f prn))))))

(defn i:mapcat
  "iterator for mapcat"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (mapcat f coll))))

(defn i:delay
  "iterator for delay"
  {:added "4.1"}
  ([ms]
   (let [ms-fn (if (fn? ms) ms (constantly ms))]
     (i:map (fn [v] (Thread/sleep (long (ms-fn))) v)))))

(defn i:dedupe
  "iterator for dedupe"
  {:added "4.1"}
  []
  (fn [coll]
    (dedupe coll)))

(defn i:partition-all
  "iterator for partition-all"
  {:added "4.1"}
  ([n]
   (fn [coll]
     (partition-all n coll)))
  ([n step]
   (fn [coll]
     (partition-all n step coll)))
  ([n step pad]
   (fn [coll]
     (partition-all n step pad coll))))

(defn i:partition-by
  "iterator for partition-by"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (partition-by f coll))))

(defn i:random-sample
  "iterator for random-sample"
  {:added "4.1"}
  ([prob]
   (fn [coll]
     (random-sample prob coll))))

(defn i:sort
  "iterator for sort"
  {:added "4.1"}
  ([]
   (fn [coll]
     (sort coll)))
  ([comp]
   (fn [coll]
     (sort comp coll))))

(defn i:sort-by
  "iterator for sort-by"
  {:added "4.1"}
  ([keyfn]
   (fn [coll]
     (sort-by keyfn coll)))
  ([keyfn comp]
   (fn [coll]
     (sort-by keyfn comp coll))))

(defn i:reductions
  "iterator for reductions"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (reductions f coll)))
  ([f init]
   (fn [coll]
     (reductions f init coll))))

(defn i:some
  "iterator for some"
  {:added "4.1"}
  ([pred]
   (fn [coll]
     (some pred coll))))

(defn i:count
  "iterator for count"
  {:added "4.1"}
  ([]
   (fn [coll]
     (count coll))))

(defn i:reduce
  "iterator for reduce"
  {:added "4.1"}
  ([f]
   (fn [coll]
     (reduce f coll)))
  ([f init]
   (fn [coll]
     (reduce f init coll))))

(defn i:max
  "iterator for max"
  {:added "4.1"}
  ([]
   (fn [coll]
     (apply max coll))))

(defn i:min
  "iterator for min"
  {:added "4.1"}
  ([]
   (fn [coll]
     (apply min coll))))

(defn i:mean
  "iterator for mean"
  {:added "4.1"}
  ([]
   (fn [coll]
     (when (seq coll)
       (/ (apply + coll) (count coll))))))

(defn i:stdev
  "iterator for stdev"
  {:added "4.1"}
  ([]
   (fn [coll]
     (let [c (vec coll)
           n (count c)
           mean (/ (apply + c) n)
           sum-sq-diff (reduce + (map #(let [diff (- % mean)] (* diff diff)) c))]
       (Math/sqrt (/ sum-sq-diff (dec n)))))))

(defn i:last
  "iterator for last"
  {:added "4.1"}
  ([]
   (fn [coll]
     (last coll))))

(defn i:str
  "iterator for str"
  {:added "4.1"}
  ([]
   (fn [coll]
     (apply str coll))))
