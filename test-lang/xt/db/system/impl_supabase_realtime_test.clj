(ns xt.db.system.impl-supabase-realtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.network :as network]))

(defn wait-for-postgrest
  []
  (network/wait-for-port
   (-> local-min/+config+ :api :hostname)
   (-> local-min/+config+ :api :port)))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[js.net.http-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (wait-for-postgrest)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-client
  []
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey (@! (-> local-min/+config+ :api :anon-key))}
    (addon/middleware-supabase))))

(defn.js default-impl
  []
  (var client (-/default-client))
  (return (main/create-impl "supabase"
                            (xt/x:get-key client "defaults")
                            nil
                            nil)))

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
(fact "connects to the local supabase realtime websocket"

  (notify/wait-on :js
    (var impl (-/default-impl))
    (var rt (realtime/ensure-realtime impl "default" {}))
    (var client (xt/x:get-key rt "client"))
    (promise/x:with-delay 500
      (fn []
        (var defaults (xt/x:get-key client "defaults"))
        (repl/notify {"connected" (xt/x:not-nil? (xt/x:get-key client "raw"))
                      "url" (xt/x:get-key defaults "url")}))))
  => (contains-in {"connected" true
                   "url" string?}))

^{:refer xt.db.system.impl-supabase-realtime/route-frame :added "4.1"}
(fact "routes xt.db/event broadcast payloads to the topic callback"

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (realtime/route-frame realtime
                         {"topic" "User:u-1"
                          "event" "broadcast"
                          "payload" {"event" "xt.db/event"
                                     "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}})
   captured)
  => [{"db/sync" {"User" [{"id" "u-1"}]}}]

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (realtime/route-frame realtime
                         {"topic" "User:u-1"
                          "event" "broadcast"
                          "payload" {"event" "some-other-event"
                                     "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}})
   captured)
  => [])


^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/resolve-api-key :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/resolve-auth-token :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/broadcast-join-payload :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/client-topics :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/topic-entry :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/on-open :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/subscribe :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/unsubscribe :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/publish :added "4.1"}
(fact "TODO")