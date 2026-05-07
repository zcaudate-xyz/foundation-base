(ns postgres.sample.scratch-v3.api-currency
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[postgres.sample.scratch-v3.state :as state]]
   :export [MODULE]})

(def.js CURRENCY_ALL
  {:table "Currency"
   :event-sync (state/db-sync ["Currency"])
   :base {:brief {:type "view"
                  :input (fn:> {:return-method "default"})}
          :list {:type "view"
                 :input (fn [[] _context]
                          (return {:select-method "all_active"}))}}
   :local {:list {:type "raw"
                  :input (fn [[] _context]
                           (return {:__deleted__ false}))}}})

(def.js CURRENCY_BY_ID
  {:table "Currency"
   :event-sync (state/db-sync ["Currency"])
   :base {:brief {:type "view"
                  :input (fn:> {:return-method "default"})}
          :list {:type "view"
                 :input (fn [[currency-id] _context]
                          (return {:select-method "by_id"
                                   :select-args [currency-id]}))}}
   :local {:list {:type "raw"
                  :input (fn [[currency-id] _context]
                           (return {:id currency-id
                                    :__deleted__ false}))}}})

(def.js CURRENCY_BY_TYPE
  {:table "Currency"
   :event-sync (state/db-sync ["Currency"])
   :base {:brief {:type "view"
                  :input (fn:> {:return-method "default"})}
          :list {:type "view"
                 :input (fn [[currency-type] _context]
                          (return {:select-method "by_type"
                                   :select-args [currency-type]}))}}
   :local {:list {:type "raw"
                  :input (fn [[currency-type] _context]
                           (return {:type currency-type
                                    :__deleted__ false}))}}})

(def.js MODULE (!:module))
