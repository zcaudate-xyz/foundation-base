(ns rt.postgres.compile.server-api-test
  (:require [rt.postgres.compile-test :as fixtures]
            [rt.postgres.compile.server-api :refer :all]
            [rt.postgres.compile.server-db :as server-db])
  (:use code.test))

(fixtures/ensure-fixtures!)

^{:refer rt.postgres.compile.server-api/xtalk-contract-input :added "4.1"}
(fact "xtalk-contract-input normalizes the template payload"
  (xtalk-contract-input
   {:contract-sym 'create-user-contract
    :wrapper-sym 'create-user-sync
    :fn-def fixtures/+manual-sync-fn+
    :sync-spec {:mode :manual
                :tables ["UserAccount" "UserProfile"]}})
  => {'contract-sym 'create-user-contract
      'id "create_user_manual"
      'sync-mode :manual
      'tables ["UserAccount" "UserProfile"]
      'handler-sym 'create-user-sync})

^{:refer rt.postgres.compile.server-api/target-entry :added "4.1"}
(fact "target-entry creates the emitted symbol for api targets"
  (select-keys (target-entry fixtures/+shape-fn+ :xtalk-contracts)
               [:target :emitted-sym :fn-def])
  => {:target :xtalk-contracts
      :emitted-sym 'create-user-contract
      :fn-def fixtures/+shape-fn+})

^{:refer rt.postgres.compile.server-api/emit-target :added "4.1"}
(fact "emit-target renders an xtalk contract when sync is enabled"
  (clojure.string/includes?
   (emit-target fixtures/+manual-sync-fn+ :xtalk-contracts)
   "create-user-manual-contract")
  => true)

^{:refer rt.postgres.compile.server-api/emit-targets :added "4.1"}
(fact "emit-targets renders the configured api targets"
  (keys (emit-targets fixtures/+manual-sync-fn+))
  => '(:xtalk-contracts)

  (emit-targets fixtures/+manual-sync-fn+ [:xtalk-contracts])
  => (contains {:xtalk-contracts string?}))

^{:refer rt.postgres.compile.server-api/list-targets :added "4.1"}
(fact "list-targets exposes the supported api targets"
  (list-targets)
  => '(:xtalk-contracts))
