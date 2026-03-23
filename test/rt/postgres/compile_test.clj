(ns rt.postgres.compile-test
  (:require [rt.postgres.compile :as compile]
            [rt.postgres.grammar.typed-common :as types])
  (:use code.test))

(def +shape-fn+
  (types/make-fn-def
   "demo.core"
   "create-user"
   [(types/->FnArg 'm :jsonb [:jsonb])]
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

^{:refer rt.postgres.compile/list-targets :added "4.0"}
(fact "compatibility facade lists and emits across both split namespaces"
  (compile/list-targets)
  => '(:supabase-db :xtalk-contracts)

  (keys (compile/emit-targets +shape-fn+))
  => '(:supabase-db :xtalk-contracts))


^{:refer rt.postgres.compile/target-entry :added "4.1"}
(fact "target-entry dispatches to the split target namespaces"
  (select-keys (compile/target-entry +shape-fn+ :supabase-db)
               [:target :emitted-sym])
  => {:target :supabase-db
      :emitted-sym 'create-user-sync}

  (select-keys (compile/target-entry +shape-fn+ :xtalk-contracts)
               [:target :emitted-sym])
  => {:target :xtalk-contracts
      :emitted-sym 'create-user-contract})

^{:refer rt.postgres.compile/emit-targets :added "4.1"}
(fact "emit-targets can be limited to a requested target subset"
  (keys (compile/emit-targets +shape-fn+ [:xtalk-contracts]))
  => '(:xtalk-contracts)

  (keys (compile/emit-targets +shape-fn+ [:supabase-db]))
  => '(:supabase-db))
