(ns lib.supabase-test
  (:require [lib.supabase :as s]
            [lib.supabase.support :as support]
            [net.http :as http])
  (:use code.test))

(fact:global
  {:setup [(support/start!)]
   :teardown [(support/stop!)]})

^{:refer lib.supabase/api-call :added "4.1.4"}
(fact "calls an api"
  (with-redefs [http/post (fn [_ _] {:status 200 :body "{\"ok\":true}"})]
    (s/api-call {:key "key"} {}))
  => {:status 200 :body {"ok" true}})

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
(fact "selects scratch table rows through postgrest against local docker supabase"
  (let [name "copilot_lib_supabase_select"
        _ (support/seed-entry! name ["copilot" "supabase" "select"])]
    (try
      (->> (s/api-select-all (atom {:id 'Entry :static/schema "scratch"})
                             (support/service-opts))
           :body
           (filter #(= name (get % "name")))
           first
           ((juxt #(get % "name") #(get % "tags"))))
      (finally
        (support/clear-entry! name))))
  => ["copilot_lib_supabase_select" ["copilot" "supabase" "select"]])

^{:refer lib.supabase/api-rpc :added "4.1.4"}
(fact "calls a scratch schema rpc through local docker supabase"
  (-> (s/api-rpc (merge (support/service-opts)
                        {:fn (atom {:id 'echo_name :static/schema "scratch"})
                         :args {"input" "copilot-rpc"}}))
      :body)
  => [{"name" "copilot-rpc"}])

^{:refer lib.supabase/connect :added "4.1.4"}
(fact "connects a native realtime websocket against local docker supabase"
  (let [client (support/service-client)]
    (try
      [(instance? java.net.http.WebSocket (s/connect client))
       (s/connected? client)]
      (finally
        (s/disconnect client))))
  => [true true])
