(ns xt.db.sql-view-access-test
  (:require [rt.postgres :as pg]
            [std.lang :as l]
            [std.string.prose :as prose]
            [xt.db.sample-data-test :as data]
            [xt.db.sample-user-test :as user])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-view :as v]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 (l/rt:scaffold :lua)
                 (l/rt:scaffold :python)
                 true)]
  :teardown [(l/rt:stop)]})


(def +select-by-org+
  (pg/bind-view user/user-account-by-organisation))

^{:refer xt.db.sql-view/view-select :adopt true :added "4.0"
  :setup [(def +select-member+
            (pg/bind-view user/organisation-all-as-member))
          (def +account-by-org+
            (pg/bind-view user/user-account-by-organisation))]}
(fact "provides a view select query"
  ^:hidden
  
  (!.js
   (v/query-select sample/Schema
                  (@! +select-member+)
                  ["hello"]
                  {}))
  => (prose/|
      "SELECT id FROM Organisation"
      "  WHERE id IN ("
      "    SELECT organisation_id FROM OrganisationAccess"
      "    WHERE role = 'member' AND account_id = 'hello'"
      "  )")
  
  (!.js
   (v/query-select sample/Schema
                  (@! +select-member+)
                  ["hello"]
                  {:access-id "<ACCOUNT-ID>"}))
  => (prose/|
      "SELECT id FROM Organisation"
      "  WHERE id IN ("
      "    SELECT organisation_id FROM OrganisationAccess"
      "    WHERE role = 'member' AND account_id = '<ACCOUNT-ID>'"
      "  )")

  (!.js
   (v/query-select sample/Schema
                  (@! +account-by-org+)
                  ["hello"]
                  {}))
  (prose/|
   "SELECT id FROM UserAccount"
   "  WHERE id IN ("
   "    SELECT account_id FROM OrganisationAccess"
   "    WHERE organisation_id = 'hello'"
   "  )"))



^{:refer xt.db.sql-view/view-return :adopt true :added "4.0"
  :setup [(def +return-member+
            (pg/bind-view user/organisation-view-membership))]}
(fact "provides a view select query"
  ^:hidden

  (!.js
   (v/query-return sample/Schema
                  (@! +return-member+)
                  "hello"
                  []
                  {}))
  => (prose/|
      "SELECT id, name, title, description, tags, (SELECT id, role, (SELECT id, nickname FROM UserAccount"
      "  WHERE id = OrganisationAccess.account_id) AS account FROM OrganisationAccess"
      "  WHERE organisation_id = Organisation.id) AS access FROM Organisation"
      "  WHERE id = 'hello'")
  
  (!.js
   (v/query-return sample/Schema
                  (@! +return-member+)
                  "hello"
                  []
                  {:access-id "<ACCOUNT-ID>"}))
  (prose/|
   "SELECT id, name, title, description, (SELECT id, role, (SELECT id, nickname FROM UserAccount"
   "  WHERE id = OrganisationAccess.account_id) AS account FROM OrganisationAccess"
   "  WHERE organisation_id = Organisation.id) AS access FROM Organisation"
   "  WHERE id = 'hello' AND id IN ("
   "    SELECT organisation_id FROM OrganisationAccess"
   "    WHERE role = 'member' AND account_id = '<ACCOUNT-ID>'"
   "  )"))


^{:refer xt.db.sql-view/view-combined :adopt true :added "4.0"
  :setup [(def +select-member+
            (pg/bind-view user/organisation-all-as-member))
          (def +return-member+
            (pg/bind-view user/organisation-view-membership))]}
(fact "provides a view select query"
  ^:hidden

  (!.js
   (v/query-combined sample/Schema
                     (@! (pg/bind-view user/organisation-by-name))
                     ["hello"]
                     (@! +return-member+)
                     []
                     nil
                     {:access-id "<ACCESS-ID>"}))
  => (prose/|
      "SELECT id, name, title, description, tags, (SELECT id, role, (SELECT id, nickname FROM UserAccount"
      "  WHERE id = OrganisationAccess.account_id) AS account FROM OrganisationAccess"
      "  WHERE organisation_id = Organisation.id) AS access FROM Organisation"
      "  WHERE name = 'hello' AND id IN ("
      "    SELECT organisation_id FROM OrganisationAccess"
      "    WHERE role = 'member' AND account_id = '<ACCESS-ID>'"
      "  )")
  
  
  (!.js
   (v/query-combined sample/Schema
                   (@! +select-member+)
                   ["hello"]
                   (@! +return-member+)
                   []
                   nil
                   {}))
  => (prose/|
   "SELECT id, name, title, description, tags, (SELECT id, role, (SELECT id, nickname FROM UserAccount"
   "  WHERE id = OrganisationAccess.account_id) AS account FROM OrganisationAccess"
   "  WHERE organisation_id = Organisation.id) AS access FROM Organisation"
   "  WHERE id IN ("
   "    SELECT organisation_id FROM OrganisationAccess"
   "    WHERE role = 'member' AND account_id = 'hello'"
   "  )"))
