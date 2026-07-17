^{:seedgen/skip true}
(ns xt.db.system.impl-supabase-session-test
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
             [xt.db.system.main :as main]
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


^{:refer xt.db.system.impl-supabase-session/get-session :added "4.1"}
(fact "get-session reads the session from impl state"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   (session/set-session source {"email" "test@example.com"})
   (session/get-session source))
  => {"email" "test@example.com"})

^{:refer xt.db.system.impl-supabase-session/set-session :added "4.1"}
(fact "set-session stores the session in impl state and returns it"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   {"returned" (session/set-session source {"refresh_token" "abc"})
    "stored" (xtd/get-in source ["state" "session"])})
  => {"returned" {"refresh_token" "abc"}
      "stored" {"refresh_token" "abc"}})

^{:refer xt.db.system.impl-supabase-session/refresh-session :added "4.1"}
(fact "refresh-session returns nil when no refresh token is present"
  (notify/wait-on :js
    (var source (main/create-impl "supabase" {} nil nil))
    (-> (session/refresh-session source)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => nil)

^{:refer xt.db.system.impl-supabase-session/auto-refresh-interval :added "4.1"}
(fact "auto-refresh-interval derives delay from expires_in or opts"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   (session/set-session source {"expires_in" 120})
   (var source-opts (main/create-impl "supabase" {} nil nil))
   (xtd/set-in source-opts ["opts" "auto_refresh_interval"] 10000)
   [(session/auto-refresh-interval source)
    (session/auto-refresh-interval source-opts)])
  => [60000 10000])

^{:refer xt.db.system.impl-supabase-session/auto-refresh-fn :added "4.1"}
(fact "auto-refresh-fn returns nil when no current id is set"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   (session/auto-refresh-fn source 100 "new-id"))
  => nil)

^{:refer xt.db.system.impl-supabase-session/auto-refresh-stop :added "4.1"}
(fact "auto-refresh-stop clears the current refresh id"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   (xtd/set-in source ["state" "auto_refresh" "current"] "abc")
   (session/auto-refresh-stop source)
   (xtd/get-in source ["state" "auto_refresh" "current"]))
  => nil)

^{:refer xt.db.system.impl-supabase-session/auto-refresh-start :added "4.1"}
(fact "auto-refresh-start sets and returns the current refresh id"
  (!.js
   (var source (main/create-impl "supabase" {} nil nil))
   (session/set-session source {"refresh_token" "abc"})
   (xt/x:not-nil? (session/auto-refresh-start source {})))
  => true)
