(ns js.cell.service-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service :as service]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.cell.service/service? :added "4.1"}
(fact "checks whether a value is a service registry"
  ^:hidden

  (!.js
   [(service/service? {"dbs" {}})
    (service/service? {})])
  => [true false])

^{:refer js.cell.service/create-service :added "4.1"}
(fact "creates a service registry"
  ^:hidden

  (!.js
   (service/create-service
    {"local-cache" {"kind" "cache"}}))
  => {"dbs" {"local-cache" {"kind" "cache"}}})

^{:refer js.cell.service/get-dbs :added "4.1"}
(fact "gets all registered dbs"
  ^:hidden

  (!.js
   (service/get-dbs
    {"dbs" {"local-cache" {"kind" "cache"}}}))
  => {"local-cache" {"kind" "cache"}})

^{:refer js.cell.service/get-db :added "4.1"}
(fact "gets a named db from the registry"
  ^:hidden

  (!.js
   (service/get-db
    {"dbs" {"local-cache" {"kind" "cache"}}}
    "local-cache"))
  => {"kind" "cache"})

^{:refer js.cell.service/assoc-db :added "4.1"}
(fact "associates a db into the registry"
  ^:hidden

  (!.js
   (service/assoc-db
    {"dbs" {"local-cache" {"kind" "cache"}}}
    "server-rpc"
    {"kind" "remote"}))
  => {"dbs" {"local-cache" {"kind" "cache"}
             "server-rpc" {"kind" "remote"}}})

^{:refer js.cell.service/resolve-db :added "4.1"}
(fact "resolves db references from descriptors and context"
  ^:hidden

  (!.js
   [(service/resolve-db
     {"dbs" {"local-cache" {"kind" "cache"}}}
     {"db" "local-cache"}
     {})
    (service/resolve-db
     {"dbs" {"local-cache" {"kind" "cache"}}}
     {"db" {"kind" "inline"}}
     {})
    (service/resolve-db
     {"dbs" {"local-cache" {"kind" "cache"}}}
     {}
     {"db" "local-cache"})])
  => [{"kind" "cache"}
      {"kind" "inline"}
      {"kind" "cache"}])
