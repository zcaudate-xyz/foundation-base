(ns scratch
  (:require [std.lang :as l]
            [std.lang.base.book :as book]))

(defn -main []
  (println "Library:" (l/default-library)))
