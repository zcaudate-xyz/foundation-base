(ns haxe.examples
  "Example Haxe program using the xtalk Haxe grammar and runtime.

   The Haxe runtime is currently one-shot: each eval compiles and runs
   a fresh Haxe program via `haxe --interp`.  The grammar is a foundation
   and emits Haxe-shaped code; the exact syntax is still being tuned."
  (:require [hara.lang :as l]
            [haxe.core :as haxe]
            [hara.runtime.haxe.impl]
            [std.lib.template :as template])
  (:refer-clojure :exclude [eval]))

(l/script :haxe
  haxe.examples
  {:runtime :haxe})

;; Note: `template/$` works inside `defmacro.hx` bodies but currently
;; causes preprocessor resolution errors inside `defn.hx` bodies, so
;; function bodies below use plain xtalk forms.

(defn.hx hello [name]
  (return (+ "Hello, " name "!")))

(defn.hx add [x y]
  (return (+ x y)))

(defn.hx factorial [n]
  (if (<= n 1)
    (return 1)
    (return (* n (factorial (- n 1))))))

(defn.hx sum [numbers]
  (var total 0)
  (for:array [n numbers]
    (:= total (+ total n)))
  (return total))

(defn.hx greet [name]
  (trace (+ "Hi, " name))
  (return nil))

(defmacro.hx log [msg]
  "Emits a Haxe trace statement."
  (template/$ (trace ~msg)))

(def.hx pi 3.14159)

(defn.hx circle-area [r]
  (return (* pi r r)))
