(ns chisel.examples.predicate-eq
  "Scan-predicate block expressed with the `jvm.chisel` primitive mapping.

   For each lane i: match = validMask[i] AND (values[i] === constant).
   The eight per-lane bits are packed into the matchMask output.

   Field access on a bundle uses `ch/field` (a Chisel Record cannot also be a
   Clojure ILookup, so `(:values io)` is not available)."
  (:require [jvm.chisel :as ch]))

(def predicate-eq
  (ch/module
   {:name "PredicateEq"}
   (fn []
     (let [io (ch/io
               (ch/bundle
                [[:values    (ch/input  (ch/vec 8 (ch/uint 8)))]
                 [:validMask (ch/input  (ch/uint 8))]
                 [:constant  (ch/input  (ch/uint 8))]
                 [:matchMask (ch/output (ch/uint 8))]]))
           matches (mapv (fn [i]
                           (ch/and (ch/index (ch/field io :validMask) i)
                                   (ch/eq (ch/index (ch/field io :values) i)
                                          (ch/field io :constant))))
                         (range 8))]
       (ch/connect! (ch/field io :matchMask)
                    (ch/vec-as-uint matches))))))

(comment
  ;; FIRRTL emission needs no external toolchain:
  (println (ch/emit-firrtl predicate-eq))

  ;; SystemVerilog needs a version-matched firtool (CIRCT); Chisel bundles a
  ;; resolver that fetches one when it is not on PATH:
  (println (ch/emit-system-verilog predicate-eq)))
