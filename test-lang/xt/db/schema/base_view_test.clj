(ns xt.db.schema.base-view-test
  (:require [hara.rt.postgres :as pg]
            [hara.lang :as l]
            [xt.db.helpers.seed-system-test :as data]
            [xt.db.helpers.seed-user-test :as user])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.schema.base-view :as v]
             [xt.db.schema.base-util :as ut]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.schema.base-view :as v]
             [xt.db.schema.base-util :as ut]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.db.schema.base-view :as v]
             [xt.db.schema.base-util :as ut]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +views+
  (mapv (comp pg/bind-view deref resolve second)
        (concat (pg/list-view 'xt.db.helpers.seed-system-test :select)
                (pg/list-view 'xt.db.helpers.seed-system-test :return)
                (pg/list-view 'xt.db.helpers.seed-user-test :select)
                (pg/list-view 'xt.db.helpers.seed-user-test :return))))

^{:refer xt.db.schema.base-view/all-overview :added "4.0"
  :setup [(def +all-overview-check+
            (just-in 
             {"RegionCity"
              {"return" (just ["with_access" "default" "info"] :in-any-order),
               "select" (just ["by_country" "by_state"] :in-any-order)},
              "Organisation"
              {"return" (just ["view_membership" "view_default"] :in-any-order),
               "select" (just ["by_name" "all_as_owner" "all_as_admin" "all_as_member"]  :in-any-order)},
              "RegionState"
              {"return" (just ["default" "info"] :in-any-order),
               "select" ["by_country"]},
              "UserAccount"
              {"return" ["info"], "select" ["by_organisation"]},
              "Currency"
              {"return" (just ["default" "info"]  :in-any-order),
               "select" (just ["all" "all_fiat" "all_crypto" "by_type" "by_country"]  :in-any-order)},
              "RegionCountry"
              {"return" (just ["with_access" "default" "info"] :in-any-order),
               "select" (just ["by_name" "all"] :in-any-order)}}))]}
(fact "gets an overview of the views"

  (!.js
    (v/all-overview (ut/collect-views (@! +views+))))
  => +all-overview-check+

  (!.lua
    (v/all-overview (ut/collect-views (@! +views+))))
  => +all-overview-check+

  (!.py
    (v/all-overview (ut/collect-views (@! +views+))))
  => +all-overview-check+)

^{:refer xt.db.schema.base-view/all-keys :added "4.0"}
(fact "gets all table keys for a view"

  (!.js
    (v/all-keys (ut/collect-views (@! +views+))
                "Currency"
                "select"))
  => (just ["all" "all_fiat" "all_crypto" "by_type" "by_country"] :in-any-order)

  (!.lua
    (v/all-keys (ut/collect-views (@! +views+))
                "Currency"
                "select"))
  => (just ["all" "all_fiat" "all_crypto" "by_type" "by_country"] :in-any-order)

  (!.py
    (v/all-keys (ut/collect-views (@! +views+))
                "Currency"
                "select"))
  => (just ["all" "all_fiat" "all_crypto" "by_type" "by_country"] :in-any-order))

^{:refer xt.db.schema.base-view/all-methods :added "4.0"
  :setup [(def +all-methods-check+
            (just 
             [["Currency" "return" "default"]
              ["Currency" "return" "info"]
              ["Currency" "select" "all"]
              ["Currency" "select" "all_crypto"]
              ["Currency" "select" "all_fiat"]
              ["Currency" "select" "by_country"]
              ["Currency" "select" "by_type"]
              ["Organisation" "return" "view_default"]
              ["Organisation" "return" "view_membership"]
              ["Organisation" "select" "all_as_admin"]
              ["Organisation" "select" "all_as_member"]
              ["Organisation" "select" "all_as_owner"]
              ["Organisation" "select" "by_name"]
              ["RegionCity" "return" "default"]
              ["RegionCity" "return" "info"]
              ["RegionCity" "return" "with_access"]
              ["RegionCity" "select" "by_country"]
              ["RegionCity" "select" "by_state"]
              ["RegionCountry" "return" "default"]
              ["RegionCountry" "return" "info"]
              ["RegionCountry" "return" "with_access"]
              ["RegionCountry" "select" "all"]
              ["RegionCountry" "select" "by_name"]
              ["RegionState" "return" "default"]
              ["RegionState" "return" "info"]
              ["RegionState" "select" "by_country"]
              ["UserAccount" "return" "info"]
              ["UserAccount" "select" "by_organisation"]]
             :in-any-order))]}
(fact "gets all methods for views"

  (!.js
    (v/all-methods (ut/collect-views (@! +views+))))
  => +all-methods-check+

  (!.lua
    (v/all-methods (ut/collect-views (@! +views+))))
  => +all-methods-check+

  (!.py
    (v/all-methods (ut/collect-views (@! +views+))))
  => +all-methods-check+)

(comment
  (s/run ['xt.db.schema.base-view])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.schema.base-view] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.schema.base-view {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.schema.base-view {:lang [:lua :python] :write true}))
