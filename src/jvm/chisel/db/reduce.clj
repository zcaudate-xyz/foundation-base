(ns jvm.chisel.db.reduce
  "Masked tree-reduction operator covering GROUP BY aggregates:
   :sum, :count, :min, :max over a Vec of UInt(width), gated by a valid mask."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(defn reduce-ref
  "Reference model. `values` = seq of ints, `valid-mask` = int bitmask,
   `op` in #{:sum :count :min :max}, `width` = lane width."
  [values valid-mask op width]
  (let [v      (vec values)
        n      (count v)
        valid? (fn [i] (pos? (bit-and valid-mask (bit-shift-left 1 i))))
        maxv   (dec (bit-shift-left 1 width))]
    (case op
      :count (reduce + (map (fn [i] (if (valid? i) 1 0)) (range n)))
      :sum   (reduce + (map (fn [i] (if (valid? i) (v i) 0)) (range n)))
      :min   (reduce min (map (fn [i] (if (valid? i) (v i) maxv)) (range n)))
      :max   (reduce max (map (fn [i] (if (valid? i) (v i) 0)) (range n))))))

(defn- out-width
  [op lanes width]
  (case op
    :count (db/log2-ceil lanes)
    :sum   (+ width (db/log2 lanes))
    width))

(defn reduce-module
  "Build a reduce module. opts: {:lanes n :width w :op :sum|:count|:min|:max :name \"Reduce\"}."
  [{:keys [lanes width op name] :or {name "Reduce"}}]
  (let [ow    (out-width op lanes width)
        maxv  (ch/u (dec (bit-shift-left 1 width)) width)]
    (ch/module
     {:name name}
     (fn []
       (let [io (ch/io (ch/bundle [[:values    (ch/input (ch/vec lanes (ch/uint width)))]
                                   [:validMask (ch/input (ch/uint lanes))]
                                   [:result    (ch/output (ch/uint ow))]]))
             mask (ch/field io :validMask)
             vals (ch/field io :values)
             res  (case op
                    :count (db/popcount mask lanes)
                    :sum   (db/tree-reduce ch/add (ch/u 0 width)
                                           (db/gated vals mask lanes (ch/u 0 width)))
                    :min   (db/tree-reduce (fn [a b] (ch/mux (ch/lt a b) a b)) maxv
                                           (db/gated vals mask lanes maxv))
                    :max   (db/tree-reduce (fn [a b] (ch/mux (ch/gt a b) a b)) (ch/u 0 width)
                                           (db/gated vals mask lanes (ch/u 0 width))))]
         (ch/connect! (ch/field io :result) res))))))
