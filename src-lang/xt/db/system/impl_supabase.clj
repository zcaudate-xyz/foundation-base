(ns xt.db.system.impl-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]]})

(defn.xt pull-async
  "runs a tree ir pull with async supabase semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl)
  (return
   (conn-sql/query-async client (sql-graph/select schema tree opts))))

(defn.xt rpc-call-async
  [impl rpc-spec args]
  (var #{client} impl)
  (return
   (sql-call/call-raw client rpc-spec args)))

(defn.xt impl-supabase
  "creates the thin supabase impl record with stored context"
  {:added "4.1"}
  [client schema lookup]
  (return
   (impl-common/impl-base "db.impl.supabase"
                          client
                          schema
                          lookup
                          (sql-util/supabase-opts lookup))))

(defn.xt impl-supabase-init
  "connects the thin supabase impl through a runtime sql driver"
  {:added "4.1"}
  [impl]
  (var #{client
         schema
         lookup
         opts} impl)
  (return
   (-> (conn-sql/connect client)
       (promise/x:promise-then
        (fn [client]
          (return impl))))))
