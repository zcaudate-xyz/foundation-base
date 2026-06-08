(ns xt.net.lib-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]
             [postgres.core :as pg]]
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :shutdown docker-min/stop-supabase}})

(l/script :js
  {:runtime :basic
   :require [[xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.lib-supabase :as lib-supabase]
             [js.net.http-fetch :as js-fetch]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})


^{:refer xt.net.lib-supabase/create-http :added "4.1"}
(fact "creates a fetch client that works with the supabase api"

  (!.js
    (lib-supabase/create-http nil
                              (js-fetch/create-methods)
                              (@! (-> docker-min/+config+ :api :hostname))
                              (@! (-> docker-min/+config+ :api :port))
                              false
                              (@! (-> docker-min/+config+ :api :base-url))
                              (@! (-> docker-min/+config+
                                      :api
                                      :anon-key)))))

^{:refer xt.net.lib-supabase/signup :added "4.1"}
(fact "sign up to the "
  
  (notify/wait-on :js
    (-> (lib-supabase/create-http nil
                                  (js-fetch/create-methods)
                                  (@! (-> docker-min/+config+ :api :hostname))
                                  (@! (-> docker-min/+config+ :api :port))
                                  false
                                  (@! (-> docker-min/+config+ :api :base-url))
                                  (@! (-> docker-min/+config+
                                          :api
                                          :anon-key)))
        (lib-supabase/signup {:email "alice1@example.com"
                              :password "123456789"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => {"body" "{\"access_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzOTQxYzhjZi05YWFlLTQxODctYTk0Zi1jYjNkMmQ3NmIzODgiLCJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzgwOTA3OTM3LCJpYXQiOjE3ODA5MDQzMzcsImVtYWlsIjoiYWxpY2UxQGV4YW1wbGUuY29tIiwicGhvbmUiOiIiLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCIsInByb3ZpZGVycyI6WyJlbWFpbCJdfSwidXNlcl9tZXRhZGF0YSI6eyJlbWFpbCI6ImFsaWNlMUBleGFtcGxlLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicGhvbmVfdmVyaWZpZWQiOmZhbHNlLCJzdWIiOiIzOTQxYzhjZi05YWFlLTQxODctYTk0Zi1jYjNkMmQ3NmIzODgifSwicm9sZSI6ImF1dGhlbnRpY2F0ZWQiLCJhYWwiOiJhYWwxIiwiYW1yIjpbeyJtZXRob2QiOiJwYXNzd29yZCIsInRpbWVzdGFtcCI6MTc4MDkwNDMzN31dLCJzZXNzaW9uX2lkIjoiMWQyN2JjYzctZTM4ZS00ZjAyLTg3NzYtYzc3OGJiMjhkMGJiIiwiaXNfYW5vbnltb3VzIjpmYWxzZX0.gu_AlyftI4O5iWs5a3fqPZJHFOjTl6Us6FdFr4H0iXw\",\"token_type\":\"bearer\",\"expires_in\":3600,\"expires_at\":1780907937,\"refresh_token\":\"Nluc9afw9URahP6RL1imNA\",\"user\":{\"id\":\"3941c8cf-9aae-4187-a94f-cb3d2d76b388\",\"aud\":\"authenticated\",\"role\":\"authenticated\",\"email\":\"alice1@example.com\",\"email_confirmed_at\":\"2026-06-08T07:38:57.405741419Z\",\"phone\":\"\",\"last_sign_in_at\":\"2026-06-08T07:38:57.408895669Z\",\"app_metadata\":{\"provider\":\"email\",\"providers\":[\"email\"]},\"user_metadata\":{\"email\":\"alice1@example.com\",\"email_verified\":false,\"phone_verified\":false,\"sub\":\"3941c8cf-9aae-4187-a94f-cb3d2d76b388\"},\"identities\":[{\"identity_id\":\"536a590c-dc7c-4bd3-95ad-1614a83edc9c\",\"id\":\"3941c8cf-9aae-4187-a94f-cb3d2d76b388\",\"user_id\":\"3941c8cf-9aae-4187-a94f-cb3d2d76b388\",\"identity_data\":{\"email\":\"alice1@example.com\",\"email_verified\":false,\"phone_verified\":false,\"sub\":\"3941c8cf-9aae-4187-a94f-cb3d2d76b388\"},\"provider\":\"email\",\"last_sign_in_at\":\"2026-06-08T07:38:57.403377294Z\",\"created_at\":\"2026-06-08T07:38:57.403407Z\",\"updated_at\":\"2026-06-08T07:38:57.403407Z\",\"email\":\"alice1@example.com\"}],\"created_at\":\"2026-06-08T07:38:57.400289Z\",\"updated_at\":\"2026-06-08T07:38:57.411107Z\",\"is_anonymous\":false}}",
      "status" 200,
      "headers" {}}
  
  
  )

^{:refer xt.net.lib-supabase/signin-with-password :added "4.1"}
(fact "TODO")

(comment
  (xt.lang.common-notify/wait-on :js
    (-> (js-fetch/create
         {:secured false
          :host "127.0.0.1"
          :port "55121"
          :headers {"apikey" (@! (-> docker-min/+config+
                                     :api
                                     :anon-key)
                              #_(-> docker-min/+config+
                                    :api
                                    :service-key))
                    "Content-Type" "application/json"}
          :basepath "/auth/v1"})
        (fetch/request-http {:method "POST"
                             :path "/signup"
                             :body (xt/x:json-encode
                                    {:email "alice@example.com"
                                     :password "123456789"})})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out))))))

(comment
  (xt.lang.common-notify/wait-on :js
    (-> (js-fetch/create
         {:secured false
          :host "127.0.0.1"
          :port "55121"
          :headers {"apikey" (@! #_(-> docker-min/+config+
                                       :api
                                       :anon-key)
                              (-> docker-min/+config+
                                  :api
                                  :service-key))
                    "Content-Type" "application/json"}
          :basepath "/auth/v1"})
        (fetch/request-http {:method "POST"
                             :path "/signup"
                             :body (xt/x:json-encode
                                    {:email "alice@example.com"
                                     :password "123456789"})})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out))))))



(comment

  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU",
  (fetch "http://127.0.0.1:55121/auth/v1/signup"))



^{:refer xt.net.lib-supabase/sign-up :added "4.1"}
(fact "TODO")


(!.js
  (-> (js-fetch/create
       {:secured false
        :host "127.0.0.1"
        :port "55121"
        :headers {"apikey" (@! (-> docker-min/+config+
                                   :api
                                   :anon-key))
                  "Content-Type" "application/json"}
        :basepath "/auth/v1"})
      (fetch/prepare-url {:method "POST"
                           :path  "/signup"
                            :body (xt/x:json-encode
                                   {:email "alice@example.com"
                                    :password "123456789"})})
      #_(fetch/prepare-input {:method "POST"
                            :path "signup"
                            :body (xt/x:json-encode
                                   {:email "alice@example.com"
                                    :password "123456789"})})))

