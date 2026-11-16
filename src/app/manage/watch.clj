(ns app.manage.watch
  "Shared source watcher with dependency-aware Clojure reload and recoverable builds."
  (:require [app.manage :as manage]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [std.fs.watch :as fs-watch])
  (:import [java.util.concurrent Executors ScheduledExecutorService TimeUnit ThreadFactory]))

(defn sources
  "Indexes project-local Clojure namespace dependencies without evaluating sources."
  [project]
  (into (sorted-map)
        (for [path (:source-paths project)
              file (file-seq (manage/project-file project path))
              :when (and (.isFile file) (str/ends-with? (.getName file) ".clj"))]
          (binding [*read-eval* false]
            (with-open [reader (java.io.PushbackReader. (io/reader file))]
              (let [forms (loop [out []]
                            (let [form (read {:eof ::eof} reader)]
                              (if (= ::eof form) out (recur (conj out form)))))
                    form (first forms)
                    relative (str (.relativize (.toPath (manage/project-root project))
                                               (.toPath (.getAbsoluteFile file))))]
                (when-not (and (seq? form) (= 'ns (first form)))
                  (throw (ex-info "Expected a namespace declaration" {:path relative})))
                [relative {:namespace (second form)
                           :depends
                           (vec (distinct
                                 (concat
                                  (for [clause (drop 2 form)
                                        :when (and (seq? clause) (= :require (first clause)))
                                        spec (rest clause) :when (vector? spec)] (first spec))
                                  (for [script forms
                                        :when (and (seq? script) (symbol? (first script))
                                                   (#{"script" "script-"} (name (first script))))
                                        :let [config (nth script 2 nil)] :when (map? config)
                                        spec (:require config) :when (vector? spec)] (first spec)))))}]))))))

(defn reload-plan
  "Finds changed namespaces and their project-local require dependants."
  [before after paths]
  (let [entries (merge before after)
        by-ns (into {} (map (fn [[_ value]] [(:namespace value) value]) entries))
        changed (set (keep #(get-in entries [% :namespace]) paths))
        impacted (loop [out changed]
                   (let [next (into out (for [[ns {:keys [depends]}] by-ns
                                              :when (some out depends)] ns))]
                     (if (= next out) out (recur next))))]
    (if (empty? impacted) []
      (manage/plan {:targets (into {} (for [ns impacted]
                                       [ns {:depends (vec (filter impacted (:depends (get by-ns ns))))}]))}
                   (sort impacted)))))

(defn reload!
  [before after paths]
  (let [order (reload-plan before after paths)
        existing (set (map (comp :namespace val) after))]
    (doseq [ns order]
      (when-not (existing ns)
        (throw (ex-info "A required project namespace was deleted" {:namespace ns})))
      ((requiring-resolve 'app.manage.make/purge!) ns)
      (require ns :reload))
    order))

(defn rebuild!
  "Processes one change batch. Failures are reported and leave the watcher available."
  [{:keys [state supplier lock options]} paths]
  (locking lock
    (when (:active @state)
      (try
        (let [project (:project @state)
              after (sources project)
              _ (when (not= false (:reload? options)) (reload! (:sources @state) after paths))
              project (supplier)
              all? (some (set (:project-sources project)) paths)
              targets (if all? (manage/plan project) (manage/affected project paths))
              result (if (seq targets)
                       (manage/build project {:targets targets :tests? (:tests? options true)})
                       {:status :ok :skipped true})]
          (swap! state assoc :project project :sources after :status (:status result) :last-result result)
          (when-let [notify (:on-result options)] (notify result))
          result)
        (catch Exception error
          (let [result {:status :error :message (.getMessage error) :data (ex-data error)}]
            (swap! state assoc :status :error :last-result result)
            (when-let [notify (:on-result options)] (notify result))
            result))))))

(defn stop!
  "Closes the OS watcher and executor once; waits for an active build to leave its lock."
  [{:keys [state lock executor watcher]}]
  (locking lock
    (when (:active @state)
      (swap! state assoc :active false :status :stopped)
      (when watcher (fs-watch/stop-watcher watcher))
      (when executor (.shutdownNow ^ScheduledExecutorService executor))))
  true)

(defn start!
  "Watches project :source-paths. Accepts a qualified project-var symbol or a current-project supplier."
  ([supplier] (start! supplier {}))
  ([supplier options]
   (let [supplier (if (symbol? supplier)
                    (let [project-var supplier] #(var-get (requiring-resolve project-var)))
                    supplier)
         project (supplier)
         roots (mapv #(manage/project-file project %) (:source-paths project))
         _ (when-not (and (seq roots) (every? #(.isDirectory %) roots))
             (throw (ex-info "Watch needs existing project :source-paths" {})))
         initial (when-not (:watch-only? options)
                   (manage/build project {:tests? (:tests? options true)}))
         _ (when (= :error (:status initial))
             (throw (ex-info "Initial project build failed" {:result initial})))
         state (atom {:active true :project project :sources (sources project)
                      :status :ready :last-result initial})
         executor (Executors/newSingleThreadScheduledExecutor
                   (reify ThreadFactory
                     (newThread [_ runnable] (doto (Thread. runnable "app.manage-watch") (.setDaemon true)))))
         pending (atom #{}) lock (or (:build-lock options) (Object.))
         handle {:state state :supplier supplier :lock lock :executor executor :options options}
         enqueue (fn [_ file]
                   (when (:active @state)
                     (let [path (str (.relativize (.toPath (manage/project-root project))
                                                 (.toPath (.getAbsoluteFile file))))]
                       (swap! pending conj path)
                       (.schedule executor
                                  ^Runnable (bound-fn []
                                              (let [[paths _] (swap-vals! pending (constantly #{}))]
                                                (when (seq paths) (rebuild! handle (sort paths)))))
                                  (long (:debounce-ms options 100)) TimeUnit/MILLISECONDS))))]
     (try
       (assoc handle :watcher (fs-watch/start-watcher
                              (fs-watch/watcher (mapv str roots) enqueue
                                                {:recursive true :types :all :mode :sync})))
       (catch Exception error (stop! handle) (throw error))))))

(defn watch-cli [project-var options]
  (let [handle (start! #(var-get (requiring-resolve project-var))
                       (assoc options :on-result prn))
        hook (Thread. ^Runnable #(stop! handle))]
    (.addShutdownHook (Runtime/getRuntime) hook)
    (println "Watching" (mapv str (:source-paths (:project @(:state handle)))) "— Ctrl-C to stop")
    (try @(promise)
         (finally
           (stop! handle)
           (try (.removeShutdownHook (Runtime/getRuntime) hook)
                (catch IllegalStateException _))))))
