(ns xt.db.system.client-supabase-test
  (:require [hara.lang :as l])
  (:require [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.client-supabase :as client]
             [xt.protocol.impl.client-fetch :as fetch]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.client-supabase/client? :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/unsupported-op :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/raw-client :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-transport :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-base-url :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-api-key :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-auth-token :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-schema-name :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/create-scaffold :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/resolve-request-headers :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/prepare-request :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/unwrap-response :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/client :added "4.1"}
(fact "creates a tagged supabase client"

  (!.js
    (var db (client/client {"base_url" "https://example.supabase.co"
                            "client" {"transport"
                                      (fetch/client-create
                                       {"request" (fn [input _opts]
                                                    (return input))}
                                       nil)}}))
    (. db ["::"]))
  => "db.client.supabase")

^{:refer xt.db.system.client-supabase/resolve-client :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/pull-sync :added "4.1"}
(fact "rejects sync pulls for the read-only async client"

  (!.js
    (client/pull-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     ["Currency" ["id"]]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/pull :added "4.1"}
(fact "compiles tree ir and shorthand query forms into PostgREST requests"

  (notify/wait-on :js
    (var seen {"url" nil})
    (var transport
         (fetch/client-create
          {"request" (fn [input _opts]
                       (xt/x:set-key seen "url" (. input ["url"]))
                       (return {"body" {"data" [{"id" "USD"}]}}))}
          nil))
    (promise/x:promise-catch
     (promise/x:promise-then
      (client/pull
       (client/client {"base_url" "https://example.supabase.co"
                       "schema_name" "public"
                       "client" {"transport" transport}})
       sample/Schema
       ["Currency"
        {"where" [{"id" "USD"}]
         "data" ["id"]
         "links" []
         "custom" []}]
       {})
      (fn [rows]
        (repl/notify [(. seen ["url"]) rows])))
     (fn [err]
       (repl/notify err))))
  => ["https://example.supabase.co/rest/v1/Currency?select=id&id=eq.USD"
     [{"id" "USD"}]]

  (notify/wait-on :js
    (var seen {"url" nil})
    (var transport
        (fetch/client-create
         {"request" (fn [input _opts]
                      (xt/x:set-key seen "url" (. input ["url"]))
                      (return {"body" {"data" [{"id" "USD"}]}}))}
         nil))
    (promise/x:promise-catch
     (promise/x:promise-then
     (client/pull
      (client/client {"base_url" "https://example.supabase.co"
                      "schema_name" "public"
                      "client" {"transport" transport}})
      sample/Schema
      ["Currency"
       {"id" "USD"}
       ["id"]]
      {})
     (fn [rows]
       (repl/notify [(. seen ["url"]) rows])))
     (fn [err]
      (repl/notify err))))
  => ["https://example.supabase.co/rest/v1/Currency?select=id&id=eq.USD"
     [{"id" "USD"}]])

^{:refer xt.db.system.client-supabase/process-event-sync :added "4.1"}
(fact "rejects write and delete operations"

  (!.js
    (client/process-event-sync
     (client/client {"base_url" "https://example.supabase.co"})
     "add"
     {}
     sample/Schema
     sample/SchemaLookup
     {}))
  => (throws)

  (!.js
    (client/process-event-remove
     (client/client {"base_url" "https://example.supabase.co"})
     "remove"
     {}
     sample/Schema
     sample/SchemaLookup
     {}))
  => (throws)

  (!.js
    (client/delete-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws)

  (!.js
    (client/delete
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws)

  (!.js
    (client/clear
     (client/client {"base_url" "https://example.supabase.co"})))
  => (throws))

^{:refer xt.db.system.client-supabase/process-event-remove :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/delete-sync :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/delete :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.client-supabase/clear :added "4.1"}
(fact "TODO")