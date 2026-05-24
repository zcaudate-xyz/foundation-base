(ns xt.db.node.view-state-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.view-state :as state]
             [xt.db.node.schema-spec :as spec]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.view-state/base-state :added "4.1"}
(fact "creates the base view state with primary and caching sources"

  (!.js
    (var out
         (state/base-state {"schema" {"Task" {}}
                            "sources" {"primary" {"kind" "postgres"}
                                       "caching" {"kind" "sqlite"}}}))
    [(. out ["::"])
     (. (. out ["sources"]) ["primary"] ["kind"])
     (. (. out ["sources"]) ["caching"] ["kind"])
     (xt/x:obj-keys (. out ["models"]))])
  => ["xt.db.state"
      "postgres"
      "sqlite"
      []])

^{:refer xt.db.node.view-state/put-model :added "4.1"}
(fact "normalizes model sources once and lets views declare a stable source role"

  (!.js
    (var out (state/base-state {}))
    (state/put-model out
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}}
                      "views" {"list" {"query" {"table" "Task"}}
                               "detail" {"query" {"table" "Task"}
                                         "default_input" ["alpha"]
                                         "source" "primary"}}})
    [(xt/x:obj-keys (. (. out ["models"]) ["entries"] ["sources"]))
     (xtd/get-in out ["models" "entries" "views" "detail" "input"])
     (xtd/get-in out ["models" "entries" "views" "detail" "source"])])
  => [["primary" "caching"]
      ["alpha"]
      "primary"])

^{:refer xt.db.node.view-state/sync-source :added "4.1"}
(fact "syncs caching from primary through the stable model source binding"

  (!.js
    (var out (state/base-state {}))
    (state/put-model out
                     "entries"
                     {"views" {"list" {"query" {"table" "Task"}}}})
    (state/set-source-data out
                           "entries"
                           "primary"
                           [{"id" "t1" "name" "alpha"}])
    (state/sync-source out "entries" "caching")
    [(xtd/get-in out ["models" "entries" "sources" "primary" "data" 0 "name"])
     (xtd/get-in out ["models" "entries" "sources" "caching" "data" 0 "name"])
     (xt/x:is-number? (xtd/get-in out ["models" "entries" "sources" "caching" "updated_at"]))])
  => ["alpha" "alpha" true])
