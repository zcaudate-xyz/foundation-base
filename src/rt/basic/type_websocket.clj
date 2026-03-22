(ns rt.basic.type-websocket
  (:require [rt.basic.server-basic :as server]
            [rt.basic.server-websocket :as ws]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-bench :as bench]
            [rt.basic.type-common :as common]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.os :as os]
            [std.protocol.context :as protocol.context]))

(defn start-websocket
  "starts bench and server for websocket runtime"
  {:added "4.0"}
  [{:keys [id lang bench port process] :as rt}]
  (let [bench  (if (nil? bench)
                 false
                 bench)]
    (basic/start-basic (assoc rt :bench bench)
                       ws/create-websocket-server)))

(impl/defimpl RuntimeWebsocket [id]
  :string basic/rt-basic-string
  :protocols [std.protocol.component/IComponent
              :method {-start start-websocket
                       -stop basic/stop-basic
                       -kill basic/stop-basic}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    basic/raw-eval-basic
                       -invoke-ptr  basic/invoke-ptr-basic}])

(defn rt-websocket:create
  "creates a websocket runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           process] :as m
    :or {runtime :websocket}}]
  (let [process (collection/merge-nested (common/get-options lang :websocket :default)
                                process)]
    (map->RuntimeWebsocket (merge  m {:id (or id (f/sid))
                                      :tag runtime
                                      :runtime runtime
                                      :process process
                                      :lifecycle process}))))

(defn rt-websocket
  "creates and start a websocket runtime"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           program
           process] :as m}]
  (-> (rt-websocket:create m)
      (component/start)))

(comment
  (def rt (bench/start-bench :lua
                            {}
                            49373
                            {}))
  bench/*active*
  (os/sh-output (:process rt))
  (bench/stop-bench rt)
  )
