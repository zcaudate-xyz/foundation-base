(ns rt.postgres.compile.server-db-test
  (:require [rt.postgres.compile-test :as fixtures]
            [rt.postgres.compile.server-db :refer :all])
  (:use code.test))

(fixtures/ensure-fixtures!)

^{:refer rt.postgres.compile.server-db/infer-sync-spec :added "4.1"}
(fact "infer-sync-spec respects the manual and off sync modes"
  (select-keys (infer-sync-spec fixtures/+manual-sync-fn+)
               [:mode :mutating? :tables])
  => {:mode :manual
      :mutating? false
      :tables ["UserAccount" "UserProfile"]}

  (select-keys (infer-sync-spec fixtures/+read-fn+)
               [:mode :mutating? :tables])
  => {:mode :none
      :mutating? false
      :tables ["UserAccount"]})

^{:refer rt.postgres.compile.server-db/db-sync-merge :added "4.1"}
(fact "db-sync-merge leaves nil outputs and existing sync payloads unchanged"
  (db-sync-merge nil ["UserAccount"])
  => nil

  (db-sync-merge {:id "u1" :db/sync {"UserAccount" [{:id "u1"}]}}
                 ["UserAccount" "UserProfile"])
  => {:id "u1"
      :db/sync {"UserAccount" [{:id "u1"}]}})

^{:refer rt.postgres.compile.server-db/supabase-db-input :added "4.1"}
(fact "supabase-db-input prepares wrapper input for generation"
  (supabase-db-input
   {:wrapper-sym 'create-user-manual-sync
    :fn-def fixtures/+manual-sync-fn+
    :sync-spec {:sync-fn 'rt.postgres.compile.server-db/db-sync-merge
                :tables ["UserAccount" "UserProfile"]}})
  => {'wrapper-sym 'create-user-manual-sync
      'input []
      'inner-sym 'demo.core/create-user-manual
      'call-args []
      'sync-fn 'rt.postgres.compile.server-db/db-sync-merge
      'tables ["UserAccount" "UserProfile"]})

^{:refer rt.postgres.compile.server-db/target-entry :added "4.1"}
(fact "target-entry creates the emitted symbol for db targets"
  (select-keys (target-entry fixtures/+shape-fn+ :supabase-db)
               [:target :emitted-sym :fn-def])
  => {:target :supabase-db
      :emitted-sym 'create-user-sync
      :fn-def fixtures/+shape-fn+})

^{:refer rt.postgres.compile.server-db/emit-target :added "4.1"}
(fact "emit-target renders a db wrapper when sync is enabled"
  (clojure.string/includes?
   (emit-target fixtures/+manual-sync-fn+ :supabase-db)
   "create-user-manual-sync")
  => true)

^{:refer rt.postgres.compile.server-db/emit-targets :added "4.1"}
(fact "emit-targets renders the configured db targets"
  (keys (emit-targets fixtures/+manual-sync-fn+))
  => '(:supabase-db)

  (emit-targets fixtures/+manual-sync-fn+ [:supabase-db])
  => (contains {:supabase-db string?}))

^{:refer rt.postgres.compile.server-db/list-targets :added "4.1"}
(fact "list-targets exposes the supported db targets"
  (list-targets)
  => '(:supabase-db))


^{:refer rt.postgres.compile.server-db/collect-pg-ops :added "4.1"}
(fact "TODO")