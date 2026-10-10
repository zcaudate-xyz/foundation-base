(ns js.react.ext-table
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]
             [xt.db.node.table-util :as table-util]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [xt.lang.spec-promise :as promise]]})

(defn.js listen-sync
  {:added "4.1"}
  [source callback]
  (cond
    (xt/x:is-function? source) (return (source callback))
    (and (xt/x:is-object? source)
         (xt/x:is-function? (xt/x:get-key source "listen")))
    (return ((xt/x:get-key source "listen") callback))
    :else (return nil)))

(defn.js table-query-input
  "uses the table's configured spec when the model has no query arguments"
  {:added "4.1"}
  [args]
  (when (== 0 (xt/x:len args))
    (return nil))
  (return args))

(defn.js init-runtime
  {:added "4.1"}
  [context]
  (var runtime-config (xt/x:get-key context "runtime"))
  (var node (xt/x:get-key context "node"))
  (when runtime-config
    (when (k/nil? node)
      (:= node (substrate/node-create {}))
      (xt/x:set-key context "node" node))
    ;; init-server installs @xt.db/rpc-call and the other node handlers.
    (runtime/init-server node)
    ;; kernel-init creates the configured Supabase and cache services.
    (return
     (client-base/kernel-init
      node
      (xt/x:get-key runtime-config "config")
      (or (xt/x:get-key runtime-config "schema") {})
      (or (xt/x:get-key runtime-config "lookup") {})
      (or (xt/x:get-key runtime-config "opts") {}))))
  (return nil))

(defn.js runtime-handler
  {:added "4.1"}
  [runtime-init handler]
  (return
   (fn [...args]
     (if runtime-init
       (return
        (promise/x:promise-then
         runtime-init
         (fn [_]
           (return (handler ...args)))))
       (return (handler ...args))))))

(defn.js makeActionView
  {:public true
   :added "4.1"}
  [impl action-key m context]
  (var action (xt/x:get-key (xt/x:get-key impl "actions" {}) action-key))
  (var runtime-init (-/init-runtime context))
  (var action-handler (table-util/action-fn action context))
  (return (ext-model/makeModelRaw
           (xtd/obj-assign
            {:handler (-/runtime-handler runtime-init action-handler)}
                           (or m {})))))

(defn.js useActionView
  {:public true :added "4.1"}
  [impl action-key m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeActionView impl action-key m context)))
  (var action (xt/x:get-key (xt/x:get-key impl "actions" {}) action-key))
  (r/init []
    (var cleanup (-/listen-sync (xt/x:get-key action "event-sync")
                                (fn [_] (return (ext-model/refresh-model view {})))))
    (return (fn []
              (when (xt/x:is-function? cleanup)
                (cleanup)))))
  (return view))

(defn.js makeRemoteView
  {:public true :added "4.1"}
  [impl request-level m context]
  (var runtime-init (-/init-runtime context))
  (return (ext-model/makeModelRaw
           (xtd/obj-assign
            {:handler (-/runtime-handler
                       runtime-init
                       (fn [...args]
                         (return (table-util/table-pull
                                  impl "call" request-level
                                  (-/table-query-input [...args])
                                  nil
                                  context))))}
            (or m {})))))

(defn.js useRemoteView
  {:public true :added "4.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeRemoteView impl request-level m context)))
  (r/init []
    (var current (xtd/get-in (event-model/get-input view) ["current" "data"]))
    (when (or (k/nil? current) (xtd/arr-every current k/not-nil?))
      (ext-model/refresh-model view {})))
  (return view))

(defn.js makeListView
  {:public true :added "4.1"}
  [impl request-level m context]
  (var runtime-init (-/init-runtime context))
  (return (ext-model/makeModelRaw
           (xtd/obj-assign
            {:handler (-/runtime-handler
                       runtime-init
                       (fn [...args]
                         (return (table-util/table-pull
                                  impl "cached" request-level [...args] [] context))))
             :pipeline {:remote {:handler (-/runtime-handler
                                           runtime-init
                                           (fn [...args]
                                             (return (table-util/table-pull
                                                      impl "call" request-level [...args] [] context))))}
                        :sync {:handler (-/runtime-handler
                                         runtime-init
                                         (fn [...args]
                                           (return (table-util/sync-unknown
                                                    impl request-level [...args] [] context))))}}}
            (or m {})))))

(defn.js useListView
  {:public true :added "4.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeListView impl request-level m context)))
  (r/init []
    (var cleanup (-/listen-sync
                  (xt/x:get-key impl "event-sync")
                  (fn [_]
                    (when (not (xt/x:get-key opts "sync_manual"))
                      (return (ext-model/refresh-model view {}))))))
    (var input (xtd/get-in (event-model/get-input view) ["current" "data"]))
    (when (and input (xtd/arr-every input k/not-nil?))
      (-> (ext-model/refresh-model view {})
          (promise/x:promise-then
           (fn []
             (when (not (xt/x:get-key opts "sync_manual"))
               (return (ext-model/refresh-model-remote view true {})))))))
    (return cleanup))
  (return view))

(defn.js makeSingleView
  {:public true :added "4.1"}
  [impl request-level m context]
  (var options (or m {}))
  (var default-process (xt/x:get-key options "defaultProcess"))
  (var runtime-init (-/init-runtime context))
  (return (ext-model/makeModelRaw
           (xtd/obj-assign
            {:handler (-/runtime-handler
                       runtime-init
                       (fn [id]
                         (when id
                           (return (promise/x:promise-then
                                    (table-util/table-ids-data
                                     impl "cached" request-level [id] [] context)
                                    (fn [rows] (return (xt/x:first rows))))))))
             :pipeline {:main {:process default-process}
                        :remote {:handler (-/runtime-handler
                                           runtime-init
                                           (fn [id]
                                             (when id
                                               (return (promise/x:promise-then
                                                        (table-util/table-ids-data
                                                         impl "call" request-level [id] [] context)
                                                        (fn [rows] (return (xt/x:first rows))))))))
                                  :process default-process}}}
            options))))

(defn.js useSingleView
  {:public true :added "4.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeSingleView impl request-level m context)))
  (r/init []
    (var cleanup (-/listen-sync
                  (xt/x:get-key impl "event-sync")
                  (fn [_] (return (ext-model/refresh-model view {})))))
    (-> (ext-model/refresh-model view {})
        (promise/x:promise-then
         (fn [] (return (ext-model/refresh-model-remote view true {})))))
    (return cleanup))
  (return view))

(defn.js useActions
  {:public true :added "4.1"}
  [impls context]
  (return (r/const
           (xtd/arr-foldl (xtd/arrayify impls)
                          (fn [acc impl]
                            (return (xtd/obj-assign
                                     acc (table-util/make-actions impl context))))
                          {}))))

