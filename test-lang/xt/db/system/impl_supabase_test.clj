(ns xt.db.system.impl-supabase-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase :as impl]
             [xt.lib.supabase :as supabase]
             [xt.protocol.impl.client-fetch :as fetch]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-supabase/supabase-client :added "4.1"}
(fact "creates the thin supabase client record with stored context"

  (!.js
   (var client
        (impl/supabase-client
         sample/Schema
         sample/SchemaLookup
         {}
         {"base_url" "https://api.test"
          "api_key" "key-1"}))
   {"tag" (. client ["::"])
    "has_instance" (xt/x:has-key? client "instance")
    "base_url" (. (. client ["settings"]) ["base_url"])})
  => {"tag" "db.client.supabase"
      "has_instance" false
      "base_url" "https://api.test"})

^{:refer xt.db.system.impl-supabase/supabase-client-init :added "4.1"}
(fact "supabase-client-init stores the underlying supabase client"

  (notify/wait-on :js
    (promise/x:promise-then
     (impl/supabase-client-init
      (impl/supabase-client
       sample/Schema
       sample/SchemaLookup
       {}
       {"base_url" "https://api.test"
        "api_key" "key-1"
        "transport"
        (fetch/client-create
         {"request" (fn [input _opts]
                      (return {"body" {"data" input}}))}
         nil)}))
     (fn [client]
       (repl/notify {"tag" (. client ["::"])
                     "instance" (supabase/client? (. client ["instance"]))}))))
  => {"tag" "db.client.supabase"
      "instance" true})

^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"}
(fact "pull-async compiles tree ir into PostgREST requests"

  (notify/wait-on :js
    (var seen {"url" nil})
    (var client
         (impl/supabase-client
          sample/Schema
          sample/SchemaLookup
          {}
          {"base_url" "https://api.test"
           "api_key" "key-1"
           "transport"
           (fetch/client-create
            {"request" (fn [input _opts]
                         (xt/x:set-key seen "url" (. input ["url"]))
                         (return {"body" {"data" [{"id" "USER-0"}]}}))}
            nil)}))
    (promise/x:promise-then
     (impl/pull-async
      client
      ["UserAccount" {"where" [] "data" ["id"] "links" [] "custom" []}])
     (fn [rows]
       (repl/notify [(. seen ["url"]) rows]))))
  => ["https://api.test/rest/v1/UserAccount?select=id"
      [{"id" "USER-0"}]])

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "rpc-call-async compiles snake_case PostgREST rpc requests"

  (notify/wait-on :js
    (var seen {"url" nil
               "method" nil
               "body" nil})
    (var client
         (impl/supabase-client
          sample/Schema
          sample/SchemaLookup
          {}
          {"base_url" "https://api.test"
           "api_key" "key-1"
           "transport"
           (fetch/client-create
            {"request" (fn [input _opts]
                         (xt/x:set-key seen "url" (. input ["url"]))
                         (xt/x:set-key seen "method" (. input ["method"]))
                         (xt/x:set-key seen "body" (. input ["body"]))
                         (return {"body" {"data" {"total" 2}}}))}
            nil)}))
    (promise/x:promise-then
     (impl/rpc-call-async
      client
      "list-orders"
      {"status" "open"})
     (fn [out]
       (repl/notify [(. seen ["url"])
                     (. seen ["method"])
                     (. seen ["body"])
                     out]))))
  => ["https://api.test/rest/v1/rpc/list_orders"
      "POST"
      "{\"status\":\"open\"}"
      {"total" 2}])
