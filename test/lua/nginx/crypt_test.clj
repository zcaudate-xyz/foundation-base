(ns lua.nginx.crypt-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.base-lib :as k :include [:json]]
             [lua.nginx :as n]
             [lua.nginx.crypt :as crypt]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.crypt/hmac :added "4.0"}
(fact "same functionality as postgres crypt"
  ^:hidden

  (crypt/crypt "hello" "$1$qI5PyQbL")
  => "$1$qI5PyQbL$CGhOca3eF1M4DEWbsndfv0")

^{:refer lua.nginx.crypt/crypt :added "4.0"}
(fact "TODO")

^{:refer lua.nginx.crypt/gen-salt :added "4.0"}
(fact "generates salt compatible with pgcrypto libraries"
  ^:hidden
  
  (crypt/gen-salt "md5")
  => string?
  

  (crypt/gen-salt "bf")
 => string?

  ;;
  ;; PG COMPATIBLE
  ;;


  (crypt/crypt "HELLO"
               (crypt/gen-salt "md5"))
  => string?)