(ns lua.nginx.common-cache
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [get set flush]))

(l/script :lua.nginx
  {:import [["cjson" :as cjson]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [lua.nginx :as n]]})

(defn.lua cache
  "returns an nginx shared dict by name"
  {:added "4.1"}
  [name]
  (return (. ngx.shared [(k/to-string name)])))

(defn.lua get
  "gets a value from the shared dict"
  {:added "4.1"}
  [store key]
  (return (. store (get key))))

(defn.lua set
  "sets a value in the shared dict"
  {:added "4.1"}
  [store key value]
  (when (or (k/is-object? value)
            (k/is-array? value))
    (:= value (cjson.encode value)))
  (return (. store (set key value))))

(defn.lua del
  "deletes a value from the shared dict"
  {:added "4.1"}
  [store key]
  (return (. store (delete key))))

(defn.lua incr
  "increments a numeric value in the shared dict"
  {:added "4.1"}
  [store key amount]
  (return (. store (incr key amount 0))))

(defn.lua flush
  "flushes the shared dict"
  {:added "4.1"}
  [store]
  (return (. store (flush_all))))

(defn.lua list-keys
  "lists all keys for the shared dict"
  {:added "4.1"}
  [store]
  (return (. store (get_keys 0))))

(defn.lua get-all
  "gets the raw contents of the shared dict"
  {:added "4.1"}
  [store]
  (var out {})
  (xt/for:array [key (-/list-keys store)]
    (:= (. out [key]) (-/get store key)))
  (return out))

(defn.lua meta-key
  "returns the metadata key for a group"
  {:added "4.1"}
  [group]
  (return (cat "__meta__:" group)))

(defn.lua meta-get
  "gets decoded metadata for a group"
  {:added "4.1"}
  [group]
  (var raw (-/get (-/cache :GLOBAL) (-/meta-key group)))
  (if raw
    (return (cjson.decode raw))
    (return {})))

(defn.lua meta-assoc
  "associates metadata for a group"
  {:added "4.1"}
  ([group key value]
   (var meta (-/meta-get group))
   (:= (. meta [key]) value)
   (-/set (-/cache :GLOBAL) (-/meta-key group) (cjson.encode meta))
   (return meta))
  ([group key value _]
   (return (-/meta-assoc group key value))))

(defn.lua meta-dissoc
  "removes metadata for a group"
  {:added "4.1"}
  [group key]
  (var meta (-/meta-get group))
  (:= (. meta [key]) nil)
  (-/set (-/cache :GLOBAL) (-/meta-key group) (cjson.encode meta))
  (return meta))
