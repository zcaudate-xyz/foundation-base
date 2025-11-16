(ns xt.sys.cache-throttle
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.sys.cache-common :as cache]]})

(defn.xt throttle-key
  "creates the throttle key"
  {:added "0.1"}
  [throttle key]
  (return (k/cat "__throttle__:" (. throttle ["tag"]) ":" key)))

(defn.xt throttle-create
  "creates a throttle"
  {:added "0.1"}
  [tag handler now-fn]
  (return {:tag tag
           :now-fn (or now-fn k/now-ms)
           :handler handler}))

(defn.xt throttle-run-async
  "runs a throttle"
  {:added "0.1"}
  [throttle id]
  (var #{handler now-fn} throttle)
  (return (k/for:async [[ret err] (handler id)]
            {:finally (do (cache/meta-dissoc (-/throttle-key throttle "active")
                                             id)
                          (var queued (cache/meta-get (-/throttle-key throttle "queued")))
                          (when (not= nil (k/get-key queued id))
                            (cache/meta-assoc (-/throttle-key throttle "active")
                                              id (now-fn))
                            (cache/meta-dissoc (-/throttle-key throttle "queued")
                                               id)
                            (return (-/throttle-run-async throttle id))))})))

(defn.xt throttle-run
  "runs a throttle"
  {:added "0.1"}
  [throttle id]
  (var #{tag handler now-fn} throttle)
  
  (var queued (cache/meta-get (-/throttle-key throttle "queued")))
  (when (not= nil (k/get-key queued id))
    (return nil))
  
  (var active (cache/meta-get (-/throttle-key throttle "active")))
  (when (not= nil (k/get-key active id))
    (cache/meta-assoc (-/throttle-key throttle "queued")
                      id (now-fn))
    (return nil))
  (cache/meta-assoc (-/throttle-key throttle "active")
                    id (now-fn))
  (return (-/throttle-run-async throttle id)))

