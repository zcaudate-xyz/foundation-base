(ns xt.event.base-listener
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defspec.xt EventPayload
  :xt/any)

(defspec.xt EventListenerMeta
  :xt/any)

(defspec.xt EventListenerCallback
  [:fn [:xt/str :xt/any [:xt/maybe :xt/any] EventListenerMeta] :xt/any])

(defspec.xt EventListenerEntry
  [:xt/record
   ["callback" EventListenerCallback]
   ["meta" EventListenerMeta]
   ["pred" [:xt/maybe [:fn [:xt/any] :xt/bool]]]])

(defspec.xt EventListenerMap
  [:xt/dict :xt/str EventListenerEntry])

(defspec.xt EventListenerGroups
  [:xt/dict :xt/str EventListenerMap])

(defspec.xt EventTypeIndex
  [:xt/dict :xt/str [:xt/array :xt/str]])

(defspec.xt EventContainer
  [:xt/record
   ["::" :xt/str]
   ["listeners" EventListenerMap]])

(defspec.xt blank-container
  [:fn [:xt/str [:xt/dict :xt/str :xt/any]] EventContainer])

(defspec.xt make-container
  [:fn [:xt/any :xt/str [:xt/dict :xt/str :xt/any]] EventContainer])

(defspec.xt make-listener-entry
  [:fn [:xt/str :xt/str
        EventListenerCallback
        [:xt/maybe EventListenerMeta]
        [:xt/maybe [:fn [:xt/any] :xt/bool]]]
       EventListenerEntry])

(defspec.xt arrayify-path
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt clear-listeners
  [:fn [EventContainer] EventListenerMap])

(defspec.xt add-listener
  [:fn [EventContainer
        :xt/str
        :xt/str
        EventListenerCallback
        [:xt/maybe EventListenerMeta]
        [:xt/maybe [:fn [:xt/any] :xt/bool]]]
       EventListenerEntry])

(defspec.xt remove-listener
  [:fn [EventContainer :xt/str] [:xt/maybe EventListenerEntry]])

(defspec.xt list-listeners
  [:fn [EventContainer] [:xt/array :xt/str]])

(defspec.xt list-listener-types
  [:fn [EventContainer] EventTypeIndex])

(defspec.xt trigger-entry
  [:fn [EventListenerEntry EventPayload] [:xt/maybe :xt/any]])

(defspec.xt trigger-listeners
  [:fn [EventContainer [:xt/maybe EventPayload]] [:xt/array :xt/str]])

(defspec.xt add-keyed-listener
  [:fn [EventContainer
        :xt/str
        :xt/str
        :xt/str
        EventListenerCallback
        [:xt/maybe EventListenerMeta]
        [:xt/maybe [:fn [:xt/any] :xt/bool]]]
       EventListenerEntry])

(defspec.xt remove-keyed-listener
  [:fn [EventContainer :xt/str :xt/str] [:xt/maybe EventListenerEntry]])

(defspec.xt list-keyed-listeners
  [:fn [EventContainer :xt/str] [:xt/array :xt/str]])

(defspec.xt all-keyed-listeners
  [:fn [EventContainer] [:xt/dict :xt/str [:xt/array :xt/str]]])

(defspec.xt trigger-keyed-listeners
  [:fn [EventContainer :xt/str [:xt/maybe EventPayload]] [:xt/array :xt/str]])

(defn.xt blank-container
  "creates a blank container"
  {:added "4.0"}
  [type-name opts]
  (var container (xt/x:obj-assign
                  {"::" type-name
                   :listeners {}}
                  opts))
  (return container))

(defn.xt make-container
  "makes a container"
  {:added "4.0"}
  [initial type-name opts]
  (var initialFn initial)
  (when (not (xt/x:is-function? initialFn))
    (:= initialFn
        (fn [] (return initial))))
  (var data   (initialFn))
  (var container (xt/x:obj-assign
                  {"::" type-name
                   :data data
                   :initial initialFn
                   :listeners {}}
                  opts))
  (return container))

(defn.xt make-listener-entry
  "makes a listener entry"
  {:added "4.0"}
  [listener-id listener-type callback meta pred]
  (return
   {:callback callback
    :pred pred
    :meta (xt/x:obj-assign
           {:listener/id   listener-id
            :listener/type listener-type}
           meta)}))

(defn.xt listener-entry?
  "checks whether a listeners map value is a plain listener entry"
  {:added "4.1"}
  [entry]
  (return (and (xt/x:not-nil? entry)
               (xt/x:is-function? (xt/x:get-key entry "callback")))))

(defn.xt arrayify-path
  "normalizes event path-like inputs, treating empty objects as empty arrays"
  {:added "4.1"}
  [x]
  (when (xt/x:is-array? x)
    (return x))
  (when (or (xt/x:nil? x)
            (and (xt/x:is-object? x)
                 (xtd/is-empty? x)))
    (return []))
  (return [x]))

(defn.xt callback-data
  "normalizes event payload for callback consumers"
  {:added "4.1"}
  [event]
  (when (not (xt/x:is-object? event))
    (return event))
  (var out (xt/x:obj-clone event))
  (when (xt/x:has-key? out "meta")
    (xt/x:del-key out "meta"))
  (return out))

(defn.xt callback-time
  "extracts an event timestamp when present"
  {:added "4.1"}
  [event]
  (when (not (xt/x:is-object? event))
    (return nil))
  (when (xt/x:has-key? event "time")
    (return (xt/x:get-key event "time")))
  (when (xt/x:has-key? event "t")
    (return (xt/x:get-key event "t")))
  (return nil))

(defn.xt clear-listeners
  "clears all listeners"
  {:added "4.0"}
  [container]
   (var #{listeners} container)
   (var cleared {})
   (var kept {})
   (xt/for:object [[id entry] listeners]
     (if (-/listener-entry? entry)
       (xt/x:set-key cleared id entry)
       (xt/x:set-key kept id entry)))
   (xt/x:set-key container "listeners" kept)
   (return cleared))

(defn.xt add-listener
  "adds a listener to container"
  {:added "4.0"}
  [container listener-id listener-type callback meta pred]
  (var #{listeners} container)
  (var entry (-/make-listener-entry listener-id listener-type callback meta pred))
  (xt/x:set-key listeners listener-id entry)
  (return entry))

(defn.xt remove-listener
  "removes a listener"
  {:added "4.0"}
  [container listener-id]
  (var #{listeners} container)
  (var entry (xt/x:get-key listeners listener-id))
  (when (not (-/listener-entry? entry))
    (return nil))
  (xt/x:del-key listeners listener-id)
  (return entry))

(defn.xt list-listeners
  "lists all current listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (var out [])
  (xt/for:object [[id entry] listeners]
    (when (-/listener-entry? entry)
      (xt/x:arr-push out id)))
  (return out))

(defn.xt list-listener-types
  "lists listeners by their type"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (var out {})
  (xt/for:object [[id listener-entry] listeners]
    (when (-/listener-entry? listener-entry)
      (var #{meta} listener-entry)
      (var t   (xt/x:get-key meta "listener/type"))
      (var arr (xt/x:get-key out t))
      (when (xt/x:nil? arr)
        (:= arr [])
        (xt/x:set-key out t arr))
      (xt/x:arr-push arr id)))
  (return out))

(defn.xt trigger-entry
  "triggers the individual entry"
  {:added "4.0"}
  [entry event]
  (var #{callback meta pred} entry)
  (when (or (xt/x:nil? pred)
            (pred event))
    (var nmeta (xt/x:obj-assign (or (xt/x:get-key event "meta")
                                    {})
                                meta))
    (var listener-id (xt/x:get-key meta "listener/id"))
    (return
     (callback
      listener-id
      (-/callback-data event)
      (-/callback-time event)
      nmeta))))

(defn.xt trigger-listeners
  "triggers listeners given event"
  {:added "4.0"}
  [container event]
  (when (xt/x:nil? event)
    (:= event {}))
  (var #{listeners} container)
  (var triggered [])
  (xt/for:object [[id entry] listeners]
    (when (-/listener-entry? entry)
      (-/trigger-entry entry event)
      (xt/x:arr-push triggered id)))
  (return triggered))


;;
;;
;;

(defn.xt add-keyed-listener
  "adds a keyed entry"
  {:added "4.0"}
  [container key listener-id listener-type callback meta pred]
  (var #{listeners} container)
  (var entry (-/make-listener-entry listener-id listener-type callback meta pred))
  (var group (xt/x:get-key listeners key))
  (when (xt/x:nil? group)
    (:= group {})
    (xt/x:set-key listeners key group))
  (xt/x:set-key group listener-id entry)
  (return entry))

(defn.xt remove-keyed-listener
  "removes a keyed listener"
  {:added "4.0"}
  [container key listener-id]
  (var #{listeners} container)
  (var group (xt/x:get-key listeners key))
  (when (or (xt/x:nil? group)
            (-/listener-entry? group))
    (return nil))
  (var entry (xt/x:get-key group listener-id))
  (xt/x:del-key group listener-id)
  (when (xtd/obj-empty? group)
    (xt/x:del-key listeners key))
  (return entry))

(defn.xt list-keyed-listeners
  "lists all listeners under and key"
  {:added "4.0"}
  [container key]
  (var #{listeners} container)
  (var group (xt/x:get-key listeners key))
  (when (or (xt/x:nil? group)
            (-/listener-entry? group))
    (return []))
  (return (xt/x:obj-keys group)))

(defn.xt all-keyed-listeners
  "lists all listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (var out {})
  (xt/for:object [[key group] listeners]
    (when (not (-/listener-entry? group))
      (xt/x:set-key out key (-/list-keyed-listeners container key))))
  (return out))

(defn.xt trigger-keyed-listeners
  "triggers listeners under a key"
  {:added "4.0"}
  [container key event]
  (when (xt/x:nil? event)
    (:= event {}))
  (var #{listeners} container)
  (var group (xt/x:get-key listeners key))
  (var triggered [])
  (when (and (xt/x:not-nil? group)
             (not (-/listener-entry? group)))
    (xt/for:object [[id entry] group]
      (-/trigger-entry entry event)
      (xt/x:arr-push triggered id)))
  (return triggered))
