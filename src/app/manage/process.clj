(ns app.manage.process
  "Bounded command execution for project-declared verification steps."
  (:require [clojure.java.io :as io])
  (:import [java.util.concurrent TimeUnit]))

(defn run
  [{:keys [root command timeout-ms] :or {timeout-ms 60000}}]
  (when-not (and (vector? command) (seq command) (every? string? command))
    (throw (ex-info "Verification command must be an argument vector" {:command command})))
  (let [out (java.io.File/createTempFile "app-manage-out-" ".log")
        err (java.io.File/createTempFile "app-manage-err-" ".log")]
    (try
      (let [process (.start (doto (ProcessBuilder. ^java.util.List command)
                             (.directory (io/file root))
                             (.redirectOutput out) (.redirectError err)))
            finished? (try (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS)
                           (catch InterruptedException error
                             (.destroyForcibly process)
                             (throw error)))
            _ (when-not finished? (.destroyForcibly process) (.waitFor process))
            exit (.exitValue process)]
        {:status (if (and finished? (zero? exit)) :ok :error)
         :command command :exit exit :timed-out (not finished?)
         :stdout (slurp out) :stderr (slurp err)})
      (finally (.delete out) (.delete err)))))
