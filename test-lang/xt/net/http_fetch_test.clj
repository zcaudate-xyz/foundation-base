(ns xt.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.event-host-util :as live]))

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
  => {"url" "http://127.0.0.1:55121/auth/v1/sign-in",
      "body" "", "method" "GET",
      "headers" {"apikey" "TOKEN", "Content-Type" "application/json"}})

^{:refer xt.net.http-fetch/create-base :added "4.1"}
(fact "creates the http base"

  (!.js
    (fetch/create-base "js.net.http-fetch"
                       (js-fetch/create-methods)))
  => {"::" "js.net.http-fetch"})

^{:refer xt.net.http-fetch/request-http :added "4.1"}
(fact "TODO"
  
  (notify/wait-on :js
    (-> (fetch/create-base "js.net.http-fetch"
                           (js-fetch/create-methods))
        (fetch/request-http {"url" "http://127.0.0.1:55121/auth/v1/sign-in",
                             "method" "POST"
                             "body" (xt/x:json-encode
                                     {"email" "a@oeue.com"
                                      "password" "12345678"})
                             "headers" {"apikey" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU", "Content-Type" "application/json"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify [(. out status)
                         (. out message)])))))
  => {"::" "js.net.http-fetch"}

  (notify/wait-on :js
    (-> (fetch "http://127.0.0.1:55121/auth/v1/sign-in",
               )
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out status))))))

  (notify/wait-on :js
    (-> (fetch "https://www.google.com",
               )
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out status)))))))

(comment
  (live/refresh-live-supabase-config!)
  {"::" "db.supabase", "client" {"base_url" "http://127.0.0.1:55121", "schema_name" "scratch", "api_key" "", "auth_token" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU"}})
