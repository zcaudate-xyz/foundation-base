(ns xt.db.system.backend-neon
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]]})

(defn.xt create-client
  "Rejects runtimes without an adapter for this explicitly selected backend."
  [defaults]
  (xt/x:err "The neon backend has no client for this runtime"))

(defn.xt provider
  "Returns this backend's native-client and implementation factories."
  []
  (return {"create_client" -/create-client "create_impl" impl/impl-neon}))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]
             [xt.net.http-fetch :as http-fetch]
             [js.net.http-fetch :as js-native]]})

(defn.js create-client
  [defaults]
  (return (js-native/create defaults [http-fetch/wrap-prepare-input http-fetch/wrap-normalise])))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]
             [xt.net.http-fetch :as http-fetch]
             [python.net.http-fetch :as py-native]]})

(defn.py create-client
  [defaults]
  (return (py-native/create defaults [http-fetch/wrap-prepare-input http-fetch/wrap-normalise])))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]
             [xt.net.http-fetch :as http-fetch]
             [ruby.net.http-fetch :as rb-native]]})

(defn.rb create-client
  [defaults]
  (return (rb-native/create defaults [http-fetch/wrap-prepare-input http-fetch/wrap-normalise])))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]
             [xt.net.http-fetch :as http-fetch]
             [dart.net.http-fetch :as dt-native]]})

(defn.dt create-client
  [defaults]
  (return (dt-native/create defaults [http-fetch/wrap-prepare-input http-fetch/wrap-normalise])))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-neon :as impl]
             [xt.net.http-fetch :as http-fetch]
             [lua.nginx.http-fetch :as lua-native]]})

(defn.lua create-client
  [defaults]
  (return (lua-native/create defaults [http-fetch/wrap-prepare-input http-fetch/wrap-normalise])))
