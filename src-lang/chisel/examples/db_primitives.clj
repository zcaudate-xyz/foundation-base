(ns chisel.examples.db-primitives
  "A composed scan -> count datapath built from the `jvm.chisel.db` fragments:
   compare-vector (eq) -> mask -> popcount. One module, two operators."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(defn scan-count-module
  "opts: {:lanes n :width w}. Output matchCount has ceil(log2(n+1)) bits."
  [{:keys [lanes width] :or {lanes 8 width 8}}]
  (ch/module
   {:name "ScanCount"}
   (fn []
     (let [io (ch/io (ch/bundle [[:values     (ch/input (ch/vec lanes (ch/uint width)))]
                                 [:validMask  (ch/input (ch/uint lanes))]
                                 [:constant   (ch/input (ch/uint width))]
                                 [:matchCount (ch/output (ch/uint (db/log2-ceil lanes)))]]))
           matches (db/cmp-vec (ch/field io :values) (ch/field io :constant) ch/eq lanes)
           masked  (mapv (fn [i] (ch/and (ch/index (ch/field io :validMask) i)
                                         (nth matches i)))
                         (range lanes))
           cnt     (db/popcount (db/mask-pack masked) lanes)]
       (ch/connect! (ch/field io :matchCount) cnt)))))

(def scan-count-8
  (scan-count-module {}))

(comment
  (println (ch/emit-firrtl (scan-count-module {})))
  (println (ch/emit-system-verilog (scan-count-module {}))))
