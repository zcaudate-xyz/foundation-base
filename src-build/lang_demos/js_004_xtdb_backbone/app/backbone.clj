(ns lang-demos.js-004-xtdb-backbone.app.backbone
  (:require [lang.core :as l]
            [lang-demos.js-004-xtdb-backbone.app.config :as config]
            [postgres.core :as pg]
            [postgres.sample.scratch-v0]))

(def +app+
  (pg/app "scratch_v0"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(def +app-lookup+
  (pg/bind-app +app+))

(def +schema+
  {"Log"
   (get +tree+ "Log")})

(def +lookup+
  {"Log"
   {"position" (get-in +app-lookup+ ["Log" :position])
    "schema" (get-in +app-lookup+ ["Log" :schema])}})

(def +supabase-config+
  (config/supabase-config))

(l/script :js
  {:require [[js.net.http-fetch :as js-fetch]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.addon-supabase :as addon]
             [xt.net.http-util :as http-util]]})

(defn.js resolve-supabase-base-url
  []
  (var config (@! +supabase-config+))
  (var base-url (xt/x:get-key config "base_url"))
  (when (xt/x:not-nil? base-url)
    (return base-url))
  (var protocol (or (xt/x:get-key config "protocol") "http"))
  (var hostname (or (xt/x:get-key config "hostname") "127.0.0.1"))
  (var port (or (xt/x:get-key config "port") 55121))
  (return
   (xt/x:cat
    (xt/x:cat protocol "://")
    (xt/x:cat hostname (xt/x:cat ":" (xt/x:to-string port))))))

(defn.js default-api-config
  []
  (var config (@! +supabase-config+))
  (return
   {"base_url" (-/resolve-supabase-base-url)
    "host" (or (xt/x:get-key config "hostname") "127.0.0.1")
    "port" (or (xt/x:get-key config "port") 55121)
    "secured" (== "https" (xt/x:get-key config "protocol"))
    "schema_name" (or (xt/x:get-key config "schema_name") "scratch_v0")
    "api_key" (xt/x:get-key config "api_key")
    "auth_token" (or (xt/x:get-key config "auth_token")
                     (xt/x:get-key config "api_key"))
    "headers" {}}))

(defn.js merge-api-config
  [opts]
  (return
   (xtd/obj-assign-nested
    (xtd/obj-assign-nested {} (-/default-api-config))
    (or opts {}))))

(defn.js create-client
  [opts]
  (var config (-/merge-api-config opts))
  (return
   (js-fetch/create
    {"host" (xt/x:get-key config "host")
     "port" (xt/x:get-key config "port")
     "secured" (xt/x:get-key config "secured")
     "apikey" (xt/x:get-key config "api_key")
     "token" (xt/x:get-key config "auth_token")
     "headers" (xt/x:get-key config "headers")}
    (addon/middleware-supabase))))

(defn.js response-data
  [response]
  (var body (http-util/get-body-data response))
  (if (xt/x:is-string? body)
    (try
      (return (xt/x:json-decode body))
      (catch err
        (return body)))
    (return body)))

(defn.js request-error
  [response tag]
  (var status (xt/x:get-key response "status"))
  (if (or (xt/x:nil? status)
          (< status 400))
    (return nil)
    (return {"status" "error"
             "tag" tag
             "data" {"status" status
                     "body" (-/response-data response)}})))

(defn.js store-session
  [client session]
  (var defaults (xt/x:get-key client "defaults"))
  (when (xt/x:not-nil? defaults)
    (xt/x:set-key defaults "token"
                  (xt/x:get-key session "access_token")))
  (return client))

(defn.js auth-request
  [client command tag]
  (return
   (promise/x:promise-then
    (js-fetch/request-http client command)
    (fn [response]
      (var error (-/request-error response tag))
      (if (xt/x:not-nil? error)
        (return {"data" {"user" nil
                          "session" nil}
                 "error" error})
        (do
          (var body (-/response-data response))
          (var session (or (xt/x:get-key body "session") body))
          (var user (xt/x:get-key body "user"))
          (when (xt/x:not-nil? session)
            (-/store-session client session))
          (return {"data" {"user" user
                            "session" session}
                   "error" nil})))))))

(defn.js login
  [client credentials]
  (return
   (-/auth-request
    client
    (addon/cmd-token-password credentials {})
    "demo.xtdb_backbone/login-failed")))

(defn.js sign-up-with-password
  [client credentials]
  (return
   (-/auth-request
    client
    (addon/cmd-signup credentials {})
    "demo.xtdb_backbone/signup-failed")))

(defn.js ensure-session
  [client credentials]
  (return
   (promise/x:promise-then
    (-/login client credentials)
    (fn [login-out]
      (if (xt/x:nil? (xt/x:get-key login-out "error"))
        (return login-out)
        (return
         (promise/x:promise-then
          (-/sign-up-with-password client credentials)
          (fn [signup-out]
            (if (xt/x:nil? (xt/x:get-key signup-out "error"))
              (return signup-out)
              (return login-out))))))))))

(defn.js ping-request
  [client]
  (return
   (promise/x:promise-then
    (js-fetch/request-http
     client
     (addon/cmd-rpc-call "ping" {} {}))
    (fn [response]
      (var error (-/request-error response "demo.xtdb_backbone/ping-failed"))
      (if (xt/x:not-nil? error)
        (return error)
        (return (-/response-data response)))))))

(defn.js recent-logs-request
  [client]
  (return
   (promise/x:promise-then
    (js-fetch/request-http
     client
     (addon/cmd-query-table
      "Log"
      "select=id,message,author_id&order=id.desc&limit=10"
      {}))
    (fn [response]
      (var error (-/request-error response "demo.xtdb_backbone/log-select-failed"))
      (if (xt/x:not-nil? error)
        (return error)
        (return (-/response-data response)))))))

(defn.js log-append-request
  [client message]
  (return
   (promise/x:promise-then
    (js-fetch/request-http
     client
     (addon/cmd-rpc-call "log_append" {"i_message" message} {}))
    (fn [response]
      (var error (-/request-error response "demo.xtdb_backbone/log-append-failed"))
      (if (xt/x:not-nil? error)
        (return error)
        (return
         (promise/x:promise-then
          (-/recent-logs-request client)
          (fn [logs]
            (if (and (xt/x:is-object? logs)
                     (== "error" (xt/x:get-key logs "status")))
              (return logs)
              (return {"append" (-/response-data response)
                       "logs" logs}))))))))))

(defn.js ping-page-model
  []
  (return
   {"meta" {"title" "scratch_v0 ping"
             "description" "Calls the public ping RPC from the scratch_v0 schema."}
    "views"
    {"main"
     {"default_input" []
      "resolver"
      {"type" "fn/local"
       "fn" (fn [_ctx]
              (return
               (promise/x:promise-then
                (-/ping-request (-/create-client nil))
                (fn [result]
                  (return result))))
              )
       "trigger.post" (fn [_ctx result]
                        (if (and (xt/x:is-object? result)
                                 (== "error" (xt/x:get-key result "status")))
                          (return result)
                          (return {"reply" result
                                   "schema_name" "scratch_v0"})))}}}}))

(defn.js log-append-page-model
  []
  (return
   {"meta" {"title" "scratch_v0 log_append"
             "description" "Signs in, appends a log row, and returns recent scratch_v0 logs."}
    "views"
    {"main"
     {"default_input" ["hello from scratch_v0"
                       "demo@greenways.local"
                       "greenways-demo"]
      "resolver"
      {"type" "fn/local"
       "fn" (fn [ctx]
              (var message (or (xt/x:get-idx (xt/x:get-key ctx "input") 0)
                               "hello from scratch_v0"))
              (var email (xt/x:get-idx (xt/x:get-key ctx "input") 1))
              (var password (xt/x:get-idx (xt/x:get-key ctx "input") 2))
              (when (or (xt/x:nil? email)
                        (xt/x:nil? password))
                (return {"status" "error"
                         "tag" "demo.xtdb_backbone/missing-credentials"
                         "data" {"email" email
                                 "password" password}}))
              (var client (-/create-client nil))
              (return
               (promise/x:promise-then
                (-/ensure-session client {"email" email
                                          "password" password})
                (fn [auth]
                  (if (xt/x:not-nil? (xt/x:get-key auth "error"))
                    (return {"status" "error"
                             "tag" "demo.xtdb_backbone/auth-failed"
                             "data" (xt/x:get-key auth "error")})
                    (return (-/log-append-request client message)))))))
       "trigger.post" (fn [_ctx result]
                        (if (and (xt/x:is-object? result)
                                 (== "error" (xt/x:get-key result "status")))
                          (return result)
                          (return {"appended" (xt/x:get-key result "append")
                                   "recent_logs" (xt/x:get-key result "logs")})))}}}}))

(defn.js page-model-specs
  []
  (return
   {"ping" (-/ping-page-model)
    "log_append" (-/log-append-page-model)}))

(defn.js sharedworker-config
  []
  (return
   {"node_id" "lang-demos.js-004-xtdb-backbone-worker"
    "space_id" "demo/shared"
    "shared_key" "__demo_xtdb_backbone_sharedworker__"
    "transport_prefix" "demo-transport-"
    "ready" {"signal" "ready"
             "worker" "lang-demos.js-004-xtdb-backbone-worker"}
    "db" {"schema" (@! +schema+)
          "lookup" (@! +lookup+)
          "sources"
          {"primary" {"kind" "supabase"
                      "config" {"client" (-/default-api-config)}}
           "caching" {"kind" "sqlite"
                      "config" {"filename" ":memory:"
                                "flags" "c"}
                      "setup" {"schema" true}}}}
    "spaces" {}}))
