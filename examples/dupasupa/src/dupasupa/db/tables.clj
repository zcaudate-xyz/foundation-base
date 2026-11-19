(ns dupasupa.db.tables
  (:require [lang.core :as l]))

(l/script :postgres
  {:require [[postgres.core :as pg]]
   :static {:application ["dupasupa"]
            :seed        ["dupasupa_type"]
            :all         {:schema ["dupasupa_type"]}}})

(deftype.pg User
  [:id           {:type :uuid :primary true}
   :handle       {:type :text :required true}
   :display-name {:type :text :required true
                  :sql {:default ""}}
   :bio          {:type :text :required true
                  :sql {:default ""}}])

(deftype.pg Campaign
  [:id    {:type :uuid :primary true}
   :owner {:type :ref :required true
           :ref {:ns -/User}}
   :title {:type :text :required true}])
