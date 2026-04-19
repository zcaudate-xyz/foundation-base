(ns xt.lang.override.custom-lua
  (:require [std.lang :as l]))

(l/script :lua xt.lang)

(defmacro.lua ^{:static/override true}
  pad-left
  "override for pad left"
  {:added "4.0"}
  ([s n ch]
   (list 'cat (list 'string.rep ch (list '- n (list 'len s))) s)))

(defmacro.lua ^{:static/override true}
  pad-right
  "override for pad right"
  {:added "4.0"}
  ([s n ch]
   (list 'cat s (list 'string.rep ch (list '- n (list 'len s))))))
