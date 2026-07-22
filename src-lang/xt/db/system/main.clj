(ns xt.db.system.main
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main-client :as main-client]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-sqlite :as impl-sqlite]
             [xt.db.system.impl-postgres :as impl-postgres]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase :as impl-supabase]]})

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
  (var #{schema lookup} impl)
  (var type (. impl ["::"]))
  (cond (== type "xt.db.system.impl_sqlite/ImplSqlite")
        (return
         (impl-sqlite/impl-sqlite-init impl))

        (== type "xt.db.system.impl_postgres/ImplPostgres")
        (return
         (impl-postgres/impl-postgres-init impl))

        :else
        (return
         (promise/x:promise-run impl))))
