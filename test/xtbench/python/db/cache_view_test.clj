(ns
 xtbench.python.db.cache-view-test
 (:require
  [rt.postgres :as pg]
  [std.lang :as l]
  [xt.db.sample-data-test :as data]
  [xt.db.sample-user-test :as user])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.db.cache-view :as v]
   [xt.db.sql-util :as ut]
   [xt.db.sql-raw :as raw]
   [xt.lang.common-lib :as k]
   [xt.db.base-schema :as sch]
   [xt.db.base-scope :as scope]
   [xt.db.sample-test :as sample]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.db.cache-view/tree-base, :added "4.0"}
(fact
 "creates a tree base"
 ^{:hidden true}
 (!.py
  (v/tree-base
   sample/Schema
   "Currency"
   [{:id "USD"} {:id "AUD"}]
   ["*/data"]))
 =>
 ["Currency" {"id" "USD"} {"id" "AUD"} ["*/data"]])

^{:refer xt.db.cache-view/tree-select,
  :added "4.0",
  :setup [(def +select+ (pg/bind-view data/currency-all-fiat))]}
(fact
 "creates a select tree"
 ^{:hidden true}
 (!.py (v/tree-select sample/Schema (@! +select+) {}))
 =>
 ["Currency" {"type" "fiat"} ["id"]])

^{:refer xt.db.cache-view/tree-return,
  :added "4.0",
  :setup [(def +return+ (pg/bind-view data/currency-default))]}
(fact
 "creates a return tree"
 ^{:hidden true}
 (!.py (v/tree-return sample/Schema (@! +return+) {} {} {}))
 =>
 ["Currency" ["*/data"]]
 (!.py
  (v/tree-return
   sample/Schema
   (@! (pg/bind-view user/user-account-info))
   {}
   {}
   {}))
 =>
 ["UserAccount" [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.cache-view/tree-combined, :added "4.0"}
(fact
 "creates a combined tree"
 ^{:hidden true}
 (!.py
  (v/tree-combined sample/Schema (@! +select+) (@! +return+) {} {}))
 =>
 ["Currency" {"type" "fiat"} ["*/data"]]
 (!.py
  (v/tree-combined
   sample/Schema
   (@! (pg/bind-view user/user-account-by-organisation))
   (@! (pg/bind-view user/user-account-info))
   {}
   {}))
 =>
 ["UserAccount"
  {"organisation_accesses" {"organisation" "{{i_organisation_id}}"}}
  [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.cache-view/query-fill-input, :added "4.0"}
(fact "fills the input for args")

^{:refer xt.db.cache-view/query-select, :added "4.0"}
(fact
 "tree for the query-select"
 ^{:hidden true}
 (!.py
  (v/query-select
   sample/Schema
   (@! (pg/bind-view user/user-account-by-organisation))
   ["ORG-1"]))
 =>
 ["UserAccount"
  {"organisation_accesses" {"organisation" "ORG-1"}}
  ["id"]])

^{:refer xt.db.cache-view/query-return, :added "4.0"}
(fact
 "tree for the query-return"
 ^{:hidden true}
 (!.py
  (v/query-return
   sample/Schema
   (@! (pg/bind-view user/user-account-info))
   "USER-0"))
 =>
 ["UserAccount"
  {"id" "USER-0"}
  [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.cache-view/query-return-bulk, :added "4.0"}
(fact
 "tree for query-return"
 ^{:hidden true}
 (!.py
  (v/query-return-bulk
   sample/Schema
   (@! (pg/bind-view user/user-account-info))
   ["USER-0"]))
 =>
 ["UserAccount"
  {"id" ["in" [["USER-0"]]]}
  [["profile" ["*/standard"]] "nickname" "id"]])

^{:refer xt.db.cache-view/query-combined, :added "4.0"}
(fact
 "tree for query combined"
 ^{:hidden true}
 (!.py
  (v/query-combined
   sample/Schema
   (@! (pg/bind-view user/user-account-by-organisation))
   ["ORG-1"]
   (@! (pg/bind-view user/user-account-info))
   []))
 =>
 ["UserAccount"
  {"organisation_accesses" {"organisation" "ORG-1"}}
  [["profile" ["*/standard"]] "nickname" "id"]])
