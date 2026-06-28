(ns lua.nginx.crypt-test
  (:require [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config  {:program :resty}
   :require [[xt.lang.spec-base :as xt :include [:json]]
             [lua.nginx :as n]
             [lua.nginx.crypt :as crypt]]})

(fact:global
 {:skip     (not (env/program-exists? "resty"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.crypt/crypt :added "4.0"}
(fact "same functionality as postgres crypt"

  (crypt/crypt "hello" "$1$qI5PyQbL")
  => "$1$qI5PyQbL$CGhOca3eF1M4DEWbsndfv0")

^{:refer lua.nginx.crypt/gen-salt :added "4.0"}
(fact "generates salt compatible with pgcrypto libraries"

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
