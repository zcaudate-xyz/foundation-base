(ns xt.db.runtime.cache-view-test
  (:require [hara.lang :as l]
            [postgres.gen.bind-macro :as gen]
            [xt.db.helpers.seed-system-test :as data]
            [xt.db.helpers.seed-user-test :as user])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime.cache-view :as v]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-lib :as k]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.runtime.cache-view :as v]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-lib :as k]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.cache-view/tree-base :added "4.0"}
(fact "creates a tree base"

  (!.js
    (v/tree-base sample/Schema
                 "Currency"
                 [{:id "USD"}
                  {:id "AUD"}]
                 ["*/data"]
                 []))
  => ["Currency"
      {"id" "USD"}
      {"id" "AUD"}
      ["*/data"]]

  (!.py
    (v/tree-base sample/Schema
                 "Currency"
                 [{:id "USD"}
                  {:id "AUD"}]
                 ["*/data"]
                 []))
  => ["Currency"
      {"id" "USD"}
      {"id" "AUD"}
      ["*/data"]])

^{:refer xt.db.runtime.cache-view/tree-select :added "4.0"
  :setup [(def +select+
            (gen/bind-view data/currency-all-fiat))]}
(fact "creates a select tree"

  (!.js
    (v/tree-select sample/Schema
                   (@! +select+)))
  => ["Currency" {"type" "fiat"} ["id"]]

  (!.py
    (v/tree-select sample/Schema
                   (@! +select+)))
  => ["Currency" {"type" "fiat"} ["id"]])

^{:refer xt.db.runtime.cache-view/tree-return :added "4.0"
  :setup [(def +return+
            (gen/bind-view data/currency-default))]}
(fact "creates a return tree"

  (!.js
    (v/tree-return sample/Schema
                   (@! +return+)
                   {}))
  => ["Currency" ["*/data"]]

  (!.js
    (v/tree-return sample/Schema
                   (@! (gen/bind-view user/user-account-info))
                   {}))
  => ["UserAccount" [["profile" ["*/standard"]]
                     "nickname"
                     "id"]]

  (!.py
    (v/tree-return sample/Schema
                   (@! +return+)
                   {}))
  => ["Currency" ["*/data"]]

  (!.py
    (v/tree-return sample/Schema
                   (@! (gen/bind-view user/user-account-info))
                   {}))
  => ["UserAccount" [["profile" ["*/standard"]]
                     "nickname"
                     "id"]])

^{:refer xt.db.runtime.cache-view/tree-combined :added "4.0"}
(fact "creates a combined tree"

  (!.js
    (v/tree-combined sample/Schema
                     (@! +select+)
                     (@! +return+)
                     []))
  => ["Currency" {"type" "fiat"} ["*/data"]]

  (!.js
    (v/tree-combined sample/Schema
                     (@! (gen/bind-view user/user-account-by-organisation))
                     (@! (gen/bind-view user/user-account-info))
                     []))
  => ["UserAccount"
      {"organisation_accesses"
       {"organisation" "{{i_organisation_id}}"}}
      [["profile" ["*/standard"]] "nickname" "id"]]

  (!.py
    (v/tree-combined sample/Schema
                     (@! +select+)
                     (@! +return+)
                     []))
  => ["Currency" {"type" "fiat"} ["*/data"]]

  (!.py
    (v/tree-combined sample/Schema
                     (@! (gen/bind-view user/user-account-by-organisation))
                     (@! (gen/bind-view user/user-account-info))
                     []))
  => ["UserAccount"
      {"organisation_accesses"
       {"organisation" "{{i_organisation_id}}"}}
      [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-fill-input :added "4.0"}
(fact "fills the input for args")

^{:refer xt.db.runtime.cache-view/query-select :added "4.0"}
(fact "tree for the query-select"

  (!.js
    (v/query-select sample/Schema
                    (@! (gen/bind-view user/user-account-by-organisation))
                    ["ORG-1"]))
  => ["UserAccount" {"organisation_accesses"
                     {"organisation" "ORG-1"}}
      ["id"]]

  (!.py
    (v/query-select sample/Schema
                    (@! (gen/bind-view user/user-account-by-organisation))
                    ["ORG-1"]))
  => ["UserAccount" {"organisation_accesses"
                     {"organisation" "ORG-1"}}
      ["id"]])

^{:refer xt.db.runtime.cache-view/query-return :added "4.0"}
(fact "tree for the query-return"

  (!.js
    (v/query-return sample/Schema
                    (@! (gen/bind-view user/user-account-info))
                    "USER-0"
                    []))
  => ["UserAccount" {"id" "USER-0"} [["profile" ["*/standard"]] "nickname" "id"]]

  (!.py
    (v/query-return sample/Schema
                    (@! (gen/bind-view user/user-account-info))
                    "USER-0"
                    []))
  => ["UserAccount" {"id" "USER-0"} [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-return-bulk :added "4.0"}
(fact "tree for query-return"

  (!.js
    (v/query-return-bulk
     sample/Schema
     (@! (gen/bind-view user/user-account-info))
     ["USER-0"]
     []))
  => ["UserAccount" {"id" ["in" [["USER-0"]]]} [["profile" ["*/standard"]] "nickname" "id"]]

  (!.py
    (v/query-return-bulk
     sample/Schema
     (@! (gen/bind-view user/user-account-info))
     ["USER-0"]
     []))
  => ["UserAccount" {"id" ["in" [["USER-0"]]]} [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.runtime.cache-view/query-combined :added "4.0"}
(fact "tree for query combined"

  (!.js
    (v/query-combined
     sample/Schema
     (@! (gen/bind-view user/user-account-by-organisation))
     ["ORG-1"]
     (@! (gen/bind-view user/user-account-info))
     []
     []))
  => ["UserAccount"
      {"organisation_accesses" {"organisation" "ORG-1"}}
      [["profile" ["*/standard"]] "nickname" "id"]]

  (!.py
    (v/query-combined
     sample/Schema
     (@! (gen/bind-view user/user-account-by-organisation))
     ["ORG-1"]
     (@! (gen/bind-view user/user-account-info))
     []
     []))
  => ["UserAccount"
      {"organisation_accesses" {"organisation" "ORG-1"}}
      [["profile" ["*/standard"]] "nickname" "id"]])
