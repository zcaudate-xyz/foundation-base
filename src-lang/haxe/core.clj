(ns haxe.core
  (:require [hara.model.spec-haxe]
            [haxe.core.builtins :as builtins]
            [haxe.core.system :as system]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [eval]))

(f/intern-all haxe.core.system
              haxe.core.builtins)

(l/script :haxe
  {})

(comment
  (./create-tests))
