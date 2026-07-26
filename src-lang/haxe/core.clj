(ns haxe.core
  (:require [tahto.model.spec-haxe]
            [haxe.core.builtins :as builtins]
            [haxe.core.system :as system]
            [tahto.core :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [eval]))

(f/intern-all haxe.core.system
              haxe.core.builtins)

(l/script :haxe
  {})

(comment
  (./create-tests))
