(ns rt.redis.client
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [std.protocol.request :as protocol.request]
            [std.protocol.log :as protocol.log]
            [std.protocol.wire :as protocol.wire]
            [std.lib :as h :refer [defimpl]]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lang.interface.type-shared :as shared]
            [net.resp.connection :as conn]
            [net.resp.pool :as pool]
            [lib.redis :as lib]
            [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [rt.redis.eval-basic :as eval-basic]
            [rt.redis.eval-script :as eval-script])
  (:import (hara.net.resp SocketConnection)))

(declare map->RedisClient)

(defn client:create
  "creates a redis client
 
   (r/client:create {:id \"localhost\"
                     :port 17001})
   => r/client?"
  {:added "3.0"}
  ([{:keys [id env mode host port] :as m}]
   (map->RedisClient
    (lib/client-create m))))

(defn client
  "creates and starts a redis client"
  {:added "3.0"}
  ([{:keys [id env] :as m}]
   (-> (client:create m)
       (h/start))))

(defn test:client
  "creates a test client on docker"
  {:added "3.0"}
  ([]
   (client (conn/test:config))))

;;
;; Wiring
;;

(defn invoke-ptr-redis
  "invokes the pointer in the redis context"
  {:added "4.0"}
  [redis ptr args]
  (let [#_#_redis (merge redis {:layout (or (:layout redis)
                                        :flat)})
        entry (ptr/get-entry ptr)
        [mode no-install]  (case (:mode redis)
                             :eval  [:json false]
                             :prod  [:raw true]
                             [(if (:rt/redis entry)
                                :raw
                                :json)
                              false])]
    (case mode
      :json (eval-basic/redis-invoke-ptr-basic redis ptr args)
      :raw  (eval-script/redis-invoke-sha redis ptr args no-install))))

(defimpl RedisClient [id host port pool format]
  :string lib/client-string
  :protocols [protocol.wire/IWire
              :prefix "pool/pool:" :suffix ""
              protocol.request/IRequest
              :prefix "pool/pool:" :suffix ""
              protocol.request/IRequestTransact
              :prefix "conn/connection:" :suffix ""

              protocol.component/IComponent
              :prefix "pool/pool:" :suffix ""
              :method {-start lib/client:start
                       -stop  lib/client:stop
                       -kill  lib/client:kill}
              protocol.component/IComponentQuery
              :prefix "pool/pool:" :suffix ""
              :body {-remote? true}
              protocol.component/IComponentProps
              :body {-props {}}

              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval eval-basic/redis-raw-eval
                       -invoke-ptr invoke-ptr-redis}])

(defn client?
  "checks that instance is a client"
  {:added "3.0"}
  ([obj]
   (instance? RedisClient obj)))

(def +lua-oneshot+
  [(default/install-type!
    :lua :redis.client
    {:type :hara/rt.redis.client
     :config {:bootstrap false
              :layout :flat}
     :instance {:create client:create}})
   (default/install-type!
    :lua :redis
    {:type :hara/rt.redis
     :config {:bootstrap false
              :layout :flat}
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :hara/rt.redis
                                 :constructor client:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])

(def +redis-oneshot+
  [(default/install-type!
    :redis :redis.client
    {:type :hara/rt.redis.client
     :config {:bootstrap false
              :layout :flat}
     :instance {:create client:create}})
   (default/install-type!
    :redis :redis
    {:type :hara/rt.redis
     :config {:bootstrap false
              :layout :flat}
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :hara/rt.redis
                                 :constructor client:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])



(comment
  
  (def +init+
    (do (rt.script/lang-rt-add
         )
        
        )))
