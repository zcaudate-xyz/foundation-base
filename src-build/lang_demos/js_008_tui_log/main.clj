(ns lang-demos.js-008-tui-log.main
  (:require [lang.core :as l]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/js-008-tui-log
            :port 28009
            :emit {:lang/jsx false}}
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.client-supabase :as client-supabase]
             [xt.lang.common-client :as client]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]
             [js.blessed :as b]
             [js.blessed.ui-core :as ui-core]
             [js.module :as jm]
             [lang-demos.js-008-tui-log.runtime :as app-runtime]]})

(defrun.js __missing__
  (jm/import-missing)
  (jm/import-set-global))

(defn.js Button
  [{:# [left top text disabled color action compact]}]
  (return
   [:button {:left (or left 0)
             :top (or top 0)
             :content text
             :shrink true
             :mouse true
             :keys true
             :onPress (fn []
                        (if (and action
                                 (not disabled))
                          (action)))
             :padding (:? compact
                        {:top 0 :right 1 :bottom 0 :left 1}
                        {:top 1 :right 2 :bottom 1 :left 2})
             :style {:bg (:? (not disabled) color "black")
                     :fg (:? (not disabled) "white" "gray")
                     :focus {:bold true}}}]))

(defn.js AppendPanel
  [{:# [node view notice setNotice noticeError setNoticeError]}]
  (var [message set-message] (r/local ""))
  (var [submitting set-submitting] (r/local (== 1 0)))
  (var append-entry
       (fn []
         (var clean-message (. message (trim)))
         (when (and (not submitting)
                    (> (. clean-message length) 0))
           (when (> (. clean-message length) 500)
             (setNotice "Messages are limited to 500 characters.")
             (setNoticeError true)
             (return nil))
           (set-submitting true)
           (setNotice "Appending to the shared log...")
           (setNoticeError false)
           (-> (client-supabase/rpc-call
                node
                "auth/supabase"
                "log_append_public"
                {"i_message" clean-message}
                {"headers" {"Content-Profile" "scratch_v0"
                            "Accept-Profile" "scratch_v0"}})
               (promise/x:promise-then
                (fn [rpc-row]
                  (set-message "")
                  (setNotice (+ "Added entry " (. rpc-row ["id"]) "."))
                  (return (ext-model/refresh-model view {}))))
               (promise/x:promise-catch
                (fn [error]
                  (setNotice (or (. error message)
                                  "Unable to append this entry."))
                  (setNoticeError true)
                  (return nil)))
               (promise/x:promise-finally
                (fn []
                  (set-submitting false)))))))
  (return
   [:box {:left 2
          :top 2
          :width 76
          :height 8
          :label "APPEND ENTRY"
          :border "line"}
    [:text {:left 2 :top 0 :style {:fg "gray"}}
     "Messages are limited to 500 characters."]
    [:% ui-core/TextBox
     {:left 2
      :top 2
      :width 47
      :height 3
      :initial ""
      :content message
      :setContent set-message}]
    [:% -/Button
     {:left 52
      :top 2
      :text (:? submitting "SENDING" "APPEND")
      :disabled (or submitting (== "" (. message (trim))))
      :action append-entry
      :color "green"}]
    [:text {:left 2
            :top 5
            :style {:fg (:? noticeError "red" "green")}}
     notice]]))

(def.js MemoAppendPanel
  (r/memo -/AppendPanel))

(defn.js LogApp
  []
  (var [runtime _]
       (r/local
        (fn []
          (var node (app-runtime/install-rpc-service
                     (substrate/node-create {"id" "js-008-tui-log-node"})))
          (return {"node" node
                   "context" (app-runtime/live-context node)
                   "table" (app-runtime/table-impl)}))))
  (var node (. runtime ["node"]))
  (var context (. runtime ["context"]))
  (var table (. runtime ["table"]))
  (var view (ext-table/useRemoteView
             table
             "data"
             {"defaultArgs" []}
             context
             {}))
  (var model-output (ext-model/listenModelOutput
                     view
                     ["output" "pending" "error"]
                     {}
                     nil))
  (var [deleting-id set-deleting-id] (r/local nil))
  (var [realtime-status set-realtime-status] (r/local "CONNECTING"))
  (var [notice set-notice] (r/local ""))
  (var [notice-error set-notice-error] (r/local (== 1 0)))
  (var current-rows (. model-output current))
  (var rows (:? (. Array (isArray current-rows)) current-rows []))
  (var has-rows (> rows.length 0))
  (var pending (or (event-model/is-pending view nil)
                   (. model-output pending)))
  (var failed (event-model/is-errored view nil))
  (var data-status (:? failed
                      (:? has-rows "STALE" "ERROR")
                      (:? pending "CHECKING" "ONLINE")))
  (var connection-warning
       (:? (and failed (== realtime-status "OFFLINE"))
           (:? has-rows
               "SUPABASE CONNECTION ERROR - Data API and Realtime failed; showing the last loaded entries."
               "SUPABASE CONNECTION ERROR - Data API and Realtime failed; no entries loaded.")
           (:? failed
               (:? has-rows
                   "SUPABASE DATA API REQUEST FAILED - Showing the last loaded entries."
                   "SUPABASE DATA API REQUEST FAILED - No entries loaded. Check the URL, anon key, and local stack.")
               (:? (== realtime-status "OFFLINE")
                   "SUPABASE REALTIME OFFLINE - Live updates are unavailable."
                   nil))))
  (var delete-entry
       (fn [entry]
         (when (and (not deleting-id)
                    (. entry id))
           (var entry-id (. entry id))
           (set-deleting-id entry-id)
           (set-notice (+ "Deleting entry " entry-id "..."))
           (set-notice-error false)
           (-> (client-supabase/rpc-call
                node
                "auth/supabase"
                "log_delete_public"
                {"i_id" entry-id}
                {"headers" {"Content-Profile" "scratch_v0"
                            "Accept-Profile" "scratch_v0"}})
               (promise/x:promise-then
                (fn [_]
                  (set-notice (+ "Deleted entry " entry-id "."))
                  (return (ext-model/refresh-model view {}))))
               (promise/x:promise-catch
                (fn [error]
                  (set-notice (or (. error message)
                                  "Unable to delete this entry."))
                  (set-notice-error true)
                  (return nil)))
               (promise/x:promise-finally
                (fn []
                  (set-deleting-id nil)))))))
  (var row-elements
       (xt/x:arr-map
        rows
        (fn [entry index]
          (var row-message (or (. entry message) ""))
          (var preview (:? (> (. row-message length) 58)
                           (+ (. row-message (slice 0 55)) "...")
                           row-message))
          (return
           [:box {:key (. entry id)
                  :top (+ index 1)
                  :height 1}
            [:text {:left 1
                    :width 4
                    :style {:fg "cyan"}
                    :content (+ "[" (+ index 1) "]")}]
            [:text {:left 7
                    :width 58
                    :style {:fg "white"}
                    :content preview}]
            [:% -/Button
             {:left 68
              :top 0
              :text (:? (== deleting-id (. entry id)) "..." "DEL")
              :disabled (not (== nil deleting-id))
              :color "red"
              :compact true
              :action (fn []
                        (delete-entry entry))}]]))))
  (var rows-content
       (:? has-rows
           row-elements
           (:? failed
               [:text {:left 2 :top 1 :style {:fg "red"}}
                "The log could not be loaded. Check the local Supabase connection."]
               (:? pending
                   [:text {:left 2 :top 1 :style {:fg "yellow"}}
                    "Loading log entries..."]
                   [:text {:left 2 :top 1 :style {:fg "gray"}}
                    "No entries yet. Add the first one above."]))))
  (r/init []
    (var disposed false)
    (var cleanup nil)
    (-> (app-runtime/subscribe-log
         node context "js-008-log"
         (fn [_]
           (when (not disposed)
             (return (ext-model/refresh-model view {})))))
        (promise/x:promise-then
         (fn [stop]
           (if disposed
             (stop)
             (do
               (:= cleanup stop)
               (set-realtime-status "LIVE")))))
        (promise/x:promise-catch
         (fn [_]
           (when (not disposed)
             (set-realtime-status "OFFLINE"))
           (return nil))))
    (return
     (fn []
       (:= disposed true)
       (when cleanup
         (cleanup)))))
  (r/init []
    (return
     (fn []
       (return
        (client-base/kernel-teardown
         node
         "db/primary"
         {})))))
  (return
   [:box {:label "JS-008 / TUI LOG"
          :border "line"
          :keys true
          :mouse true
          :style {:border {:fg "green"}}}
    [:text {:left 2 :top 0 :style {:fg "gray"}}
     (+ "Shared activity log · scratch_v0 · Data API " data-status
        " · Realtime " realtime-status)]
    (:? connection-warning
        [:text {:left 2 :top 1 :style {:fg "red"}}
         connection-warning]
        nil)
    [:% -/MemoAppendPanel {:node node
                           :view view
                           :notice notice
                           :set-notice set-notice
                           :notice-error notice-error
                           :set-notice-error set-notice-error}]
    [:box {:left 2
           :top 11
           :width 76
           :height 11
           :label "LOG ENTRIES"
           :border "line"
           :scrollable true
           :keys true
           :mouse true
           :scrollbar {:style {:bg "gray" :fg "gray"}
                       :track true}}
     [:text {:left 2 :top 0 :style {:fg "gray"}}
      "Rows loaded from scratch_v0.Log through js.react.ext-table"]
     rows-content]
    [:text {:left 2 :top 22 :style {:fg "gray"}}
     "Tab to focus the message and row actions · press q, Esc, or Ctrl-C to exit"]]))

(defn.js Screen
  []
  (var screen (b/screen {:autoPadding true
                         :smartCSR true
                         :title "JS-008 TUI Log"}))
  (screen.key ["q" "C-c" "Esc"]
              (fn []
                (. screen (destroy))))
  (return screen))

(defrun.js __init__
  (do
    (:# (!:uuid))
    (client/client-ws "localhost" 28009 {})
    (b/renderBlessed [:% -/LogApp] (-/Screen))))

