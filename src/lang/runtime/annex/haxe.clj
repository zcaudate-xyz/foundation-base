(ns lang.runtime.annex.haxe
  (:require [std.lib :as h]
            [lang.runtime.annex.haxe.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/haxe
 impl/haxe:create
 impl/raw-eval-haxe)
