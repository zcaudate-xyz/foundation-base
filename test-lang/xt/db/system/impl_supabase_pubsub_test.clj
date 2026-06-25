(ns xt.db.system.impl-supabase-pubsub-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.impl-supabase-pubsub :as pubsub]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-supabase-pubsub/broadcast->event-vectors :added "4.1"}
(fact "converts xt.db/event broadcast payloads to event vectors"

  (!.js
   (pubsub/broadcast->event-vectors
    {"db/sync" {"Currency" [{"id" "USD"}]}
     "db/remove" {"Currency" ["XLM"]}}))
  => [["add" {"Currency" [{"id" "USD"}]}]
      ["remove" {"Currency" ["XLM"]}]]

  (!.js
   (pubsub/broadcast->event-vectors
    {"db/sync" {"User" [{"id" "u-1"}]}}))
  => [["add" {"User" [{"id" "u-1"}]}]])

^{:refer xt.db.system.impl-supabase-pubsub/shared-message-handler :added "4.1"}
(fact "routes xt.db/event broadcast payloads to the topic callback"

  (!.js
   (var captured [])
   (var impl {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var handler (pubsub/shared-message-handler impl))
   (handler (xt/x:json-encode [nil nil "User:u-1" "broadcast"
                               {"event" "xt.db/event"
                                "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}]))
   captured)
  => [["add" {"User" [{"id" "u-1"}]}]]

  (!.js
   (var captured [])
   (var impl {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var handler (pubsub/shared-message-handler impl))
   (handler (xt/x:json-encode [nil nil "User:u-1" "broadcast"
                               {"event" "some-other-event"
                                "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}]))
   captured)
  => [])
