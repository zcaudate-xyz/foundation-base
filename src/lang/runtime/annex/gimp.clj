(ns lang.runtime.annex.gimp
  (:require [std.lib :as h]
            [lang.runtime.annex.gimp.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/gimp
 impl/gimp:create
 impl/raw-eval-gimp)
