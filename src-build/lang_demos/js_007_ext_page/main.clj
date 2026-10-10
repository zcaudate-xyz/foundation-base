(ns lang-demos.js-007-ext-page.main
  (:require [lang.core :as l]
            [postgres.core :as pg]
            [postgres.sample.scratch-v0]
            [scaffold.supabase.local-min :as local-min]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.node.client-supabase :as client-supabase]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.main :as db-main]
             [js.react :as r]
             [js.react-native :as n]
             [js.react.ext-table :as ext-table]
             [lang-demos.js-007-ext-page.core :as app-core]
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

(def.js SupabaseDefaults
  {"host" (. -/SupabaseUrl hostname)
   "port" (:? (== "" (. -/SupabaseUrl port))
              (:? (== "https:" (. -/SupabaseUrl protocol)) 443 80)
              (Number (. -/SupabaseUrl port)))
   "secured" (== "https:" (. -/SupabaseUrl protocol))
   "basepath" (:? (== "/" (. -/SupabaseUrl pathname))
                  ""
                  (. -/SupabaseUrl pathname))
   "apikey" -/AnonKey
   "token" -/AnonKey})

(def.js RuntimeConfig
  {"primary" {"type" "supabase"
              "defaults" -/SupabaseDefaults}
   "caching" {"type" "memory"
              "defaults" {}}})

(defn.js install-rpc-service
  [node]
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" -/SupabaseDefaults nil nil))
  (db-runtime/init-server node)
  (return node))

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
             (-/install-rpc-service
              (substrate/node-create {"id" "ext-page-log-node"}))))
  (var context (r/const (-/live-context node)))
  (var view (ext-table/useRemoteView
             (-/table-impl)
             "data"
             {}
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
