(ns jvm.chisel.db.scan
  "SIMD scan/filter operator.

   For each lane i: match[i] = validMask[i] AND (every predicate holds on values[i]).
   Predicates are (op constant) pairs, e.g. [[:eq c0] [:gte c1]].
   `PredicateEq` is the special case of a single :eq predicate."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(defn- cmp-ref
  "clojure comparison fn for a keyword"
  [op]
  ({:eq = :neq not= :lt < :lte <= :gt > :gte >=} op))

(defn scan-ref
  "Reference model. `values` = seq of int lane values, `valid-mask` = int bitmask,
   `preds` = seq of [op-kw const]. Returns the match bitmask (int, lsb-first)."
  [values valid-mask preds]
  (let [v (vec values)
        n (count v)]
    (reduce (fn [m i]
              (let [valid (pos? (bit-and valid-mask (bit-shift-left 1 i)))
                    hit   (every? (fn [[op c]] ((cmp-ref op) (v i) c)) preds)]
                (if (and valid hit) (bit-or m (bit-shift-left 1 i)) m)))
            0 (range n))))

(defn scan-module
  "Build a scan module. opts: {:lanes n :width w :preds [[:eq 0] [:gte 1] ...] :name \"Scan\"}.
   One constant input port c0, c1, ... is created per predicate."
  [{:keys [lanes width preds name] :or {name "Scan"}}]
  (ch/module
   {:name name}
   (fn []
     (let [const-fields (mapv (fn [i] [(keyword (str "c" i)) (ch/input (ch/uint width))])
                              (range (count preds)))
           io (ch/io (ch/bundle (into [[:values    (ch/input (ch/vec lanes (ch/uint width)))]
                                       [:validMask (ch/input (ch/uint lanes))]
                                       [:matchMask (ch/output (ch/uint lanes))]]
                                      const-fields)))
           matches (mapv
                    (fn [i]
                      (reduce (fn [acc [pi [op _]]]
                                (let [c   (ch/field io (keyword (str "c" pi)))
                                      cmp ((db/op->fn op) (ch/index (ch/field io :values) i) c)]
                                  (ch/and acc cmp)))
                              (ch/index (ch/field io :validMask) i)
                              (map-indexed vector preds)))
                    (range lanes))]
       (ch/connect! (ch/field io :matchMask) (db/mask-pack matches))))))
