(ns xt.db.node.example-auth-profile-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.env :as env]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true
                 :langs [:js :lua.nginx :python :dart :ruby]}}
(l/script- :js
  {:runtime :basic
   :test-mode true
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.client-supabase :as supabase-client]]})

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :test-mode true
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [lua.nginx.common-promise]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.client-supabase :as supabase-client]]})

(defn.js local-client-defaults
  "returns client defaults for scaffold/supabase local-min"
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

(fact:global
 {:skip (not (env/program-exists? "supabase"))
  :setup [(local-min/start-supabase)
          (l/rt:restart)]
  :teardown [(l/rt:stop)
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

^{:refer xt.db.node.example-auth-profile/auth-profile-models.integration
  :added "4.1"
  :timeout 70000}
(fact "runs the auth/profile model handlers through local-min"

  (notify/wait-on [:js 60000]
    (var email (xt/x:cat "auth-profile-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var credentials {"email" email
                      "password" "secret123"})
    (var node (-/create-local-node))
    (xtd/set-in (substrate/get-service node "auth/supabase")
                 ["opts" "auto_refresh_interval"]
                 5000)
    (var models (example/auth-profile-models "auth/supabase"))
    (var context {"node" node})
    (var profile-output nil)
    (var sign-up (xtd/get-in models ["sign-up" "handler"]))
    (var logout (xtd/get-in models ["logout" "handler"]))
    (var login (xtd/get-in models ["login" "handler"]))
    (var session-get (xtd/get-in models ["session" "handler"]))
    (var profile-get (xtd/get-in models ["profile" "handler"]))
    (var profile-change (xtd/get-in models ["change-profile" "handler"]))
    (-> (sign-up context credentials)
        (promise/x:promise-then
         (fn [_]
           (return (logout context))))
        (promise/x:promise-then
         (fn [_]
           (return (login context credentials))))
        (promise/x:promise-then
         (fn [_]
           (return (session-get context))))
        (promise/x:promise-then
         (fn [_]
           (return (profile-get context))))
        (promise/x:promise-then
         (fn [_]
           (return (profile-change context {"display_name" "Test User"}))))
        (promise/x:promise-then
         (fn [_]
           (return (profile-get context))))
        (promise/x:promise-then
         (fn [output]
           (:= profile-output output)
           (return (logout context))))
        (promise/x:promise-then
         (fn [_]
           (return (session-get context))))
        (promise/x:promise-then
         (fn [session-output]
           (var impl (substrate/get-service node "auth/supabase"))
           (session/auto-refresh-stop impl)
           (repl/notify
            {"email" (xtd/get-in profile-output ["user" "email"])
             "display_name" (xtd/get-in profile-output
                                        ["user" "user_metadata" "display_name"])
             "session" session-output})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)
                         "value" (xt/x:to-string err)})))))
  => (contains {"email" string?
                "display_name" "Test User"
                "session" nil}))


^{:refer xt.db.node.example-auth-profile/first-arg :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/session-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/profile-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/sign-up-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/login-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/logout-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/change-profile-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.example-auth-profile/attach-auth-profile-models :added "4.1"}
(fact "TODO")