(ns postgres.sample.scratch-v3
  (:require [hara.lang :as l]))

(l/script :postgres
  {:require [[postgres.core :as pg]]
   :import [["citext"]
            ["uuid-ossp"]]
   :config {:dbname "test-scratch"}
   :static {:application ["scratch-v3"]
            :seed        ["scratch-v3"]
            :all {:schema ["scratch-v3"]}}})

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

