(ns std.log.common
  (:require [std.concurrent :as cc]
            [std.protocol.component :as protocol.component]
            [std.protocol.log :as protocol.log]))

(def ^:dynamic *level* :debug)

(def ^:dynamic *overwrite* nil)

(def ^:dynamic *context* nil)

(def ^:dynamic *trace* nil)

(def ^:dynamic *static* false)

(defonce ^:dynamic *logger* nil)

(defonce ^:dynamic *logger-basic* nil)

(defonce ^:dynamic *logger-verbose* nil)

(def +levels+ (zipmap [:verbose :debug :info :warn :error :fatal]
                      (range)))

(def +control-keys+ [:log/ignore :log/display :meter/display :trace/display])

(defn set-static!
  "sets the global static variable"
  {:added "3.0"}
  ([bool]
   (alter-var-root #'*static* (fn [_] bool))))

(defn set-level!
  "sets the global level variable"
  {:added "3.0"}
  ([status]
   (alter-var-root #'*level* (fn [_] status))))

(defn set-context!
  "sets the global context"
  {:added "3.0"}
  ([context]
   (alter-var-root #'*context* (fn [_] context))))

(defn set-logger!
  "sets the global logger"
  {:added "3.0"}
  ([logger]
   (set-logger! logger #'*logger*))
  ([logger var]
   (alter-var-root var (fn [_] logger))))

(defn put-logger!
  "updates the global logger"
  {:added "3.0"}
  ([{:keys [level context namespace function filter] :as data}]
   (put-logger! *logger* data))
  ([{:keys [instance] :as logger} data]
   (swap! instance merge data)
   logger))

(defn default-logger
  "returns the default logger"
  {:added "3.0"}
  ([]
   (or *logger*
       (doto (protocol.log/-create {:type :console})
         (set-logger!)))))

(defn basic-logger
  "returns the basic logger"
  {:added "3.0"}
  ([]
   (or *logger-basic*
       (doto (protocol.log/-create {:type :basic :level :verbose})
         (set-logger! #'*logger-basic*)))))

(defn verbose-logger
  "returns the verbose logger"
  {:added "3.0"}
  ([]
   (or *logger-verbose*
       (doto (protocol.log/-create {:type :console :level :verbose})
         (set-logger! #'*logger-verbose*)))))

(comment

  {:log/type :log  ;; :log :print :spy :meter
   :log/tag :hello/world
   :log/column 5,
   :log/filename "form-init13389367943922872356.clj",
   :log/function "eval44813",
   :log/level :info,
   :log/line 13,
   :log/message "",
   :log/method "invokeStatic",
   :log/namespace "std.log.serilog",
   :log/timestamp 1592046958467,
   :log/value "HELLO"}

  {:meter/form
   :meter/status
   :meter/result
   :meter/start
   :meter/end
   :meter/duratiion}

  {:trace/id
   :trace/name
   :trace/root
   :trace/parent})
