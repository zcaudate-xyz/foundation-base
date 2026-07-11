(ns jvm.chisel.db.hash
  "Multiplicative hash: bucket = high log-buckets bits of (key * K),
   a building block for hash join and group-by."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(defn hash-ref
  "Reference model. `key` int, `width` key width, `k` multiplier, `log-n` = log2(buckets)."
  [key width k log-n]
  (let [prod  (* key k)
        shift (- (* 2 width) log-n)]
    (bit-and (bit-shift-right prod shift)
             (dec (bit-shift-left 1 log-n)))))

(defn hash-module
  "Build a hash module. opts: {:width w :buckets B :k K :name \"Hash\"}.
   `buckets` must be a power of two; output width = log2(buckets)."
  [{:keys [width buckets k name] :or {name "Hash" k 0x9E}}]
  (let [log-n (long (Math/ceil (/ (Math/log buckets) (Math/log 2))))]
    (ch/module
     {:name name}
     (fn []
       (let [io (ch/io (ch/bundle [[:key    (ch/input (ch/uint width))]
                                   [:bucket (ch/output (ch/uint log-n))]]))]
         (ch/connect! (ch/field io :bucket)
                      (db/mhash (ch/field io :key) width k log-n)))))))
