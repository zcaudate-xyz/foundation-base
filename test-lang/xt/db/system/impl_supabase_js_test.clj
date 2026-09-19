(ns xt.db.system.impl-supabase-js-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase-js :as impl]
             [xt.db.system.main :as main]]})

(defn.js fake-client
  []
  (var calls [])
  (var query {})
  (xt/x:set-key query "select"
                (fn [columns]
                  (xt/x:arr-push calls ["select" columns])
                  (return query)))
  (xt/x:set-key query "filter"
                (fn [column op value]
                  (xt/x:arr-push calls ["filter" column op value])
                  (return query)))
  (xt/x:set-key query "in"
                (fn [column values]
                  (xt/x:arr-push calls ["in" column values])
                  (return query)))
  (xt/x:set-key query "or"
                (fn [filters]
                  (xt/x:arr-push calls ["or" filters])
                  (return query)))
  (xt/x:set-key query "order"
                (fn [column options]
                  (xt/x:arr-push calls ["order" column options])
                  (return query)))
  (xt/x:set-key query "limit"
                (fn [count]
                  (xt/x:arr-push calls ["limit" count])
                  (return query)))
  (xt/x:set-key query "range"
                (fn [from to]
                  (xt/x:arr-push calls ["range" from to])
                  (return query)))
  (xt/x:set-key query "then"
                (fn [handler]
                  (return
                   (new Promise
                        (fn [resolve reject]
                          (resolve
                           (handler {"data" [{"id" "LOG-1"}]
                                     "error" nil})))))))
  (var client {})
  (xt/x:set-key client "from"
                (fn [table]
                  (xt/x:arr-push calls ["from" table])
                  (return query)))
  (xt/x:set-key client "schema"
                (fn [name]
                  (xt/x:arr-push calls ["schema" name])
                  (return client)))
  (xt/x:set-key client "rpc"
                (fn [name body]
                  (xt/x:arr-push calls ["rpc" name body])
                  (return
                   (new Promise
                        (fn [resolve reject]
                          (resolve
                           {"data" "ok"
                            "error" nil}))))))
  (return {"client" client
           "calls" calls}))

^{:refer xt.db.system.impl-supabase-js/query-builder
  :added "4.1.7"
  :id "query-builder-applies-native-filters"}
(fact "translates tree controls into Supabase-js builder calls"
  (!.js
   (var fake (-/fake-client))
   (var adapter (impl/impl-supabase-js (. fake ["client"]) {} {}))
   (impl/query-builder
    adapter
    {"table" "Topic"
     "select" "*"
     "filters" [{"path" "campaign_id"
                 "op" "eq"
                 "value" "campaign-1"}]
     "params" ["select=*"
               "campaign_id=eq.campaign-1"
               "order=created_at.desc"
               "limit=10"
               "offset=20"]})
   (. fake ["calls"]))
  => [["from" "Topic"]
      ["select" "*"]
      ["filter" "campaign_id" "eq" "campaign-1"]
      ["order" "created_at" {"ascending" false}]
      ["range" 20 29]])

^{:refer xt.db.system.impl-supabase-js/pull-async
  :added "4.1.7"
  :id "pull-async-uses-native-client"}
(fact "pull-async returns the data portion of a native Supabase response"
  (notify/wait-on :js
    (var fake (-/fake-client))
    (var adapter (impl/impl-supabase-js (. fake ["client"]) {} {}))
    (-> (impl/pull-async
         adapter
         ["Log" {"where" [{"message" ["eq" "hello"]}]}])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => [{"id" "LOG-1"}])

^{:refer xt.db.system.impl-supabase-js/rpc-call-async
  :added "4.1.7"
  :id "rpc-call-uses-native-client"}
(fact "rpc-call-async maps the xt.db RPC input to a native RPC body"
  (notify/wait-on :js
    (var fake (-/fake-client))
    (var adapter (impl/impl-supabase-js (. fake ["client"]) {} {}))
    (-> (impl/rpc-call-async
         adapter
         {"id" "topic_summary"
          "input" [{"symbol" "i_campaign_id"}]}
         ["campaign-1"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify (. fake ["calls"]))))))
  => [["rpc" "topic_summary" {"i_campaign_id" "campaign-1"}]])

^{:refer xt.db.system.main/create-impl
  :added "4.1.7"
  :id "create-native-supabase-impl"}
(fact "create-impl accepts an injected native Supabase client"
  (!.js
   (var fake (-/fake-client))
   (xtd/get-in
    (main/create-impl
     "supabase-js"
     {"client" (. fake ["client"])}
     {}
     {})
    ["::"]))
  => "xt.db.system.impl_supabase_js/ImplSupabaseJs")
