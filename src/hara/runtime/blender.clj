(ns hara.runtime.blender
  (:require [std.lib :as h]
            [hara.runtime.blender.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/blender
 impl/blender:create
 impl/raw-eval-blender)
