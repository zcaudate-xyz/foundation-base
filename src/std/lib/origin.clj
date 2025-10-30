(ns std.lib.origin
  (:require [std.lib.foundation :as h]
            [std.lib.function :as fn]
            [std.lib.atom :as atm]
            [clojure.set :as set]))

(def ^:dynamic *origin*
  (atom {}))

(defn clear-origin
  "clears the `*origin*` atom"
  {:added "3.0"}
  []
  (reset! *origin* {}))

(defn set-origin
  "sets the origin of a namespace"
  {:added "3.0"}
  [ns-origin & [ns-source]]
  (let [ns-key (or ns-source
                   (.getName *ns*))]
    (atm/swap-return! *origin*
      (fn [m]
        [{ns-key ns-origin}
         (assoc m ns-key ns-origin)
         ]))))

(defn unset-origin
  "unsets the origin of a namespace"
  {:added "3.0"}
  [& [ns-source]]
  (let [ns-key (or ns-source
                   (.getName *ns*))]
    (atm/swap-return! *origin*
      (fn [m]
        [(get m ns-key)
         (dissoc m ns-key)]))))

(defn get-origin
  "get the origin of a namespace"
  {:added "3.0"}
  [& [ns-source]]
  (get @*origin* (or ns-source
                     (.getName *ns*))))

(defmacro defn.origin
  "creates a function with settable origin"
  {:added "3.0"}
  [name & more]
  (let [[doc attr arglist & body] (fn/fn:create-args more)
        ns  (.getName *ns*)]
    `(defn ~name ~doc ~attr
       ~arglist
       (binding [*ns* (the-ns (or (get-origin)
                                  (quote ~ns)))]
         ~@body))))
