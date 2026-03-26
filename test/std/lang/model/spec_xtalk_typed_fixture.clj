(ns std.lang.model.spec-xtalk-typed-fixture
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defspec.xt User
  [:record
   [id :xt/str]
   [name :xt/str]])

(defspec.xt UserMap
  [:dict :xt/str User])

(defspec.xt find-user
  [:fn [UserMap :xt/str] [:maybe User]])

(defn.xt find-user
  [users id]
  (return (k/get-key users id)))

(defn.xt ^{:- [User]}
  wrong-user-name
  [user]
  (return "not-a-user"))

(defn.xt ^{:- [[:maybe User]]}
  find-user-wrong-key
  [UserMap users
   :xt/int id]
  (return (-/find-user users id)))
