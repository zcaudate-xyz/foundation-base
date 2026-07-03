(ns xt.substrate.page-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]]})

(defn.xt wrap-space-args
  "puts the model context as first argument"
  {:added "4.1"}
  [handler]
  (return
   (fn [context]
     (var args (or (. context ["args"]) []))
     (var params [context])
     (xt/x:arr-assign params args)
     (return (xt/x:apply handler params)))))

(defn.xt check-event
  "checks that a trigger matches signal and event"
  {:added "4.1"}
  [pred signal event ctx]
  (var check false)
  (try
    (var t (:? (xt/x:nil? pred)
               true

               (xt/x:is-boolean? pred)
               pred

               (xt/x:is-function? pred)
               (pred signal ctx)

               (xt/x:is-object? pred)
               (xt/x:get-key pred signal)

               :else
               (== signal pred)))
    (:= check (or (== true t)
                  (and (xt/x:is-function? t) (t event ctx))
                  false))
    (catch err
      (:= check false)))
  (return check))

(defn.xt run-tail-call
  "helper function for tail calls on run commands"
  {:added "4.1"}
  [context refresh-deps-fn]
  (var acc (xt/x:get-key context "acc"))
  (var path (xt/x:get-key context "path"))
  (var node (xt/x:get-key context "node"))
  (var space-id (xt/x:get-key (xt/x:get-key context "space") "id"))
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (when (and acc (not (xt/x:get-key acc "error")))
    (when refresh-deps-fn
      (refresh-deps-fn node space-id group-id model-id refresh-deps-fn)))
  (return acc))

(defn.xt run-remote
  "runs the remote function"
  {:added "4.1"}
  [context save-output path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-model/pipeline-run-remote context save-output event-model/async-fn-promise nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt run-refresh
  "helper function for refresh"
  {:added "4.1"}
  [context disabled path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-model/pipeline-run context disabled event-model/async-fn-promise nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt get-group-deps
  "gets group deps"
  {:added "4.1"}
  [group-id models]
  (var all-deps {})
  (xt/for:object [[model-id model-entry] models]
    (var deps (xt/x:get-key model-entry "deps"))
    (xt/for:array [path (or deps [])]
      (:= path (:? (xt/x:is-array? path) path [group-id path]))
      (xtd/set-in all-deps
                  [(xt/x:first path)
                   (xt/x:second path)
                   model-id]
                  true)))
  (return all-deps))

(defn.xt raw-callback-id
  "creates a stable node trigger id for one page space"
  {:added "4.1"}
  [space-id]
  (return (xt/x:cat "@/raw/page/" (or space-id ""))))

(defn.xt register-page-trigger
  "registers a raw page trigger directly on the node"
  {:added "4.1"}
  [node signal trigger-fn meta]
  (var entry {"id" signal
             "fn" trigger-fn
             "meta" (or meta {})})
  (xt/x:set-key (xt/x:get-key node "triggers")
               signal
               entry)
  (return entry))

(defn.xt unregister-page-trigger
  "removes a raw page trigger directly from the node"
  {:added "4.1"}
  [node signal]
  (var triggers (xt/x:get-key node "triggers"))
  (var prev (xt/x:get-key triggers signal))
  (xt/x:del-key triggers signal)
  (return prev))
