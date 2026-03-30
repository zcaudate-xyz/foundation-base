(ns play.ts-000-user-directory.types
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defspec.xt UserId
  :xt/str)

(defspec.xt User
  [:xt/record
   ["id" UserId]
   ["displayName" [:xt/maybe :xt/str]]
   ["roles" [:xt/array :xt/str]]])

(defspec.xt UserMap
  [:xt/dict UserId User])

(defn.xt ^{:- [[:xt/maybe User]]}
  lookupUser
  [UserMap users
   UserId id]
  (return (k/get-key users id)))

(defn.xt ^{:- [[:xt/array UserId]]}
  userIds
  [UserMap users]
  (return (k/arr-map (k/obj-keys users)
                     (fn [id]
                       (return id)))))

(def.xt ^{:- [:xt/int]}
  DEFAULT_PAGE_SIZE
  20)
