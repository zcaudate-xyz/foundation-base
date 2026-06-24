(ns xt.db.system.impl-supabase-test
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
   :require [[js.net.http-fetch :as js-fetch]
             [xt.net.http-fetch :as http-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

(defn.js default-client
  [apikey token]
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey apikey
     :token token}
    (addon/middleware-supabase))))

^{:refer xt.db.system.impl-supabase/normalise-body :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase/cmd-pull-async :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]} 
(fact "pulls data "

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/pull-async ["Log"])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      [{"message" "hello", "author_id" nil, "id" string?}])
  
  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/pull-async ["Log"])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      [{"author_id" nil,
        "id" string?
        "message" "hello"}]))

^{:refer xt.db.system.impl-supabase/cmd-rpc-call-async :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "performs an rpc call"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/rpc-call-async  {:input []
                               :return "text"
                               :schema "scratch_v0"
                               :id "ping"
                               :flags {}}
                              [])
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  => "pong"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/rpc-call-async  {:input [{:symbol "i_message" :type "text"}]
                               :return "jsonb"
                               :schema "scratch_v0"
                               :id "log_append_public"
                               :flags {}}
                              ["hello"]
                              {:token (@! (-> local-min/+config+ :api :service-key))})
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  => (contains-in
      {"author_id" nil,
       "id" string?
       "message" "hello"}))

^{:refer xt.db.system.impl-supabase/session-info :added "4.1"}
(fact "reads the current user for the active session"
  (notify/wait-on :js
    (var email (xt/x:cat "impl-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (http-fetch/request-http
         (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
         (addon/cmd-signup {"email" email
                            "password" "secret123"}
                           {}))
        (promise/x:promise-then
         (fn [signed-up]
           (var session (. signed-up ["body"]))
           (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                         (. session ["access_token"])))
           (var source (impl/impl-supabase
                        client
                        (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
                        (@! (pg/bind-app (pg/app "scratch_v0")))))
           (impl/set-session! source session)
           (-> (impl/session-info source)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(xt/x:get-key (. out ["user"]) "email")
                                (. (. out ["session"]) ["refresh_token"])])))
               (promise/x:promise-catch
                (fn [out]
                  (repl/notify out))))))))
  => (contains-in
      [string? string?]))

^{:refer xt.db.system.impl-supabase/refresh-session :added "4.1"}
(fact "refreshes the active session"
  (notify/wait-on :js
    (var email (xt/x:cat "impl-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (http-fetch/request-http
         (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
         (addon/cmd-signup {"email" email
                            "password" "secret123"}
                           {}))
        (promise/x:promise-then
         (fn [signed-up]
           (var session (. signed-up ["body"]))
           (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                         (. session ["access_token"])))
           (var source (impl/impl-supabase
                        client
                        (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
                        (@! (pg/bind-app (pg/app "scratch_v0")))))
           (impl/set-session! source session)
           (-> (impl/refresh-session source)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(. out ["access_token"])
                                (. out ["refresh_token"])
                                (. (. (. source ["client"]) ["defaults"]) ["token"])])))
               (promise/x:promise-catch
                (fn [out]
                  (repl/notify out))))))))
  => (contains-in
      [string? string? string?]))

^{:refer xt.db.system.impl-supabase/start-auto-refresh :added "4.1"}
(fact "starts the auto refresh timer"
  (notify/wait-on :js
    (var email (xt/x:cat "impl-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (http-fetch/request-http
         (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
         (addon/cmd-signup {"email" email
                            "password" "secret123"}
                           {}))
        (promise/x:promise-then
         (fn [signed-up]
           (var session (. signed-up ["body"]))
           (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                         (. session ["access_token"])))
           (var source (impl/impl-supabase
                        client
                        (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
                        (@! (pg/bind-app (pg/app "scratch_v0")))))
           (impl/set-session! source session)
           (impl/start-auto-refresh source {"interval" 10})
           (var started (xt/x:not-nil? (. (. (. source ["state"]) ["refresh"]) ["timer"])))
           (impl/stop-auto-refresh source)
           (var stopped (xt/x:nil? (. (. (. source ["state"]) ["refresh"]) ["timer"])))
           (repl/notify [started stopped])))))
  => [true true])

^{:refer xt.db.system.impl-supabase/impl-supabase :added "4.1"}
(fact "creates a supabase implementation"

  (!.js
    (impl/impl-supabase
     (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
     (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
     (@! (pg/bind-app (pg/app "scratch_v0")))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" {},
       "::" "xt.db.system.impl_supabase/ImplSupabase",
       "::/protocols" ["xt.db.system.impl_common/ISourceRemote"],
       "client" map?
       "state" {"session" nil?, "refresh" nil?, "pubsub" map?, "id_counter" 0}}))
