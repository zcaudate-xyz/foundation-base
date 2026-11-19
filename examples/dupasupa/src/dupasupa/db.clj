(ns dupasupa.db
  (:require [lang.core :as l]))

(l/script :postgres
  {:require [[dupasupa.db.tables :as tables]
             [dupasupa.db.rpc :as rpc]]
   :static {:application ["dupasupa"]}})
