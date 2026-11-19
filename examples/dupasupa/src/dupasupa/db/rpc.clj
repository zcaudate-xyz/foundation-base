(ns dupasupa.db.rpc
  (:require [lang.core :as l]))

(l/script :postgres
  {:require [[postgres.core :as pg]
             [dupasupa.db.tables :as tables]]
   :static {:application ["dupasupa"]
            :seed        ["dupasupa_rpc"]
            :all         {:schema ["dupasupa_rpc"]}}})

(defn.pg ^{:%% :sql :- [:text]}
  ping
  []
  [:select "pong"])

(defn.pg list-campaigns
  []
  (let [o-rows (pg/t:select tables/Campaign {:order-by [:id] :order-sort :asc})]
    (return (pg/coalesce o-rows (:jsonb "[]")))))

(defn.pg ^{:- [:text]}
  validate-campaign-title
  [:text i-title]
  (when (or [i-title :is-null] (== (pg/btrim i-title) ""))
    [:raise-exception "Campaign title is required"])
  (when (> (pg/length (pg/btrim i-title)) 120)
    [:raise-exception "Campaign title must be 120 characters or fewer"])
  (return (pg/btrim i-title)))

(defn.pg create-owned-campaign
  [:uuid i-user :text i-title]
  (let [(:uuid o-id) (pg/gen-random-uuid)
        (:text o-title) (-/validate-campaign-title i-title)]
    (let [o-insert (pg/t:insert tables/Campaign {:id o-id :owner i-user :title o-title})
          o-row (pg/t:get tables/Campaign {:where {:id o-id :owner i-user}})]
      (return o-row))))

(defn.pg ^{:- [:jsonb]}
  save-campaign
  [:uuid i-id :text i-title]
  (when (or (== i-title nil) (== (pg/btrim i-title) ""))
    [:raise-exception "Campaign title is required"])
  (let [result (pg/t:update tables/Campaign
                 {:where {:id i-id} :set {:title (-/validate-campaign-title i-title)}})]
    (return result)))

(defn.pg ret-profile
  [:uuid i-user]
  (let [o-user (pg/t:get tables/User {:where {:id i-user}})] (return o-user)))

(defn.pg save-profile
  [:uuid i-user :text i-handle :text i-name :text i-bio]
  (when (or (== i-handle nil) (< (pg/length (pg/btrim i-handle)) 2) (> (pg/length i-handle) 32))
    [:raise-exception "Handle must contain 2 to 32 characters"])
  (when (or (== i-name nil) (== (pg/btrim i-name) "") (> (pg/length i-name) 80))
    [:raise-exception "Display name must contain 1 to 80 characters"])
  (when (> (pg/length (pg/coalesce i-bio "")) 500)
    [:raise-exception "Bio must be 500 characters or fewer"])
  (let [o-user (pg/t:update tables/User
                 {:where {:id i-user}
                  :set {:handle (pg/btrim i-handle)
                        :display-name (pg/btrim i-name)
                        :bio (pg/coalesce i-bio "")}})]
    (return (-/ret-profile i-user))))

(defn.pg list-owned-campaigns
  [:uuid i-user]
  (let [o-rows (pg/t:select tables/Campaign
                 {:where {:owner i-user} :order-by [:id] :order-sort :asc})]
    (return (pg/coalesce o-rows (:jsonb "[]")))))

(defn.pg save-owned-campaign
  [:uuid i-user :uuid i-id :text i-title]
  (let [o-row (pg/t:get tables/Campaign {:where {:id i-id :owner i-user}})]
    (when [o-row :is-null]
      [:raise-exception "Campaign is not available to this account"])
    (return (-/save-campaign i-id i-title))))
