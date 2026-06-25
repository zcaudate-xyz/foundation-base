(ns xt.db.system.impl-supabase-realtime-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.impl-supabase-realtime :as realtime]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"}
(fact "returns the realtime client for an id"

  (!.js
   (var impl {"state" {"realtime" {"default" {"id" "default"}}}})
   (realtime/get-realtime impl "default"))
  => {"id" "default"}

  (!.js
   (var impl {"state" {"realtime" {}}})
   (realtime/get-realtime impl "default"))
  => nil)

^{:refer xt.db.system.impl-supabase-realtime/ensure-realtime :added "4.1"}
(fact "creates a realtime client wrapper if missing"

  (!.js
   (var impl {"state" {"realtime" {}}})
   (var rt (realtime/ensure-realtime impl "default"))
   (var stored (realtime/get-realtime impl "default"))
   {"id" (xt/x:get-key rt "id")
    "client" (xt/x:get-key rt "client")
    "stored_id" (xt/x:get-key stored "id")
    "stored_tag" (xt/x:get-key stored "::")})
  => {"id" "default"
      "client" nil
      "stored_id" "default"
      "stored_tag" "xt.db.system.impl_supabase_realtime/RealtimeClient"})

^{:refer xt.db.system.impl-supabase-realtime/shared-message-handler :added "4.1"}
(fact "routes xt.db/event broadcast payloads to the topic callback"

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (var handler (realtime/shared-message-handler realtime))
   (handler (xt/x:json-encode [nil nil "User:u-1" "broadcast"
                               {"event" "xt.db/event"
                                "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}]))
   captured)
  => [{"db/sync" {"User" [{"id" "u-1"}]}}]

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (var handler (realtime/shared-message-handler realtime))
   (handler (xt/x:json-encode [nil nil "User:u-1" "broadcast"
                               {"event" "some-other-event"
                                "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}]))
   captured)
  => [])
