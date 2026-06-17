(ns haxe.core.system
  (:require [hara.lang :as l]
            [std.lib.template :as template])
  (:refer-clojure :exclude [eval]))

(l/script :haxe
  haxe.core
  {})

(defmacro.hx sys:args
  "returns command-line arguments"
  {:added "4.1"}
  []
  '(Sys.args))

(defmacro.hx sys:print
  "prints to stdout without a newline"
  {:added "4.1"}
  [s]
  (template/$ (Sys.print ~s)))

(defmacro.hx sys:println
  "prints a line to stdout"
  {:added "4.1"}
  [s]
  (template/$ (Sys.println ~s)))

(defmacro.hx sys:exit
  "exits the process with the given code"
  {:added "4.1"}
  [code]
  (template/$ (Sys.exit ~code)))
