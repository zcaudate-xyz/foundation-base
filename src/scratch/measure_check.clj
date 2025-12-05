(ns scratch.measure-check
  (:require [code.ai.measure :as measure]
            [std.lib :as h]))

(defn check []
  (try
    (let [repo-path "."
          history (measure/measure-history repo-path {:limit 5})]
      (h/local :println "History:" history))
    (catch Throwable e
      (h/local :println "Error:" e)
      (.printStackTrace e))))
