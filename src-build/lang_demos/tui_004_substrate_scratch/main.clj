(ns lang-demos.tui-004-substrate-scratch.main
  (:require [lang.core :as l]
            [lang-demos.tui-004-substrate-scratch.main-data]))

(l/script :js
  {:runtime :websocket
   :config {:bench false
            :id :dev/tui-004-substrate-scratch
            :port 28004
            :emit {:lang/jsx false}}
   :require [[js.react :as r]
             [js.blessed :as b]
             [js.module :as jm]
             [lang-demos.tui-004-substrate-scratch.main-data :as demo-data]
             [xt.lang.common-data :as data]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]
   :import [["node:module" :as [* NodeModule]]
            ["node:util" :as util]]})

(defrun.js __missing__
  (jm/import-missing)
  (jm/import-set-global))

(defn.js error-output
  [error]
  (return
   {"status" "error"
    "message" (or (. error message)
                  (String error))}))

(defn.js inspect-value
  [value]
  (return (. util (inspect value {:colors true
                                  :depth 6
                                  :compact 2
                                  :breakLength 68}))))

(defn.js reset-busy
  [set-busy]
  (set-busy false)
  (return true))

(defn.js load-demo
  [session demo-id set-output set-busy]
  (set-busy true)
  (set-output {"status" "loading"
               "demo_id" demo-id})
  (return
   (promise/x:promise-catch
    (promise/x:promise-then
     (demo-data/attach-demo
      (x:get-key session "client")
      demo-id
      demo-data/DEFAULT_USER_ID)
     (fn [result]
       (set-output result)
       (-/reset-busy set-busy)
       (return result)))
    (fn [error]
      (set-output (-/error-output error))
      (-/reset-busy set-busy)
      (return nil)))))

(defn.js SliceButton
  [#{entry index active disabled onSelect}]
  (var demo-id (x:get-key entry "id"))
  (return
   [:button {:left 1
             :top (+ 1 (* index 3))
             :width 30
             :height 2
             :content (+ (:? active ">" " ")
                         " "
                         (x:get-key entry "title"))
             :disabled disabled
             :keys true
             :mouse true
             :shrink true
             :onPress (fn []
                        (if (not disabled)
                          (onSelect demo-id)))
             :style {:bg (:? active "blue" "black")
                     :fg (:? active "white" "cyan")
                     :focus {:bold true}}}]))

(defn.js AppMain
  [props]
  (var [demo-id set-demo-id] (r/local "currencies"))
  (var session (x:get-key props "session"))
  (var connection-status (x:get-key props "connection_status"))
  (var [busy set-busy] (r/local false))
  (var [output set-output]
       (r/local (or (x:get-key props "initial_output")
                    {"status" "Select a slice and press Enter to query."})))
  (var selected-index
       (data/arr-find demo-data/DEMOS
                      (fn [entry]
                        (return (== (x:get-key entry "id") demo-id)))))
  (var selected (data/get-in demo-data/DEMOS [selected-index]))
  (var select-demo
       (fn [next-demo-id]
         (set-demo-id next-demo-id)
         (if session
           (-/load-demo session next-demo-id set-output set-busy))))
  (return
   [:box {:label "TUI 004 - Substrate Scratch"
          :border "line"
          :style {:border {:fg "green"}}}
    [:text {:left 2 :top 0}
     "Live scratch_v3 RPC and generated dataview results."]
    [:box {:left 1
           :top 2
           :width 32
           :height 16
           :label "SLICES"
           :border "line"}
     (xt/x:arr-map demo-data/DEMOS
                   (fn [entry index]
                     (return
                      [:% -/SliceButton
                       {:key (x:get-key entry "id")
                        :entry entry
                        :index index
                        :active (== demo-id (x:get-key entry "id"))
                        :disabled (or (not session) busy)
                        :onSelect select-demo}])))]
    [:box {:left 34
           :top 2
           :width 45
           :height 5
           :label "SELECTED DEMO"
           :border "line"}
     [:text {:left 1 :top 0}
      (x:get-key selected "title")]
     [:text {:left 1 :top 1 :width 41 :height 3}
      (x:get-key selected "description")]]
    [:box {:left 34
           :top 8
           :width 45
           :height 3
           :label "WORKER"
           :border "line"}
     [:text {:left 1 :top 0}
      connection-status]]
    [:box {:left 34
           :top 12
           :width 45
           :height 7
           :label "LIVE MODEL OUTPUT"
           :border "line"
           :scrollable true
           :keys true
           :content (-/inspect-value output)}]
    [:box {:left 34
           :top 20
           :width 45
           :height 6
           :label "EXAMPLE EVENT"
           :border "line"
           :scrollable true
           :keys true
           :content (-/inspect-value
                     (demo-data/example-event demo-id))}]
    [:text {:left 2 :top 27}
     "Mouse or focus + Enter to query. Press q to disconnect and exit."]]))

(defn.js Screen
  [connect-promise]
  (var screen (b/screen {:autoPadding true
                         :smartCSR true
                         :title "TUI 004 - Substrate Scratch"}))
  (screen.key ["q" "C-c" "Esc"]
              (fn []
                (promise/x:promise-catch
                 (promise/x:promise-then
                  connect-promise
                  (fn [connected-session]
                    (return (demo-data/disconnect connected-session))))
                 (fn [_]
                   (return nil)))
                (. screen (destroy))))
  (return screen))

(defrun.js __init__
  (do
    (:# (!:uuid))
    (xt/x:set-key globalThis "require"
                  (. NodeModule
                     (createRequire (+ (. process (cwd)) "/package.json"))))
    (var connect-promise (demo-data/connect demo-data/SUPABASE_CONFIG))
    (-> connect-promise
        (promise/x:promise-then
         (fn [session]
           (b/renderBlessed
            [:% -/AppMain {"session" session
                           "connection_status" "connected"}]
            (-/Screen connect-promise))
           (return session)))
        (promise/x:promise-catch
         (fn [error]
           (b/renderBlessed
            [:% -/AppMain {"session" nil
                           "connection_status" "connection failed"
                           "initial_output" (-/error-output error)}]
            (-/Screen connect-promise))
           (return nil))))))
