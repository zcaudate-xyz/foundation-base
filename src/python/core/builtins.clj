(ns python.core.builtins
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:refer-clojure :exclude [eval]))

(l/script :python
  python.core
  {})

(def$.py ^{:arglists '([])} globals globals)

(def$.py ^{:arglists '([])} locals locals)

(def$.py ^{:arglists '([s & [globals locals]])} exec exec)

(def$.py ^{:arglists '([s & [globals locals]])} eval eval)

(def$.py ^{:arglists '([dict k])} hasattr hasattr)

(def$.py ^{:arglists '([dict k])} getattr getattr)
