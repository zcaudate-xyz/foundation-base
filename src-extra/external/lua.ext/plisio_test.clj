(ns lua.ext.plisio-test
  (:use code.test)
  (:require [lua.ext.plisio :refer :all]))

^{:refer lua.ext.plisio/plisio-query :added "4.0"}
(fact "performs a plisio query")

^{:refer lua.ext.plisio/tx-search :added "4.0"}
(fact "performs transation search")

^{:refer lua.ext.plisio/tx-detail :added "4.0"}
(comment "gets transaction detail"
  ^:hidden
  
  {"data" {"id" "631200f0561dbf5bbc778c90",
           "shop_id" "62bdfe461a289f727940d84c",
           "status" "new",
           "tx_url" nil,
           "type" "invoice",
           "user_id" 10705},
   "status" "success"})

^{:refer lua.ext.plisio/get-currencies :added "4.0"}
(fact "gets all currencies")

^{:refer lua.ext.plisio/get-currencies-exchange :added "4.0"}
(fact "gets currencies for exchange fiat currency")

^{:refer lua.ext.plisio/get-balance :added "4.0"}
(fact "gets the current balance of crypto")

^{:refer lua.ext.plisio/get-fee-plan :added "4.0"}
(fact "gets the fee plan")

^{:refer lua.ext.plisio/get-fee-estimate :added "4.0"}
(fact "gets the fee estimate")

^{:refer lua.ext.plisio/get-fee-commission :added "4.0"}
(fact "gets the fee commission")

^{:refer lua.ext.plisio/new-invoice :added "4.0"}
(comment "creates a new invoice"
  ^:hidden
  
  {"status" "success",
   "data" {"invoice_url" "https://plisio.net/invoice/6311e861b37602586b795b07",
           "txn_id" "6311e861b37602586b795b07"}})

^{:refer lua.ext.plisio/withdrawal :added "4.0"}
(fact "creates a new withdrawal action")


(comment
  

  (mapv symbol ["addresses"
                             "amounts"
                             "feePlan"
                "amount"])
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/get-balance "DOGE" (@! +plisio-statstrade-key+))))

  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/get-fee-plan "DOGE" (@! +plisio-statstrade-key+))))

  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/get-fee-plan "BTC" (@! +plisio-statstrade-key+))))
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/get-fee-plan "ETH" (@! +plisio-statstrade-key+))))
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/tx-search {:type "invoice"
                   :status "new"}
                  (@! +plisio-statstrade-key+))))
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (-/new-invoice {:type "invoice"
                   :status "new"}
                  (@! +plisio-statstrade-key+))))
  
  
  (-/plisio-query
   "tx_search"
   []
   {:limit 1})
  
  (-/plisio-query
   "search")

  (def +res+
    (notify/wait-on [:lua 5000]
      (var '[res err] (-/plisio-query
                       "tx_search"
                       []
                       {:limit 1}
                       (@! +plisio-statstrade-key+)))
      (:= RES res)
      (repl/notify
       [(:? res
            (k/obj-pick res ["body" "status" "reason"]))])))
  
  (!.lua
   (k/get-spec RES))
  
  )






(comment
  {"api_key" (@! +plisio-statstrade-key+)
                                             
                                             "amount" 10
                                             "currency" "DOGE"
                                             "email" "zcaudate@outlook.com"
                                             "order_name" "test_order"
                                             "order_number" (@! (str (h/uuid)))})













(comment
  (def +hello+
    (notify/wait-on [:lua 5000]
                    (var client (http/new))
                    (var '[res err] (http/request-uri client
                                                      "https://plisio.net/api/v1/invoices/new"
                                                      {:query {"api_key" (@! +plisio-statstrade-key+)
                                                               
                                                               "amount" 10
                                                               "currency" "DOGE"
                                                               "email" "zcaudate@outlook.com"
                                                               "order_name" "test_order"
                                                               "order_number" (@! (str (h/uuid)))}
                                                       :ssl-verify false}))
                    
                    (repl/notify (k/obj-pick res ["body" "status"]))))


  (def +tx+
    (!.lua
     (var client (http/new))
     (var '[res err] (http/request-uri client
                                       "https://plisio.net/api/v1/invoices/new"
                                       {:query {"api_key" (@! +plisio-statstrade-key+)
                                                
                                                "amount" 10
                                                "currency" "DOGE"
                                                "email" "zcaudate@outlook.com"
                                                "order_name" "test_order"
                                                "order_number" (@! (str (h/uuid)))}
                                        :ssl-verify false}))
     (k/obj-pick res ["body" "status"])))

  (def +res+
    (!.lua
     (var client (http/new))
     (var '[res err] (http/request-uri client
                                       "https://plisio.net/api/v1/operations"
                                       {:query {"api_key" (@! +plisio-statstrade-key+)
                                                "search"  "63103b866e295627da5e630d"
                                                }
                                        :ssl-verify false}))
     (k/obj-pick res ["body" "status"])))

  (def +res+
    (notify/wait-on [:lua 5000]
                    (var client (http/new))
                    (var '[res err] (http/request-uri client
                                                      "https://plisio.net/api/v1/operations"
                                                      {:query {"api_key" (@! +plisio-statstrade-key+)
                                                               "limit" 50
                                                               "type" "cash_out"}
                                                       :ssl-verify false}))
                    
                    (repl/notify (k/obj-pick res ["body" "status"]))))


  (update-in +res+ ["body"] std.json/read)


  (!.lua
   (var client (http/new))
   (http/request-uri client "https://plisio.net/" {}))

  (!.lua
   (var client (http/new))
   (var '[res err] (http/request-uri client "https://www.baidu.com" {:ssl-verify false}))
   (k/obj-pick res ["body" "status"])))


