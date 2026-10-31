(ns xt.db.system.impl-neon-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:langs [:python :ruby :dart :lua.nginx]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.backend-neon :as backend-neon]
             [xt.db.system.impl-neon :as neon]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as common]
             [xt.db.system.impl-memory :as memory]
             [xt.db.helpers.data-main-test :as sample]
             [xt.db.system.impl-neon-realtime :as realtime]
             [xt.db.system.impl-neon-session :as session]
             [xt.net.addon-neon :as addon]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]]})

(fact:global {:setup [(l/rt:restart)]
              :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-neon/impl-neon :added "4.2"}
(fact "creates a Neon implementation"
  (l/with:global-init
    (!.js (. (neon/impl-neon nil nil nil) ["::"])))
  => "xt.db.system.impl_neon/ImplNeon")

^{:refer xt.db.system.impl-neon/response-data :added "4.2"}
(fact "returns decoded data from successful responses"
  (!.js (neon/response-data {"status" 200 "body" [{"id" 1}]}))
  => [{"id" 1}])

^{:refer xt.db.system.main/create-impl :id neon-factory}
(fact "factory constructs the HTTP Neon backend"
  (l/with:global-init
    (!.js (. (main/create-impl "neon" {"data_url" "https://example.test"} nil nil {"neon" (xt.db.system.backend-neon/provider)}) ["::"])))
  => "xt.db.system.impl_neon/ImplNeon")

^{:refer xt.db.system.impl-neon/request-input}
(fact "joins the service URL and preserves literal rest paths inside query values"
  (!.js (neon/request-input
    {"client" {"defaults" {"data_url" "https://example.test/db/rest/v1/" "token" "jwt"}}}
    {"path" "/rest/v1/Thing?value=eq./rest/v1/" "method" "GET"}))
  => {"path" "/rest/v1/Thing?value=eq./rest/v1/" "method" "GET"
      "url" "https://example.test/db/rest/v1/Thing?value=eq./rest/v1/"
      "headers" {"Accept" "application/json" "Content-Type" "application/json" "Authorization" "Bearer jwt"}})

^{:refer xt.db.system.impl-neon/cmd-rpc-call-async}
(fact "RPC maps positional arguments and schema headers"
  (!.js (var command (neon/cmd-rpc-call-async nil
    {"id" "thing-update" "schema" "app" "input" [{"symbol" "i_id"} {"name" "i_value"}]}
    [7 "hello"]))
    (xt/x:set-key command "body" (xt/x:json-decode (. command ["body"])))
    command)
  => {"path" "/rpc/thing_update" "method" "POST"
      "headers" {"Content-Profile" "app" "Accept-Profile" "app"}
      "body" {"i_id" 7 "i_value" "hello"}})

^{:refer xt.db.system.impl-neon/response-data :id preserve-data}
(fact "does not unwrap legitimate data columns"
  (!.js (neon/response-data {"status" 200 "body" {"data" [1] "count" 1}}))
  => {"data" [1] "count" 1})

^{:refer xt.net.addon-neon/response-data :id reject-failure}
(fact "HTTP and transport failures reject without exposing credentials"
  (!.js (var rejected [])
    (var responses [{"status" 401 "body" {"secret" "hidden"}} {"status" nil}])
    (xt/for:array [response responses]
      (try (addon/response-data response) (xt/x:arr-push rejected false)
        (catch err (xt/x:arr-push rejected true))))
    rejected)
  => [true true])

^{:refer xt.db.system.impl-neon-realtime/current?}
(fact "obsolete connections cannot apply responses"
  (!.js (var old {"active" true "generation" 1})
    (realtime/current? {"state" {"connections" {"c" {"active" true "generation" 2}}}} "c" old))
  => false)

^{:refer xt.db.system.impl-neon-session/set-session}
(fact "session renewal preserves the opaque bridge credential"
  (!.js (var impl {"state" {} "client" {"defaults" {"bridge_token" "opaque"}}})
    (session/set-session impl {"user" {"id" "u1"} "access_token" "renewed"})
    (. (. impl ["client"]) ["defaults"]))
  => {"bridge_token" "opaque" "token" "renewed"})

^{:refer xt.db.system.impl-neon-realtime/sequence-after?}
(fact "compares bigint sequences without loss of precision"
  (!.js [(realtime/sequence-after? "10" "9")
         (realtime/sequence-after? "9007199254740993" "9007199254740992")
         (realtime/sequence-after? "9" "10")
         (realtime/sequence-after? "10" "10")])
  => [true true false false])

^{:refer xt.db.system.impl-neon-realtime/apply-events}
(fact "event replay updates the cache and notifies listeners once"
  (l/with:global-init
    (!.js
      (var cache (memory/impl-memory sample/Schema sample/SchemaLookup))
      (var impl {"metadata" {"caching_fn" (fn [] (return cache))}})
      (var entry {"seen" {}})
      (var calls [])
      (common/add-db-listener cache "test" {"guard" {"UserAccount" true}
        "callback" (fn [event] (xt/x:arr-push calls event))})
      (var batch {"cursor" "next" "events" [{"topic" "t" "seq" "1"
        "payload" {"db/sync" {"UserAccount" [sample/RootUser]}}}]})
      (realtime/apply-events impl entry batch)
      (realtime/apply-events impl entry batch)
      [(xt/x:len calls) (. entry ["cursor"])
       (memory/pull cache ["UserAccount" {"id" "00000000-0000-0000-0000-000000000000"} ["nickname"]])]))
  => [1 "next" [{"nickname" "root"}]])
