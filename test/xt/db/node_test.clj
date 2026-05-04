(ns xt.db.node-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.node.query]
            [xt.db.node.state]
            [xt.db.node.sync]
            [xt.event.node]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.query :as db-query]
             [xt.db.node.state :as db-state]
             [xt.db.node.sync :as db-sync]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.state/ensure-state :added "4.1"}
(fact "creates node cache state for a space"

  (!.js
   (do
     (var node (event-node/node-create {"id" "local"}))
     (db-state/set-node-opts node {"db" {"::" "db.cache"}})
     (var space (event-node/ensure-space node "$node" nil))
     (db-state/ensure-state space node)))
  => (contains {"::" "xt.db.node.state"}))

^{:refer xt.db.node.query/run-local-query :added "4.1"}
(fact "syncs rows into the cache, runs a local query, and clears cached watches"

  (!.js
   (do
     (var sorted-keys
          (fn [m]
            (return
             (xt/x:arr-sort (xt/x:obj-keys (or m {}))
                            (fn [x] (return x))
                            xt/x:str-lt))))
     (var sorted-ids
          (fn [rows]
            (var ids [])
            (xt/for:array [row (or rows [])]
              (xt/x:arr-push ids (xt/x:get-key row "id")))
            (return
             (xt/x:arr-sort ids
                            (fn [x] (return x))
                            xt/x:str-lt))))
     (var schema
          (xt/x:json-decode "{\"Currency\":{\"id\":{\"type\":\"text\",\"cardinality\":\"one\",\"primary\":true,\"scope\":\"id\",\"order\":0,\"ident\":\"id\"},\"type\":{\"type\":\"text\",\"cardinality\":\"one\",\"required\":true,\"scope\":\"data\",\"order\":1,\"ident\":\"type\"}}}"))
     (var lookup
          (xt/x:json-decode "{\"Currency\":{\"schema-primary\":{\"type\":\"text\",\"id\":\"id\"},\"position\":9}}"))
     (var views
          (xt/x:json-decode "{\"Currency\":{\"select\":{\"all_fiat\":{\"input\":[],\"return\":\"jsonb\",\"view\":{\"table\":\"Currency\",\"type\":\"select\",\"tag\":\"all_fiat\",\"access\":{\"roles\":{}},\"guards\":[],\"query\":{\"type\":\"fiat\"}}}},\"return\":{\"default\":{\"input\":[{\"symbol\":\"i_currency_id\",\"type\":\"text\"}],\"return\":\"jsonb\",\"view\":{\"table\":\"Currency\",\"type\":\"return\",\"tag\":\"default\",\"access\":{\"roles\":{}},\"guards\":[],\"query\":[\"id\"]}}}}}"))
     (var seed
          (xt/x:json-decode "[{\"id\":\"STATS\",\"type\":\"digital\"},{\"id\":\"XLM\",\"type\":\"crypto\"},{\"id\":\"XLM.T\",\"type\":\"crypto\"},{\"id\":\"USD\",\"type\":\"fiat\"}]"))
     (var opts {"db" {"::" "db.cache"}})
     (xt/x:set-key opts "schema" schema)
     (xt/x:set-key opts "lookup" lookup)
     (xt/x:set-key opts "views" views)
     (var node (event-node/node-create {"id" "cache"}))
     (db-state/set-node-opts node opts)
     (var space (event-node/ensure-space node "$node" nil))
     (var state (db-state/ensure-state space node))
     (db-state/ensure-db state)
     (db-sync/run-sync-local state {"sync" {"Currency" seed}} {})
     (var [_ result] (db-query/run-local-query state
                                               {"table" "Currency"
                                                "select-method" "all_fiat"
                                                "return-method" "default"}
                                               {:args []}
                                               nil
                                               nil))
     (var before-queries (xt/x:len (xt/x:obj-keys (xt/x:get-key state "queries"))))
     (db-sync/clear-state-cache state)
     {"query-key?" (xt/x:not-nil? (xt/x:get-key result "query_key"))
      "queries-before" before-queries
      "queries-after" (xt/x:len (xt/x:obj-keys (xt/x:get-key state "queries")))}))
  => {"query-key?" true
      "queries-before" 1
      "queries-after" 0})
