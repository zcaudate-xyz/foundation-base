(ns js.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]
             [postgres.core :as pg]
             [postgres.core.supabase :as s]]
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :shutdown docker-min/stop-supabase}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(fact:global
 {:setup    [(l/rt:restart :js)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.fetch/request-http-raw :added "4.1"}
(fact "performs a http request"

  (notify/wait-on :js
    (-> (js-fetch/request-http-raw {:url (fetch/prepare-url {:defaults {:host "127.0.0.1",
                                                                        :port "55121"}}
                                                            {:path "/auth/v1/health"})
                                    :headers {"apikey" (@! (-> docker-min/+config+ :api :anon-key))}} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200)

^{:refer js.net.fetch/request-http-client :added "4.1"}
(fact "request http client for supabase"
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :anon-key))}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http-client {:path "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200

  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :anon-key))}
          :host "127.0.0.1",
          :port "55121"})
        (fetch/request-http {:path "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200

  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :anon-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> docker-min/+config+ :api :anon-key)))
                    "Content-Profile" "scratch_v0"
                    "Content-Type" "application/json"}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http-client {:path "/rest/v1/rpc/ping"
                                       :method "POST"
                                       :body "{}"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" "\"pong\"", "status" 200, "headers" {}}  

  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :service-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> docker-min/+config+ :api :service-key)))
                    "Content-Profile" "scratch_v0"
                    "Content-Type" "application/json"}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http-client {:path "/rest/v1/rpc/log_append_public"
                                       :method "POST"
                                       :body "{\"i_message\": \"hello\"}"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (xt/x:json-decode (. out body)))))))
  => (contains-in
      {"message" "hello",
       "author_id" nil,
       "id" string?}))

^{:refer js.net.fetch/create-methods :added "4.1"}
(fact "creates the wrapper methods for fetch"

  (!.js
    (tree/tree-get-spec
     (js-fetch/create-methods)))
  => {"request_http" "function"})

^{:refer js.net.fetch/create :added "4.1"}
(fact "creates the wrapper for fetching"
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :service-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> docker-min/+config+ :api :service-key)))}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http-client {:path "/auth/v1/admin/users"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" "{\"users\":[],\"aud\":\"authenticated\"}", "status" 200, "headers" {}})
