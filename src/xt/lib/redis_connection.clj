(ns xt.lib.redis-connection
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as proto]
             [xt.protocol.redis-connection :as redis-if]]})

(defn.xt driver?
  "checks if a value is a wrapped runtime redis driver"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "redis.connection.driver"
                   (xt/x:get-key obj "::")))))

(defn.xt connection?
  "checks if a value is a wrapped runtime redis connection"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "redis.connection"
                   (xt/x:get-key obj "::")))))

(defn.xt ensure-promise
  "wraps sync values in a native promise while passing promises through"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise
             (fn []
               (return value))))))

(defn.xt require-driver
  "ensures a value is a runtime redis driver"
  {:added "4.1"}
  [value]
  (when (not (-/driver? value))
    (xt/x:err "Value is not a runtime Redis driver"))
  (return value))

(defn.xt require-connection
  "ensures a value is a runtime redis connection"
  {:added "4.1"}
  [value]
  (when (not (-/connection? value))
    (xt/x:err "Value is not a runtime Redis connection"))
  (return value))

(defn.xt connection-create
  "wraps a raw backend connection with the runtime redis connection protocol"
  {:added "4.1"}
  [raw impl]
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[redis-if/IRedisRuntimeConnection
           {"disconnect" (fn [self]
                           (var raw  (xt/x:get-key self "_raw"))
                           (var impl (xt/x:get-key self "_impl"))
                           (var disconnect-fn (xt/x:get-key impl "disconnect"))
                           (when (xt/x:nil? disconnect-fn)
                             (xt/x:err "Redis runtime connection missing disconnect implementation"))
                           (return (disconnect-fn raw)))
            "exec"       (fn [self command args]
                           (var raw  (xt/x:get-key self "_raw"))
                           (var impl (xt/x:get-key self "_impl"))
                           (var exec-fn (xt/x:get-key impl "exec"))
                           (when (xt/x:nil? exec-fn)
                             (xt/x:err "Redis runtime connection missing exec implementation"))
                           (return (exec-fn raw command args)))}]])))
  (var conn {"::" "redis.connection"
             :_raw raw
             :_impl impl})
  (xt/proto:set conn protocol)
  (return conn))

(defn.xt driver-create
  "wraps an implementation map with the runtime redis driver protocol"
  {:added "4.1"}
  [impl]
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[redis-if/IRedisRuntimeDriver
           {"connect" (fn [self opts]
                        (var impl (xt/x:get-key self "_impl"))
                        (var connect-fn (xt/x:get-key impl "connect"))
                        (when (xt/x:nil? connect-fn)
                          (xt/x:err "Redis runtime driver missing connect implementation"))
                        (return
                         (promise/x:promise-then
                          (-/ensure-promise (connect-fn opts))
                          (fn [conn]
                            (return (-/require-connection conn))))))}]])))
  (var driver {"::" "redis.connection.driver"
               :_impl impl})
  (xt/proto:set driver protocol)
  (return driver))

(defn.xt connect
  "connects through the runtime redis driver protocol"
  {:added "4.1"}
  [driver opts]
  (:= driver (-/require-driver driver))
  (var connect-fn (xt/proto:method driver "connect"))
  (when (xt/x:nil? connect-fn)
    (xt/x:err "Redis runtime driver missing connect method"))
  (return (connect-fn driver opts)))

(defn.xt disconnect
  "disconnects through the runtime redis connection protocol"
  {:added "4.1"}
  [conn]
  (:= conn (-/require-connection conn))
  (var disconnect-fn (xt/proto:method conn "disconnect"))
  (when (xt/x:nil? disconnect-fn)
    (xt/x:err "Redis runtime connection missing disconnect method"))
  (return (disconnect-fn conn)))

(defn.xt exec
  "executes commands through the runtime redis connection protocol"
  {:added "4.1"}
  [conn command args]
  (:= conn (-/require-connection conn))
  (var exec-fn (xt/proto:method conn "exec"))
  (when (xt/x:nil? exec-fn)
    (xt/x:err "Redis runtime connection missing exec method"))
  (return (exec-fn conn command args)))
