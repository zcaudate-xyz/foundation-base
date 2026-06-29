(ns xt.db.poc-v3.profile-ui
  "React UI for viewing and editing a scratch_v3 UserProfile through the
   xt.db.poc-v3.sharedworker SharedWorker runtime.

   The UI is intentionally plain-DOM so it can be rendered in any browser
   without extra CSS dependencies."
  (:require [hara.lang :as l]
            [xt.db.poc-v3.sharedworker :as sharedworker]))

(l/script :js
  {:require [[js.react :as r]
             [js.react.ext-page :as ext-page]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.db.poc-v3.sharedworker :as sharedworker]]})

(defn.js useProfileSession
  "Connects to the SharedWorker, initialises the scratch-v3 adaptor, and
   attaches the profile read/update RPC models. Returns a state map with
   :status, :client and :transport-id."
  [account-id]
  (var [state setState] (r/local {"status" "loading"}))
  (r/init []
    (var mounted true)
    (-> (sharedworker/with-session
          (fn [client transport-id]
            (-> (sharedworker/attach-user-profile-rpc client transport-id account-id)
                (promise/x:promise-then
                 (fn [_]
                   (return (sharedworker/attach-update-profile-rpc client transport-id))))
                (promise/x:promise-then
                 (fn [_]
                   (return (sharedworker/refresh-profile client))))
                (promise/x:promise-then
                 (fn [_]
                   (when mounted
                     (setState {"status" "ready"
                                "client" client
                                "transport-id" transport-id}))
                   (return nil))))))
        (promise/x:promise-catch
         (fn [err]
           (when mounted
             (setState {"status" "error"
                        "error" (or (. err ["message"])
                                    (xt/x:ex-message err)
                                    (String err))})))))
    (return (fn [] (:= mounted false))))
  (return state))

(defn.js ProfileView
  "Renders the profile output and a small editor. Must be used after the
   SharedWorker session is ready."
  [#{[client account-id]}]
  (var output (ext-page/listenModelOutput client "room/a" ["demo" "profile"] ["output"] nil))
  (var current (or (xt/x:get-key output "current") {}))
  (var [firstName setFirstName] (r/local (or (xt/x:get-key current "first-name") "")))
  (var [lastName setLastName] (r/local (or (xt/x:get-key current "last-name") "")))
  (var [saveStatus setSaveStatus] (r/local "idle"))
  (r/watch [(xt/x:get-key current "first-name") (xt/x:get-key current "last-name")]
    (setFirstName (or (xt/x:get-key current "first-name") ""))
    (setLastName (or (xt/x:get-key current "last-name") "")))
  (return
   [:% "div"
    {:style {:fontFamily "sans-serif"
             :padding 20
             :maxWidth 400}}
    [:% "h2" "User Profile"]
    [:% "div" {:style {:marginBottom 8}}
     [:% "strong" "First name: "]
     (or (xt/x:get-key current "first-name") "-")]
    [:% "div" {:style {:marginBottom 8}}
     [:% "strong" "Last name: "]
     (or (xt/x:get-key current "last-name") "-")]
    [:% "div" {:style {:marginBottom 8}}
     [:% "strong" "Language: "]
     (or (xt/x:get-key current "language") "-")]
    [:% "div" {:style {:marginBottom 16}}
     [:% "strong" "About: "]
     (or (xt/x:get-key current "about") "-")]
    [:% "h3" "Edit"]
    [:% "div" {:style {:marginBottom 8}}
     [:% "label" {:style {:display "block"}} "First name"]
     [:% "input"
      {:value firstName
       :style {:width "100%"}
       :onChange (fn [e]
                   (return (setFirstName (. e ["target"] ["value"]))))}]]
    [:% "div" {:style {:marginBottom 8}}
     [:% "label" {:style {:display "block"}} "Last name"]
     [:% "input"
      {:value lastName
       :style {:width "100%"}
       :onChange (fn [e]
                   (return (setLastName (. e ["target"] ["value"]))))}]]
    [:% "button"
     {:onClick (fn [_]
                 (setSaveStatus "saving")
                 (-> (sharedworker/update-profile client account-id
                                                  {"first-name" firstName
                                                   "last-name" lastName}
                                                  {})
                     (promise/x:promise-then
                      (fn [_]
                        (return (sharedworker/refresh-profile client))))
                     (promise/x:promise-then
                      (fn [_]
                        (return (setSaveStatus "saved"))))
                     (promise/x:promise-catch
                      (fn [err]
                        (return (setSaveStatus (str "error: " err))))))
                 (return nil))}
     "Save"]
    (when (!= saveStatus "idle")
      [:% "div" {:style {:marginTop 10 :color "#666"}} saveStatus])]))

(defn.js ProfileApp
  "Root component that waits for the SharedWorker session and then renders
   the profile view."
  [#{[account-id]}]
  (var session (-/useProfileSession account-id))
  (var status (xt/x:get-key session "status"))
  (cond (== status "error")
        (return [:% "div" {:style {:color "red" :padding 20}}
                 (str "Error: " (xt/x:get-key session "error"))])
        (== status "ready")
        (return [:% -/ProfileView
                 {:client (xt/x:get-key session "client")
                  :account-id account-id}])
        :else
        (return [:% "div" {:style {:padding 20}} "Loading profile..."])))

(defn.js renderProfileUI
  "Mounts the profile UI into the DOM element with `id`."
  [id account-id]
  (return
   (r/renderDOMRoot id
                    (fn []
                      (return [:% -/ProfileApp {:account-id account-id}])))))
