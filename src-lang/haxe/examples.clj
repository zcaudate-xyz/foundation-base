(ns haxe.examples
  (:require [hara.lang :as l]
            [haxe.core :as haxe]
            [hara.runtime.haxe.impl]
            [std.lib.template :as template])
  (:refer-clojure :exclude [eval]))

(l/script :haxe
  haxe.examples
  {:runtime :haxe})

(defn.hx hello [name]
  (+ "Hello, " name "!"))

(defn.hx add [x y]
  (+ x y))

(defn.hx factorial [n]
  (if (<= n 1)
    1
    (* n (factorial (- n 1)))))

(defn.hx sum [numbers]
  (var total 0)
  (for:array [n numbers]
    (:= total (+ total n)))
  total)

(defmacro.hx log [msg]
  (template/$ (trace ~msg)))

(def.hx pi 3.14159)

(defn.hx circle-area [r]
  (* pi r r))
