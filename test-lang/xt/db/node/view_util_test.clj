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
