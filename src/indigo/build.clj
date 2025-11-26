(ns indigo.build
  (:require [std.lang :as l]
            [std.fs :as fs]))

(defn build []
  (let [js (l/emit-as :js '(indigo.frontend.main))]
    (fs/create-directory "resources/public")
    (spit "resources/public/main.js" js)))
