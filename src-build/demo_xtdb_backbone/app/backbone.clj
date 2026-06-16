(ns demo-xtdb-backbone.app.backbone
  (:require [hara.lang :as l]
            [demo-xtdb-backbone.app.config :as config]
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
             [xt.db.runtime.client-supabase :as client-supabase]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lib.supabase :as supabase]]})

(defn.js resolve-supabase-base-url
  []
  (var config (@! +supabase-config+))
  (var base-url (xt/x:get-key config "base_url"))
  (when (xt/x:not-nil? base-url)
    (return base-url))
  (var protocol (or (xt/x:get-key config "protocol")
                    (:? (== "https:" (xtd/get-in globalThis ["location" "protocol"]))
                        "https:"
                        "http:")))
  (var hostname (or (xt/x:get-key config "hostname")
                    (xtd/get-in globalThis ["location" "hostname"])
                    "127.0.0.1"))
  (var port (or (xt/x:get-key config "port")
                55121))
  (return (xt/x:cat protocol "//" hostname ":" port)))

(defn.js default-api-config
  []
  (var config (@! +supabase-config+))
  (return
   {"base_url" (-/resolve-supabase-base-url)
    "schema_name" (or (xt/x:get-key config "schema_name")
                      "scratch_v0")
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
  (var raw (-/merge-api-config opts))
  (xt/x:set-key raw "transport" (js-fetch/create {} {}))
  (return (client-supabase/client raw)))

(defn.js request-error
  [response tag]
  (var status (xt/x:get-key response "status"))
  (if (or (xt/x:nil? status)
          (< status 400))
    (return nil)
    (return {"status" "error"
             "tag" tag
             "data" {"status" status
                     "body" (xt/x:get-key response "body")}})))

(defn.js sign-up-with-password
  [client credentials]
  (return
   (promise/x:promise-then
    (supabase/dispatch-auth-request
     client
     {"method" "POST"
      "url" (supabase/auth-path "/signup")
      "body" (supabase/password-request-body credentials)}
     {})
    (fn [response]
      (var error (supabase/response-error response))
      (if (xt/x:not-nil? error)
        (return {"data" {"user" nil
                         "session" nil}
                 "error" error})
        (do
          (var out (supabase/auth-response (or (xt/x:get-key response "body") {})))
          (var data (xt/x:get-key out "data"))
          (var session (xt/x:get-key data "session"))
          (var user (xt/x:get-key data "user"))
          (when (xt/x:not-nil? session)
            (supabase/store-session! client session user))
          (return out)))))))

(defn.js ensure-session
  [client credentials]
  (return
   (promise/x:promise-then
    (supabase/login client credentials)
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
    (client-supabase/dispatch-request
     client
     {"method" "POST"
      "url" "/rest/v1/rpc/ping"}
     {})
    (fn [response]
      (var error (-/request-error response "demo.xtdb_backbone/ping-failed"))
      (if (xt/x:not-nil? error)
        (return error)
        (return (client-supabase/unwrap-response response)))))))

(defn.js recent-logs-request
  [client]
  (return
   (promise/x:promise-then
    (client-supabase/dispatch-request
     client
     {"method" "GET"
      "url" "/rest/v1/Log?select=id,message,author_id&order=id.desc&limit=10"}
     {})
    (fn [response]
      (var error (-/request-error response "demo.xtdb_backbone/log-select-failed"))
      (if (xt/x:not-nil? error)
        (return error)
        (return (client-supabase/unwrap-response response)))))))

(defn.js log-append-request
  [client message]
  (return
   (promise/x:promise-then
    (client-supabase/dispatch-request
     client
     {"method" "POST"
      "url" "/rest/v1/rpc/log_append"
      "body" {"i_message" message}}
     {})
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
              (return {"append" (client-supabase/unwrap-response response)
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
   {"node_id" "demo-xtdb-backbone-worker"
    "space_id" "demo/shared"
    "shared_key" "__demo_xtdb_backbone_sharedworker__"
    "transport_prefix" "demo-transport-"
    "ready" {"signal" "ready"
             "worker" "demo-xtdb-backbone-worker"}
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
