(ns xt.lang.event-box
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.event-common :as event-common]]})

(defspec.xt BoxPath
  [:xt/array :xt/any])

(defspec.xt BoxEvent
  [:xt/record
   ["path" BoxPath]
   ["value" [:xt/maybe :xt/any]]
   ["data" [:xt/dict :xt/str :xt/any]]
   ["meta" [:xt/maybe xt.lang.event-common/EventListenerMeta]]])

(defspec.xt EventBox
  [:xt/record
   ["::" :xt/str]
   ["listeners" xt.lang.event-common/EventListenerMap]
   ["data" [:xt/dict :xt/str :xt/any]]])

(defspec.xt make-box
  [:fn [:xt/any] EventBox])

(defspec.xt check-event
  [:fn [BoxEvent BoxPath] :xt/bool])

(defspec.xt add-listener
  [:fn [EventBox
        :xt/str
        BoxPath
        [:fn [BoxEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt get-data
  [:fn [EventBox [:xt/maybe BoxPath]] [:xt/maybe :xt/any]])

(defspec.xt set-data-raw
  [:fn [EventBox BoxPath :xt/any] :xt/any])

(defspec.xt set-data
  [:fn [EventBox BoxPath :xt/any] [:xt/array :xt/str]])

(defspec.xt del-data-raw
  [:fn [EventBox BoxPath] :xt/bool])

(defspec.xt del-data
  [:fn [EventBox BoxPath] [:xt/maybe [:xt/array :xt/str]]])

(defspec.xt reset-data
  [:fn [EventBox] [:xt/array :xt/str]])

(defspec.xt merge-data
  [:fn [EventBox BoxPath [:xt/dict :xt/str :xt/any]] [:xt/array :xt/str]])

(defspec.xt append-data
  [:fn [EventBox BoxPath :xt/any] [:xt/array :xt/str]])

(defn.xt make-box
  "creates a box"
  {:added "4.0"}
  [initial]
  (return
   (event-common/make-container initial "event.box" {})))

(defn.xt check-event
  "checks that event matches path predicate"
  {:added "4.0"}
  [event path]
  (var evpath (xt/x:get-key event "path"))
  (when (> (xt/x:len path) (xt/x:len evpath))
    (return false))
  (xt/for:array [[i v] path]
    (when (not= v (. evpath [i]))
      (return false)))
  (return true))

(defn.xt add-listener
  "adds a listener to box"
  {:added "4.0"}
  [box listener-id path callback meta]
  (:= path (event-common/arrayify-path path))
  (return
   (event-common/add-listener
    box listener-id "box" callback
    (xt/x:obj-assign
     {:box/path  path}
     meta)
    (fn [event]
      (return (-/check-event event path))))))

(def.xt ^{:arglists ([box listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists ([box])}
  list-listeners
  event-common/list-listeners)

(defn.xt get-data
  "gets the current data in the box"
  {:added "4.0"}
  [box path]
  (var #{data} box)
  (:= path (event-common/arrayify-path path))
  (return (xtd/get-in data path)))

(defn.xt set-data-raw
  "sets the data in the box"
  {:added "4.0"}
  [box path value]
  (var #{data} box)
  (cond (xtd/arr-empty? path)
        (xt/x:set-key box "data" value)
        
        :else
        (return (xtd/set-in data path value))))

(defn.xt set-data
  "sets data with a trigger"
  {:added "4.0"}
  [box path value]
  (var #{data} box)
  (:= path (event-common/arrayify-path path))
  (-/set-data-raw box path value)
  (return
   (event-common/trigger-listeners
    box
    {:path path
     :value value
     :data data})))

(defn.xt del-data-raw
  "removes the data in the box"
  {:added "4.0"}
  [box path]
  (:= path (event-common/arrayify-path path))
  (var #{data} box)
  (var ppath (xt/x:arr-slice path 0 (- (xt/x:len path) 1)))
  (var parent (xtd/get-in data ppath))
  (when parent
    (var val (xt/x:get-key parent (xt/x:last path)))
    (xt/x:del-key parent (xt/x:last path))
    (return (xt/x:not-nil? val)))
  (return false))

(defn.xt del-data
  "removes data with trigger"
  {:added "4.0"}
  [box path]
  (:= path (event-common/arrayify-path path))
  (var #{data} box)
  (when (-/del-data-raw box path)
    (return
     (event-common/trigger-listeners
      box
      {:path path
       :value nil
       :data data}))))

(defn.xt reset-data
  "resets the data in the box"
  {:added "4.0"}
  [box]
  (var #{initial} box)
  (return (-/set-data box (initial) [])))

(defn.xt merge-data
  "merges the data in the box"
  {:added "4.0"}
  [box path value]
  (:= path (event-common/arrayify-path path))
  (var prev   (-/get-data box path))
  (var merged (xtd/obj-assign (xtd/obj-clone prev) value))
  (return
   (-/set-data box path merged)))

(defn.xt append-data
  "merges the data in the box"
  {:added "4.0"}
  [box path value]
  (:= path (event-common/arrayify-path path))
  (var arr   (xt/x:arr-clone (-/get-data box path)))
  (xt/x:arr-push arr value)
  (return
   (-/set-data box path arr)))
