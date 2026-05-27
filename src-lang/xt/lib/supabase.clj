(ns xt.lib.supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.client-fetch :as fetch-if]
             [xt.protocol.impl.client-fetch :as fetch]]})

(def.xt AUTH_ROOT "/auth/v1")

(defn.xt client?
  {:added "4.1.4"}
  [obj]
  (return (and (fetch/client? obj)
               (== "supabase.client"
                   (xt/x:get-key (xt/x:get-key obj "_raw")
                                 "::supabase")))))

(defn.xt raw-client
  {:added "4.1.4"}
  [client]
  (if (fetch/client? client)
    (return (or (xt/x:get-key client "_raw") {}))
    (return (or client {}))))

(defn.xt resolve-transport
  {:added "4.1.4"}
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
  {:added "4.1.4"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "base_url")
              (xt/x:get-key opts "base_url")
              nil)))

(defn.xt resolve-api-key
  {:added "4.1.4"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "api_key")
              (xt/x:get-key opts "api_key")
              nil)))

(defn.xt resolve-auth-token
  {:added "4.1.4"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (return (or (xt/x:get-key raw_client "auth_token")
              (xt/x:get-key opts "auth_token")
              nil)))

(defn.xt create-scaffold
  {:added "4.1.4"}
  [db client opts]
  (var raw_client (-/raw-client client))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key raw_client "headers")
                (xt/x:get-key opts "headers")))
  (return {"client" client
           "base_url" (-/resolve-base-url db client opts)
           "api_key" (-/resolve-api-key db client opts)
           "auth_token" (-/resolve-auth-token db client opts)
           "headers" headers}))

(defn.xt client
  {:added "4.1.4"}
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
    {"request" (fn [raw input opts]
                 (var transport (-/resolve-transport raw))
                 (return (fetch/request transport input opts)))})))

(defn.xt create-client
  {:added "4.1.4"}
  [base_url api_key opts]
  (var raw (xt/x:obj-clone (or opts {})))
  (xt/x:set-key raw "base_url" base_url)
  (xt/x:set-key raw "api_key" api_key)
  (return (-/client raw)))

(defn.xt auth-path
  {:added "4.1.4"}
  [path]
  (return (-/join-url -/AUTH_ROOT path)))

(defn.xt join-url
  {:added "4.1.4"}
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

(defn.xt has-session?
  {:added "4.1.4"}
  [data]
  (return (and (xt/x:not-nil? (xt/x:get-key data "access_token"))
               (xt/x:not-nil? (xt/x:get-key data "refresh_token"))
               (xt/x:not-nil? (xt/x:get-key data "expires_in")))))

(defn.xt token-response->session
  {:added "4.1.4"}
  [data]
  (var session nil)
  (when (-/has-session? data)
    (:= session (xt/x:obj-clone data))
    (when (xt/x:nil? (xt/x:get-key session "expires_at"))
      (xt/x:set-key session
                    "expires_at"
                    (+ (xt/x:m-floor (/ (xt/x:now-ms) 1000))
                       (xt/x:get-key data "expires_in")))))
  (return session))

(defn.xt auth-response
  {:added "4.1.4"}
  [data]
  (var session (-/token-response->session data))
  (var user (or (xt/x:get-key data "user")
                data))
  (var out {"data" {"session" session
                    "user" user}
            "error" nil})
  (when (xt/x:not-nil? (xt/x:get-key data "weak_password"))
    (xt/x:set-key (xt/x:get-key out "data")
                  "weak_password"
                  (xt/x:get-key data "weak_password")))
  (return out))

(defn.xt response-error
  {:added "4.1.4"}
  [response]
  (var status (xt/x:get-key response "status"))
  (when (or (xt/x:nil? status)
            (< status 400))
    (return nil))
  (var body (xt/x:get-key response "body"))
  (cond (xt/x:nil? body)
        (return {"status" status
                 "message" "Supabase auth request failed"})

        (xt/x:is-object? body)
        (do (var err (xt/x:obj-clone body))
            (when (xt/x:nil? (xt/x:get-key err "status"))
              (xt/x:set-key err "status" status))
            (return err))

        :else
        (return {"status" status
                 "message" body})))

(defn.xt invalid-password-credentials
  {:added "4.1.4"}
  []
  (return {"data" {"user" nil
                   "session" nil}
           "error" {"message" "You must provide either an email or phone number and a password"}}))

(defn.xt password-request-body
  {:added "4.1.4"}
  [credentials]
  (var body {})
  (when (xt/x:not-nil? (xt/x:get-key credentials "email"))
    (xt/x:set-key body "email" (xt/x:get-key credentials "email")))
  (when (xt/x:not-nil? (xt/x:get-key credentials "phone"))
    (xt/x:set-key body "phone" (xt/x:get-key credentials "phone")))
  (when (xt/x:not-nil? (xt/x:get-key credentials "password"))
    (xt/x:set-key body "password" (xt/x:get-key credentials "password")))
  (var options (xt/x:get-key credentials "options"))
  (var captcha_token (or (xt/x:get-key options "captcha_token")
                         (xt/x:get-key options "captchaToken")
                         nil))
  (when (xt/x:not-nil? captcha_token)
    (xt/x:set-key body
                  "gotrue_meta_security"
                  {"captcha_token" captcha_token}))
  (return body))

(defn.xt store-session!
  {:added "4.1.4"}
  [client session user]
  (:= client (-/client client))
  (var raw (-/raw-client client))
  (if (xt/x:nil? session)
    (do
      (xt/x:del-key raw "session")
      (xt/x:del-key raw "auth_token")
      (xt/x:del-key raw "refresh_token"))
    (do
      (xt/x:set-key raw "session" session)
      (xt/x:set-key raw "auth_token" (xt/x:get-key session "access_token"))
      (xt/x:set-key raw "refresh_token" (xt/x:get-key session "refresh_token"))))
  (if (xt/x:not-nil? user)
    (xt/x:set-key raw "user" user)
    (xt/x:del-key raw "user"))
  (return client))

(defn.xt clear-session!
  {:added "4.1.4"}
  [client]
  (return (-/store-session! client nil nil)))

(defn.xt get-session
  {:added "4.1.4"}
  [client]
  (:= client (-/client client))
  (var raw (-/raw-client client))
  (return
   (promise/x:promise-run
    {"data" {"session" (xt/x:get-key raw "session")}
     "error" nil})))

(defn.xt get-user
  {:added "4.1.4"}
  [client]
  (:= client (-/client client))
  (var raw (-/raw-client client))
  (return
   (promise/x:promise-run
    {"data" {"user" (or (xt/x:get-key raw "user")
                        (xt/x:get-key (xt/x:get-key raw "session") "user")
                        nil)}
     "error" nil})))

(defn.xt resolve-auth-request-headers
  {:added "4.1.4"}
  [db client request opts]
  (var scaffold (-/create-scaffold db client opts))
  (var headers (fetch-if/merge-headers
                (xt/x:get-key request "headers")
                (xt/x:get-key scaffold "headers")))
  (var api_key (xt/x:get-key scaffold "api_key"))
  (when (xt/x:not-nil? api_key)
    (xt/x:set-key headers "apikey" api_key))
  (var auth_token (xt/x:get-key scaffold "auth_token"))
  (when (xt/x:not-nil? auth_token)
    (xt/x:set-key headers
                  "Authorization"
                  (xt/x:cat "Bearer " auth_token)))
  (when (and (xt/x:not-nil? (xt/x:get-key request "body"))
             (xt/x:nil? (xt/x:get-key headers "Content-Type")))
    (xt/x:set-key headers "Content-Type" "application/json;charset=UTF-8"))
  (return headers))

(defn.xt prepare-auth-request
  {:added "4.1.4"}
  [db client input opts]
  (var scaffold (-/create-scaffold db client opts))
  (var request (fetch-if/request-prepare input))
  (xt/x:set-key request
                "url"
                (-/join-url (xt/x:get-key scaffold "base_url")
                            (xt/x:get-key request "url")))
  (xt/x:set-key request
                "headers"
                (-/resolve-auth-request-headers db client request opts))
  (return request))

(defn.xt dispatch-auth-request
  {:added "4.1.4"}
  [raw input opts]
  (var transport (-/resolve-transport raw))
  (var request (-/prepare-auth-request nil raw input (or opts {})))
  (return (fetch/request transport request opts)))

(defn.xt sign-in-with-password
  {:added "4.1.4"}
  [client credentials]
  (:= client (-/client client))
  (when (or (xt/x:nil? (xt/x:get-key credentials "password"))
            (and (xt/x:nil? (xt/x:get-key credentials "email"))
                 (xt/x:nil? (xt/x:get-key credentials "phone"))))
    (return (promise/x:promise-run
             (-/invalid-password-credentials))))
  (return
   (promise/x:promise-then
    (-/dispatch-auth-request
     client
     {"method" "POST"
      "url" (-/auth-path "/token?grant_type=password")
      "body" (-/password-request-body credentials)}
     {})
    (fn [response]
      (var error (-/response-error response))
      (when (xt/x:not-nil? error)
        (return {"data" {"user" nil
                         "session" nil}
                 "error" error}))
      (var out (-/auth-response (or (xt/x:get-key response "body") {})))
      (var data (xt/x:get-key out "data"))
      (var session (xt/x:get-key data "session"))
      (var user (xt/x:get-key data "user"))
      (if (or (xt/x:nil? session)
              (xt/x:nil? user))
        (return {"data" {"user" nil
                         "session" nil}
                 "error" {"message" "Supabase auth returned an invalid session"}})
        (do
          (-/store-session! client session user)
          (return out)))))))

(defn.xt login
  {:added "4.1.4"}
  [client credentials]
  (return (-/sign-in-with-password client credentials)))

(defn.xt sign-out
  {:added "4.1.4"}
  [client opts]
  (:= client (-/client client))
  (var raw (-/raw-client client))
  (var scope (or (xt/x:get-key opts "scope")
                 "global"))
  (var auth_token (or (xt/x:get-key opts "auth_token")
                      (xt/x:get-key raw "auth_token")
                      (xt/x:get-key (xt/x:get-key raw "session") "access_token")
                      nil))
  (when (xt/x:nil? auth_token)
    (when (not (== scope "others"))
      (-/clear-session! client))
    (return (promise/x:promise-run {"error" nil})))
  (return
   (promise/x:promise-then
    (-/dispatch-auth-request
     client
     {"method" "POST"
      "url" (-/auth-path (xt/x:cat "/logout?scope=" scope))}
     {"auth_token" auth_token})
    (fn [response]
      (var error (-/response-error response))
      (var status (xt/x:get-key response "status"))
      (when (xt/x:not-nil? error)
        (:= status (or (xt/x:get-key error "status")
                       status
                       nil)))
      (when (and (xt/x:not-nil? error)
                 (not (or (== status 401)
                          (== status 403)
                          (== status 404))))
        (return {"error" error}))
      (when (not (== scope "others"))
        (-/clear-session! client))
      (return {"error" nil})))))

(defn.xt logout
  {:added "4.1.4"}
  [client opts]
  (return (-/sign-out client opts)))
