(ns xt.protocol.net-http-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.net-http :as http]]})


^{:refer xt.protocol.net-http/IFetchClient}
(fact "figuring out what ifetch client does")
(!.js
  )

