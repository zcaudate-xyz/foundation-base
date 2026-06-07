(ns xt.net.conn-sql
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as protocol]
             [xt.lang.spec-base :as xt]]})

(def.xt ISqlClient
  ["connect"
   "disconnect"
   "query"
   "query_async"])

(defn.xt create-base
  [type methods]
  (return
   (xt/x:obj-assign {"::" (or type "net.sql")}
                    (protocol/proto-spec
                     [[-/ISqlClient methods]]))))

;;
;; Methods
;;

(defn.xt connect
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client opts]
  (var connect-fn (xt/x:get-key client "connect"))
  (return (protocol/ensure-promise
           (connect-fn client opts))))

(defn.xt disconnect
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client]
  (var disconnect-fn (xt/x:get-key client "disconnect"))
  (return (disconnect-fn client opts)))


(defn.xt query
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input]
  (var query-fn (xt/x:get-key client "query"))
  (return (query-fn client input)))

(defn.xt query-async
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input]
  (var query-fn (xt/x:get-key client "query_async"))
  (return (protocol/ensure-promise
           (query-fn client input))))


