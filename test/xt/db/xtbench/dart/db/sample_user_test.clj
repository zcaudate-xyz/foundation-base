(ns
 xtbench.dart.db.sample-user-test
 (:require
  [rt.postgres :as pg :refer [defsel.pg defret.pg]]
  [std.lang :as l]))

(l/script
 :postgres
 {:require [[rt.postgres :as pg] [xt.db.sample-data-test :as data]],
  :static
  {:application ["xt.db.sample"],
   :seed ["scratch-sample-db"],
   :all {:schema ["scratch-sample-db"]}}})

(defn.pg
 as-array
 [:jsonb input]
 (when (== input "{}") (return "[]"))
 (return input))

(deftype.pg
 ^{:public true} UserAccount
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :nickname
  {:type :citext, :required true, :scope :-/info, :unique true}
  :password-hash
  {:type :text, :required true, :scope :-/hidden}
  :password-salt
  {:type :text, :required true, :scope :-/hidden}
  :password-updated
  {:type :long, :web {:example 0}, :sql {:default (pg/time-us)}}
  :is-super
  {:type :boolean,
   :required true,
   :scope :-/info,
   :sql {:default false}}
  :is-suspended
  {:type :boolean,
   :required true,
   :scope :-/info,
   :sql {:default false}}
  :is-official
  {:type :boolean,
   :required true,
   :scope :-/info,
   :sql {:default false}}])

(deftype.pg
 ^{:public true} UserProfile
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :account
  {:type :ref,
   :required true,
   :unique true,
   :ref {:ns -/UserAccount, :rval :profile},
   :sql {:cascade true}}
  :first-name
  {:type :text, :scope :-/info, :web {:example "Test"}}
  :last-name
  {:type :text, :scope :-/info, :web {:example "User"}}
  :city
  {:type :text, :web {:example "This is the test user account"}}
  :state
  {:type :ref, :ref {:ns data/RegionState}}
  :country
  {:type :ref, :ref {:ns data/RegionCountry}}
  :about
  {:type :text, :web {:example "This is the test user account"}}
  :language
  {:type :citext, :required true, :sql {:default "en"}}
  :detail
  {:type :map, :scope :-/detail}])

(deftype.pg
 ^{:public true} UserNotification
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :account
  {:type :ref,
   :required true,
   :unique true,
   :ref {:ns -/UserAccount, :rval :notification},
   :sql {:cascade true}}
  :general
  {:type :jsonb, :required true}
  :trading
  {:type :jsonb, :required true}
  :funding
  {:type :jsonb, :required true}])

(deftype.pg
 ^{:public true} UserPrivilege
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :account
  {:type :ref,
   :ref {:ns -/UserAccount, :rval :privileges},
   :sql {:cascade true}}
  :type
  {:type :citext, :required true, :check #{"pool" "rake"}}
  :start-time
  {:type :long, :required true}
  :end-time
  {:type :long, :required true}])

(deftype.pg
 ^{:public true} Asset
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :currency
  {:type :ref,
   :required true,
   :ref {:ns data/Currency},
   :web {:example "STATS"}}])

(deftype.pg
 ^{:public true} Wallet
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :slug
  {:type :citext, :sql {:default "default", :unique "default"}}
  :owner
  {:type :ref,
   :required true,
   :ref {:ns -/UserAccount},
   :sql {:unique "default", :cascade true}}])

(deftype.pg
 ^{:public true} WalletAsset
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :asset
  {:type :ref,
   :unique true,
   :ref {:ns -/Asset, :rval :linked-wallet},
   :sql {:unique "default", :cascade true}}
  :wallet
  {:type :ref,
   :required true,
   :ref {:ns -/Wallet, :rval :entries},
   :sql {:unique "default", :cascade true}}])

(deftype.pg
 ^{:public true} Organisation
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :name
  {:type :citext, :required true, :unique true}
  :title
  {:type :text, :required true}
  :description
  {:type :text}
  :tags
  {:type :array, :sql {:process -/as-array}}
  :owner
  {:type :ref, :ref {:ns -/UserAccount}, :sql {:cascade true}}])

(deftype.pg
 OrganisationAccess
 [:id
  {:type :uuid, :primary true, :sql {:default (pg/uuid-generate-v4)}}
  :organisation
  {:type :ref,
   :required true,
   :ref {:ns -/Organisation, :rval :access},
   :sql {:unique "default", :cascade true}}
  :account
  {:type :ref,
   :required true,
   :ref {:ns -/UserAccount},
   :sql {:unique "default", :cascade true}}
  :role
  {:type :text, :required true, :sql {:default "member"}}])

(defn.pg
 organisation-assert-is-member
 [:uuid i-account-id :uuid i-organisation-id]
 (return true))

(defsel.pg
 ^{:- [-/Organisation], :args [:text i-name], :api/view true} organisation-by-name
 {:name i-name})

(defsel.pg
 ^{:- [-/Organisation], :args [:uuid i-account-id], :scope #{:personal}, :api/view true} organisation-all-as-owner)

(defsel.pg
 ^{:- [-/Organisation], :args [:uuid i-account-id], :scope #{:personal}, :api/view true} organisation-all-as-owner)

(defsel.pg
 ^{:- [-/Organisation], :args [:uuid i-account-id], :scope #{:personal}, :api/view true} organisation-all-as-admin)

(defsel.pg
 ^{:- [-/Organisation], :args [:uuid i-account-id], :scope #{:personal}, :api/view true} organisation-all-as-member)

(defret.pg
 ^{:- [-/UserAccount], :scope #{:public}, :api/view true} user-account-info
 [:uuid i-account-id]
 #{[:profile #{:*/standard}] :nickname :id})

(defsel.pg
 ^{:- [-/UserAccount], :args [:uuid i-organisation-id], :guards [(-/organisation-assert-is-member <%> i-organisation-id)], :api/view true} user-account-by-organisation
 {:organisation-accesses {:organisation i-organisation-id}})

(defret.pg
 ^{:- [-/Organisation], :api/view true} organisation-view-membership
 [:uuid i-organisation-id]
 #{:*/data [:access #{:*/data [:account #{:nickname :id}]}]})

(defret.pg
 ^{:- [-/Organisation], :scope #{:public}, :api/view true} organisation-view-default
 [:uuid i-organisation-id]
 #{:*/data})
