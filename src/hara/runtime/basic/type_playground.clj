(ns hara.runtime.basic.type-playground
  "Browser playground runtime backed by a WebSocket connection.

   Unlike `:websocket`, which launches a Node process that connects back
   to the JVM, this runtime starts an http-kit server, serves a generated
   HTML page, and waits for a browser on the LAN to load that page and
   connect over `/ws`. Once connected, `!.js` forms are evaluated live in
   the browser."
  (:require [clojure.string]
            [hara.lang.runtime :as default]
            [org.httpkit.server :as server]
            [std.fs :as fs]
            [std.html :as html]
            [std.json :as json]
            [std.lib.collection :as collection]
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

(def +ws-client+
  "browser-side WebSocket eval client, embedded in every playground page.

   Connects to ws://<current-host>:<current-port>/ws, evals incoming JS,
   and returns the result as JSON."
  "(function(){
  var output = document.getElementById('ws-output');
  if (!output) {
    output = document.createElement('div');
    output.id = 'ws-output';
    document.body.appendChild(output);
  }
  function log(msg) {
    var line = document.createElement('div');
    line.textContent = msg;
    output.appendChild(line);
  }
  var wsUrl = 'ws://' + window.location.host + '/ws';
  var ws = new WebSocket(wsUrl);
  ws.onopen = function() { log('connected to ' + wsUrl); };
  ws.onclose = function() { log('disconnected'); };
  ws.onerror = function(err) { log('error: ' + err); };
  function sendResponse(id, status, body) {
    ws.send(JSON.stringify({id: id, status: status, body: body}));
  }
  ws.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    var id = msg.id;
    var body = msg.body;
    try {
      var result = eval(body);
      var resolve = function(value) {
        var safe = (value === undefined) ? null : value;
        sendResponse(id, 'ok', safe);
        log('eval [' + id + '] ok');
      };
      var reject = function(err) {
        sendResponse(id, 'error', (err.message || String(err)));
        log('eval [' + id + '] error: ' + (err.message || String(err)));
      };
      if (result && typeof result.then === 'function') {
        result.then(resolve, reject);
      } else {
        resolve(result);
      }
    } catch (err) {
      sendResponse(id, 'error', (err.message || String(err)));
      log('eval [' + id + '] error: ' + (err.message || String(err)));
    }
  };
})();")

(defn- page-html
  "renders a websocket playground page"
  [{:keys [title head body scripts]
    :or {title "hara.runtime playground"
         body [:div {:id "playground"} [:h1 "playground"]]}}]
  (let [csp (clojure.string/join ";"
                                 ["default-src * 'unsafe-eval'"
                                  "connect-src * 'unsafe-eval' ws: wss:"
                                  "script-src  'self' 'unsafe-eval' 'unsafe-inline'"
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
         scripts
         "<script>" +ws-client+ "</script>"
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

(defn start-playground
  "starts the playground server and returns the runtime with state attached"
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

(defn stop-playground
  "stops the playground server"
  {:added "4.0"}
  [{:keys [channel stop] :as rt}]
  (when channel
    (when-let [ch @channel]
      (server/close ch)))
  (when stop
    (stop))
  rt)

(defn raw-eval-playground
  "raw eval for playground runtime

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

(defn invoke-ptr-playground
  "invoke for playground runtime"
  {:added "4.0"}
  ([rt ptr args]
   (default/default-invoke-script rt ptr args raw-eval-playground (:process rt))))

(defn rt-playground-string
  "string representation of the playground runtime"
  {:added "4.0"}
  [{:keys [id lang port host]}]
  (str "#rt.playground" [lang id host port]))

(impl/defimpl RuntimePlayground [id]
  :string rt-playground-string
  :protocols [std.protocol.component/IComponent
              :method {-start start-playground
                       -stop stop-playground
                       -kill stop-playground}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-playground
                       -invoke-ptr  invoke-ptr-playground}])

(defn rt-playground:create
  "creates a playground runtime"
  {:added "4.0"}
  [{:keys [id lang runtime process]
    :as m
    :or {runtime :playground}}]
  (map->RuntimePlayground
   (merge m
          {:id (or id (f/sid))
           :tag runtime
           :runtime runtime
           :process (merge {:json false
                            :timeout 2000}
                           (or process {}))})))

(defn rt-playground
  "creates and starts a playground runtime"
  {:added "4.0"}
  [{:keys [id lang runtime process] :as m}]
  (-> (rt-playground:create m)
      (component/start)))

(defn play-url
  "gets the playground url for a runtime instance

   The returned URL is reachable on the local network so a browser can load
   the page and connect back over the WebSocket."
  {:added "4.0"}
  [{:keys [host port]}]
  (str "http://" host ":" port "/index.html"))

(defn play-file
  "gets the file path in the playground runtime's served root"
  {:added "4.0"}
  [{:keys [root]} & paths]
  (clojure.string/join "/" (cons root paths)))

(defn play-script
  "emits Clojure forms to JavaScript and writes the script into the served root.

   With `as-script` (truthy), returns the raw JS string instead of the filename."
  {:added "4.0"}
  [rt forms & [as-script layout]]
  (let [emit-fn (requiring-resolve 'hara.lang/emit-script)]
    (cond as-script
          (emit-fn (cons 'do forms) {:lang :js
                                     :layout (or layout :flat)})

          :else
          (let [script (emit-fn (cons 'do forms) {:lang :js
                                                  :layout (or layout :flat)})
                sha (security/sha1 script)
                filename (str (:root rt) "/" sha ".js")
                _ (when (not (fs/exists? filename))
                    (spit filename script))]
            (str sha ".js")))))

(defn play-page
  "creates a page asset in the playground runtime's served root"
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

(comment
  (def rt (rt-playground {:lang :js :port 0}))
  (play-url rt)
  ;; open the URL in a browser, then:
  ;; (!.js (+ 1 2 3))
  (component/stop rt))
