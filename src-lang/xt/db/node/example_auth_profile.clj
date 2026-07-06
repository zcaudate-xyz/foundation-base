(ns xt.db.node.example-auth-profile
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
              [xt.lang.spec-promise :as promise]
              [xt.lang.common-data :as xtd]
              [xt.substrate :as substrate]
              [xt.substrate.page-core :as page-core]
              [xt.db.system.main :as db-main]
              [xt.db.system.impl-supabase-session :as session]
              [xt.db.node.kernel-supabase :as kernel-supabase]
              [xt.db.node.client-supabase :as supabase]]})

(def.xt DEFAULT_SPACE_ID "example/auth")

(def.xt DEFAULT_GROUP_ID "auth")

(def.xt DEFAULT_SERVICE_ID "auth/supabase")

(defn.xt first-arg
  "returns the first model argument from wrapper args or input/event data"
  {:added "4.1"}
  [ctx arg event-key]
  (var value (or arg
                 (xtd/get-in ctx ["event" event-key])
                 (xt/x:first (xt/x:get-key ctx "args"))
                 (xt/x:first (xtd/get-in ctx ["input" "data"]))
                 (xt/x:first (xtd/get-in ctx ["event" "data"]))))
  (when (and (xt/x:is-array? value)
             (== 1 (xt/x:len value)))
    (:= value (xt/x:first value)))
  (return value))

(defn.xt session-model
  "creates a model for the current Supabase session"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx]
                (var #{node} ctx)
                (return (supabase/current-session node service-id {})))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt profile-model
  "creates a model for the current Supabase auth user"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx]
                (var #{node} ctx)
                (return (supabase/user-get node service-id {})))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt sign-up-model
  "creates a model for password sign-up"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx credentials]
                (var #{node} ctx)
                (:= credentials (-/first-arg ctx credentials "credentials"))
                (return (supabase/sign-up node service-id credentials {})))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt login-model
  "creates a model for password login"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx credentials]
                (var #{node} ctx)
                (:= credentials (-/first-arg ctx credentials "credentials"))
                (return (supabase/sign-in node service-id credentials {})))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt logout-model
  "creates a model for logout"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx]
                (var #{node} ctx)
                (return
                 (-> (supabase/current-session node service-id {})
                     (promise/x:promise-then
                      (fn [curr-session]
                        (return
                         (supabase/sign-out node
                                            service-id
                                            {"token" (xt/x:get-key curr-session "access_token")})))))))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt change-profile-model
  "creates a model for updating Supabase auth user metadata"
  {:added "4.1"}
  [service-id]
  (return
   {"handler" (fn [ctx profile-data]
                (var #{node} ctx)
                (:= profile-data (-/first-arg ctx profile-data "profile"))
                (return
                 (-> (supabase/current-session node service-id {})
                     (promise/x:promise-then
                      (fn [curr-session]
                        (return
                         (supabase/user-put node
                                            service-id
                                            {"data" profile-data}
                                            {"token" (xt/x:get-key curr-session "access_token")}))))
                     (promise/x:promise-then
                      (fn [updated]
                        (var updated-user
                             (or (xt/x:get-key updated "user")
                                 updated))
                        (var impl (substrate/get-service node service-id))
                        (var curr-session (session/get-session impl))
                        (when curr-session
                          (session/set-session
                           impl
                           (xt/x:obj-assign (xt/x:obj-clone curr-session)
                                            {"user" updated-user})))
                        (return updated-user))))))
    "defaults" {"args" []}
    "options" {}}))

(defn.xt auth-profile-models
  "returns sign-up/login/logout/profile page models backed by Supabase auth"
  {:added "4.1"}
  [service-id]
  (:= service-id (or service-id -/DEFAULT_SERVICE_ID))
  (return
   {"session"        (-/session-model service-id)
    "profile"        (-/profile-model service-id)
    "sign-up"        (-/sign-up-model service-id)
    "login"          (-/login-model service-id)
    "logout"         (-/logout-model service-id)
    "change-profile" (-/change-profile-model service-id)}))

(defn.xt attach-auth-profile-models
  "attaches the auth/profile example models to a page group"
  {:added "4.1"}
  [node service-id page-args]
  (:= page-args (or page-args {}))
  (var space-id (or (xt/x:get-key page-args "space_id")
                    -/DEFAULT_SPACE_ID))
  (var group-id (or (xt/x:get-key page-args "group_id")
                    -/DEFAULT_GROUP_ID))
  (page-core/group-add-attach node
                              space-id
                              group-id
                              (-/auth-profile-models service-id))
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "models" ["session" "profile" "sign-up" "login" "logout" "change-profile"]}))

(defn.xt create-auth-profile-node
  "creates a substrate node with a Supabase service, handlers and auth/profile models"
  {:added "4.1"}
  [client-defaults service-id page-args]
  (:= service-id (or service-id -/DEFAULT_SERVICE_ID))
  (var node (substrate/node-create {}))
  (var impl (db-main/create-impl "supabase" client-defaults nil nil))
  (substrate/set-service node service-id impl)
  (kernel-supabase/init-handlers node)
  (-/attach-auth-profile-models node service-id page-args)
  (return node))
