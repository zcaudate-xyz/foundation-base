(ns xtbench.python.db.node.instance-util-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.node.instance-util :as util]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.instance-util/node-opts :added "4.1"}
(fact "gets xt.db options from node metadata"

  (!.py
    (util/node-opts {"meta" {"xt.db" {"auto_refresh" false
                                      "space" "main"}}}))
  => {"auto_refresh" false
      "space" "main"})

^{:refer xt.db.node.instance-util/set-node-opts :added "4.1"}
(fact "stores xt.db options on node metadata"

  (!.py
    (var node {"meta" {}})
    (util/set-node-opts node {"auto_refresh" false})
    (xtd/get-in node ["meta" "xt.db"]))
  => {"auto_refresh" false})

^{:refer xt.db.node.instance-util/state? :added "4.1"}
(fact "checks for db.node state maps"

  (!.py
    [(util/state? {"::" "xt.db.state"})
     (util/state? {"::" "other"})
     (util/state? nil)])
  => [true false false])

^{:refer xt.db.node.instance-util/request-payload :added "4.1"}
(fact "gets the first request payload or an empty object"

  (!.py
    [(util/request-payload [{"id" "ord-1"} {"id" "ord-2"}])
     (util/request-payload [])])
  => [{"id" "ord-1"} {}])

^{:refer xt.db.node.instance-util/response-value :added "4.1"}
(fact "unwraps remote response values when present"

  (!.py
    [(util/response-value {"value" {"ok" true}})
     (util/response-value {"ok" true})])
  => [{"ok" true}
      {"ok" true}])
