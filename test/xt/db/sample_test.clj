(ns xt.db.sample-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.db.sample-user-test :as user]
            [xt.db.sample-data-test :as data]
            [rt.postgres :as pg]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def +currency+
  [{:description "Default Currency for Statstrade",
    :symbol "Δ",
    :name "Stat Coin",
    :type "digital",
    :time-updated 1631909757019247,
    :time-created 1631909757019247,
    :native "Δ",
    :plural "Stat coins",
    :decimal 0,
    :id "STATS"}
   {:description "Default Currency for the Stellar Blockchain",
    :symbol "XLM",
    :name "Stellar Coin",
    :type "crypto",
    :time-updated 1631909757019247,
    :time-created 1631909757019247,
    :native nil,
    :plural "Stellar coins",
    :decimal -1,
    :id "XLM"}
   {:description
    "Default Currency for the Stellar TestNet Blockchain",
    :symbol "XLM.T",
    :name "Stellar TestNet Coin",
    :type "crypto",
    :time-updated 1631909757019247,
    :time-created 1631909757019247,
    :native nil,
    :plural "Stellar TestNet coins",
    :decimal -1,
    :id "XLM.T"}
   {:description "Default Current for the United States of America",
    :symbol "USD",
    :name "US Dollar",
    :type "fiat",
    :time-updated 1631909757019247,
    :time-created 1631909757019247,
    :native nil,
    :plural "US Dollars",
    :decimal 2,
    :id "USD"}])

(def +xlm+
  [{:description "Default Currency for the Stellar Blockchain",
    :symbol "XLM",
    :name "Stellar Coin",
    :type "crypto",
    :time-updated 1631909757019247,
    :time-created 1631909757019247,
    :native nil,
    :plural "Stellar coins",
    :decimal -1,
    :id "XLM"}])

(def.xt RootUser
  {:is-super true,
   :is-suspended false,
   :is-official false,
   :nickname "root",
   :is-verified true,
   :password-updated 1630408723423619,
   :time-updated 1630408722786926,
   :time-created 1630408722786926,
   :funding nil,
   :is-active true,
   :id "00000000-0000-0000-0000-000000000000",
   :emails
   [{:value "root@statstrade.io",
     :is-verified true,
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :is-active true,
     :id "db0898be-4630-43f5-96f3-fac1663267c8",
     :is-primary true,
     :is-public false}],
   :profile
   [{:last-name "User",
     :country-id nil,
     :city nil,
     :background nil,
     :first-name "Root",
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :language "en",
     :id "c4643895-b0ce-44cc-b07b-2386bf18d43b",
     :state-id nil,
     :about nil
     :detail {:hello "world"}}]})

(def.xt RootUserFull
  {:is-super true,
   :is-suspended false,
   :notification
   [{:general {},
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :funding {},
     :id "d0adc63a-0bfa-41fe-b054-f4fb0cb354bd",
     :trading {}}],
   :is-official false,
   :nickname "root",
   :is-verified true,
   :password-updated 1630408723423619,
   :time-updated 1630408722786926,
   :time-created 1630408722786926,
   :funding nil,
   :is-active true,
   :id "00000000-0000-0000-0000-000000000000",
   :organisations
   [{:description nil,
     :is-personal true,
     :color "#5F8A16",
     :name "root",
     :background nil,
     :title "",
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :id "ec088f52-310b-491b-a034-d4efc222fd00",
     :picture nil}],
   :wallets
   [{:entries
     [{:id "6eb2fa48-c753-41c6-abda-c680828da1d2",
       :asset
       {:escrow 0,
        :time-updated 1630408722786926,
        :time-created 1630408722786926,
        :currency {:id "STATS"},
        :balance 10000000,
        :id "63acfd25-4b1b-4de4-aa82-909019c95591"}}
      {:id "4b146b40-947a-42a5-b116-2ad8816c4078",
       :asset
       {:escrow 0,
        :time-updated 1630408722786926,
        :time-created 1630408722786926,
        :currency {:id "XLM"},
        :balance 0,
        :id "222de282-ca29-4d04-81dd-86ec3f9189cf"}}
      {:id "2b3d4318-8cea-4420-a31c-f110d8198654",
       :asset
       {:escrow 0,
        :time-updated 1630408722786926,
        :time-created 1630408722786926,
        :currency {:id "XLM.T"},
        :balance 0,
        :id "9261d072-b7f5-41df-935a-c36fe13acf14"}}
      {:id "38889fdc-de34-4161-bb37-f8844d67ee5a",
       :asset
       {:escrow 0,
        :time-updated 1630408722786926,
        :time-created 1630408722786926,
        :currency {:id "USD"},
        :balance 0,
        :id "9e576e3e-c73e-4d18-92b4-f975c1bed3d4"}}],
     :id "531f3edb-b9d4-4c8e-8419-22edfe715b15"}],
   :emails
   [{:value "root@statstrade.io",
     :is-verified true,
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :is-active true,
     :id "db0898be-4630-43f5-96f3-fac1663267c8",
     :is-primary true,
     :is-public false}],
   :portfolios
   [{:contracts nil, :id "0a4bcae1-534b-4ce8-b261-d7c9097d4e19"}],
   :identities nil,
   :profile
   [{:last-name "User",
     :country-id nil,
     :city nil,
     :background nil,
     :first-name "Root",
     :time-updated 1630408722786926,
     :time-created 1630408722786926,
     :language "en",
     :id "c4643895-b0ce-44cc-b07b-2386bf18d43b",
     :state-id nil,
     :about nil
     :detail {:hello "world"}}]})

(def +app+ (pg/app "xt.db.sample"))

(def +tree+
  (pg/bind-schema (:schema +app+)))

(def.xt Schema
  (@! +tree+))

(def.xt SchemaCurrency
  (@! {"Currency" (get-in +tree+ ["Currency"])}))

(def.xt SchemaLookup
  (@! (pg/bind-app +app+)))

(comment
  (keys +app+)
  (h/qualified-keys @(get (:pointers +app+)
                          'UserNotification)
                    
                    
                    :static)
  
  (h/qualified-keys @(get (:pointers +app+)
                          'UserNotification)
                    
                    
                    :static)

  (-> (:id
       @(get (:pointers +app+)
             'UserNotification))
      :tree
      :UserNotification
      :time-updated)
  
  (-> (:static/schema-seed
       @(get (:pointers +app+)
             'UserNotification))
      :tree
      :UserNotification
      :time-updated)

    (-> (:static/schema-seed
       @(get (:pointers +app+)
             'UserAccount))
      :tree
      :UserAccount
      keys)

  (get (:pointers +app+)
       'UserAccount)
  
  
  
  
  (get @(get (:pointers +app+)
             'UserNotification)
       :static/schema)
  
  (get @(get (:pointers +app+)
             'UserNotification)
       :static/schema)
  
  (get @(get (:pointers +app+)
             'Currency)
       :static/schema)
  

  (def +compatibility+
    {:tables {}
     :enums  {}})


  
  )
