(ns xt.db.node.view-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt node-opts
  "gets xt.db.node options from node metadata"
  {:added "4.1"}
  [node]
  (return (or (xtd/get-in node ["meta" spec/META_KEY])
              {})))

(defn.xt set-node-opts
  "stores xt.db.node options on node metadata"
  {:added "4.1"}
  [node opts]
  (xtd/set-in node ["meta" spec/META_KEY] (or opts {}))
  (return opts))

(defn.xt state?
  "checks for a db.node state map"
  {:added "4.1"}
  [state]
  (return (and (xt/x:is-object? state)
               (== spec/STATE_TAG
                   (xt/x:get-key state "::")))))

(defn.xt request-payload
  "gets the first request payload"
  {:added "4.1"}
  [args]
  (return (or (:? (and (xt/x:is-array? args)
                       (> (xt/x:len args) 0))
                    (xt/x:first args)
                    nil)
              {})))

(defn.xt response-value
  "extracts the value portion of a response payload"
  {:added "4.1"}
  [response]
  (return (:? (and (xt/x:is-object? response)
                   (xt/x:has-key? response "value"))
               (xt/x:get-key response "value")
               response)))
