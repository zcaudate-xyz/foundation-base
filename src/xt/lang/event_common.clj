(ns xt.lang.event-common
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defspec.xt EventPayload
  :xt/any)

(defspec.xt EventListenerMeta
  :xt/any)

(defspec.xt EventListenerEntry
  [:xt/record
   ["callback" [:fn [:xt/any] :xt/any]]
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
        [:fn [:xt/any] :xt/any]
        [:xt/maybe EventListenerMeta]
        [:xt/maybe [:fn [:xt/any] :xt/bool]]]
       EventListenerEntry])

(defspec.xt clear-listeners
  [:fn [EventContainer] EventListenerMap])

(defspec.xt add-listener
  [:fn [EventContainer
        :xt/str
        :xt/str
        [:fn [:xt/any] :xt/any]
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
        [:fn [:xt/any] :xt/any]
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
  (var container (k/obj-assign
                  {"::" type-name
                   :listeners {}}
                  opts))
  (return container))

(defn.xt make-container
  "makes a container"
  {:added "4.0"}
  [initial type-name opts]
  (var initialFn (:? (k/fn? initial)
                     initial
                     (fn:> initial)))
  (var data   (initialFn))
  (var container (k/obj-assign
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
          :meta (k/obj-assign
                 {:listener/id   listener-id
                  :listener/type listener-type}
                 meta)
          :pred pred}))

(defn.xt clear-listeners
  "clears all listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (k/set-key container "listeners" {})
  (return listeners))

(defn.xt add-listener
  "adds a listener to container"
  {:added "4.0"}
  [container listener-id listener-type callback meta pred]
  (var #{listeners} container)
  (var entry (-/make-listener-entry listener-id listener-type callback meta pred))
  (k/set-key listeners listener-id entry)
  (return entry))

(defn.xt remove-listener
  "removes a listener"
  {:added "4.0"}
  [container listener-id]
  (var #{listeners} container)
  (var entry (k/get-key listeners listener-id))
  (k/del-key listeners listener-id)
  (return entry))

(defn.xt list-listeners
  "lists all current listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (return (k/obj-keys listeners)))

(defn.xt list-listener-types
  "lists listeners by their type"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (var out {})
  (k/for:object [[id entry] listeners]
    (var #{meta} entry)
    (var t   (k/get-key meta "listener/type"))
    (var arr (k/get-key out t))
    (when (k/nil? arr)
      (:= arr [])
      (k/set-key out t arr))
    (x:arr-push arr id))
  (return out))

(defn.xt trigger-entry
  "triggers the individual entry"
  {:added "4.0"}
  [entry event]
  (var #{callback meta pred} entry)
  (when (or (k/nil? pred)
            (pred event))
    (var nmeta (k/obj-assign (or (k/get-key event "meta")
                                 {})
                             meta))
    (callback (k/obj-assign
               (k/obj-clone event)
               {:meta nmeta}))))

(defn.xt trigger-listeners
  "triggers listeners given event"
  {:added "4.0"}
  [container event]
  (:= event (or event {}))
  (var #{listeners} container)
  (var triggered [])
  (k/for:object [[id entry] listeners]
    (-/trigger-entry entry event)
    (x:arr-push triggered id))
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
  (var group (k/get-key listeners key))
  (when (k/nil? group)
    (:= group {})
    (k/set-key listeners key group))
  (k/set-key group listener-id entry)
  (return entry))

(defn.xt remove-keyed-listener
  "removes a keyed listener"
  {:added "4.0"}
  [container key listener-id]
  (var #{listeners} container)
  (var group (k/get-key listeners key))
  (when (k/nil? group)
    (return nil))
  (var entry (k/get-key group listener-id))
  (k/del-key group listener-id)
  (when (k/is-empty? group)
    (k/del-key listeners key))
  (return entry))

(defn.xt list-keyed-listeners
  "lists all listeners under and key"
  {:added "4.0"}
  [container key]
  (var #{listeners} container)
  (var group (k/get-key listeners key))
  (when (k/nil? group)
    (return []))
  (return (k/obj-keys group)))

(defn.xt all-keyed-listeners
  "lists all listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (return
   (k/arr-juxt (k/obj-keys listeners)
               k/identity
               (fn [key]
                 (return (-/list-keyed-listeners container key))))))

(defn.xt trigger-keyed-listeners
  "triggers listeners under a key"
  {:added "4.0"}
  [container key event]
  (:= event (or event {}))
  (var #{listeners} container)
  (var group (k/get-key listeners key))
  (var triggered [])
  (when (k/not-nil? group)
    (k/for:object [[id entry] group]
      (-/trigger-entry entry event)
      (x:arr-push triggered id)))
  (return triggered))
