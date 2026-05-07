(ns postgres.sample.scratch-v3
  (:require [hara.lang :as l]
             [postgres.core :as pg :refer [defret.pg defsel.pg]]
             [hara.runtime.postgres.base.application :as app]
             [hara.model.spec-postgres.gen-bind :as bind-pg]))

(l/script :postgres
  {:config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]]
   :import [["citext"]
            ["uuid-ossp"]]
   :static {:application ["scratch-v3"]
            :seed        ["scratch-v3"]
            :all {:schema ["scratch-v3"]}}})

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

(def +app+ (app/app-create "scratch-v3"))
(def +lookup+ (bind-pg/bind-app +app+ :time-updated))
(def +schema+
  {"Currency"
   {"id" {"ident" "id" "type" "text" "order" 0}
    "type" {"ident" "type" "type" "text" "order" 1}
    "symbol" {"ident" "symbol" "type" "text" "order" 2}
    "native" {"ident" "native" "type" "text" "order" 3}
    "decimal" {"ident" "decimal" "type" "long" "order" 4}
    "name" {"ident" "name" "type" "text" "order" 5}
    "plural" {"ident" "plural" "type" "text" "order" 6}
    "description" {"ident" "description" "type" "text" "order" 7}
    "time_created" {"ident" "time_created" "type" "long" "order" 8}
    "time_updated" {"ident" "time_updated" "type" "long" "order" 9}
    "op_created" {"ident" "op_created" "type" "text" "order" 10}
    "op_updated" {"ident" "op_updated" "type" "text" "order" 11}
    "__deleted__" {"ident" "__deleted__" "type" "boolean" "order" 12}}})

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]
   :export [MODULE]})

(defn.xt get-schema
  "returns the generated scratch-v3 schema"
  {:added "4.1"}
  []
  (return (@! +schema+)))

(defn.xt get-schema-lookup
  "returns the generated schema lookup with update-key metadata"
  {:added "4.1"}
  []
  (return (@! +lookup+)))

(def.xt MODULE (!:module))
