(ns debug-xt-load
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.lang.base.library :as lib]))

(defn run []
  (require 'xt.lang.base-lib)
  (let [lib (l/default-library)
        module (l/get-module lib :xtalk 'xt.lang.base-lib)]
    (println "Module lang:" (:lang module))
    (println "Expected lang: :xtalk")
    (println "Match?" (= (:lang module) :xtalk))))

(run)
