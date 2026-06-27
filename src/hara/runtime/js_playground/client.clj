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

(defn.js make-tab-id
  "returns a unique tab id"
  {:added "4.1"}
  [label]
  (var prefix (or label "tab"))
  (return (+ "tab-" prefix "-" (xt/x:now-ms))))

(defn.js find-tab-index
  "finds the index of a tab by id"
  {:added "4.1"}
  [tabs id]
  (return (. tabs
             (findIndex (fn [t]
                          (return (== (. t ["id"]) id)))))))

(defn.js has-tab?
  "checks whether a tab exists"
  {:added "4.1"}
  [tabs id]
  (return (>= (-/find-tab-index tabs id) 0)))

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
  "top menu bar with a compact connection status dropdown"
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
              :gap "8px"}}
     [:div
      {:style {:position "relative"}}
      [:button
       {:style {:display "flex"
                :alignItems "center"
                :gap "8px"
                :background "#f5f5f5"
                :border "1px solid #ddd"
                :borderRadius "999px"
                :padding "6px 10px"
                :cursor "pointer"
                :fontSize "12px"
                :fontWeight 600}
        :onClick (fn [] (setDropdownOpen (not dropdownOpen)))}
       [:span
        {:style {:width "10px"
                 :height "10px"
                 :borderRadius "50%"
                 :background color
                 :display "inline-block"}}]
       "connected"
       [:span
        {:style {:fontSize "10px" :color "#888" :lineHeight 1}}
        "▾"]]
      (:? dropdownOpen
          [:div
           {:style {:position "absolute"
                    :top "38px"
                    :right "0"
                    :width "340px"
                    :overflow "hidden"
                    :background "#fff"
                    :border "1px solid #ddd"
                    :borderRadius "10px"
                    :boxShadow "0 4px 12px rgba(0,0,0,0.15)"
                    :zIndex 100}}
           [:div
            {:style {:padding "10px 12px"
                     :borderBottom "1px solid #eee"
                     :fontWeight 600
                     :fontSize "12px"}}
            [:div
             {:style {:display "flex"
                      :alignItems "center"
                      :gap "8px"}}
             [:span
              {:style {:width "10px"
                       :height "10px"
                       :borderRadius "50%"
                       :background color
                       :display "inline-block"}}]
             [:span nil status]]]
           [:div
            {:style {:padding "10px 12px"
                     :borderBottom "1px solid #eee"
                     :color "#666"
                     :fontSize "12px"}}
            url]
           [:div
            {:style {:padding "10px 12px"
                     :textAlign "center"}}
            [:button
             {:style {:background "transparent"
                      :border "1px solid #ddd"
                      :borderRadius "999px"
                      :padding "6px 12px"
                      :color "#2196f3"
                      :cursor "pointer"
                      :fontSize "12px"}
              :onClick (fn []
                         (do (setShowMessages (not showMessages))
                             (setDropdownOpen false)))}
             (:? showMessages "Hide message log" (+ "Show message log (" (. messages length) ")"))]]]
          nil)]]]))

(defn.js TabBar
  "renders the configurable tab bar"
  {:added "4.0"}
  [{:# [tabs
        activeTab
        setActiveTab
        createTab
        closeTab]}]
  (var tab-buttons
    (. tabs
       (map (fn [t i]
              (var id (. t ["id"]))
              (var label (. t ["label"]))
              (var is-active (== id activeTab))
              (return
               [:div
                {:key id
                 :style {:display "flex"
                         :alignItems "center"
                         :gap "4px"}}
                [:button
                 {:style {:display "flex"
                          :alignItems "center"
                          :gap "8px"
                          :background (:? is-active "#fff" "transparent")
                          :border "1px solid #ddd"
                          :borderBottom (:? is-active "1px solid #fff" "1px solid #ddd")
                          :borderRadius "10px 10px 0 0"
                          :padding "5px 10px"
                          :cursor "pointer"
                          :fontSize "12px"
                          :fontWeight (:? is-active 700 500)
                          :marginBottom "-1px"}
                  :onClick (fn [] (setActiveTab id))}
                 [:span nil label]]
                (:? (not= id "stage")
                    [:button
                     {:style {:background "transparent"
                              :border "none"
                              :padding "0"
                              :cursor "pointer"
                              :fontSize "12px"
                              :lineHeight 1
                              :color "#999"}
                      :onClick (fn [e]
                                 (do (. e (stopPropagation))
                                     (closeTab id)))}
                     "×"]
                    nil)]))))))
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
    tab-buttons
    [:button
     {:style {:background "#fff"
              :border "1px solid #ddd"
              :borderBottom "1px solid #fff"
              :borderRadius "10px 10px 0 0"
              :padding "5px 10px"
              :cursor "pointer"
              :fontSize "12px"
              :fontWeight 700
              :marginBottom "-1px"}
      :onClick (fn [] (createTab nil nil))}
     "+"]])

(defn.js ActiveTabPanel
  "renders the content area for the active tab"
  {:added "4.0"}
  [{:# [activeTab
        tabs
        stage
        tabContent]}]
  (var active-tab (. tabs
                     (find (fn [t]
                             (return (== (. t ["id"]) activeTab))))))
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
              [:h2 nil (:? active-tab (. active-tab ["label"]) activeTab)]
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
  (var default-tabs (or (. config ["tabs"])
                        [{"id" "stage" "label" "Stage"}]))
  (var [tabs setTabs] (r/local default-tabs))
  (var tabsRef (r/ref default-tabs))
  (var default-tab (. (or (. default-tabs [0]) {"id" "stage"}) ["id"]))
  (var [activeTab setActiveTab] (r/local default-tab))
  (var activeTabRef (r/ref default-tab))
  (var [showMessages setShowMessages] (r/local true))
  (var [stage setStage] (r/local nil))
  (var stageRef (r/ref nil))
  (var [tabContent setTabContent] (r/local {}))
  (var tabContentRef (r/ref {}))
  (var add-message (-/make-add-message messagesRef setMessages))
  (var ws-url (+ "ws://" (. window location host) "/ws"))
  (var setActiveTab* (fn [id]
                       (var next-id (if (-/has-tab? (. tabsRef current) id)
                                      id
                                      (. (or (. tabsRef current [0]) {"id" "stage"}) ["id"])))
                       (:= (. activeTabRef current) next-id)
                       (setActiveTab next-id)))
  (var setTabs* (fn [next-tabs preferred]
                  (var normalized (or next-tabs []))
                  (:= (. tabsRef current) normalized)
                  (setTabs normalized)
                  (var fallback (. (or (. normalized [0]) {"id" "stage"}) ["id"]))
                  (var next-active (or preferred (. activeTabRef current) fallback))
                  (if (-/has-tab? normalized next-active)
                    (setActiveTab* next-active)
                    (setActiveTab* fallback))))
  (var createTab* (fn [id label]
                    (var next-id (or id (-/make-tab-id label)))
                    (var new-tab {"id" next-id
                                  "label" (or label next-id)})
                    (var next (. (. tabsRef current) (slice)))
                    (if (not (-/has-tab? next next-id))
                      (. next (push new-tab)))
                    (setTabs* next next-id)
                    (setActiveTab* next-id)
                    (return new-tab)))
  (var closeTab* (fn [id]
                   (if (== id "stage")
                     (return nil))
                   (var next (. (. tabsRef current) (filter (fn [t]
                                                             (return (not= (. t ["id"]) id))))))
                   (var next-content (Object.assign {} (. tabContentRef current)))
                   (delete (. next-content [id]))
                   (:= (. tabContentRef current) next-content)
                   (setTabContent next-content)
                   (setTabs* next)
                   (if (== (. activeTabRef current) id)
                     (setActiveTab* (. (or (. next [0]) {"id" "stage"}) ["id"])))
                   (return id)))
  (var setStatus* (fn [s]
                    (:= (. statusRef current) s)
                    (setStatus s)))
  (var setTitle* (fn [t]
                   (:= (. titleRef current) t)
                   (:= (. document title) t)
                   (setTitle t)))
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
          (:= (. PLAYGROUND ["createTab"]) createTab*)
          (:= (. PLAYGROUND ["closeTab"]) closeTab*)
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
                (-/run-eval ws add-message messagesRef setMessages (JSON.parse (. event ["data"]))))))
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
                  :setActiveTab setActiveTab*
                  :createTab createTab*
                  :closeTab closeTab*}]
    [:div
     {:style {:display "flex"
              :flex 1
              :overflow "hidden"}}
     [:% -/ActiveTabPanel {:activeTab activeTab
                           :tabs tabs
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
         nil)]] )

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
       "createTab" (fn [spec label] spec)
       "closeTab" (fn [id] id)
       "setTitle" (fn [t] t)
       "getMessages" (fn [] [])
       "getStatus" (fn [] "connecting")
       "getActiveTab" (fn [] "stage")
       "getTitle" (fn [] "")
       "send" (fn [data] data)
       "ws" null
       "stage" null})
  (r/renderDOMRoot "root" (fn [] (return [:% -/App {:config config}]))))
