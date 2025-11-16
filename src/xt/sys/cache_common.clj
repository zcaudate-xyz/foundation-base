(ns xt.sys.cache-common
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [flush get set]))

(l/script :xtalk
  {:require  [[xt.lang.base-lib :as k]]})

;;
;; cache encode
;;


(defmacro.xt cache
  "gets a cache"
  {:added "4.0"}
  [name]
  (list 'x:cache name))

(defmacro.xt list-keys
  "lists keys in the cache"
  {:added "4.0"}
  [cache]
  (list 'x:cache-list cache))

(defmacro.xt flush
  "clears all keys in the cache"
  {:added "4.0"}
  [cache]
  (list 'x:cache-flush cache))

(defmacro.xt get
  "gets a cache entry"
  {:added "4.0"}
  [cache key]
  (list 'x:cache-get cache key))

(defmacro.xt set
  "sets a cache entry"
  {:added "4.0"}
  [cache key val]
  (list 'x:cache-set cache key val))

(defmacro.xt del
  "removes a cache entry"
  {:added "4.0"}
  [cache key]
  (list 'x:cache-del cache key))

(defmacro.xt incr
  "increments the cache key"
  {:added "4.0"}
  [cache key num]
  (list 'x:cache-incr cache key num))

(defn.xt get-all
  "gets the cache map"
  {:added "4.0"}
  [cache]
  (return (k/arr-juxt (-/list-keys cache)
                      k/identity
                      (fn:> [key] (-/get cache key)))))

(defmacro.xt meta-key
  "constructs a meta key"
  {:added "4.0"}
  [key]
  (list 'x:cat "__meta__:" key))

(defn.xt meta-get
  "gets the meta map"
  {:added "4.0"}
  [type]
  (var g (-/cache :GLOBAL))
  (var mkey (-/meta-key type))
  (var mstr (-/get g mkey))

  (cond (k/nil? mstr)
        (return {})

        (== "null" mstr)
        (return {})

        :else
        (return (k/json-decode mstr))))

(defn.xt meta-update
  "updates the meta map"
  {:added "4.0"}
  [type f]
  (var g (-/cache :GLOBAL))
  (var mprev (-/meta-get type))
  (var mcurr (f mprev))
  (var mkey (-/meta-key type))
  (-/set g mkey (k/json-encode mcurr))
  (return mcurr))

(defn.xt meta-assoc
  "adds a key to the meta"
  {:added "4.0"}
  [type key item]
  (return (-/meta-update
           type
           (fn:> [prev] (k/step-set-key prev key item)))))

(defn.xt meta-dissoc
  "dissocs a key from the meta"
  {:added "4.0"}
  [type key]
  (return (-/meta-update
           type
           (fn:> [prev] (k/step-del-key prev key)))))

