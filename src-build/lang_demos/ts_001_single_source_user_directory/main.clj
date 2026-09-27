(ns lang-demos.ts-001-single-source-user-directory.main
  (:require [lang.core :as l]
            [xt.lang.common-lib]
            [xt.lang.common-data]
            [lang.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as common-data]]})

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
  (var result nil)
  (xt/for:array [key (x:obj-keys users)]
    (when (== key id)
      (:= result (x:get-key users key))))
  (return result))

(defspec.xt userIds
  [:fn [UserMap] [:xt/array UserId]])

(defn.xt ^{:public true}
  userIds
  [users]
  (return (x:obj-keys users)))

(defspec.xt listUsers
  [:fn [UserMap] [:xt/array User]])

(defn.xt ^{:public true}
  listUsers
  [users]
  (return (x:arr-map (-/userIds users)
                     (fn [id]
                       (return (x:get-key users id))))))

(defspec.xt SAMPLE_USERS
  UserMap)

(def.xt ^{:public true}
  SAMPLE_USERS
  {"u-001" {"id" "u-001" "displayName" "Ari Chen" "roles" ["admin" "engineering"]}
   "u-002" {"id" "u-002" "displayName" "Bea Martin" "roles" ["editor"]}
   "u-003" {"id" "u-003" "displayName" "Dara Kim" "roles" ["support" "editor"]}
   "u-004" {"id" "u-004" "displayName" "Eli Navarro" "roles" ["engineering"]}
   "u-005" {"id" "u-005" "displayName" "Morgan Lee" "roles" ["support" "admin"]}})

(defspec.xt DEFAULT_PAGE_SIZE
  :xt/int)

(def.xt ^{:public true}
  DEFAULT_PAGE_SIZE
  20)
