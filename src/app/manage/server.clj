(ns app.manage.server
  "Loopback management frontend over the real project graph and management operations."
  (:require [app.manage :as manage]
            [app.manage.verify :as verify]
            [app.manage.slices :as slices]
            [app.manage.make :as make]
            [app.manage.protocol :as protocol]
            [app.manage.http-api :as http-api]
            [app.manage.services :as services]
            [app.manage.watch :as watch]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [com.sun.net.httpserver HttpServer HttpHandler]
           [java.net InetSocketAddress URLDecoder]
           [java.nio.file Files]
           [java.util UUID]
           [java.util.concurrent Executors ExecutorService ThreadFactory]))

(defn- json-value [value]
  (cond
    (map? value) (into {} (map (fn [[key value]] [(if (keyword? key) (subs (str key) 1) (str key))
                                                (json-value value)]) value))
    (or (sequential? value) (set? value)) (mapv json-value value)
    (keyword? value) (subs (str value) 1)
    (symbol? value) (str value)
    (or (string? value) (number? value) (boolean? value) (nil? value)) value
    :else (str value)))

(defn- relative [root file]
  (str/replace (str (.relativize (.toPath root) (.toPath (.getAbsoluteFile file)))) "\\" "/"))

(defn- file-index [project roots]
  (let [root (manage/project-root project)]
    (vec (sort-by :path
                 (for [path roots
                       :let [file (manage/project-file project path)]
                       entry (if (.isDirectory file)
                               (with-open [stream (Files/walk (.toPath file)
                                                   (make-array java.nio.file.FileVisitOption 0))]
                                 (mapv #(.toFile %) (.toList stream)))
                               [file])
                       :when (.isFile entry)
                       :let [path (relative root entry)]
                       ;; Check every child too, so symlinks cannot escape the project.
                       :when (manage/project-file project path)]
                   {:path path :bytes (.length entry)})))))

(defn preview-index
  "Resolves declared static pages or loopback development URLs. Falls back to emitted HTML."
  [project targets]
  (let [declared (or (:previews project)
                     (for [target targets file (:output-files target)
                           :when (str/ends-with? (:path file) ".html")]
                       {:id (:path file) :label (:path file) :target (:id target) :path (:path file)}))]
    (mapv (fn [{:keys [id label target path url command server]}]
            (let [target (when target (name target))
                  owner (some #(when (= target (:id %)) %) targets)]
              (when-not (and id owner (not= (boolean path) (boolean url)))
                (throw (ex-info "Preview needs an id, a declared target and exactly one of :path or :url" {:id id})))
              (when path
                (manage/project-file project path)
                (when-not (some #(or (= path %) (str/starts-with? path (str % "/"))) (:outputs owner))
                  (throw (ex-info "Preview path must belong to its target outputs" {:id id :path path}))))
              (when url
                (let [uri (java.net.URI. url)]
                  (when-not (and (#{"http" "https"} (.getScheme uri))
                                 (#{"localhost" "127.0.0.1" "[::1]"} (.getHost uri))
                                 (nil? (.getUserInfo uri)))
                    (throw (ex-info "Preview URL must use a loopback HTTP server" {:id id})))))
              {:id (name id) :label (or label (name id)) :target target
               :url (or url (str "/preview/" path)) :path path :command command :server (when server (name server))
               :kind (if path :static :server)
               :ready (if path (.isFile (manage/project-file project path)) true)})) declared)))

(defn project-index
  "Serializable declared graph with actual source/output file inventories. Does not build."
  [project]
  (let [order (manage/plan project (sort (keys (:targets project))))
        levels (reduce (fn [out key]
                         (assoc out key (if-let [deps (seq (get-in project [:targets key :depends]))]
                                          (inc (apply max (map out deps))) 0))) {} order)
        targets (mapv (fn [key]
                        (let [target (get-in project [:targets key])]
                          {:id (name key) :label (str/capitalize (str/replace (name key) "-" " "))
                           :depth (get levels key) :depends (mapv name (:depends target))
                           :sources (:sources target) :outputs (:outputs target)
                           :source-files (file-index project (:sources target))
                           :output-files (file-index project (:outputs target))})) order)]
    {:id (str (:id project)) :root (str (manage/project-root project))
     :pages (when-let [provider (:pages project)] ((if (symbol? provider) (requiring-resolve provider) provider)))
     :unit-analysis (boolean (:unit-analysis project))
     :targets targets :plan (mapv name (manage/plan project))
     :previews (preview-index project targets)
     :dev-servers (mapv #(select-keys % [:id :label :url :command]) (services/definitions project))
     :checks (mapv (fn [[key check]] {:id (name key) :targets (mapv name (:targets check))
                                     :command (:command check)}) (sort-by key (:checks project)))}))

(defn- update-state! [server f & args]
  (apply swap! (:state server) (fn [state & args]
                               (update (apply f state args) :revision inc)) args))

(defn- begin-run! [server id operation target]
  (let [run {:id id :operation operation :target target :status :running
             :started (System/currentTimeMillis) :events []}]
    (update-state! server #(-> % (assoc :active id)
                               (assoc-in [:runs id] run)
                               (update :order (fn [order] (vec (take-last 20 (conj (vec (remove #{id} order)) id)))))))))

(defn- progress! [server event]
  (when-not (:active @(:state server))
    (begin-run! server (str (UUID/randomUUID)) "watch" nil))
  (let [id (:active @(:state server))
        key (:target event)
        status (if (#{:target/started :check/started} (:type event)) :running (:status event))]
    (update-state! server
                   (fn [state]
                     (cond-> (update-in state [:runs id :events]
                                        #(vec (take-last 250 (conj % event))))
                       key (assoc-in [:target-status (name key)]
                                     {:status status :run id :duration-ms (:duration-ms event)})
                       (:check event) (assoc-in [:check-status (name (:check event))]
                                               {:status status :duration-ms (:duration-ms event)}))))))

(defn- finish-run! [server id result stdout]
  (let [project ((:supplier server))]
    (update-state! server
                   #(cond-> (-> % (assoc :project (project-index project))
                                (assoc-in [:runs id :status] (or (:status result) :ok))
                                (assoc-in [:runs id :finished] (System/currentTimeMillis))
                                (assoc-in [:runs id :result] result)
                                (assoc-in [:runs id :stdout] stdout))
                      (= id (:active %)) (assoc :active nil)))))

(defn- watch-result! [server result]
  (let [id (or (:active @(:state server)) (str (UUID/randomUUID)))]
    (when-not (:active @(:state server)) (begin-run! server id "watch" nil))
    (finish-run! server id result "")))

(defn snapshot [server]
  (let [verification (verify/snapshot ((:supplier server)))
        current (slices/status ((:supplier server)))
        processes (services/snapshot (:services server) ((:supplier server)))
        state (swap! (:state server)
                     #(if (and (= current (:slices %)) (= verification (:verification %)) (= processes (:dev-servers %))) % (-> % (assoc :slices current :verification verification :dev-servers processes) (update :revision inc))))]
    (let [fingerprint (when (some #(get-in % [:result :fingerprint]) (vals (:runs state)))
                        (try ((requiring-resolve 'app.manage.units/fingerprint) ((:supplier server)))
                             (catch Exception _ ::unknown)))]
    (json-value (-> state
                    (assoc :watching (boolean @(:watcher server)))
                    (assoc :verification (verify/snapshot ((:supplier server))))
                    (assoc :runs (mapv (fn [id]
                                        (let [run (get-in state [:runs id])]
                                          (if-let [recorded (get-in run [:result :fingerprint])]
                                            (assoc-in run [:result :stale] (or (get-in run [:result :stale]) (not= recorded fingerprint)))
                                            run)))
                                      (reverse (:order state)))))))))

(defn- perform! [server {:keys [operation target tests slice stage request] :as action}]
  (let [project ((:supplier server))
        selected (if slice (slices/targets project slice stage) (when target [(keyword target)]))]
    (when (and target (not (get-in project [:targets (keyword target)])))
      (throw (ex-info "Unknown target" {:target target})))
    (case operation
      ("server-start" "server-stop" "server-restart") (services/execute! (:services server) project operation (:server request))
      "run-test-namespaces" ((requiring-resolve 'app.manage.testing/execute!) project request)
      "verify-definitions" (verify/definitions! project)
      "verify-page" (verify/execute! project request)
      "save-page-case" (verify/save! project request)
      "run-page-cases" (verify/run-cases! project)
      "build" (manage/build project {:targets selected :tests? (not= false tests)})
      "check" (if selected (manage/check project selected) (manage/check project))
      "analyse" (manage/analyse project)
      "generate" (manage/generate project {:targets selected :write true})
      "test" (manage/test-project project)
      "watch-start"
      (do (when-not @(:watcher server)
            (reset! (:watcher server)
                    (watch/start! (:supplier server)
                                  {:watch-only? true :tests? false :build-lock (:gate server)
                                   :on-result #(watch-result! server %)})))
          {:status :ok :watching true})
      "watch-stop" (do (when-let [handle @(:watcher server)] (watch/stop! handle))
                        (reset! (:watcher server) nil) {:status :ok :watching false}))))

(defn submit!
  "Queues one allowlisted management operation. Returns immediately with a real run ID."
  [server {:keys [operation] :as action}]
  (when-not (some #{operation} protocol/operations)
    (throw (ex-info "Unknown management operation" {:operation operation :http-status 400})))
  (when-not @(:running server)
    (throw (ex-info "Management server has stopped" {:http-status 503})))
  (let [id (str (UUID/randomUUID)) done (promise)]
    (swap! (:completions server) assoc id done)
    (update-state! server #(-> % (assoc-in [:runs id] {:id id :operation operation :status :queued :events []})
                               (update :order (fn [order] (vec (take-last 20 (conj (vec (remove #{id} order)) id)))))))
    (.submit ^ExecutorService (:jobs server)
             ^Runnable
             (bound-fn []
               (locking (:gate server)
                 (begin-run! server id operation (:target action))
                 (let [out (java.io.StringWriter.)
                       project ((:supplier server))
                       before (slices/fingerprints project)]
                   (try
                     (let [result (binding [manage/*on-event* #(progress! server %)]
                                    ;; Persistent watcher threads must not retain a run's log writer.
                                    (if (str/starts-with? operation "watch-")
                                      (perform! server action)
                                      (binding [*out* out *err* out] (perform! server action))))]
                       (slices/record! project (if (:slice action) (slices/targets project (:slice action) (:stage action))
                                                        (when (:target action) [(keyword (:target action))])) result before)
                       (finish-run! server id result (str out))
                       (deliver done result))
                     (catch Throwable error
                       (let [result {:status :error :message (.getMessage error)
                                     :data (ex-data error)}]
                         (try (finish-run! server id result (str out))
                              (catch Throwable _
                                (update-state! server #(-> % (assoc :active nil)
                                                           (assoc-in [:runs id :status] :error)
                                                           (assoc-in [:runs id :result] result)))))
                         (deliver done result))))))))
    {:id id :status :queued}))

(defn await-run [server id timeout-ms]
  (if-let [done (get @(:completions server) id)] (deref done timeout-ms ::timeout) ::unknown))

(defn- under? [path root]
  (or (= path root) (str/starts-with? path (str root "/"))))

(defn read-project-file
  "Reads only files owned by a declared target, after canonical-path validation."
  [server target path]
  (let [project (:project @(:state server))
        declaration (some #(when (= target (:id %)) %) (:targets project))
        file (manage/project-file project path)]
    (when-not (and declaration (some #(under? path %) (concat (:sources declaration) (:outputs declaration))))
      (throw (ex-info "File is not owned by this target" {:http-status 403})))
    (when-not (.isFile file) (throw (ex-info "File does not exist" {:http-status 404})))
    (when (> (.length file) 524288) (throw (ex-info "File is too large for the source viewer" {:http-status 413})))
    {:path path :content (slurp file)}))

(defn- ui-assets [project]
  (let [prefix ".app-manage-assets/"
        result (make/render-sections {:root (manage/project-root project)}
                 {:load ['app.manage.ui-model 'app.manage.ui]
                  :sections [{:type :module.graph :lang :js :main 'app.manage.ui-model
                              :target ".app-manage-assets"
                              :emit {:code {:label false :link {:path-suffix ".mjs"}}}}
                             {:type :module.single :lang :js :main 'app.manage.ui
                              :file ".app-manage-assets/app.mjs" :emit {:code {:label false}}}]})]
    (assoc (into {} (map (fn [[path content]] [(subs path (count prefix)) content]) result))
           "manager.css" (slurp (io/resource "assets/app.manage/manager.css")))))

(defn- params [query]
  (into {} (for [pair (str/split (or query "") #"&") :when (seq pair)
                 :let [[key value] (str/split pair #"=" 2)]]
             [(URLDecoder/decode key "UTF-8") (URLDecoder/decode (or value "") "UTF-8")])))

(defn- respond! [exchange status content-type body]
  (let [bytes (if (string? body) (.getBytes ^String body "UTF-8") body)]
    (.set (.getResponseHeaders exchange) "Content-Type" content-type)
    (.set (.getResponseHeaders exchange) "Cache-Control" "no-store")
    (.set (.getResponseHeaders exchange) "X-Content-Type-Options" "nosniff")
    (.sendResponseHeaders exchange status (count bytes))
    (with-open [out (.getResponseBody exchange)] (.write out ^bytes bytes))))

(defn- json-response! [exchange status value]
  (respond! exchange status "application/json; charset=utf-8" (json/write-str (json-value value))))

(defn- mime [path]
  (cond (str/ends-with? path ".html") "text/html; charset=utf-8"
        (str/ends-with? path ".css") "text/css; charset=utf-8"
        (or (str/ends-with? path ".mjs") (str/ends-with? path ".js")) "text/javascript; charset=utf-8"
        (str/ends-with? path ".json") "application/json; charset=utf-8"
        :else "application/octet-stream"))

(defn- next-policy []
  (let [html (slurp (io/resource "assets/app.manage/next/index.html"))
        hashes (for [[_ script] (re-seq #"(?s)<script[^>]*>(.*?)</script>" html)]
                 (str "'sha256-" (.encodeToString (java.util.Base64/getEncoder)
                                  (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes script "UTF-8"))) "'"))]
    (str "default-src 'self'; script-src 'self' " (str/join " " hashes)
         "; style-src 'self' 'unsafe-inline'; worker-src 'self' blob:; font-src 'self' data:; frame-src 'self' http://127.0.0.1:*; connect-src 'self'; object-src 'none'; frame-ancestors 'self'")))

(defn- handle! [server exchange]
  (try
    (let [uri (.getRequestURI exchange) path (.getPath uri) method (.getRequestMethod exchange)]
      (cond
        (str/starts-with? path "/api/v1")
        (http-api/handle! server exchange {:submit! submit! :project-index project-index})

        (and (= "GET" method) (or (and (= "/" path) (= :next (:workbench-ui ((:supplier server))))) (str/starts-with? path "/_next/") (str/starts-with? path "/monaco/")))
        (let [name (if (= "/" path) "index.html" (subs path 1))
              manifest (io/resource "assets/app.manage/next/manifest.edn")
              allowed (when manifest (set (edn/read-string (slurp manifest))))]
          (if (and allowed (allowed name))
            (do (.set (.getResponseHeaders exchange) "Content-Security-Policy" (next-policy))
                (respond! exchange 200 (mime name) (with-open [in (io/input-stream (io/resource (str "assets/app.manage/next/" name)))] (.readAllBytes in))))
            (json-response! exchange 404 {:error "Workbench assets have not been built"})))

        (and (= "GET" method) (#{"/" "/legacy"} path))
        (do (.set (.getResponseHeaders exchange) "Content-Security-Policy"
                  "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; worker-src 'self' blob:; font-src 'self' data:; connect-src 'self'; frame-src 'self' http://127.0.0.1:* http://localhost:* http://[::1]:* https://127.0.0.1:* https://localhost:* https://[::1]:*; object-src 'none'; base-uri 'none'; frame-ancestors 'self'")
            (respond! exchange 200 "text/html; charset=utf-8"
                      (str "<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                           "<meta name=\"app-manage-token\" content=\"" (:token server) "\"><title>app.manage</title>"
                           "<link rel=\"stylesheet\" href=\"/assets/manager.css\"><body><div id=\"app-manage-root\">Loading project…</div>"
                           "<script type=\"module\" src=\"/assets/app.mjs\"></script></body></html>")))

        (and (= "GET" method) (str/starts-with? path "/assets/"))
        (let [key (subs path 8)]
          (if-let [content (get (:assets server) key)] (respond! exchange 200 (mime key) content)
              (json-response! exchange 404 {:error "Unknown asset"})))

        (and (= "GET" method) (= "/api/session" path))
        (do (.set (.getResponseHeaders exchange) "Cache-Control" "no-store")
            (json-response! exchange 200 {:token (:token server) :project (str (manage/project-root ((:supplier server))))}))

        (and (= "GET" method) (= "/api/test-profiles" path))
        (json-response! exchange 200 ((requiring-resolve 'app.manage.testing/profiles) ((:supplier server))))

        (and (= "GET" method) (= "/api/source" path))
        (let [project ((:supplier server))
              path (get (params (.getRawQuery uri)) "path")
              allowed (set (concat ((requiring-resolve 'app.manage.units/files) project (get-in project [:unit-analysis :sources]))
                                   ((requiring-resolve 'app.manage.units/files) project (get-in project [:unit-analysis :tests]))))]
          (when-not (allowed path) (throw (ex-info "File is outside the indexed source/test inventory" {:http-status 403})))
          (let [file (manage/project-file project path)]
            (when (> (.length file) 524288) (throw (ex-info "Source file is too large" {:http-status 413})))
            (json-response! exchange 200 {:path path :language "clojure" :content (slurp file) :fingerprint (str (.lastModified file) ":" (.length file))})))

        (and (= "GET" method) (= "/api/units" path))
        (let [project ((:supplier server))]
          (if (:unit-analysis project)
            (json-response! exchange 200 ((requiring-resolve 'app.manage.units/report)
                                         project (into {} (map (fn [[k v]] [(keyword k) v]) (params (.getRawQuery uri))))))
            (json-response! exchange 200 {:enabled false :units []})))

        (and (= "GET" method) (= "/api/pages" path))
        (json-response! exchange 200 (get-in @(:state server) [:project :pages]))

        (and (= "GET" method) (= "/api/slices" path))
        (json-response! exchange 200 (slices/status ((:supplier server))))

        (and (= "POST" method) (= "/api/slice-work" path))
        (do
          (when-not (= (:token server) (.getFirst (.getRequestHeaders exchange) "X-App-Manage-Token"))
            (throw (ex-info "Missing local management token" {:http-status 403})))
          (with-open [body (.getRequestBody exchange)]
            (let [bytes (.readNBytes body 20001)]
              (when (> (count bytes) 20000) (throw (ex-info "Work update too large" {:http-status 413})))
              (locking (:gate server)
                (json-response! exchange 200 (slices/update-work! ((:supplier server))
                  (json/read-str (String. bytes "UTF-8") :key-fn keyword)))))))

        (and (= "GET" method) (= "/api/state" path)) (json-response! exchange 200 (snapshot server))

        (and (= "GET" method) (= "/api/file" path))
        (let [query (params (.getRawQuery uri))]
          (json-response! exchange 200 (read-project-file server (get query "target") (get query "path"))))

        (and (= "POST" method) (= "/api/actions" path))
        (do
          (when-not (= (:token server) (.getFirst (.getRequestHeaders exchange) "X-App-Manage-Token"))
            (throw (ex-info "Missing local management token" {:http-status 403})))
          (with-open [body (.getRequestBody exchange)]
            (let [bytes (.readNBytes body 65537)]
              (when (> (count bytes) 65536) (throw (ex-info "Request too large" {:http-status 413})))
              (json-response! exchange 202
                              (submit! server (json/read-str (String. bytes "UTF-8") :key-fn keyword))))))

        (and (= "GET" method) (str/starts-with? path "/preview/"))
        (let [relative (subs path 9)
              relative (if (str/ends-with? relative "/") (str relative "index.html") relative)
              project (:project @(:state server))
              allowed (mapcat :outputs (:targets project))
              file (manage/project-file project relative)]
          (when-not (some #(under? relative %) allowed)
            (throw (ex-info "Preview is limited to declared outputs" {:http-status 403})))
          (if (.isFile file) (respond! exchange 200 (mime relative) (Files/readAllBytes (.toPath file)))
              (json-response! exchange 404 {:error "Build this output first"})))

        :else (json-response! exchange 404 {:error "Unknown route"})))
    (catch Throwable error
      (json-response! exchange (or (:http-status (ex-data error)) 400)
                      {:error (.getMessage error)}))))

(defn stop! [server]
  (when (compare-and-set! (:running server) true false)
    (when-let [handle @(:watcher server)] (watch/stop! handle))
    (services/close! (:services server) ((:supplier server)))
    (when-let [http (:http server)] (.stop ^HttpServer http 0))
    (.shutdownNow ^ExecutorService (:jobs server))
    (.shutdownNow ^ExecutorService (:requests server)))
  true)

(defn- executor [threads]
  (Executors/newFixedThreadPool threads
    (reify ThreadFactory
      (newThread [_ runnable] (doto (Thread. runnable "app.manage-server") (.setDaemon true))))))

(defn session!
  "Creates the management controller without opening a socket; also usable headlessly."
  [project-var]
  (let [supplier (if (symbol? project-var) #(var-get (requiring-resolve project-var)) project-var)]
    {:supplier supplier :gate (Object.)
     :token (str (UUID/randomUUID)) :running (atom true)
     :jobs (executor 1) :requests (executor 4)
     :watcher (atom nil) :completions (atom {}) :services (services/controller)
     :state (atom {:revision 0 :project (project-index (supplier)) :active nil
                   :runs {} :order [] :target-status {} :check-status {}})}))

(defn start!
  "Starts a reusable management UI on loopback. A symbol is re-resolved after source reloads."
  ([project-var] (start! project-var {}))
  ([project-var {:keys [port assets] :or {port 8130}}]
   (let [session (session! project-var)]
     (try
       (let [assets (or assets (ui-assets ((:supplier session))))
             http (HttpServer/create (InetSocketAddress. "127.0.0.1" (int port)) 0)
             server (assoc session :assets assets :http http)]
         (try
           (.createContext http "/" (reify HttpHandler (handle [_ exchange] (handle! server exchange))))
           (.setExecutor http (:requests server))
           (.start http)
           (assoc server :port (.getPort (.getAddress http)))
           (catch Throwable error (stop! server) (throw error))))
       (catch Throwable error (stop! session) (throw error))))))

(defn -main [project-var & args]
  (let [options (apply hash-map args)
        unknown (remove #{"--port"} (keys options))]
    (when (seq unknown) (throw (ex-info "Unknown management server option" {:options unknown})))
    (let [server (start! (symbol project-var) {:port (Integer/parseInt (get options "--port" "8130"))})
          hook (Thread. ^Runnable #(stop! server))]
      (.addShutdownHook (Runtime/getRuntime) hook)
      (println (str "app.manage: http://127.0.0.1:" (:port server)))
      (flush)
      (try @(promise)
           (finally (stop! server)
                    (try (.removeShutdownHook (Runtime/getRuntime) hook)
                         (catch IllegalStateException _)))))))
