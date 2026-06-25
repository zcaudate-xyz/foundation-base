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
             [js.net.ws-native :as js-ws]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.addon-supabase :as addon]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

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


^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/create-realtime :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/set-websocket :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/get-auth-token :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/join-topic-payload :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/join-topic :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/join- :added "4.1"}
(fact "TODO")