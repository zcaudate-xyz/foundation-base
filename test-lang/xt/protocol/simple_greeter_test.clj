(ns xt.protocol.simple-greeter-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.simple-greeter :as greeter]
             [xt.lang.common-protocol :as proto]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.simple-greeter :as greeter]
             [xt.lang.common-protocol :as proto]]})

^{:refer xt.protocol.simple-greeter/greet :added "4.1"}
(fact "TODO"

  (!.js
    (greeter/greet {"::" "simple.greeter<js>"
                    "greet" (fn []
                              (return
                               "hello"))}))
  => "hello"

  (!.py
    (greeter/greet {"::" "simple.greeter<python>"
                    "greet" (fn []
                              (return
                               "hello"))}))
  => "hello")
