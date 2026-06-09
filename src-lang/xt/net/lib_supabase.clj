(ns xt.net.lib-supabase
  (:require [hara.lang :as l]
            [scaffold.supabase.event-host-util :as live]))

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
  (return
   (-> (fetch/request-http client opts)
       (fetch/then-normalise))))

(defn.xt health
  [client opts]
  (return
   (-/request client (xt/x:obj-assign {:path "/auth/v1/health"}
                                      opts))))

(defn.xt signup
  [client data opts]
  (-> (-/request client (xt/x:obj-assign {:path "/auth/v1/signup"
                                          :method "POST"
                                          :body (xt/x:json-encode data)}
                                         opts))))

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

