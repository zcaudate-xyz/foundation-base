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
            [std.protocol.context :as protocol.context]
            [hara.runtime.js-playground-client]))

(def +pre-arranged+
  "pre-arranged ports for well-known playground instances"
  {:dev/ws-play 29002})

(def ^:private +cache-control+
  "headers that tell the browser not to cache playground assets"
  {"Cache-Control" "no-store, no-cache, must-revalidate, max-age=0"
   "Pragma"        "no-cache"
   "Expires"       "0"})

(defn- content-type
  "guesses a content type for a static file path"
  [path]
  (condp #(.endsWith %2 %1) path
    ".html" "text/html"
    ".js"   "application/javascript"
    ".css"  "text/css"
    ".json" "application/json"
    "application/octet-stream"))

(defn playground-client-script
  "emits the browser-side React playground app as JS via hara.lang.

   The client lives in `hara.runtime.js-playground-client` and is compiled to
   a single ES module with `js.react` for the UI and
   `xt.lang.common-lib/return-eval` for safe eval."
  {:added "4.0"}
  []
  (l/emit-script
   '(hara.runtime.js-playground-client/mount!)
   {:lang :js
    :layout :flat
    :module 'hara.runtime.js-playground-client
    :emit {:lang/jsx false
           :override {"react"            "https://esm.sh/react@18"
                      "react-dom/client" "https://esm.sh/react-dom@18/client"
                      "react-nil"        "https://esm.sh/react-nil"}}}))

(defn page-html
  "renders the js playground page"
  {:added "4.0"}
  [{:keys [title head body]
    :or {title "hara.runtime js playground"
         body [:div {:id "root"}]}}]
  (let [csp (clojure.string/join ";"
                                 ["default-src * 'unsafe-eval'"
                                  "connect-src * 'unsafe-eval' ws: wss:"
                                  "script-src  'self' 'unsafe-eval' 'unsafe-inline' https://esm.sh"
                                  "worker-src  blob: data: 'self' 'unsafe-eval' 'unsafe-inline'"])
        body (if (string? body)
               body
               (html/html body))
        head (cond (nil? head) ""
                   (string? head) head
                   :else (html/html head))]
    (str "<!doctype html>"
         (html/html
          [:html
           [:head
            [:meta {:http-equiv "Content-Security-Policy" :content csp}]
            [:meta {:http-equiv "Cache-Control" :content "no-store, no-cache, must-revalidate, max-age=0"}]
            [:meta {:http-equiv "Pragma" :content "no-cache"}]
            [:meta {:http-equiv "Expires" :content "0"}]
            [:title title]
            head]
           [:body
            body
            [:script {:type "module"} (playground-client-script)]]]))))

(defn- serve-file
  "serves a file from the playground root"
  [^String root ^String path]
  (let [file (java.io.File. root path)]
    (cond (.exists file)
          {:status 200
           :headers (merge +cache-control+
                           {"Content-Type" (content-type path)})
           :body file}

          :else
          {:status 404
           :headers +cache-control+
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
      (let [uri (:uri request)]
        (cond (= "/favicon.ico" uri)
              {:status 204
               :headers {"Content-Type" "image/x-icon"}
               :body ""}

              (= "/" uri)
              (serve-file root "index.html")

              :else
              (serve-file root (subs uri 1)))))))

(defn start-js-playground
  "starts the js playground server and returns the runtime with state attached"
  {:added "4.0"}
  [{:keys [id port host] :as rt}]
  (let [root (str (fs/create-tmpdir))
        _    (spit (str root "/index.html") (page-html {}))
        requested (let [p (or port (+pre-arranged+ id))]
                    (when (and p (not= 0 p)) p))
        port (or requested
                 (network/port:check-available 0))
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
                                                :layout (or layout :flat)
                                                :emit {:lang/jsx false}})]
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
