(ns xtbench.dart.lang.common-protocol-test
  (:use code.test)
  (:require [hara.lang :as l]))

(do (l/script- :xtalk
      {:require [[xt.lang.common-protocol :as proto]
                 [xt.lang.spec-base :as xt]
                 [xt.lang.spec-promise :as promise]]}))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/protocol-method :added "4.1"}
(fact "looks up the registered method by protocol and implementation type"

  (!.dt
   (do
     (xt/x:set-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHello"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/Hello"
                             {"hello_str" "hello-str-fn"}}})
     (proto/protocol-method {"::" "xt.lang.common_protocol_test/Hello"}
                            "xt.lang.common_protocol_test/IHello"
                            "hello_str")))
  => "hello-str-fn")

^{:refer xt.lang.common-protocol/register-protocol-impl :added "4.1"}
(fact "registers protocol implementations in the registry"

  (!.dt
   (do
     (xt/x:set-key proto/PROTOCOLS "xt.lang.common_protocol_test/IHello"
                   {"::" "type/protocol"
                    "impls" {}})
     (proto/register-protocol-impl "xt.lang.common_protocol_test/IHello"
                                   "xt.lang.common_protocol_test/Hello"
                                   {"hello_prn" "hello-prn-fn"
                                    "hello_str" "hello-str-fn"})
     (xt/x:get-key (xt/x:get-key (xt/x:get-key proto/PROTOCOLS "xt.lang.common_protocol_test/IHello")
                                 "impls")
                   "xt.lang.common_protocol_test/Hello")))
  => {"hello_prn" "hello-prn-fn"
      "hello_str" "hello-str-fn"})

^{:refer xt.lang.common-protocol/create-protocol-fn :added "4.1"}
(fact "creates a runtime protocol descriptor"

  (!.dt
   (proto/create-protocol-fn "xt.lang.common_protocol_test/IHello"
                             {"hello_prn" {"name" "hello_prn"
                                           "arglist" ["impl"]}}))
  => {"::" "type/protocol"
      "on" "xt.lang.common_protocol_test/IHello"
      "sigs" {"hello_prn" {"name" "hello_prn"
                           "arglist" ["impl"]}}
      "impls" {}})

^{:refer xt.lang.common-protocol/defprotocol.xt :added "4.1"}
(fact "expands to a protocol value and method wrappers"

  (!.dt
   (xt/x:is-function? -/hello-str))
  => true)

(comment
  (s/snapto '[xt.lang.common-protocol])
  
  (s/seedgen-langadd '[xt.lang.common-protocol] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-protocol] {:lang [:lua :python] :write true}))

^{:refer xt.lang.common-protocol/raw-method :added "4.1"}
(fact "looks up a raw method fn from the protocol registry"

  (!.dt
   (do
     (xt/x:set-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHelloRaw"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/HelloRaw"
                             {"hello_str" "hello-str-fn"}}})
     (proto/raw-method "xt.lang.common_protocol_test/IHelloRaw"
                       "xt.lang.common_protocol_test/HelloRaw"
                       "hello_str")))
  => "hello-str-fn")

^{:refer xt.lang.common-protocol/protocol-exists :added "4.1"}
(fact "checks if a type implementation has been registered"

  (!.dt
   (do
     (xt/x:set-key proto/IMPLEMENTATIONS
                   "xt.lang.common_protocol_test/HelloExists"
                   true)
     (proto/protocol-exists "xt.lang.common_protocol_test/HelloExists")))
  => true)

^{:refer xt.lang.common-protocol/register-protocol :added "4.1"}
(fact "registers a protocol descriptor in the global registry"

  (!.dt
   (do
     (proto/register-protocol {"::" "type/protocol"
                               "on" "xt.lang.common_protocol_test/IHelloReg"
                               "sigs" {}
                               "impls" {}})
     (xt/x:get-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHelloReg")))
  => {"::" "type/protocol"
      "on" "xt.lang.common_protocol_test/IHelloReg"
      "sigs" {}
      "impls" {}})
