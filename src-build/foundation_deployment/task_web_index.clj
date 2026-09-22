(ns foundation-deployment.task-web-index
  (:require [std.make :as make]
            [std.make.github :as github]))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

^{:public true}
(defn project-config
  "Resolves the generated Foundation Web project definition on demand."
  []
  (require 'component.build-web-index)
  (deref (resolve 'component.build-web-index/WEB-INDEX)))

^{:public true}
(defn task-build
  "Regenerates the generated Foundation Web project."
  []
  (make/build-all (project-config)))

^{:public true}
(defn task-gh-init
  "Initialises the generated project repository and publishes its first revision."
  [& [message]]
  (github/gh-dwim-init (project-config) message))

^{:public true}
(defn task-gh-push
  "Regenerates and pushes the generated project so GitHub Actions can publish gh-pages."
  [& [message]]
  (github/gh-dwim-push (project-config) message))

^{:public true}
(defn task-run
  "Runs a Foundation Web deployment task without terminating the process.

  Tasks:
  - `build` regenerates the local project
  - `init` creates the remote project repository and publishes its first revision
  - `publish` (or `push`) regenerates and pushes the project repository"
  [task & args]
  (case task
    "build" (task-build)
    "init" (apply task-gh-init args)
    "publish" (apply task-gh-push args)
    "push" (apply task-gh-push args)
    (throw (ex-info "Unknown deployment task"
                    {:task task
                     :tasks ["build" "init" "publish" "push"]}))))

^{:public true}
(defn -main
  "Runs a deployment task and terminates the command-line process."
  [& [task & args]]
  (try
    (apply task-run task args)
    (*exit* 0)
    (catch Throwable _
      (*exit* 1))))
