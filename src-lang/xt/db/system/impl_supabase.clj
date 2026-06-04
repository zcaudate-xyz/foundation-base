(ns xt.db.system.impl-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lib.supabase :as supabase]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]]})


(defn.xt supabase-client
  "creates the thin supabase client record with stored context"
  {:added "4.1"}
  [schema lookup opts settings]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.supabase"
                             schema
                             lookup
                             (or opts {}))
    {"settings" (or settings {})})))

(defn.xt supabase-client-init
  "creates the underlying supabase transport client and stores it"
  {:added "4.1"}
  [client]
  (var #{settings} client)
  (var instance (supabase/client settings))
  (xt/x:set-key client "instance" instance)
  (return
   (promise/x:promise-run client)))



(comment

  (defn.xt pull-async
    "runs a tree ir pull with async supabase semantics"
    {:added "4.1"}
    [client tree]
    (var #{instance
           schema
           settings
           opts} client)
    (when (xt/x:nil? instance)
      (:= instance (supabase/client settings))
      (xt/x:set-key client "instance" instance))
    (var compiled (pgrest-graph/select-return schema tree 0 opts))
    (var request (fetch-if/request-prepare compiled))
    (var headers (fetch-if/merge-headers
                  (xt/x:get-key settings "headers")
                  (xt/x:get-key request "headers")))
    (var schema-name (or (xt/x:get-key settings "schema_name")
                         (xt/x:get-key opts "schema_name")
                         nil))
    (when (xt/x:not-nil? schema-name)
      (xt/x:set-key headers "Content-Profile" schema-name)
      (when (and (== "GET" (xt/x:get-key request "method"))
                 (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
        (xt/x:set-key headers "Accept-Profile" schema-name)))
    (when (xt/x:not-nil? (xt/x:get-key settings "api_key"))
      (xt/x:set-key headers "apikey" (xt/x:get-key settings "api_key")))
    (when (xt/x:not-nil? (xt/x:get-key settings "auth_token"))
      (xt/x:set-key headers
                    "Authorization"
                    (xt/x:cat "Bearer " (xt/x:get-key settings "auth_token"))))
    (xt/x:set-key request
                  "url"
                  (supabase/join-url (xt/x:get-key settings "base_url")
                                     (xt/x:get-key request "url")))
    (xt/x:set-key request "headers" headers)
    (return
     (promise/x:promise-then
      (fetch/request instance request opts)
      (fn [response]
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
              (return response))))))

  (defn.xt rpc-call-async
    "calls a supabase rpc endpoint with async semantics"
    {:added "4.1"}
    [client fn-name args]
    (var #{instance
           settings
           opts} client)
    (when (xt/x:nil? instance)
      (:= instance (supabase/client settings))
      (xt/x:set-key client "instance" instance))
    (var rpc-name (xt/x:str-replace (xt/x:str-to-lower fn-name) "-" "_"))
    (var request
         (fetch-if/request-prepare
          {"type" "rpc"
           "fn" rpc-name
           "method" "POST"
           "path" (xt/x:cat "/rest/v1/rpc/" rpc-name)
           "url" (xt/x:cat "/rest/v1/rpc/" rpc-name)
           "body" (or args {})
           "headers" {"Content-Type" "application/json"}}))
    (var headers (fetch-if/merge-headers
                  (xt/x:get-key settings "headers")
                  (xt/x:get-key request "headers")))
    (var schema-name (or (xt/x:get-key settings "schema_name")
                         (xt/x:get-key opts "schema_name")
                         nil))
    (when (xt/x:not-nil? schema-name)
      (xt/x:set-key headers "Content-Profile" schema-name))
    (when (xt/x:not-nil? (xt/x:get-key settings "api_key"))
      (xt/x:set-key headers "apikey" (xt/x:get-key settings "api_key")))
    (when (xt/x:not-nil? (xt/x:get-key settings "auth_token"))
      (xt/x:set-key headers
                    "Authorization"
                    (xt/x:cat "Bearer " (xt/x:get-key settings "auth_token"))))
    (xt/x:set-key request
                  "url"
                  (supabase/join-url (xt/x:get-key settings "base_url")
                                     (xt/x:get-key request "url")))
    (xt/x:set-key request "headers" headers)
    (return
     (promise/x:promise-then
      (fetch/request instance request opts)
      (fn [response]
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
              (return response)))))))
