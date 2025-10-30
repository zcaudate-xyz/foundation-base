(ns jvm.require
  (:require [std.lib :as h])
  (:refer-clojure :exclude [load resolve]))

(defn force-require
  "a more flexible require, reloading namespaces if errored"
  {:added "4.0"}
  ([ns]
   (force-require ns []))
  ([ns stack]
   (let [parse-fn (fn [msg]
                    (if-let [ns-str (second (re-find #"namespace '(.*)' not found"
                                                     msg))]
                      (symbol ns-str)))
         ns-dep (try
                  (require ns :reload)
                  nil
                  (catch Throwable t
                    (if-let [cause (.getCause t)]
                      (if-let [ns-dep (parse-fn (.getMessage cause))]
                        ns-dep
                        (throw t))
                      (throw t))))]
     (cond ns-dep
           (do (h/p :REQUIRING ns-dep)
               (recur ns-dep (cons ns stack)))

           (not-empty stack)
           (recur (first stack) (rest stack))
           
           :else
           (h/p :DONE)))))
