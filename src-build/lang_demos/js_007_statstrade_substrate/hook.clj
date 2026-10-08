(ns lang-demos.js-007-statstrade-substrate.hook
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.js-007-statstrade-substrate.link :as link]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [xt.event.base-model :as event-model]]})

(defn.js makeListView
  [impl m context]
  (return (ext-model/makeViewRaw
           (Object.assign {:handler (link/action-fn (. impl views list) context)
                           :defaultOutput []}
                          m))))

(defn.js makeSingleView
  [impl m context]
  (return (ext-model/makeViewRaw
           (Object.assign {:handler (link/action-fn (. impl views detail) context)}
                          m))))

(defn.js attachViewEvents
  "refreshes reads, filtering detail events by the current model input"
  [view event-sync context listener-id single]
  (return
   (link/addRawListener
    event-sync
    (fn [frame]
      (var args (. (event-model/get-input view) current data))
      (when (and args
                 (or (not single)
                     (== (. args [0]) (. frame data id))))
        (:= (. view event_refresh) (ext-model/refresh-view view))))
    context listener-id)))

(defn.js listenRawEvents
  "React lifecycle equivalent of ext-cell/listenRawEvents"
  [event-sync callback context]
  (var listener-id (r/const (. (Math.random) (toString 36) (slice 2))))
  (r/watch [event-sync callback context]
    (return (link/addRawListener event-sync callback context listener-id))))

(defn.js useViewEvents
  [view impl context single]
  (var listener-id (r/const (. (Math.random) (toString 36) (slice 2))))
  (r/watch [view impl context single]
    (return (-/attachViewEvents view (. impl event-sync) context listener-id single))))

(defn.js useListView
  [impl m context]
  (var view (r/useMemo (fn [] (return (-/makeListView impl m context))) [impl context]))
  (-/useViewEvents view impl context false)
  (ext-model/useRefreshArgs view [] {:remote "none"})
  (return view))

(defn.js useSingleView
  [impl m context id]
  (var view (r/useMemo (fn [] (return (-/makeSingleView impl m context))) [impl context]))
  (-/useViewEvents view impl context true)
  (ext-model/useRefreshArgs view [id] {:remote "none"})
  (return view))

(defn.js useActions
  [impl context]
  (return (r/useMemo (fn [] (return (link/make-actions impl context))) [impl context])))
