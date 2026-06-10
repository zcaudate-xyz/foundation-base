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
  [impl tree]
  (return ((-/get-method impl "pull") impl tree)))

(defn.xt pull-async
  [impl tree]
  (return ((-/get-method impl "pull_async") impl tree)))

(defn.xt rpc-call-async
  [impl rpc-spec args]
  (return ((-/get-method impl "rpc_call_async") impl rpc-spec args)))

(defn.xt pull-async
  [impl tree]
  (return ((-/get-method impl "pull_async") impl tree)))

(defn.xt record-add
  [impl table-name records]
  (return ((-/get-method impl "record_add") impl table-name records)))

(defn.xt record-add-async
  [impl table-name records]
  (return ((-/get-method impl "record_add_async") impl table-name records)))

(defn.xt record-delete
  [impl table-name ids]
  (return ((-/get-method impl "record_delete") impl table-name ids)))

(defn.xt record-delete-async
  [impl table-name ids]
  (return ((-/get-method impl "record_delete_async") impl table-name ids)))

(defn.xt process-add-event
  [impl data]
  (return ((-/get-method impl "process_add_event") impl data)))

(defn.xt process-remove-event
  [impl data]
  (return ((-/get-method impl "process_remove_event") impl data)))


