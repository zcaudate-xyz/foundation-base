(ns xt.ui.state.session-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.session :as session]]})

^{:refer xt.ui.state.session/project :added "4.1"}
(fact "projects access state without leaking session credentials or routing fields"
  (!.js
   (var empty (session/project nil nil nil))
   (var current (session/project {"authenticated" true "user_id" "u-1" "token" "private" "route" "/admin"}
                                {"handle" "ada"} {"admin" true}))
   [(. empty ["authenticated"]) (xt/x:nil? (. empty ["user_id"]))
    (. empty ["profile"]) (. empty ["capabilities"]) current])
  => [false true {} {} {"authenticated" true "user_id" "u-1" "profile" {"handle" "ada"} "capabilities" {"admin" true}}])

^{:refer xt.ui.state.session/capable? :added "4.1"}
(fact "capability checks distinguish granted, denied and absent capabilities"
  (!.js
   (var current (session/project {} {} {"read" true "write" false "other" "yes"}))
   [(session/capable? current "read") (session/capable? current "write")
    (session/capable? current "other") (session/capable? current "missing")])
  => [true false false false])
