(ns xt.db.node.example-auth-profile-view
  "Target-neutral substrate view for the auth/profile example."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.example-auth-profile :as example]]})

(def$.xt VIEW_ID "example/auth-profile")

(defn.xt make-binding
  [source space-id path opts]
  (return (xt/x:obj-assign {"source" source
                            "space_id" space-id
                            "path" (or path [])}
                           (or opts {}))))

(defn.xt spec
  "declares all substrate-owned state observed by the auth view"
  [space-id group-id]
  (:= space-id (or space-id example/DEFAULT_SPACE_ID))
  (:= group-id (or group-id example/DEFAULT_GROUP_ID))
  (return
   (view/view-spec
    -/VIEW_ID
    {"email" (-/make-binding "state" space-id ["email"] {"default" ""})
     "password" (-/make-binding "state" space-id ["password"] {"default" ""})
     "display_name" (-/make-binding "state" space-id ["display_name"] {"default" ""})
     "pending" (-/make-binding "state" space-id ["pending"] {})
     "error" (-/make-binding "state" space-id ["error"] {})
     "session" (-/make-binding "model-output" space-id []
                           {"group_id" group-id "model_id" "session"})
     "profile" (-/make-binding "model-output" space-id []
                           {"group_id" group-id "model_id" "profile"})}
    nil)))

(defn.xt action-id [suffix]
  (return (xt/x:cat "@/view/auth-profile/" suffix)))

(defn.xt input
  [id label value action-id disabled]
  (return
   (view/node "ui/column" {"class" "flex flex-col gap-1.5"}
              [(view/node "ui/label" {"value" label "for" id} [])
               (view/node "ui/input"
                          {"id" id
                           "value" (or value "")
                           "disabled" (== true disabled)
                           "on_change" (view/action action-id
                                                    (view/event-value ["value"]))}
                          [])])))

(defn.xt button
  [label action-id disabled pending]
  (return
   (view/node "ui/button"
              {"disabled" (== true disabled)
               "pending" (== true pending)
               "on_press" (view/action action-id nil)}
              [label])))

(defn.xt render
  "projects one substrate snapshot into serializable semantic view IR"
  [snapshot]
  (var session (. snapshot ["session"]))
  (var profile-output (. snapshot ["profile"]))
  (var profile (:? (xt/x:not-nil? profile-output)
                   (or (. profile-output ["user"]) profile-output)
                   nil))
  (var current-user (or profile
                        (:? (xt/x:not-nil? session)
                            (. session ["user"])
                            nil)))
  (var signed-in (xt/x:not-nil? session))
  (var pending (. snapshot ["pending"]))
  (var error (. snapshot ["error"]))
  (var auth-card
       (view/node
        "ui/card" {"class" "flex flex-col gap-3.5 p-5"}
        [(-/input "email" "Email" (. snapshot ["email"])
                   (-/action-id "set-email") signed-in)
         (-/input "password" "Password" (. snapshot ["password"])
                   (-/action-id "set-password") signed-in)
         (view/node
          "ui/row" {"class" "flex flex-row gap-2.5"}
          [(-/button "Create account" (-/action-id "sign-up")
                      (or signed-in (xt/x:not-nil? pending)) (== pending "sign-up"))
           (-/button "Sign in" (-/action-id "login")
                      (or signed-in (xt/x:not-nil? pending)) (== pending "login"))
           (-/button "Sign out" (-/action-id "logout")
                      (or (not signed-in) (xt/x:not-nil? pending)) (== pending "logout"))])
         (:? error
             (view/node "ui/alert" {"variant" "destructive"} [error])
             nil)]))
  (var profile-card
       (view/node
        "ui/card" {"class" "flex flex-col gap-3.5 p-5"}
        [(view/node "ui/title" {"value" "Current session"} [])
         (view/node "ui/text"
                    {"value" (:? signed-in
                                  (or (. current-user ["email"])
                                      "Authenticated user")
                                  "Not signed in")}
                    [])
         (-/input "display-name" "Display name"
                   (. snapshot ["display_name"])
                   (-/action-id "set-display-name") (not signed-in))
         (-/button "Save profile" (-/action-id "change-profile")
                    (or (not signed-in) (xt/x:not-nil? pending))
                    (== pending "change-profile"))]))
  (return
   (view/node
    "ui/column" {"class" "auth-profile flex flex-col gap-4 p-6"}
    [(view/node "ui/title" {"value" "Supabase auth profile"} [])
     (view/node "ui/description"
                {"value" "Authentication and profile state are owned by xt.substrate."}
                [])
     auth-card
     profile-card])))

(defn.xt error-message
  [err]
  (return (or (xt/x:ex-message err) (xt/x:to-string err))))

(defn.xt refresh-visible
  [node space-id group-id]
  (return
   (-> (page-core/model-refresh node space-id group-id "session" {} nil)
       (promise/x:promise-then
        (fn [_]
          (if (xt/x:not-nil?
               (page-core/model-get-output node space-id group-id "session"))
            (return (page-core/model-refresh node space-id group-id "profile" {} nil))
            (return nil)))))))

(defn.xt run-model
  [node space-id group-id model-id event]
  (view/state-set node space-id -/VIEW_ID ["pending"] model-id)
  (view/state-set node space-id -/VIEW_ID ["error"] nil)
  (return
   (-> (page-core/model-refresh node space-id group-id model-id event nil)
       (promise/x:promise-then
        (fn [_]
          (return (-/refresh-visible node space-id group-id))))
       (promise/x:promise-then
        (fn [output]
          (view/state-set node space-id -/VIEW_ID ["pending"] nil)
          (return output)))
       (promise/x:promise-catch
        (fn [err]
          (view/state-set node space-id -/VIEW_ID ["pending"] nil)
          (view/state-set node space-id -/VIEW_ID ["error"] (-/error-message err))
          (return nil))))))

(defn.xt register-state-handler
  [node action-id space-id path]
  (substrate/register-handler
   node action-id
   (fn [_space args _frame local-node]
     (return (view/state-set local-node space-id -/VIEW_ID path (xt/x:first args))))
   {"view_id" -/VIEW_ID})
  (return action-id))

(defn.xt install
  "installs substrate state and handler functionality required by the view"
  [node opts]
  (:= opts (or opts {}))
  (var space-id (or (. opts ["space_id"]) example/DEFAULT_SPACE_ID))
  (var group-id (or (. opts ["group_id"]) example/DEFAULT_GROUP_ID))
  (-/register-state-handler node (-/action-id "set-email") space-id ["email"])
  (-/register-state-handler node (-/action-id "set-password") space-id ["password"])
  (-/register-state-handler node (-/action-id "set-display-name") space-id ["display_name"])
  (substrate/register-handler
   node (-/action-id "sign-up")
   (fn [_space _args _frame local-node]
     (var credentials
          {"email" (view/state-get local-node space-id -/VIEW_ID ["email"] "")
           "password" (view/state-get local-node space-id -/VIEW_ID ["password"] "")})
     (return (-/run-model local-node space-id group-id "sign-up"
                          {"credentials" credentials})))
   {"view_id" -/VIEW_ID})
  (substrate/register-handler
   node (-/action-id "login")
   (fn [_space _args _frame local-node]
     (var credentials
          {"email" (view/state-get local-node space-id -/VIEW_ID ["email"] "")
           "password" (view/state-get local-node space-id -/VIEW_ID ["password"] "")})
     (return (-/run-model local-node space-id group-id "login"
                          {"credentials" credentials})))
   {"view_id" -/VIEW_ID})
  (substrate/register-handler
   node (-/action-id "logout")
   (fn [_space _args _frame local-node]
     (return (-/run-model local-node space-id group-id "logout" {})))
   {"view_id" -/VIEW_ID})
  (substrate/register-handler
   node (-/action-id "change-profile")
   (fn [_space _args _frame local-node]
     (var display-name
          (view/state-get local-node space-id -/VIEW_ID ["display_name"] ""))
     (return (-/run-model local-node space-id group-id "change-profile"
                          {"profile" {"display_name" display-name}})))
   {"view_id" -/VIEW_ID})
  (view/state-set node space-id -/VIEW_ID ["email"]
                  (or (. opts ["email"]) ""))
  (view/state-set node space-id -/VIEW_ID ["password"]
                  (or (. opts ["password"]) "secret123"))
  (view/state-set node space-id -/VIEW_ID ["display_name"]
                  (or (. opts ["display_name"]) "Playground User"))
  (return (-/spec space-id group-id)))
