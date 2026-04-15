(ns xt.cell.service.db-remote-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-remote :as db-remote]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-remote/remote-capable? :added "4.1"}
(fact "checks whether a db descriptor supports remote dispatch"
  ^:hidden

  (!.js
   [(db-remote/remote-capable? {"dispatch" (fn:> [request ctx] [true request])})
    (db-remote/remote-capable? {})])
  => [true false])

^{:refer xt.cell.service.db-remote/normalize-remote :added "4.1"}
(fact "merges remote settings from remote spec, db, and view context"
  ^:hidden

  (!.js
   (db-remote/normalize-remote
    {"target" "db-target"
     "dispatch" "db-dispatch"}
    {"decode" "remote-decode"}
    {"target" "view-target"
     "map_error" "view-map"}))
  => {"target" "db-target"
      "dispatch" "db-dispatch"
      "decode" "remote-decode"
      "map_error" "view-map"})

^{:refer xt.cell.service.db-remote/build-request :added "4.1"}
(fact "builds a remote request envelope with view context metadata"
  ^:hidden

  (!.js
   (db-remote/build-request
    {"target" "db-target"}
    {"op" "query"}
    ["select"]
    {"model-id" "orders"
     "view-id" "list"}))
  => {"target" "db-target"
      "op" "query"
      "body" ["select"]
      "dispatch" nil
      "decode" nil
      "map_error" nil
      "view_id" "list"
      "model_id" "orders"})

^{:refer xt.cell.service.db-remote/dispatch-request :added "4.1"}
(fact "dispatches requests through the resolved dispatch function"
  ^:hidden

  (!.js
   [(db-remote/dispatch-request
     {}
     {"dispatch" (fn [request ctx]
                   (return [true {"op" request.op
                                  "model" ctx.model-id}]))}
     {"model-id" "orders"})
    (db-remote/dispatch-request {} {} {})])
  => [[true {"op" nil
             "model" "orders"}]
      [false
       {"status" "error"
        "tag" "db/remote-dispatch-not-provided"}]])

^{:refer xt.cell.service.db-remote/decode-response :added "4.1"}
(fact "applies an optional decode function to remote responses"
  ^:hidden

  (!.js
   (db-remote/decode-response
    {}
    {"decode" (fn [response ctx]
                (return {"decoded" response.body
                         "view" ctx.view-id}))}
    {"body" {"rows" [1 2 3]}}
    {"view-id" "list"}))
  => [true
      {"decoded" {"rows" [1 2 3]}
       "view" "list"}])

^{:refer xt.cell.service.db-remote/map-remote-error :added "4.1"}
(fact "maps remote errors into the local contract"
  ^:hidden

  (!.js
   [(db-remote/map-remote-error {} {"message" "boom"} {})
    (db-remote/map-remote-error
     {"map_error" (fn [error ctx]
                    (return {"status" "error"
                             "tag" "custom/remote"
                             "data" {"view" ctx.view-id
                                     "error" error}}))}
     {"message" "boom"}
     {"view-id" "list"})])
  => [{"status" "error"
       "tag" "db/remote-request-failed"
       "data" {"message" "boom"}}
      {"status" "error"
       "tag" "custom/remote"
       "data" {"view" "list"
               "error" {"message" "boom"}}}])

^{:refer xt.cell.service.db-remote/run-remote-query :added "4.1"}
(fact "returns query preparation errors before dispatch"
  ^:hidden

  (!.js
   (db-remote/run-remote-query
    {"schema" {}
     "views" {"Order" {"select" {} "return" {}}}}
    {}
    {"table" "Order"
     "select-method" "missing"}
    {}))
  => [false
      {"status" "error"
       "tag" "net/select-method-not-found"
       "data" {"input" "missing"}}])

^{:refer xt.cell.service.db-remote/run-remote-sync :added "4.1"}
(fact "returns sync preparation errors before dispatch"
  ^:hidden

  (!.js
   (db-remote/run-remote-sync
    {"schema" {}}
    {}
    {}
    {}))
  => [false
      {"status" "error"
       "tag" "db/sync-empty-request"}])
