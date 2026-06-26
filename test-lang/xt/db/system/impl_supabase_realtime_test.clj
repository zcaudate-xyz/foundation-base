(ns xt.db.system.impl-supabase-realtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.network :as network]
            [net.http.websocket :as ws]))

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
             [js.net.ws-native :as js-websocket]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common-ws :as common-ws]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.addon-supabase :as addon]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-impl
  [opts]
  (return
   (main/create-impl "supabase"
                     (xt/x:obj-assign (@! local-min/+config-supabase-anon+)
                                      opts)
                     -/Schema
                     -/SchemaLookup)))

^{:refer xt.db.system.impl-supabase-realtime/join-topic-payload :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"}
(fact "creates the connect-url"
  
  (!.js
    (realtime/prepare-connect-url
     (-/default-impl)
     {}))
  => #"ws://127.0.0.1:55121/realtime/v1/websocket")

^{:refer xt.db.system.impl-supabase-realtime/get-auth-token :added "4.1"}
(fact "TODO"

  (!.js
    (realtime/get-auth-token
     (-/default-impl)))
  )

^{:refer xt.db.system.impl-supabase-realtime/topic-join-payload :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/topic-leave-payload :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/create-realtime-on-message :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/create-realtime :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "creates a realtime connection"

  (let [p (promise)]
    (ws/websocket
     (!.js
       (realtime/prepare-connect-url
        (-/default-impl)
        {}))
     {:on-open (fn [& args]
                 (deliver p "opened"))})
    @p)
  => "opened"

  (notify/wait-on :js
    (var client (realtime/create-realtime
                 (-/default-impl)
                 (xts/str-rand 8)))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify "opened")))))
  => "opened"
  
  (notify/wait-on [:js 2000]
    (var client
         (js-websocket/create (@! local-min/+config-supabase-anon+)))
    (var joined false)
    (js-websocket/connect-ws client
                             {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                       (@! (-> local-min/+config+ :api :anon-key)))})
    (websocket/add-listeners
     client
     {"open"
      (fn [_]
        (phoenix/send-frame
         client
         (phoenix/make-frame-join
          {"config" {"broadcast" {"ack" false "self" false}}}
          {"topic" "realtime:room:send-join-test"
           "ref" "join-1"})))
      "message"
      (phoenix/wrap-phoenix
       {"phx_reply"
        (fn [frame]
          (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                     (not joined))
            (:= joined true)
            (websocket/disconnect client)
            (repl/notify frame)))})})
    true)
  => {"event" "phx_reply", "ref" "join-1", "payload" {"status" "ok", "response" {"postgres_changes" []}}, "topic" "realtime:room:send-join-test"})

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"}
(fact "gets a realtime connection"

  (!.js
    (realtime/get-realtime
     (-/default-impl)
     "hello"))
  => nil)

^{:refer xt.db.system.impl-supabase-realtime/set-realtime :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/ensure-realtime :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/add-realtime-callback :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime-callback :added "4.1"}
(fact "TODO")

^{:refer xt.db.system.impl-supabase-realtime/join-topic :added "4.1"}
(fact "TODO")

(comment
  


  )

(comment
  (common-ws/create-ws-client {}))