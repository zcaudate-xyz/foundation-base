(ns xt.lang.impl-bitop
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]])
  (:refer-clojure :exclude []))

(defn.xt bit-count
  "get the bit count"
  {:added "4.1"}
  [x]
  (var v0 (- x (x:bit-and (x:bit-rshift x 1) (:- "0x55555555"))))
  (var v1 (+ (x:bit-and v0 (:- "0x33333333"))
             (x:bit-and (x:bit-rshift v0 2) (:- "0x33333333"))))
  (return
   (x:bit-rshift
    (* (x:bit-and (+ v1 (x:bit-rshift v1 4))
                  (:- "0xF0F0F0F"))
       (:- "0x1010101"))
    24)))
