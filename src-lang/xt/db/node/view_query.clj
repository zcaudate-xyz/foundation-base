(ns xt.db.node.view-query
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt not-implemented
  "returns a standard query-not-implemented payload"
  {:added "4.1"}
  [payload]
  (return {:status "error"
           :tag "xt.db.node.view/query-not-implemented"
           :data payload}))

(defn.xt refresh-result
  "returns the public refresh result from the current view state"
  {:added "4.1"}
  [view]
  (return {:query_key (xt/x:get-key view "query_key")
           :value (xt/x:get-key view "value")
           :status (xt/x:get-key view "status")
           :use (or (xt/x:get-key view "use") {})}))
