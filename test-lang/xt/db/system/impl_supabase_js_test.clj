(ns xt.db.system.impl-supabase-js-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

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
   :require [[js.lib.supabase :as supabase]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase-js :as impl]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js wrap-supabase-fetch
  [schema]
  (return
   (fn [url init]
     (var input (or init {}))
     (var headers (or (xt/x:get-key input "headers") {}))
     (:= input (xt/x:obj-assign
                input
                {"headers" (xt/x:obj-assign headers
                                             {"Content-Profile" schema
                                              "Accept-Profile" schema})}))
     (return (fetch url input)))))

(defn.js default-client
  [apikey]
  (return
   (supabase/createSupabaseClient
    (xt/x:cat "http://127.0.0.1:"
              (@! (-> local-min/+config+ :api :port)))
    apikey
    {"db" {"schema" "scratch_v0"}
     "global" {"fetch" (-/wrap-supabase-fetch "scratch_v0")}
     "auth" {"autoRefreshToken" false
              "persistSession" false
              "detectSessionInUrl" false}})))

^{:refer xt.db.system.impl-supabase-js/pull-async :added "4.1"}
(fact "pulls data through supabase-js"
  
  (notify/wait-on :js
    (-> (impl/impl-supabase-js
         (-/default-client (@! (-> local-min/+config+ :api :service-key)))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/pull-async ["Log"])
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      [{"author_id" nil,
        "id" string?
        "message" string?}]))

^{:refer xt.db.system.impl-supabase-js/rpc-call-async :added "4.1"}
(fact "performs an rpc call through supabase-js"

  (notify/wait-on :js
    (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))))
    (-> (impl/impl-supabase-js
         client
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/pull-async ["Log"])
        (promise/x:promise-then
         (fn [_]
           (impl/rpc-call-async  {:input []
                                  :return "text"
                                  :schema "scratch_v0"
                                  :id "ping"
                                  :flags {}}
                                 [])))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  => "pong")

^{:refer xt.db.system.impl-supabase-js/impl-supabase-js :added "4.1"}
(fact "creates a supabase-js implementation"
  (!.js
    (xt/x:is-object?
     (impl/impl-supabase-js
      (-/default-client (@! (-> local-min/+config+ :api :service-key)))
      (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
      (@! (pg/bind-app (pg/app "scratch_v0"))))))
  => true)
