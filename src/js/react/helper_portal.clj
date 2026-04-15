(ns js.react.helper-portal
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j] [xt.lang.common-spec :as xt] [xt.lang.common-data :as xtd]]})

(defn.js newRegistry
  "creates a new portal registry"
  {:added "4.0"}
  []
  (return {:id (j/randomId 6)
           :sinks {}
           :sources  {}
           :initial  {}}))

;;
;; Usually, it's required for the portal to have a reference
;; to the sink, as it needs to know how the sink is
;; positioned in order to render `absolute` positioned items.
;;
;; Capture immediately will occur if the `sinkRef` has been initialised. 
;; The `sinkRef` may not be available when the
;; portal asks to capture it. This will be the case when a portial
;; has been placed inside the sink. In the second case, the `onSink`
;; callback is placed on an `initial` map to be executed
;; when the sink initialises
;;

(defn.js captureSink
  "captures the sink ref"
  {:added "4.0"}
  [reg name portalId onSink]
  (var #{initial} reg)
  (var entry (xtd/get-in reg ["sinks"
                              name]))
  (cond entry
        (do (var #{sinkRef} entry)
            (onSink sinkRef)
            (return sinkRef))
         
        :else
        (do (xtd/set-in initial
                        [name portalId]  onSink)
            (return nil))))

(defn.js triggerSink
  "triggers the registry"
  {:added "4.0"}
  [reg name]
  (var #{sinks
         sources} reg)
  (var entry (. sinks [name]))
  (when entry
    (var #{setSource} entry)
    (setSource (j/values (or (. sources [name])
                             {})))))

(defn.js addSink
  "adds a sink to the registry"
  {:added "4.0"}
  [reg name entry]
  (var #{sinks initial} reg)
  (xt/x:set-key sinks name entry)
  (var callbacks (xt/x:get-key initial name))
  (when callbacks
    (var #{sinkRef} entry)
    (xt/for:object [[id callback] callbacks]
      (callback sinkRef)))
  (xt/x:del-key initial name)
  (return (-/triggerSink reg name)))

(defn.js removeSink
  "removes a sink
 
   (!.js
    (portal/removeSink
     {:sinks  {:hello {:setSource (fn:>)}}
      :initial   {}
      :sources   {:hello {:hello-1 123
                          :hello-2 456
                          :hello-3 789}}}
     \"hello\"))
  => {}"
  {:added "4.0"}
  [reg name]
  (var #{sinks} reg)
  (var curr (xt/x:get-key sinks name))
  (xt/x:del-key sinks name)
  (return curr))

(defn.js addSource
  "adds a source for render"
  {:added "4.0"}
  [reg name portalId child]
  (var #{sources} reg)
  (var entries (. sources [name]))
  (when (not entries)
    (:= entries {})
    (xt/x:set-key sources name entries))
  (xt/x:set-key entries
                portalId
                child)
  (return (-/triggerSink reg name)))

(defn.js removeSource
  "removes a source for render"
  {:added "4.0"}
  [reg name portalId]
  (var #{sources} reg)
  (var entries (. sources [name]))
  (when entries
    (var curr (xt/x:get-key entries portalId))
    (xt/x:del-key entries portalId)
    (when (xtd/is-empty? entries)
      (xt/x:del-key sources name))
    (return (-/triggerSink reg name))))
