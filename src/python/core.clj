(ns python.core
  (:require [python.core.builtins :as builtins]
            [python.core.system :as sys]
            [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [eval]))

(f/intern-all python.core.system
              python.core.builtins)

(l/script :python
  {})

(comment
  (./create-tests))
