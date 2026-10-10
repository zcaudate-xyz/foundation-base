(ns js.lib.react-query-test
  (:require [clojure.data.json :as json]
            [lang.core :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.sample.scratch-v0 :as scratch-v0]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [js.react :as r]
             [js.lib.react-query :as rq]
             [js.lib.supabase :as sb]
             [js.react.helper-jsdom :as helper]]})

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]
             [postgres.core :as pg]
             [postgres.core.supabase :as s]]
   :config {:host (-> local-min/+config+ :db :host)
            :port (-> local-min/+config+ :db :port)
            :user (-> local-min/+config+ :db :user)
            :pass (-> local-min/+config+ :db :password)
            :dbname (-> local-min/+config+ :db :database)
            :startup local-min/start-supabase
            :shutdown local-min/stop-supabase}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

(def.js SupabaseConfig
  {"url" (xt/x:cat "http://" (@! (-> local-min/+config+ :api :hostname))
                   ":" (@! (-> local-min/+config+ :api :port)))
   "key" (@! (-> local-min/+config+ :api :service-key))
   "options" {"auth" {"persistSession" false
                       "autoRefreshToken" false
                       "detectSessionInUrl" false}}})

(def +public-message+ (str "react-query/public/" (random-uuid)))
(def +auth-message+ (str "react-query/auth/" (random-uuid)))

(def.js MutationMessages
  {"public-message" (@! +public-message+)
   "auth-message" (@! +auth-message+)})

(defn.js waitForOutput
  "waits for a settled output element in JSDOM"
  [document test-id]
  (return
   (new Promise
    (fn [resolve reject]
      (var attempts 0)
      (var poll
           (fn []
             (var selector (xt/x:cat "[data-testid='" test-id "']"))
             (var node (. document (querySelector selector)))
             (var value (:? node (. node ["textContent"]) ""))
             (if (and node (not= value "loading"))
               (resolve value)
               (do (:= attempts (+ attempts 1))
                   (if (> attempts 120)
                     (reject (new Error (xt/x:cat "Timed out waiting for " test-id)))
                     (setTimeout poll 25))))))
      (poll)))))

^{:refer js.lib.react-query-test/waitForOutput :added "4.1"}
(fact "waits for a settled JSDOM output"
  (helper-source/test
   (fn []
     (return (r/createElement "output"
                              {"data-testid" "value"}
                              "ready")))
   {}
   (fn [_ document _]
     (return (-/waitForOutput document "value"))))
  => "ready")

^{:refer js.lib.react-query/useApiQueriesSingle :added "4.1"}
(fact "resolves a real local Supabase ping query in JSDOM"
  (helper-source/test
   (fn []
     (var client (sb/createSupabaseClient
                  (. -/SupabaseConfig ["url"])
                  (. -/SupabaseConfig ["key"])
                  (. -/SupabaseConfig ["options"])))
     (var db (. client (schema "scratch_v0")))
     (var query-client (new rq/QueryClient
                            {"defaultOptions" {"queries" {"retry" false
                                                          "gcTime" 0}}}))
     (var App
          (fn []
            (var query
                 (rq/useApiQueriesSingle
                  ["ping" {"enabled" true
                           "fn" (fn [_]
                                  (return (. db (rpc "ping" {}))))}]))
            (return (r/createElement "output" {"data-testid" "value"}
                                     (or (. query ["output"]) "loading")))))
     (return (r/createElement rq/QueryClientProvider
                             {"client" query-client}
                             (r/createElement App))))
   {}
   (fn [_ document _]
     (return (-/waitForOutput document "value"))))
  => "pong")

^{:refer js.lib.react-query/useApiQueriesBase :added "4.1"}
(fact "loads independent echo and ping RPCs into a keyed query map"
  (helper-source/test
   (fn []
     (var client (sb/createSupabaseClient
                  (. -/SupabaseConfig ["url"])
                  (. -/SupabaseConfig ["key"])
                  (. -/SupabaseConfig ["options"])))
     (var db (. client (schema "scratch_v0")))
     (var query-client (new rq/QueryClient
                            {"defaultOptions" {"queries" {"retry" false
                                                          "gcTime" 0}}}))
     (var api
          {"queries"
           {"a-echo" {"enabled" true
                      "fn" (fn [_]
                             (return (. db (rpc "echo" {"i_input" "react-query-echo"}))))}
            "b-ping" {"enabled" true
                      "fn" (fn [_]
                             (return (. db (rpc "ping" {}))))}}})
     (var App
          (fn []
            (var queries (rq/useApiQueriesBase api))
            (var echo (. queries ["a-echo"]))
            (var ping (. queries ["b-ping"]))
            (var echo-output (. echo ["output"]))
            (var ping-output (. ping ["output"]))
            (var output (:? (and echo-output ping-output)
                            (JSON.stringify [echo-output ping-output])
                            "loading"))
            (return (r/createElement "output" {"data-testid" "value"} output))))
     (return (r/createElement rq/QueryClientProvider
                             {"client" query-client}
                             (r/createElement App))))
   {}
   (fn [_ document _]
     (return (-/waitForOutput document "value"))))
  => "[\"react-query-echo\",\"pong\"]")

^{:refer js.lib.react-query/useApiQueriesWire :added "4.1"}
(fact "passes the ping result into the dependent echo-plus RPC"
  (helper-source/test
   (fn []
     (var client (sb/createSupabaseClient
                  (. -/SupabaseConfig ["url"])
                  (. -/SupabaseConfig ["key"])
                  (. -/SupabaseConfig ["options"])))
     (var db (. client (schema "scratch_v0")))
     (var query-client (new rq/QueryClient
                            {"defaultOptions" {"queries" {"retry" false
                                                          "gcTime" 0}}}))
     (var api
          {"queries"
           {"ping" {"enabled" true
                    "fn" (fn [_]
                           (return (. db (rpc "ping" {}))))}
            "echo-plus" {"fn" (fn [input]
                                (return (. db
                                           (rpc "echo_plus"
                                                {"i_input" (. input ["i_input"])}))))
                         "deps" {"ping" {"key" "i_input"}}}}})
     (var App
          (fn []
            (var queries (rq/useApiQueriesBase api))
            (var wired (rq/useApiQueriesWire api queries))
            (var ping (. wired ["ping"]))
            (var echo (. wired ["echo-plus"]))
            (var ping-output (. ping ["output"]))
            (var echo-output (. echo ["output"]))
            (var output (:? (and ping-output echo-output)
                            (JSON.stringify [ping-output echo-output])
                            "loading"))
            (return (r/createElement "output" {"data-testid" "value"} output))))
     (return (r/createElement rq/QueryClientProvider
                             {"client" query-client}
                             (r/createElement App))))
   {}
   (fn [_ document _]
     (return (-/waitForOutput document "value"))))
  => "[\"pong\",\"pong-REMOTE\"]")

^{:refer js.lib.react-query/useApiQueries :added "4.1"}
(fact "runs composed queries against local Supabase RPCs"
  (helper-source/test
   (fn []
     (var client (sb/createSupabaseClient
                  (. -/SupabaseConfig ["url"])
                  (. -/SupabaseConfig ["key"])
                  (. -/SupabaseConfig ["options"])))
     (var db (. client (schema "scratch_v0")))
     (var query-client (new rq/QueryClient
                            {"defaultOptions" {"queries" {"retry" false
                                                          "gcTime" 0}}}))
     (var api
          {"queries"
           {"ping" {"enabled" true
                    "fn" (fn [_]
                           (return (. db (rpc "ping" {}))))}
            "echo-plus" {"fn" (fn [input]
                                (return (. db
                                           (rpc "echo_plus"
                                                {"i_input" (. input ["i_input"])}))))
                         "deps" {"ping" {"key" "i_input"}}}}})
     (var App
          (fn []
            (var queries (rq/useApiQueries api))
            (var ping (. queries ["ping"]))
            (var echo (. queries ["echo-plus"]))
            (var ping-output (. ping ["output"]))
            (var echo-output (. echo ["output"]))
            (var output (:? (and ping-output echo-output)
                            (JSON.stringify [ping-output echo-output])
                            "loading"))
            (return (r/createElement "output" {"data-testid" "value"} output))))
     (return (r/createElement rq/QueryClientProvider
                             {"client" query-client}
                             (r/createElement App))))
   {}
   (fn [_ document _]
     (return (-/waitForOutput document "value"))))
  => "[\"pong\",\"pong-REMOTE\"]")

^{:refer js.lib.react-query/useApi :added "4.1"}
(fact "runs both append RPC mutations and returns the inserted rows"
  (let [public-message +public-message+
        auth-message +auth-message+]
    (try
      (json/read-str
       (helper-source/test
       (fn [props]
         (var client (sb/createSupabaseClient
                      (. -/SupabaseConfig ["url"])
                      (. -/SupabaseConfig ["key"])
                      (. -/SupabaseConfig ["options"])))
         (var db (. client (schema "scratch_v0")))
         (var query-client (new rq/QueryClient
                                {"defaultOptions" {"queries" {"retry" false
                                                              "gcTime" 0}
                                                   "mutations" {"retry" false}}}))
         (var App
              (fn []
                (var [result setResult] (r/useState "loading"))
                (var api
                     {"queries"
                      {"ping" {"enabled" true
                               "fn" (fn [_]
                                      (return (. db (rpc "ping" {}))))}}
                      "mutations"
                      {"append-public" {"refresh" ["ping"]
                                        "fn" (fn [input]
                                               (return (. db (rpc "log_append_public" input))))}
                       "append-auth" {"refresh" ["ping"]
                                      "fn" (fn [input]
                                             (return (. db (rpc "log_append" input))))}}})
                (var state (rq/useApi api))
                (var mutations (. state ["mutations"]))
                (r/useEffect
                 (fn []
                   (-> (. Promise
                          (all [(. mutations ["append-public"]
                                   (mutateAsync {"i_message" (. props ["public-message"])}))
                                (. mutations ["append-auth"]
                                   (mutateAsync {"i_message" (. props ["auth-message"])}))]))
                       (promise/x:promise-then
                        (fn [responses]
                          (var rows
                               (xtd/arr-map responses
                                            (fn [response]
                                              (return (. response ["data"])))))
                          (setResult (JSON.stringify rows))))
                       (promise/x:promise-catch
                        (fn [error]
                          (setResult (xt/x:cat "error:" (. error ["message"]))))))
                   nil)
                 [])
                (return (r/createElement "output" {"data-testid" "value"} result))))
         (return (r/createElement rq/QueryClientProvider
                                 {"client" query-client}
                                 (r/createElement App))))
       -/MutationMessages
       (fn [_ document _]
         (return (-/waitForOutput document "value")))))
      => (contains-in
          [{"id" string? "message" public-message "author_id" nil}
           {"id" string? "message" auth-message "author_id" nil}])
      (finally
        (pg/t:delete scratch-v0/Log
                    {:where {:message public-message}})
        (pg/t:delete scratch-v0/Log
                    {:where {:message auth-message}})))))
