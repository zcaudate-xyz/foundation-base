(ns lang-demos.go-001-xtalk-user-directory.main
  (:require [lang.core :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as common-data]
             [xt.lang.spec-base :as xt]]})

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

(defn.xt ^{:public true}
  lookupUser
  [users id]
  (return (x:get-key users id)))

(defspec.xt userIds
  [:fn [UserMap] [:xt/array UserId]])

(defn.xt ^{:public true}
  userIds
  [users]
  (return (x:obj-keys users)))

(defspec.xt putUser
  [:fn [UserMap User] UserMap])

(defn.xt ^{:public true}
  putUser
  [users user]
  (:= (. users [(x:get-key user "id")]) user)
  (return users))

(defspec.xt removeUser
  [:fn [UserMap UserId] UserMap])

(defn.xt ^{:public true}
  removeUser
  [users id]
  (when (not= nil (x:get-key users id))
    (x:del (. users [id])))
  (return users))

(defspec.xt resetUserMap
  [:fn [UserMap] UserMap])

(defn.xt ^{:public true}
  resetUserMap
  [users]
  (xt/for:array [id (x:obj-keys users)]
    (x:del (. users [id])))
  (return users))

(defspec.xt userCount
  [:fn [UserMap] :xt/int])

(defn.xt ^{:public true}
  userCount
  [users]
  (return (x:len (x:obj-keys users))))

(defspec.xt DEFAULT_PAGE_SIZE
  :xt/int)

(def.xt
  DEFAULT_PAGE_SIZE
  20)

(defspec.xt pageUserIds
  [:fn [UserMap :xt/int :xt/int] [:xt/array UserId]])

(defn.xt ^{:public true}
  pageUserIds
  [users offset pageSize]
  (var ids (-/userIds users))
  (var skip (x:m-max 0 offset))
  (var size (:? (<= pageSize 0) -/DEFAULT_PAGE_SIZE pageSize))
  (var page [])
  (xt/for:array [id ids]
    (if (> skip 0)
      (:= skip (- skip 1))
      (when (< (x:len page) size)
        (x:arr-push page id))))
  (return page))
