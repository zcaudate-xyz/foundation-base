(ns lua.nginx.openssl-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.spec-base :as xt :include [:json]]
             [lua.nginx :as n]
             [lua.nginx.openssl :as ssl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.openssl/hmac :added "4.0"}
(fact "creates an encrypted hmac string"
  
  (!.lua
   (n/encode-base64 (ssl/hmac "HELLO"
                              "HELLO")))
  => "WF4qn8J+qzxv9/39ISakTMTNSBc=")
