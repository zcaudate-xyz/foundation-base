(ns lua.ext.plisio
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :lua
  {:require [[lua.nginx.http-client :as http]
             [xt.lang.base-lib :as k]]
   })

(def.lua ROOT_API_URL
  "https://plisio.net/api/v1/")

(def.lua ROOT_API_RESOURCES
  {:new-invoice {:path "invoices/new"
                 :query {:default {:expire-min 60}
                         :fields  ["currency"
                                   "order_name"
                                   "order_number"
                                   "amount"
                                   "description"
                                   "email"
                                   "expire_min"]}}
   
   :withdrawal  {:path "operations/withdraw"
                 :query {:fields  ["currency"
                                   "type"
                                   "to"
                                   "amount"
                                   "feePlan"]}}
   :currencies  {:path "currencies"
                 :args ["<currency_id>"]}
   :balance     {:path "balances"
                 :args ["<currency_id>"]}
   :fee-commission    {:path "operations/fee-commission"
                       :args ["<currency_id>"]
                       :fields  ["addresses"
                                 "amounts"
                                 "feePlan"
                                 "amount"
                                 "type"]}
   :fee-plan    {:path "operations/fee-plan"
                 :args ["<currency_id>"]}

   :fee-estimate  {:path "operations/fee"
                   :args ["<currency_id>"]
                   :fields  ["addresses"
                             "amounts"
                             "feePlan"
                             "amount"]}
   :tx-search   {:path "operations"
                 :query {:fields  ["page"
                                   "limit"
                                   "type"
                                   "shop_id"
                                   "status"
                                   "currency"
                                   "search"]}}
   :tx-detail   {:path "operations"
                 :args ["<id>"]}})

(defn.lua plisio-query
  "performs a plisio query"
  {:added "4.0"}
  [tag args query-data api-key]
  (var resource (. -/ROOT_API_RESOURCES [tag]))
  (var #{path} resource)
  
  (var url (k/cat -/ROOT_API_URL
                  path
                  (:? (k/not-empty? args)
                      (k/cat "/" (k/join "/" args))
                      "")))
  
  
  (var fields   (k/get-in resource ["query" "fields"]))
  (var query    (-> (k/obj-clone (k/get-in resource ["query" "defaults"]))
                    (k/obj-assign
                     (:? fields
                         (k/obj-pick query-data (or fields []))
                         query-data))
                    (k/step-set-key "api_key" api-key)))
  (var conn (http/new))
  (var '[res err] (http/request-uri conn url
                                    {:query query
                                     :ssl-verify false}))
  (when err
    (return
     {:status "error"
      :data {:message err}}))

  (when (or (== 401 (. res status))
            (== 200 (. res status)))
    (return
     (k/json-decode (. res body))))

  (return
   {:status "error"
    :data {:message (. res body)
             :response {:status (. res status)
                        :reason (. res reason)}}}))

(defn.lua ^{:arglists '([{:keys [page limit type shop_id
                                  status currency search]}
                         api-key])}
  tx-search
  "performs transation search"
  {:added "4.0"}
  [m api-key]
  (return
   (-/plisio-query "tx_search" [] m api-key)))

(defn.lua tx-detail
  "gets transaction detail"
  {:added "4.0"}
  [tx-id api-key]
  (return
   (-/plisio-query "tx_detail" [tx-id] {} api-key)))

(defn.lua get-currencies
  "gets all currencies"
  {:added "4.0"}
  []
  (return
   (-/plisio-query "currencies" [] {} nil)))

(defn.lua get-currencies-exchange
  "gets currencies for exchange fiat currency"
  {:added "4.0"}
  [currency-id]
  (return
   (-/plisio-query "balance" [currency-id] {} nil)))

(defn.lua get-balance
  "gets the current balance of crypto"
  {:added "4.0"}
  [currency-id api-key]
  (return
   (-/plisio-query "balance" [currency-id] {} api-key)))

(defn.lua get-fee-plan
  "gets the fee plan"
  {:added "4.0"}
  [currency-id api-key]
  (return
   (-/plisio-query "fee_plan" [currency-id] {} api-key)))

(defn.lua  ^{:arglists '([{:keys [addresses amounts feePlan]}
                          api-key])}
  get-fee-estimate
  "gets the fee estimate"
  {:added "4.0"}
  [currency-id m api-key]
  (return
   (-/plisio-query "fee_estimate" [currency-id] m api-key)))

(defn.lua  ^{:arglists '([{:keys [addresses amounts feePlan type]}
                          api-key])}
  get-fee-commission
  "gets the fee commission"
  {:added "4.0"}
  [currency-id m api-key]
  (return
   (-/plisio-query "fee_commission" [currency-id] m api-key)))

(defn.lua ^{:arglists '([{:keys [currency order_name order_number
                                  amount description email expire_min]}
                         api-key])}
  new-invoice
  "creates a new invoice"
  {:added "4.0"}
  [m api-key]
  (return
   (-/plisio-query "new_invoice" [] m api-key)))

(defn.lua ^{:arglists '([{:keys [currency type to amount feePlan]}
                          api-key])}
  withdrawal
  "creates a new withdrawal action"
  {:added "4.0"}
  [m api-key]
  (return
   (-/plisio-query "withdrawal" [] m api-key)))

(def.lua MODULE (!:module))
