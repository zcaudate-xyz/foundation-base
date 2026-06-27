^{:no-test true}
(ns hara.runtime.js-playground-client
  "Browser-side playground client.

   Written as a proper `l/script :js` namespace so it can be compiled with
   `js.react`, `xt.lang.common-lib/return-eval` and the rest of the xt/js
   ecosystem. It is emitted by `hara.runtime.js-playground` and served to the
   browser as an ES module."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]]})

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
                  "body" value
                  "time" (xt/x:now-ms)})
    (catch err
      (var text (or (. err ["message"]) (String err)))
      (-/send-response ws id "error" text)
      (add-message {"type" "reply"
                    "id" id
                    "status" "error"
                    "body" text
                    "time" (xt/x:now-ms)}))))

(defn.js StatusBar
  "renders the connection status pill and websocket URL"
  {:added "4.0"}
  [#{[status url]}]
  (var color (:? (== status "connected") "#4caf50"
                 (:? (== status "error") "#f44336" "#ff9800")))
  (return
   [:% "div"
    {:style {:padding "12px 16px"
             :borderBottom "1px solid #ddd"
             :fontWeight 600
             :display "flex"
             :alignItems "center"
             :gap "8px"}}
    [:% "span"
     {:style {:width "10px"
              :height "10px"
              :borderRadius "50%"
              :background color
              :display "inline-block"}}]
    "Status: " status
    [:% "div"
     {:style {:marginLeft "auto"
              :fontSize "12px"
              :color "#666"
              :fontWeight 400}}
     url]]))

(defn.js MessageItem
  "renders a single message in the sidebar log"
  {:added "4.0"}
  [#{[m]}]
  (var type (. m ["type"]))
  (var status (. m ["status"]))
  (return
   [:% "div"
    {:style {:marginBottom "10px"
             :padding "10px"
             :borderRadius "6px"
             :fontSize "12px"
             :background (:? (== type "sent") "#e3f2fd" "#e8f5e9")
             :borderLeft (+ "4px solid "
                            (:? (== type "sent") "#2196f3" "#4caf50"))}}
    [:% "div"
     {:style {:fontWeight 600
              :marginBottom "4px"
              :textTransform "uppercase"
              :fontSize "10px"
              :color "#555"}}
     type " " (. (or (. m ["id"]) "") (slice 0 8))]
    (:? status
        [:% "div"
         {:style {:fontSize "10px"
                  :color (:? (== status "ok") "#4caf50" "#f44336")}}
         status]
        nil)
    [:% "pre"
     {:style {:margin "6px 0 0 0"
              :whiteSpace "pre-wrap"
              :wordBreak "break-all"
              :fontSize "11px"}}
     (. m ["body"])]]))

(defn.js MessageList
  "renders the list of sent/received messages"
  {:added "4.0"}
  [#{[messages]}]
  (return
   [:% "div"
    {:style {:flex 1
             :overflow "auto"
             :padding "12px"}}
    (:? (== (. messages length) 0)
        [:% "div"
         {:style {:color "#999" :fontSize "12px"}}
         "No messages yet."]
        (. messages
           (map (fn [m i]
                  (return [:% -/MessageItem {:key i :m m}])))))]))

(defn.js App
  "top-level split-screen playground UI"
  {:added "4.0"}
  []
  (var [status setStatus] (r/local "connecting"))
  (var messagesRef (r/ref []))
  (var [messages setMessages] (r/local []))
  (var [stage setStage] (r/local nil))
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
   [:% "div"
    {:style {:display "flex"
             :height "100vh"
             :fontFamily "system-ui, sans-serif"}}
    [:% "div"
     {:id "stage"
      :style {:flex 1
              :padding "20px"
              :overflow "auto"
              :borderRight "1px solid #ddd"
              :background "#fafafa"}}
     (:? stage
         stage
         [:% "div"
          {:style {:color "#999"
                   :marginTop "40px"
                   :textAlign "center"}}
          [:% "h2" nil "Stage"]
          [:% "p" nil "Push a React element from the REPL:"]
          [:% "pre"
           {:style {:background "#fff"
                    :padding "12px"
                    :borderRadius "4px"
                    :display "inline-block"}}
           "window.PLAYGROUND.setStage(React.createElement(\"div\", null, \"hello\"))"]])
    [:% "div"
     {:id "sidebar"
      :style {:width "360px"
              :display "flex"
              :flexDirection "column"
              :background "#fff"}}
     [:% -/StatusBar {:status status :url ws-url}]
     [:% -/MessageList {:messages messages}]]]]))

(defn.js mount!
  "mounts the playground app into #root and exposes window.PLAYGROUND"
  {:added "4.0"}
  []
  (:= (!:G PLAYGROUND)
      {"setStage" (fn [el] el)
       "send" (fn [data] data)
       "ws" null
       "stage" null})
  (r/renderDOMRoot "root" -/App))
