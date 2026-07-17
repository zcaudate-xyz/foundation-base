(ns xt.db.node.example-auth-profile-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [xt.lang.common-notify :as notify]))

(require '[hara.runtime.js-playground :as js-playground]
         '[hara.runtime.chromedriver :as chromedriver])

(l/script- :js
  {:runtime :playground
   :config {:port 0}
   :test-mode true
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.node.example-auth-profile-ui :as ui]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.client-supabase :as supabase-client]
             [hara.runtime.js-playground.client :as client]]
   :emit {:native {:suppress true}
          :lang/jsx false}})

(defn.js local-client-defaults
  "returns browser client defaults for scaffold/supabase local-min"
  {:added "4.1"}
  []
  (return
   {:host (@! (-> local-min/+config+ :api :hostname))
    :port (@! (-> local-min/+config+ :api :port))
    :secured false
    :apikey (@! (-> local-min/+config+ :api :anon-key))}))

(defn.js create-local-node
  "creates the example node connected to scaffold/supabase local-min"
  {:added "4.1"}
  []
  (return
   (example/create-auth-profile-node
    (-/local-client-defaults)
    example/DEFAULT_SERVICE_ID
    {"space_id" "room/a"
     "group_id" "auth"})))

(defn- wait-for-channel
  "waits up to 5s for the playground websocket channel to be connected"
  [rt]
  (let [channel (:channel rt)]
    (loop [i 0]
      (when (and (< i 50) (not @channel))
        (Thread/sleep 100)
        (recur (inc i))))))

(fact:global
 {:setup [(local-min/start-supabase)
          (l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (def +url+ (str "http://127.0.0.1:" (:port (l/rt :js)) "/index.html"))
          (def +browser+ (chromedriver/browser {}))
          (chromedriver/goto +url+ 5000 +browser+)
          (wait-for-channel (l/rt :js))]
  :teardown [(component/stop +browser+)
             (l/rt:stop)
             (local-min/stop-supabase nil)]})

^{:refer xt.db.node.example-auth-profile/auth-profile-models :added "4.1"}
(fact "returns the auth/profile example model specs"

  (!.js
   (var models (example/auth-profile-models "auth/supabase"))
   [(xt/x:is-function? (xtd/get-in models ["session" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["profile" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["sign-up" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["login" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["logout" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["change-profile" "handler"]))])
  => [true true true true true true])

^{:refer xt.db.node.example-auth-profile/create-auth-profile-node :added "4.1"}
(fact "creates a local-min node with the Supabase service and handlers"

  (!.js
   (var node (-/create-local-node))
   (var [_group profile-model]
        (page-core/model-ensure node "room/a" "auth" "profile"))
   [(xt/x:not-nil? (substrate/get-service node "auth/supabase"))
    (xt/x:not-nil? (xt/x:get-key
                    (xt/x:get-key node "handlers")
                    "@xt.supabase/sign-up"))
    (xt/x:not-nil? profile-model)])
  => [true true true])

^{:refer xt.db.node.example-auth-profile-test/notify-canary :added "4.1"}
(fact "notify works in playground"

  (notify/wait-on [:js 5000]
    (promise/x:with-delay 100
      (fn [] (repl/notify "ok"))))
  => "ok")

^{:refer xt.db.node.example-auth-profile-test/typeof-fetch :added "4.1"}
(fact "fetch is defined in playground"

  (!.js (typeof fetch))
  => "function")

^{:refer xt.db.node.example-auth-profile-test/raw-fetch-health :added "4.1"}
(fact "raw fetch to supabase health in playground"

  (notify/wait-on [:js 15000]
    (-> (fetch "http://127.0.0.1:55121/auth/v1/health"
               {"headers" {"apikey" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"}})
        (.then (fn [res]
                 (return (. res (text)))))
        (.then (fn [body]
                 (repl/notify ["ok" body])))
        (.catch (fn [err]
                  (repl/notify ["err" (String err)])))))
  => vector?)

^{:refer xt.db.node.example-auth-profile-test/raw-fetch-notify :added "4.1"}
(fact "raw fetch to notify server in playground"

  (notify/wait-on [:js 15000]
    (-> (fetch (str "http://127.0.0.1:" (@! (-> (std.lib.resource/res :hara/lang.notify) :http-port)) "/"))
        (.then (fn [res]
                 (return (. res (text)))))
        (.then (fn [body]
                 (repl/notify ["ok" body])))
        (.catch (fn [err]
                  (repl/notify ["err" (String err)])))))
  => vector?)

^{:refer xt.db.node.example-auth-profile/attach-auth-profile-models :added "4.1"}
(fact "signs up, signs back in, updates auth metadata and logs out through local-min"

  (notify/wait-on [:js 15000]
    (var email (xt/x:cat "auth-profile-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var credentials {"email" email
                      "password" "secret123"})
    (var node (-/create-local-node))
    (-> (page-core/model-refresh node
                                 "room/a"
                                 "auth"
                                 "sign-up"
                                 {"credentials" credentials}
                                 nil)
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh node
                                     "room/a"
                                     "auth"
                                     "logout"
                                     {}
                                     nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh node
                                     "room/a"
                                     "auth"
                                     "login"
                                     {"credentials" credentials}
                                     nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh node
                                     "room/a"
                                     "auth"
                                     "session"
                                     {}
                                     nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh node
                                     "room/a"
                                     "auth"
                                     "profile"
                                     {}
                                     nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh
             node
             "room/a"
             "auth"
             "change-profile"
             {"profile" {"display_name" "Test User"}}
             nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-core/model-refresh node
                                     "room/a"
                                     "auth"
                                     "profile"
                                     {}
                                     nil))))
        (promise/x:promise-then
         (fn [_]
           (var profile-output
                (page-core/model-get-output node
                                            "room/a"
                                            "auth"
                                            "profile"))
           (return
            (-> (page-core/model-refresh node
                                         "room/a"
                                         "auth"
                                         "logout"
                                         {}
                                         nil)
                (promise/x:promise-then
                 (fn [_]
                   (return
                    (page-core/model-refresh node
                                             "room/a"
                                             "auth"
                                             "session"
                                             {}
                                             nil))))
                (promise/x:promise-then
                 (fn [_]
                   (var session-output
                        (page-core/model-get-output node
                                                    "room/a"
                                                    "auth"
                                                    "session"))
                   (var impl
                        (substrate/get-service node "auth/supabase"))
                   (session/auto-refresh-stop impl)
                   (repl/notify
                    {"email" (xtd/get-in profile-output
                                         ["user" "email"])
                     "display_name" (xtd/get-in profile-output
                                                ["user"
                                                 "user_metadata"
                                                 "display_name"])
                     "session" session-output})))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)
                         "value" (xt/x:to-string err)})))))
  => {"email" string?
      "display_name" "Test User"
      "session" nil})

^{:refer xt.db.node.example-auth-profile-ui/mount-playground :added "4.1"}
(fact "mounts the interactive auth/profile UI in the playground stage"

  (notify/wait-on [:js 5000]
    (var node
         (ui/mount-playground
          (-/local-client-defaults)
          {"space_id" "room/ui"
           "group_id" "auth"}))
    (promise/x:with-delay
     200
     (fn []
       (var heading (. document (querySelector "h1")))
       (repl/notify
        [(xt/x:get-key heading "textContent")
         (xt/x:not-nil?
          (substrate/get-service node "auth/supabase"))]))))
  => ["Supabase auth profile" true])

(comment

  (local-min/start-supabase)
  (l/rt:restart :js)
  (def +url+ (js-playground/play-url (l/rt :js)))

  ;; Open +url+ in a browser, then evaluate:
  (!.js
   (ui/mount-playground
    (-/local-client-defaults)
    {"space_id" "room/playground"
     "group_id" "auth"}))

  (l/rt:stop))
