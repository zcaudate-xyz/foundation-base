(ns xt.runtime.type-sql-connection
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [lua.nginx.common-promise]
             [xt.runtime.interface-spec :as spec]
              [xt.runtime.interface-sql-connection :as sql-if]]})

(defn.xt driver?
  "checks if a value is a wrapped runtime sql driver"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "sql.connection.driver"
                   (xt/x:get-key obj "::")))))

(defn.xt connection?
  "checks if a value is a wrapped runtime sql connection"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "sql.connection"
                   (xt/x:get-key obj "::")))))

(defn.xt protocol-method
  "looks up a runtime protocol method from an object or its attached protocol"
  {:added "4.1"}
  [obj key]
  (var method (xt/x:get-key obj key))
  (when (xt/x:nil? method)
    (var protocol (spec/runtime-protocol obj))
    (when protocol
      (:= method (xt/x:get-key protocol key))))
  (return method))

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
  "ensures a value is a runtime sql driver"
  {:added "4.1"}
  [value]
  (when (not (-/driver? value))
    (xt/x:err "Value is not a runtime SQL driver"))
  (return value))

(defn.xt require-connection
  "ensures a value is a runtime sql connection"
  {:added "4.1"}
  [value]
  (when (not (-/connection? value))
    (xt/x:err "Value is not a runtime SQL connection"))
  (return value))

(defn.xt connection-create
  "wraps a raw backend connection with the runtime sql connection protocol"
  {:added "4.1"}
  [raw impl]
  (var protocol
       (spec/proto-spec
         [[sql-if/ISqlRuntimeConnection
           {"disconnect" (fn [self]
                          (var impl (xt/x:get-key self "_impl"))
                          (var raw  (xt/x:get-key self "_raw"))
                          (var disconnect-fn (xt/x:get-key impl "disconnect"))
                          (when (xt/x:nil? disconnect-fn)
                            (xt/x:err "SQL runtime connection missing disconnect implementation"))
                          (return (disconnect-fn raw)))
            "query"      (fn [self input]
                          (var impl (xt/x:get-key self "_impl"))
                          (var raw  (xt/x:get-key self "_raw"))
                          (var query-fn (xt/x:get-key impl "query"))
                          (when (xt/x:nil? query-fn)
                            (xt/x:err "SQL runtime connection missing query implementation"))
                          (return (query-fn raw input)))
            "query_sync" (fn [self input]
                          (var impl (xt/x:get-key self "_impl"))
                          (var raw  (xt/x:get-key self "_raw"))
                          (var query-sync-fn (xt/x:get-key impl "query_sync"))
                          (when (xt/x:nil? query-sync-fn)
                            (xt/x:err "SQL runtime connection missing query_sync implementation"))
                          (return (query-sync-fn raw input)))}]]))
  (return
   (spec/runtime-attach {"::" "sql.connection"
                         :_raw raw
                         :_impl impl}
                        protocol)))

(defn.xt driver-create
  "wraps an implementation map with the runtime sql driver protocol"
  {:added "4.1"}
  [impl]
  (var protocol
       (spec/proto-spec
         [[sql-if/ISqlRuntimeDriver
           {"connect" (fn [self opts]
                        (var impl (xt/x:get-key self "_impl"))
                        (var connect-fn (xt/x:get-key impl "connect"))
                         (when (xt/x:nil? connect-fn)
                           (xt/x:err "SQL runtime driver missing connect implementation"))
                         (return
                          (promise/x:promise-then
                           (-/ensure-promise (connect-fn opts))
                           (fn [conn]
                             (return (-/require-connection conn))))))}]]))
  (return
   (spec/runtime-attach {"::" "sql.connection.driver"
                          :_impl impl}
                         protocol)))

(defn.xt driver-connect
  "connects through the runtime sql driver protocol"
  {:added "4.1"}
  [driver opts]
  (:= driver (-/require-driver driver))
  (var connect-fn (-/protocol-method driver "connect"))
  (when (xt/x:nil? connect-fn)
    (xt/x:err "SQL runtime driver missing connect method"))
  (return (connect-fn driver opts)))

(defn.xt connection-disconnect
  "disconnects through the runtime sql connection protocol"
  {:added "4.1"}
  [conn]
  (:= conn (-/require-connection conn))
  (var disconnect-fn (-/protocol-method conn "disconnect"))
  (when (xt/x:nil? disconnect-fn)
    (xt/x:err "SQL runtime connection missing disconnect method"))
  (return (disconnect-fn conn)))

(defn.xt connection-query
  "queries through the runtime sql connection protocol"
  {:added "4.1"}
  [conn input]
  (:= conn (-/require-connection conn))
  (var query-fn (-/protocol-method conn "query"))
  (when (xt/x:nil? query-fn)
    (xt/x:err "SQL runtime connection missing query method"))
  (return (query-fn conn input)))

(defn.xt connection-query-sync
  "runs sync queries through the runtime sql connection protocol"
  {:added "4.1"}
  [conn input]
  (:= conn (-/require-connection conn))
  (var query-sync-fn (-/protocol-method conn "query_sync"))
  (when (xt/x:nil? query-sync-fn)
    (xt/x:err "SQL runtime connection missing query_sync method"))
  (return (query-sync-fn conn input)))
