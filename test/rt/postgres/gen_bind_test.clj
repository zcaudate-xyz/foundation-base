(ns rt.postgres.gen-bind-test
  (:require [rt.postgres :as pg]
            [rt.postgres.gen-bind :as gen]
            [rt.postgres.script.test.scratch-v1 :as scratch]
            [std.lang :as l]
            [xt.db.sample-data-test :as data]
            [xt.db.sample-user-test :as user])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.lang.base-lib :as k]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.sql-graph :as g]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-schema :as sch]
             [xt.db.base-scope :as scope]
             [xt.db.sample-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer rt.postgres.gen-bind/transform-to-str :added "4.0"}
(fact "transforms relevant forms to string"
  ^:hidden
  
  (gen/transform-to-str 'scratch/Task)
  => {"::" "sql/deftype", :schema "scratch", :name "Task"}

  (gen/transform-to-str :hello)
  => "hello"

  (gen/transform-to-str [1 2 3])
  => [1 2 3])

^{:refer rt.postgres.gen-bind/transform-query-or :added "4.0"}
(fact "transforms a setvec form"
  ^:hidden
  
  (gen/transform-query-or
   #{[:name "hello"
      :id "hello"
      [:or]
      :name "hello"
      :id "hello"]})
  => [{:name "hello", :id "hello"} {:name "hello", :id "hello"}])

^{:refer rt.postgres.gen-bind/transform-query-classify :added "4.0"}
(fact "transform function and quote representations"
  ^:hidden

  (gen/transform-query-classify
   ''("hello" "world"))
  => [["hello" "world"]]

  (gen/transform-query-classify
   '(jsonb-array-elements-text i-org))
  => '{"::" "sql/fn", :name "jsonb_array_elements_text", :args [i-org]}

  (gen/transform-query-classify
   '[:select * :from scratch/Task])
  => '{"::" "sql/select", :args [* :from scratch/Task]})

^{:refer rt.postgres.gen-bind/transform-query :added "4.0"
  :setup [(def +query-json+
            ["UserAccount"
             {"custom" [],
              "where" [],
              "links"
              [["profile"
                "reverse"
                ["UserProfile"
                 {"custom" [],
                  "where" [{"id" 1, "account" ["eq" ["UserAccount.id"]]}],
                  "links" [],
                  "data" ["first_name" "last_name"]}]]],
              "data" ["id" "nickname"]}])]}
(fact "generates the query interface"
  ^:hidden
  
  (gen/transform-query
   #{:id :nickname
     [:profile
      {:id 1}
      #{:first-name :last-name}]})
  => ["nickname" "id" ["profile" {"id" 1} ["last_name" "first_name"]]]
  
  (!.js
   (scope/get-tree
    sample/Schema
    "UserAccount"
    {}
    (@! (gen/transform-query
         #{:id :nickname
           [:profile
            {:id 1}
            #{:first-name :last-name}]}))
    {}))
  => +query-json+
  
  (!.lua
   (scope/get-tree
    sample/Schema
    "UserAccount"
    {}
    (@! (gen/transform-query
         #{:id :nickname
           [:profile
            {:id 1}
            #{:first-name :last-name}]}))
    {}))
  => ["UserAccount"
      {"custom" {},
       "where" {},
       "links"
       [["profile"
         "reverse"
         ["UserProfile"
          {"custom" {},
           "where" [{"id" 1, "account" ["eq" ["UserAccount.id"]]}],
           "links" {},
           "data" ["first_name" "last_name"]}]]],
       "data" ["id" "nickname"]}]
  
  (!.py
   (scope/get-tree
    sample/Schema
    "UserAccount"
    {}
    (@! (gen/transform-query
         #{:id :nickname
           [:profile
            {:id 1}
            #{:first-name :last-name}]}))
    {}))
  => +query-json+)

^{:refer rt.postgres.gen-bind/transform-schema :added "4.0"}
(fact "transforms the schema"
  ^:hidden
  
  (gen/transform-schema (:tree (:schema sample/+app+)))
  => map?)

^{:refer rt.postgres.gen-bind/bind-function :added "4.0"}
(fact "generates the type signatures for a pg function"
  ^:hidden

  (gen/bind-function scratch/ping)
  => {:input [], :return "text", :schema "scratch", :id "ping", :flags {}}  

  (gen/bind-function scratch/addf)
  => {:input [{:symbol "x", :type "numeric"}
              {:symbol "y", :type "numeric"}],
      :return "numeric",
      :schema "scratch",
      :id "addf",
      :flags {}}
  
  (gen/bind-function scratch/echo)
  => {:input [{:symbol "input", :type "jsonb"}],
      :return "jsonb", :schema "scratch", :id "echo", :flags {}})

^{:refer rt.postgres.gen-bind/bind-view-access :added "4.0"}
(fact "gets the view access"
  ^:hidden

  (gen/bind-view-access (:access (:static/view @user/organisation-all-as-member)))
  => {:symbol "xt.db.sample_user_test/organisation_access_is_member",
      :relation "reverse",
      :query
      {"clause" {"access" {"role" "member", "account" "{{<%>}}"}}},
      :roles {:organisation.member true}})

^{:refer rt.postgres.gen-bind/bind-view-guards :added "4.0"}
(fact "gets more guards"
  ^:hidden
  
  (gen/bind-view-guards (:guards (:static/view @user/user-account-by-organisation)))
  => [{:function
       {:input
        [{:symbol "i_account_id", :type "uuid"}
         {:symbol "i_organisation_id", :type "uuid"}],
        :return "jsonb",
        :schema "scratch-sample-db",
        :id "organisation_assert_is_member",
        :flags {}},
       :args ["{{<%>}}" "{{i_organisation_id}}"]}])

^{:refer rt.postgres.gen-bind/bind-view :added "4.0"}
(fact "generates the view interface"
  ^:hidden
  
  (gen/bind-view data/currency-all-fiat)
  => {:flags {:public true},
      :id "currency_all_fiat",
      :input [],
      :return "jsonb",
      :schema "scratch-sample-db",
      :view {:query {"type" "fiat"},
             :table "Currency",
             :tag "all_fiat",
             :type "select"}}
  
  
  (gen/bind-view data/currency-default)
  => {:input [{:symbol "i_currency_id", :type "citext"}],
      :return "jsonb",
      :schema "scratch-sample-db",
      :id "currency_default",
      :flags {:public true},
      :view
      {:table "Currency",
       :type "return",
       :tag "default",
       :query ["*/data"],}}
  
  
  (gen/bind-view user/organisation-view-membership)
  => {:input [{:symbol "i_organisation_id", :type "uuid"}],
      :return "jsonb",
      :schema "scratch-sample-db",
      :id "organisation_view_membership",
      :flags {},
      :view
      {:table "Organisation",
       :type "return",
       :tag "view_membership"
       :query
       ["*/data" ["access" ["*/data" ["account" ["nickname" "id"]]]]],}}
  
  (gen/bind-view user/user-account-by-organisation)
  => {:input [{:symbol "i_organisation_id", :type "uuid"}],
      :return "jsonb",
      :schema "scratch-sample-db",
      :id "user_account_by_organisation",
      :flags {},
      :view
      {:table "UserAccount",
       :type "select",
       :tag "by_organisation",
       :query
       {"organisation_accesses"
        {"organisation" "{{i_organisation_id}}"}},}})

^{:refer rt.postgres.gen-bind/bind-access :added "4.0"}
(fact "generates the access interface"
  ^:hidden

  (gen/bind-access data/access-city-in-country)
  => {:forward {:clause {"country" "{{<%>}}"}, :table "RegionCity"},
      :reverse {:clause {"region_cities" "{{<%>}}"}, :table "RegionCountry"}})

^{:refer rt.postgres.gen-bind/bind-table :added "4.0"}
(fact "gets the table interface"
  ^:hidden
  
  (gen/bind-table data/Currency)
  => {:schema "scratch-sample-db",
      :schema-primary {"type" "citext", "id" "id"},
      :public true,
      :schema-update false})

^{:refer rt.postgres.gen-bind/bind-app :added "4.0"}
(fact "gets the app interface given a name"
  ^:hidden
  
  (gen/bind-app (pg/app "xt.db.sample"))
  => {"RegionCity"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "text", "id" "id"},
       :public true,
       :schema-update false,
       :position 12},
      "UserProfile"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 1},
      "Asset"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 4},
      "Organisation"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 7},
      "OrganisationAccess"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public nil,
       :schema-update false,
       :position 8},
      "UserPrivilege"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 3},
      "RegionState"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "text", "id" "id"},
       :public true,
       :schema-update false,
       :position 11},
      "UserNotification"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 2},
      "UserAccount"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 0},
      "WalletAsset"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 6},
      "Wallet"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "uuid", "id" "id"},
       :public true,
       :schema-update false,
       :position 5},
      "Currency"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "citext", "id" "id"},
       :public true,
       :schema-update false,
       :position 9},
      "RegionCountry"
      {:schema "scratch-sample-db",
       :schema-primary {"type" "citext", "id" "id"},
       :public true,
       :schema-update false,
       :position 10}})

^{:refer rt.postgres.gen-bind/bind-schema :added "4.0"}
(fact "binds a schema"
  ^:hidden
  
  (gen/bind-schema (:schema (pg/app "xt.db.sample")))
  => map?)

^{:refer rt.postgres.gen-bind/list-view :added "4.0"}
(fact "lists all views in the schema"
  ^:hidden
  
  (gen/list-view 'xt.db.sample-user-test :select)
  => vector?

  (gen/list-view 'xt.db.sample-user-test :return)
  => '[[user-account-info xt.db.sample-user-test/user-account-info]
       [organisation-view-membership xt.db.sample-user-test/organisation-view-membership]
       [organisation-view-default xt.db.sample-user-test/organisation-view-default]])

^{:refer rt.postgres.gen-bind/list-api :added "4.0"}
(fact "lists all apis"
  ^:hidden
  
  (gen/list-api 'rt.postgres.script.test.scratch-v1)
  => '[[ping rt.postgres.script.test.scratch-v1/ping]
       [ping-ok rt.postgres.script.test.scratch-v1/ping-ok]
       [echo rt.postgres.script.test.scratch-v1/echo]])

^{:refer rt.postgres.gen-bind/list-debug :added "4.0"}
(fact  "lists all debug apis"
  ^:hidden

  (gen/list-debug 'rt.postgres.script.test.scratch-v1)
  => '[[as-array rt.postgres.script.test.scratch-v1/as-array]
       [as-upper rt.postgres.script.test.scratch-v1/as-upper]
       [addf rt.postgres.script.test.scratch-v1/addf]
       [subf rt.postgres.script.test.scratch-v1/subf]
       [mulf rt.postgres.script.test.scratch-v1/mulf]
       [divf rt.postgres.script.test.scratch-v1/divf]
       [insert-task rt.postgres.script.test.scratch-v1/insert-task]
       [insert-entry rt.postgres.script.test.scratch-v1/insert-entry]])

^{:refer rt.postgres.gen-bind/list-all :added "4.0"}
(fact "lists all function forms"
  ^:hidden
  
  (gen/list-all 'rt.postgres.script.test.scratch-v1)
  => '[[as-array rt.postgres.script.test.scratch-v1/as-array]
       [entry-all rt.postgres.script.test.scratch-v1/entry-all]
       [entry-by-name rt.postgres.script.test.scratch-v1/entry-by-name]
       [entry-default rt.postgres.script.test.scratch-v1/entry-default]
       [as-upper rt.postgres.script.test.scratch-v1/as-upper]
       [ping rt.postgres.script.test.scratch-v1/ping]
       [ping-ok rt.postgres.script.test.scratch-v1/ping-ok]
       [echo rt.postgres.script.test.scratch-v1/echo]
       [addf rt.postgres.script.test.scratch-v1/addf]
       [subf rt.postgres.script.test.scratch-v1/subf]
       [mulf rt.postgres.script.test.scratch-v1/mulf]
       [divf rt.postgres.script.test.scratch-v1/divf]
       [insert-task rt.postgres.script.test.scratch-v1/insert-task]
       [insert-entry rt.postgres.script.test.scratch-v1/insert-entry]])
