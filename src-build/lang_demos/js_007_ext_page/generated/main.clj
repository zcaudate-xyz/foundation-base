(ns lang-demos.js-007-ext-page.generated.main
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.node.client-supabase :as client-supabase]
             [js.react :as r]
             [js.react-native :as n]
             [js.react.ext-table :as ext-table]
             [lang-demos.js-007-ext-page.generated.core :as app-core]
             [lang-demos.js-007-ext-page.generated.runtime :as app-runtime]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(defn.js LogPage
  [props]
  (var state (app-core/useLogState props))
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
      [:p {:className "eyebrow"} "EXT PAGE / SUPABASE"]
      [:h1 "Shared activity log"]
      [:p {:className "intro-copy"}
       "A small client-side workspace backed by the Ext Table and Ext Model layers. Read and append entries directly through Supabase."]]
     [:span {:className "live-chip"}
      [:span {:className "live-dot"}]
      "LOCAL SUPABASE"]]
    [:div {:className "workspace"}
     [:section {:className "panel"}
      [:div {:className "panel-heading"}
       [:div
        [:h2 "Append an entry"]
        [:p {:className "panel-subtitle"}
         "The browser calls log_append_public with the public anon key."]]]
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
         "Rows loaded through the browser's anon-key read runtime."]]
       [:span {:className "count"} (+ rows.length " entries")]]
      rows-content]]
    [:footer {:className "footer"}
     "This public sample lets anyone reach and append to the shared log."]]))

(defn.js App
  []
  (var node (r/const
             (app-runtime/install-rpc-service
              (substrate/node-create {"id" "ext-page-log-node"}))))
  (var context (r/const (app-runtime/live-context node)))
  (var table (r/const (app-runtime/table-impl)))
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
  (return [:% -/LogPage {"view" view "append" append}]))
