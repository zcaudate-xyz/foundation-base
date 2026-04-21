(ns js.cell.service.db-remote-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.service.db-remote :as db-remote]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +db+
  {"schema"
   {"Order"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "status" {"ident" "status", "type" "text", "order" 1}
     "account" {"ident" "account",
                "type" "ref",
                "order" 2,
                "ref" {"ns" "Account",
                       "type" "forward",
                       "key" "account"}}}
    "Account"
    {"id" {"ident" "id", "type" "text", "order" 0}
     "nickname" {"ident" "nickname", "type" "text", "order" 1}}}
   "views"
   {"Order"
    {"select"
     {"by_account"
      {"input" [{"symbol" "i_account_id", "type" "text"}]
       "return" "jsonb"
       "view" {"table" "Order",
               "type" "select",
               "tag" "by_account",
               "access" {"roles" {}},
               "guards" [],
               "query" {"account" {"id" "{{i_account_id}}"}}}}}
     "return"
     {"default"
      {"input" [{"symbol" "i_order_id", "type" "text"}]
       "return" "jsonb"
       "view" {"table" "Order",
               "type" "return",
               "tag" "default",
               "access" {"roles" {}},
               "guards" [],
               "query" ["status"
                        ["account" ["nickname"]]]}}}}}})

^{:refer js.cell.service.db-remote/remote-capable? :added "4.1"}
(fact "checks whether a descriptor can dispatch remote requests"

  (!.js
    (var db (xtd/obj-assign (@! +db+)
                          {"dispatch" (fn [request _]
                                        (return [true request]))}))
   [(db-remote/remote-capable? db)
    (db-remote/remote-capable? (@! +db+))])
  => [true false])

^{:refer js.cell.service.db-remote/normalize-remote :added "4.1"}
(fact "normalizes remote dispatch helpers from db and remote spec"

  (!.js
    (var db (xtd/obj-assign (@! +db+)
                          {"target" "server-rpc"
                           "dispatch" (fn [request _]
                                        (return [true request]))}))
   (var remote (db-remote/normalize-remote
                db
                 {"decode" (fn [response _]
                            (return (xt/x:get-key response "body")))}
                 {}))
    [(xt/x:get-key remote "target")
     (xt/x:is-function? (xt/x:get-key remote "dispatch"))
     (xt/x:is-function? (xt/x:get-key remote "decode"))])
  => ["server-rpc" true true])

^{:refer js.cell.service.db-remote/build-request :added "4.1"}
(fact "builds a remote request envelope around a prepared operation"

  (!.js
    (var db (xtd/obj-assign (@! +db+)
                          {"dispatch" (fn [request _]
                                        (return [true request]))}))
   (var request (db-remote/build-request
                 db
                 {"target" "server-rpc"
                  "op" "query"}
                 ["Order" {"account" {"id" "acct-1"}} ["status"]]
                 {"view-id" "orders/list"
                  "model-id" "orders"}))
    [(xt/x:get-key request "target")
     (xt/x:get-key request "op")
     (xt/x:get-key request "body")
     (xt/x:get-key request "view_id")
     (xt/x:get-key request "model_id")])
  => ["server-rpc"
      "query"
      ["Order" {"account" {"id" "acct-1"}} ["status"]]
      "orders/list"
      "orders"])

^{:refer js.cell.service.db-remote/dispatch-request :added "4.1"}
(fact "dispatches a remote request through the configured dispatcher"

  (!.js
   (db-remote/dispatch-request
    (@! +db+)
    {"dispatch" (fn [request _]
                  (return [true {"status" "ok"
                                 "body" (xt/x:get-key request "body")}]))
     "body" {"hello" "world"}}
    {}))
  => [true {"status" "ok"
            "body" {"hello" "world"}}])

^{:refer js.cell.service.db-remote/decode-response :added "4.1"}
(fact "decodes remote responses when a decoder is provided"

  (!.js
   [(db-remote/decode-response
     (@! +db+)
      {"decode" (fn [response _]
                  (return (xt/x:get-key response "body")))}
     {"status" "ok"
      "body" {"value" 1}}
     {})
    (db-remote/decode-response
     (@! +db+)
     {}
     {"status" "ok"}
     {})])
  => [[true {"value" 1}]
      [true {"status" "ok"}]])

^{:refer js.cell.service.db-remote/map-remote-error :added "4.1"}
(fact "maps remote errors into the local error contract"

  (!.js
   [(db-remote/map-remote-error
     (@! +db+)
     {"status" "error"
      "tag" "remote/fail"}
     {})
    (db-remote/map-remote-error
     {"map_error" (fn [error _]
                    (return {"status" "error"
                             "tag" "mapped/error"
                             "data" error}))}
     {"status" "error"
      "tag" "remote/fail"}
     {})])
  => [{"status" "error"
       "tag" "db/remote-request-failed"
       "data" {"status" "error"
               "tag" "remote/fail"}}
      {"status" "error"
       "tag" "mapped/error"
       "data" {"status" "error"
               "tag" "remote/fail"}}])

^{:refer js.cell.service.db-remote/run-remote-query :added "4.1"}
(fact "prepares and dispatches a remote query"

  (!.js
    (var db (xtd/obj-assign (@! +db+)
                          {"dispatch" (fn [request _]
                                        (return [true {"status" "ok"
                                                       "body" (xt/x:get-key request "body")}]))}))
   (var [ok result] (db-remote/run-remote-query
                     db
                     {"target" "server-rpc"
                       "decode" (fn [response _]
                                  (return (xt/x:get-key response "body")))}
                     {:table "Order"
                      :select-method "by_account"
                      :return-method "default"}
                     {"args" ["acct-1"]
                      "view-id" "orders/list"
                      "model-id" "orders"}))
   [ok
     (xt/x:first result)
     (xtd/get-in result [1 "account" "id"])
     (xtd/last result)])
  => [true
      "Order"
      "acct-1"
      ["status"
       ["account" ["nickname"]]]])

^{:refer js.cell.service.db-remote/run-remote-sync :added "4.1"}
(fact "prepares and dispatches a remote sync request"

  (!.js
    (var db (xtd/obj-assign (@! +db+)
                          {"dispatch" (fn [request _]
                                        (return [true {"status" "ok"
                                                       "body" (xt/x:get-key request "body")}]))}))
   (db-remote/run-remote-sync
    db
    {"target" "server-rpc"
      "decode" (fn [response _]
                 (return (xt/x:get-key response "body")))}
    {:sync {"Order" [{"id" "ord-1"
                      "status" "open"}]}}
    {"model-id" "orders"}))
  => [true
      {"db/sync" {"Order" [{"id" "ord-1"
                            "status" "open"}]}}])
