(ns postgres.sample.scratch-v3
  (:require [hara.lang :as l]
             [postgres.core :as pg :refer [defret.pg defsel.pg]]
             [hara.runtime.postgres.base.application :as app]
             [postgres.gen.bind-macro :as bind-pg]))

(l/script :postgres
  {:config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]]
   :import [["citext"]
            ["uuid-ossp"]]
   :static {:application ["scratch_v3"]
            :seed        ["scratch_v3"]
            :all {:schema ["scratch_v3"]}}})

(def RecordType
  [:op-created {:type :uuid :scope :-/system}
   :op-updated {:type :uuid :scope :-/system}
   :time-created {:type :long}
   :time-updated {:type :long}
   :__deleted__ {:type :boolean :scope :-/hidden
                 :sql {:default false}}])

(def TrackingMin
  {:name "min"
   :in {:create {:op-created :id
                 :op-updated :id
                 :time-created :time
                 :time-updated :time}
        :modify {:op-updated :id
                 :time-updated :time}}
   :ignore #{:delete}})

(defenum.pg ^{}
  EnumCurrencyType [:digital :fiat :crypto])

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
  Currency
  "Currency source of truth for the scratch-v3 sample."
  [:id           {:type :citext :primary true
                  :web {:example "AUD"}}
   :type         {:type :enum :required true :scope :-/info
                  :enum {:ns -/EnumCurrencyType}
                  :web {:example "fiat"}}
   :symbol       {:type :text :scope :-/info
                  :web {:example "$"}}
   :native       {:type :text :scope :-/info
                  :web {:example "$AU"}}
   :decimal      {:type :long
                  :web {:example 2}}
   :name         {:type :text :scope :-/info
                  :web {:example "Australian Dollar"}}
   :plural       {:type :text
                  :web {:example "Australian Dollars"}}
   :description  {:type :text
                  :web {:example "Currency for Australia"}}])

(defsel.pg ^{:- [-/Currency]
             :args []
             :api/view true}
  currency-all-active
  {:__deleted__ false})

(defsel.pg ^{:- [-/Currency]
             :args [:citext i-currency-id]
             :api/view true}
  currency-by-id
  {:id i-currency-id
   :__deleted__ false})

(defsel.pg ^{:- [-/Currency]
             :args [:text i-currency-type]
             :api/view true}
  currency-by-type
  {:type i-currency-type
   :__deleted__ false})

(defret.pg ^{:- [-/Currency]
             :args []
             :api/view true}
  currency-default
  [:citext i-currency-id]
  #{:*/standard})

(defn.pg ^{:api/flags []}
  insert-currency
  "Creates a currency row."
  {:added "4.1"}
  [:citext i-id
   :text i-type
   :text i-symbol
   :text i-native
   :bigint i-decimal
   :text i-name
   :text i-plural
   :text i-description
   :jsonb o-op]
  (let [o-out (pg/g:insert -/Currency
                           {:id i-id
                            :type i-type
                            :symbol i-symbol
                            :native i-native
                            :decimal i-decimal
                            :name i-name
                            :plural i-plural
                            :description i-description}
                           {:track o-op})]
    (return o-out)))

(defn.pg ^{:api/flags []}
  update-currency
  "Updates currency attributes without changing the primary id."
  {:added "4.1"}
  [:citext i-id
   :jsonb m
   :jsonb o-op]
  (let [o-out (pg/t:update -/Currency
                           {:where {:id i-id}
                            :set m
                            :track o-op})]
    (return o-out)))

(defn.pg ^{:api/flags []}
  delete-currency
  "Marks a currency row as deleted so clients can remove it from cache."
  {:added "4.1"}
  [:citext i-id
   :jsonb o-op]
  (let [o-out (pg/t:update -/Currency
                           {:where {:id i-id}
                            :set {:__deleted__ true}
                            :track o-op})]
    (return o-out)))

;;
;; User / UserProfile / Wallet / Asset
;;

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
  User
  "User identity for the scratch-v3 sample."
  [:id            {:type :uuid :primary true
                   :sql {:default (pg/uuid-generate-v4)}}
   :nickname      {:type :citext :required true :unique true
                   :scope :-/info
                   :web {:example "user123"}}
   :email         {:type :citext :required true :unique true
                   :scope :-/info
                   :web {:example "user@example.com"}}
   :is-active     {:type :boolean :required true
                   :scope :-/info
                   :sql {:default true}}
   :is-verified   {:type :boolean :required true
                   :scope :-/info
                   :sql {:default false}}])

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
  UserProfile
  "Public profile attached to a User."
  [:id            {:type :uuid :primary true
                   :sql {:default (pg/uuid-generate-v4)}}
   :account       {:type :ref :required true :unique true
                   :ref {:ns -/User :rval :profile}
                   :sql {:cascade true}}
   :first-name    {:type :text :scope :-/info
                   :web {:example "John"}}
   :last-name     {:type :text :scope :-/info
                   :web {:example "Smith"}}
   :language      {:type :citext :required true
                   :scope :-/info
                   :sql {:default "EN"}}
   :about         {:type :text
                   :web {:example "Scratch v3 user"}}])

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
  Wallet
  "Wallet owned by a User."
  [:id            {:type :uuid :primary true
                   :sql {:default (pg/uuid-generate-v4)}}
   :slug          {:type :citext :required true
                   :scope :-/info
                   :sql {:default "default"}}
   :owner         {:type :ref :required true :unique true
                   :ref {:ns -/User}
                   :sql {:cascade true}}])

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
  Asset
  "Asset balance for a currency inside a Wallet."
  [:id            {:type :uuid :primary true
                   :sql {:default (pg/uuid-generate-v4)}}
   :currency      {:type :ref :required true
                   :ref {:ns -/Currency}}
   :wallet        {:type :ref :required true
                   :ref {:ns -/Wallet :rval :entries}
                   :sql {:cascade true}}
   :balance       {:type :numeric :required true
                   :sql {:default 0}}
   :count-tx      {:type :bigint :required true
                   :sql {:default 0}}])

;; -- User helpers

(defsel.pg ^{:- [-/User]
             :args []
             :api/view true}
  user-all
  {:__deleted__ false})

(defsel.pg ^{:- [-/User]
             :args [:uuid i-user-id]
             :api/view true}
  user-by-id
  {:id i-user-id
   :__deleted__ false})

(defsel.pg ^{:- [-/User]
             :args [:citext i-nickname]
             :api/view true}
  user-by-nickname
  {:nickname i-nickname
   :__deleted__ false})

(defn.pg
  insert-user
  "Creates a user row."
  {:added "4.1"}
  [:uuid i-id
   :citext i-nickname
   :citext i-email
   :boolean i-is-active
   :boolean i-is-verified
   :jsonb o-op]
  (let [o-out (pg/g:insert -/User
                           {:id i-id
                            :nickname i-nickname
                            :email i-email
                            :is-active i-is-active
                            :is-verified i-is-verified}
                           {:track o-op})]
    (return o-out)))

(defn.pg
  update-user
  "Updates a user row."
  {:added "4.1"}
  [:uuid i-id
   :jsonb m
   :jsonb o-op]
  (let [o-out (pg/t:update -/User
                           {:where {:id i-id}
                            :set m
                            :track o-op})]
    (return o-out)))

(defn.pg
  delete-user
  "Soft-deletes a user row."
  {:added "4.1"}
  [:uuid i-id
   :jsonb o-op]
  (let [o-out (pg/t:update -/User
                           {:where {:id i-id}
                            :set {:__deleted__ true}
                            :track o-op})]
    (return o-out)))

(defn.pg
  create-user
  "Creates a user row, generating the id."
  {:added "4.1"}
  [:citext i-nickname
   :citext i-email
   :jsonb o-op]
  (let [(:uuid i-id) (pg/gen-random-uuid)]
    (return (-/insert-user i-id i-nickname i-email true false o-op))))

;; -- UserProfile helpers

(defsel.pg ^{:- [-/UserProfile]
             :args [:uuid i-account-id]
             :api/view true}
  user-profile-by-account
  {:account i-account-id})

(defn.pg
  insert-user-profile
  "Creates a profile for a user."
  {:added "4.1"}
  [:uuid i-account-id
   :text i-first-name
   :text i-last-name
   :citext i-language
   :text i-about
   :jsonb o-op]
  (let [o-out (pg/g:insert -/UserProfile
                           {:account i-account-id
                            :first-name i-first-name
                            :last-name i-last-name
                            :language i-language
                            :about i-about}
                           {:track o-op})]
    (return o-out)))

(defn.pg
  update-user-profile
  "Updates the profile for a user."
  {:added "4.1"}
  [:uuid i-account-id
   :jsonb m
   :jsonb o-op]
  (let [o-out (pg/t:update -/UserProfile
                           {:where {:account i-account-id}
                            :set m
                            :track o-op})]
    (return o-out)))

;; -- Wallet helpers

(defsel.pg ^{:- [-/Wallet]
             :args [:uuid i-owner-id]
             :api/view true}
  wallet-by-owner
  {:owner i-owner-id})

(defn.pg
  insert-wallet
  "Creates a wallet for a user."
  {:added "4.1"}
  [:uuid i-owner-id
   :citext i-slug
   :jsonb o-op]
  (let [o-out (pg/g:insert -/Wallet
                           {:owner i-owner-id
                            :slug i-slug}
                           {:track o-op})]
    (return o-out)))

(defn.pg
  ensure-wallet
  "Creates a default wallet for a user if one does not exist."
  {:added "4.1"}
  [:uuid i-owner-id
   :jsonb o-op]
  (let [v-wallet (pg/t:get -/Wallet
                           {:where {:owner i-owner-id}})]
    (if v-wallet
      (return v-wallet)
      (let [o-out (pg/g:insert -/Wallet
                               {:owner i-owner-id
                                :slug "default"}
                               {:track o-op})]
        (return o-out)))))

;; -- Asset helpers

(defsel.pg ^{:- [-/Asset]
             :args [:uuid i-wallet-id]
             :api/view true}
  asset-by-wallet
  {:wallet i-wallet-id})

(defsel.pg ^{:- [-/Asset]
             :args [:uuid i-wallet-id
                    :citext i-currency-id]
             :api/view true}
  asset-by-wallet-currency
  {:wallet i-wallet-id
   :currency i-currency-id})

(defn.pg
  insert-asset
  "Creates an asset row."
  {:added "4.1"}
  [:uuid i-wallet-id
   :citext i-currency-id
   :numeric i-balance
   :jsonb o-op]
  (let [o-out (pg/g:insert -/Asset
                           {:wallet i-wallet-id
                            :currency i-currency-id
                            :balance i-balance}
                           {:track o-op})]
    (return o-out)))

(defn.pg
  ensure-asset
  "Creates an asset for a wallet/currency pair if one does not exist."
  {:added "4.1"}
  [:uuid i-wallet-id
   :citext i-currency-id
   :jsonb o-op]
  (let [v-asset (pg/t:get -/Asset
                          {:where {:wallet i-wallet-id
                                   :currency i-currency-id}})]
    (if v-asset
      (return v-asset)
      (let [o-out (pg/g:insert -/Asset
                               {:wallet i-wallet-id
                                :currency i-currency-id
                                :balance 0}
                               {:track o-op})]
        (return o-out)))))

(defn.pg
  asset-credit
  "Credits an asset balance."
  {:added "4.1"}
  [:uuid i-asset-id
   :numeric i-amount
   :jsonb o-op]
  (let [o-out (pg/t:update -/Asset
                           {:where {:id i-asset-id}
                            :set {:balance (+ #{"balance"} i-amount)
                                  :count-tx (+ #{"count_tx"} 1)}
                            :track o-op
                            :single true})]
    (return o-out)))

(defn.pg
  asset-deduct
  "Deducts from an asset balance."
  {:added "4.1"}
  [:uuid i-asset-id
   :numeric i-amount
   :jsonb o-op]
  (let [o-out (pg/t:update -/Asset
                           {:where {:id i-asset-id}
                            :set {:balance (- #{"balance"} i-amount)
                                  :count-tx (+ #{"count_tx"} 1)}
                            :track o-op
                            :single true})]
    (return o-out)))





(def +app+ (app/app-create "scratch_v3"))
(def +lookup+ (bind-pg/bind-app +app+ :time-updated))




(def +schema+
  (bind-pg/bind-schema (:schema +app+)))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]
   :export [MODULE]})

(defn.xt get-schema
  "returns the generated scratch_v3 schema"
  {:added "4.1"}
  []
  (return (@! +schema+)))

(defn.xt get-schema-lookup
  "returns the generated schema lookup with update-key metadata"
  {:added "4.1"}
  []
  (return (@! +lookup+)))

(def.xt MODULE (!:module))
