(ns indigo.repl
  (:require [clojure.string :as str]
            [indigo.event :as event]
            [std.lib :as h])
  (:import [java.io StringWriter]))

(defn evaluate-repl-command
  [event-bus command]
  (let [out-sw (new StringWriter)
        err-sw (new StringWriter)]
    (binding [*out* out-sw
              *err* err-sw]
      (try
        (let [result (eval (read-string command))]
          (event/publish event-bus :repl {:out (str out-sw)
                                          :err (str err-sw)
                                          :result (pr-str result)}))
        (catch Throwable t
          (event/publish event-bus :repl {:out (str out-sw)
                                          :err (str err-sw)
                                          :error (.getMessage t)}))))))
