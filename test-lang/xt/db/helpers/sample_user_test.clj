(ns xt.db.helpers.sample-user-test
  (:require [hara.runtime.postgres :as pg :refer [defsel.pg defret.pg]]
            [hara.lang :as l]
            [xt.db.helpers.sample-data-test :as data]))

(l/script :postgres
  {:require [[hara.runtime.postgres :as pg]
              [xt.db.helpers.sample-data-test :as data]]
   :import [["citext"]
            ["uuid-ossp"]]
   :static {:application ["xt.db.helpers.sample"]
            :seed        ["scratch-sample-db"]
            :all         {:schema   ["scratch-sample-db"]}}})

(defn.pg as-array
  [:jsonb input]
  (when (== input "{}")
    (return "[]"))
  (return input))

(deftype.pg  ^{:public true}
  UserAccount
  [:id                {:type :uuid :primary true
                       :sql {:default (pg/uuid-generate-v4)}}
   :nickname          {:type :citext  :required true :scope :-/info :unique true}
   :password-hash     {:type :text  :required true :scope :-/hidden}
   :password-salt     {:type :text  :required true :scope :-/hidden}
   :password-updated  {:type :long :web {:example 0}
                       :sql {:default (pg/time-us)}}
   :is-super          {:type :boolean :required true :scope :-/info
                       :sql {:default false}}
   :is-suspended      {:type :boolean :required true :scope :-/info
                       :sql {:default false}}
   :is-official       {:type :boolean :required true :scope :-/info
                       :sql {:default false}}])

(deftype.pg ^{:public true}
  UserProfile
  [:id                {:type :uuid :primary true
                       :sql {:default (pg/uuid-generate-v4)}}
   :account    {:type :ref :required true :unique true
                :ref {:ns -/UserAccount
                      :rval :profile}
                :sql {:cascade true}}
   :first-name   {:type :text :scope :-/info
                  :web {:example "Test"}}
   :last-name    {:type :text :scope :-/info
                  :web {:example "User"}}
   :city         {:type :text
                  :web {:example "This is the test user account"}}
   :state        {:type :ref
                  :ref {:ns xt.db.helpers.sample-data-test/RegionState}}
   :country      {:type :ref
                  :ref {:ns xt.db.helpers.sample-data-test/RegionCountry}}
   :about        {:type :text
                  :web {:example "This is the test user account"}}
   :language     {:type :citext  :required true
                  :sql {:default "en"}}
   :detail       {:type :map  :scope :-/detail}])

(deftype.pg ^{:public true}
  UserNotification
  [:id                {:type :uuid :primary true
                       :sql {:default (pg/uuid-generate-v4)}}
   :account    {:type :ref :required true :unique true
                :ref {:ns -/UserAccount
                      :rval :notification}
                :sql {:cascade true}}
   :general    {:type :jsonb :required true}
   :trading    {:type :jsonb :required true}
   :funding    {:type :jsonb :required true}])

(deftype.pg ^{:public true}
  UserPrivilege
  [:id                {:type :uuid :primary true
                       :sql {:default (pg/uuid-generate-v4)}}
   :account       {:type :ref
                   :ref {:ns -/UserAccount
                         :rval :privileges}
                   :sql {:cascade true}}
   :type          {:type :citext :required true
                   :check #{"pool" "rake"}}
   :start-time    {:type :long :required true}
   :end-time      {:type :long :required true}])

(deftype.pg ^{:public true}
  Asset
   [:id          {:type :uuid :primary true
                  :sql {:default (pg/uuid-generate-v4)}}
    :currency    {:type :ref :required true
                  :ref {:ns xt.db.helpers.sample-data-test/Currency}
                  :web {:example "STATS"}}])

(deftype.pg ^{:public true}
  Wallet
  [:id       {:type :uuid :primary true
              :sql {:default (pg/uuid-generate-v4)}}
   :slug     {:type :citext
              :sql {:default "default"
                    :unique "default"}}
   :owner    {:type :ref :required true
              :ref {:ns -/UserAccount}
              :sql {:unique "default"
                    :cascade true}}])

(deftype.pg ^{:public true}
  WalletAsset
  [:id          {:type :uuid :primary true
                 :sql {:default (pg/uuid-generate-v4)}}
   :asset        {:type :ref :unique true
                  :ref {:ns -/Asset
                        :rval :linked-wallet}
                  :sql {:unique "default"
                        :cascade true}}
   :wallet       {:type :ref :required true
                  :ref {:ns -/Wallet :rval :entries}
                  :sql {:unique "default"
                        :cascade true}}])

(deftype.pg ^{:public true}
  Organisation
  [:id                {:type :uuid :primary true
                       :sql {:default (pg/uuid-generate-v4)}}
   :name         {:type :citext :required true :unique true}
   :title        {:type :text :required true}
   :description  {:type :text}
   :tags         {:type :array
                  :sql  {:process -/as-array}}
   :owner        {:type :ref :ref {:ns -/UserAccount}
                  :sql {:cascade true}}])

(deftype.pg
  OrganisationAccess
  [:id            {:type :uuid :primary true
                   :sql {:default (pg/uuid-generate-v4)}}
   :organisation  {:type :ref :required true
                   :ref  {:ns -/Organisation :rval :access}
                   :sql  {:unique "default"
                          :cascade true}}
   :account       {:type :ref :required true
                   :ref  {:ns -/UserAccount}
                   :sql  {:unique "default"
                          :cascade true}}
   :role          {:type :text :required true
                   :sql  {:default "member"}}])

(defn.pg organisation-assert-is-member
  [:uuid i-account-id :uuid i-organisation-id]
  (return true))
