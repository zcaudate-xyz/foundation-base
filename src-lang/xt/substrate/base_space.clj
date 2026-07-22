(ns xt.substrate.base-space
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate.base-frame :as frame]]})

(defspec.xt NodeSpace
  [:xt/record
   ["id" :xt/str]
   ["state" :xt/any]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt NodeSpaceUpdater
  [:fn [:xt/any NodeSpace :xt/any] :xt/any])

(defspec.xt space
  [:fn [:xt/str
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeSpace])

(defspec.xt get-space
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
       [:xt/maybe NodeSpace]])

(defspec.xt create-space
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeSpace])

(defspec.xt ensure-space
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeSpace])

(defspec.xt remove-space
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
       [:xt/maybe NodeSpace]])

(defspec.xt list-spaces
  [:fn [:xt/any] [:xt/array :xt/str]])

(defspec.xt get-space-state
  [:fn [:xt/any
        [:xt/maybe :xt/str]]
       :xt/any])

(defspec.xt set-space-state
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        :xt/any]
       :xt/any])

(defspec.xt update-space-state
  [:fn [:xt/any
        [:xt/maybe :xt/str]
        NodeSpaceUpdater]
       :xt/any])

(defn.xt space
  "constructs a node space record"
  {:added "4.1"}
  [space-id opts]
  (:= opts (or opts {}))
  (return
   {:id space-id
    :state (or (. opts ["state"]) {})
    :meta (or (. opts ["meta"]) {})}))

(defn.xt get-space
  "gets a node space by id"
  {:added "4.1"}
  [node space-id]
  (return
   (xt/x:get-key
    (. node ["spaces"])
    (or space-id frame/SPACE_NODE))))

(defn.xt create-space
  "creates or replaces a node space"
  {:added "4.1"}
  [node space-id opts]
  (var entry (-/space (or space-id frame/SPACE_NODE) opts))
  (xt/x:set-key (. node ["spaces"])
                (. entry ["id"])
                entry)
  (return entry))

(defn.xt ensure-space
  "ensures that a node space exists"
  {:added "4.1"}
  [node space-id opts]
  (var sid (or space-id frame/SPACE_NODE))
  (var entry (-/get-space node sid))
  (when (xt/x:nil? entry)
    (:= entry (-/create-space node sid opts)))
  (return entry))

(defn.xt remove-space
  "removes a node space"
  {:added "4.1"}
  [node space-id]
  (var sid (or space-id frame/SPACE_NODE))
  (var spaces (. node ["spaces"]))
  (var entry (xt/x:get-key spaces sid))
  (xt/x:del-key spaces sid)
  (return entry))

(defn.xt list-spaces
  "lists all current node spaces"
  {:added "4.1"}
  [node]
  (return (xtd/arr-sort (xt/x:obj-keys (. node ["spaces"]))
                        (fn [x] (return x))
                        xt/x:str-lt)))

(defn.xt get-space-state
  "gets state for a node space"
  {:added "4.1"}
  [node space-id]
  (var entry (-/ensure-space node space-id nil))
  (return (. entry ["state"])))

(defn.xt set-space-state
  "sets state for a node space"
  {:added "4.1"}
  [node space-id state]
  (var entry (-/ensure-space node space-id nil))
  (xt/x:set-key entry "state" state)
  (return state))

(defn.xt update-space-state
  "updates state for a node space"
  {:added "4.1"}
  [node space-id updater]
  (var entry (-/ensure-space node space-id nil))
  (var curr (. entry ["state"]))
  (var next (updater curr entry node))
  (xt/x:set-key entry "state" next)
  (return next))
