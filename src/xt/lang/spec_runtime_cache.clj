(ns xt.lang.spec-runtime-cache
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:cache [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:cache
  "selects the global cache store"
  {:added "4.1"}
  ([name] (list (quote x:cache) name)))

(defspec.xt x:cache-list [:fn [:xt/any] [:xt/array :xt/str]])

(defmacro.xt ^{:standalone true} 
  x:cache-list
  "lists cache keys"
  {:added "4.1"}
  ([cache] (list (quote x:cache-list) cache)))

(defspec.xt x:cache-flush [:fn [:xt/any] :xt/self])

(defmacro.xt ^{:standalone true} 
  x:cache-flush
  "flushes cache stores"
  {:added "4.1"}
  ([cache] (list (quote x:cache-flush) cache)))

(defspec.xt x:cache-get [:fn [:xt/any :xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:cache-get
  "reads cache values"
  {:added "4.1"}
  ([cache key] (list (quote x:cache-get) cache key)))

(defspec.xt x:cache-set [:fn [:xt/any :xt/str :xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:cache-set
  "writes cache values"
  {:added "4.1"}
  ([cache key value] (list (quote x:cache-set) cache key value)))

(defspec.xt x:cache-del [:fn [:xt/any :xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:cache-del
  "deletes cache values"
  {:added "4.1"}
  ([cache key] (list (quote x:cache-del) cache key)))

(defspec.xt x:cache-incr [:fn [:xt/any :xt/str :xt/int] :xt/int])

(defmacro.xt ^{:standalone true} 
  x:cache-incr
  "increments cached numeric values"
  {:added "4.1"}
  ([cache key val] (list (quote x:cache-incr) cache key val)))
