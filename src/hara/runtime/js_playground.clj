(ns hara.runtime.js-playground
  "Browser playground runtime backed by a WebSocket connection.

   Unlike the runtimes in `hara.runtime.basic`, which target generic JS
   evaluation environments, this runtime is purpose-built for the browser. It
   starts an http-kit server, serves a generated HTML page with a split-screen
   React UI, and evaluates Clojure forms sent over `/ws` live in the browser."
  (:require [clojure.string]
            [hara.lang :as l]
            [hara.lang.runtime :as default]
            [hara.runtime.basic.type-common :as common]
            [org.httpkit.server :as server]
            [std.fs :as fs]
            [std.html :as html]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.network :as network]
            [std.lib.security :as security]
            [std.protocol.context :as protocol.context]))

(def +pre-arranged+
  "pre-arranged ports for well-known playground instances"
  {:dev/ws-play 29002})

(defn- content-type
  "guesses a content type for a static file path"
  [path]
  (condp #(.endsWith %2 %1) path
    ".html" "text/html"
    ".js"   "application/javascript"
    ".css"  "text/css"
    ".json" "application/json"
    "application/octet-stream"))

(defn- playground-client-script
  "emits the browser-side React playground app as JS via hara.lang.

   Renders a split-screen UI: a stage on the left for components pushed from
   the REPL, and a sidebar on the right showing connection status, sent eval
   requests, and received replies. Exposes window.PLAYGROUND.setStage."
  []
  (l/emit-script
   '(do
      (defn send-response [ws id status body]
        (. ws (send (. JSON stringify {"id" id "status" status "body" body}))))

      (defn run-eval [ws add-message msg]
        (var id (. msg ["id"]))
        (var body (. msg ["body"]))
        (add-message {"type" "sent"
                      "id" id
                      "body" (. body (slice 0 500))
                      "time" (. Date (now))})
        (try
          (var result (eval body))
          (defn resolve [value]
            (var safe (:? (== value undefined) null value))
            (var safe-text (. JSON stringify safe))
            (send-response ws id "ok" safe)
            (add-message {"type" "reply"
                          "id" id
                          "status" "ok"
                          "body" (. safe-text (slice 0 500))
                          "time" (. Date (now))}))
          (defn reject [err]
            (var text (or (. err ["message"]) (String err)))
            (send-response ws id "error" text)
            (add-message {"type" "reply"
                          "id" id
                          "status" "error"
                          "body" text
                          "time" (. Date (now))}))
          (if (and result (== (typeof (. result then)) "function"))
            (. result (then resolve reject))
            (resolve result))
          (catch err
            (var text (or (. err ["message"]) (String err)))
            (send-response ws id "error" text)
            (add-message {"type" "reply"
                          "id" id
                          "status" "error"
                          "body" text
                          "time" (. Date (now))}))))

      (var -set-stage null)
      (var -set-status null)
      (var -add-message null)
      (var -ws null)

      (:= (!:G PLAYGROUND)
          {"setStage" (fn [el]
                        (if -set-stage
                          (-set-stage el)))
           "send" (fn [data]
                    (if (and -ws (== (. -ws readyState) 1))
                      (. -ws (send (. JSON stringify data)))))
           "ws" null
           "stage" null})

      (defn App []
        (var React (!:G React))
        (var useState (. React useState))
        (var useEffect (. React useEffect))
        (var useRef (. React useRef))

        (var status-state (useState "connecting"))
        (var status (. status-state ["0"]))
        (:= -set-status (. status-state ["1"]))

        (var messages-ref (useRef []))
        (var messages-state (useState []))
        (var messages (. messages-state ["0"]))
        (var set-messages (. messages-state ["1"]))

        (defn add-message [m]
          (. messages-ref current (push m))
          (if (> (. messages-ref current length) 200)
            (. messages-ref current (splice 0 (- (. messages-ref current length) 200))))
          (set-messages (. messages-ref current (slice))))

        (:= -add-message add-message)

        (var stage-state (useState null))
        (var stage-content (. stage-state ["0"]))
        (var set-stage-content (. stage-state ["1"]))

        (:= -set-stage (fn [el]
                         (set-stage-content el)
                         (:= (. (!:G PLAYGROUND) ["stage"]) el)))

        (useEffect (fn []
                     (var ws-url (+ "ws://" (. window location host) "/ws"))
                     (var ws (new WebSocket ws-url))
                     (:= -ws ws)
                     (:= (. (!:G PLAYGROUND) ["ws"]) ws)
                     (:= (. ws onopen) (fn [] (-set-status "connected")))
                     (:= (. ws onclose) (fn [] (-set-status "disconnected")))
                     (:= (. ws onerror) (fn [] (-set-status "error")))
                     (:= (. ws onmessage)
                         (fn [event]
                           (run-eval ws add-message (. JSON parse (. event ["data"])))))
                     (fn [] (. ws (close))))
                   [])

        (var status-color (:? (== status "connected") "#4caf50"
                              (:? (== status "error") "#f44336" "#ff9800")))

        (var stage-placeholder
             (. React (createElement "div"
                                     {"style" {"color" "#999"
                                               "marginTop" "40px"
                                               "textAlign" "center"}}
                                     (. React (createElement "h2" nil "Stage"))
                                     (. React (createElement "p" nil "Push a React element from the REPL:"))
                                     (. React (createElement "pre"
                                                             {"style" {"background" "#fff"
                                                                       "padding" "12px"
                                                                       "borderRadius" "4px"
                                                                       "display" "inline-block"}}
                                                             "window.PLAYGROUND.setStage(React.createElement(\"div\", null, \"hello\"))")))))

        (var message-list
             (:? (== (. messages length) 0)
                 (. React (createElement "div"
                                         {"style" {"color" "#999" "fontSize" "12px"}}
                                         "No messages yet."))
                 (. messages
                    (map (fn [m i]
                           (return (. React (createElement
                                             "div"
                                             {"key" i
                                              "style" {"marginBottom" "10px"
                                                       "padding" "10px"
                                                       "borderRadius" "6px"
                                                       "fontSize" "12px"
                                                       "background" (:? (== (. m ["type"]) "sent")
                                                                       "#e3f2fd"
                                                                       "#e8f5e9")
                                                       "borderLeft" (+ "4px solid "
                                                                       (:? (== (. m ["type"]) "sent")
                                                                           "#2196f3"
                                                                           "#4caf50"))}}
                                             (. React (createElement
                                                       "div"
                                                       {"style" {"fontWeight" "600"
                                                                 "marginBottom" "4px"
                                                                 "textTransform" "uppercase"
                                                                 "fontSize" "10px"
                                                                 "color" "#555"}}
                                                       (. m ["type"])
                                                       " "
                                                       (. (or (. m ["id"]) "") (slice 0 8))))
                                             (:? (. m ["status"])
                                                 (. React (createElement
                                                           "div"
                                                           {"style" {"fontSize" "10px"
                                                                     "color" (:? (== (. m ["status"]) "ok")
                                                                                 "#4caf50"
                                                                                 "#f44336")}}
                                                           (. m ["status"])))
                                                 null)
                                             (. React (createElement
                                                       "pre"
                                                       {"style" {"margin" "6px 0 0 0"
                                                                 "whiteSpace" "pre-wrap"
                                                                 "wordBreak" "break-all"
                                                                 "fontSize" "11px"}}
                                                       (. m ["body"]))))))))))

        (return (. React (createElement
                          "div"
                          {"style" {"display" "flex"
                                    "height" "100vh"
                                    "fontFamily" "system-ui, sans-serif"}}
                          (. React (createElement
                                    "div"
                                    {"id" "stage"
                                     "style" {"flex" 1
                                              "padding" "20px"
                                              "overflow" "auto"
                                              "borderRight" "1px solid #ddd"
                                              "background" "#fafafa"}}
                                    (or stage-content stage-placeholder)))
                          (. React (createElement
                                    "div"
                                    {"id" "sidebar"
                                     "style" {"width" "360px"
                                              "display" "flex"
                                              "flexDirection" "column"
                                              "background" "#fff"}}
                                    (. React (createElement
                                              "div"
                                              {"style" {"padding" "12px 16px"
                                                        "borderBottom" "1px solid #ddd"
                                                        "fontWeight" "600"
                                                        "display" "flex"
                                                        "alignItems" "center"
                                                        "gap" "8px"}}
                                              (. React (createElement
                                                        "span"
                                                        {"style" {"width" "10px"
                                                                  "height" "10px"
                                                                  "borderRadius" "50%"
                                                                  "background" status-color
                                                                  "display" "inline-block"}}))
                                              "Status: "
                                              status))
                                    (. React (createElement
                                              "div"
                                              {"style" {"padding" "8px 16px"
                                                        "borderBottom" "1px solid #ddd"
                                                        "fontSize" "12px"
                                                        "color" "#666"}}
                                              (+ "ws://" (. window location host) "/ws")))
                                    (. React (createElement
                                              "div"
                                              {"style" {"flex" 1
                                                        "overflow" "auto"
                                                        "padding" "12px"}}
                                              message-list)))))))

      (var ReactDOM (!:G ReactDOM))
      (var root (. ReactDOM (createRoot (. document (getElementById "root")))))
      (. root (render (. React (createElement App)))))
   {:lang :js
    :layout :flat}))

(defn- page-html
  "renders the js playground page"
  [{:keys [title head body scripts]
    :or {title "hara.runtime js playground"
         body [:div {:id "root"}]}}]
  (let [csp (clojure.string/join ";"
                                 ["default-src * 'unsafe-eval'"
                                  "connect-src * 'unsafe-eval' ws: wss:"
                                  "script-src  'self' 'unsafe-eval' 'unsafe-inline' https://cdn.jsdelivr.net"
                                  "worker-src  blob: data: 'self' 'unsafe-eval' 'unsafe-inline'"])
        body (if (string? body)
               body
               (html/html body))
        head (cond (nil? head) ""
                   (string? head) head
                   :else (html/html head))
        scripts (apply str (map (fn [src]
                                  (str "<script src=\"" src "\"></script>"))
                                scripts))]
    (str "<!doctype html>"
         "<html><head>"
         "<meta http-equiv=\"Content-Security-Policy\" content=\"" csp "\">"
         "<title>" title "</title>"
         head
         "</head><body>"
         body
         "<script src=\"https://cdn.jsdelivr.net/npm/react@18/umd/react.development.js\" crossorigin></script>"
         "<script src=\"https://cdn.jsdelivr.net/npm/react-dom@18/umd/react-dom.development.js\" crossorigin></script>"
         scripts
         "<script>" (playground-client-script) "</script>"
         "</body></html>")))

(defn- serve-file
  "serves a file from the playground root"
  [^String root ^String path]
  (let [file (java.io.File. root path)]
    (cond (.exists file)
          {:status 200
           :headers {"Content-Type" (content-type path)}
           :body file}

          :else
          {:status 404
           :headers {"Content-Type" "text/plain"}
           :body "not found"})))

(defn- websocket-receive
  "handles an incoming websocket message, delivering responses to pending evals"
  [msg return channel]
  (cond (= msg "ping")
        (server/send! channel "pong")

        :else
        (let [{:keys [id] :as data} (json/read msg json/+keyword-case-mapper+)]
          (when-let [p (get @return id)]
            (deliver p data)
            (swap! return dissoc id)))))

(defn- playground-handler
  "Ring handler that serves static playground assets and upgrades /ws to WebSocket"
  [channel return root]
  (fn [request]
    (if (:websocket? request)
      (server/with-channel request ch
        (if @channel (server/close @channel))
        (reset! channel ch)
        (server/on-close ch
                         (fn [status]
                           (when (= @channel ch)
                             (reset! channel nil))))
        (server/on-receive ch
                           (fn [msg]
                             (websocket-receive msg return ch))))
      (let [uri (:uri request)
            path (if (= "/" uri) "index.html" (subs uri 1))]
        (serve-file root path)))))

(defn start-js-playground
  "starts the js playground server and returns the runtime with state attached"
  {:added "4.0"}
  [{:keys [id port host] :as rt}]
  (let [root (str (fs/create-tmpdir))
        _    (spit (str root "/index.html") (page-html {}))
        port (network/port:check-available (or port
                                               (+pre-arranged+ id)
                                               0))
        port (if (boolean? port) 0 port)
        channel (atom nil)
        return  (atom {})
        handler (playground-handler channel return root)
        stop-fn (server/run-server handler {:port port
                                            :ip   "0.0.0.0"})
        _ (network/wait-for-port "localhost" port)]
    (assoc rt
           :root root
           :port port
           :host (or host
                     (network/local-ip-lan)
                     (network/local-ip))
           :channel channel
           :return return
           :stop stop-fn)))

(defn stop-js-playground
  "stops the js playground server"
  {:added "4.0"}
  [{:keys [channel stop] :as rt}]
  (when channel
    (when-let [ch @channel]
      (server/close ch)))
  (when stop
    (stop))
  rt)

(defn raw-eval-js-playground
  "raw eval for js playground runtime

   Sends the emitted JS body to the connected browser and returns the parsed
   result, or a status map when disconnected or timed out."
  {:added "4.0"}
  ([{:keys [channel return process] :as rt} body]
   (cond (not @channel)
         {:status "not-connected"}

         :else
         (let [msg-id (str (f/uuid))
               p (promise)
               timeout (or (:timeout process) 2000)
               _ (swap! return assoc msg-id p)
               _ (server/send! @channel
                               (json/write {:id msg-id :body body}))
               output (deref p timeout {:status "timeout"})]
           (if (= "ok" (:status output))
             (:body output)
             output)))))

(defn invoke-ptr-js-playground
  "invoke for js playground runtime"
  {:added "4.0"}
  ([rt ptr args]
   (default/default-invoke-script rt ptr args raw-eval-js-playground (:process rt))))

(defn rt-js-playground-string
  "string representation of the js playground runtime"
  {:added "4.0"}
  [{:keys [id lang port host]}]
  (str "#rt.js-playground" [lang id host port]))

(impl/defimpl RuntimeJsPlayground [id]
  :string rt-js-playground-string
  :protocols [std.protocol.component/IComponent
              :method {-start start-js-playground
                       -stop stop-js-playground
                       -kill stop-js-playground}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-js-playground
                       -invoke-ptr  invoke-ptr-js-playground}])

(defn rt-js-playground:create
  "creates a js playground runtime"
  {:added "4.0"}
  [{:keys [id lang runtime process]
    :as m
    :or {runtime :playground}}]
  (map->RuntimeJsPlayground
   (merge m
          {:id (or id (f/sid))
           :tag runtime
           :runtime runtime
           :process (merge {:json false
                            :timeout 2000}
                           (or process {}))})))

(defn rt-js-playground
  "creates and starts a js playground runtime"
  {:added "4.0"}
  [{:keys [id lang runtime process] :as m}]
  (-> (rt-js-playground:create m)
      (component/start)))

(defn play-url
  "gets the js playground url for a runtime instance

   The returned URL is reachable on the local network so a browser can load
   the page and connect back over the WebSocket."
  {:added "4.0"}
  [{:keys [host port]}]
  (str "http://" host ":" port "/index.html"))

(defn play-file
  "gets the file path in the js playground runtime's served root"
  {:added "4.0"}
  [{:keys [root]} & paths]
  (clojure.string/join "/" (cons root paths)))

(defn play-script
  "emits Clojure forms to JavaScript and writes the script into the served root.

   With `as-script` (truthy), returns the raw JS string instead of the filename."
  {:added "4.0"}
  [rt forms & [as-script layout]]
  (let [script (l/emit-script (cons 'do forms) {:lang :js
                                                :layout (or layout :flat)})]
    (cond as-script
          script

          :else
          (let [sha (security/sha1 script)
                filename (str (:root rt) "/" sha ".js")
                _ (when (not (fs/exists? filename))
                    (spit filename script))]
            (str sha ".js")))))

(defn play-page
  "creates a page asset in the js playground runtime's served root"
  {:added "4.0"}
  [rt m & [as-script]]
  (let [page (page-html m)]
    (if as-script
      page
      (let [sha (security/sha1 page)
            name (or (:name m) "page")
            filename (str name "-" sha ".html")
            _ (spit (str (:root rt) "/" filename) page)]
        filename))))

(def +config+
  "default context options for the js playground runtime"
  (common/set-context-options
   [:js :playground :default]
   {:bootstrap false
    :main  {}
    :emit  {:native {:suppress true}
            :body  {:transform #'default/return-transform}
            :lang/jsx false
            :lang/format :global}
    :json false
    :encode :json
    :timeout 2000}))

(def +install+
  "runtime type registration for [:js :playground]"
  [(default/install-type!
    :js :playground
    {:type :hara/rt.js-playground
     :instance {:create #'rt-js-playground:create}
     :config {:layout :full}})])

(comment
  (def rt (rt-js-playground {:lang :js :port 0}))
  (play-url rt)
  ;; open the URL in a browser, then:
  ;; (!.js (+ 1 2 3))
  (component/stop rt))
