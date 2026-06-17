(ns haxe.core.builtins
  (:require [hara.lang :as l]
            [std.lib.template :as template])
  (:refer-clojure :exclude [eval]))

(l/script :haxe
  haxe.core
  {})

(def$.hx ^{:arglists '([x])} trace trace)

(defmacro.hx json:encode
  "encodes a value to a JSON string"
  {:added "4.1"}
  [obj]
  (template/$ (haxe.Json.stringify ~obj)))

(defmacro.hx json:decode
  "decodes a JSON string to a value"
  {:added "4.1"}
  [s]
  (template/$ (haxe.Json.parse ~s)))

(defmacro.hx type:of
  "returns the Haxe type name of a value"
  {:added "4.1"}
  [obj]
  (template/$ (str (type ~obj))))
