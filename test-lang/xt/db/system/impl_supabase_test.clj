(ns xt.db.system.impl-supabase-test
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
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}}))

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[js.net.http-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase :as impl]
             [xt.net.lib-supabase :as lib-supabase]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]}
(fact "pulls data "
  
  (notify/wait-on :js
    (-> (impl/impl-supabase
         (lib-supabase/create-client
          (js-fetch/create-methods)
          "127.0.0.1"
          (@! (-> docker-min/+config+ :api :port))
          false
          ""
          (@! (-> docker-min/+config+ :api :service-key)))
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
        "message" "hello"}]))

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "performs an rpc call"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (lib-supabase/create-client
          (js-fetch/create-methods)
          "127.0.0.1"
          (@! (-> docker-min/+config+ :api :port))
          false
          ""
          (@! (-> docker-min/+config+ :api :anon-key)))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/rpc-call-async  {:input []
                               :return "text"
                               :schema "scratch_v0"
                               :id "ping"
                               :flags {}}
                              [])
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  ;; "fetch failed"
  => "pong"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (lib-supabase/create-client
          (js-fetch/create-methods)
          "127.0.0.1"
          (@! (-> docker-min/+config+ :api :port))
          false
          ""
          (@! (-> docker-min/+config+ :api :service-key)))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/rpc-call-async  {:input [{:symbol "i_message" :type "text"}]
                               :return "jsonb"
                               :schema "scratch_v0"
                               :id "log_append_public"
                               :flags {}}
                              ["hello"]
                              {:token (@! (-> docker-min/+config+ :api :service-key))})
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  ;; {"message" "permission denied for function log_append_public", "hint" nil, "details" nil, "code" "42501"}
  ;; "fetch failed"
  => (contains-in
      {"author_id" nil,
       "id" string?
       "message" "hello"}))

^{:refer xt.db.system.impl-supabase/impl-supabase :added "4.1"}
(fact "TODO")
