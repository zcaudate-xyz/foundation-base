(ns xt.db.helpers.neon-live
  (:require [lang.core :as l]
            [postgres.core :as pg]))

(l/script :postgres
  {:require [[postgres.core :as pg]]
   :static {:application ["foundation-neon-scratch"]
            :all {:schema ["neon_scratch"]}}})

(deftype.pg NeonSmoke
  [:id {:type :uuid :primary true}
   :owner-id {:type :text :required true}
   :nickname {:type :text :required true}])

(l/script :xtalk)
(def.xt Schema (@! (pg/bind-schema (:schema (pg/app "foundation-neon-scratch")))))
(def.xt Lookup (@! (pg/bind-app (pg/app "foundation-neon-scratch"))))
