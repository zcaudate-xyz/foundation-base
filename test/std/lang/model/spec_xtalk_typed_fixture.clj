(ns std.lang.model.spec-xtalk-typed-fixture
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-lib :as k]]})

(defspec.xt User
  [:xt/record
   ["id" :xt/str]
   ["name" :xt/str]])

(defspec.xt UserMap
  [:xt/dict :xt/str User])

(defspec.xt find-user
  [:fn [UserMap :xt/str] [:xt/maybe User]])

(defn.xt find-user
  [users id]
  (return (xt/x:get-key users id)))

(defn.xt ^{:- [User]}
  wrong-user-name
  [user]
  (return "not-a-user"))

(defn.xt ^{:- [[:xt/maybe User]]}
  find-user-wrong-key
  [UserMap users
   :xt/int id]
  (return (-/find-user users id)))
