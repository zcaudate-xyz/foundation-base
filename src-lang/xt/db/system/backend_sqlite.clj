(ns xt.db.system.backend-sqlite
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]]})

(defn.xt create-client
  "Rejects runtimes without an adapter for this explicitly selected backend."
  [defaults]
  (xt/x:err "The sqlite backend has no client for this runtime"))

(defn.xt provider
  "Returns this backend's native-client and implementation factories."
  []
  (return {"create_client" -/create-client "create_impl" impl/impl-sqlite}))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]
             [js.net.conn-sqlite :as js-native]]})

(defn.js create-client
  [defaults]
  (return (js-native/create defaults)))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]
             [python.net.conn-sqlite :as py-native]]})

(defn.py create-client
  [defaults]
  (return (py-native/create defaults)))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]
             [ruby.net.conn-sqlite :as rb-native]]})

(defn.rb create-client
  [defaults]
  (return (rb-native/create defaults)))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]
             [dart.net.conn-sqlite :as dt-native]]})

(defn.dt create-client
  [defaults]
  (return (dt-native/create defaults)))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-sqlite :as impl]
             [lua.nginx.conn-sqlite :as lua-native]]})

(defn.lua create-client
  [defaults]
  (return (lua-native/create defaults)))
