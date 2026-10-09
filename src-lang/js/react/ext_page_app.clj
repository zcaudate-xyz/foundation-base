(ns js.react.ext-page-app
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.event.base-model :as event-model]
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

(defn.js makeActionView
  {:public true :added "4.1"}
  [impl action-key m context]
  (var action (xt/x:get-key (xt/x:get-key impl "actions" {}) action-key))
  (return (ext-model/makeViewRaw
           (xtd/obj-assign {:handler (table-util/action-fn action context)}
                           (or m {})))))

(defn.js useActionView
  {:public true :added "4.1"}
  [impl action-key m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeActionView impl action-key m context)))
  (var action (xt/x:get-key (xt/x:get-key impl "actions" {}) action-key))
  (r/init []
    (var cleanup (-/listen-sync (xt/x:get-key action "event-sync")
                                (fn [_] (return (ext-model/refresh-view view {})))))
    (ext-model/refresh-view view {})
    (return cleanup))
  (return view))

(defn.js makeRemoteView
  {:public true :added "4.1"}
  [impl request-level m context]
  (return (ext-model/makeViewRaw
           (xtd/obj-assign
            {:handler (fn [...args]
                        (return (table-util/table-pull
                                 impl "call" request-level [...args] [] context)))}
            (or m {})))))

(defn.js useRemoteView
  {:public true :added "4.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeRemoteView impl request-level m context)))
  (r/init []
    (var current (xtd/get-in (event-model/get-input view) ["current" "data"]))
    (when (or (k/nil? current) (xtd/arr-every current k/not-nil?))
      (ext-model/refresh-view view {})))
  (return view))

(defn.js makeListView
  {:public true :added "4.1"}
  [impl request-level m context]
  (return (ext-model/makeViewRaw
           (xtd/obj-assign
            {:handler (fn [...args]
                        (return (table-util/table-pull
                                 impl "cached" request-level [...args] [] context)))
             :pipeline {:remote {:handler (fn [...args]
                                            (return (table-util/table-pull
                                                     impl "call" request-level [...args] [] context)))}
                        :sync {:handler (fn [...args]
                                          (return (table-util/sync-unknown
                                                   impl request-level [...args] [] context)))}}}
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
                      (return (ext-model/refresh-view view {}))))))
    (var input (xtd/get-in (event-model/get-input view) ["current" "data"]))
    (when (and input (xtd/arr-every input k/not-nil?))
      (-> (ext-model/refresh-view view {})
          (then (fn []
                  (when (not (xt/x:get-key opts "sync_manual"))
                    (return (ext-model/refresh-view-remote view true {})))))))
    (return cleanup))
  (return view))

(defn.js makeSingleView
  {:public true :added "4.1"}
  [impl request-level m context]
  (var options (or m {}))
  (var default-process (xt/x:get-key options "defaultProcess"))
  (return (ext-model/makeViewRaw
           (xtd/obj-assign
            {:handler (fn [id]
                        (when id
                          (return (promise/x:promise-then
                                   (table-util/table-ids-data
                                    impl "cached" request-level [id] [] context)
                                   (fn [rows] (return (xt/x:first rows)))))))
             :pipeline {:main {:process default-process}
                        :remote {:handler (fn [id]
                                            (when id
                                              (return (promise/x:promise-then
                                                       (table-util/table-ids-data
                                                        impl "call" request-level [id] [] context)
                                                       (fn [rows] (return (xt/x:first rows)))))))
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
                  (fn [_] (return (ext-model/refresh-view view {})))))
    (-> (ext-model/refresh-view view {})
        (then (fn [] (return (ext-model/refresh-view-remote view true {})))))
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

