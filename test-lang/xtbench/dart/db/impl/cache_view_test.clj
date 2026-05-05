(ns xtbench.dart.db.impl.cache-view-test
  (:require [hara.rt.postgres :as pg]
            [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.db.runtime.cache-view :as v]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-lib :as k]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]]})

(fact:global
 {:setup [(l/rt:restart)
          (def +select+
            {:flags {:public true}
             :id "currency_all_fiat"
             :input []
             :return "jsonb"
             :schema "scratch-sample-db"
             :view {:query {"type" "fiat"}
                    :table "Currency"
                    :tag "all_fiat"
                    :type "select"}})
          (def +return+
            {:flags {:public true}
             :id "currency_default"
             :input [{:symbol "i_currency_id" :type "citext"}]
             :return "jsonb"
             :schema "scratch-sample-db"
             :view {:query ["*/data"]
                    :table "Currency"
                    :tag "default"
                    :type "return"}})
          (def +user-account-info+
            {:flags {}
             :id "user_account_info"
             :input [{:symbol "i_account_id" :type "uuid"}]
             :return "jsonb"
             :schema "scratch-sample-db"
             :view {:query [["profile" ["*/standard"]] "nickname" "id"]
                    :table "UserAccount"
                    :tag "info"
                    :type "return"}})
          (def +user-account-by-organisation+
            {:flags {}
             :id "user_account_by_organisation"
             :input [{:symbol "i_organisation_id" :type "uuid"}]
             :return "jsonb"
             :schema "scratch-sample-db"
             :view {:query {"organisation_accesses"
                            {"organisation" "{{i_organisation_id}}"}}
                    :table "UserAccount"
                    :tag "by_organisation"
                    :type "select"}})]
  :teardown [(l/rt:stop)]})

(def +app+ (pg/app "xt.db.helpers.sample"))
(def +schema+ (pg/bind-schema (:schema +app+)))

^{:refer xt.db.runtime.cache-view/tree-base :added "4.0"}
(fact "creates a tree base"

  (!.dt
   (v/tree-base (@! +schema+)
                "Currency"
                [{:id "USD"}
                 {:id "AUD"}]
                ["*/data"]
                []))
  => ["Currency"
      {"id" "USD"}
      {"id" "AUD"}
      ["*/data"]])

^{:refer xt.db.runtime.cache-view/tree-select :added "4.0"}
(fact "creates a select tree"

  (!.dt
   (v/tree-select (@! +schema+)
                  (@! +select+)))
  => ["Currency" {"type" "fiat"} ["id"]])

^{:refer xt.db.runtime.cache-view/tree-return :added "4.0"}
(fact "creates a return tree"

  (!.dt
   (v/tree-return (@! +schema+)
                  (@! +return+)
                  {}))
  => ["Currency" ["*/data"]]

  (!.dt
   (v/tree-return (@! +schema+)
                  (@! +user-account-info+)
                  {}))
  => ["UserAccount" [["profile" ["*/standard"]]
                     "nickname"
                     "id"]])

^{:refer xt.db.runtime.cache-view/tree-combined :added "4.0"}
(fact "creates a combined tree"

  (!.dt
   (v/tree-combined (@! +schema+)
                    (@! +select+)
                    (@! +return+)
                    []))
  => ["Currency" {"type" "fiat"} ["*/data"]]

  (!.dt
   (v/tree-combined (@! +schema+)
                    (@! +user-account-by-organisation+)
                    (@! +user-account-info+)
                    []))
  => ["UserAccount"
      {"organisation_accesses"
       {"organisation" "{{i_organisation_id}}"}}
      [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-select :added "4.0"}
(fact "tree for the query-select"

  (!.dt
   (v/query-select (@! +schema+)
                   (@! +user-account-by-organisation+)
                   ["ORG-1"]))
  => ["UserAccount" {"organisation_accesses"
                     {"organisation" "ORG-1"}}
      ["id"]])

^{:refer xt.db.runtime.cache-view/query-return :added "4.0"}
(fact "tree for the query-return"

  (!.dt
   (v/query-return (@! +schema+)
                   (@! +user-account-info+)
                   "USER-0"
                   []))
  => ["UserAccount" {"id" "USER-0"} [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-return-bulk :added "4.0"}
(fact "tree for query-return"

  (!.dt
   (v/query-return-bulk
    (@! +schema+)
    (@! +user-account-info+)
    ["USER-0"]
    []))
  => ["UserAccount" {"id" ["in" [["USER-0"]]]} [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-combined :added "4.0"}
(fact "tree for query combined"

  (!.dt
   (v/query-combined
    (@! +schema+)
    (@! +user-account-by-organisation+)
    ["ORG-1"]
    (@! +user-account-info+)
    []
    []))
  => ["UserAccount"
      {"organisation_accesses" {"organisation" "ORG-1"}}
      [["profile" ["*/standard"]] "nickname" "id"]])
