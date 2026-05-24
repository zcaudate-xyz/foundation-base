(ns xt.db.runtime.supabase-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.pgrest :as pgrest]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.xt client?
  {:added "4.1.3"}
  [obj]
  (return (and (fetch/client? obj)
               (== "supabase.client"
                   (xt/x:get-key (xt/x:get-key obj "_raw")
                                 "::supabase")))))

(defn.xt raw-client
  {:added "4.1.3"}
  [client]
  (if (fetch/client? client)
    (return (or (xt/x:get-key client "_raw") {}))
    (return (or client {}))))

(defn.xt resolve-transport
  {:added "4.1.3"}
  [client]
  (var raw_client (-/raw-client client))
  (var transport-source (xt/x:get-key raw_client "transport"))
  (when (and (xt/x:nil? transport-source)
             (or (xt/x:is-function? (xt/x:get-key raw_client "request"))
                 (xt/x:is-function? (xt/x:get-key raw_client "fetch"))))
    (:= transport-source raw_client))
  (when (xt/x:nil? transport-source)
    (xt/x:err "Supabase client missing transport"))
  (if (fetch/client? transport-source)
    (return transport-source)
    (return (fetch/client-create transport-source nil))))

(defn.xt resolve-base-url
  {:added "4.1.3"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "base_url")
              (xt/x:get-key opts "base_url")
              nil)))

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
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "api_key")
              (xt/x:get-key opts "api_key")
              nil)))

(defn.xt resolve-auth-token
  {:added "4.1.3"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "auth_token")
              (xt/x:get-key opts "auth_token")
              nil)))

(defn.xt create-scaffold
  {:added "4.1.3"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key raw_client "headers")
                (xt/x:get-key opts "headers")))
  (return {"client" client
           "base_url" (-/resolve-base-url db client opts)
           "schema_name" (-/resolve-schema-name db client opts)
           "api_key" (-/resolve-api-key db client opts)
           "auth_token" (-/resolve-auth-token db client opts)
           "headers" headers}))

(defn.xt join-url
  {:added "4.1.3"}
  [base_url path]
  (cond (or (xt/x:nil? base_url)
            (not (xt/x:is-string? base_url)))
        (return path)

        (and (str/ends-with? base_url "/")
             (str/starts-with? path "/"))
        (return (xt/x:cat base_url
                          (xt/x:str-substring path 1)))

        (and (not (str/ends-with? base_url "/"))
             (not (str/starts-with? path "/")))
        (return (xt/x:cat base_url "/" path))

        :else
        (return (xt/x:cat base_url path))))

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
  (if (-/client? source)
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
