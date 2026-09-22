(ns foundation-deploy.dev.web-index
  (:require [std.lib.os :as os]
            [std.make :as make]))

(def ^:dynamic *session* "DEV")

(def ^:dynamic *default-trigger* "component.web-index")

(defn project-config
  "Resolves the generated Foundation Web project definition on demand."
  []
  (require 'component.build-web-index)
  (deref (resolve 'component.build-web-index/WEB-INDEX)))

(defn dev-window
  "Returns the tmux window used by the project's development server."
  [config]
  (str "DEV-" (:tag @(:instance config))))

(defn task-build
  "Regenerates the generated Foundation Web project."
  []
  (make/build-all (project-config)))

(defn task-trigger
  "Rebuilds generated targets triggered by a source namespace."
  ([]
   (task-trigger *default-trigger*))
  ([ns]
   (project-config)
   (make/build-triggered (symbol (str ns)))))

(defn task-start
  "Starts Expo Web in the project's tmux development window."
  []
  (make/run:dev (project-config)))

(defn task-stop
  "Stops the project's development window, if it is running."
  []
  (let [config (project-config)
        window (dev-window config)]
    (if (and (os/tmux:has-session? *session*)
             (os/tmux:has-window? *session* window))
      (make/run-close config :dev)
      :not-running)))

(defn task-status
  "Returns the tmux and generated-project state for the dev workflow."
  []
  (let [config (project-config)
        active? (os/tmux:has-session? *session*)
        windows (if active? (os/tmux:list-windows *session*) [])
        window (dev-window config)]
    {:session *session*
     :project (make/dir config)
     :window window
     :active? active?
     :running? (boolean (some #{window} windows))
     :windows windows}))

(defn task-attach
  "Opens a terminal attached to the development tmux session."
  []
  (if (os/tmux:has-session? *session*)
    (os/os-run "tmux" "attach-session" "-t" *session*)
    :not-running))

(defn task-setup
  "Builds the generated project and starts its development server."
  []
  (task-build)
  (task-start))

(defn task-run
  "Runs a named development task.

  Tasks:
  - `setup` builds the generated project and starts Expo Web
  - `build` regenerates the generated project
  - `start` starts Expo Web in tmux
  - `trigger` rebuilds targets for a source namespace
  - `stop` stops the Expo Web tmux window
  - `status` reports the tmux and project state
  - `attach` opens a terminal attached to tmux"
  [task & args]
  (case (or task "status")
    "setup" (task-setup)
    "build" (task-build)
    "start" (task-start)
    "trigger" (apply task-trigger args)
    "stop" (task-stop)
    "status" (task-status)
    "attach" (task-attach)
    (throw (ex-info "Unknown development task"
                    {:task task
                     :tasks ["setup" "build" "start" "trigger" "stop" "status" "attach"]}))))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

(defn -main
  "Runs a development task from the command line."
  [& [task & args]]
  (try
    (let [result (apply task-run task args)]
      (when (= "status" (or task "status"))
        (prn result))
      (*exit* 0))
    (catch Throwable t
      (binding [*out* *err*]
        (println (.getMessage t)))
      (*exit* 1))))
