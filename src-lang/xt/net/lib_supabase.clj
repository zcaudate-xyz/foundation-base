(ns xt.net.lib-supabase
  (:require [hara.lang :as l]
            [scaffold.supabase.event-host-util :as live]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]]})

(l/script :js
  {:runtime :basic
   :require [[xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(defn.xt create-http
  [type methods host port secured apikey]
  (return
   ()))


(comment
  (live/refresh-live-supabase-config!)
  {"::" "db.supabase",
   "client"
   {"base_url" "http://127.0.0.1:55121",
    "schema_name" "scratch",
    "api_key"
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU",
    "auth_token"
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU"}})

(def +token+
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImV4cCI6MTk4MzgxMjk5Nn0.EGIM96RAZx35lJzdJsyH-qQwv8Hdp7fsn3W0YpN81IU")
(defn.xt sign-up
  [client])


(comment
  (xt.lang.common-notify/wait-on :js
    (-> (js-fetch/create
         {:secured false
          :host "127.0.0.1"
          :port "55121"
          :headers {"apikey" (@! +token+)
                    "Content-Type" "application/json"}
          :basepath "/auth/v1"})
        (fetch/request-http {:method "POST"
                             :path "signup"
                             :body (xt/x:json-encode
                                    {:email "alice@example.com"
                                     :password "123456789"})})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out))))))

