(ns rt.postgres.base.compile-test
  (:require [rt.postgres.base.compile :as compile]
            [rt.postgres.base.typed.typed-common :as types])
  (:use code.test))

(def +shape-fn+
  (types/make-fn-def
   "demo.core"
   "create-user"
   [(types/->FnArg 'm :jsonb [:jsonb] :payload)]
   :jsonb
   {:raw-body '[(pg/t:insert 'UserAccount {:id "u1"})]}
   nil))

(def +manual-sync-fn+
  (types/make-fn-def
   "demo.core"
   "create-user-manual"
   []
   :jsonb
   {:raw-body '[(pg/t:insert 'UserAccount {:id "u1"})]
    :sync/mode :manual
    :sync/tables ['UserAccount 'UserProfile]}
   nil))

(def +read-fn+
  (types/make-fn-def
   "demo.core"
   "get-user"
   []
   :jsonb
   {:raw-body '[(pg/t:get 'UserAccount {:where {:id "u1"}})]}
   nil))

(defn ensure-fixtures!
  []
  (swap! types/*type-registry*
         assoc
         'demo.core/UserAccount
         (types/make-table-def 'demo.core 'UserAccount [] :id nil nil nil)))

(ensure-fixtures!)

^{:refer rt.postgres.base.compile/infer-sync-spec :added "4.1"}
(fact "infer-sync-spec delegates to the split db compiler"
  (select-keys (compile/infer-sync-spec +manual-sync-fn+)
               [:mode :tables])
  => {:mode :manual
      :tables ["UserAccount" "UserProfile"]})

^{:refer rt.postgres.base.compile/db-sync-merge :added "4.1"}
(fact "db-sync-merge delegates to the split db compiler"
  (compile/db-sync-merge {:id "u1"} ["UserAccount"])
  => {:id "u1"
      :db/sync {"UserAccount" [{:id "u1"}]}})

^{:refer rt.postgres.base.compile/emit-target :added "4.1"}
(fact "emit-target delegates to both split target namespaces"
  (clojure.string/includes?
   (compile/emit-target +shape-fn+ :supabase-db)
   "create-user-sync")
  => true

  (clojure.string/includes?
   (compile/emit-target +shape-fn+ :xtalk-contracts)
   "create-user-contract")
  => true)

^{:refer rt.postgres.base.compile/list-targets :added "4.0"}
(fact "compatibility facade lists and emits across both split namespaces"
  (compile/list-targets)
  => '(:supabase-db :xtalk-contracts)

  (keys (compile/emit-targets +shape-fn+))
  => '(:supabase-db :xtalk-contracts))


^{:refer rt.postgres.base.compile/target-entry :added "4.1"}
(fact "target-entry dispatches to the split target namespaces"
  (select-keys (compile/target-entry +shape-fn+ :supabase-db)
               [:target :emitted-sym])
  => {:target :supabase-db
      :emitted-sym 'create-user-sync}

  (select-keys (compile/target-entry +shape-fn+ :xtalk-contracts)
               [:target :emitted-sym])
  => {:target :xtalk-contracts
      :emitted-sym 'create-user-contract})

^{:refer rt.postgres.base.compile/emit-targets :added "4.1"}
(fact "emit-targets can be limited to a requested target subset"
  (keys (compile/emit-targets +shape-fn+ [:xtalk-contracts]))
  => '(:xtalk-contracts)

  (keys (compile/emit-targets +shape-fn+ [:supabase-db]))
  => '(:supabase-db))
