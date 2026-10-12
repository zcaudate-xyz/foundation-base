(ns lang-demos.shared-log.page
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [lang-demos.shared-log.react :as log-react]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(defn.js PageHeader
  []
  (return
   [:header {:className "topbar"}
    [:div {:className "brand"}
     [:span {:className "brand-mark"} "ST"]
     [:span "STATSTRADE / OPERATIONS"]]
    [:span {:className "topbar-tag"} "FOUNDATION SAMPLE"]]))

(defn.js PageIntro
  [props]
  (var status (. props status))
  (return
   [:section {:className "intro"}
    [:div
     [:p {:className "eyebrow"} "EXT PAGE / SHARED LOG"]
     [:h1 "Shared activity log"]
     [:p {:className "intro-copy"}
      "Read, append, and delete entries in the shared log. The page uses Ext Table and Ext Model over a Supabase connection."]]
    [:span {:className "live-chip"}
     [:span {:className "live-dot"}]
     status]]))

(defn.js PageFooter
  []
  (return
   [:footer {:className "footer"}
    "Anyone who can reach this public sample can read, append, and delete shared log entries."]))

(defn.js LogPageStatus
  [props]
  (var status (. props status))
  (var message (. props message))
  (var error (. props error))
  (return
   [:main {:className "shell"}
    [:% -/PageHeader {}]
    [:% -/PageIntro {"status" status}]
    [:section {:className "panel" :role (:? error "alert" "status")}
     [:p {:className (:? error "notice error" "notice")}
      message]]
    [:% -/PageFooter {}]]))

(defn.js LogPage
  [props]
  (var state (log-react/useLogState props))
  (var message (. state message))
  (var submitting (. state submitting))
  (var notice (. state notice))
  (var notice-error (. state noticeError))
  (var rows (. state rows))
  (var pending (. state pending))
  (var failed (. state failed))
  (var deleting-id (. state deletingId))
  (var delete-entry (. state deleteEntry))
  (var realtime-status (. props realtimeStatus))
  (var realtime-error (. props realtimeError))
  (var row-elements
       (xt/x:arr-map
        rows
        (fn [entry index]
          (return
           [:article {:key (. entry id) :className "log-row"}
            [:span {:className "row-index"} (+ index 1)]
            [:div
             [:p {:className "row-message"} (. entry message)]
             [:span {:className "row-id"} (. entry id)]]
            [:% ui-button/Button
             {:design {:color "red"}
              :className "delete-button"
              :text (:? (== deleting-id (. entry id)) "Deleting..." "Delete")
              :disabled (or pending (not (== nil deleting-id)))
              :onPress (fn [] (delete-entry (. entry id)))}]]))))
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
    [:% -/PageHeader {}]
    [:% -/PageIntro {"status" realtime-status}]
    (:? realtime-error
        [:p {:className "notice error" :role "alert"}
         (+ "Supabase Realtime is unavailable: " realtime-error)]
        nil)
    [:div {:className "workspace"}
     [:section {:className "panel"}
      [:div {:className "panel-heading"}
       [:div
        [:h2 "Append an entry"]
        [:p {:className "panel-subtitle"}
         "Entries are written with the public anon key."]]]
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
         "Rows are loaded through the anon-key read runtime."]]
       [:span {:className "count"} (+ rows.length " entries")]]
      rows-content]]
    [:% -/PageFooter {}]]))
