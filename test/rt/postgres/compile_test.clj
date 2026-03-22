(ns rt.postgres.compile-test
  (:use code.test)
  (:require [rt.postgres.compile :as compile]
            [rt.postgres.compile.server-db :as server-db]
            [rt.postgres.compile.server-api :as server-api]
            [rt.postgres.grammar.typed-common :as types]))

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

(swap! types/*type-registry*
       assoc
       'demo.core/UserAccount
       (types/make-table-def 'demo.core 'UserAccount [] :id nil nil nil))

^{:refer rt.postgres.compile.server-db/infer-sync-spec :added "4.0"}
(fact "infers auto sync for mutating table-return functions"
  (let [out (server-db/infer-sync-spec +shape-fn+)]
    [(:mode out) (:mutating? out) (:tables out)])
  => [:auto true ["UserAccount"]]

  (let [out (server-db/infer-sync-spec +manual-sync-fn+)]
    [(:mode out) (:mutating? out) (:tables out)])
  => [:manual false ["UserAccount" "UserProfile"]]

  (let [out (server-db/infer-sync-spec +read-fn+)]
    [(:mode out) (:mutating? out) (:tables out)])
  => [:none false ["UserAccount"]])

^{:refer rt.postgres.compile.server-db/db-sync-merge :added "4.0"}
(fact "attaches db/sync when the result does not already contain one"
  (server-db/db-sync-merge {:id "u1"} ["UserAccount"])
  => {:id "u1"
      :db/sync {"UserAccount" [{:id "u1"}]}}

  (server-db/db-sync-merge
   {:id "u1" :db/sync {"UserAccount" [{:id "u1"}]}}
   ["UserAccount"])
  => {:id "u1" :db/sync {"UserAccount" [{:id "u1"}]}})

^{:refer rt.postgres.compile.server-db/emit-target :added "4.0"}
(fact "emits source for the server-db target"
  (server-db/emit-target +shape-fn+ :supabase-db)
  => (contains "defn.pg create-user-sync")

  (server-db/list-targets)
  => '(:supabase-db))

^{:refer rt.postgres.compile.server-api/emit-target :added "4.0"}
(fact "emits source for the server-api target"
  (server-api/emit-target +shape-fn+ :xtalk-contracts)
  => (contains "def.xt"))

^{:refer rt.postgres.compile/list-targets :added "4.0"}
(fact "compatibility facade lists and emits across both split namespaces"
  (compile/list-targets)
  => '(:supabase-db :xtalk-contracts)

  (keys (compile/emit-targets +shape-fn+))
  => '(:supabase-db :xtalk-contracts))


^{:refer rt.postgres.compile/target-entry :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.compile/emit-targets :added "4.1"}
(fact "TODO")