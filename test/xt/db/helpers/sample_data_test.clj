(ns xt.db.helpers.sample-data-test
  (:require [rt.postgres :as pg
               :refer [defsel.pg defret.pg]]
            [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :postgres
  {:require [[rt.postgres :as pg]]
   :static {:application ["xt.db.helpers.sample"]
            :seed        ["scratch-sample-db"]
            :all         {:schema   ["scratch-sample-db"]}}})

(defenum.pg ^{}
  EnumCurrencyType [:digital :fiat :crypto])

(deftype.pg ^{:public true}
  Currency
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

(deftype.pg ^{:public true}
  RegionCountry
  [:id           {:type :citext :primary true
                  :web {:example "AU"}}
   :ref          {:type :integer}
   :name         {:type :text :required true :scope :-/info
                  :web {:example "Australia"}}
   :name-native  {:type :text
                  :web {:example "Australia"}}
   :iso          {:type :citext :scope :-/info
                  :web {:example "AUS"}}
   :iso-numeric  {:type :text :scope :-/info
                  :web {:example "23"}}
   :iso-tld      {:type :text :scope :-/info
                  :web {:example ".au"}}
   :iso-phone    {:type :text :scope :-/info
                  :web {:example "61"}}
   :capital      {:type :text :scope :-/info
                  :web {:example "Canberra"}}
   :flag         {:type :text :scope :-/info
                  :web {:example "U+1F1E6 U+1F1FC"}}
   :region       {:type :text :scope :-/info
                  :web {:example "Oceania"}}
   :subregion    {:type :text :scope :-/info
                  :web {:example "Australia and New Zealand"}}
   :latitude     {:type :text :scope :-/info}
   :longitude    {:type :text :scope :-/info}
   :translations {:type :jsonb}
   :timezones    {:type :jsonb}
   :currencies   {:type :jsonb}])

(deftype.pg ^{:public true}
  RegionState
  [:id           {:type :text :primary true
                  :web {:example "3903"}}
   :ref          {:type :integer}
   :name         {:type :text :required true
                  :web {:example "Victoria"}}
   :code         {:type :text :required true
                  :web {:example "VIC"}}
   :country      {:type :ref :ref {:ns -/RegionCountry}
                  :web {:example "AU"}}
   :latitude     {:type :text}
   :longitude    {:type :text}])

(deftype.pg ^{:public true}
  RegionCity
  [:id           {:type :text :primary true
                  :web {:example "6235"}}
   :ref          {:type :integer}
   :name         {:type :text :required true
                  :web {:example "Melbourne"}}
   :latitude     {:type :text :required true
                  :web {:example "-37.81400000"}}
   :longitude    {:type :text :required true
                  :web {:example "144.96332000"}}
   :state        {:type :ref :ref {:ns -/RegionState}
                  :web {:example "3903"}}
   :country      {:type :ref :ref {:ns -/RegionCountry}
                  :web {:example "AU"}}])

(defconst.pg 
  CurrencyUSD [-/Currency]
  {:id "USD" 
   :symbol "USD"
   :type "fiat" 
   :description "Default Current for the United States of America"
   :decimal 2 :name "US Dollar" :plural "US Dollars"})

(defconst.pg 
  CurrencySTATS [-/Currency]
  {:id "STATS"
   :type "digital"
   :symbol "Δ"
   :native "Δ"
   :description "Default Currency for Statstrade"
   :decimal 0 :name "Stat Coin" :plural "Stat coins"})

(defconst.pg 
  CurrencyXLM [-/Currency]
  {:id "XLM" 
   :symbol "XLM"
   :type "crypto" 
   :description "Default Currency for the Stellar Blockchain"
   :decimal -1 :name "Stellar Coin" :plural "Stellar coins"})

(defconst.pg 
  CurrencyXLMTest [-/Currency]
  {:id "XLM.T" 
   :symbol "XLM.T"
   :type "crypto" 
   :description "Default Currency for the Stellar TestNet Blockchain"
   :decimal -1 :name "Stellar TestNet Coin" :plural "Stellar TestNet coins"})

(comment
  ;;
  ;; LUA-ROUTE BINDINGS
  ;;
  (def +route-entries+
    (mapv (fn [[db-sym source-sym]]
            [(symbol (str "lua-route-" (name source-sym))) db-sym])
          +db-entries+))
   
  (f/template-entries [bind-route]
    +route-entries+))
