(ns rt.basic.type-bench
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.os]
            [std.lib.walk]
            [std.make :as make]))

(defonce ^:dynamic *active*
  (atom {}))

(defn- rt-bench-string [{:keys [type lang program port]}]
  (str "#rt.bench" [type lang port]))

(defimpl RuntimeBench [type lang program port]
  :string rt-bench-string)

(defn bench?
  "checks if object is a bench"
  {:added "4.0"}
  [obj]
  (instance? RuntimeBench obj))

;;
;; WS
;;

(defn get-bench
  "gets an active bench given port"
  {:added "4.0"}
  [port]
  (get @*active* port))

(defn create-bench-process
  "creates the bench process"
  {:added "4.0"}
  [lang port {:keys [root-dir
                     host]
              :as opts}
   input-args input-body]
  (let [cmd  (concat input-args (std.lib.collection/seqify input-body))
        cmd  (std.lib.walk/postwalk (fn [x]
                           (if (keyword? x)
                             (or (get (:params opts) x)
                                 (throw (ex-info (str "Need to supply in [:config :params " x "]")
                                                 {:cmd cmd
                                                  :params (:params opts)})))
                             x))
                         cmd)  
        process (std.lib.os/sh (merge (:shell opts)
                             {:args cmd
                              :wait false
                              :root root-dir}))
        thread  (-> (std.lib.future/future (std.lib.os/sh-wait process))
                    (std.lib.future/on:complete (fn [ret err]
                                     (try (let [out (std.lib.os/sh-output process)]
                                            (when (not= 0 (:exit out))
                                              (std.lib.env/prn out)))
                                          (catch Throwable t))
                                     (swap! *active* dissoc port))))]
    (map->RuntimeBench
     {:type :bench/basic
      :host (or host "127.0.0.1")
      :lang lang
      :port port
      :process process
      :thread thread
      :root-dir root-dir})))

(defn start-bench-process
  "starts a bench process"
  {:added "4.0"}
  [lang {:keys [exec bootstrap] :as bench} port {:keys [host root-dir] :as opts}]
  (assert (identity bootstrap) "The bootstrap function is needed.")
  (-> (if (not (get @*active* port))
        (swap! *active*
               (fn [m]
                 (assoc m port (create-bench-process
                                lang port opts
                                exec
                                (bootstrap port opts)))))
        @*active*)
      (get port)))

(defn stop-bench-process
  "stops the bench process"
  {:added "4.0"}
  [port]
  (let [{:keys [process] :as entry} (get @*active* port)]
    (when process
      (doto process
        (std.lib.os/sh-kill)
        (std.lib.os/sh-exit)
        (std.lib.os/sh-wait)))
    entry))

(defn start-bench
  "starts a test bench process"
  {:added "4.0"}
  [lang bench port opts]
  (let [[program process exec] (oneshot/rt-oneshot-setup
                                lang
                                (:program bench)
                                (:process bench)
                                (:exec bench)
                                (:runtime opts))
        bench    (merge bench
                        process
                        {:lang lang
                         :program program
                         :exec exec})
        root-dir (if (:save bench)
                   (str "test-bench/"
                        lang "/"
                        (name program)
                        "/" port)
                   (str (fs/create-tmpdir)))
        _  (if (:make bench)
             (make/build-at root-dir (:make bench)))]
    (start-bench-process lang bench port (assoc opts :root-dir root-dir))))

(defn stop-bench
  "stops a test bench process"
  {:added "4.0"}
  [{:keys [port save root-dir] :as entry}]
  (let [stopped  (stop-bench-process port)
        _ (when (and root-dir (not save))
            (fs/delete root-dir))]
    stopped))

