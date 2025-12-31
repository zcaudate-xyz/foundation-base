(ns szndb.core.type-seed
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]))

(l/script :postgres
  {:require [[rt.postgres :as pg]
             [rt.postgres.supabase :as s]]
   :static {:application ["szn"]
            :seed        ["szn_type"]
            :all         {:schema   ["szn_type"]}}
   :import  [["citext"]
             ["pgcrypto"]
             ["uuid-ossp"]]
   :emit    {:code {:transforms
                    {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"szn_type"}))

(defn.pg ^{:- [:text]
           :props [:immutable :parallel-safe]}
  as-limit-length
  "limits the length of the entry"
  {:added "0.1"}
  [:text input :integer limit-to]
  (let [(:integer v-len)  (pg/length input)
        _ (pg/assert (< v-len limit-to)
            [:system/length-limited  {:input input
                                      :max-length limit-to}])]
    (return input)))

(defn.pg ^{:props [:immutable :parallel-safe]}
  as-jsonb
  "Converts a string to a standard JSONB object."
  {:added "0.1"}
  [:jsonb input]
  (cond (== (pg/jsonb-typeof input) "string")
        (return
         (-/as-jsonb
          (:jsonb (pg/jsonb-extract-path-text input
                                              [:variadic
                                               (++ (array []) :text [])]))))

        :else
        (return input)))

(defn.pg ^{:props [:immutable :parallel-safe]}
  as-jsonb-array
  "makes an empty map into an empty array"
  {:added "0.1"}
  [:jsonb input]
  (let [v-out (-/as-jsonb input)]
    (when (== v-out "{}")
      (return "[]"))
    (return v-out)))

(defn.pg ^{:props [:immutable :parallel-safe]}
  as-distribution
  "as probability distribution"
  {:added "0.1"}
  [:jsonb input]
  (let [v-out (-/as-jsonb-array input)
        (:numeric v-sum) [:select (pg/sum (:numeric k))
                          :from (pg/jsonb-array-elements-text v-out) k]
        _ (pg/assert (< v-sum 1)
            [:system/distribution-above-one  #{v-out
                                               v-sum}])
        _ (pg/assert (< 0 v-sum )
            [:system/distribution-below-zero  #{v-out
                                                v-sum}])]
    (return v-out)))

(defn.pg ^{:%% :sql
           :- [:citext]
           :props [:immutable :parallel-safe]}
  as-upper
  "makes citext entries uppercase"
  {:added "0.1"}
  [:citext input]
  (:citext (pg/upper (:text input))))

(defn.pg ^{:- [:citext]
           :props [:immutable :parallel-safe]}
  as-upper-formatted
  "limits the length of the entry"
  {:added "0.1"}
  [:citext input]
  (let [_ (if [input :is-null]
            (return input))
        (:text v-matched)  (pg/regexp-match input "^[\\d\\w\\.\\-\\_\\:]+$")
        _ (pg/assert [v-matched :is-not-null]
            [:system/invalid-input  {:input input
                                     :allowed "a-z,A-z,0-9,.-_"}])]
    (return (pg/upper (:text input)))))

(defn.pg ^{:- [:citext]
           :props [:immutable :parallel-safe]}
  as-upper-limit-length
  "limits the length of the entry"
  {:added "0.1"}
  [:citext input :integer limit-to]
  (let [(:integer v-len)  (pg/length input)
        _ (pg/assert (< v-len limit-to)
            [:system/length-limited  {:input input
                                      :max-length limit-to}])]
    (return (pg/upper input))))

(defn.pg ^{:%% :sql
           :- [:citext]
           :props [:immutable :parallel-safe]}
  as-lower
  "makes citext entries lowercase"
  {:added "0.1"}
  [:citext input]
  (:citext (pg/lower (:text input))))

(defn.pg ^{:- [:citext]
           :props [:immutable :parallel-safe]}
  as-lower-formatted
  "limits the length of the entry"
  {:added "0.1"}
  [:citext input]
  (let [_ (if [input :is-null]
            (return input))
        (:text v-matched)  (pg/regexp-match input "^[\\d\\w\\.\\-\\_\\:]+$")
        _ (pg/assert [v-matched :is-not-null]
            [:system/invalid-input  {:input input
                                     :allowed "a-z,A-z,0-9,.-_"}])]
    (return (pg/lower (:text input)))))

(defn.pg ^{:- [:citext]
           :props [:immutable :parallel-safe]}
  as-lower-limit-length
  "limits the length of the entry"
  {:added "0.1"}
  [:citext input :integer limit-to]
  (let [(:integer v-len)  (pg/length input)
        _ (pg/assert (< v-len limit-to)
            [:system/length-limited  {:input input
                                      :max-length limit-to}])]
    (return (pg/lower input))))

(defn.pg ^{:- [:uuid]
           :props [:immutable :parallel-safe]}
  as-uuid
  "Converts a text string to a UUID."
  {:added "0.1"}
  [:text input]
  [:begin
   \\ (do (return (:uuid input)))
   :exception
   \\ :when others :then
   \\ (do
        (return nil))
   \\ :end])

(defn.pg ^{:- [:citext]
           :props [:immutable :parallel-safe]}
  as-crypto-address
  "Formats a string as a cryptotoken address."
  {:added "0.1"}
  [:citext input]
  (cond (== "0x" (:citext (pg/substring input 1 2)))
        (return (:citext (|| "0x" (pg/upper (pg/substring input 3)))))

        :else
        (return input)))

(defn.pg ^{:- [:numeric]
           :props [:immutable :parallel-safe]}
  as-crypto-dust
  "Converts a numeric value to a crypto dust amount."
  {:added "0.1"}
  [:numeric input :integer factor :integer digits]
  (let [(:bigint seed) (* input (pg/pow 10 digits))]
    (return (* seed (pg/pow 0.1 (+ factor digits))))))

(defn.pg ^{:%% :sql
           :- [:text]}
  color-rand
  "creates a random color"
  {:added "0.1"}
  ([]
   (|| "#" (pg/upper (pg/rand-hex 6)))))

(defn.pg ^{:%% :sql
           :- [:boolean]
           :props [:immutable :parallel-safe]}
  color-check
  "checks that string is a color"
  {:added "0.1"}
  ([:text s]
   [:select (pg/regexp-match s "#[0-9A-F]{6}") :is-not-null]))


;;
;; DIFF / PATCH
;;

(defn.pg ^{:%% :sql
           :props [:immutable :parallel-safe]}
  js-keys-common
  "Retrieves the common keys between two JavaScript objects."
  {:added "0.1"}
  [:jsonb m1 :jsonb m2]
  [:select (pg/coalesce
            (pg/jsonb-agg k)
            (pg/jsonb-build-array))
   \\
   :from  [:select  (pg/jsonb-object-keys m1) :as k
           \\
           :union
           \\
           :select  (pg/jsonb-object-keys m2) :as k]
   :as t])

(defn.pg ^{:props [:immutable :parallel-safe]}
  get-diff-forward
  "gets the diff but ignores the backward changes"
  {:added "0.1"}
  [:jsonb i-before
   :jsonb i-after]
  (let [diff    {}
        all-keys (-/js-keys-common i-before i-after)
        (:text curr-key) nil
        old-val nil
        new-val nil]
    [:for curr-key :in
     :select (pg/jsonb-array-elements-text all-keys)
     (loop []
       (:= old_val (:-> i-before curr-key))
       (:= new_val (:-> i-after curr-key))

       (cond [old-val :is-not-distinct-from new-val]
             [:continue])

       (cond [old-val :is-null :and new-val :is-not-null]
             (:= diff (|| diff {curr-key ["+" new-val]}))

             [old-val :is-not-null :and new-val :is-null]
             (:= diff (|| diff
                          {curr-key ["-"]}))

             (and (== "object" (pg/jsonb-typeof old-val))
                  (== "object" (pg/jsonb-typeof new-val)))
             (:= diff (|| diff
                          {curr-key [">" (-/get-diff-forward old-val new-val)]}))

             :else
             (:= diff (|| diff
                          {curr-key ["%" new-val]}))))]
    (return diff)))

(defn.pg ^{:props [:immutable :parallel-safe]}
  get-diff
  "Computes the difference between two JavaScript objects."
  {:added "0.1"}
  [:jsonb i-before
   :jsonb i-after]
  (let [diff    {}
        all-keys (-/js-keys-common i-before i-after)
        (:text curr-key) nil
        old-val nil
        new-val nil]
    [:for curr-key :in
     :select (pg/jsonb-array-elements-text all-keys)
     (loop []
       (:= old_val (:-> i-before curr-key))
       (:= new_val (:-> i-after curr-key))

       (cond [old-val :is-not-distinct-from new-val]
             [:continue])

       (cond [old-val :is-null :and new-val :is-not-null]
             (:= diff (|| diff {curr-key ["+" new-val]}))

             [old-val :is-not-null :and new-val :is-null]
             (:= diff (|| diff
                          {curr-key ["-" old-val]}))

             (and (== "object" (pg/jsonb-typeof old-val))
                  (== "object" (pg/jsonb-typeof new-val)))
             (:= diff (|| diff
                          {curr-key [">" (-/get-diff old-val new-val)]}))

             :else
             (:= diff (|| diff
                          {curr-key ["%" new-val old-val]}))))]
    (return diff)))

(defn.pg ^{:props [:immutable :parallel-safe]}
  patch-diff
  "Applies a diff to a JavaScript object to create a new state."
  {:added "0.1"}
  [:jsonb i-before
   :jsonb i-diff]
  (let [(:text curr-key) nil
        op-array nil
        (:text op-type)  nil
        out i-before]
    [:for curr-key :in
     :select (pg/jsonb-object-keys i-diff)
     (loop []
       (:= op-array (:-> i-diff curr-key))
       (:= op-type  (:->> op-array 0))
       (:= out
           (case op-type
             "+" (|| out {curr-key (:-> op-array 1)})
             "-" (- out curr-key)
             "%" (|| out {curr-key (:-> op-array 1)})
             ">" (|| out {curr-key
                          (-/patch-diff
                           (:-> out curr-key)
                           (:-> op-array 1))})
             :else out)))]
    (return out)))


(defn.pg ^{:props [:immutable :parallel-safe]}
  patch-diff-rev
  "patches a diff in reverse"
  {:added "0.1"}
  [:jsonb i-after
   :jsonb i-diff]
  (let [(:text curr-key) nil
        op-array nil
        (:text op-type)  nil
        out i-after]
    [:for curr-key :in
     :select (pg/jsonb-object-keys i-diff)
     (loop []
       (:= op-array (:-> i-diff curr-key))
       (:= op-type  (:->> op-array 0))
       (:= out
           (case op-type
             "+" (- out curr-key)
             "-" (|| out {curr-key (:-> op-array 1)})
             "%" (|| out {curr-key (:-> op-array 2)})
             ">" (|| out {curr-key
                          (-/patch-diff-rev
                           (:-> out curr-key)
                           (:-> op-array 1))})
             :else out)))]
    (return out)))


;;
;; Default Templates
;;

(def Id
  [:id  {:type :uuid :primary true
         :sql {:default `(pg/uuid-generate-v4)}}])

(def IdClass
  [:id  {:type :uuid :primary "default"
         :sql {:default `(pg/uuid-generate-v4)}}])

(def IdLog
  [:id  {:type :uuid :primary true
         :sql {:default `(pg/uuid-generate-v1)}}])

(def IdText
  [:id  {:type :citext :primary true
         :sql {:process [[`-/as-upper-formatted]
                         [`-/as-upper-limit-length 50]]}}])

(def RecordType
  [:op-created     {:type :uuid    :scope :-/system}
   :op-updated     {:type :uuid    :scope :-/system}
   :time-created   {:type :time
                    :web {:type "time"}}
   :time-updated   {:type :time
                    :web {:type "time"}}
   :__deleted__    {:type :boolean :scope :-/hidden
                    :sql {:default false}}])

(def DataType
  [:op-created     {:type :uuid}
   :op-updated     {:type :uuid}
   :time-created   {:type :time}
   :time-updated   {:type :time}])

(def TimeType
  [:time-created   {:type :time
                    :web {:type "time"}}
   :time-updated   {:type :time
                    :web {:type "time"}}])

(def TempType
  [:op-created     {:type :uuid}
   :op-updated     {:type :uuid}
   :time-created   {:type :time}
   :time-updated   {:type :time}])

(def LogType
  [:op-created     {:type :uuid}
   :time-created   {:type :time}])


;;
;; Fragments
;;

(defenum.pg EnumTablename
  ["Token"
   "Commodity"
   "Publisher"
   "Feed"
   "Chat"
   "ChatChannel"
   "User"
   "UserProfile"
   "Organisation"
   "Campaign"
   "Topic"])

(defenum.pg EnumClassType
  ["Default"
   "User"
   "Organisation"
   "Campaign"
   "Topic"])

(def ClassPart
  [:class        {:type :enum :required true :scope :-/info :primary "default"
                  :enum {:ns `-/EnumClassType}
                  :sql  {:unique ["class"]}}
   :class-ref    {:type :uuid :required true
                  :sql  {:unique ["class"]}}])

(def ClassTablePart
  [:class        {:type :enum :required true :scope :-/info :primary "default"
                  :enum {:ns `-/EnumClassType}
                  :sql  {:unique ["class"]}}
   :class-table  {:type :enum :required true  :scope :-/info :primary "default"
                  :enum {:ns `-/EnumTablename}
                  :sql  {:unique ["class"]}}
   :class-ref    {:type :uuid :required true
                  :sql  {:unique ["class"]}}])

(def ClassEntryPart
  [:class        {:type :enum :required true :scope :-/info  :primary "default"
                  :enum {:ns `-/EnumClassType}}])

(def ClassTableEntryPart
  [:class        {:type :enum :required true :scope :-/info  :primary "default"
                  :enum {:ns `-/EnumClassType}}
   :class-table  {:type :enum :required true  :scope :-/info :primary "default"
                  :enum {:ns `-/EnumTablename}}])

(def TypePart
  [:type     {:type :citext :required true
              :sql {:process [[`-/as-upper-formatted]
                              [`-/as-upper-limit-length 30]]}}])

(def IsActivePart
  [:is-active   {:type :boolean :required true
                 :sql {:default true}}])

(def IsPublicPart
  [:is-public   {:type :boolean :required true
                 :sql {:default false}}])

(def IsOfficialPart
  [:is-official  {:type :boolean :required true
                  :scope :-/info
                  :sql {:default false}}])


(def IsOnboardedPart
  [:is-onboarded  {:type :boolean :required true
                   :scope :-/info
                   :sql {:default false}}])


(def ColorPart
  [:color        {:type :text :required true :scope :-/info
                  :sql {:default    `(-/color-rand)
                        :constraint `(-/color-check #{"color"})}
                  :profile {:web {:edit #{:create :modify}
                                  :type "color"}}}])

(def NamePart
  [:name         {:type :citext :required true :scope :-/info
                  :sql {:process [[`-/as-upper-formatted]
                                  [`-/as-upper-limit-length 36]]
                        :unique "default"}}])

(def TagsPart
  [:tags         {:type :array :scope :-/info
                  :sql {:process `-/as-jsonb-array}
                  :profile {:web {:edit #{:create :modify}
                                  :type "chip"
                                  :create []}}}])


(def LogPart
  [:log         {:type :array :required true
                 :scope :-/detail
                 :sql  {:process `-/as-jsonb
                        :default "[]"}
                 :map  {:status  {:type :text}
                        :message {:type :text}
                        :error   {:type :text}}}])

(def EntryPart
  [:entry          {:type :map :required true
                    :scope :-/info
                    :sql  {:process `-/as-jsonb}
                    :map  {:status  {:type :text}
                           :message {:type :text}
                           :error   {:type :text}}}])

(def DeltasPart
  [:deltas       {:type :jsonb :required true}])

(def ContentIconPart
  [:icon      {:type :image
               :sql  {:process `-/as-jsonb}
               :profiles {:web {:edit #{:create :modify}
                                :type "image"
                                :create {}}}}])

(def ContentPicturePart
  [:picture      {:type :image
                  :sql  {:process `-/as-jsonb}
                  :profiles {:web {:edit #{:create :modify}
                                   :type "image"
                                   :create {}}}}])

(def ContentBackgroundPart
  [:background   {:type :image
                  :sql  {:process `-/as-jsonb}
                  :profiles {:web {:edit #{:create :modify}
                                   :type "image"
                                   :create {}}}}])

(def DetailPart
  [:detail         {:type :map
                    :sql  {:process `-/as-jsonb
                           :default "{}"}
                    :profiles {:web {:edit #{:create :modify}
                                     :type "map"}}}])

(def BalancePart
  [:balance      {:type :numeric :required true
                  :sql {:default 0}}])

;;
;; Tracking
;;

(def TrackingMin
  {:name  "min"
   :in   {:create {:op-created   :id
                   :op-updated   :id
                   :time-created :time
                   :time-updated :time}
          :modify {:op-updated   :id
                   :time-updated :time}}
   :ignore #{:delete}})

(def TrackingLog
  {:name  "log"
   :in  {:create {:op-created   :id
                  :time-created :time}}
   :disable #{:modify}
   :ignore  #{:delete}})

(def TrackingTime
  {:name  "time"
   :in  {:create {:time-updated :time
                  :time-created :time}
         :modify {:time-updated :time}}
   :disable #{:modify}})

(def TrackingEntry
  {:name  "entry"
   :in  {:create {:op-created   :id
                  :time-created :time}}
   :ignore  #{:modify :delete}})


;;
;; Op
;;
;; This is a coordinating data structure that tracks time as well as
;; actions that cause data to change. Events should look at the WAL logs
;; to synchronize related events
;;

(deftype.pg ^{:prepend  [-/IdLog]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Op
  {:added "0.1"}
  [:tag          {:type :text :required true
                  :web {:example "account.registration"}}
   :time         {:type :time    :required true
                  :sql {:default (pg/time-us)}}
   :data         {:type :map
                  :web {:example {:email "test@test.com"
                                  :password "password"}}}
   :user-id   {:type :uuid}])

;;
;; Op Functions
;;

(defmacro.pg ^{:- [:block]}
  op
  {:added "0.1"}
  ([tag user-id & [data m]]
   (let [tag (cond (and (symbol? tag)
                        (namespace tag))
                   (str (:id
                         @(resolve tag)))

                   (keyword? tag) (h/strn tag)
                   :else tag)]
     (list `pg/t:insert `-/Op
           (cond-> {:tag  tag
                    :user-id user-id}
             data (assoc :data data)
             (:time m) (assoc :time (:time m)))
           (or m {})))))

(defn.pg ^{:- [:jsonb]
           :%% :sql}
  op-fn

  {:added "0.1"}
  ([:text i-tag
    :uuid i-user-id
    :jsonb i-data]
   (-/op i-tag i-user-id i-data)))


;;
;; Metadata
;;
;; This is a sparse field, meaning that many tables could have metadata fields.
;; The id is composed of table name and table key. generic function can be used
;; to maintain the table.
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend []
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Metadata

  {:added "0.1"}
  [:id        {:type :jsonb :primary true}
   :entry     {:type :jsonb}])


(defn.pg ^{:%% :sql}
  get-meta
  "gets the metadata entry"
  {:added "0.1"}
  [:jsonb i-table-full
   :text  i-key]
  (pg/t:get-field -/Metadata
    {:where {:id (|| i-table-full
                     (pg/to-jsonb i-key))}
     :returning :entry}))

(defn.pg ^{:%% :sql}
  set-metadata
  "updates the metadata dynamically"
  {:added "0.1"}
  [:jsonb i-table-full
   :text  i-key
   :jsonb i-entry
   :jsonb o-op]
  (pg/t:upsert -/Metadata
    {:id  (:jsonb (|| i-table-full
                      (pg/to-jsonb i-key)))
     :entry i-entry}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  get-metadata
  "gets the metadata from an object"
  {:added "0.1"}
  [:jsonb  i-table-full
   :text  i-key]
  (pg/t:get -/Metadata
    {:where {:id  (|| i-table-full
                      (pg/to-jsonb i-key))}}))

(defn.pg ^{:%% :sql}
  purge-metadata
  "Removes metadata associated with a given entity."
  {:added "0.1"}
  [:jsonb  i-table-full
   :text  i-key]
  (pg/t:delete -/Metadata
    {:where {:id (|| i-table-full
                     (pg/to-jsonb i-key))}
     :single true}))

;;
;; Rev
;;
;; This is another sparse field that keeps track of changes of data in various
;; tables. This is a generic implementation.
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        -/ClassTablePart]
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Rev
  {:added "0.1"}
  [:index     {:type :integer :required true :scope :-/info
               :sql {:default 0}}
   :current   {:type :map
               :sql {:default "{}"}}]
  {:partition-by [:list #{"class"}]})

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/ClassTableEntryPart]
              :append  [-/DeltasPart
                        -/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  RevLog
  {:added "0.1"}
  [:rev      {:type :ref :primary "default"
              :required true
              :ref {:ns -/Rev}
              :sql {:cascade true
                    :partition true}}
   :version  {:type :integer :required true :primary "default"}]
  {:partition-by [:list #{"class"}]})

(def RevPart
  [:rev          {:type :ref :required true
                  :ref {:ns `-/Rev}
                  :sql {:unique ["rev"]
                        :cascade true
                        :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-rev-class
  [:text i-class
   :uuid i-rev-id]
  (pg/t:exists -/Rev
    {:where {:id i-rev-id
             :class i-class}}))

(defn.pg  ^{:%% :sql}
  insert-rev
  "inserts a revision entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb i-current
   :jsonb o-op]
  (pg/t:insert -/Rev
    {:class     i-class
     :class-table i-class-table
     :class-ref i-classref
     :current i-current}
    {:track o-op}))

(defn.pg  ^{:%% :sql}
  update-rev
  "Updates an existing revision entry with new data."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb i-latest
   :jsonb o-op]
  (pg/t:update -/Rev
    {:where {:class     i-class
             :class-table i-class-table
             :class-ref i-classref}
     :set   {:current i-latest
             :index (+ #{"index"} 1)}
     :single true
     :track o-op}))

(defn.pg ^{:%% :sql}
  insert-rev-log
  "inserts a revision log entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-rev-id
   :integer i-version
   :jsonb i-deltas
   :jsonb o-op]
  (pg/t:insert -/RevLog
    {:class i-class
     :class-table i-class-table
     :rev i-rev-id
     :version i-version
     :deltas i-deltas}
    {:track o-op}))

(defn.pg commit-rev-init
  "performs the inital commit"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :text  i-key
   :jsonb i-latest
   :jsonb o-op]
  (let [o-rev (-/insert-rev i-class
                            i-class-table
                            i-class-ref
                            i-key
                            i-latest
                            o-op)
        #{(:uuid v-id)} o-rev
        (:boolean o-changed) true
        v-deltas (-/get-diff-forward {} i-latest)
        o-log (-/insert-rev-log
               i-class
               i-class-table
               v-id
               0
               v-deltas
               o-op)]
    (return #{o-rev
              o-log
              o-changed})))

(defn.pg commit-rev
  "Commits a new revision for a given entity, tracking changes over time."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb i-latest]
  (let [v-latest (- i-latest
                    "op_created"
                    "op_updated"
                    "time_updated"
                    "time_created")
        o-rev  (pg/t:get -/Rev
                 {:where {:class i-class
                          :class-table i-class-table
                          :class-ref i-class-ref}})
        _ (when [o-rev :is-null]
            (return (-/commit-rev-init i-class
                                       i-class-table
                                       i-class-ref
                                       v-latest
                                       {:id   (:->> i-latest "op_created")
                                        :time (:->> i-latest "time_created")})))
        #{(:uuid v-id)
          (:integer v-index)
          v-current}     o-rev
        (:boolean o-changed) (not= 0
                                   (pg/jsonb-cmp v-latest
                                                 v-current))
        _ (when (not o-changed)
            (return #{o-rev
                      o-changed}))

        o-op  {:id   (:->> i-latest "op_updated")
               :time (:->> i-latest "time_updated")}
        v-deltas (-/get-diff-forward v-current v-latest)
        o-rev (-/update-rev i-class i-class-table v-id v-latest o-op)
        o-log (-/insert-rev-log
               i-class
               i-class-table
               v-id
               (+ v-index 1)
               v-deltas
               o-op)]
    (return #{o-changed
              o-rev
              o-log})))

(defn.pg ^{:%% :sql
           :- [:boolean]}
  has-rev
  "checks if revision exists"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :text  i-key]
  (pg/t:exists -/Rev
    {:where {:class i-class
             :class-table i-class-table
             :class-ref i-class-ref}}))

(defn.pg ^{:%% :sql}
  get-rev
  "Retrieves a specific revision entry for an entity."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :text  i-key]
  (pg/t:get -/Rev
    {:where {:class i-class
             :class-table i-class-table
             :class-ref i-class-ref}}))

(defn.pg ^{:%% :sql}
  get-rev-logs
  "Retrieves the revision history (logs) for a given entity."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/g:select -/RevLog
    {:where {:rev {:class i-class
                   :class-table i-class-table
                   :class-ref i-class-ref}
             :order-by :version}))

(defn.pg get-rev-version
  "gets various versions of a record"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :integer i-version]
  (let [o-out {}
        v-d nil
        v-deltas (pg/g:select -/RevLog
                   {:where {:rev {:class i-class
                                  :class-table i-class-table
                                  :class-ref i-class-ref}
                            :class i-class
                            :class-table i-class-table
                            :version [:lte i-version]}
                    :returning :deltas
                    :order-by :version})]
    [:for v-d :in :select (pg/jsonb-array-elements v-deltas)
     (loop []
       (:= o-out (-/patch-diff o-out (:-> v-d "deltas"))))]
    (return o-out)))

(defn.pg ^{:%% :sql}
  purge-rev-logs
  "Removes all revision logs for a specific entity."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/g:delete -/RevLog
    {:where {:rev {:class i-class
                   :class-table i-class-table
                   :class-ref i-class-ref}}}))

(defn.pg purge-rev
  "Removes a specific revision entry and its associated logs."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (let [o-logs (-/purge-rev-logs
                i-class
                i-class-table
                i-class-ref)
        o-rev  (pg/t:delete -/Rev
                 {:where {:class i-class
                          :class-table i-class-table
                          :class-ref i-class-ref}
                  :single true})]
    (return (|| o-rev #{o-logs}))))

;;
;; Audit
;;
;; This is used for tracking actions on a datastructure.
;;

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassTablePart]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Audit
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/ClassTableEntryPart]
              :append  [-/EntryPart
                        [-/LogType :op-created {:primary "default"
                                                :sql  {:default (pg/uuid-generate-v1)}}]]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  AuditLog
  {:added "0.1"}
  [:audit   {:type :ref :primary "default"
             :ref {:ns -/Audit}
             :sql {:cascade true
                   :partition true}}]
  {:partition-by [:list #{"class"}]})

(def AuditPart
  [:audit          {:type :ref  :required true
                    :ref {:ns `-/Audit}
                    :sql {:unique ["audit"]
                          :cascade true
                          :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-audit-class
  [:text i-class
   :text  i-class-table
   :uuid i-audit-id]
  (pg/t:exists -/Audit
    {:where {:id i-audit-id
             :class i-class
             :class-table  i-class-table}}))

(defn.pg ^{:%% :sql}
  create-audit
  "Creates a new audit entry to record system events or changes."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Audit
    {:class i-class
     :class-table  i-class-table
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  create-audit-log
  "create a audit log entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-audit-id
   :jsonb i-entry
   :jsonb o-op]
  (pg/t:insert -/AuditLog
    {:class i-class
     :class-table  i-class-table
     :audit i-audit-id
     :entry i-entry}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  get-audit-logs
  "Retrieves the audit logs for a specific audit entry."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/g:select -/AuditLog
    {:where {:audit  {:class  i-class
                      :class-table  i-class-table
                      :class-ref  i-class-ref}
             :class  i-class
             :class-table  i-class-table}}))

(defn.pg ^{:%% :sql}
  purge-audit-logs
  "purges the audit logs"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/g:delete -/AuditLog
    {:where {:audit  {:class  i-class
                      :class-ref  i-class-ref}
             :class  i-class}}))

(defn.pg
  purge-audit
  "Removes an audit entry and its associated logs."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (let [o-logs (-/purge-audit-logs i-class i-class-table i-class-ref)
        o-audit (pg/t:delete -/Audit
                  {:where {:class  i-class
                           :class-table  i-class-table
                           :class-ref  i-class-ref}
                   :single true})]
    (return (|| o-audit #{o-logs}))))

;;
;; Lock
;;

(defenum.pg ^{}
  EnumLockAction
  [:lock :unlock])

(defenum.pg ^{}
  EnumLockReason
  [:none :abuse :unpaid :other])

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassTablePart]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Lock
  "Allow locking of organisation

   ;; User creation triggers Lock
   (pg/t:exists -/Lock)
   => true"
  {:added "0.1"}
  [:value   {:type :boolean :required true
             :sql {:default false}}]
  {:partition-by [:list #{"class"}]})

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/ClassTableEntryPart]
              :append  [-/DetailPart
                        [-/LogType :op-created {:primary "default"
                                                :sql  {:default (pg/uuid-generate-v1)}}]]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  LockLog
  "Timestamped log of locking on an object

   (pg/t:exists -/LockLog)
   => true"
  {:added "0.1"}
  [:lock     {:type :ref :required true :primary "default"
              :ref {:ns -/Lock}
              :sql {:cascade true
                    :partition true}}
   :reason   {:type :enum :required true :scope :-/info
              :enum {:ns -/EnumLockReason}}
   :action   {:type :enum :required true :scope :-/info
              :enum {:ns -/EnumLockAction}}]
  {:partition-by [:list #{"class"}]})

(def LockPart
  [:lock           {:type :ref :required true
                    :ref {:ns `-/Lock}
                    :sql {:unique ["lock"]
                          :cascade true
                          :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-lock-class
  [:text  i-class
   :text  i-class-table
   :uuid  i-lock-id]
  (pg/t:exists -/Lock
    {:where {:id i-lock-id
             :class-table i-class-table
             :class i-class}}))

(defn.pg ^{:%% :sql}
  insert-lock
  "Creates a new lock entry for a given entity."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Lock
    {:class i-class
     :class-table i-class-table
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  insert-lock-log
  "Creates a new log entry for a lock, recording the action and reason."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-lock-id
   :text  i-reason
   :text  i-action
   :jsonb i-detail
   :jsonb o-op]
  (pg/t:insert -/LockLog
    {:lock    i-lock-id
     :class-table i-class-table
     :class   i-class
     :reason  i-reason
     :action  i-action
     :detail  i-detail}
    {:track o-op}))

(defn.pg perform-lock
  "Applies a lock to an entity, recording the action and its details."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :text  i-reason
   :text  i-action
   :jsonb i-log-detail
   :jsonb o-op]
  (let [o-log   (-/insert-lock-log
                 i-class
                 i-class-table
                 i-lock-id
                 i-reason
                 i-action
                 i-log-detail
                 o-op)
        (:boolean v-value) (case i-action
                             "lock"   true
                             "unlock" false)
        o-lock  (pg/t:update -/Lock
                  {:where {:class i-class
                           :class-table i-class-table
                           :class-ref i-class-ref}
                   :set {:value v-value}
                   :single true
                   :track o-op})]
    (return #{o-lock
              o-log})))

(defn.pg ^{:%% :sql
           :- [:boolean]}
  get-lock-value
  "gets the current lock value"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/t:get -/Lock
    {:where {:class i-class
             :class-table i-class-table
             :class-ref i-class-ref}
     :returning :value
     :as :raw}))

(defn.pg ^{:%% :sql}
  purge-lock-logs
  "Removes all lock logs associated with a specific lock."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/g:delete -/LockLog
    {:where {:class i-class
             :class-table i-class-table
             :lock  {:class i-class
                     :class-table i-class-table
                     :class-ref i-class-ref}}}))

(defn.pg purge-lock
  "Removes a lock entry and its associated logs."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (let [o-logs (-/purge-lock-logs i-class i-class-table i-class-ref)
        o-lock (pg/t:delete -/Lock
                 {:where {:class i-class
                          :class-table i-class-table
                          :class-ref i-class-ref}
                  :single true})]
    (return (|| o-lock #{o-logs}))))

;;
;; KeyStore
;;

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassTablePart]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  KeyStore
  "Generic KeyStore for API, Oracle, and Feed access"
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(def KeyStorePart
  [:keystore           {:type :ref :required true
                        :ref {:ns `-/KeyStore}
                        :sql {:unique ["keystore"]
                              :cascade true
                              :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-keystore-class
  [:text i-class
   :text  i-class-table
   :uuid i-keystore-id]
  (pg/t:exists -/KeyStore
    {:where {:id i-keystore-id
             :class i-class
             :class-table i-class-table}}))

(defn.pg ^{:%% :sql}
  insert-keystore
  "creates a keystore entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/KeyStore
    {:class i-class
     :class-table i-class-table
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg purge-keystore
  "purges a keystore entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (let [o-store (pg/t:delete -/KeyStore
                  {:where {:class i-class
                           :class-table i-class-table
                           :class-ref i-class-ref}
                   :single true})]
    (return o-store)))

;;
;; KeyInstance
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [[-/ClassTableEntryPart
                         :class {:sql {:unique ["class"]}}]]
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  KeyInstance
  "Specific credential for a KeyStore"
  {:added "0.1"}
  [:keystore      {:type :ref :required true :primary "default"
                   :ref {:ns -/KeyStore}
                   :sql {:cascade true
                         :partition true}}
   :is-revoked    {:type :boolean
                   :sql  {:default false}}
   :lookup-string {:type :text :required true :primary "default"
                   :sql {:unique ["class"]}}
   :secret-hash   {:type :text :required true}]
  {:partition-by [:list #{"class"}]})

(defn.pg ^{:- [:jsonb]}
  generate-key-instance
  "generates a key instance"
  {:added "0.1"}
  [:text i-class
   :text  i-class-table
   :uuid i-keystore-id
   :jsonb o-op]
  (let [(:text v-lookup-string) (pg/encode (pg/gen-random-bytes 9) "hex")
        (:text v-key-secret)    (pg/encode (pg/gen-random-bytes 24) "base64")
        (:text v-secret-hash)   (pg/crypt v-key-secret (pg/gen-salt "bf" 10))
        o-entry (pg/t:insert -/KeyInstance
                  {:keystore i-keystore-id
                   :class i-class
                   :class-table i-class-table
                   :lookup-string v-lookup-string
                   :secret-hash v-secret-hash}
                  {:track o-op})
        (:text o-key) (|| v-lookup-string "." v-key-secret)]
    (return #{o-key
              o-entry})))

(defn.pg ^{:- [:uuid]}
  validate-key-instance
  "validates a key instance"
  {:added "0.1"}
  [:text i-class
   :text  i-class-table
   :text i-key-full]
  (let [(:text v-key-lookup) (pg/split-part i-key-full "." 1)
        (:text v-key-secret) (pg/split-part i-key-full "." 2)
        _ (when (or (== v-key-lookup "")
                    (== v-key-secret ""))
            (return nil))
        v-key (pg/t:get -/KeyInstance
                {:where {:class i-class
                         :class-table i-class-table
                         :lookup-string v-key-lookup}})
        _ (when [v-key :is-null]
            (return nil))
        #{(:uuid v-keystore-id)
          (:text v-secret-hash)
          (:boolean v-is-revoked)} v-key
        _ (when v-is-revoked
            (return nil))]
    (if (== v-secret-hash
            (pg/crypt v-key-secret v-secret-hash))
      (return v-keystore-id)
      (return nil))))

(defn.pg ^{:%% :sql}
  revoke-key-instance
  "revokes a key instance"
  {:added "0.1"}
  [:text i-class
   :text  i-class-table
   :text i-key-lookup
   :jsonb o-op]
  (pg/t:update -/KeyInstance
    {:where {:class i-class
             :class-table i-class-table
             :lookup-string i-key-lookup}
     :set {:is-revoked true}
     :single true
     :track o-op}))

(defn.pg  ^{:%% :sql}
  purge-key-instance
  "purges a key instance"
  {:added "0.1"}
  [:text i-class
   :text  i-class-table
   :text i-key-lookup]
  (pg/t:delete -/KeyInstance
    {:where {:class i-class
             :class-table i-class-table
             :lookup-string i-key-lookup}
     :single true}))

;;
;; Publisher
;;

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        [-/ClassTablePart
                         :class {:sql {:unique ["class" "keystore"]}}
                         :class-table  {:primary nil :ignore true
                                        :sql  {:raw [:generated-always-as '((++ "Publisher" -/EnumTablename)) :stored]}}]]
              :append  [[-/KeyStorePart]
                        -/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Publisher
  "Publisher of data feeds"
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(def PublisherPart
  [:publisher        {:type :ref :required true
                      :ref  {:ns `-/Publisher}
                      :sql {:unique ["publisher"]
                            :cascade true
                            :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-publisher-class
  [:text i-class
   :uuid i-publisher-id]
  (pg/t:exists -/Publisher
    {:where {:id i-publisher-id
             :class i-class}}))

(defn.pg create-publisher
  "creates a feed publisher"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref
   :jsonb o-op]
  (let [(:uuid v-id)  (pg/uuid-generate-v4)
        v-keystore    (-/insert-keystore i-class "Publisher" v-id o-op)
        v-publisher (pg/t:insert -/Publisher
                      {:id v-id
                       :class i-class
                       :class-ref i-class-ref
                       :keystore (pg/id v-keystore)}
                      {:track o-op})]
    (return v-publisher)))

(defn.pg purge-publisher
  "purges a feed publisher"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref]
  (let [o-publisher (pg/t:delete -/Publisher
                      {:where {:class i-class
                               :class-ref i-class-ref}
                       :single true})
        o-keystore (-/purge-keystore i-class "Publisher" (pg/id o-publisher))]
    (return (|| o-publisher
                #{o-keystore}))))

;;
;; Feed
;;

(defenum.pg EnumFeedStatus [:draft :active :published :hidden :archived])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        [-/ClassTableEntryPart
                         :class {:sql {:unique ["publisher" "lock" "rev"]}}
                         :class-table  {:primary nil :ignore true
                                        :sql  {:raw [:generated-always-as '((++ "Feed" -/EnumTablename)) :stored]}}]
                        [-/NamePart :name {:sql {:unique ["publisher"]}}]
                        -/PublisherPart
                        -/LockPart
                        -/RevPart]
              :append  [-/DetailPart
                        -/RecordType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  Feed
  "A specific data feed stream"
  {:added "0.1"}
  [:status       {:type :enum :required true
                  :enum {:ns -/EnumFeedStatus}
                  :sql {:default "draft"}}
   :aggregate    {:type :jsonb}
   ;; Templates
   :template-process   {:type :jsonb :scope :-/info} ;; e.g. JQ or similar logic
   :template-aggregate {:type :jsonb :scope :-/info}
   :template-display   {:type :jsonb :scope :-/info}]
  {:partition-by [:list #{"class"}]})

(defn.pg create-feed
  "creates a feed "
  {:added "0.1"}
  [:text i-class
   :uuid i-publisher-id
   :jsonb m
   :jsonb o-op]
  (let [(:uuid v-feed-id) (pg/uuid-generate-v4)
        v-lock  (-/insert-lock i-class "Feed" v-feed-id o-op)
        v-rev   (-/insert-rev i-class "Feed" v-feed-id nil o-op)
        v-m  (|| m {:id v-feed-id
                    :class i-class
                    :lock-id (pg/id v-lock)
                    :rev-id  (pg/id v-rev)
                    :publisher-id i-publisher-id})
        o-feed (pg/t:insert -/Feed
                 v-m
                 {:track o-op
                  :coalesce true})
        _ [:perform (-/commit-rev i-class "Feed" v-feed-id o-feed)]]
    (return o-feed)))

(defn.pg update-feed
  "updates a feed "
  {:added "0.1"}
  [:text i-class
   :uuid i-feed-id
   :jsonb m
   :jsonb o-op]
  (let [o-feed (pg/t:update -/Feed
                 {:where {:id i-feed-id
                          :class i-class}
                  :set m
                  :columns [:name :status :aggregate
                            :template-process :template-aggregate :template-display
                            :detail]
                  :single true
                  :track o-op})
        _ [:perform (-/commit-rev i-class "Feed" i-feed-id o-feed)]]
    (return o-feed)))

;; TODO IMPLEMENT FEED LIFECYCLE FEATURES
;; - templates only updatable during :draft
;; - locked after :archive

;; LATER TODO IMPLEMENT FEED POLICY FEATURES FOR PUBLIC
;; LATER TODO IMPLEMENT FEED POLICY FEATURES FOR MANAGEMENT

(defn.pg purge-feed
  "purges a feed "
  {:added "0.1"}
  [:text i-class
   :uuid i-feed-id]
  (let [o-feed (pg/t:delete -/Feed
                    {:where {:id i-feed-id
                             :class i-class}
                     :single true})
        o-rev   (-/purge-rev i-class "Feed" i-feed-id)
        o-lock  (-/purge-lock i-class "Feed" i-feed-id)]
    (return (|| o-feed
                #{o-rev
                  o-lock}))))

(defenum.pg EnumFeedItemType [:json :text :binary])

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassEntryPart]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select}}}
  FeedItem
  "Data point in a feed"
  {:added "0.1"}
  [:feed         {:type :ref :required true
                  :ref {:ns -/Feed}
                  :sql {:cascade true
                        :partition true}}
   :type         {:type :enum :required true
                  :enum {:ns -/EnumFeedItemType}
                  :sql {:default "json"}}
   :data         {:type :jsonb}
   :text         {:type :text}
   :value        {:type :numeric}]
  {:partition-by [:list #{"class"}]})

(defn.pg ^{:%% :sql}
  insert-feed-item
  "creates a feed item"
  {:added "0.1"}
  [:text i-class
   :uuid  i-feed-id
   :jsonb m
   :jsonb o-op]
  (let [v-m (|| m {:feed-id i-feed-id
                   :class i-class})
        v-item (pg/t:insert -/FeedItem
                 v-m
                 {:track o-op
                  :columns [:feed :type :data :text :value]
                  :coalesce true})]
    (return v-item)))

(defn.pg write-feed-item
  "creates a feed item"
  {:added "0.1"}
  [:text i-class
   :uuid  i-feed-id
   :jsonb m
   :jsonb o-op]
  (let [(:text v-status) (pg/t:get-field -/Feed
                           {:where {:id i-feed-id
                                    :class i-class}
                            :returning :status})
        _ (pg/assert [v-status :is-not-null]
            [:system/feed-not-found #{i-feed-id
                                      i-class}])
        _ (pg/assert (or (== v-status "active")
                         (== v-status "published")
                         (== v-status "hidden"))
            [:system/feed-invalid-status #{i-feed-id
                                           i-class
                                           v-status}])]
    (return (-/insert-feed-item i-class i-feed-id m o-op))))


;;
;; Starred
;;

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassPart]
              :append  [-/LogType]}
  Starred
  []
  {:partition-by [:list #{"class"}]})

(def StarredPart
  [:starred        {:type :ref :required true
                    :ref  {:ns `-/Starred}
                    :sql {:cascade true
                          :partition true
                          :unique ["starred"]}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-starred-class
  [:text i-class
   :uuid i-starred-id]
  (pg/t:exists -/Starred
    {:where {:id i-starred-id
             :class i-class}}))

(defn.pg ^{:%% :sql}
  insert-starred
  "creates an starred entry"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Starred
    {:class i-class
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg  ^{:%% :sql}
  purge-starred
  "Removes an starred entry from the database."
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref]
  (pg/t:delete -/Starred
    {:where {:class i-class
             :class-ref i-class-ref}
     :single true}))

;;
;; Social
;;

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdClass
                        -/ClassPart]
              :append  [-/DetailPart
                        -/LogType]
              :api/meta  {:sb/rls true
                          :sb/access {:admin :all
                                      :auth  :select
                                      :anon  :none}}}
  Social
  "Connection to social media provider"
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(def SocialPart
  [:social          {:type :ref :required true
                     :ref  {:ns `-/Social}
                     :sql  {:unique ["social"]
                            :cascade true
                            :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-social-class
  [:uuid i-social-id
   :text i-class]
  (pg/t:exists -/Social
    {:where {:id i-social-id
             :class i-class}}))

(defn.pg ^{:%% :sql}
  insert-social
  "creates an social entry"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Social
    {:class i-class
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg  ^{:%% :sql}
  purge-social
  "Removes an social entry from the database."
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref]
  (pg/t:delete -/Social
    {:where {:class i-class
             :class-ref i-class-ref}
     :single true}))

;;
;; SocialHandle
;;

(defenum.pg ^{}
  EnumSocialHandleType
  [:twitter :facebook :linkedin :instagram :tiktok])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        [-/ClassEntryPart
                         :class {:sql {:unique ["social"]}}]
                        -/SocialPart]
              :append  [-/RecordType]
              :api/meta  {:sb/rls true
                          :sb/access {:admin :all
                                      :auth  :select
                                      :anon  :none}}}
  SocialHandle
  "Connection to social media provider"
  {:added "0.1"}
  [:type         {:type :enum :required true
                  :enum {:ns -/EnumSocialHandleType}
                  :sql {:unique ["social"]}}
   :handle       {:type :text :required true
                  :sql {:unique ["social"]}}
   :auth         {:type :map}]
  {:partition-by [:list #{"class"}]})

(defn.pg ^{:%% :sql}
  create-social-handle
  "creates a social handle"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-social-id
   :text  i-type
   :text  i-handle
   :jsonb i-auth
   :jsonb o-op]
  (pg/t:upsert -/SocialHandle
    {:type i-type
     :class  i-class
     :handle i-handle
     :social i-social-id
     :auth i-auth}
    {:on-conflict #{:class :social :type :handle}
     :track o-op}))

(defn.pg ^{:%% :sql}
  update-social-handle
  "updates a social handle"
  {:added "0.1"}
  [:text  i-class
   :uuid i-handle-id
   :jsonb i-auth
   :jsonb o-op]
  (pg/t:update -/SocialHandle
    {:where {:id i-handle-id
             :class i-class}
     :set {:auth i-auth}
     :single true
     :track o-op}))

(defn.pg  ^{:%% :sql}
  purge-social-handle
  "purges a social handle"
  {:added "0.1"}
  [:text  i-class
   :uuid i-handle-id]
  (pg/t:delete -/SocialHandle
    {:where {:id i-handle-id
             :class i-class}
     :single true}))

;;
;; Access
;;

(defenum.pg EnumAccessSetting
  [:public   ;; Anyone can join without request
   :open     ;; Need to ask to join
   :closed   ;; Need to be invited
   ])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        -/ClassTablePart]
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Access
  {:added "0.1"}
  [:setting     {:type :enum :required true :scope :-/info
                 :enum {:ns -/EnumAccessSetting}
                 :sql {:default "closed"}}]
  {:partition-by [:list #{"class"}]})

(def AccessPart
  [:access     {:type :ref :required true
                :ref {:ns `-/Access}
                :sql {:unique ["access"]
                      :cascade true
                      :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-access-class
  [:text i-class
   :text i-class-table
   :uuid i-access-id]
  (pg/t:exists -/Access
    {:where {:id i-access-id
             :class i-class
             :class-table i-class-table}}))

(defn.pg ^{:%% :sql}
  insert-access
  "creates an access entry"
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref
   :text  i-setting
   :jsonb o-op]
  (pg/t:insert -/Access
    {:class i-class
     :class-table i-class-table
     :class-ref i-class-ref
     :setting i-setting}
    {:track o-op}))

(defn.pg  ^{:%% :sql}
  purge-access
  "Removes an access entry from the database."
  {:added "0.1"}
  [:text  i-class
   :text  i-class-table
   :uuid  i-class-ref]
  (pg/t:delete -/Access
    {:where {:class i-class
             :class-table i-class-table
             :class-ref i-class-ref}
     :single true}))

;;
;; Chat
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        [-/ClassTablePart
                         :class-table  {:primary nil :ignore true
                                        :sql  {:raw [:generated-always-as '((++ "Chat" -/EnumTablename)) :stored]}}]]
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Chat
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(defn.pg ^{:%% :sql}
  insert-chat
  "creates a chat entry"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Chat
    {:class i-class
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  purge-chat
  "purges a chat entry"
  {:added "0.1"}
  [:uuid i-chat-id]
  (pg/t:delete -/Chat
    {:where {:id i-chat-id}
     :single true}))

(def ChatPart
  [:chat         {:type :ref :required true
                  :ref {:ns '-/Chat}
                  :sql {:cascade true
                        :partition true
                        :unique ["chat"]}}])

;;
;; ChatChannel
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        [-/ClassTableEntryPart
                         :class {:sql {:unique ["chat" "access" "rev"]}}
                         :class-table  {:primary nil :ignore true
                                        :sql  {:raw [:generated-always-as '((++ "ChatChannel" -/EnumTablename)) :stored]}}]

                        [-/NamePart :name {:sql {:unique ["chat"]}}]
                        -/ChatPart]
              :append  [-/IsPublicPart
                        -/DetailPart
                        -/AccessPart
                        -/RevPart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  ChatChannel
  "A chat channel for the campaign"
  {:added "0.1"}
  [:description  {:type :text  :required true}]
  {:partition-by [:list #{"class"}]})

(defn.pg create-chat-channel
  "creates a chat channel"
  {:added "0.1"}
  [:text    i-class
   :uuid    i-chat-id
   :text    i-name
   :text    i-description
   :boolean i-is-public
   :jsonb   i-detail
   :jsonb o-op]
  (let [(:uuid v-channel-id) (pg/uuid-generate-v4)
        o-access  (-/insert-access i-class "ChatChannel" v-channel-id o-op)
        o-rev     (-/insert-rev i-class "ChatChannel" v-channel-id nil o-op)
        o-channel (pg/t:insert -/ChatChannel
                    {:id v-channel-id
                     :class i-class
                     :access (pg/id o-access)
                     :rev (pg/id o-rev)
                     :chat i-chat-id
                     :name i-name
                     :description i-description
                     :is-public i-is-public}
                    {:track o-op})
        _ [:perform (-/commit-rev i-class "ChatChannel" v-channel-id o-channel)]]
    (return o-channel)))

(defn.pg update-chat-channel
  "updates a feed "
  {:added "0.1"}
  [:text i-class
   :uuid i-channel-id
   :jsonb m
   :jsonb o-op]
  (let [o-channel (pg/t:update -/ChatChannel
                    {:where {:id i-channel-id
                             :class i-class}
                     :set m
                     :columns [:name :is-public :detail]
                     :single true
                     :track o-op})
        _ [:perform (-/commit-rev i-class "ChatChannel" i-channel-id o-channel)]]
    (return o-channel)))

(defn.pg purge-chat-channel
  "purges a chat channel"
  {:added "0.1"}
  [:text    i-class
   :uuid i-channel-id]
  (let [o-channel (pg/t:delete -/ChatChannel
                    {:where {:id i-channel-id
                             :class i-class}
                     :single true})
        o-rev    (-/purge-rev i-class "ChatChannel" i-channel-id)
        o-access (-/purge-access i-class "ChatChannel" i-channel-id)]
    (return (|| o-channel
                #{o-rev
                  o-access}))))


;;
;; Playable
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/Id]
              :append  [-/DetailPart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Playable
  "Allow classification of tokens and games

   (pg/t:exists -/Playable)
   => true"
  {:added "0.1"}
  [])

(def PlayablePart
  [:playable        {:type :ref :required true
                     :ref  {:ns `-/Playable}
                     :sql {:unique ["playable"]
                           :cascade true}}])

(defn.pg ^{:%% :sql}
  create-playable
  "creates a playable entry"
  {:added "0.1"}
  [:jsonb i-detail
   :jsonb o-op]
  (pg/t:insert -/Playable
    {:detail i-detail}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  create-playable-root
  "creates the root playable"
  {:added "0.1"}
  [:jsonb o-op]
  (pg/t:upsert -/Playable
    {:id "00000000-0000-0000-0000-000000000000"
     :detail {:root true}}
    {:track o-op}))

(defn.pg purge-playable
  "Removes a playable entry from the database."
  {:added "0.1"}
  [:uuid  i-playable-id]
  (let [o-playable (pg/t:delete -/Playable
                     {:where {:id  i-playable-id}
                      :single true})
        o-metadata (-/purge-metadata
                    (pg/full -/Playable)
                    (:text i-playable-id))]
    (return (|| o-playable #{o-metadata}))))

;;
;; Wallet
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        -/ClassPart]
              :append  [-/IsPublicPart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Wallet
  "A wallet to hold assets

   ;; User creation triggers Wallet
   (pg/t:exists -/Wallet)
   => true"
  {:added "0.1"}
  []
  {:partition-by [:list #{"class"}]})

(def WalletPart
  [:wallet        {:type :ref :required true
                   :ref  {:ns `-/Wallet}
                   :sql {:cascade true
                         :partition true}}])

(defn.pg ^{:%% :sql
           :- [:boolean]}
  is-wallet-class
  [:uuid i-wallet-id
   :text i-class]
  (pg/t:exists -/Wallet
    {:where {:id i-wallet-id
             :class i-class}}))

(defn.pg ^{:%% :sql}
  create-wallet
  "creates a feed wallet"
  {:added "0.1"}
  [:text  i-class
   :uuid  i-class-ref
   :jsonb o-op]
  (pg/t:insert -/Wallet
    {:id v-id
     :class i-class
     :class-ref i-class-ref}
    {:track o-op}))

(defn.pg ^{:%% :sql}
  purge-wallet
  "Removes a wallet entry from the database."
  {:added "0.1"}
  [:uuid  i-wallet-id]
  (pg/t:delete -/Wallet
    {:where {:id  i-wallet-id}
     :single true}))

;;
;; Token
;;

(defenum.pg ^{}
  EnumTokenType [:fiat
                 :platform
                 :brand
                 :erc20
                 :erc20_testnet])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/Id
                        [-/ClassTableEntryPart
                         :class {:primary nil}
                         :class-table {:primary nil}]
                        -/NamePart
                        -/ColorPart]
              :append  [-/ContentPicturePart
                        -/DetailPart
                        -/AuditPart
                        -/RevPart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Token
  "Defines the structure for currencies and tokens within the system.

   (pg/t:get -/Token {:where {:id \"USD\"}})
   => (contains {:type \"fiat\"})"
  {:added "0.1"
   :sql {:constraints ["CONSTRAINT fk_token_wallet FOREIGN KEY (issuer_id, class) REFERENCES \"szn_type\".\"Wallet\" (id, class) ON DELETE RESTRICT"
                       "CONSTRAINT fk_token_rev FOREIGN KEY (rev_id, class, class_table) REFERENCES \"szn_type\".\"Rev\" (id, class, class_table) ON DELETE CASCADE"]}}
  [:code         {:type :citext :scope :-/info
                  :sql  {:unique  ["default"]}
                  :profiles {:web {:edit #{:create :modify}
                                   :example "Australian Dollar"}}}
   :type         {:type :enum :required true :scope :-/info
                  :enum {:ns -/EnumTokenType}
                  :profiles {:web {:edit #{:create :modify}
                                   :example "fiat"}}}
   :symbol       {:type :text :scope :-/info
                  :profiles {:web {:edit #{:create :modify}
                                   :example "$"}}}
   :native       {:type :text :scope :-/info
                  :profiles {:web {:edit #{:create :modify}
                                   :example "$AU"}}}
   :decimal      {:type :integer
                  :profiles {:web {:edit #{:create :modify}
                                   :example 2}}}
   :title        {:type :text
                  :profiles {:web {:edit #{:create :modify}
                                   :type "text"
                                   :example "Australian Dollar"}}}
   :issuer       {:type :ref
                  :ref  {:ns -/Wallet
                         :cascade true}
                  :sql  {:unique  ["default"]}}])

;;
;; Token
;;

(defn.pg create-token
  "creates a token entry"
  {:added "0.1"}
  [:citext i-token-name
   :jsonb  m
   :jsonb  o-op]
  (let [(:uuid v-token-id) (pg/uuid-generate-v4)
        o-audit (-/create-audit "Token" "Default" v-token-id o-op)
        o-rev   (-/insert-rev "Token" "Default" v-token-id nil o-op)
        v-m     (|| m
                    {:id v-token-id
                     :name i-token-name
                     :audit-id (pg/id o-audit)
                     :rev-id   (pg/id o-rev)})
        o-token (pg/t:insert -/Token
                  v-m
                  {:track o-op
                   :coalesce true})
        _       [:perform (-/commit-rev "Token" v-token-id o-token)]]
    (return o-token)))

(defn.pg set-token
  "Creates or updates a token entry in the database."
  {:added "0.1"}
  [:jsonb i-token
   :jsonb i-log-entry
   :jsonb o-op]
  (let [#{(:citext v-name)} i-token
        o-token  (pg/t:update -/Token
                   {:set   i-token
                    :where {:name v-name}
                    :single true
                    :columns [:code :type :symbol :native :decimal :title :picture :detail]
                    :track o-op})
        _ (if [o-token :is-null]
            (:= o-token (-/create-token v-name i-token o-op))
            [:perform (-/commit-rev "Token"
                                    "Default"
                                    (:uuid (:->> o-token "idref"))
                                    o-token)])

        o-log     (-/create-audit-log
                   "Token"
                   "Default"
                   (:uuid (:->> o-token "audit_id"))
                   i-log-entry
                   o-op)]
    (return (|| o-token #{o-log}))))

(defn.pg ^{:%% :sql}
  set-tokens
  "Creates or updates multiple token entries in the database."
  {:added "0.1"}
  [:jsonb i-tokens
   :jsonb i-log-entry
   :jsonb o-op]
  (pg/map:js -/set-token v-tokens i-log-entry o-op))

(defn.pg purge-token
  "Removes a token entry from the database."
  {:added "0.1"}
  [:citext  i-token-name]
  (let [o-token    (pg/t:delete -/Token
                     {:where {:name i-token-name}
                      :single true})
        #{(:uuid v-audit-id)
          (:uuid v-rev-id)} o-token
        o-audit    (-/purge-audit v-audit-id)
        o-rev      (-/purge-rev v-rev-id)]
    (return (|| o-token
                #{o-audit
                  o-rev}))))

;;
;; Commodity
;;


(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :append  [-/BalancePart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  TokenSupply
  "Tracks the token supply on the platform

   (pg/t:get -/TokenSupply {:where {:token \"USD\"}})
   => (contains {:balance 1000})"
  {:added "0.1"}
  [:token      {:primary "default"
                :type :ref :required true
                :ref {:ns -/Token}
                :sql  {:cascade true}
                :profiles {:web {:example "STC"}}}
   :playable      {:primary "default"
                   :type :ref
                   :ref {:ns -/Playable}
                   :sql {:cascade true}}])

(defn.pg update-token-supply
  "sets the token supply"
  {:added "0.1"}
  [:citext  i-token-id
   :uuid    i-playable-id
   :numeric i-value
   :jsonb o-op]
  (let [o-supply (pg/t:update -/TokenSupply
                   {:set   {:balance (+ #{"balance"} i-value)}
                    :where {:token i-token-id
                            :playable i-playable-id}
                    :single true
                    :track o-op})
        _ (when [o-supply :is-not-null]
            (return o-supply))
        o-supply  (pg/t:insert -/TokenSupply
                    {:token i-token-id
                     :playable i-playable-id
                     :balance i-value}
                    {:track o-op})]
    (return o-supply)))

(defn.pg ^{:%% :sql}
  purge-token-supply
  "purges the token supply"
  {:added "0.1"}
  [:citext  i-token-id
   :uuid i-playable-id]
  (pg/t:delete -/TokenSupply
    {:where {:token  i-token-id
             :playable i-playable-id}
     :single true}))

;;
;; Commodity
;;

(defenum.pg ^{}
  EnumCommodityType
  [:digital :achievement :physical :access :experience :discount])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdText]
              :append  [-/ContentPicturePart
                        -/DetailPart
                        -/AuditPart
                        -/RevPart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Commodity
  "Defines the structure for items, badges, and rewards.

   (pg/t:get -/Commodity {:where {:id \"ITEM1\"}})
   => (contains {:type \"digital\"})"
  {:added "0.1"}
  [:idref        {:type :uuid :required true :unique true
                  :sql {:default (pg/uuid-generate-v4)}}
   :type         {:type :enum :required true :scope :-/info
                  :enum {:ns -/EnumCommodityType}}
   :code         {:type :citext :scope :-/info
                  :sql  {:unique  true}}
   :issuer       {:type :ref
                  :ref  {:ns -/Wallet}
                  :sql  {:cascade true}}])



;;
;; Commodity
;;

(defn.pg create-commodity
  "creates a commodity entry"
  {:added "0.1"}
  [:citext i-commodity-id
   :jsonb  m
   :jsonb  o-op]
  (let [v-idref (pg/uuid-generate-v4)
        o-audit (-/create-audit "Commodity" v-idref o-op)
        o-rev   (-/insert-rev "Commodity" v-idref nil o-op)
        v-m     (|| m
                    {:idref v-idref
                     :audit-id (pg/id o-audit)
                     :rev-id   (pg/id o-rev)})
        o-commodity (pg/t:insert -/Commodity
                      v-m
                      {:track o-op
                       :coalesce true})
        _       [:perform (-/commit-rev "Commodity" v-idref o-commodity)]]
    (return o-commodity)))

(defn.pg set-commodity
  "Creates or updates a commodity entry in the database."
  {:added "0.1"}
  [:jsonb i-commodity
   :jsonb i-log-entry
   :jsonb o-op]
  (let [#{(:citext v-id)} i-commodity
        o-commodity  (pg/t:update -/Commodity
                   {:set   i-commodity
                    :where {:id v-id}
                    :single true
                    :columns [:code :type :picture :detail]
                    :track o-op})
        _ (if [o-commodity :is-null]
            (:= o-commodity (-/create-commodity i-commodity o-op))
            [:perform (-/commit-rev "Commodity"
                                    (:uuid (:->> o-commodity "idref"))
                                    o-commodity)])

        o-log     (-/create-audit-log
                   (:uuid (:->> o-commodity "audit_id"))
                   i-log-entry
                   o-op)]
    (return (|| #{o-commodity
                  o-log}))))

(defn.pg ^{:%% :sql}
  set-commodities
  "Creates or updates multiple commodity entries in the database."
  {:added "0.1"}
  [:jsonb i-commodities
   :jsonb i-log-entry
   :jsonb o-op]
  (pg/map:js -/set-commodity v-commodities i-log-entry o-op))

(defn.pg purge-commodity
  "Removes a commodity entry from the database."
  {:added "0.1"}
  [:citext  i-commodity-id]
  (let [o-commodity    (pg/t:delete -/Commodity
                     {:where {:id  i-commodity-id}
                      :single true})
        #{(:uuid v-audit-id)
          (:uuid v-rev-id)}
        o-audit    (-/purge-audit v-audit-id)
        o-rev      (-/purge-rev v-rev-id)]
    (return (|| o-commodity
                #{o-audit
                  o-rev}))))

;;
;; Asset
;;

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/Id]
              :append  [-/BalancePart
                        -/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Asset
  "Defines the structure for an asset type held within a wallet.

   (pg/t:exists -/Asset)
   => true"
  {:added "0.1"}
  [:wallet        {:type :ref :required true
                   :ref {:ns -/Wallet}
                   :sql {:cascade true
                         :unique  ["default"]}}
   :token         {:type :ref :required true
                   :ref {:ns -/Token}
                   :sql  {:unique ["default"]
                          :process -/as-upper
                          :cascade true}
                   :profiles {:web {:example "STC"}}}
   :playable      {:type :ref
                   :ref {:ns -/Playable}
                   :sql {:unique  ["default"]
                         :cascade true}}])

(defenum.pg ^{}
  EnumAssetTxType [:action
                   :mint
                   :mint_to
                   :burn
                   :burn_from
                   :withdraw
                   :deposit
                   :claim
                   :redeem
                   :transfer_in
                   :transfer_out
                   :stake_mint
                   :stake_burn
                   :stake_buy
                   :stake_sell
                   :stake_payout])

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdLog]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  AssetTx
  "Asset transaction record

   (pg/t:exists -/AssetTx)
   => true"
  {:added "0.1"}
  [:asset       {:type :ref :required true
                 :ref {:ns -/Asset}
                 :sql {:cascade true}}
   :type        {:type :enum :required true :scope :-/info
                 :enum {:ns -/EnumAssetTxType}}
   :amount      {:type :numeric :required true}
   :balance     {:type :numeric :required true}
   :ref-id      {:type :uuid}])

(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/Id]
              :append  [-/DataType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Item
  "Defines the structure for an asset type held within a wallet.

   (pg/t:exists -/Item)
   => true"
  {:added "0.1"}
  [:type          {:type :ref :required true
                   :ref {:ns -/Commodity}
                   :sql  {:cascade true
                          :unique ["default"]}
                   :profiles {:web {:example "STC"}}}
   :key           {:type :text :required true
                   :sql  {:unique ["default"]
                          :default "main"}
                   :profiles {:web {:example "STC"}}}
   :value         {:type :map
                   :web  {:example {:color "red"}}}
   :wallet        {:type :ref :required true
                   :ref {:ns -/Wallet}
                   :sql {:cascade true}}
   :playable      {:type :ref
                   :ref {:ns -/Playable}
                   :sql {:cascade true}}])

(defenum.pg ^{}
  EnumItemTxType [:mint
                  :transfer
                  :burn])

(deftype.pg ^{:public true
              :track   [-/TrackingLog]
              :prepend [-/IdLog]
              :append  [-/LogType]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  ItemTx
  "Asset item transaction record

   (pg/t:exists -/ItemTx)
   => true"
  {:added "0.1"}
  [:type        {:type :enum :required true :scope :-/info
                 :enum {:ns -/EnumItemTxType}}
   :item        {:type :ref  :required true
                 :ref {:ns -/Item}
                 :sql  {:cascade true}}
   :from        {:type :ref  :required true
                 :ref {:ns -/Wallet}
                 :sql  {:cascade true}}
   :to          {:type :ref  :required true
                 :ref {:ns -/Wallet}
                 :sql  {:cascade true}}])
