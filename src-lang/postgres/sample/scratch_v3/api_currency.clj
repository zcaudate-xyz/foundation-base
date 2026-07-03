^{:no-test true}
(ns postgres.sample.scratch-v3.api-currency
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[postgres.sample.scratch-v3.realtime :as realtime]]
   :export [MODULE]})

(def.js EVENT_SYNC
  (realtime/db-sync ["Currency"]))

(def.js BRIEF
  {:type "view"
   :input (fn:> {:return-method "default"})})

(defn.js descriptor
  [base-input local-input]
  (return {:table "Currency"
           :event-sync -/EVENT_SYNC
           :base {:brief -/BRIEF
                  :list {:type "view"
                         :input base-input}}
           :local {:list {:type "raw"
                          :input local-input}}}))

(def.js CURRENCY_ALL
  (-/descriptor
   (fn [[] _context]
     (return {:select-method "all_active"}))
   (fn [[] _context]
     (return {:__deleted__ false}))))

(def.js CURRENCY_BY_ID
  (-/descriptor
   (fn [[currency-id] _context]
     (return {:select-method "by_id"
              :select-args [currency-id]}))
   (fn [[currency-id] _context]
     (return {:id currency-id
              :__deleted__ false}))))

(def.js CURRENCY_BY_TYPE
  (-/descriptor
   (fn [[currency-type] _context]
     (return {:select-method "by_type"
              :select-args [currency-type]}))
   (fn [[currency-type] _context]
     (return {:type currency-type
              :__deleted__ false}))))

(def.js MODULE (!:module))
