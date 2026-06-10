(ns xt.db.system.main
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase :as impl-supabase]
             [xt.db.system.main-client :as main-client]]})

;;
;; The xt.db.node.adaptor-base 
;;

(defn.xt create-impl
  "creates impls for local and live backends"
  {:added "4.1"}
  [type defaults schema lookup]
  (var client (main-client/create-client type defaults))
  (cond (== type "memory")
        (return
         (impl-memory/impl-memory schema lookup))

        (== type "sqlite")
        (return
         (impl-sqlite/impl-sqlite client schema lookup))

        (== type "postgres")
        (return
         (impl-postgres/impl-postgres client schema lookup))

        (== type "supabase")
        (return
         (impl-supabase/impl-supabase client schema lookup))))


(defn.xt create-impl-init
  "initialises postgres impls and leaves the wrapper output usable"
  {:added "4.1"}
  [impl]
  (var type (xt/x:get-key impl "::"))
  (cond (== type "db.impl.sqlite")
        (return
         (-> (impl-sqlite/impl-sqlite-init impl)
             (promise/x:promise-then
              (fn [client]
                (return impl)))))
        
        (== type "db.impl.postgres")
        (return
         (-> (impl-postgres/impl-postgres-init impl)
             (promise/x:promise-then
              (fn [client]
                (return impl)))))

        :else
        (return
         (promise/x:promise-run impl))))

(defn.xt get-method
  "gets the method for a given implementation"
  {:added "4.1"}
  [impl method-name]
  (var #{methods} impl)
  (var impl-fn (xt/x:get-key methods method-name))
  (when (xt/x:nil? impl-fn)
    (xt/x:err (xt/x:cat "Method Not Supported - "
                        method-name
                        " - "
                        (xt/x:get-key impl "::"))))
  (return impl-fn))

(defn.xt pull
  "pull reads from the local memory impl"
  {:added "4.1"}
  [impl tree]
  (return ((-/get-method impl "pull") impl tree)))

(defn.xt pull-async
  "getting same semantics for supabase and postgres"
  {:added "4.1"}
  [impl tree]
  (return ((-/get-method impl "pull_async") impl tree)))

(defn.xt rpc-call-async
  "rpc-call-async reaches the live supabase rpc endpoint"
  {:added "4.1"}
  [impl rpc-spec args]
  (return ((-/get-method impl "rpc_call_async") impl rpc-spec args)))

(defn.xt pull-async
  "getting same semantics for supabase and postgres"
  {:added "4.1"}
  [impl tree]
  (return ((-/get-method impl "pull_async") impl tree)))

(defn.xt record-add
  "record-add writes through the local memory impl"
  {:added "4.1"}
  [impl table-name records]
  (return ((-/get-method impl "record_add") impl table-name records)))

(defn.xt record-add-async
  "record-add-async writes through promise semantics"
  {:added "4.1"}
  [impl table-name records]
  (return ((-/get-method impl "record_add_async") impl table-name records)))

(defn.xt record-delete
  "record-delete removes ids through the local memory impl"
  {:added "4.1"}
  [impl table-name ids]
  (return ((-/get-method impl "record_delete") impl table-name ids)))

(defn.xt record-delete-async
  "record-delete-async removes ids through promise semantics"
  {:added "4.1"}
  [impl table-name ids]
  (return ((-/get-method impl "record_delete_async") impl table-name ids)))

(defn.xt process-add-event
  "process-add-event merges nested data into the local memory impl"
  {:added "4.1"}
  [impl data]
  (return ((-/get-method impl "process_add_event") impl data)))

(defn.xt process-remove-event
  "process-remove-event removes nested data in lookup order"
  {:added "4.1"}
  [impl data]
  (return ((-/get-method impl "process_remove_event") impl data)))


