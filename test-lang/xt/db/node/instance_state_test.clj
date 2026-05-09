(ns xt.db.node.instance-state-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
  :require [[xt.db.node.instance-state :as instance-state]
              [xt.db.node.schema-state :as schema-state]
              [xt.event.base-view :as event-view]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

(def +schema+
  {"Order"
   {"id" {"ident" "id", "type" "text", "order" 0}
    "status" {"ident" "status", "type" "text", "order" 1}}})

(def +lookup+
  {"Order" {"position" 0}})

(def +model-spec+
  {"views"
   {"main"
    {"default_input" ["ord-1"]
      "query" {:table "Order"
               :return-method "default"
               :return-id "ord-1"}}}})

(def +dependent-model-spec+
  {"views"
   {"summary"
    {"deps" [["orders" "main"]]}}})

^{:refer xt.db.node.instance-state/ensure-state :added "4.1"}
(fact "attaches node state to a space when missing"

  (!.js
    (var space {})
    (var node {"meta" {"xt.db" {"schema" (@! +schema+)
                                "lookup" (@! +lookup+)}}})
    (var state (instance-state/ensure-state space node))
    [(. state ["::"])
     (. (. space ["state"]) ["::"])])
  => ["xt.db.state" "xt.db.state"])

^{:refer xt.db.node.instance-state/ensure-db :added "4.1"}
(fact "creates the local cache db on demand"

  (!.js
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "lookup" (@! +lookup+)}))
    (. (instance-state/ensure-db state) ["::"]))
  => "db.cache")

^{:refer xt.db.node.instance-state/put-model :added "4.1"}
(fact "stores a model and normalizes its views"

  (!.js
    (var state (schema-state/base-state {}))
    (var model (instance-state/put-model state "orders" (@! +model-spec+)))
    [(. model ["id"])
     (xtd/get-in state ["models" "orders" "views" "main" "::"])
     (xt/x:get-path
      (event-view/get-input
       (xtd/get-in state ["models" "orders" "views" "main"]))
      ["current" "data"])
      (xtd/get-in state ["models" "orders" "views" "main" "status"])
      (xtd/get-in state ["models" "orders" "deps"])
      (xtd/get-in state ["models" "orders" "unknown_deps"])])
   => ["orders"
       "event.view"
       ["ord-1"]
       "idle"
       {}
       []])

^{:refer xt.db.node.instance-state/put-view :added "4.1"}
(fact "stores a single view on an existing model"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" {"views" {}})
    (var view
     (instance-state/put-view
          state
          "orders"
          "secondary"
          {"default_input" ["ord-2"]}))
     [(. view ["id"])
      (. view ["::"])
      (xt/x:get-path (event-view/get-input view)
                     ["current" "data"])])
  => ["secondary"
      "event.view"
      ["ord-2"]])

^{:refer xt.db.node.instance-state/set-view-input :added "4.1"}
(fact "updates a view input"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (xt/x:get-path
     (event-view/get-input
      (instance-state/set-view-input state "orders" "main" ["ord-2"]))
     ["current" "data"]))
  => ["ord-2"])

^{:refer xt.db.node.instance-state/set-view-pending :added "4.1"}
(fact "marks a view as pending"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view (instance-state/set-view-pending state "orders" "main"))
    [(. view ["pending"])
     (. view ["status"])
     (. view ["error"])])
  => [true
      "pending"
      nil])

^{:refer xt.db.node.instance-state/set-view-success :added "4.1"}
(fact "stores a successful view value"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-success
          state
          "orders"
          "main"
           "q1"
           [{"status" "open"}]
           {"Order" true}))
    [(. view ["status"])
     (. view ["query_key"])
     (. view ["value"])
     (. view ["tables"])
     (. (. (. state ["view_watch"]) ["Order"] ["orders"]) ["main"])
     (xt/x:is-number? (. view ["updated_at"]))])
   => ["ready"
       "q1"
       [{"status" "open"}]
       {"Order" true}
       true
       true])

^{:refer xt.db.node.instance-state/set-view-error :added "4.1"}
(fact "stores a view error"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-error
          state
          "orders"
          "main"
          {"status" "error"
           "tag" "boom"}))
    [(. view ["status"])
     (. view ["error"] ["tag"])
     (. view ["pending"])])
  => ["error"
      "boom"
      false])

^{:refer xt.db.node.instance-state/set-view-stale :added "4.1"}
(fact "marks a view as stale"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-stale
          state
          "orders"
          "main"
          "changed"))
    [(. view ["status"])
     (. view ["error"])
     (. view ["pending"])])
  => ["stale"
      "changed"
      false])

^{:refer xt.db.node.instance-state/remove-query-watch :added "4.1"}
(fact "removes watched query ids from selected tables"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true "Audit" true})
    (instance-state/remove-query-watch state "q1" {"Order" true})
    [(. (. state ["watch"]) ["Order"])
     (xt/x:obj-keys (. (. state ["watch"]) ["Audit"]))])
  => [nil
      ["q1"]])

^{:refer xt.db.node.instance-state/remove-view-watch :added "4.1"}
(fact "removes watched view ids from selected tables"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-view state "orders" "main" {"Order" true "Audit" true})
    (instance-state/remove-view-watch state "orders" "main" {"Order" true})
    [(. (. state ["view_watch"]) ["Order"])
     (xt/x:obj-keys (. (. state ["view_watch"]) ["Audit"]))])
  => [nil
      ["orders"]])

^{:refer xt.db.node.instance-state/watch-query :added "4.1"}
(fact "indexes watched queries by table"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true "Audit" true})
    [(xt/x:obj-keys (. state ["watch"]))
     (. (. (. state ["watch"]) ["Order"]) ["q1"])])
  => [["Order" "Audit"]
      true])

^{:refer xt.db.node.instance-state/watch-view :added "4.1"}
(fact "indexes watched views by table and model"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-view state "orders" "main" {"Order" true "Audit" true})
    [(xt/x:obj-keys (. state ["view_watch"]))
     (. (. (. state ["view_watch"]) ["Order"] ["orders"]) ["main"])])
  => [["Order" "Audit"]
      true])

^{:refer xt.db.node.instance-state/affected-query-ids :added "4.1"}
(fact "collects affected query ids from watched tables"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true})
    (instance-state/watch-query state "q2" {"Audit" true})
    (instance-state/watch-query state "q3" {"Order" true "Audit" true})
    [(xtd/arr-lookup (instance-state/affected-query-ids state ["Order"]))
     (xtd/arr-lookup (instance-state/affected-query-ids state {"Audit" true}))])
  => [{"q1" true "q3" true}
      {"q2" true "q3" true}])

^{:refer xt.db.node.instance-state/affected-view-bindings :added "4.1"}
(fact "collects affected bound views from watched tables"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-view state "orders" "main" {"Order" true})
    (instance-state/watch-view state "stats" "summary" {"Audit" true})
    (instance-state/watch-view state "stats" "totals" {"Order" true "Audit" true})
    [(instance-state/affected-view-bindings state ["Order"])
     (instance-state/affected-view-bindings state {"Audit" true})])
  => [{"orders" {"main" true}
       "stats" {"totals" true}}
      {"stats" {"summary" true
                "totals" true}}])

^{:refer xt.db.node.instance-state/remove-query :added "4.1"}
(fact "removes cached queries and their watch entries"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true})
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1"
                                               "tables" {"Order" true}})
    (var prev (instance-state/remove-query state "q1"))
    [(. prev ["key"])
     (xt/x:obj-keys (. state ["queries"]))
     (. (. state ["watch"]) ["Order"])])
   => ["q1"
       []
       nil])

^{:refer xt.db.node.instance-state/get-view-dependents :added "4.1"}
(fact "indexes dependent views across models"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (instance-state/put-model state "stats" (@! +dependent-model-spec+))
    [(instance-state/get-view-dependents state "orders" "main")
     (instance-state/get-model-dependents state "orders")
     (xtd/get-in state ["models" "stats" "unknown_deps"])])
  => [{"stats" ["summary"]}
      {"stats" true}
      []])

^{:refer xt.db.node.instance-state/rebuild-model-deps :added "4.1"}
(fact "rebuilds unknown deps when source models appear later"

  (!.js
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "stats" (@! +dependent-model-spec+))
    (var before (xtd/get-in state ["models" "stats" "unknown_deps"]))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var after (xtd/get-in state ["models" "stats" "unknown_deps"]))
    [before after])
  => [[["orders" "main"]]
      []])


^{:refer xt.db.node.instance-state/get-model-dependents :added "4.1"}
(fact "collects dependent models for a source model"

  (!.js
   (var state (schema-state/base-state {}))
   (instance-state/put-model state "orders" (@! +model-spec+))
   (instance-state/put-model state "stats" (@! +dependent-model-spec+))
   (instance-state/get-model-dependents state "orders"))
  => {"stats" true})

^{:refer xt.db.node.instance-state/sync-view-state :added "4.1"}
(fact "mirrors base-view output fields onto compatibility keys"

  (!.js
   (var view {"output" {"current" {"id" "ord-1"}
                        "pending" true
                        "updated" 123}})
   (instance-state/sync-view-state view "pending" {"tag" "waiting"})
   [(. (. view ["value"]) ["id"])
    (. view ["pending"])
    (. view ["status"])
    (. (. view ["error"]) ["tag"])
    (. view ["updated_at"])])
  => ["ord-1"
      true
      "pending"
      "waiting"
      123])

^{:refer xt.db.node.instance-state/clear-view-errored :added "4.1"}
(fact "removes the errored marker from view output"

  (!.js
   (var view {"output" {"errored" true
                        "current" {"id" "ord-1"}}})
   (instance-state/clear-view-errored view)
   [(xt/x:has-key? (. view ["output"]) "errored")
    (. (. (. view ["output"]) ["current"]) ["id"])])
  => [false
      "ord-1"])
