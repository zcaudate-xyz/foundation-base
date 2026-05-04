(ns kmi.lang.type-connection-sql
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.connection-sql :as sql]]})

(defn.xt driver?
  "checks if a value is a wrapped runtime sql driver"
  {:added "4.1"}
  [obj]
  (return (sql/driver? obj)))

(defn.xt connection?
  "checks if a value is a wrapped runtime sql connection"
  {:added "4.1"}
  [obj]
  (return (sql/connection? obj)))

(defn.xt ensure-promise
  "wraps sync values in a native promise while passing promises through"
  {:added "4.1"}
  [value]
  (return (sql/ensure-promise value)))

(defn.xt require-driver
  "ensures a value is a runtime sql driver"
  {:added "4.1"}
  [value]
  (return (sql/require-driver value)))

(defn.xt require-connection
  "ensures a value is a runtime sql connection"
  {:added "4.1"}
  [value]
  (return (sql/require-connection value)))

(defn.xt connection-create
  "wraps a raw backend connection with the runtime sql connection protocol"
  {:added "4.1"}
  [raw impl]
  (return (sql/connection-create raw impl)))

(defn.xt driver-create
  "wraps an implementation map with the runtime sql driver protocol"
  {:added "4.1"}
  [impl]
  (return (sql/driver-create impl)))

(defn.xt driver-connect
  "connects through the runtime sql driver protocol"
  {:added "4.1"}
  [driver opts]
  (return (sql/connect driver opts)))

(defn.xt connection-disconnect
  "disconnects through the runtime sql connection protocol"
  {:added "4.1"}
  [conn]
  (return (sql/disconnect conn)))

(defn.xt connection-query
  "queries through the runtime sql connection protocol"
  {:added "4.1"}
  [conn input]
  (return (sql/query conn input)))

(defn.xt connection-query-sync
  "runs sync queries through the runtime sql connection protocol"
  {:added "4.1"}
  [conn input]
  (return (sql/query-sync conn input)))
