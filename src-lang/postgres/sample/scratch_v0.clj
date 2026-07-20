^{:no-test true}
(ns postgres.sample.scratch-v0
  (:require [hara.lang :as l]
            [postgres.core :as pg :refer [defret.pg defsel.pg]]
            [postgres.core.supabase :as s]))

(l/script :postgres
  {:require [[postgres.core :as pg]
             [postgres.core.supabase :as s]]
   :import [["uuid-ossp"]]
   :static {:application ["scratch_v0"]
            :seed ["scratch_v0"]
            :all {:schema ["scratch_v0"]}}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(deftype.pg ^{:api/meta {:sb/access {:admin :all
                                     :auth :select
                                     :anon :select}}
              :public true}
  Log
  "Append-only log rows exposed read-only through Supabase."
  [:id {:type :uuid :primary true
        :sql {:default (pg/uuid-generate-v4)}}
   :message {:type :text :required true}
   :author-id {:type :uuid}])

(defsel.pg ^{:- [-/Log]
             :args []
             :api/view true}
  log-all)

(defret.pg ^{:- [-/Log]
             :args []
             :api/view true}
  log-default
  [:uuid i-log-id]
  #{:*/standard})

(defn.pg ^{:%% :sql
           :- [:text]
           :props [:immutable :parallel-safe]
           :api/flags []
           :api/meta {:sb/grant :all}}
  ping
  "Returns a stable scratch-v0 ping response."
  {:added "4.1.4"}
  []
  "pong")

(defn.pg ^{:%% :sql
           :- [:text]
           :props [:immutable :parallel-safe]
           :api/meta {:sb/grant :all}}
  echo
  "Returns a stable scratch-v0 ping response."
  {:added "4.1.4"}
  [:text i-input]
  i-input)

(defn.pg ^{:props [:security :definer]
           :api/meta {:sb/grant :auth
                      :mcp {:name "log_append_public"
                            :title "Append public log"
                            :description "Appends a log row for the current authenticated user."}}}
  log-append-public
  "Appends a log row for the current authenticated user."
  {:added "4.1.4"}
  [:text i-message]
  (let [o-log (pg/t:insert -/Log
                {:message i-message})]
    (return o-log)))

(defn.pg ^{:props [:security :definer]
           :api/flags []
           :api/meta {:sb/grant :auth}}
  log-append
  "Appends a log row for the current authenticated user."
  {:added "4.1.4"}
  [:text i-message]
  (let [o-log (pg/t:insert -/Log
                {:message i-message
                 :author-id (s/auth-uid)})]
    (return o-log)))
