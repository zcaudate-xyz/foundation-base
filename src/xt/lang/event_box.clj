(ns xt.lang.event-box
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]]
   :export [MODULE]})

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
  (var evpath (k/get-key event "path"))
  (when (> (k/len path) (k/len evpath))
    (return false))
  (k/for:array [[i v] path]
    (when (not= v (. evpath [i]))
      (return false)))
  (return true))

(defn.xt add-listener
  "adds a listener to box"
  {:added "4.0"}
  [box listener-id path callback meta]
  (:= path (k/arrayify path))
  (return
   (event-common/add-listener
    box listener-id "box" callback
    (k/obj-assign
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
  (:= path (k/arrayify path))
  (return (k/get-in data path)))

(defn.xt set-data-raw
  "sets the data in the box"
  {:added "4.0"}
  [box path value]
  (var #{data} box)
  (cond (k/is-empty? path)
        (k/set-key box "data" value)
        
        :else
        (return (k/set-in data path value))))

(defn.xt set-data
  "sets data with a trigger"
  {:added "4.0"}
  [box path value]
  (var #{data} box)
  (:= path (k/arrayify path))
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
  (var #{data} box)
  (var ppath (k/arr-slice path 0 (- (k/len path) 1)))
  (var parent (k/get-in data ppath))
  (when parent
    (var val (k/get-key parent (k/last path)))
    (k/del-key parent (k/last path))
    (return (k/not-nil? val)))
  (return false))

(defn.xt del-data
  "removes data with trigger"
  {:added "4.0"}
  [box path]
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
  "sets data with a trigger"
  {:added "4.0"}
  [box path value]
  (var prev   (-/get-data box path))
  (var merged (k/obj-assign (k/obj-clone prev) value))
  (return
   (-/set-data box path merged)))

(defn.xt append-data
  "sets data with a trigger"
  {:added "4.0"}
  [box path value]
  (var arr   (k/arr-clone (-/get-data box path)))
  (x:arr-push arr value)
  (return
   (-/set-data box path arr)))

(def.xt MODULE (!:module))
