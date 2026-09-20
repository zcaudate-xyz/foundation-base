(ns lang.runtime.annex.blender
  (:require [std.lib :as h]
            [lang.runtime.annex.blender.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/blender
 impl/blender:create
 impl/raw-eval-blender)
