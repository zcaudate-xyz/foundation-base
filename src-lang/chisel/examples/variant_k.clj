(ns chisel.examples.variant-k
  "Composed streaming entry point for the variant measure kernels: a
   length-`L` sliding-window k-measure (`jvm.chisel.variant.window/k-window-module`)
   exposed as a ready-to-emit `KStream` module.

   One bit per cycle on `in` (gated by `valid`); the registered outputs
   `p, k0..k3` carry the running k-measure `[p [k0 k1 k2 k3]]` of the current
   window, updating in O(1) per cycle (subtract the leaving left-edge pair, add
   the entering right-edge pair)."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant.window :as win]))

(defn k-stream-module
  "opts {:keys [length]} -> a KStream module (see `k-window-module`)."
  [{:keys [length] :or {length 4}}]
  (win/k-window-module {:length length :name "KStream"}))

(def k-stream-4
  (k-stream-module {:length 4}))

(comment
  (println (ch/emit-firrtl (k-stream-module {:length 4})))
  (println (ch/emit-system-verilog (k-stream-module {:length 4}))))
