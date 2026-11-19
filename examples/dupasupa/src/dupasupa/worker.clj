(ns dupasupa.worker
  (:require [lang.core :as l]))

(l/script :js
  {:import [["../lib/model.mjs" :as model]] :static {:export [handleMessage]}})

;; An explicit demonstration transport. Production supplies database RPC calls.
(def.js rows [{"id" "campaign-1" "title" "First campaign"}])
(def.js transport
  {"list" (fn [] (return (JSON.parse (JSON.stringify -/rows))))
   "call" (fn [operation values]
            (when (not (== operation "save-campaign"))
              (throw (new Error "Unknown action")))
            (when (not (. values.i_title (trim)))
              (throw (new Error "Campaign title is required")))
            (var row (. -/rows (find (fn [row] (return (== row.id values.i_id))))))
            (when (not row)
              (throw (new Error "Campaign unavailable")))
            (:= row.title values.i_title)
            (return row))})
(def.js owner (model.open -/transport))

(defn.js ^{:- [:async]} handleMessage
  [message]
  (try (var result nil)
       (cond (== message.action "load") (:= result (await (model.loadRows -/owner)))
             (== message.action "save") (:= result
                                            (await (model.save -/owner message.id message.title)))
             (== message.action "close") (:= result (model.close -/owner))
             :else (throw (new Error "Unknown action")))
       (return {"request" message.request "result" result})
       (catch error (return {"request" message.request "error" error.message}))))

(defrun.js install
  (when (not (== (typeof self) "undefined"))
    (:= self.onmessage
        (async (fn [event] (. self (postMessage (await (-/handleMessage event.data)))))))))
