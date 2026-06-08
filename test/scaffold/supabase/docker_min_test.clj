(ns scaffold.supabase.docker-min-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v1 :as scratch]
             [postgres.sample.scratch-v0 :as scratch-v0]]
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :teardown docker-min/stop-supabase}})

^{:refer scaffold.supabase.docker-min/start-supabase :added "4.1"}
(fact "starts the supabase")

^{:refer scaffold.supabase.docker-min/stop-supabase :added "4.1"}
(fact "stops the supabase")
