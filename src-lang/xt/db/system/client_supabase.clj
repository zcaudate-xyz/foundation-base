(ns xt.db.system.client-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.client-common :as client-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lib.supabase :as supabase]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]
             [xt.protocol.impl.graphdb :as graphdb]]})

(defn.xt client?
  "checks if a value is a tagged system supabase client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "db.client.supabase"
                   (xt/x:get-key obj "::")))))

(defn.xt unsupported-op
  "signals that the system supabase client is read only"
  {:added "4.1"}
  [op]
  (xt/x:err (xt/x:cat "Unsupported operation for read-only db.client.supabase - "
                     op)))

(defn.xt raw-client
  "returns the raw supabase client source"
  {:added "4.1"}
  [client]
  (if (fetch/client? client)
    (return (or (xt/x:get-key client "_raw") {}))
    (return (or client {}))))

(defn.xt resolve-transport
  "resolves the transport used for fetch requests"
  {:added "4.1"}
  [client]
  (var raw-client (-/raw-client client))
  (var transport-source (xt/x:get-key raw-client "transport"))
  (when (and (xt/x:nil? transport-source)
             (or (xt/x:is-function? (xt/x:get-key raw-client "request"))
                 (xt/x:is-function? (xt/x:get-key raw-client "fetch"))))
    (:= transport-source raw-client))
  (when (xt/x:nil? transport-source)
    (xt/x:err "Supabase client missing transport"))
  (if (fetch/client? transport-source)
    (return transport-source)
    (return (fetch/client-create transport-source nil))))

(defn.xt resolve-base-url
  "resolves the base url override"
  {:added "4.1"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (client-common/get-setting raw-client "base_url")
              (xt/x:get-key opts "base_url")
              nil)))

(defn.xt resolve-api-key
  "resolves the api key"
  {:added "4.1"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (client-common/get-setting raw-client "api_key")
              (xt/x:get-key opts "api_key")
              nil)))

(defn.xt resolve-auth-token
  "resolves the auth token"
  {:added "4.1"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (client-common/get-setting raw-client "auth_token")
              (xt/x:get-key opts "auth_token")
              nil)))

(defn.xt resolve-schema-name
  "resolves the schema name"
  {:added "4.1"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (client-common/get-setting raw-client "schema_name")
              (xt/x:get-key opts "schema_name")
              nil)))

(defn.xt create-scaffold
  "creates the request scaffold"
  {:added "4.1"}
  [client opts]
  (var raw-client (-/raw-client client))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key raw-client "headers")
                (xt/x:get-key opts "headers")))
  (return {"client" client
           "base_url" (-/resolve-base-url client opts)
           "api_key" (-/resolve-api-key client opts)
           "auth_token" (-/resolve-auth-token client opts)
           "schema_name" (-/resolve-schema-name client opts)
           "headers" headers}))

(defn.xt resolve-request-headers
  "resolves headers for the outgoing request"
  {:added "4.1"}
  [client request opts]
  (var scaffold (-/create-scaffold client opts))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key request "headers")
                (xt/x:get-key scaffold "headers")))
  (var schema-name (xt/x:get-key scaffold "schema_name"))
  (when (xt/x:not-nil? schema-name)
    (xt/x:set-key headers "Content-Profile" schema-name))
  (when (and (== "GET" (xt/x:get-key request "method"))
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (var api-key (xt/x:get-key scaffold "api_key"))
  (when (xt/x:not-nil? api-key)
    (xt/x:set-key headers "apikey" api-key))
  (var auth-token (xt/x:get-key scaffold "auth_token"))
  (when (xt/x:not-nil? auth-token)
    (xt/x:set-key headers "Authorization" (xt/x:cat "Bearer " auth-token)))
  (return headers))

(defn.xt prepare-request
  "prepares a compiled request for transport"
  {:added "4.1"}
  [client request opts]
  (var scaffold (-/create-scaffold client opts))
  (var out (fetch-if/request-prepare request))
  (xt/x:set-key out
                "url"
                (supabase/join-url (xt/x:get-key scaffold "base_url")
                                   (xt/x:get-key out "url")))
  (xt/x:set-key out
                "headers"
                (-/resolve-request-headers client out opts))
  (return out))

(defn.xt unwrap-response
  "unwraps standard fetch response payloads"
  {:added "4.1"}
  [response]
  (cond (xt/x:nil? response)
        (return nil)

        (xt/x:not-nil? (xt/x:get-key response "body"))
        (do (var body (xt/x:get-key response "body"))
            (if (and (xt/x:is-object? body)
                     (xt/x:not-nil? (xt/x:get-key body "data")))
              (return (xt/x:get-key body "data"))
              (return body)))

        (xt/x:not-nil? (xt/x:get-key response "data"))
        (return (xt/x:get-key response "data"))

        :else
        (return response)))

(defn.xt normalize-client
  "normalises a raw value into a tagged supabase client shape"
  {:added "4.1"}
  [raw]
  (when (-/client? raw)
    (return raw))
  (var source (client-common/create-client raw
                                           "db.client.supabase"
                                           ["base_url"
                                            "api_key"
                                            "auth_token"
                                            "schema_name"
                                            "headers"
                                            "client"
                                            "transport"]))
  (return source))

(defn.xt resolve-client
  "resolves the underlying fetch client"
  {:added "4.1"}
  [client opts]
  (:= client (-/normalize-client client))
  (var source (or (xt/x:get-key client "client")
                  (xt/x:get-key opts "client")
                  (xt/x:get-key client "transport")
                  (xt/x:get-key opts "transport")
                  nil))
  (when (xt/x:nil? source)
    (xt/x:err "Supabase pull missing client"))
  (if (supabase/client? source)
    (return source)
    (return (supabase/client source))))

(defn.xt pull-sync
  "client-supabase only supports async reads"
  {:added "4.1"}
  [client schema tree opts]
  (return (-/unsupported-op "pull_sync")))

(defn.xt pull
  "fetches tree ir data through a PostgREST request"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/normalize-client client))
  (var compiled (pgrest-graph/select-return schema tree 0 (or opts {})))
  (var fetch-client (-/resolve-client client (or opts {})))
  (var request (-/prepare-request client compiled (or opts {})))
  (return
   (promise/x:promise-then
    (fetch/request fetch-client request opts)
    (fn [response]
      (return (-/unwrap-response response))))))

(defn.xt process-event-sync
  "client-supabase does not support writes"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (return (-/unsupported-op "process_event_sync")))

(defn.xt process-event-remove
  "client-supabase does not support deletes by nested payload"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (return (-/unsupported-op "process_event_remove")))

(defn.xt record-add-sync
  "client-supabase does not support direct record writes"
  {:added "4.1"}
  [client schema table-name records opts]
  (return (-/unsupported-op "record_add_sync")))

(defn.xt record-add
  "client-supabase does not support async record writes"
  {:added "4.1"}
  [client schema table-name records opts]
  (return (-/unsupported-op "record_add")))

(defn.xt record-delete-sync
  "client-supabase does not support direct deletes"
  {:added "4.1"}
  [client schema table-name ids opts]
  (return (-/unsupported-op "record_delete_sync")))

(defn.xt record-delete
  "client-supabase does not support async deletes"
  {:added "4.1"}
  [client schema table-name ids opts]
  (return (-/unsupported-op "record_delete")))

(defn.xt attach-graphdb
  "attaches graphdb methods to a supabase client"
  {:added "4.1"}
  [client]
  (return
   (graphdb/db-create
   client
   {"pull" (fn [client schema tree opts]
             (return (-/pull client schema tree opts)))})))

(defn.xt client
  "creates a tagged supabase client"
  {:added "4.1"}
  [raw]
  (return (-/attach-graphdb (-/normalize-client raw))))

(def.xt DRIVER
  (graphdb/driver-create
   {"create" (fn [m]
               (return (-/client m)))}))
