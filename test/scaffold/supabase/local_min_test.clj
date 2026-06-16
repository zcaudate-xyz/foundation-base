(ns scaffold.supabase.local-min-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v1 :as scratch]
             [postgres.sample.scratch-v0 :as scratch-v0]]
   :config {:host   (-> local-min/+config+ :db :host)
            :port   (-> local-min/+config+ :db :port)
            :user   (-> local-min/+config+ :db :user)
            :pass   (-> local-min/+config+ :db :password)
            :dbname (-> local-min/+config+ :db :database)
            :startup  local-min/start-supabase
            :teardown local-min/stop-supabase}})

^{:refer scaffold.supabase.local-min/start-supabase :added "4.1"}
(fact "starts the supabase")

^{:refer scaffold.supabase.local-min/stop-supabase :added "4.1"}
(fact "stops the supabase")


^{:refer scaffold.supabase.local-min/shutdown-supabase :added "4.1"}
(fact "TODO")