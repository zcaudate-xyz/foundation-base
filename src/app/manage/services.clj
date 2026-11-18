(ns app.manage.services
  "Session-owned development processes. Commands come exclusively from project declarations."
  (:require [app.manage :as manage]
            [clojure.java.io :as io])
  (:import [java.net URI Socket InetSocketAddress]
           [java.lang ProcessHandle]
           [java.util.concurrent TimeUnit]))

(defn definitions [project]
  (let [items (mapv (fn [{:keys [id command url env stop-timeout-ms] :as item}]
                      (let [uri (when url (URI. url))]
                        (when-not (and id (vector? command) (seq command) (every? string? command)
                                       uri (= "http" (.getScheme uri))
                                       (#{"127.0.0.1" "localhost" "[::1]"} (.getHost uri))
                                       (nil? (.getUserInfo uri)) (pos? (.getPort uri))
                                       (or (nil? env) (and (map? env) (every? string? (concat (keys env) (vals env)))))
                                       (<= 100 (or stop-timeout-ms 30000) 120000))
                          (throw (ex-info "Development server needs an id, command vector and loopback URL with a port" {:id id})))
                        (assoc item :id (name id)))) (:dev-servers project))]
    (when-not (= (count items) (count (set (map :id items))))
      (throw (ex-info "Development server ids must be unique" {})))
    items))

(defn controller [] {:entries (atom {}) :lock (Object.) :closed (atom false)})

(defn- listening? [url]
  (try
    (let [uri (URI. url)]
      (with-open [socket (Socket.)]
        (.connect socket (InetSocketAddress. (.getHost uri) (.getPort uri)) 150)
        true))
    (catch Exception _ false)))

(defn- append-log! [controller id line]
  (swap! (:entries controller) update-in [id :logs]
         #(vec (take-last 250 (conj (or % []) (subs line 0 (min 4000 (count line))))))))

(defn snapshot [controller project]
  (mapv (fn [{:keys [id url label command]}]
          (let [entry (get @(:entries controller) id)
                process (:process entry)
                alive? (boolean (or (and process (.isAlive ^Process process))
                                    (some #(.isAlive ^ProcessHandle %) (:children entry))))
                listening (listening? url)
                status (cond
                         (= :stopping (:status entry)) :stopping
                         alive? (if listening :running :starting)
                         listening :external
                         (= :stopped (:status entry)) :stopped
                         (= :error (:status entry)) :error
                         (and process (not (zero? (.exitValue ^Process process)))) :error
                         :else :stopped)]
            (merge (select-keys entry [:pid :started :logs :message])
                   {:id id :label (or label id) :url url :command command
                    :status status :managed (boolean alive?) :ready (boolean listening)
                    :exit (when (and process (not alive?)) (.exitValue ^Process process))})))
        (definitions project)))

(defn- declaration [project id]
  (or (some #(when (= (name id) (:id %)) %) (definitions project))
      (throw (ex-info "Unknown development server" {:server id}))))

(defn start! [controller project id]
  (locking (:lock controller)
    (when @(:closed controller) (throw (ex-info "Development controller is closed" {})))
    (let [{:keys [command env url] :as item} (declaration project id)
          id (:id item) existing (get-in @(:entries controller) [id :process])]
      (cond
        (and existing (.isAlive ^Process existing)) {:status :ok :server id :already-running true}
        (listening? url) (throw (ex-info "This port is used by a server started outside this workbench. Stop it in its original terminal first."
                                      {:server id :url url}))
        :else
        (let [directory (if-let [path (:directory item)] (manage/project-file project path) (manage/project-root project))
              builder (doto (ProcessBuilder. ^java.util.List command)
                        (.directory directory)
                        (.redirectErrorStream true))
              _ (.putAll (.environment builder) (or env {}))
              process (try (.start builder)
                           (catch Exception error
                             (swap! (:entries controller) assoc id {:status :error :message (.getMessage error) :logs []})
                             (throw error)))]
          (swap! (:entries controller) assoc id {:process process :pid (.pid process) :children []
                                                 :status :starting :started (System/currentTimeMillis) :logs []})
          (future
            (loop []
              (when (.isAlive process)
                (with-open [stream (.descendants process)]
                  (swap! (:entries controller) update-in [id :children]
                         #(vec (distinct (concat % (.toList stream))))))
                (when-not (.waitFor process 200 TimeUnit/MILLISECONDS) (recur)))))
          (future
            (try
              (with-open [reader (io/reader (.getInputStream process))]
                (doseq [line (line-seq reader)] (append-log! controller id line)))
              (catch Exception _)))
          {:status :ok :server id :pid (.pid process)})))))

(defn stop! [controller project id]
  (locking (:lock controller)
    (let [{:keys [id stop-timeout-ms stop-argument]} (declaration project id)
          process (get-in @(:entries controller) [id :process])]
      (when process
        (swap! (:entries controller) assoc-in [id :status] :stopping)
        (let [children (distinct (concat (get-in @(:entries controller) [id :children])
                                        (with-open [stream (.descendants ^Process process)] (vec (.toArray stream)))))
              handles (conj (vec (reverse children)) (.toHandle ^Process process))
              entrypoints (when stop-argument
                            (filter (fn [^ProcessHandle handle]
                                      (some #{stop-argument}
                                            (seq (.orElse (.arguments (.info handle)) (make-array String 0))))) handles))
              deadline (+ (System/currentTimeMillis) (or stop-timeout-ms 30000))]
          ;; Signal the owned tree gracefully. In particular, fixture wrappers and
          ;; their Node children must both have time to release Auth/DB resources.
          ;; A declared entrypoint orchestrates its own children and cleanup.
          ;; Signalling its parents too can deliver SIGTERM twice to the same
          ;; child, interrupting that cleanup after a once-only signal handler.
          (doseq [^ProcessHandle handle (if (seq entrypoints) entrypoints handles) :when (.isAlive handle)] (.destroy handle))
          (while (and (some #(.isAlive ^ProcessHandle %) handles) (< (System/currentTimeMillis) deadline))
            (Thread/sleep 50))
          (doseq [^ProcessHandle handle handles :when (.isAlive handle)] (.destroyForcibly handle))
          (.waitFor ^Process process 2000 TimeUnit/MILLISECONDS)
          (swap! (:entries controller) assoc-in [id :status] :stopped)))
      {:status :ok :server id})))

(defn execute! [controller project operation id]
  (when-not (string? id) (throw (ex-info "Select a declared development server" {})))
  (when (and (not= operation "server-start")
             (= :external (:status (some #(when (= id (:id %)) %) (snapshot controller project)))))
    (throw (ex-info "This server was started outside this workbench; stop it in its original terminal first." {:server id})))
  (case operation
    "server-start" (start! controller project id)
    "server-stop" (stop! controller project id)
    "server-restart" (do (stop! controller project id) (start! controller project id))))

(defn close! [controller project]
  (locking (:lock controller)
    (reset! (:closed controller) true)
    (doseq [id (keys @(:entries controller))] (stop! controller project id))))
