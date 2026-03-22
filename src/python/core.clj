(ns python.core
  (:require [python.core.builtins :as builtins]
            [python.core.system :as sys]
            [std.lang :as l]
            [std.lib.foundation])
  (:refer-clojure :exclude [eval]))

(std.lib.foundation/intern-all python.core.system
              python.core.builtins)

(l/script :python
  {})

(comment
  (./create-tests))
