(ns xt.db.system.impl-supabase-js
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :js
  {:require [[js.lib.supabase :as supabase]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.js response->body
  [response]
  (var #{data error} (or response {}))
  (cond (xt/x:not-nil? data)
        (return data)

        (xt/x:not-nil? error)
        (return error)

        :else
        (return response)))

(defn.js schema-client
  [client schema-name]
  (cond (xt/x:not-nil? schema-name)
        (return (. client (schema schema-name)))

        :else
        (return client)))

(defn.js pull-async
  "runs a tree ir pull using the official supabase-js query builder"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         lookup
         opts} impl)
  (var request (pgrest-graph/select schema tree opts))
  (var table-name (xt/x:first tree))
  (var schema-name (xt/x:get-path lookup [table-name "schema"]))
  (var base-client (-/schema-client client schema-name))
  (return
   (-> (. base-client
          (from table-name)
          (select (xt/x:get-key request "select")))
       (promise/x:promise-then
        (fn [response]
          (return (-/response->body response)))))))

(defn.js rpc-call-async
  [impl rpc-spec args opts]
  (var #{client} impl)
  (var input-spec (or (xt/x:get-key rpc-spec "input") []))
  (var body {})
  (var schema (xt/x:get-key rpc-spec "schema"))
  (:= opts (or opts {}))
  (xt/for:array [[i input] input-spec]
    (var key (or (xt/x:get-key input "symbol")
                 (xt/x:get-key input "name")
                 nil))
    (when (xt/x:not-nil? key)
      (xt/x:set-key body key (xt/x:get-idx args i))))
  (return
   (-> (. client
          (rpc (xt/x:get-key rpc-spec "id")
               body
               (xt/x:obj-assign opts
                                (:? (xt/x:not-nil? schema)
                                    {"schema" schema}))))
       (promise/x:promise-then
        (fn [response]
          (return (-/response->body response)))))))

(defimpl.xt ^{:lang :js}
  ImplSupabaseJs
  [client schema lookup opts]
  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async})

(defn.js impl-supabase-js
  [client schema lookup]
  (return
   (-/ImplSupabaseJs client schema lookup {})))
