(ns code.doc.engine
  (:require [std.lib :as h])
  (:import (clojure.lang MultiFn)))

(defn wrap-hidden
  "helper function to not process elements with the `:hidden` tag"
  {:added "3.0"}
  ([f]
   (fn [{:keys [hidden] :as elem}]
     (if-not hidden (f elem)))))

(defn engine
  "dynamically loads the templating engine for publishing
 
   (engine \"winterfell\")"
  {:added "3.0"}
  ([name]
   (let [ns (cond (string? name)
                  (symbol (str "code.doc.engine." name))

                  (symbol? name) name

                  :else (throw (Exception.
                                (format "Not string or symbol: %s" name))))]
     (require ns)
     (reduce-kv (fn [out k ref]
                  (let [v @ref]
                    (cond (h/multi? v)
                          (assoc out (keyword k)
                                 (wrap-hidden (h/multi:clone v (str k))))

                          (fn? v)
                          (assoc out (keyword k) v)

                          :else out)))
                {}
                (ns-interns ns)))))
