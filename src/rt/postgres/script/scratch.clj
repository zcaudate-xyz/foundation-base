(ns rt.postgres.script.scratch
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres :refer [defret.pg defsel.pg]]))

(l/script :postgres
  {:require [[rt.postgres :as pg]]
   :import  [["citext"]
             ["uuid-ossp"]]
   :config {:dbname "test-scratch"}
   :static {:application ["scratch"]
            :seed        ["scratch"]
            :all    {:schema   ["scratch"]}}})

(defn.pg as-array
  "returns a jsonb array"
  {:added "4.0"}
  [:jsonb input]
  (when (== input "{}")
    (return "[]"))
  (return input))

(defenum.pg ^{:final true}
  EnumStatus
  [:pending :error :success])

(def Id
  [:id  {:type :uuid :primary true
         :sql {:default '(rt.postgres/uuid-generate-v4)}}])

(def RecordType
  [:op-created     {:type :uuid    :scope :-/system}
   :op-updated     {:type :uuid    :scope :-/system}
   :time-created   {:type :long}
   :time-updated   {:type :long}
   :__deleted__    {:type :boolean :scope :-/hidden
                    :sql {:default false}}])

(def TrackingMin
  {:name  "min"
   :in   {:create {:op-created   :id
                   :op-updated   :id
                   :time-created :time
                   :time-updated :time}
          :modify {:op-updated   :id
                   :time-updated :time}}
   :ignore #{:delete}})

(deftype.pg ^{:track  [TrackingMin]
              :append [RecordType]
              :public true}
  TaskCache
  "constructs a task cache"
  {:added "4.0"}
  [:id        {:type :uuid :primary true 
               :web {:example "AUD"}
               :sql {:default (rt.postgres/uuid-generate-v4)}}])

(deftype.pg ^{:track   [TrackingMin]
              :prepend [[Id :id {:web {:example "00000000-0000-0000-0000-000000000000"}}]]
              :append  [RecordType]
              :public true}
  Task
  "constructs a task"
  {:added "4.0"}
  [:status   {:type :enum :required true :scope :-/info
              :enum {:ns -/EnumStatus}
              :web {:example "success"}}
   :name     {:type :text :required true
              :sql {:unique "default"
                    :index {:using :hash}}}
   :cache    {:type :ref :required true
              :ref {:ns -/TaskCache}}])

(deftype.pg ^{:track   [TrackingMin]
              :prepend [[Id :id {:web {:example "00000000-0000-0000-0000-000000000000"}}]]
              :append  [RecordType]
              :public true}
  Entry
  "construcs an entry"
  {:added "4.0"}
  [:name     {:type :text :required true
              :sql {:unique "default"
                    :index {:using :hash}}}
   :tags     {:type :array :required true
              :sql {:process -/as-array}}])

(defsel.pg ^{:- [-/Entry]
             :args []
             :api/view true}
  entry-all)

(defsel.pg ^{:- [-/Entry]
             :args [:text i-name]
             :api/view true}
  entry-by-name
  {:name i-name})

(defret.pg ^{:- [-/Entry]
             :args [] 
             :api/view true}
  entry-default
  [:uuid i-entry-id]
  #{:*/standard})

(defn.pg ^{:%% :sql
           :- [:citext]
           :props [:immutable :parallel-safe]}
  as-upper
  "converts to upper case"
  {:added "4.0"}
  [:citext input]
  (:citext (pg/upper (:text input))))

(defn.pg ^{:- [:text]
           :api/flags []}
  ping
  "tests that the db is working"
  {:added "4.0"}
  [] (return "pong"))

(defn.pg ^{:- [:jsonb]
           :api/flags []}
  ping-ok
  "tests that the db is working with json"
  {:added "4.0"}
  [] (return {:reply "ok"}))

(defn.pg ^{:- [:jsonb]
           :api/flags []}
  echo
  "tests that the db is working with echo json"
  {:added "4.0"}
  [:jsonb input] (return input))

(defn.pg ^{:- [:numeric]}
  addf
  "adds two values"
  {:added "4.0"}
  [:numeric x :numeric y]
  (return (+ x y)))

(defn.pg ^{:- [:numeric]}
  subf
  "subtracts two values"
  {:added "4.0"}
  [:numeric x :numeric y]
  (return (- x y)))

(defn.pg ^{:- [:numeric]}
  mulf
  "multiplies two values"
  {:added "4.0"}
  [:numeric x :numeric y]
  (return (* x y)))

(defn.pg ^{:- [:numeric]}
  divf
  "divide two values"
  {:added "4.0"}
  [:numeric x :numeric y]
  (return (/ x y)))
  
(defn.pg insert-task
  "inserts a task"
  {:added "4.0"}
  [:text i-name :text i-status :jsonb o-op]
  (let [out (pg/g:insert -/Task
              {:status i-status
               :name i-name
               :cache {}}
              {:track o-op})]
    (return out)))

(defn.pg insert-entry
  "inserts an entry"
  {:added "4.0"}
  [:text i-name :jsonb i-tags :jsonb o-op]
  (let [out (pg/g:insert -/Entry
              {:name i-name
               :tags i-tags}
              {:track o-op})]
    (return out)))


