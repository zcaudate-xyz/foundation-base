(ns xt.lang.event-common
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

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

(defspec.xt arrayify-path
  [:fn [:xt/any] [:xt/array :xt/any]])

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

(defn.xt notify-task
  "extracts an async notification task from a callback return value"
  {:added "4.1"}
  [value]
  (when (and (xt/x:is-object? value)
             (== "notify.task"
                 (xt/x:get-key value "::")))
    (return (xt/x:get-key value "task")))
  (return nil))

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
  (var initialFn (:? (xt/x:is-function? initial)
                     initial
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

(defn.xt clear-listeners
  "clears all listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (xt/x:set-key container "listeners" {})
  (return listeners))

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
  (xt/x:del-key listeners listener-id)
  (return entry))

(defn.xt list-listeners
  "lists all current listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (return
   (xtd/arr-sort (xt/x:obj-keys listeners)
                 (fn [x] (return x))
                 xt/x:str-lt)))

(defn.xt list-listener-types
  "lists listeners by their type"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (var out {})
  (xt/for:object [[id listener-entry] listeners]
    (var #{meta} listener-entry)
    (var t   (xt/x:get-key meta "listener/type"))
    (var arr (xt/x:get-key out t))
    (when (xt/x:nil? arr)
      (:= arr [])
      (xt/x:set-key out t arr))
    (xt/x:arr-push arr id))
  (return out))

(defn.xt trigger-entry
  "triggers the individual entry"
  {:added "4.0"}
  [entry event]
  (var #{callback meta pred} entry)
  (var allowed true)
  (when (xt/x:not-nil? pred)
    (var result (pred event))
    (:= allowed (and (xt/x:not-nil? result)
                     (not= false result))))
  (when allowed
    (var event-meta (xt/x:get-key event "meta"))
    (var nmeta (xt/x:obj-assign (:? (xt/x:nil? event-meta)
                                    {}
                                    event-meta)
                                meta))
    (return (callback (xt/x:obj-assign
                       (xt/x:obj-clone event)
                       {:meta nmeta}))))
  (return nil))

(defn.xt trigger-listeners
  "triggers listeners given event"
  {:added "4.0"}
  [container event]
  (:= event (:? (xt/x:nil? event)
                {}
                event))
  (var #{listeners} container)
  (var triggered [])
  (xt/for:object [[id entry] listeners]
    (-/trigger-entry entry event)
    (xt/x:arr-push triggered id))
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
  (when (xt/x:nil? group)
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
  (when (xt/x:nil? group)
    (return []))
  (return
   (xtd/arr-sort (xt/x:obj-keys group)
                 (fn [x] (return x))
                 xt/x:str-lt)))

(defn.xt all-keyed-listeners
  "lists all listeners"
  {:added "4.0"}
  [container]
  (var #{listeners} container)
  (return
   (xtd/arr-juxt (xtd/arr-sort (xt/x:obj-keys listeners)
                               (fn [x] (return x))
                               xt/x:str-lt)
                 (fn [x] (return x))
                 (fn [key]
                    (return (-/list-keyed-listeners container key))))))

(defn.xt trigger-keyed-listeners
  "triggers listeners under a key"
  {:added "4.0"}
  [container key event]
  (:= event (:? (xt/x:nil? event)
                {}
                event))
  (var #{listeners} container)
  (var group (xt/x:get-key listeners key))
  (var triggered [])
  (when (xt/x:not-nil? group)
    (xt/for:object [[id entry] group]
      (-/trigger-entry entry event)
      (xt/x:arr-push triggered id)))
  (return triggered))
