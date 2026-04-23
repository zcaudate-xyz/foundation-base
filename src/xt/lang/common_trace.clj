(ns xt.lang.common-trace
  (:require [std.lang :as l :refer [defspec.xt]]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [std.lib.env :as env]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

;;
;; METADATA
;;

(defn meta:info-fn
  "the function to get meta info"
  {:added "4.0"}
  [& [m]]
  (let [{:keys [namespace id]} (:entry (l/macro-opts))
        {:keys [line]} (meta (l/macro-form))]
    (merge
     {:meta/fn    (str (or namespace (env/ns-sym)) "/" id)
      :meta/line  line}
     m)))

(defmacro.xt meta:info
  "macro to inject meta info"
  {:added "4.0"}
  [& [m]]
  (meta:info-fn m))

(defmacro.xt LOG!
  "logging with meta info"
  {:added "4.0"}
  [& args]
  (let [{:keys [label]} (meta (l/macro-form))
        {:meta/keys [fn line]} (meta:info-fn)]
    (clojure.core/apply list 'x:print (clojure.core/str
                                       label
                                       " "
                                       fn)
                        line "\n\n" args)))

;;
;; TRACE
;;

(defn.xt trace-log
  "gets the current trace log"
  {:added "4.0"}
  []
  (if (xt/x:global-has? TRACE)
    (return (!:G TRACE))
    (do (xt/x:global-set TRACE [])
        (return (!:G TRACE)))))

(defn.xt trace-log-clear
  "resets the trace log"
  {:added "4.0"}
  []
  (do (xt/x:global-set TRACE [])
      (return (!:G TRACE))))

(defn.xt trace-log-add
  "adds an entry to the log"
  {:added "4.0"}
  [data tag opts]
  (var log (-/trace-log))
  (var m (xtd/obj-assign
          {:tag tag
           :data data
           :time (xt/x:now-ms)}
          opts))
  (xt/x:arr-push log m)
  (return (xt/x:len log)))

(defn.xt trace-filter
  "filters out traced entries"
  {:added "4.0"}
  [tag]
  (return (xt/x:arr-filter (-/trace-log) (fn [e] (return (== tag (xt/x:get-key e "tag")))))))

(defn.xt trace-last-entry
  "gets the last entry"
  {:added "4.0"}
  [tag]
  (var log (-/trace-log))
  (if (== nil tag)
    (return (xt/x:last log))
    (do (var tagged (-/trace-filter tag))
        (return (xt/x:last tagged)))))

(defn.xt trace-data
  "gets the trace data"
  {:added "4.0"}
  [tag]
  (return (xt/x:arr-map (-/trace-log)
                        (fn [e] (return (xt/x:get-key e "data"))))))

(defn.xt trace-last
  "gets the last value"
  {:added "4.0"}
  [tag]
  (return (xt/x:get-key (-/trace-last-entry tag)
                        "data")))

(defmacro.xt TRACE!
  "performs a trace call"
  {:added "4.0"}
  [data & [tag]]
  (let [pos   (meta (l/macro-form))
        ns    (env/ns-sym)
        opts  (assoc (select-keys pos [:line :column])
                     :ns (str ns))]
    (list `trace-log-add data (or tag (f/sid)) opts)))

(defn.xt trace-run
  "run helper for `RUN!` macro"
  {:added "4.0"}
  [f]
  (-/trace-log-clear)
  (f)
  (return (-/trace-log)))

(defmacro.xt RUN!
  "runs a form, saving trace forms"
  {:added "4.0"}
  [& body]
  (template/$ (do (var f (fn [] ~@body))
                  (return (xt.lang.common-trace/trace-run f)))))
