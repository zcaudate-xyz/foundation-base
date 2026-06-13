(ns xt.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(do 
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
    (s/grant-usage #{"scratch_v0"})))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]
             [xt.lang.common-protocol :as protocol]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.net.http-fetch :as fetch]
             [xt.lang.common-protocol :as proto]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.net.http-fetch :as fetch]
             [xt.lang.common-protocol :as proto]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.net.http-fetch/prepare-url :added "4.1"}
(fact "prepares the url for clients"

  (!.js
    (fetch/prepare-url
     {:defaults {:secured false
                 :host "127.0.0.1"
                 :port "55121"
                 :headers {"apikey" "TOKEN"
                           "Content-Type" "application/json"}
                 :basepath "/auth/v1"}}
     {:path "/sign-in"}))
  => "http://127.0.0.1:55121/auth/v1/sign-in")

^{:refer xt.net.http-fetch/prepare-input :added "4.1"}
(fact "prepares the input for clients"

  (!.js
    (fetch/prepare-input
     {:defaults {:secured false
                 :host "127.0.0.1"
                 :port "55121"
                 :headers {"apikey" "TOKEN"
                           "Content-Type" "application/json"}
                 :basepath "/auth/v1"}}
     {:path "/sign-in"}))
  => {"url" "http://127.0.0.1:55121/auth/v1/sign-in", "method" "GET", "headers" {"apikey" "TOKEN", "Content-Type" "application/json"}})

^{:refer xt.net.http-fetch/request-http :added "4.1"}
(fact "dispatches request through the wrapped fetch client"
  
  (notify/wait-on :js
    (-> (js-fetch/create
         {:headers {"apikey" (@! (-> docker-min/+config+ :api :anon-key))}
          :host "127.0.0.1"
          :port "55121"})
        (fetch/request-http {"path" "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify [(. out status)
                         (. out message)]))))))

^{:refer xt.net.http-fetch/then-normalise :added "4.1"}
(fact "normalises promise results through the shared fetch response envelope"

  (notify/wait-on :js
    (-> (promise/x:promise-run
         {"status" 200
          "headers" {"content-type" "application/json"}
          "body" "{\"id\":\"ord-1\"}"
          "error" nil})
        (fetch/then-normalise)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"status" 200
      "headers" {"content-type" "application/json"}
      "body" {"id" "ord-1"}
      "error" nil})
