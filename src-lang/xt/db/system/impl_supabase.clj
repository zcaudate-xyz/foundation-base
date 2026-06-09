(ns xt.db.system.impl-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.db.text.pgrest-tree :as pgrest-tree]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.lib-supabase :as lib-supabase]]})

(defn.xt pull-async
  "runs a tree ir pull with async supabase semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl))

(defn.xt rpc-call-async
  [impl rpc-spec args]
  (var #{client} impl)
  (return
   (lib-supabase/rpc-call client ....)))

(defn.xt impl-supabase
  "creates the thin supabase impl record with stored context"
  {:added "4.1"}
  [client schema lookup]
  (return
   (impl-common/impl-base "db.impl.supabase"
                          client
                          schema
                          lookup
                          {})))
