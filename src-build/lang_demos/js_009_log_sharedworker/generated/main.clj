(ns lang-demos.js-009-log-sharedworker.generated.main
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.node.client-supabase :as client-supabase]
             [js.react :as r]
             [js.react-native :as n]
             [js.react.ext-table :as ext-table]
             [lang-demos.js-009-log-sharedworker.generated.core :as app-core]
             [lang-demos.js-009-log-sharedworker.generated.runtime :as app-runtime]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(defn.js LogPage
  [props]
  (var node (. props node))
  (var context (app-runtime/live-context node))
  (var table (app-runtime/table-impl))
  (var view (ext-table/useRemoteView
             table
             "data"
             {"defaultArgs" []}
             context
             {}))
  (var append
       (fn [message]
         (return
          (client-supabase/rpc-call
           node
           "auth/supabase"
           "log_append_public"
           {"i_message" message}
           {"headers" {"Content-Profile" "scratch_v0"
                       "Accept-Profile" "scratch_v0"}}))))
  (var state (app-core/useLogState {"view" view "append" append}))
  (var message (. state message))
  (var submitting (. state submitting))
  (var notice (. state notice))
  (var notice-error (. state noticeError))
  (var rows (. state rows))
  (var pending (. state pending))
  (var failed (. state failed))
  (var row-elements
       (xt/x:arr-map
        rows
        (fn [entry index]
          (return
           [:article {:key (. entry id) :className "log-row"}
            [:span {:className "row-index"} (+ index 1)]
            [:div
             [:p {:className "row-message"} (. entry message)]
             [:span {:className "row-id"} (. entry id)]]]))))
  (var rows-content
       (:? pending
           [:div {:className "empty-state" :role "status"} "Loading log entries..."]
           (:? failed
               [:div {:className "empty-state" :role "alert"}
                "The log could not be loaded. Check the local Supabase connection and schema grants."]
               (:? (== 0 rows.length)
                   [:div {:className "empty-state"} "No entries yet. Add the first one here."]
                   [:div {:className "log-list"} row-elements]))))
  (return
   [:main {:className "shell"}
    [:header {:className "topbar"}
     [:div {:className "brand"}
      [:span {:className "brand-mark"} "ST"]
      [:span "STATSTRADE / OPERATIONS"]]
     [:span {:className "topbar-tag"} "FOUNDATION SAMPLE"]]
    [:section {:className "intro"}
     [:div
      [:p {:className "eyebrow"} "EXT PAGE / SHAREDWORKER"]
      [:h1 "Shared activity log"]
      [:p {:className "intro-copy"}
       "The page reads and appends through a proxy connection to a SharedWorker that owns the Supabase node."]]
     [:span {:className "live-chip"}
      [:span {:className "live-dot"}]
      "SHARED WORKER"]]
    [:div {:className "workspace"}
     [:section {:className "panel"}
      [:div {:className "panel-heading"}
       [:div
        [:h2 "Append an entry"]
        [:p {:className "panel-subtitle"}
         "The shared worker calls log_append_public with the public anon key."]]]
      [:div {:className "entry-form"}
       [:label {:className "field-label" :htmlFor "log-message"}
        "Message"]
       [:% ui-input/Input
        {:design {:color "blue"}
         :id "log-message"
         :className "entry-input"
         :accessibilityLabel "Log message"
         :placeholder "Write a short activity note..."
         :value message
         :onChangeText (. state setMessage)
         :multiline true
         :numberOfLines 4}]
       [:% ui-button/Button
        {:design {:color "blue"}
         :className "submit-button"
         :text (:? submitting "Appending..." "Add to log")
         :disabled (or submitting (== "" (. message (trim))))
         :onPress (. state appendEntry)}]
       [:p {:className (+ "notice" (:? notice-error " error" ""))
            :role "status"}
        notice]]]
     [:section {:className "panel"}
      [:div {:className "list-header"}
       [:div
        [:h2 "Log entries"]
        [:p {:className "panel-subtitle"}
         "Rows loaded by the shared worker's anon-key read runtime."]]
       [:span {:className "count"} (+ rows.length " entries")]]
      rows-content]]
    [:footer {:className "footer"}
     "All pages connect to the same SharedWorker node for this shared log."]]))

(defn.js App
  []
  (var node (r/const
             (substrate/node-create {"id" "log-sharedworker-client"})))
  (var [worker-state set-worker-state] (r/local nil))
  (var [worker-error set-worker-error] (r/local ""))
  (var connection-ref (r/ref nil))
  (r/useEffect
   (fn []
     (var active true)
     (. (app-runtime/connect-client node)
        (then
         (fn [state]
           (if active
             (do
               (:= (. connection-ref current) state)
               (set-worker-state state))
             (app-runtime/disconnect-client state))))
        (catch
         (fn [error]
           (when active
             (set-worker-error
              (or (. error message)
                  "Unable to connect to the shared worker."))))))
     (fn []
       (:= active false)
       (var state (. connection-ref current))
       (when state
         (. (app-runtime/disconnect-client state)
            (catch (fn [_] nil))))))
   [])
  (when worker-state
    (return [:% -/LogPage {"node" node}]))
  (return
   [:main {:className "shell"}
    [:header {:className "topbar"}
     [:div {:className "brand"}
      [:span {:className "brand-mark"} "ST"]
      [:span "STATSTRADE / OPERATIONS"]]
     [:span {:className "topbar-tag"} "FOUNDATION SAMPLE"]]
    [:section {:className "intro"}
     [:div
      [:p {:className "eyebrow"} "EXT PAGE / SHAREDWORKER"]
      [:h1 "Shared activity log"]
      [:p {:className "intro-copy"}
       "Connecting to the SharedWorker that owns the Supabase node..."]]]
    [:section {:className "panel" :role (:? worker-error "alert" "status")}
     [:p {:className (:? worker-error "notice error" "notice")}
      (:? worker-error
          (+ "Unable to connect to the SharedWorker: " worker-error)
          "The shared log is starting.")]]]))
