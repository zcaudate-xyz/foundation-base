(ns xt.protocol.type-request-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.type-request :as reqp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.type-request :as reqp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.type-request :as reqp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.type-request/ITypeRequest :added "4.1"}
(fact "defines the request protocol surface"

  (!.js
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
       "respond_error"]]

  (!.lua
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
       "respond_error"]]

  (!.py
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
