(ns play.ts-000-user-directory.main
  (:require [std.lang :as l]))

(l/script :js)

(def.js DEFAULT_PAGE_SIZE 20)

(defn.js lookupUser
  [users id]
  (return (. users [id])))

(defn.js userIds
  [users]
  (return (. Object (keys users))))
