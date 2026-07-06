(ns xt.db.node.example-auth-profile-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.js install-fake-supabase-handlers
  "installs in-memory Supabase auth handlers for the example models"
  {:added "4.1"}
  [node]
  (substrate/set-service node "auth/supabase" {"state" {"session" nil}})
  (substrate/register-handler
   node
   "@xt.supabase/sign-in"
   (fn [space args request node]
     (var service-id (xt/x:first args))
     (var credentials (xt/x:second args))
     (var impl (substrate/get-service node service-id))
     (var user {"email" (xt/x:get-key credentials "email")
                "user_metadata" {}})
     (var next-session {"access_token" "token-1"
                        "refresh_token" "refresh-1"
                        "user" user})
     (session/set-session impl next-session)
     (return next-session))
   nil)
  (substrate/register-handler
   node
   "@xt.supabase/sign-out"
   (fn [space args request node]
     (var service-id (xt/x:first args))
     (session/set-session (substrate/get-service node service-id) nil)
     (return {"status" "ok"}))
   nil)
  (substrate/register-handler
   node
   "@xt.supabase/current-session"
   (fn [space args request node]
     (var service-id (xt/x:first args))
     (return (session/get-session (substrate/get-service node service-id))))
   nil)
  (substrate/register-handler
   node
   "@xt.supabase/user-get"
   (fn [space args request node]
     (var service-id (xt/x:first args))
     (return {"user" (xtd/get-in (substrate/get-service node service-id)
                                  ["state" "session" "user"])}))
   nil)
  (substrate/register-handler
   node
   "@xt.supabase/user-put"
   (fn [space args request node]
     (var service-id (xt/x:first args))
     (var data (xt/x:second args))
     (var impl (substrate/get-service node service-id))
     (var curr-user (xtd/get-in impl ["state" "session" "user"]))
     (var updated-user (xt/x:obj-assign (xt/x:obj-clone curr-user)
                                        {"user_metadata" (xt/x:get-key data "data")}))
     (return updated-user))
   nil)
  (return node))

^{:refer xt.db.node.example-auth-profile/auth-profile-models :added "4.1"}
(fact "returns the auth/profile example model specs"

  (!.js
   (var models (example/auth-profile-models "auth/supabase"))
   [(xt/x:is-function? (xtd/get-in models ["session" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["profile" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["login" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["logout" "handler"]))
    (xt/x:is-function? (xtd/get-in models ["change-profile" "handler"]))])
  => [true true true true true])

^{:refer xt.db.node.example-auth-profile-test/install-fake-supabase-handlers :added "4.1"}
(fact "fake handlers support the Supabase client API"

  (notify/wait-on [:js 10000]
    (var node (substrate/node-create {}))
    (-/install-fake-supabase-handlers node)
    (-> (substrate/request node
                           nil
                           "@xt.supabase/sign-in"
                           ["auth/supabase" {"email" "person@example.com"} {}]
                           {})
        (promise/x:promise-then
         (fn [_]
           (return (substrate/request node
                                      nil
                                      "@xt.supabase/current-session"
                                      ["auth/supabase"]
                                      {}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify (xtd/get-in out ["user" "email"]))))))
  => "person@example.com")

^{:refer xt.db.node.example-auth-profile/attach-auth-profile-models :added "4.1"}
(fact "logs in, updates auth profile metadata and logs out through page models"

  (notify/wait-on [:js 10000]
    (var credentials {"email" "person@example.com" "password" "secret123"})
    (var node (substrate/node-create {}))
    (-/install-fake-supabase-handlers node)
    (example/attach-auth-profile-models node
                                        "auth/supabase"
                                        {"space_id" "room/a"
                                         "group_id" "auth"})
    (-> (page-core/model-refresh node
                                    "room/a"
                                    "auth"
                                    "login"
                                    {"credentials" credentials}
                                    nil)
        (promise/x:promise-then
         (fn [_]
           (return (page-core/model-refresh node "room/a" "auth" "session" {} nil))))
        (promise/x:promise-then
         (fn [_]
           (return (page-core/model-refresh node "room/a" "auth" "profile" {} nil))))
        (promise/x:promise-then
         (fn [_]
           (return (page-core/model-refresh node
                                              "room/a"
                                              "auth"
                                              "change-profile"
                                              {"profile" {"display_name" "Test User"}}
                                              nil))))
        (promise/x:promise-then
         (fn [_]
           (return (page-core/model-refresh node "room/a" "auth" "profile" {} nil))))
        (promise/x:promise-then
         (fn [_]
           (var profile-output (page-core/model-get-output node "room/a" "auth" "profile"))
           (return
            (-> (page-core/model-refresh node "room/a" "auth" "logout" {} nil)
                (promise/x:promise-then
                 (fn [_]
                   (return (page-core/model-refresh node "room/a" "auth" "session" {} nil))))
                (promise/x:promise-then
                 (fn [_]
                   (var session-output (page-core/model-get-output node "room/a" "auth" "session"))
                   (repl/notify {"email" (xtd/get-in profile-output ["user" "email"])
                                 "display_name" (xtd/get-in profile-output ["user" "user_metadata" "display_name"])
                                 "session" session-output})))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)
                         "value" (xt/x:to-string err)})))))
  => {"email" "person@example.com"
      "display_name" "Test User"
      "session" nil})
