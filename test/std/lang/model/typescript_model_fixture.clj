(ns std.lang.model.typescript-model-fixture
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk)

;; Data/type declarations live in defspec.xt.
(defspec.xt UserId
  :xt/str)

(defspec.xt LookupKey
  [:or UserId :xt/int])

(defspec.xt UserRow
  [:tuple UserId :xt/str])

(defspec.xt User
  [:xt/record
   ["id" UserId]
   ["display_name" [:xt/maybe :xt/str]]
   ["roles" [:xt/array :xt/str]]
   ["meta" [:xt/dict :xt/str :xt/str]]])

(defspec.xt UserMap
  [:xt/dict UserId User])

(defspec.xt SearchResult
  [:xt/record
   ["user" [:xt/maybe User]]
   ["next_cursor" [:xt/maybe :xt/str]]])

(defn.xt ^{:- [[:xt/maybe User]]}
  lookup-user
  [UserMap users
   UserId id]
  (return (x:get-key users id)))

(def.xt ^{:- [:xt/int]}
  default-page-size
  20)
