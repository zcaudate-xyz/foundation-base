^{:seedgen/skip true}
(ns xt.db.system.impl-supabase-test
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
   :require [[js.net.http-fetch :as js-fetch]
             [xt.net.http-fetch :as http-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-supabase :as impl]
             [xt.db.system.impl-supabase-session :as session]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

(defn.js default-client
  [apikey token]
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey apikey
     :token token}
    (addon/middleware-supabase))))

^{:refer xt.db.system.impl-supabase/cmd-pull-async :added "4.1"}
(fact "cmd-pull-async builds a pgrest GET request"
  (!.js
   (impl/cmd-pull-async
    (impl/impl-supabase
     (js-fetch/create
      {:host (@! (-> local-min/+config+ :api :hostname))
       :port (@! (-> local-min/+config+ :api :port))
       :apikey (@! (-> local-min/+config+ :api :service-key))}
      (addon/middleware-supabase))
     (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
     (@! (pg/bind-app (pg/app "scratch_v0")))
     {})
    ["Log"]))
  => (contains-in
      {"path" string?
       "method" "GET"
       "headers" map?}))

^{:refer xt.db.system.impl-supabase/pull-async :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]} 
(fact "pulls data "

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
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
      [{"message" "hello", "author_id" nil, "id" string?}])
  
  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
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

^{:refer xt.db.system.impl-supabase/cmd-rpc-call-async :added "4.1"}
(fact "cmd-rpc-call-async builds a supabase rpc POST request"
  (!.js
   (impl/cmd-rpc-call-async
    nil
    {:input [{:symbol "i_message" :type "text"}]
     :return "jsonb"
     :schema "scratch_v0"
     :id "log_append_public"
     :flags {}}
    ["hello"]
    {:token "abc"}))
  => (contains-in
      {"path" "/rest/v1/rpc/log_append_public"
       "method" "POST"
       "headers" map?
       "body" string?}))

^{:refer xt.db.system.impl-supabase/rpc-call-async :added "4.1"}
(fact "performs an rpc call"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
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
  => "pong"

  (notify/wait-on :js
    (-> (impl/impl-supabase
         (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
         (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
         (@! (pg/bind-app (pg/app "scratch_v0"))))
        (impl/rpc-call-async  {:input [{:symbol "i_message" :type "text"}]
                               :return "jsonb"
                               :schema "scratch_v0"
                               :id "log_append_public"
                               :flags {}}
                              ["hello"]
                              {:token (@! (-> local-min/+config+ :api :service-key))})
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify (. out message))))))
  => (contains-in
      {"author_id" nil,
       "id" string?
       "message" "hello"}))

^{:refer xt.db.system.impl-supabase/impl-supabase :added "4.1"}
(fact "creates a supabase implementation"

      (!.js
    (impl/impl-supabase
     (js-fetch/create
          {:host (@! (-> local-min/+config+ :api :hostname))
           :port (@! (-> local-min/+config+ :api :port))
           :apikey (@! (-> local-min/+config+ :api :service-key))}
          (addon/middleware-supabase))
     (@! (pg/bind-schema (:schema (pg/app "scratch_v0"))))
     (@! (pg/bind-app (pg/app "scratch_v0")))
     {}))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" {},
       "::" "xt.db.system.impl_supabase/ImplSupabase",
       "::/protocols" ["xt.db.system.impl_common/ISourceRemote"],
       "client" map?
       "state" {"session" nil?, "auto_refresh" nil?, "realtimes" map?}}))
