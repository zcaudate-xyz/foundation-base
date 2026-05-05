(ns xt.db.node.instance-util-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-util :as util]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.instance-util/node-opts :added "4.1"}
(fact "gets xt.db options from node metadata"

  (!.js
    (util/node-opts {"meta" {"xt.db" {"auto_refresh" false
                                      "space" "main"}}}))
  => {"auto_refresh" false
      "space" "main"})

^{:refer xt.db.node.instance-util/set-node-opts :added "4.1"}
(fact "stores xt.db options on node metadata"

  (!.js
    (var node {"meta" {}})
    (util/set-node-opts node {"auto_refresh" false})
    (xtd/get-in node ["meta" "xt.db"]))
  => {"auto_refresh" false})

^{:refer xt.db.node.instance-util/state? :added "4.1"}
(fact "checks for db.node state maps"

  (!.js
    [(util/state? {"::" "xt.db.state"})
     (util/state? {"::" "other"})
     (util/state? nil)])
  => [true false false])

^{:refer xt.db.node.instance-util/request-payload :added "4.1"}
(fact "gets the first request payload or an empty object"

  (!.js
    [(util/request-payload [{"id" "ord-1"} {"id" "ord-2"}])
     (util/request-payload [])])
  => [{"id" "ord-1"} {}])

^{:refer xt.db.node.instance-util/response-value :added "4.1"}
(fact "unwraps remote response values when present"

  (!.js
    [(util/response-value {"value" {"ok" true}})
     (util/response-value {"ok" true})])
  => [{"ok" true}
      {"ok" true}])
