(ns std.concurrent.request-command
  (:require [std.concurrent.request :as r]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]))

(defn format-input
  "helper for formatting command input"
  {:added "3.0"}
  ([{:keys [format options] :as command} input opts]
   (if-let [input-fn (:input format)]
     (input-fn input (select-keys opts (:select options)))
     input)))

(defn format-output
  "helper for formatting command input"
  {:added "3.0"}
  ([{:keys [format options] :as command} output opts]
   (if-let [output-fn (:output format)]
     (output-fn output (select-keys opts (:select options)))
     output)))

(defmulti run-request
  "extensible function for command templates"
  {:added "3.0"}
  (fn [{:keys [type]} client args opts] type))

(defmethod run-request :single
  ([{:keys [function]} client args opts]
   (let [command (function args opts)]
     (r/req client command opts))))

(defmethod run-request :bulk
  ([{:keys [function options]} client args opts]
   (let [commands (function args opts)]
     (r/bulk:map client
                 #(r/req-fn %1 %2 (:bulk options))
                 commands opts))))

(defmethod run-request :transact
  ([{:keys [function options]} client args opts]
   (let [commands (function args opts)]
     (r/transact:map client
                     #(r/req-fn %1 %2 (:bulk options))
                     commands opts))))

(defmethod run-request :retry
  ([{:keys [function retry]} client args opts]
   (let [command   (function args opts)
         {:keys [pred]} retry
         retry-fn (fn [val]
                    (cond (instance? Throwable val)
                          (if (pred val)
                            ((:fn retry) client args opts)
                            (throw val))

                          :else val))]
     (->> (r/req:opts opts {:post [retry-fn]})
          (r/req client command)))))

(defn req:run
  "runs a command"
  {:added "3.0"}
  ([command client args]
   (req:run command client args {}))
  ([{:keys [options process] :as command} client args opts]
   (let [input-fn  (or (:input options) f/NIL)
         output-fn (or (:output options) f/NIL)
         input     (format-input command args (merge (input-fn args) opts))
         out-fn    #(format-output command % (merge (output-fn args) opts))
         opts (-> (merge (:default options) opts)
                  (r/req:opts process)
                  (r/req:opts {:post [out-fn]}))]
     (run-request command client input opts))))

(impl/defimpl Command [type name arguments function options format process])

(defn req:command
  "constructs a command"
  {:added "3.0"}
  ([m]
   (map->Command m)))
