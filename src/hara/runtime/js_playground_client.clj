^{:no-test true}
(ns hara.runtime.js-playground-client
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
  (cond (== null body) ""
        (== undefined body) ""
        (== "string" (typeof body)) body
        :else (JSON.stringify body null 2)))

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
  "creates the add-message callback used by the sidebar log"
  {:added "4.0"}
  [messagesRef setMessages]
  (return (fn [m]
            (. messagesRef current (push m))
            (if (> (. messagesRef current length) 200)
              (. messagesRef current (splice 0 (- (. messagesRef current length) 200))))
            (setMessages (. messagesRef current (slice))))))

(defn.js run-eval
  "handles an incoming eval message, evaluates it and sends the response"
  {:added "4.0"}
  [ws add-message msg]
  (var id (. msg ["id"]))
  (var body (. msg ["body"]))
  (add-message {"type" "sent"
                "id" id
                "body" body
                "time" (xt/x:now-ms)})
  (try
    (var [status value] (-/eval-body body))
    (-/send-response ws id status value)
    (add-message {"type" "reply"
                  "id" id
                  "status" status
                  "body" (-/format-body value)
                  "time" (xt/x:now-ms)})
    (catch err
      (var text (or (. err ["message"]) (String err)))
      (-/send-response ws id "error" text)
      (add-message {"type" "reply"
                    "id" id
                    "status" "error"
                    "body" text
                    "time" (xt/x:now-ms)}))))

(defn.js MessageItem
  "renders a single expandable message with a print-to-console button"
  {:added "4.0"}
  [{:# [m]}]
  (var [expanded setExpanded] (r/local))
  (var type (. m ["type"]))
  (var status (. m ["status"]))
  (var body (-/format-body (. m ["body"])))
  (var preview-limit 120)
  (var is-long (> (. body length) preview-limit))
  (var display-body (:? (and (not expanded) is-long)
                        (+ (. body (slice 0 preview-limit)) "...")
                        body))
  (var button-style {:background "transparent"
                     :border "1px solid #ccc"
                     :borderRadius "4px"
                     :padding "2px 6px"
                     :cursor "pointer"
                     :fontSize "10px"
                     :color "#555"})
  (return
   [:div
    {:style {:marginBottom "10px"
             :padding "10px"
             :borderRadius "6px"
             :fontSize "12px"
             :background (:? (== type "sent") "#e3f2fd" "#e8f5e9")
             :borderLeft (+ "4px solid "
                            (:? (== type "sent") "#2196f3" "#4caf50"))}}
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
      type " " (. (or (. m ["id"]) "") (slice 0 8))]
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
    (:? status
        [:div
         {:style {:fontSize "10px"
                  :color (:? (== status "ok") "#4caf50" "#f44336")}}
         status]
        nil)
    [:pre
     {:style {:margin "6px 0 0 0"
              :whiteSpace "pre-wrap"
              :wordBreak "break-all"
              :fontSize "11px"}}
     display-body]]))

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
  [{:# [status
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
     "JS Playground"]
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

(defn.js App
  "top-level playground UI with a menu bar and collapsible message sidebar"
  {:added "4.0"}
  []
  (var [status setStatus] (r/local "connecting"))
  (var messagesRef (r/ref []))
  (var [messages setMessages] (r/local []))
  (var [stage setStage] (r/local nil))
  (var [showMessages setShowMessages] (r/local true))
  (var add-message (-/make-add-message messagesRef setMessages))
  (var ws-url (+ "ws://" (. window location host) "/ws"))

  (r/init []
          (var PLAYGROUND (!:G PLAYGROUND))
          (:= (. PLAYGROUND ["setStage"]) (fn [el] (setStage el)))
          (:= (. PLAYGROUND ["send"])
              (fn [data]
                (var ws (. PLAYGROUND ["ws"]))
                (if (and ws (== (. ws readyState) 1))
                  (. ws (send (JSON.stringify data))))))
          (var ws (new WebSocket ws-url))
          (:= (. PLAYGROUND ["ws"]) ws)
          (:= (. ws onopen) (fn [] (setStatus "connected")))
          (:= (. ws onclose) (fn [] (setStatus "disconnected")))
          (:= (. ws onerror) (fn [] (setStatus "error")))
          (:= (. ws onmessage)
              (fn [event]
                (-/run-eval ws add-message (JSON.parse (. event ["data"])))))
          (return (fn [] (. ws (close)))))

  (return
   [:div
    {:style {:display "flex"
             :flexDirection "column"
             :height "100vh"
             :fontFamily "system-ui, sans-serif"}}
    [:% -/TopMenu {:status status
                   :url ws-url
                   :messages messages
                   :showMessages showMessages
                   :setShowMessages setShowMessages}]
    [:div
     {:style {:display "flex"
              :flex 1
              :overflow "hidden"}}
     [:div
      {:id "stage"
       :style {:flex 1
               :padding "20px"
               :overflow "auto"
               :borderRight "1px solid #ddd"
               :background "#fafafa"}}
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
            "window.PLAYGROUND.setStage(React.createElement(\"div\", null, \"hello\"))"]])]
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
  (:= (!:G React) React)
  (:= (!:G ReactDOM) ReactDOM)
  (:= (!:G PLAYGROUND)
      {"setStage" (fn [el] el)
       "send" (fn [data] data)
       "ws" null
       "stage" null})
  (r/renderDOMRoot "root" -/App))
