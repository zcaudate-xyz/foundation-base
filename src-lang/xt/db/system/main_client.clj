(ns xt.db.system.main-client
  (:require [hara.lang :as l]))


(l/script :xtalk)

(defn.xt create-client
  "returns nil when the target runtime has no matching native database adapter"
  [type defaults]
  (return nil))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.addon-supabase :as addon]
             [js.net.http-fetch :as js-fetch]
             [js.net.ws-native :as js-ws]
             [js.net.conn-sqlite :as js-sqlite]
             [js.net.conn-postgres :as js-postgres]]})

(defn.js create-client
  [type defaults]
  (cond (== type "sqlite")
        (return (js-sqlite/create defaults))

        (== type "postgres")
        (return (js-postgres/create defaults))

        (== type "supabase")
        (do (var client (js-fetch/create defaults (addon/middleware-supabase)))
            (xt/x:set-key client "create_ws_client" js-ws/create)
            (return client))

        :else
        (return nil)))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.addon-supabase :as addon]
             [python.net.http-fetch :as py-fetch]
             [python.net.ws-native :as py-ws]
             [python.net.conn-sqlite :as py-sqlite]
             [python.net.conn-postgres :as py-postgres]]})

(defn.py create-client
  [type defaults]
  (cond (== type "sqlite")
        (return (py-sqlite/create defaults))

        (== type "postgres")
        (return (py-postgres/create defaults))

        (== type "supabase")
        (do (var client (py-fetch/create defaults (addon/middleware-supabase)))
            (xt/x:set-key client "create_ws_client" py-ws/create)
            (return client))

        :else
        (return nil)))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.addon-supabase :as addon]
             [dart.net.http-fetch :as dart-fetch]
             [dart.net.ws-native :as dart-ws]
             [dart.net.conn-sqlite :as dart-sqlite]
             [dart.net.conn-postgres :as dart-postgres]]})

(defn.dt create-client
  [type defaults]
  (cond (== type "sqlite")
        (return (dart-sqlite/create defaults))

        (== type "postgres")
        (return (dart-postgres/create defaults))

        (== type "supabase")
        (do (var client (dart-fetch/create defaults (addon/middleware-supabase)))
            (xt/x:set-key client "create_ws_client" dart-ws/create)
            (return client))

        :else
        (return nil)))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.addon-supabase :as addon]
             [lua.nginx.http-fetch :as lua-fetch]
             [lua.net.ws-native :as lua-ws]
             [lua.nginx.conn-sqlite :as lua-sqlite]
             [lua.nginx.conn-postgres :as lua-postgres]]})

(defn.lua create-client
  [type defaults]
  (cond (== type "sqlite")
        (return (lua-sqlite/create defaults))

        (== type "postgres")
        (return (lua-postgres/create defaults))

        (== type "supabase")
        (do (var client (lua-fetch/create defaults (addon/middleware-supabase)))
            (xt/x:set-key client "create_ws_client" lua-ws/create)
            (return client))

        :else
        (return nil)))
