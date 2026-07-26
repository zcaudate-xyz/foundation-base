(ns tahto.runtime.gimp
  (:require [std.lib :as h]
            [tahto.runtime.gimp.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/gimp
 impl/gimp:create
 impl/raw-eval-gimp)
