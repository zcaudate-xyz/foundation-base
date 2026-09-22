(ns std.make.foundation-deploy-dev-test
  (:require [foundation-deploy.dev.web-index :as dev]
            [std.lib.os :as os]
            [std.make :as make])
  (:use code.test))

^{:refer foundation-deploy.dev.web-index/project-config :added "4.1.9"}
(fact "resolves the generated project definition on demand"
  (let [config (Object.)]
    (with-redefs [clojure.core/require (fn [& _] nil)
                  clojure.core/resolve (fn [_] (atom config))]
      (dev/project-config) => config)))

^{:refer foundation-deploy.dev.web-index/dev-window :added "4.1.9"}
(fact "names the project development window from its tag"
  (dev/dev-window {:instance (atom {:tag "web-index"})})
  => "DEV-web-index")

^{:refer foundation-deploy.dev.web-index/task-build :added "4.1.9"}
(fact "builds the generated web project"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [dev/project-config (fn [] config)
                  make/build-all (fn [& args]
                                   (swap! calls conj args)
                                   :built)]
      (dev/task-build) => :built
      @calls => [[config]])))

^{:refer foundation-deploy.dev.web-index/task-trigger :added "4.1.9"}
(fact "builds targets triggered by a source namespace"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [dev/project-config (fn [] config)
                  make/build-triggered (fn [ns]
                                         (swap! calls conj ns)
                                         :triggered)]
      (dev/task-trigger "component.web-index") => :triggered
      (binding [dev/*default-trigger* "tama"]
        (dev/task-trigger) => :triggered)
      @calls => [(symbol "component.web-index") (symbol "tama")])))

^{:refer foundation-deploy.dev.web-index/task-start :added "4.1.9"}
(fact "starts Expo Web through the project make configuration"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [dev/project-config (fn [] config)
                  make/run:dev (fn [& args]
                                 (swap! calls conj args)
                                 :started)]
      (dev/task-start) => :started
      @calls => [[config]])))

^{:refer foundation-deploy.dev.web-index/task-stop :added "4.1.9"}
(fact "stops only an active project development window"
  (let [config {:instance (atom {:tag "web-index"})}
        calls (atom [])]
    (with-redefs [dev/project-config (fn [] config)
                  os/tmux:has-session? (fn [_] true)
                  os/tmux:has-window? (fn [_ _] true)
                  make/run-close (fn [& args]
                                   (swap! calls conj args)
                                   :closed)]
      (dev/task-stop) => :closed
      @calls => [[config :dev]])
    (with-redefs [dev/project-config (fn [] config)
                  os/tmux:has-session? (fn [_] false)
                  make/run-close (fn [& _]
                                   (throw (ex-info "should-not-run" {})))]
      (dev/task-stop) => :not-running)))

^{:refer foundation-deploy.dev.web-index/task-status :added "4.1.9"}
(fact "reports project and tmux state"
  (let [config {:instance (atom {:tag "web-index"})}]
    (with-redefs [dev/project-config (fn [] config)
                  make/dir (fn [_] ".build/web-index")
                  os/tmux:has-session? (fn [_] true)
                  os/tmux:list-windows (fn [_] ["CMD" "DEV-web-index"])]
      (dev/task-status)
      => {:session "DEV"
          :project ".build/web-index"
          :window "DEV-web-index"
          :active? true
          :running? true
          :windows ["CMD" "DEV-web-index"]})))

^{:refer foundation-deploy.dev.web-index/task-attach :added "4.1.9"}
(fact "opens a terminal attached to the development session"
  (let [calls (atom [])]
    (with-redefs [os/tmux:has-session? (fn [_] true)
                  os/os-run (fn [& args]
                              (swap! calls conj args)
                              :attached)]
      (dev/task-attach) => :attached
      @calls => [["tmux" "attach-session" "-t" "DEV"]])
    (with-redefs [os/tmux:has-session? (fn [_] false)
                  os/os-run (fn [& _]
                              (throw (ex-info "should-not-run" {})))]
      (dev/task-attach) => :not-running)))

^{:refer foundation-deploy.dev.web-index/task-setup :added "4.1.9"}
(fact "builds before starting the development server"
  (let [calls (atom [])]
    (with-redefs [dev/task-build (fn [] (swap! calls conj :build) :built)
                  dev/task-start (fn [] (swap! calls conj :start) :started)]
      (dev/task-setup) => :started
      @calls => [:build :start])))

^{:refer foundation-deploy.dev.web-index/task-run :added "4.1.9"}
(fact "dispatches development tasks and reports unknown names"
  (let [calls (atom [])]
    (with-redefs [dev/task-setup (fn [] (swap! calls conj :setup) :setup)
                  dev/task-build (fn [] (swap! calls conj :build) :build)
                  dev/task-start (fn [] (swap! calls conj :start) :start)
                  dev/task-trigger (fn [& args]
                                     (swap! calls conj (into [:trigger] args))
                                     :trigger)
                  dev/task-stop (fn [] (swap! calls conj :stop) :stop)
                  dev/task-status (fn [] (swap! calls conj :status) {:state :status})
                  dev/task-attach (fn [] (swap! calls conj :attach) :attach)]
      (dev/task-run "setup") => :setup
      (dev/task-run "build") => :build
      (dev/task-run "start") => :start
      (dev/task-run "trigger" "melbourne.slim") => :trigger
      (dev/task-run "stop") => :stop
      (dev/task-run "status") => {:state :status}
      (dev/task-run "attach") => :attach
      (dev/task-run nil) => {:state :status}
      @calls => [:setup :build :start [:trigger "melbourne.slim"] :stop :status :attach :status]
      (try
        (dev/task-run "unknown")
        false
        (catch clojure.lang.ExceptionInfo e
          [(ex-data e) (:task (ex-data e))]))
      => [{:task "unknown"
           :tasks ["setup" "build" "start" "trigger" "stop" "status" "attach"]}
          "unknown"])))

^{:refer foundation-deploy.dev.web-index/-main :added "4.1.9"}
(fact "returns success and failure exit codes"
  (let [calls (atom [])]
    (with-redefs [dev/task-run (fn [& args]
                                 (swap! calls conj [:run args])
                                 :ran)
                  dev/*exit* (fn [status]
                               (swap! calls conj [:exit status])
                               status)]
      (dev/-main "build" "extra") => 0
      @calls => [[:run ["build" "extra"]] [:exit 0]]))
  (let [calls (atom [])]
    (with-redefs [dev/task-run (fn [& _]
                                 (throw (ex-info "failed" {})))
                  dev/*exit* (fn [status]
                               (swap! calls conj status)
                               status)]
      (binding [*err* (java.io.StringWriter.)]
        (dev/-main "build") => 1)
      @calls => [1])))
