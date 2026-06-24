(ns xt.lang.common-protocol-test
  (:use code.test)
  (:require [hara.lang :as l]))

(do (l/script- :xtalk
      {:require [[xt.lang.common-protocol :as proto]
                 [xt.lang.spec-base :as xt]
                 [xt.lang.spec-promise :as promise]]}))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-protocol/protocol-method :added "4.1"}
(fact "looks up the registered method by protocol and implementation type"

  (!.js
   (do
     (xt/x:set-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHello"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/Hello"
                             {"hello_str" "hello-str-fn"}}})
     (proto/protocol-method {"::" "xt.lang.common_protocol_test/Hello"}
                            "xt.lang.common_protocol_test/IHello"
                            "hello_str")))
  => "hello-str-fn"

  (!.lua
   (do
     (xt/x:set-key proto/PROTOCOLS "xt.lang.common_protocol_test/IHello"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/Hello"
                             {"hello_str" "hello-str-fn"}}})
     (proto/protocol-method {"::" "xt.lang.common_protocol_test/Hello"}
                            "xt.lang.common_protocol_test/IHello"
                            "hello_str")))
  => "hello-str-fn"

  (!.py
   (do
     (xt/x:set-key proto/PROTOCOLS "xt.lang.common_protocol_test/IHello"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/Hello"
                             {"hello_str" "hello-str-fn"}}})
     (proto/protocol-method {"::" "xt.lang.common_protocol_test/Hello"}
                            "xt.lang.common_protocol_test/IHello"
                            "hello_str")))
  => "hello-str-fn")

^{:refer xt.lang.common-protocol/register-protocol-impl :added "4.1"}
(fact "registers protocol implementations in the registry"

  (!.js
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
      "hello_str" "hello-str-fn"}

  (!.py
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

  (!.js
   (proto/create-protocol-fn "xt.lang.common_protocol_test/IHello"
                             {"hello_prn" {"name" "hello_prn"
                                           "arglist" ["impl"]}}))
  => {"::" "type/protocol"
      "on" "xt.lang.common_protocol_test/IHello"
      "sigs" {"hello_prn" {"name" "hello_prn"
                           "arglist" ["impl"]}}
      "impls" {}})

^{:refer xt.lang.common-protocol/format-defprotocol-method-xt :added "4.1"}
(fact "formats a protocol method wrapper"

  (proto/format-defprotocol-method-xt "xt.lang.common_protocol_test/IHello"
                                      'hello-str
                                      '[impl])
  => '(defn.xt hello-str
        [impl]
        (var method-fn (xt.lang.common-protocol/protocol-method
                        impl
                        "xt.lang.common_protocol_test/IHello" "hello_str"))
        (return (method-fn impl))))

^{:refer xt.lang.common-protocol/format-defprotocol-xt :added "4.1"}
(fact "formats a defprotocol.xt form into a runtime protocol descriptor"

  (proto/format-defprotocol-xt 'IHello
                               '((hello-prn [impl])
                                 (hello-str [impl])))
  => '(xt.lang.common-protocol/create-protocol-fn
       "xt.lang.common_protocol_test/IHello"
       (tab ["hello_prn" {"name" "hello_prn",
                          "arglist" ["impl"]}]
            ["hello_str" {"name" "hello_str",
                          "arglist" ["impl"]}])) )

^{:refer xt.lang.common-protocol/defprotocol.xt :added "4.1"}
(fact "expands to a protocol value and method wrappers"

  (macroexpand-1
   '(proto/defprotocol.xt IHello
      (hello-str [impl])
      (hello-prn [impl])))
  => seq?

  (proto/defprotocol.xt IHello
    (hello-str [impl])
    (hello-prn [impl]))
  
  (!.js
   (xt/x:is-function? -/hello-str))
  => true)

^{:refer xt.lang.common-protocol/format-defimpl-xt :added "4.1"}
(fact "formats a defimpl.xt constructor and protocol registration"

  (proto/format-defimpl-xt 'Hello
                           '[state client schema lookup opts]
                           '[[xt.lang.common-protocol-test/IHello
                              {hello-prn -/hello-prn-fn
                               hello-str -/hello-str-fn}]])
  => '(defn.xt Hello
        [state client schema lookup opts]
        (when
            (not
             (xt.lang.spec-base/x:get-key
              xt.lang.common-protocol/IMPLEMENTATIONS
              "xt.lang.common_protocol_test/Hello"))
          (do
            (xt.lang.spec-base/x:set-key
             xt.lang.common-protocol/IMPLEMENTATIONS
             "xt.lang.common_protocol_test/Hello"
             true)
            (xt.lang.common-protocol/register-protocol-impl
             (xt.lang.spec-base/x:get-key
              xt.lang.common-protocol-test/IHello
              "on")
             "xt.lang.common_protocol_test/Hello"
             {"hello_prn" -/hello-prn-fn, "hello_str" -/hello-str-fn})))
        (return
         {"::" "xt.lang.common_protocol_test/Hello",
          "::/protocols" [(xt.lang.spec-base/x:get-key
                           xt.lang.common-protocol-test/IHello
                           "on")],
          "state" state,
          "client" client,
          "schema" schema,
          "lookup" lookup,
          "opts" opts})))

^{:refer xt.lang.common-protocol/defimpl.xt :added "4.1"}
(fact "expands to a constructor and protocol registrations"
  
  (defn.xt hello-str-fn
    [impl]
    (return (xt/x:cat "hello " (xt/x:get-key impl "state"))))
  
  (defn.xt hello-prn-fn
    [impl]
    (return (xt/x:cat "prn " (xt/x:get-key impl "state"))))
  
  (proto/defimpl.xt Hello
    [state client schema lookup opts]
    -/IHello
    {hello-prn -/hello-prn-fn
     hello-str -/hello-str-fn}
    
    -/IHello
    {hello-prn -/hello-prn-fn
     hello-str -/hello-str-fn})
  => #'xt.lang.common-protocol-test/Hello)

(comment
  (s/snapto '[xt.lang.common-protocol])
  
  (s/seedgen-langadd '[xt.lang.common-protocol] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-protocol] {:lang [:lua :python] :write true}))



^{:refer xt.lang.common-protocol/raw-method :added "4.1"}
(fact "looks up a raw method fn from the protocol registry"

  (!.js
   (do
     (xt/x:set-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHelloRaw"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/HelloRaw"
                             {"hello_str" "hello-str-fn"}}})
     (proto/raw-method "xt.lang.common_protocol_test/IHelloRaw"
                       "xt.lang.common_protocol_test/HelloRaw"
                       "hello_str")))
  => "hello-str-fn"

  (!.lua
   (do
     (xt/x:set-key proto/PROTOCOLS
                   "xt.lang.common_protocol_test/IHelloRaw"
                   {"::" "type/protocol"
                    "impls" {"xt.lang.common_protocol_test/HelloRaw"
                             {"hello_str" "hello-str-fn"}}})
     (proto/raw-method "xt.lang.common_protocol_test/IHelloRaw"
                       "xt.lang.common_protocol_test/HelloRaw"
                       "hello_str")))
  => "hello-str-fn"

  (!.py
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

  (!.js
   (do
     (xt/x:set-key proto/IMPLEMENTATIONS
                   "xt.lang.common_protocol_test/HelloExists"
                   true)
     (proto/protocol-exists "xt.lang.common_protocol_test/HelloExists")))
  => true

  (!.lua
   (do
     (xt/x:set-key proto/IMPLEMENTATIONS
                   "xt.lang.common_protocol_test/HelloExists"
                   true)
     (proto/protocol-exists "xt.lang.common_protocol_test/HelloExists")))
  => true

  (!.py
   (do
     (xt/x:set-key proto/IMPLEMENTATIONS
                   "xt.lang.common_protocol_test/HelloExists"
                   true)
     (proto/protocol-exists "xt.lang.common_protocol_test/HelloExists")))
  => true)

^{:refer xt.lang.common-protocol/format-defimpl-xt-symbol :added "4.1"}
(fact "returns the def form symbol for the implementation language"

  (proto/format-defimpl-xt-symbol 'Hello)
  => 'def.xt

  (proto/format-defimpl-xt-symbol (with-meta 'Hello {:lang :js}))
  => 'def.js

  (proto/format-defimpl-xt-symbol 'Hello "defn")
  => 'defn.xt)

^{:refer xt.lang.common-protocol/register-protocol :added "4.1"}
(fact "registers a protocol descriptor in the global registry"

  (!.js
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
      "impls" {}}

  (!.lua
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
      "impls" {}}

  (!.py
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