(ns xt.db.node.view-util-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.view-util :as util]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.view-util/node-opts :added "4.1"}
(fact "gets xt.db options from node metadata"

  (!.js
    (util/node-opts {"meta" {"xt.db" {"auto_refresh" false
                                      "sources" {"primary" {"kind" "postgres"}}}}}))
  => {"auto_refresh" false
      "sources" {"primary" {"kind" "postgres"}}})

^{:refer xt.db.node.view-util/set-node-opts :added "4.1"}
(fact "stores xt.db options on node metadata"

  (!.js
    (var node {"meta" {}})
    (util/set-node-opts node {"auto_refresh" false})
    (xtd/get-in node ["meta" "xt.db"]))
  => {"auto_refresh" false})


^{:refer xt.db.node.view-util/state? :added "4.1"}
(fact "checks for xt.db state records"

  (!.js
    [(util/state? {"::" "xt.db.state"})
     (util/state? {"::" "other.state"})
     (util/state? nil)])
  => [true false false])

^{:refer xt.db.node.view-util/request-payload :added "4.1"}
(fact "returns the first request payload or an empty map"

  (!.js
    [(util/request-payload [{"action" "query"} {"action" "other"}])
     (util/request-payload [])
     (util/request-payload nil)])
  => [{"action" "query"} {} {}])

^{:refer xt.db.node.view-util/response-value :added "4.1"}
(fact "extracts response values when wrapped"

  (!.js
    [(util/response-value {"value" {"rows" [1 2]}})
     (util/response-value {"status" "ok"})
     (util/response-value nil)])
  => [{"rows" [1 2]} {"status" "ok"} nil])