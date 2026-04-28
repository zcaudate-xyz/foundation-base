(ns python.cache.localstore
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt get [cache key]
  (return (xt/x:get-key cache key)))

(defn.xt set [cache key value]
  (xt/x:set-key cache key value)
  (return cache))

(defn.xt list [cache]
  (return (xt/x:obj-keys cache)))

(defn.xt swap [cache key f args]
  (return (xtd/swap-key cache key f args)))
