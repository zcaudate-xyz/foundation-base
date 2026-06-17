(ns hara.runtime.haxe
  (:require [std.lib :as h]
            [hara.runtime.haxe.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/haxe
 impl/haxe:create
 impl/raw-eval-haxe)
