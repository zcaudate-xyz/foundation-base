(ns lang-demos.js-007-ext-page.main
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.sample.scratch-v0]
            [scaffold.supabase.local-min :as local-min]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [js.react :as r]
             [js.react-native :as n]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js SupabaseUrl
  (new URL (or (. process env NEXT_PUBLIC_SUPABASE_URL)
               (@! (str "http://"
                        (-> local-min/+config+ :api :hostname)
                        ":"
                        (-> local-min/+config+ :api :port))))))

(def.js AnonKey
  (or (. process env NEXT_PUBLIC_SUPABASE_ANON_KEY)
      (@! (-> local-min/+config+ :api :anon-key))))

(def.js RuntimeConfig
  {"primary"
   {"type" "supabase"
    "defaults" {"host" (. -/SupabaseUrl hostname)
                "port" (:? (== "" (. -/SupabaseUrl port))
                           (:? (== "https:" (. -/SupabaseUrl protocol)) 443 80)
                           (Number (. -/SupabaseUrl port)))
                "secured" (== "https:" (. -/SupabaseUrl protocol))
                "basepath" (:? (== "/" (. -/SupabaseUrl pathname))
                               ""
                               (. -/SupabaseUrl pathname))
                "apikey" -/AnonKey
                "token" -/AnonKey}}
   "caching" {"type" "memory"
              "defaults" {}}})

(defn.js live-context
  [node]
  (return {"node" node
           "runtime" {"config" -/RuntimeConfig
                      "schema" -/Schema
                      "lookup" -/SchemaLookup}}))

(defn.js table-impl
  []
  (return
   {"base"
    {"list" {"spec" ["Log" {"data" ["id" "message"]}]}
     "data" {"spec" ["Log" {"data" ["id" "message"]}]}}
    "call" {}
    "cached" {}}))

(defn.js App
  []
  (var node (r/const (substrate/node-create {"id" "ext-page-log-node"})))
  (var context (r/const (-/live-context node)))
  (var view (ext-table/useRemoteView
             (-/table-impl)
             "data"
             {}
             context
             {}))
  (var model-output (ext-model/listenModelOutput
                     view
                     ["output" "pending" "error"]
                     {}
                     nil))
  (var [message setMessage] (r/local ""))
  (var [submitting setSubmitting] (r/local (== 1 0)))
  (var [notice setNotice] (r/local ""))
  (var [notice-error setNoticeError] (r/local (== 1 0)))
  (var rows (or (event-model/get-current view nil) []))
  (var pending (or (event-model/is-pending view nil)
                   (. model-output pending)))
  (var failed (event-model/is-errored view nil))
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
  (var append-entry
       (fn []
         (var clean-message (. message (trim)))
         (when (and (not submitting)
                    (> (. clean-message length) 0))
           (setSubmitting true)
           (setNotice "")
           (setNoticeError false)
           (. (fetch "/api/log"
                     {:method "POST"
                      :headers {"Content-Type" "application/json"}
                      :body (JSON.stringify {"message" clean-message})})
              (then
               (fn [response]
                 (when (not (. response ok))
                   (throw (new Error "Unable to append this entry.")))
                 (return (. response (json)))))
              (then
               (fn [_]
                 (setMessage "")
                 (setNotice "Entry added to the shared log.")
                 (return (ext-model/refresh-model view {}))))
              (catch
               (fn [error]
                 (setNotice (or (. error message)
                                "Unable to append this entry."))
                 (setNoticeError true)))
              (finally
               (fn [] (setSubmitting false)))))))
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
       "A small admin workspace backed by the Ext Table and Ext Model layers. Read entries from local Supabase and append a new one through the Next.js server route."]]
     [:span {:className "live-chip"}
      [:span {:className "live-dot"}]
      "LOCAL SUPABASE"]]
    [:div {:className "workspace"}
     [:section {:className "panel"}
      [:div {:className "panel-heading"}
       [:div
        [:h2 "Append an entry"]
        [:p {:className "panel-subtitle"}
         "The server calls the existing log_append_public RPC."]]]
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
         :onChangeText setMessage
         :multiline true
         :numberOfLines 4}]
       [:% ui-button/Button
        {:design {:color "blue"}
         :className "submit-button"
         :text (:? submitting "Appending..." "Add to log")
         :disabled (or submitting (== "" (. message (trim))))
         :onPress append-entry}]
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
     "Reads use the public anon key. The service role key stays in the Next.js server environment."]]))
