(ns xt.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.net.http-fetch :as http]
             [xt.lang.common-protocol :as proto]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.net.http-fetch :as http]
             [xt.lang.common-protocol :as proto]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.net.http-fetch :as http]
             [xt.lang.common-protocol :as proto]]})



^{:refer xt.net.http-fetch/client-fetch :added "4.1"}
(fact "TODO")
