(ns js.react.ext-page-app
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as xtt]
             [js.react.ext-model :as ext-model]
             [js.react.ext-page :as ext-page]
             [xt.lang.spec-promise :as promise]]})


(l/script :js
  {:require [[js.core.impl :as j]
             [js.react :as r]
             [statslink.full.link-remote :as link-remote]
             [statslink.full.util :as ut]
             [js.react.ext-model :as ext-view]
             [js.react.ext-cell :as ext-cell]
             [xt.lang.base-lib :as k]
             [xt.event.base-model :as event-view]]
   :export [MODULE]})

(defn.js makeActionView
  "makes an action view"
  {:added "0.1"}
  [impl action-key m context]
  (var main-handler  (link-remote/action-fn
                      (. impl actions [action-key])
                      context))
  (return
   (ext-view/makeViewRaw
          (j/assign
           {:handler  main-handler}
           m))))

(defn.js useActionView
  "initialises an action view"
  {:added "0.1"}
  [impl action-key m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeActionView impl action-key m context)))
  (var #{event-sync} (. impl actions [action-key]))
  (when event-sync
    (ext-cell/listenRawEvents
     event-sync
     (fn [e]
       (ext-view/refresh-view view))
     context))
  (r/init []
    (ext-view/refresh-view view))
  (return view))

(defn.js makeRemoteView
  "makes a remote view"
  {:added "0.1"}
  [impl request-level m context]
  (var main-handler
       (fn:> [...args]
         (link-remote/table-pull
          impl "remote" request-level [...args] [] context)))
  (return
   (ext-view/makeViewRaw
    (j/assign
     {:handler  main-handler}
     m))))

(defn.js useRemoteView
  "uses a remote view"
  {:added "0.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeRemoteView impl request-level m context)))
  (var #{event-sync} impl)
  (when event-sync
    (ext-cell/listenRawEvents
     event-sync
     (fn [e]
       #_(ext-view/refresh-view view))
     context))
  (r/init []
    (when (k/arr-every (or (. (event-view/get-input view)
                              current)
                           [])
                       k/not-nil?)
      (ext-view/refresh-view view)))
  (return view))

(defn.js makeListView
  "makes a table view"
  {:added "0.1"}
  [impl request-level m context]
  (var main-handler
       (fn [...args]
         (return
          (link-remote/table-pull
           impl "local" request-level [...args] [] context))))
  (var remote-handler
       (fn:> [...args]
         (link-remote/table-pull
          impl "remote" request-level [...args] [] context)))
  (var sync-handler
       (fn:> [...args]
         (link-remote/sync-unknown
          impl request-level [...args] [] context)))
  (var is-remote (k/not-nil? (or (k/get-in impl ["base" request-level])
                                 (k/get-in impl ["remote" request-level]))))
  (return
   (ext-view/makeViewRaw
    (j/assign
     {:handler  main-handler 
      :pipeline  {:remote (:? is-remote {:handler remote-handler})
                  :sync   (:? is-remote {:handler sync-handler})}}
     m))))

(defn.js useListView
  "main workhorse for view components"
  {:added "0.1"}
  [impl request-level m context opts]
  (:= opts (or opts {}))
  (var view (r/const (-/makeListView impl request-level m context)))
  (var #{event-sync} impl)
  (when event-sync
    (ext-cell/listenRawEvents
     event-sync
     (fn [e]
       (cond (. opts ["sync_manual"])
             (return)
             
             :else
             (ext-view/refresh-view view)))
     context))
  (r/init []
    (var input (k/get-in (event-view/get-input view)
                         ["current"
                          "data"]))
    (when (and input
               (k/arr-every input k/not-nil?))
      (. (ext-view/refresh-view view {})
         (then (fn []
                 (cond (. opts ["sync_manual"])
                       (return)
                       
                       (or (. opts ["sync_full"])
                           (k/is-empty? (event-view/get-current view)))
                       (return (ext-view/refresh-view-remote view true {}))
                       
                       :else
                       (return (ext-view/refresh-view-remote view true {}))
                       #_(return (ext-view/refresh-view-sync view true {}))))))))
  (return view))

(defn.js makeSingleView
  "makes a table view"
  {:added "0.1"}
  [impl request-level m context]
  (var #{defaultProcess} m)
  (var main-handler
       (fn [id]
         (when id
           (return ((ut/wrap-first
                     link-remote/table-ids-data)
                    impl "local" request-level [id] [] context)))))
  (var remote-handler
       (fn [id]
         (when id
           (return ((ut/wrap-first
                     link-remote/table-ids-data)
                    impl "remote" request-level [id] [] context)))))
  (return
   (ext-view/makeViewRaw
    (j/assign
     {:handler  main-handler 
      :pipeline  {:main   {:process defaultProcess}
                  :remote {:handler remote-handler
                           :process defaultProcess}}}
     m))))

(defn.js useSingleView
  "main workhorse for view components"
  {:added "0.1"}
  [impl request-level m context opts]
  (var view (r/const (-/makeSingleView impl request-level m context)))
  (var #{event-sync} impl)
  (when event-sync
    (ext-cell/listenRawEvents
     event-sync
     (fn [e]
       (ext-view/refresh-view view))
     context))
  (r/init []
    (. (ext-view/refresh-view view)
       (then (fn:> (ext-view/refresh-view-remote view)))))
  (return view))

(defn.js useActions
  "createss actions for api"
  {:added "0.1"}
  [impls context]
  (return
   (r/const
    (k/arr-foldl (k/arrayify impls)
                 (fn:> [acc impl]
                   (j/assign acc (link-remote/make-actions impl context)))
                 {}))))

(def.js MODULE (!:module))
