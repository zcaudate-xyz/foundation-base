(ns std.lib.stream.iter)

(defn i:map
  "iterator for map"
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
  ([f]
   (fn [coll]
     (letfn [(mapi [idx coll]
               (lazy-seq
                (when-let [s (seq coll)]
                  (cons (f idx (first s)) (mapi (inc idx) (rest s))))))]
       (mapi 0 coll)))))

(defn i:filter
  "iterator for filter"
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
  ([pred]
   (i:filter (complement pred))))

(defn i:keep
  "iterator for keep"
  ([f]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (let [x (f (first s))]
          (if (nil? x)
            ((i:keep f) (rest s))
            (cons x ((i:keep f) (rest s))))))))))

(defn i:keep-indexed
  "iterator for keep-indexed"
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
  ([n]
   (fn [coll]
     (lazy-seq
      (when (and (pos? n) (seq coll))
        (cons (first coll) ((i:take (dec n)) (rest coll))))))))

(defn i:drop
  "iterator for drop"
  ([n]
   (fn [coll]
     (lazy-seq
      (if (and (pos? n) (seq coll))
        ((i:drop (dec n)) (rest coll))
        coll)))))

(defn i:take-nth
  "iterator for take-nth"
  ([n]
   (fn [coll]
     (lazy-seq
      (when-let [s (seq coll)]
        (cons (first s)
              ((i:take-nth n) (drop (dec n) (rest s)))))))))

(defn i:drop-last
  "iterator for drop-last"
  ([n]
   (fn [coll]
     (map (fn [_ val] val) (drop n coll) coll))))

(defn i:butlast
  "iterator for butlast"
  []
  (i:drop-last 1))

(defn i:peek
  "iterator for peek"
  ([f]
   (i:map (fn [v] (doto v f)))))

(defn i:prn
  "iterator for prn"
  ([f]
   (i:map (fn [v] (doto v (-> f prn))))))

(defn i:mapcat
  "iterator for mapcat"
  ([f]
   (fn [coll]
     (mapcat f coll))))

(defn i:delay
  "iterator for delay"
  ([ms]
   (let [ms-fn (if (fn? ms) ms (constantly ms))]
     (i:map (fn [v] (Thread/sleep (long (ms-fn))) v)))))

(defn i:dedupe
  "iterator for dedupe"
  []
  (fn [coll]
    (dedupe coll)))

(defn i:partition-all
  "iterator for partition-all"
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
  ([f]
   (fn [coll]
     (partition-by f coll))))

(defn i:random-sample
  "iterator for random-sample"
  ([prob]
   (fn [coll]
     (random-sample prob coll))))

(defn i:sort
  "iterator for sort"
  ([]
   (fn [coll]
     (sort coll)))
  ([comp]
   (fn [coll]
     (sort comp coll))))

(defn i:sort-by
  "iterator for sort-by"
  ([keyfn]
   (fn [coll]
     (sort-by keyfn coll)))
  ([keyfn comp]
   (fn [coll]
     (sort-by keyfn comp coll))))

(defn i:reductions
  "iterator for reductions"
  ([f]
   (fn [coll]
     (reductions f coll)))
  ([f init]
   (fn [coll]
     (reductions f init coll))))

(defn i:some
  "iterator for some"
  ([pred]
   (fn [coll]
     (some pred coll))))

(defn i:count
  "iterator for count"
  ([]
   (fn [coll]
     (count coll))))

(defn i:reduce
  "iterator for reduce"
  ([f]
   (fn [coll]
     (reduce f coll)))
  ([f init]
   (fn [coll]
     (reduce f init coll))))

(defn i:max
  "iterator for max"
  ([]
   (fn [coll]
     (apply max coll))))

(defn i:min
  "iterator for min"
  ([]
   (fn [coll]
     (apply min coll))))

(defn i:mean
  "iterator for mean"
  ([]
   (fn [coll]
     (when (seq coll)
       (/ (apply + coll) (count coll))))))

(defn i:stdev
  "iterator for stdev"
  ([]
   (fn [coll]
     (let [c (vec coll)
           n (count c)
           mean (/ (apply + c) n)
           sum-sq-diff (reduce + (map #(let [diff (- % mean)] (* diff diff)) c))]
       (Math/sqrt (/ sum-sq-diff (dec n)))))))

(defn i:last
  "iterator for last"
  ([]
   (fn [coll]
     (last coll))))

(defn i:str
  "iterator for str"
  ([]
   (fn [coll]
     (apply str coll))))
