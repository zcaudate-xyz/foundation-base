(ns xt.protocol.net-http-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]
             [xt.lang.common-protocol :as proto]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]
             [xt.lang.common-protocol :as proto]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]
             [xt.lang.common-protocol :as proto]]})


^{:refer xt.protocol.net-http/IFetchClient}
(fact "figuring out what ifetch client does"

  (!.js
    (proto/proto))


  (!.js
    (proto/iface-combine [["connect" "disconnect"]
                          ["disconnect" "exec"]
                          ["connect" "subscribe"]]))

  (!.js
    
    (proto/proto-spec
     [[["connect"] {"connect" (fn [])}]
      [["disconnect" "exec"]
       {"disconnect" (fn []), "exec" (fn [])}]])))

