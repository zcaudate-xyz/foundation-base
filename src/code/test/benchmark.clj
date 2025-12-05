(ns code.test.benchmark)

(defn current-time []
  (System/nanoTime))

(defn stats [times]
  (let [n (count times)
        sum (reduce + times)
        mean (/ sum n)
        sorted (sort times)
        min (first sorted)
        max (last sorted)
        variance (/ (reduce + (map #(Math/pow (- % mean) 2) times)) n)
        std-dev (Math/sqrt variance)]
    {:n n
     :mean mean
     :min min
     :max max
     :std-dev std-dev
     :total sum}))

(defmacro bench
  "benchmarks an expression

   (bench (Thread/sleep 10) {:samples 5})
   => (contains {:mean number?})"
  {:added "3.0"}
  ([expr]
   `(bench ~expr {}))
  ([expr {:keys [samples warmup] :or {samples 100 warmup 5}}]
   `(let [run-fn# (fn [] ~expr)]
      ;; Warmup
      (dotimes [_# ~warmup]
        (run-fn#))

      ;; Actual run
      (let [times# (loop [i# 0
                          acc# []]
                     (if (< i# ~samples)
                       (let [start# (current-time)
                             _# (run-fn#)
                             end# (current-time)]
                         (recur (inc i#) (conj acc# (- end# start#))))
                       acc#))]
        (assoc (stats times#)
               :unit "ns")))))
