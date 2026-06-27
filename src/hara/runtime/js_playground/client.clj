^{:no-test true}
(ns hara.runtime.js-playground.client
  "Browser-side playground client.

   Written as a proper `l/script :js` namespace so it can be compiled with
   `js.react`, `xt.lang.common-lib/return-eval` and the rest of the xt/js
   ecosystem. It is emitted by `hara.runtime.js-playground` and served to the
   browser as an ES module.

   Vectors such as `[:div ...]` are transpiled to `React.createElement` calls
   because the playground emits scripts with `:lang/jsx false`."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]]})

(defn.js format-body
  "returns a string representation of a message body for display"
  {:added "4.0"}
  [body]
  (if (== null body)
    (return "")
    (if (== undefined body)
      (return "")
      (if (== "string" (typeof body))
        (return body)
        (return (or (JSON.stringify body null 2) (String body)))))))

(defn.js send-response
  "sends a response frame back over the websocket"
  {:added "4.0"}
  [ws id status body]
  (. ws (send (JSON.stringify {"id" id
                                "status" status
                                "body" body}))))

(defn.js eval-body
  "evaluates a JS string using xt.lang.common-lib/return-eval and decodes the
   result. Returns a vector of [status value]."
  {:added "4.0"}
  [body]
  (var result (xt/x:json-decode (k/return-eval body)))
  (var is-error (== "error" (. result ["type"])))
  (return [(:? is-error "error" "ok")
           (. result ["value"])]))

(defn.js make-add-message
  "creates the add-message callback that prepairs pairs so the latest pair is first"
  {:added "4.0"}
  [messagesRef setMessages]
  (return (fn [m]
            (. messagesRef current (unshift m))
            (if (> (. messagesRef current length) 200)
              (. messagesRef current (pop)))
            (setMessages (. messagesRef current (slice))))))

(defn.js run-eval
  "handles an incoming eval message as a sent/reply pair"
  {:added "4.0"}
  [ws add-message messagesRef setMessages msg]
  (var id (. msg ["id"]))
  (var body (. msg ["body"]))
  (var entry {"id" id
              "time" (xt/x:now-ms)
              "sent" body
              "reply" nil
              "status" nil})
  (add-message entry)
  (try
    (var [status value] (-/eval-body body))
    (-/send-response ws id status value)
    (:= (. entry ["reply"]) (-/format-body value))
    (:= (. entry ["status"]) status)
    (catch err
      (var text (or (. err ["message"]) (String err)))
      (-/send-response ws id "error" text)
      (:= (. entry ["reply"]) text)
      (:= (. entry ["status"]) "error")))
  (setMessages (. messagesRef current (slice))))

(defn.js MessageItem
  "renders a sent/reply pair with expand/collapse and print button"
  {:added "4.0"}
  [{:# [m]}]
  (var [expanded setExpanded] (r/local))
  (var id (. m ["id"]))
  (var status (. m ["status"]))
  (var sent (or (-/format-body (. m ["sent"])) ""))
  (var reply (or (-/format-body (. m ["reply"])) ""))
  (var has-reply (not= nil (. m ["reply"])))
  (var preview-limit 120)
  (var long-sent (> (. sent length) preview-limit))
  (var long-reply (and has-reply (> (. reply length) preview-limit)))
  (var is-long (or long-sent long-reply))
  (var display-sent (:? (and (not expanded) long-sent)
                        (+ (. sent (slice 0 preview-limit)) "...")
                        sent))
  (var display-reply (:? (and (not expanded) long-reply)
                         (+ (. reply (slice 0 preview-limit)) "...")
                         reply))
  (var status-color (:? (== status "ok") "#4caf50"
                        (:? (== status "error") "#f44336" "#ff9800")))
  (var border-color (:? has-reply
                        status-color
                        "#ff9800"))
  (var button-style {:background "transparent"
                     :border "1px solid #ccc"
                     :borderRadius "4px"
                     :padding "2px 6px"
                     :cursor "pointer"
                     :fontSize "10px"
                     :color "#555"})
  (var label-style {:fontSize "10px"
                    :color "#888"
                    :marginTop "4px"})
  (return
   [:div
    {:style {:marginBottom "10px"
             :padding "10px"
             :borderRadius "6px"
             :fontSize "12px"
             :background "#fff"
             :border "1px solid #ddd"
             :borderLeft (+ "4px solid " border-color)}}
    [:div
     {:style {:display "flex"
              :justifyContent "space-between"
              :alignItems "center"
              :marginBottom "4px"}}
     [:div
      {:style {:fontWeight 600
               :textTransform "uppercase"
               :fontSize "10px"
               :color "#555"}}
      "Eval " (. (or id "") (slice 0 8))
      (:? status
          [:span
           {:style {:marginLeft "8px"
                    :color status-color}}
           "[" status "]"]
          nil)]
     [:div
      {:style {:display "flex" :gap "6px"}}
      (:? is-long
          [:button
           {:style button-style
            :onClick (fn [] (setExpanded (not expanded)))}
           (:? expanded "Collapse" "Expand")]
          nil)
      [:button
       {:style button-style
        :onClick (fn [] (. console (log m)))}
       "Print"]]]
    [:div
     {:style label-style}
     "Sent"]
    [:pre
     {:style {:margin "2px 0 0 0"
              :whiteSpace "pre-wrap"
              :wordBreak "break-all"
              :fontSize "11px"}}
     display-sent]
    (:? has-reply
        [:div
         [:div
          {:style label-style}
          "Reply"]
         [:pre
          {:style {:margin "2px 0 0 0"
                   :whiteSpace "pre-wrap"
                   :wordBreak "break-all"
                   :fontSize "11px"}}
          display-reply]]
        [:div
         {:style {:color "#999" :fontSize "11px" :marginTop "6px"}}
         "Waiting for reply..."])]))

(defn.js MessageList
  "renders the list of sent/received messages"
  {:added "4.0"}
  [{:# [messages]}]
  (return
   [:div
    {:style {:flex 1
             :overflow "auto"
             :padding "12px"}}
    (:? (== (. messages length) 0)
        [:div
         {:style {:color "#999" :fontSize "12px"}}
         "No messages yet."]
        (. messages
           (map (fn [m i]
                  (return [:% -/MessageItem {:key i :m m}])))))]))

(defn.js TopMenu
  "top menu bar with connection status on the right and a messages dropdown"
  {:added "4.0"}
  [{:# [title
        status
        url
        messages
        showMessages
        setShowMessages]}]
  (var [dropdownOpen setDropdownOpen] (r/local))
  (var color (:? (== status "connected") "#4caf50"
                 (:? (== status "error") "#f44336" "#ff9800")))
  (var recent (. messages (slice -5)))
  (return
   [:div
    {:id "topbar"
     :style {:position "relative"
             :display "flex"
             :alignItems "center"
             :justifyContent "space-between"
             :padding "0 16px"
             :height "48px"
             :background "#fff"
             :borderBottom "1px solid #ddd"
             :fontFamily "system-ui, sans-serif"
             :fontSize "14px"}}
    [:div
     {:style {:fontWeight 600 :fontSize "16px"}}
     title]
    [:div
     {:style {:display "flex"
              :alignItems "center"
              :gap "12px"}}
     [:div
      {:style {:position "relative"}}
      [:button
       {:style {:background "#f5f5f5"
                :border "1px solid #ddd"
                :borderRadius "4px"
                :padding "6px 12px"
                :cursor "pointer"
                :fontSize "12px"}
        :onClick (fn [] (setDropdownOpen (not dropdownOpen)))}
       "Messages (" (+ (. messages length)) ")"]
      (:? dropdownOpen
          [:div
           {:style {:position "absolute"
                    :top "38px"
                    :right "0"
                    :width "320px"
                    :maxHeight "400px"
                    :overflow "auto"
                    :background "#fff"
                    :border "1px solid #ddd"
                    :borderRadius "4px"
                    :boxShadow "0 4px 12px rgba(0,0,0,0.15)"
                    :zIndex 100}}
           [:div
            {:style {:padding "8px 12px"
                     :borderBottom "1px solid #eee"
                     :fontWeight 600
                     :fontSize "12px"}}
            "Recent Messages"]
           (:? (> (. messages length) 0)
               (. recent
                  (map (fn [m i]
                         (return [:% -/MessageItem {:key i :m m}]))))
               [:div
                {:style {:padding "12px" :color "#999" :fontSize "12px"}}
                "No messages yet."])
           [:div
            {:style {:padding "8px 12px"
                     :borderTop "1px solid #eee"
                     :textAlign "center"}}
            [:button
             {:style {:background "transparent"
                      :border "none"
                      :color "#2196f3"
                      :cursor "pointer"
                      :fontSize "12px"}
              :onClick (fn []
                         (do (setShowMessages (not showMessages))
                             (setDropdownOpen false)))}
             (:? showMessages "Hide message log" "Show message log")]]]
          nil)]
     [:div
      {:style {:display "flex"
               :alignItems "center"
               :gap "6px"
               :padding "4px 10px"
               :borderRadius "12px"
               :background "#f5f5f5"}}
      [:span
       {:style {:width "10px"
                :height "10px"
                :borderRadius "50%"
                :background color
                :display "inline-block"}}]
      "Status: " status]
     [:div
      {:style {:color "#666" :fontSize "12px"}}
      url]]]))

(defn.js TabBar
  "renders the configurable tab bar"
  {:added "4.0"}
  [{:# [tabs
        activeTab
        setActiveTab]}]
  (return
   [:div
    {:style {:display "flex"
             :alignItems "center"
             :gap "4px"
             :padding "0 16px"
             :height "36px"
             :background "#f5f5f5"
             :borderBottom "1px solid #ddd"
             :fontSize "13px"}}
    (. tabs
       (map (fn [t i]
              (var id (. t ["id"]))
              (var label (. t ["label"]))
              (var is-active (== id activeTab))
              (return
               [:button
                {:key id
                 :style {:background (:? is-active "#fff" "transparent")
                         :border "1px solid #ddd"
                         :borderBottom (:? is-active "1px solid #fff" "1px solid #ddd")
                         :borderRadius "4px 4px 0 0"
                         :padding "4px 12px"
                         :cursor "pointer"
                         :fontSize "12px"
                         :fontWeight (:? is-active 600 400)
                         :marginBottom "-1px"}
                 :onClick (fn [] (setActiveTab id))}
                label]))))]))

(defn.js ActiveTabPanel
  "renders the content area for the active tab"
  {:added "4.0"}
  [{:# [activeTab
        stage
        tabContent]}]
  (return
   [:div
    {:style {:flex 1
             :padding "20px"
             :overflow "auto"
             :background "#fafafa"}}
    (:? (== activeTab "stage")
        (:? stage
            stage
            [:div
             {:style {:color "#999"
                      :marginTop "40px"
                      :textAlign "center"}}
             [:h2 nil "Stage"]
             [:p nil "Push a React element from the REPL:"]
             [:pre
              {:style {:background "#fff"
                       :padding "12px"
                       :borderRadius "4px"
                       :display "inline-block"}}
              "window.PLAYGROUND.setStage(React.createElement(\"div\", null, \"hello\"))"]])
        [:div
         {:id (+ "tab-" activeTab)
          :style {:width "100%" :height "100%"}}
         (:? (. tabContent [activeTab])
             (. tabContent [activeTab])
             [:div
              {:style {:color "#999"
                       :marginTop "40px"
                       :textAlign "center"}}
              [:h2 nil activeTab]
              [:p nil "Use the REPL to set content for this tab:"]
              [:pre
               {:style {:background "#fff"
                        :padding "12px"
                        :borderRadius "4px"
                        :display "inline-block"}}
               (+ "window.PLAYGROUND.setTabContent(\"" activeTab "\", element)")]])])]))

(defn.js get-config
  "reads playground config from window.PLAYGROUND_CONFIG"
  {:added "4.0"}
  []
  (var config (!:G PLAYGROUND_CONFIG))
  (return (or config
              {"title" "JS Playground"
               "tabs" [{"id" "stage" "label" "Stage"}]})))

(defn.js App
  "top-level playground UI with a menu bar, tabs and collapsible message sidebar"
  {:added "4.0"}
  [#{[config]}]
  (var [status setStatus] (r/local "connecting"))
  (var statusRef (r/ref "connecting"))
  (var [messages setMessages] (r/local []))
  (var messagesRef (r/ref []))
  (var [title setTitle] (r/local (. config ["title"])))
  (var titleRef (r/ref (. config ["title"])))
  (var tabs (. config ["tabs"]))
  (var default-tab (. (or (. tabs [0]) {"id" "stage"}) ["id"]))
  (var [activeTab setActiveTab] (r/local default-tab))
  (var activeTabRef (r/ref default-tab))
  (var [showMessages setShowMessages] (r/local true))
  (var [stage setStage] (r/local nil))
  (var stageRef (r/ref nil))
  (var [tabContent setTabContent] (r/local {}))
  (var tabContentRef (r/ref {}))
  (var add-message (-/make-add-message messagesRef setMessages))
  (var ws-url (+ "ws://" (. window location host) "/ws"))

  (var setStatus* (fn [s]
                    (:= (. statusRef current) s)
                    (setStatus s)))
  (var setTitle* (fn [t]
                   (:= (. titleRef current) t)
                   (:= (. document title) t)
                   (setTitle t)))
  (var setActiveTab* (fn [id]
                       (:= (. activeTabRef current) id)
                       (setActiveTab id)))
  (var setStage* (fn [el]
                   (:= (. stageRef current) el)
                   (setStage el)
                   (setActiveTab* "stage")))
  (var setTabContent* (fn [id el]
                        (var next (Object.assign {} (. tabContentRef current)))
                        (:= (. next [id]) el)
                        (:= (. tabContentRef current) next)
                        (setTabContent next)
                        (setActiveTab* id)))

  (r/init []
          (var PLAYGROUND (!:G PLAYGROUND))
          (:= (. PLAYGROUND ["setStage"]) setStage*)
          (:= (. PLAYGROUND ["setTabContent"]) setTabContent*)
          (:= (. PLAYGROUND ["switchTab"]) setActiveTab*)
          (:= (. PLAYGROUND ["setTitle"]) setTitle*)
          (:= (. PLAYGROUND ["getMessages"]) (fn [] (return (. messagesRef current))))
          (:= (. PLAYGROUND ["getStatus"]) (fn [] (return (. statusRef current))))
          (:= (. PLAYGROUND ["getActiveTab"]) (fn [] (return (. activeTabRef current))))
          (:= (. PLAYGROUND ["getTitle"]) (fn [] (return (. titleRef current))))
          (:= (. PLAYGROUND ["send"])
              (fn [data]
                (var ws (. PLAYGROUND ["ws"]))
                (if (and ws (== (. ws readyState) 1))
                  (. ws (send (JSON.stringify data))))))
          (var ws (new WebSocket ws-url))
          (:= (. PLAYGROUND ["ws"]) ws)
          (:= (. ws onopen) (fn [] (setStatus* "connected")))
          (:= (. ws onclose) (fn [] (setStatus* "disconnected")))
          (:= (. ws onerror) (fn [] (setStatus* "error")))
          (:= (. ws onmessage)
              (fn [event]
                (-/run-eval ws add-message messagesRef setMessages (JSON.parse (. event ["data"])))))
          (return (fn [] (. ws (close)))))

  (return
   [:div
    {:style {:display "flex"
             :flexDirection "column"
             :height "100vh"
             :fontFamily "system-ui, sans-serif"}}
    [:% -/TopMenu {:title title
                   :status status
                   :url ws-url
                   :messages messages
                   :showMessages showMessages
                   :setShowMessages setShowMessages}]
    [:% -/TabBar {:tabs tabs
                  :activeTab activeTab
                  :setActiveTab setActiveTab*}]
    [:div
     {:style {:display "flex"
              :flex 1
              :overflow "hidden"}}
     [:% -/ActiveTabPanel {:activeTab activeTab
                           :stage stage
                           :tabContent tabContent}]
     (:? showMessages
         [:div
          {:id "sidebar"
           :style {:width "360px"
                   :display "flex"
                   :flexDirection "column"
                   :background "#fff"}}
          [:% -/MessageList {:messages messages}]]
         nil)]]))

(defn.js mount!
  "mounts the playground app into #root and exposes global React and PLAYGROUND"
  {:added "4.0"}
  []
  (var config (-/get-config))
  (:= (!:G React) React)
  (:= (!:G ReactDOM) ReactDOM)
  (:= (!:G PLAYGROUND)
      {"setStage" (fn [el] el)
       "setTabContent" (fn [id el] [id el])
       "switchTab" (fn [id] id)
       "setTitle" (fn [t] t)
       "getMessages" (fn [] [])
       "getStatus" (fn [] "connecting")
       "getActiveTab" (fn [] "stage")
       "getTitle" (fn [] "")
       "send" (fn [data] data)
       "ws" null
       "stage" null})
  (r/renderDOMRoot "root" (fn [] (return [:% -/App {:config config}]))))
