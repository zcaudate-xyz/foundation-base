(ns xt.db.system.impl-supabase-js
  (:require [lang.core :as l]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt impl-supabase-js
  "creates a JavaScript-only xt.db adapter from a native Supabase client"
  {:added "4.1.7"}
  [_client _schema _lookup]
  (xt/x:err "xt.db.system.impl-supabase-js is only available on the JavaScript target"))

(l/script :js
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [js.lib.supabase :as supabase]]})

(defn.js response-data
  "returns Supabase data or raises the native Supabase error"
  {:added "4.1.7"}
  [response]
  (var data (. response ["data"]))
  (var error (. response ["error"]))
  (when error
    (xt/x:err (or (. error ["message"]) error)))
  (return data))

(defn.js apply-filter
  "applies one pgrest filter descriptor to a Supabase query builder"
  {:added "4.1.7"}
  [query descriptor]
  (var #{path op value} descriptor)
  (if (== op "in")
    (return (. query (in path value)))
    (return (. query (filter path op value)))))

(defn.js get-or-filter
  "extracts the native Supabase OR expression from pgrest params"
  {:added "4.1.7"}
  [params]
  (var out nil)
  (xt/for:array [param (or params [])]
    (when (and (xt/x:is-string? param)
               (. param (startsWith "or=(")))
      (var value (xt/x:second (xt/x:str-split param "=")))
      (when (and value
                 (>= (xt/x:str-len value) 2))
        (:= out
            (xt/x:str-substring value
                                (xt/x/offset 1)
                                (- (xt/x:str-len value) 1))))))
  (return out))

(defn.js apply-controls
  "applies order, limit and offset controls to a Supabase query builder"
  {:added "4.1.7"}
  [query params]
  (var order-entries [])
  (var limit nil)
  (var offset 0)
  (xt/for:array [param (or params [])]
    (var parts (xt/x:str-split param "="))
    (var key (xt/x:first parts))
    (var value (xt/x:second parts))
    (cond (== key "order")
          (xt/for:array [entry (xt/x:str-split (or value "") ",")]
            (when (not (== entry ""))
              (xt/x:arr-push order-entries entry)))

          (== key "limit")
          (:= limit (xt/x:to-number value))

          (== key "offset")
          (:= offset (xt/x:to-number value))))
  (xt/for:array [entry order-entries]
    (var spec (xt/x:str-split entry "."))
    (var column (xt/x:first spec))
    (var direction (xt/x:second spec))
    (:= query
        (. query
           (order column
                  {"ascending" (not (== direction "desc"))}))))
  (cond (xt/x:not-nil? limit)
        (:= query
            (:? (> offset 0)
                (. query (range offset (+ offset limit -1)))
                (. query (limit limit))))

        (> offset 0)
        (:= query (. query (range offset (+ offset 999)))))
  (return query))

(defn.js query-builder
  "translates an xt.db PostgREST request into native Supabase-js calls"
  {:added "4.1.7"}
  [impl request]
  (var #{client lookup} impl)
  (var table-name (. request ["table"]))
  (var schema-name (xt/x:get-path lookup [table-name "schema"]))
  (var source (:? schema-name
                  (. client (schema schema-name))
                  client))
  (var query (. source (from table-name)))
  (:= query (. query (select (or (. request ["select"]) "*"))))
  (var params (or (. request ["params"]) []))
  (var or-filter (-/get-or-filter params))
  (if or-filter
    (:= query (. query (or or-filter)))
    (xt/for:array [descriptor (or (. request ["filters"]) [])]
      (:= query (-/apply-filter query descriptor))))
  (return (-/apply-controls query params)))

(defn.js pull-async
  "pulls a canonical xt.db tree through a native Supabase query builder"
  {:added "4.1.7"}
  [impl tree]
  (var #{schema opts} impl)
  (var request (pgrest-graph/select schema tree (or opts {})))
  (return
   (-> (-/query-builder impl request)
       (promise/x:promise-then -/response-data))))

(defn.js rpc-call-async
  "calls an xt.db RPC through the native Supabase client"
  {:added "4.1.7"}
  [impl rpc-spec args]
  (var input-spec (or (. rpc-spec ["input"]) []))
  (var body {})
  (xt/for:array [[i input] input-spec]
    (var key (or (. input ["symbol"])
                 (. input ["name"])
                 nil))
    (when (xt/x:not-nil? key)
      (xt/x:set-key body key (xt/x:get-idx args i))))
  (var #{client} impl)
  (var schema-name (. rpc-spec ["schema"]))
  (var source (:? schema-name
                  (. client (schema schema-name))
                  client))
  (return
   (-> (. source (rpc (. rpc-spec ["id"]) body))
       (promise/x:promise-then -/response-data))))

(defimpl.xt ^{:lang :js}
  ImplSupabaseJs
  [client schema lookup listeners opts metadata]

  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async}

  impl-common/ISourceListener
  {impl-common/add-db-listener    impl-common/add-db-listener-default
   impl-common/remove-db-listener impl-common/remove-db-listener-default
   impl-common/get-db-listener    impl-common/get-db-listener-default})

(defn.js impl-supabase-js
  "creates a JavaScript-only xt.db adapter from a native Supabase client"
  {:added "4.1.7"}
  [client schema lookup]
  (return
   (-/ImplSupabaseJs client schema lookup {} {} {})))
