(ns my-http-tracer
  (:require [clojure.set :as set]
            [std.lib.trace :as trace]
            [std.lib.foundation :as h]
            [org.httpkit.client :as http]))

(defonce ^:dynamic *trace-server-url* "http://localhost:8080/trace")

(defn- wrap-http-print
  "A wrapper that sends the output of the standard :print trace via HTTP."
  [f ^trace/Trace t]
  (let [print-fn (trace/wrap-print f t)]
    (fn [& args]
      (let [sw (java.io.StringWriter.)
            result (binding [*out* sw]
                     (apply print-fn args))]
        ;; Post the trace message asynchronously
        (http/post *trace-server-url*
                   {:body (str sw)
                    :headers {"Content-Type" "text/plain"}})
        result))))

(defn- add-http-print-trace
  "Adds a print trace that also sends its output via HTTP."
  [v]
  (when (and (fn? @v) (not (trace/has-trace? v)))
    (let [original-fn @v
          t (trace/make-trace v :http-print original-fn)]
      (alter-meta! v assoc :code/trace t)
      (alter-var-root v (fn [_] (wrap-http-print original-fn t))))))

(defonce watched-ns-vars (atom {}))

(defn- var-watcher
  "This function is called by the watch mechanism whenever the namespace changes."
  [_key ns _old-state _new-state]
  (let [ns-sym (h/ns-sym ns)
        last-known-vars (get @watched-ns-vars ns-sym #{})
        current-vars (set (keys (ns-interns ns)))]
    (when-not (= last-known-vars current-vars)
      (let [new-var-syms (set/difference current-vars last-known-vars)]
        (when (seq new-var-syms)
          (doseq [var-sym new-var-syms]
            (let [v (find-var (symbol (str ns-sym) (str var-sym)))]
              (when (and (fn? @v) (not (-> v meta :macro)))
                (add-http-print-trace v))))))
      (swap! watched-ns-vars assoc ns-sym current-vars))))

(defn watch-ns
  "Starts watching a namespace for new var definitions and applies an HTTP trace.
   Usage: (watch-ns 'user)"
  [ns-sym]
  (if-let [ns (find-ns ns-sym)]
    (do
      (swap! watched-ns-vars assoc ns-sym (set (keys (ns-interns ns))))
      (add-watch ns ::var-watcher var-watcher))
    (println (format "[ERROR] Namespace not found: %s" ns-sym))))

(defn unwatch-ns
  "Stops watching a namespace and removes all traces from it.
   Usage: (unwatch-ns 'user)"
  [ns-sym]
  (if-let [ns (find-ns ns-sym)]
    (do
      (remove-watch ns ::var-watcher)
      (swap! watched-ns-vars dissoc ns-sym)
      (trace/untrace-ns ns))
    (println (format "[ERROR] Namespace not found: %s" ns-sym))))
