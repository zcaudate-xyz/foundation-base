(ns play.go-001-xtalk-user-directory.main
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as common-data]]})

(defspec.xt UserId
  :xt/str)

(defspec.xt User
  [:xt/record
   ["id" UserId]
   ["displayName" [:xt/maybe :xt/str]]
   ["roles" [:xt/array :xt/str]]])

(defspec.xt UserMap
  [:xt/dict UserId User])

(defspec.xt lookupUser
  [:fn [UserMap UserId] [:xt/maybe User]])

(defn.xt
  lookupUser
  [users id]
  (return (x:get-key users id)))

(defspec.xt userIds
  [:fn [UserMap] [:xt/array UserId]])

(defn.xt
  userIds
  [users]
  (return (x:arr-map (x:obj-keys users)
                     (fn [id]
                       (return id)))))

(defspec.xt DEFAULT_PAGE_SIZE
  :xt/int)

(def.xt
  DEFAULT_PAGE_SIZE
  20)
