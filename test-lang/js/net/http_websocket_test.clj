(ns js.net.http-websocket-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> docker-min/+config+ :db :host)
              :port   (-> docker-min/+config+ :db :port)
              :user   (-> docker-min/+config+ :db :user)
              :pass   (-> docker-min/+config+ :db :password)
              :dbname (-> docker-min/+config+ :db :database)
              :startup  docker-min/start-supabase
              :shutdown docker-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[js.net.http-websocket :as js-ws]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-phoenix :as phoenix]
             [xt.net.http-websocket :as websocket]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer js.net.http-websocket/connect-ws :added "4.1"}
(fact "connects to the local Supabase realtime websocket and receives a Phoenix broadcast")


^{:refer js.net.http-websocket/disconnect-ws :added "4.1"}
(fact "TODO")

^{:refer js.net.http-websocket/send-ws :added "4.1"}
(fact "TODO")

^{:refer js.net.http-websocket/add-listeners-ws :added "4.1"}
(fact "TODO")

^{:refer js.net.http-websocket/create :added "4.1"}
(fact "TODO")
