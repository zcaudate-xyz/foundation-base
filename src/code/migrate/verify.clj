(ns code.migrate.verify
  (:require [clojure.java.io :as io]))

(defn run-process
  "runs one isolated verification process and captures its complete result"
  {:added "4.1"}
  [command {:keys [dir stdin]}]
  (let [builder (ProcessBuilder. ^java.util.List (mapv str command))
        _       (when dir
                  (.directory builder (io/file dir)))
        process (.start builder)
        stdout  (future (slurp (.getInputStream process)))
        stderr  (future (slurp (.getErrorStream process)))]
    (with-open [writer (io/writer (.getOutputStream process))]
      (when stdin
        (.write writer (str stdin))))
    (let [exit (.waitFor process)]
      {:command (mapv str command)
       :dir (some-> dir str)
       :exit exit
       :stdout @stdout
       :stderr @stderr
       :passed (zero? exit)})))

(defn verify-source
  "evaluates generated source in a fresh native Hara process"
  {:added "4.1"}
  [source {:keys [hara project-root]}]
  (when-not (and hara project-root)
    (throw (ex-info "verify-source requires :hara and :project-root"
                    {:hara hara
                     :project-root project-root})))
  (let [hara         (.getCanonicalPath (io/file hara))
        project-root (.getCanonicalPath (io/file project-root))]
    (assoc
     (run-process [hara "--project" project-root "--offline" "stdin"]
                  {:dir project-root
                   :stdin source})
     :verification/type :source)))
