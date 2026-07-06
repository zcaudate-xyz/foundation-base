(ns xt.db.node.example-auth-profile-ui
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.example-auth-profile :as example]
             [js.react :as r]]})

(defn.js error-message
  "returns a readable message for an async auth failure"
  {:added "4.1"}
  [err]
  (return (or (xt/x:ex-message err)
              (xt/x:to-string err))))

(defn.js refresh-auth-state
  "refreshes the session/profile models and updates the supplied React setters"
  {:added "4.1"}
  [node space-id group-id setSession setProfile]
  (return
   (-> (page-core/model-refresh node space-id group-id "session" {} nil)
       (promise/x:promise-then
        (fn [_]
          (var curr-session
               (page-core/model-get-output node space-id group-id "session"))
          (setSession curr-session)
          (if (xt/x:not-nil? curr-session)
            (return
             (-> (page-core/model-refresh node space-id group-id "profile" {} nil)
                 (promise/x:promise-then
                  (fn [_]
                    (var profile-output
                         (page-core/model-get-output node space-id group-id "profile"))
                    (setProfile (xtd/get-in profile-output ["user"]))
                    (return profile-output)))))
            (do (setProfile nil)
                (return nil))))))))

(defn.js run-action
  "runs a page-model action and refreshes the visible auth state"
  {:added "4.1"}
  [node space-id group-id model-id event
   setBusy setError setSession setProfile]
  (setBusy model-id)
  (setError nil)
  (return
   (-> (page-core/model-refresh node
                                space-id
                                group-id
                                model-id
                                (or event {})
                                nil)
       (promise/x:promise-then
        (fn [_]
          (return
           (-/refresh-auth-state node
                                 space-id
                                 group-id
                                 setSession
                                 setProfile))))
       (promise/x:promise-then
        (fn [out]
          (setBusy nil)
          (return out)))
       (promise/x:promise-catch
        (fn [err]
          (setBusy nil)
          (setError (-/error-message err))
          (return nil))))))

(defn.js field-style
  []
  (return {:display "flex"
           :flexDirection "column"
           :gap "6px"
           :fontSize "13px"
           :fontWeight 600
           :color "#374151"}))

(defn.js input-style
  []
  (return {:border "1px solid #d1d5db"
           :borderRadius "8px"
           :padding "10px 12px"
           :fontSize "14px"
           :fontWeight 400
           :outline "none"}))

(defn.js button-style
  [primary]
  (return {:border "1px solid"
           :borderColor (:? primary "#111827" "#d1d5db")
           :background (:? primary "#111827" "#ffffff")
           :color (:? primary "#ffffff" "#111827")
           :borderRadius "8px"
           :padding "9px 13px"
           :fontSize "13px"
           :fontWeight 600
           :cursor "pointer"}))

(defn.js AuthProfileApp
  "interactive auth/profile example backed by the attached page models"
  {:added "4.1"}
  [#{node space-id group-id initial-email}]
  (:= space-id (or space-id example/DEFAULT_SPACE_ID))
  (:= group-id (or group-id example/DEFAULT_GROUP_ID))
  (var [email setEmail]
       (r/local
        (or initial-email
            (xt/x:cat "auth-profile-"
                      (xt/x:to-string (xt/x:now-ms))
                      "@example.com"))))
  (var [password setPassword] (r/local "secret123"))
  (var [displayName setDisplayName] (r/local "Playground User"))
  (var [session setSession] (r/local nil))
  (var [profile setProfile] (r/local nil))
  (var [busy setBusy] (r/local nil))
  (var [error setError] (r/local nil))
  (var credentials {"email" email
                    "password" password})
  (var current-user (or profile
                        (xt/x:get-key session "user")))
  (var current-email (xt/x:get-key current-user "email"))
  (var current-name (xtd/get-in current-user
                                ["user_metadata" "display_name"]))
  (var signed-in (xt/x:not-nil? session))
  (r/init []
    (-/refresh-auth-state node
                          space-id
                          group-id
                          setSession
                          setProfile)
    (return nil))
  (return
   [:main
    {:style {:minHeight "100%"
             :boxSizing "border-box"
             :padding "32px"
             :background "#f3f4f6"
             :fontFamily "system-ui, sans-serif"
             :color "#111827"}}
    [:div
     {:style {:maxWidth "720px"
              :margin "0 auto"
              :display "flex"
              :flexDirection "column"
              :gap "18px"}}
     [:header
      [:div
       {:style {:fontSize "12px"
                :fontWeight 700
                :letterSpacing "0.08em"
                :textTransform "uppercase"
                :color "#6b7280"}}
       "foundation-base / local-min"]
      [:h1
       {:style {:margin "6px 0 4px"
                :fontSize "30px"}}
       "Supabase auth profile"]
      [:p
       {:style {:margin 0
                :color "#4b5563"
                :lineHeight 1.5}}
       "Create or sign in to a local account, then update the user's auth metadata through substrate page models."]]
     [:section
      {:style {:background "#ffffff"
               :border "1px solid #e5e7eb"
               :borderRadius "12px"
               :padding "20px"
               :display "flex"
               :flexDirection "column"
               :gap "14px"
               :boxShadow "0 8px 24px rgba(17,24,39,0.05)"}}
      [:label
       {:style (-/field-style)}
       "Email"
       [:input
        {:style (-/input-style)
         :value email
         :disabled signed-in
         :onChange (fn [event]
                     (setEmail (. event target value)))}]]
      [:label
       {:style (-/field-style)}
       "Password"
       [:input
        {:style (-/input-style)
         :type "password"
         :value password
         :disabled signed-in
         :onChange (fn [event]
                     (setPassword (. event target value)))}]]
      [:div
       {:style {:display "flex"
                :gap "10px"
                :flexWrap "wrap"}}
       [:button
        {:style (-/button-style true)
         :disabled (xt/x:not-nil? busy)
         :onClick (fn []
                    (-/run-action node
                                  space-id
                                  group-id
                                  "sign-up"
                                  {"credentials" credentials}
                                  setBusy
                                  setError
                                  setSession
                                  setProfile))}
        (:? (== busy "sign-up")
            "Creating..."
            "Create account")]
       [:button
        {:style (-/button-style false)
         :disabled (xt/x:not-nil? busy)
         :onClick (fn []
                    (-/run-action node
                                  space-id
                                  group-id
                                  "login"
                                  {"credentials" credentials}
                                  setBusy
                                  setError
                                  setSession
                                  setProfile))}
        (:? (== busy "login")
            "Signing in..."
            "Sign in")]
       [:button
        {:style (-/button-style false)
         :disabled (or (xt/x:not-nil? busy)
                       (not signed-in))
         :onClick (fn []
                    (-/run-action node
                                  space-id
                                  group-id
                                  "logout"
                                  {}
                                  setBusy
                                  setError
                                  setSession
                                  setProfile))}
        (:? (== busy "logout")
            "Signing out..."
            "Sign out")]]
      (:? error
          [:div
           {:style {:padding "10px 12px"
                    :borderRadius "8px"
                    :background "#fef2f2"
                    :color "#b91c1c"
                    :fontSize "13px"}}
           error]
          nil)]
     [:section
      {:style {:background "#ffffff"
               :border "1px solid #e5e7eb"
               :borderRadius "12px"
               :padding "20px"
               :display "flex"
               :flexDirection "column"
               :gap "14px"}}
      [:div
       {:style {:display "flex"
                :justifyContent "space-between"
                :gap "16px"
                :alignItems "flex-start"}}
       [:div
        [:h2
         {:style {:margin "0 0 4px"
                  :fontSize "18px"}}
         "Current session"]
        [:div
         {:style {:color "#6b7280"
                  :fontSize "13px"}}
         (:? signed-in
             (or current-email "Authenticated user")
             "Not signed in")]]
       [:span
        {:style {:padding "5px 9px"
                 :borderRadius "999px"
                 :fontSize "12px"
                 :fontWeight 700
                 :background (:? signed-in "#dcfce7" "#f3f4f6")
                 :color (:? signed-in "#166534" "#6b7280")}}
        (:? signed-in "signed in" "signed out")]]
      [:label
       {:style (-/field-style)}
       "Display name"
       [:input
        {:style (-/input-style)
         :value displayName
         :disabled (not signed-in)
         :onChange (fn [event]
                     (setDisplayName (. event target value)))}]]
      [:button
       {:style (-/button-style true)
        :disabled (or (xt/x:not-nil? busy)
                      (not signed-in))
        :onClick (fn []
                   (-/run-action node
                                 space-id
                                 group-id
                                 "change-profile"
                                 {"profile" {"display_name" displayName}}
                                 setBusy
                                 setError
                                 setSession
                                 setProfile))}
       (:? (== busy "change-profile")
           "Saving..."
           "Save profile")]
      (:? current-name
          [:div
           {:style {:fontSize "13px"
                    :color "#374151"}}
           "Stored display name: "
           [:strong current-name]]
          nil)]]]))

(defn.js render-playground
  "renders the auth/profile UI into the active playground stage"
  {:added "4.1"}
  [node opts]
  (:= opts (or opts {}))
  (var space-id (or (xt/x:get-key opts "space_id")
                    example/DEFAULT_SPACE_ID))
  (var group-id (or (xt/x:get-key opts "group_id")
                    example/DEFAULT_GROUP_ID))
  (window.PLAYGROUND.setTitle "Supabase auth profile")
  (window.PLAYGROUND.setStage
   [:% -/AuthProfileApp
    {:node node
     :space-id space-id
     :group-id group-id
     :initial-email (xt/x:get-key opts "email")}])
  (return node))

(defn.js mount-playground
  "creates the example node and mounts its UI in a :runtime :playground page"
  {:added "4.1"}
  [client-defaults opts]
  (:= opts (or opts {}))
  (var service-id (or (xt/x:get-key opts "service_id")
                      example/DEFAULT_SERVICE_ID))
  (var page-args {"space_id" (or (xt/x:get-key opts "space_id")
                                  example/DEFAULT_SPACE_ID)
                  "group_id" (or (xt/x:get-key opts "group_id")
                                  example/DEFAULT_GROUP_ID)})
  (var node (example/create-auth-profile-node client-defaults
                                               service-id
                                               page-args))
  (-/render-playground node opts)
  (return node))
