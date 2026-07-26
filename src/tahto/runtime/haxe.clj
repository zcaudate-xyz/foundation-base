(ns tahto.runtime.haxe
  (:require [std.lib :as h]
            [tahto.runtime.haxe.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/haxe
 impl/haxe:create
 impl/raw-eval-haxe)
