(ns play.ts-000-user-directory.types
  (:require [hara.lang :as l]
            [hara.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

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
  (return (xt/x:get-key users id)))

(defn.xt ^{:- [[:xt/array UserId]]}
  userIds
  [UserMap users]
  (return (xt/x:arr-map (xt/x:obj-keys users)
                        (fn [id]
                          (return id)))))

(def.xt ^{:- [:xt/int]}
  DEFAULT_PAGE_SIZE
  20)
