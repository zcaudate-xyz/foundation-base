(ns app.manage.process
  "Bounded command execution for project-declared verification steps."
  (:require [clojure.java.io :as io])
  (:import [java.util.concurrent TimeUnit]))

(defn stop-process! [process]
  (let [children (with-open [stream (.descendants process)] (vec (.toArray stream)))]
    (doseq [child (reverse children)] (.destroyForcibly ^java.lang.ProcessHandle child))
    (.destroyForcibly process)
    (.waitFor process)))

(defn run
  [{:keys [root command timeout-ms on-output] :or {timeout-ms 60000}}]
  (when-not (and (vector? command) (seq command) (every? string? command))
    (throw (ex-info "Verification command must be an argument vector" {:command command})))
  (let [out (java.io.File/createTempFile "app-manage-out-" ".log")
        err (java.io.File/createTempFile "app-manage-err-" ".log")]
    (try
      (let [process (.start (doto (ProcessBuilder. ^java.util.List command)
                             (.directory (io/file root))
                             (.redirectOutput out) (.redirectError err)))
            finished? (try (if on-output
                             (let [deadline (+ (System/nanoTime) (* timeout-ms 1000000))]
                               (loop [sent-out 0 sent-err 0]
                                 (let [finished (.waitFor process 100 TimeUnit/MILLISECONDS)
                                       stdout (slurp out) stderr (slurp err)]
                                   (when (> (count stdout) sent-out) (on-output :stdout (subs stdout sent-out)))
                                   (when (> (count stderr) sent-err) (on-output :stderr (subs stderr sent-err)))
                                   (cond finished true
                                         (>= (System/nanoTime) deadline) false
                                         :else (recur (count stdout) (count stderr))))))
                             (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS))
                           (catch InterruptedException error
                             (stop-process! process)
                             (throw error)))
            _ (when-not finished? (stop-process! process))
            exit (.exitValue process)]
        {:status (if (and finished? (zero? exit)) :ok :error)
         :command command :exit exit :timed-out (not finished?)
         :stdout (slurp out) :stderr (slurp err)})
      (finally (.delete out) (.delete err)))))
