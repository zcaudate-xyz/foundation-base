(ns xtbench.ruby.protocol.type-request-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.protocol.type-request :as reqp]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.type-request/ITypeRequest :added "4.1"}
(fact "defines the request protocol surface"

  (!.rb
    [reqp/ITypeRequest
     reqp/ITypeRuntimeRequest])
  => [["request"
       "receive_request"
       "receive_response"
       "respond_ok"
       "respond_error"]
      ["request"
       "receive_request"
       "receive_response"
       "respond_ok"
       "respond_error"]])
