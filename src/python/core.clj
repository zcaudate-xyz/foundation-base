(ns python.core
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [python.core.system :as sys]
            [python.core.builtins :as builtins])
  (:refer-clojure :exclude [eval]))

(h/intern-all python.core.system
              python.core.builtins)

(l/script :python
  {})

(comment
  (./create-tests))
