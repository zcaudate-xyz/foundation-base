(ns xt.net.lib-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as ut]]})

(defn.xt create-client
  [methods host port secured basepath apikey]
  (return
   (fetch/create-base "net.superbase"
                      methods
                      {:secured secured
                       :host host
                       :port port
                       :headers {"apikey" apikey
                                 "Content-Type" "application/json"
                                 "Accept" "application/json"}
                       :basepath ""})))

(defn.xt request
  [client opts]
  (:= opts (or opts {}))
  (var #{token
         apikey} opts)
  (var headers (xt/x:obj-clone (or (xt/x:get-key opts "headers") {})))
  (when token
    (xt/x:set-key headers "Authorization" (xt/x:cat "Bearer " token)))
  (when apikey
    (xt/x:set-key headers "apikey" apikey))
  (return
   (-> (fetch/request-http client (xt/x:obj-assign {:headers headers}
                                                   opts))
       (fetch/then-normalise))))

(defn.xt query-path
  [path opts]
  (var query (xt/x:get-key (or opts {}) "query"))
  (var query-string (ut/encode-query-params (or query {})))
  (if (== "" query-string)
    (return path)
    (return (xt/x:cat path "?" query-string))))

(defn.xt request-get
  [client path opts]
  (return
   (-/request client (xt/x:obj-assign {:path path
                                       :method "GET"}
                                      opts))))

(defn.xt request-json
  [client path method data opts]
  (return
   (-/request client (xt/x:obj-assign {:path path
                                       :method method
                                       :body (xt/x:json-encode data)}
                                      opts))))

(defn.xt health
  [client opts]
  (return
   (-/request-get client "/auth/v1/health" opts)))

(defn.xt signup
  [client data opts]
  (return
   (-/request-json client "/auth/v1/signup" "POST" data opts)))

(defn.xt admin-create-user
  [client data opts]
  (return
   (-/request-json client "/auth/v1/admin/users" "POST" data opts)))

(defn.xt admin-delete-user
  [client user_id opts]
  (return
   (-/request client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/user/" user_id)
                                       :method "DELETE"}
                                      opts))))

(defn.xt admin-generate-link
  [client data opts]
  (return
   (-/request-json client "/auth/v1/admin/generate_link" "POST" data opts)))

(defn.xt admin-get-user
  [client user_id opts]
  (return
   (-/request-get client (xt/x:cat "/auth/v1/admin/user/" user_id) opts)))

(defn.xt admin-list-users
  [client opts]
  (return
   (-/request-get client "/auth/v1/admin/users" opts)))

(defn.xt admin-update-user
  [client user_id opts]
  (return
   (-/request client (xt/x:obj-assign {:path (xt/x:cat "/auth/v1/admin/user/" user_id)
                                       :method "PUT"}
                                      opts))))

(defn.xt authorize
  [client opts]
  (return
   (-/request-get client
                  (-/query-path "/auth/v1/authorize" opts)
                  opts)))

(defn.xt callback
  [client opts]
  (return
   (-/request-get client "/auth/v1/callback" opts)))

(defn.xt invite
  [client data opts]
  (return
   (-/request-json client "/auth/v1/invite" "POST" data opts)))

(defn.xt logout
  [client opts]
  (return
   (-/request client (xt/x:obj-assign {:path "/auth/v1/logout"
                                       :method "POST"}
                                      opts))))

(defn.xt otp
  [client data opts]
  (return
   (-/request-json client "/auth/v1/otp" "POST" data opts)))

(defn.xt recovery
  [client data opts]
  (return
   (-/request-json client "/auth/v1/recover" "POST" data opts)))

(defn.xt settings
  [client opts]
  (return
   (-/request-get client "/auth/v1/settings" opts)))

(defn.xt token-password
  [client data opts]
  (return
   (-/request-json client
                   "/auth/v1/token?grant_type=password"
                   "POST" data opts)))

(defn.xt token-refresh
  [client data opts]
  (return
   (-/request-json client
                   "/auth/v1/token?grant_type=refresh_token"
                   "POST" data opts)))

(defn.xt user-get
  [client opts]
  (return
   (-/request-get client "/auth/v1/user" opts)))

(defn.xt user-put
  [client data opts]
  (return
   (-/request-json client "/auth/v1/user" "PUT" data opts)))

(defn.xt verify-get
  [client opts]
  (return
   (-/request-get client
                  (-/query-path "/auth/v1/verify" opts)
                  opts)))

(defn.xt verify-post
  [client data opts]
  (return
   (-/request-json client "/auth/v1/verify" "POST" data opts)))

(comment
  (xt.lang.common-notify/wait-on :js
    (-> (js-fetch/create
         {:defaults {:secured false
                     :host "127.0.0.1"
                     :port "55121"
                     :headers {"apikey" (@! +token+)
                               "Content-Type" "application/json"}
                     :basepath "/auth/v1"}})
        (-/request {:method "POST"
                    :path "signup"
                    :body (xt/x:json-encode
                           {:email "alice@example.com"
                            :password "123456789"})})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out))))))
