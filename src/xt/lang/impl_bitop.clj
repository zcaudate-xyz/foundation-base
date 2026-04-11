(ns xt.lang.impl-bitop
  (:require [std.lang :as l  :refer [defspec.xt]])
  (:refer-clojure :exclude []))

(defn.xt bit-count
  "get the bit count"
  {:added "4.1"}
  [x]
  (var v0 (- x (xt/x:bit-and (xt/x:bit-rshift x 1) (:- "0x55555555"))))
  (var v1 (+ (xt/x:bit-and v0 (:- "0x33333333"))
             (xt/x:bit-and (xt/x:bit-rshift v0 2) (:- "0x33333333"))))
  (return
   (xt/x:bit-rshift
    (* (xt/x:bit-and (+ v1 (xt/x:bit-rshift v1 4))
                  (:- "0xF0F0F0F"))
       (:- "0x1010101"))
    24)))
