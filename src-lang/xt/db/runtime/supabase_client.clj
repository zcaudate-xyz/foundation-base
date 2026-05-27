(ns xt.db.runtime.supabase-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.pgrest :as pgrest]
             [xt.lib.supabase :as supabase]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.xt client?
  {:added "4.1.3"}
  [obj]
  (return (supabase/client? obj)))

(defn.xt raw-client
  {:added "4.1.3"}
  [client]
  (return (supabase/raw-client client)))

(defn.xt resolve-transport
  {:added "4.1.3"}
  [client]
  (return (supabase/resolve-transport client)))

(defn.xt resolve-base-url
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-base-url db client opts)))

(defn.xt resolve-schema-name
  {:added "4.1.3"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "schema_name")
              (xt/x:get-key opts "schema_name")
              nil)))

(defn.xt resolve-api-key
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-api-key db client opts)))

(defn.xt resolve-auth-token
  {:added "4.1.3"}
  [db client opts]
  (return (supabase/resolve-auth-token db client opts)))

(defn.xt create-scaffold
  {:added "4.1.3"}
  [db client opts]
  (var scaffold (supabase/create-scaffold db client opts))
  (xt/x:set-key scaffold "schema_name" (-/resolve-schema-name db client opts))
  (return scaffold))

(defn.xt join-url
  {:added "4.1.3"}
  [base_url path]
  (return (supabase/join-url base_url path)))

(defn.xt resolve-request-headers
  {:added "4.1.3"}
  [db client request opts]
  (var scaffold (-/create-scaffold db client opts))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key request "headers")
                (xt/x:get-key scaffold "headers")))
  (var schema_name (xt/x:get-key scaffold "schema_name"))
  (when (xt/x:not-nil? schema_name)
    (xt/x:set-key headers "Content-Profile" schema_name))
  (when (and (== "GET" (xt/x:get-key request "method"))
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (var api_key (xt/x:get-key scaffold "api_key"))
  (when (xt/x:not-nil? api_key)
    (xt/x:set-key headers "apikey" api_key))
  (var auth_token (xt/x:get-key scaffold "auth_token"))
  (when (xt/x:not-nil? auth_token)
    (xt/x:set-key headers
                  "Authorization"
                  (xt/x:cat "Bearer " auth_token)))
  (return headers))

(defn.xt prepare-request
  {:added "4.1.3"}
  [db client input opts]
  (var scaffold (-/create-scaffold db client opts))
  (var request (fetch-if/request-prepare input))
  (xt/x:set-key request
                "url"
                (-/join-url (xt/x:get-key scaffold "base_url")
                            (xt/x:get-key request "url")))
  (xt/x:set-key request
                "headers"
                (-/resolve-request-headers db client request opts))
  (return request))

(defn.xt dispatch-request
  {:added "4.1.3"}
  [raw input opts]
  (var transport (-/resolve-transport raw))
  (var request (-/prepare-request nil raw input (or opts {})))
  (return (fetch/request transport request opts)))

(defn.xt unwrap-response
  {:added "4.1.3"}
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

(defn.xt client
  {:added "4.1.3"}
  [raw]
  (when (-/client? raw)
    (return raw))
  (var source nil)
  (if (fetch/client? raw)
    (:= source {"transport" raw})
    (if (xt/x:nil? raw)
      (:= source {})
      (:= source (xt/x:obj-clone raw))))
  (xt/x:set-key source "::supabase" "supabase.client")
  (return
   (fetch/client-create
    source
    {"request" -/dispatch-request})))

(defn.xt resolve-client
  {:added "4.1.3"}
  [db opts]
  (var source (or (xt/x:get-key db "client")
                  (xt/x:get-key opts "client")
                  (xt/x:get-key db "transport")
                  (xt/x:get-key opts "transport")
                  nil))
  (when (xt/x:nil? source)
    (xt/x:err "Supabase pull missing client"))
  (if (supabase/client? source)
    (return source)
    (return (-/client source))))

(defn.xt pull
  {:added "4.1.3"}
  [db schema query-plan opts]
  (var compiled (pgrest/compile-query query-plan))
  (var client (-/resolve-client db (or opts {})))
  (return
   (promise/x:promise-then
    (fetch/request client compiled opts)
    (fn [response]
      (return (-/unwrap-response response))))))
