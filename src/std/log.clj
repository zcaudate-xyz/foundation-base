(ns std.log
  (:require [std.protocol.log :as protocol.log]
            [std.log.core :as core]
            [std.log.common :as common]
            [std.log.console :as console]
            [std.log.form :as form]
            [std.log.profile :as profile]
            [std.lib :as h]))

(h/intern-in  common/put-logger!
              common/set-logger!
              common/set-static!
              common/set-level!
              common/set-context!

              console/console-logger

              core/identity-logger
              core/multi-logger
              core/basic-logger
              core/step

              form/log-meta
              form/log-runtime
              form/log-context
              form/log
              form/verbose
              form/debug
              form/info
              form/warn
              form/error
              form/fatal

              profile/spy
              profile/show
              profile/trace
              profile/track
              profile/status
              profile/silent
              profile/action
              profile/meter
              profile/block
              profile/section
              profile/profile
              profile/note
              profile/task
              profile/todo)

(defn create
  "creates a component compatible logger
 
   (log/create {:type :basic})"
  {:added "3.0"}
  ([m]
   (protocol.log/-create m)))

(defn logger
  "creates an starts a logger
 
   (log/logger {:type :basic})"
  {:added "3.0"}
  ([m]
   (-> (create m)
       (h/start))))

(defn logger?
  "checks if the object is a logger
 
   (log/logger? (log/create {:type :basic}))
   => true"
  {:added "3.0"}
  ([m]
   (satisfies? protocol.log/ILogger m)))

(defmacro with-level
  "executes body with a given level"
  {:added "3.0"}
  ([level & body]
   `(binding [common/*level* ~level]
      ~@body)))

(defmacro with-logger
  "executes code with a logger"
  {:added "3.0"}
  ([logger & body]
   `(binding [common/*logger* ~logger]
      ~@body)))

(defmacro with-overwrite
  "executes code with a given overwrite"
  {:added "3.0"}
  ([overwrite & body]
   `(binding [common/*overwrite* ~overwrite]
      ~@body)))

(defn current-context
  "returns the current context"
  {:added "3.0"}
  ([]
   common/*context*))

(defmacro with-context
  "executes code with a given context"
  {:added "3.0"}
  ([context & body]
   `(binding [common/*context* (merge common/*context* ~context)]
      ~@body)))

(defmacro with
  "enables targeted printing of statements"
  {:added "3.0"}
  ([opts & body]
   `(let [~'{:keys [instance] :as logger} (or common/*logger* (console-logger))
          ~'logger (assoc ~'logger :instance (atom (merge (deref ~'instance) ~opts)))]
      (binding [common/*logger* ~'logger]
        ~@body))))

(defmacro with-logger-basic
  "executes code with the basic logger (for debugging the logger)"
  {:added "3.0"}
  ([& body]
   `(binding [common/*logger* (common/basic-logger)]
      ~@body)))

(defmacro with-logger-verbose
  "executes code with a verbose logger (for printing out everything)"
  {:added "3.0"}
  ([& body]
   `(binding [common/*logger* (common/verbose-logger)
              common/*level* :verbose]
      ~@body)))

(comment
  (:raw (:executor @(:instance common/*logger*)))

  (with-logger-basic
    (info "OEUOEu"))
  (with-logger-verbose
    (info "oeuoeu"
          {:hello [1 2 3 4]}))
  (with-logger-basic
    (macroexpand-1 (meter (+ 1 2 3))))
  (./reset ['std.log]))
