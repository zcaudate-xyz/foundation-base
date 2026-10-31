(ns xt.db.system.backend-supabase
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]]})

(defn.xt create-client
  "Rejects runtimes without an adapter for this explicitly selected backend."
  [defaults]
  (xt/x:err "The supabase backend has no client for this runtime"))

(defn.xt provider
  "Returns this backend's native-client and implementation factories."
  []
  (return {"create_client" -/create-client "create_impl" impl/impl-supabase}))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.http-fetch :as http-fetch]
             [js.net.http-fetch :as js-native]
             [xt.net.addon-supabase :as addon]
             [js.net.ws-native :as js-ws]]})

(defn.js create-client
  [defaults]
  (var client (js-native/create defaults (addon/middleware-supabase)))
  (xt/x:set-key client "create_ws_client" js-ws/create)
  (return client))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.http-fetch :as http-fetch]
             [python.net.http-fetch :as py-native]
             [xt.net.addon-supabase :as addon]
             [python.net.ws-native :as py-ws]]})

(defn.py create-client
  [defaults]
  (var client (py-native/create defaults (addon/middleware-supabase)))
  (xt/x:set-key client "create_ws_client" py-ws/create)
  (return client))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.http-fetch :as http-fetch]
             [ruby.net.http-fetch :as rb-native]
             [xt.net.addon-supabase :as addon]
             [ruby.net.ws-native :as rb-ws]]})

(defn.rb create-client
  [defaults]
  (var client (rb-native/create defaults (addon/middleware-supabase)))
  (xt/x:set-key client "create_ws_client" rb-ws/create)
  (return client))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.http-fetch :as http-fetch]
             [dart.net.http-fetch :as dt-native]
             [xt.net.addon-supabase :as addon]
             [dart.net.ws-native :as dt-ws]]})

(defn.dt create-client
  [defaults]
  (var client (dt-native/create defaults (addon/middleware-supabase)))
  (xt/x:set-key client "create_ws_client" (fn [options] (return (dt-ws/create (xt/x:obj-assign options {"background" true})))))
  (return client))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.http-fetch :as http-fetch]
             [lua.nginx.http-fetch :as lua-native]
             [xt.net.addon-supabase :as addon]
             [lua.net.ws-native :as lua-ws]]})

(defn.lua create-client
  [defaults]
  (var client (lua-native/create defaults (addon/middleware-supabase)))
  (xt/x:set-key client "create_ws_client" lua-ws/create)
  (return client))
