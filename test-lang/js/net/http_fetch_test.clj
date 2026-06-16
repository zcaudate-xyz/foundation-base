(ns js.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.fetch/request-http-raw :added "4.1"}
(comment "SKIPPED: requires Supabase local-min"

  (notify/wait-on :js
    (-> (js-fetch/request-http-raw {:url (fetch/prepare-url {:defaults {:host "127.0.0.1",
                                                                        :port "55121"}}
                                                            {:path "/auth/v1/health"})
                                    :headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))}} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200)

^{:refer js.net.fetch/request-http :added "4.1"}
(comment "SKIPPED: requires Supabase local-min"
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http {:path "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200

  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))}
          :host "127.0.0.1",
          :port "55121"})
        (fetch/request-http {:path "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200

  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> local-min/+config+ :api :anon-key)))
                    "Content-Profile" "scratch_v0"
                    "Content-Type" "application/json"}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http {:path "/rest/v1/rpc/ping"
                                       :method "POST"
                                       :body "{}"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" "\"pong\"", "status" 200, "headers" {}}  
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :service-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> local-min/+config+ :api :service-key)))
                    "Content-Profile" "scratch_v0"
                    "Content-Type" "application/json"}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http {:path "/rest/v1/rpc/log_append_public"
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

^{:refer js.net.fetch/create :added "4.1"}
(comment "SKIPPED: requires Supabase local-min"
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :service-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> local-min/+config+ :api :service-key)))}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http {:path "/auth/v1/admin/users"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"body" string?})
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))
                    "Authorization" (xt/x:cat "Bearer " (@! (-> local-min/+config+ :api :anon-key)))
                    "Accept-Profile" "scratch_v0"}
          :host "127.0.0.1",
          :port "55121"})
        (js-fetch/request-http {:path "/rest/v1/Log?select=id,message,author_id&order=id.desc&limit=20"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (xt/x:json-decode (. out body)))))))
  => (contains-in
      [{"message" "hello",
        "author_id" nil,
        "id" string?}]))



