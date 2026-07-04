(ns postgres.gen.bind-macro-test
  (:use code.test)
  (:require [postgres.core :as pg]
            [postgres.gen.bind-macro :as gen]
            [postgres.sample.scratch-v1 :as scratch]
            [hara.lang :as l]
            [hara.lang.book :as book]
            [xt.db.helpers.seed-system-test :as data]
            [xt.db.helpers.seed-user-test :as user]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.sql-graph :as g]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-lib :as k]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.text.sql-graph :as g]
             [xt.lang.common-lib :as k]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.db.text.sql-graph :as g]
             [xt.lang.common-lib :as k]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.base-schema :as sch]
             [xt.db.text.base-scope :as scope]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (l/rt:scaffold :lua)
             (l/rt:scaffold :python)]
  :teardown [(l/rt:stop)]})

^{:refer postgres.gen.bind-macro/to-lookup :added "4.1"}
(fact "creates a lookup map from array"
  (gen/to-lookup [:a :b :c]) => {:a true :b true :c true}

  (gen/to-lookup []) => {}

  (gen/to-lookup ["x" "y"]) => {"x" true "y" true})

^{:refer postgres.gen.bind-macro/plain-symbol? :added "4.1"}
(fact "checks if form is a plain symbol without namespace"
  (gen/plain-symbol? 'foo) => true

  (gen/plain-symbol? 'clojure.core/map) => false

  (gen/plain-symbol? :keyword) => false

  (gen/plain-symbol? "string") => false

  (gen/plain-symbol? 123) => false)

^{:refer postgres.gen.bind-macro/bind-entry :added "4.1"}
(fact "gets a materialized postgres code entry when available"

  (select-keys (gen/bind-entry scratch/ping)
               [:lang :section :module :id :op-key])
  => {:lang :postgres
      :section :code
      :module 'postgres.sample.scratch-v1
      :id 'ping
      :op-key :defn}

  (let [entry (book/book-entry {:lang :js
                                :section :code
                                :module 'test.ns
                                :id 'foo
                                :namespace 'test.ns
                                :form '(defn foo [])
                                :declared 1
                                :line 1})]
    (select-keys (gen/bind-entry entry)
                 [:lang :section :module :id]))
  => {:lang :js
      :section :code
      :module 'test.ns
      :id 'foo}

  (select-keys (gen/bind-entry (book/book-entry {:lang :postgres
                                                 :section :code
                                                 :module 'test.ns
                                                 :namespace 'test.ns
                                                 :form '(defn foo [])
                                                 :declared 1
                                                 :line 1}))
               [:lang :section :module :id])
  => {:lang :postgres
      :section :code
      :module 'test.ns
      :id nil}

  (select-keys (gen/bind-entry (book/book-entry {:lang :postgres
                                                 :section :code
                                                 :id 'foo
                                                 :namespace 'test.ns
                                                 :form '(defn foo [])
                                                 :declared 1
                                                 :line 1}))
               [:lang :section :module :id])
  => {:lang :postgres
      :section :code
      :module nil
      :id 'foo})

^{:refer postgres.gen.bind-macro/transform-to-str :added "4.0"}
(fact "transforms relevant forms to string"

  (gen/transform-to-str 'scratch/Task)
  => {"::" "sql/deftype", :schema "scratch", :name "Task"}

  (gen/transform-to-str :hello)
  => "hello"

  (gen/transform-to-str [1 2 3])
  => [1 2 3])

^{:refer postgres.gen.bind-macro/transform-query-or :added "4.0"}
(fact "transforms a setvec form"

  (gen/transform-query-or
   #{[:name "hello"
      :id "hello"
      [:or]
      :name "hello"
      :id "hello"]})
  => [{:name "hello", :id "hello"} {:name "hello", :id "hello"}])

^{:refer postgres.gen.bind-macro/transform-query-classify :added "4.0"}
(fact "transform function and quote representations"

  (gen/transform-query-classify
   ''("hello" "world"))
  => [["hello" "world"]]

  (gen/transform-query-classify
   '(jsonb-array-elements-text i-org))
  => '{"::" "sql/fn", :name "jsonb_array_elements_text", :args [i-org]}

  (gen/transform-query-classify
   '[:select * :from scratch/Task])
  => '{"::" "sql/select", :args [* :from scratch/Task]})

^{:refer postgres.gen.bind-macro/transform-query :added "4.0"
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
      {"custom" [],
       "data" ["id" "nickname"],
       "links"
       [["profile"
         "reverse"
         ["UserProfile"
          {"custom" [],
           "data" ["first_name" "last_name"],
           "links" [],
           "where" [{"account" ["eq" ["UserAccount.id"]], "id" 1}]}]]],
       "where" []}]

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

^{:refer postgres.gen.bind-macro/transform-schema :added "4.0"}
(fact "transforms the schema"

  (gen/transform-schema (:tree (:schema sample/+app+)))
  => map?)

^{:refer postgres.gen.bind-macro/bind-function :added "4.0"}
(fact "generates the type signatures for a pg function"

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

^{:refer postgres.gen.bind-macro/bind-view-guards :added "4.0"}
(fact "gets more guards"

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

^{:refer postgres.gen.bind-macro/bind-view :added "4.0"}
(fact "generates the view interface"

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
       :query ["*/data"]}}


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
       ["*/data" ["access" ["*/data" ["account" ["nickname" "id"]]]]]}}

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
        {"organisation" "{{i_organisation_id}}"}}}})

^{:refer postgres.gen.bind-macro/bind-table :added "4.0"}
(fact "gets the table interface"

  (gen/bind-table data/Currency)
  => {:schema "scratch-sample-db",
      :schema-primary {"type" "citext", "id" "id"},
      :public true,
      :schema-update false})

^{:refer postgres.gen.bind-macro/bind-app :added "4.0"}
(fact "gets the app interface given a name"

  (gen/bind-app (pg/app "test-db-helpers"))
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

^{:refer postgres.gen.bind-macro/bind-schema :added "4.0"}
(fact "binds a schema"

  (gen/bind-schema (:schema (pg/app "test-db-helpers")))
  => map?)

^{:refer postgres.gen.bind-macro/list-view :added "4.0"}
(fact "lists all views in the schema"

  (gen/list-view 'xt.db.helpers.seed-user-test :select)
  => vector?

  (gen/list-view 'xt.db.helpers.seed-user-test :return)
  => '[[user-account-info xt.db.helpers.seed-user-test/user-account-info]
       [organisation-view-membership xt.db.helpers.seed-user-test/organisation-view-membership]
       [organisation-view-default xt.db.helpers.seed-user-test/organisation-view-default]])

^{:refer postgres.gen.bind-macro/list-api :added "4.0"}
(fact "lists all apis"

  (gen/list-api 'postgres.sample.scratch-v1)
  => '[[ping postgres.sample.scratch-v1/ping]
       [ping-ok postgres.sample.scratch-v1/ping-ok]
       [echo postgres.sample.scratch-v1/echo]])

^{:refer postgres.gen.bind-macro/list-debug :added "4.0"}
(fact  "lists all debug apis"

  (gen/list-debug 'postgres.sample.scratch-v1)
  => '[[as-array postgres.sample.scratch-v1/as-array]
       [as-upper postgres.sample.scratch-v1/as-upper]
       [addf postgres.sample.scratch-v1/addf]
       [subf postgres.sample.scratch-v1/subf]
       [mulf postgres.sample.scratch-v1/mulf]
       [divf postgres.sample.scratch-v1/divf]
       [insert-task postgres.sample.scratch-v1/insert-task]
       [insert-entry postgres.sample.scratch-v1/insert-entry]])

^{:refer postgres.gen.bind-macro/list-all :added "4.0"}
(fact "lists all function forms"

  (gen/list-all 'postgres.sample.scratch-v1)
  => '[[as-array postgres.sample.scratch-v1/as-array]
       [entry-all postgres.sample.scratch-v1/entry-all]
       [entry-by-name postgres.sample.scratch-v1/entry-by-name]
       [entry-default postgres.sample.scratch-v1/entry-default]
       [as-upper postgres.sample.scratch-v1/as-upper]
       [ping postgres.sample.scratch-v1/ping]
       [ping-ok postgres.sample.scratch-v1/ping-ok]
       [echo postgres.sample.scratch-v1/echo]
       [addf postgres.sample.scratch-v1/addf]
       [subf postgres.sample.scratch-v1/subf]
       [mulf postgres.sample.scratch-v1/mulf]
       [divf postgres.sample.scratch-v1/divf]
       [insert-task postgres.sample.scratch-v1/insert-task]
       [insert-entry postgres.sample.scratch-v1/insert-entry]])
