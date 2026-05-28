(ns lib.supabase-test
  (:require [lib.supabase :as s]
            [lib.supabase.support :as support])
  (:use code.test))

(fact:global
  {:setup [(support/start!)]
   :teardown [(support/stop!)]})

^{:refer lib.supabase/api-call :added "4.1.4"}
(fact "calls a live api route against local docker supabase"
  (let [message (support/random-log-message)
        _ (support/seed-log! message)]
    (try
      (->> (s/api-call (merge (support/anon-opts)
                              {:group :rest
                               :method :get
                               :route "/Log?select=message"
                               :headers {"Content-Profile" support/+scratch-v0-schema+}})
                       {})
           :body
           (filter #(= message (get % "message")))
           first
           (get "message"))
      (finally
        (support/clear-log! message))))
  => string?)

^{:refer lib.supabase/api-signup-create :added "4.1.4"}
(fact "creates an auth user and signs in against local docker supabase"
  (let [email (support/random-email)
        password "pass123456"
        created (s/api-signup-create {"email" email
                                      "password" password
                                      "email_confirm" true}
                                     (support/service-opts))
        uid (or (get-in created [:body "user" "id"])
                (get-in created [:body "id"]))]
    (try
      (let [signed-in (s/api-signin {"email" email
                                     "password" password}
                                    (support/anon-opts))]
        [(some? uid)
         (string? (get-in signed-in [:body "access_token"]))
         (get-in signed-in [:body "user" "email"])])
      (finally
        (when uid
          (s/api-signup-delete uid (support/service-opts))))))
  => #(and (= true (nth % 0))
           (= true (nth % 1))
           (string? (nth % 2))))

^{:refer lib.supabase/api-select-all :added "4.1.4"}
(fact "selects scratch-v0 log rows through postgrest against local docker supabase"
  (let [message (support/random-log-message)
        _ (support/seed-log! message)]
    (try
      (->> (s/api-select-all support/+log-table+
                             (support/anon-opts))
           :body
           (filter #(= message (get % "message")))
           first
           (get "message"))
      (finally
        (support/clear-log! message))))
  => string?)

^{:refer lib.supabase/api-rpc :added "4.1.4"}
(fact "calls a public scratch-v0 ping rpc through local docker supabase"
  (-> (s/api-rpc (merge (support/anon-opts)
                        {:fn support/+ping-fn+}))
      :body)
  => "pong")

^{:refer lib.supabase/api-rpc :added "4.1.4"}
(fact "calls the authenticated scratch-v0 log append rpc through local docker supabase"
  (let [{:keys [uid access-token]} (support/create-auth-session!)
        message (support/random-log-message)]
    (try
      (let [response (s/api-rpc (merge (support/auth-opts access-token)
                                       {:fn support/+log-append-fn+
                                        :args {"i_message" message}}))]
        [(:status response)
         (->> (support/list-logs)
              (filter #(= message (get % "message")))
              first
              (get "message"))])
      (finally
        (support/clear-log! message)
        (support/delete-auth-user! uid))))
  => [200 string?])

^{:refer lib.supabase/api-rpc :added "4.1.4"}
(fact "rejects anonymous access to the scratch-v0 log append rpc"
  (let [message (support/random-log-message)]
    (try
      (s/api-rpc (merge (support/anon-opts)
                        {:fn support/+log-append-fn+
                         :args {"i_message" message}}))
      (finally
        (support/clear-log! message))))
  => (throws clojure.lang.ExceptionInfo "Supabase API request failed"))

^{:refer lib.supabase/connect :added "4.1.4"}
(fact "connects a native realtime websocket against local docker supabase"
  (let [client (support/service-client)]
    (try
      [(instance? java.net.http.WebSocket (s/connect client))
       (s/connected? client)]
      (finally
        (s/disconnect client))))
  => [true true])
