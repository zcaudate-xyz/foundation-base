(ns xt.lang.base-text-test
  (:require [std.lang :as l])
  (:use code.test))

(do 
  (l/script- :js
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-text :as text]]})
  
  (l/script- :lua
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-text :as text]]})
  
  (l/script- :python
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-text :as text]]}))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-text/tag-string :added "4.0"}
(fact "gets the string description for a given tag"
  ^:hidden
  
  (!.js
   (text/tag-string "user.account/login"))
  => "account login"

  (!.lua
   (text/tag-string "user.account/login"))
  => "account login")
