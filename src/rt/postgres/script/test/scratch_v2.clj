(ns rt.postgres.script.test.scratch-v2
    (:require [std.lib :as h]
              [std.lang :as l]))

(l/script :postgres
  {:require [[rt.postgres :as pg]]
   :import [["citext"]
            ["uuid-ossp"]]
   :config {:dbname "test-scratch"}
   :static {:application ["scratch-v2"]
            :seed        ["scratch-v2"]
            :all {:schema ["scratch-v2"]}}})

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
     [:id {:type :uuid :primary true
           :sql {:default '(rt.postgres/uuid-generate-v4)}}])

(def RecordType
     [:op-created {:type :uuid :scope :-/system}
      :op-updated {:type :uuid :scope :-/system}
      :time-created {:type :long}
      :time-updated {:type :long}
      :__deleted__ {:type :boolean :scope :-/hidden
                    :sql {:default false}}])

(def TrackingMin
     {:name "min"
      :in {:create {:op-created :id
                    :op-updated :id
                    :time-created :time
                    :time-updated :time}
           :modify {:op-updated :id
                    :time-updated :time}}
      :ignore #{:delete}})

(deftype.pg ^{:track [-/TrackingMin]
              :append [-/RecordType]
              :public true}
 TaskCache
  "constructs a task cache"
  {:added "4.0"}
  [:id {:type :uuid :primary true
        :web {:example "AUD"}
        :sql {:default (rt.postgres/uuid-generate-v4)}}])

(deftype.pg ^{:track [-/TrackingMin]
              :prepend [[-/Id :id {:web {:example "00000000-0000-0000-0000-000000000000"}}]]
              :append [-/RecordType]
              :public true}
 Task
  "constructs a task"
  {:added "4.0"}
  [:status {:type :enum :required true :scope :-/info
            :enum {:ns -/EnumStatus}
            :web {:example "success"}}
   :name {:type :text :required true
          :sql {:unique "default"
                :index {:using :hash}}}
   :cache {:type :ref :required true
           :ref {:ns -/TaskCache}}
   :detail {:type :map
            :map {:rake {:type :text
                         :required true}
                  :staking {:type :text
                            :required true}
                  :allotment {:type :smallint}
                  :decimal {:type :smallint}
                  :nested {:type :map
                           :map {:home-name {:type :text}
                                 :home-title {:type :text}
                                 :home-entity {:type :uuid}
                                 :away-name {:type :text}
                                 :away-title {:type :text}
                                 :away-entity {:type :uuid}}}}}])

(deftype.pg ^{:track [-/TrackingMin]
              :prepend [[-/Id :id {:web {:example "00000000-0000-0000-0000-000000000000"}}]]
              :append [-/RecordType]
              :public true}
 Entry
  "construcs an entry"
  {:added "4.0"}
  [:name {:type :text :required true
          :sql {:unique "default"
                :index {:using :hash}}}
   :tags {:type :array :required true
          :sql {:process -/as-array}}])

(defn.pg insert-entry
         "inserts an entry"
  {:added "4.0"}
  [:text i-name :jsonb i-tags :jsonb o-op]
  (let [o-out (pg/g:insert -/Entry
                           {:name i-name
                            :tags i-tags}
                           {:track o-op})]
       (return o-out)))

(defn.pg update-entry-tags
         "inserts an entry"
  {:added "4.0"}
  [:text i-name :jsonb i-tags :jsonb o-op]
  (let [o-out (pg/t:update -/Entry
                           {:where {:name i-name}
                            :set {:tags i-tags}
                            :track o-op})]
       (return o-out)))

(defn.pg insert-task-fields
         "inserts a task"
  {:added "4.0"}
  [:text i-name :text i-status :jsonb o-op]
  (let [o-out (pg/g:insert -/Task
                           {:status i-status
                            :name i-name
                            :cache {}}
                           {:track o-op})]
       (return o-out)))

(defn.pg insert-task-raw
         "inserts a task"
  {:added "4.0"}
  [:jsonb m :jsonb o-op]
  (let [o-out (pg/t:insert -/Task
                           m
                           {:track o-op})]
       (return o-out)))

(defn.pg insert-task-wrapped
         "inserts a task"
  {:added "4.0"}
  [:jsonb m :jsonb o-op]
  (return
   (-/insert-task-raw m o-op)))




