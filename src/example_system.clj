(ns example-system
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]))

;; Placeholder for l/script if we don't want to run it
(comment
(l/script :postgres
  {:require [[rt.postgres :as pg]
             [rt.postgres.supabase :as s]]
   :static {:application ["szn"]
            :seed        ["szn_type"]
            :all         {:schema   ["szn_type"]}}})
)

;;
;; Default Templates
;;

(def IdClass
  [:id  {:type :uuid :primary "default"
         :sql {:default `(pg/uuid-generate-v4)}}])

(def DataType
  [:op-created     {:type :uuid}
   :op-updated     {:type :uuid}
   :time-created   {:type :time}
   :time-updated   {:type :time}])

;;
;; Fragments
;;

;; Stub for defenum.pg
(def EnumClassTableType "EnumClassTableType")
(def EnumClassContextType "EnumClassContextType")

(def Class0DType
  [:class-table          {:type :enum :scope :-/info
                          :ignore true
                          :enum {:ns `-/EnumClassTableType}}
   :class-context        {:type :enum :scope :-/info
                          :ignore true
                          :sql {:raw [:generated-always-as '((++ "Global" -/EnumClassContextType)) :stored]}
                          :enum {:ns `-/EnumClassContextType}}])

(def Class1DType
  [:class-table        {:type :enum  :scope :-/info
                        :required true :primary "default"
                        :enum {:ns `-/EnumClassTableType}
                        :sql  {:unique ["class"]}}
   :class-context      {:type :enum  :scope :-/info
                        :ignore true
                        :sql {:raw [:generated-always-as '((++ "Global" -/EnumClassContextType)) :stored]}
                        :enum {:ns `-/EnumClassContextType}}
   :class-ref          {:type :uuid :required true
                        :sql  {:unique ["class"]}}])


(def Class2DType
  [:class-table       {:type :enum :required true  :scope :-/info :primary "default"
                       :enum {:ns `-/EnumClassTableType}
                       :sql  {:unique ["class"]}}
   :class-context     {:type :enum :required true :scope :-/info :primary "default"
                       :enum {:ns `-/EnumClassContextType}
                       :sql  {:unique ["class"]}}
   :class-ref         {:type :uuid :required true
                       :sql  {:unique ["class"]}}])


(def EntryPart
  [:entry          {:type :map :required true
                    :scope :-/info
                    :sql  {:process `-/as-jsonb}
                    :map  {:status  {:type :text}
                           :message {:type :text}
                           :error   {:type :text}}}])

;; Added RevPart because it appeared in the expected output
(def RevPart [:rev-part])

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

;;
;; Rev
;;

;; Stub for deftype.pg
(comment
(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        -/Class2DType]
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
  {:partition-by {:strategy :list :columns [:class-table]
                  :default {:in "szn_type_impl"}}})
)

;;
;; Global
;;

(comment
(deftype.pg ^{:public true
              :track   [-/TrackingMin]
              :prepend [-/IdClass
                        [-/Class0DType
                         :class-table {:sql  {:raw [:generated-always-as '((++ "Global" -/EnumClassTableType)) :stored]}
                                       :foreign   {:rev       {:ns -/Rev :column :class-context}}}]]
              :append  [-/DataType
                        -/RevPart]
              :api/meta {:sb/rls true
                         :sb/access {:admin :all
                                     :auth  :select
                                     :anon  :select}}}
  Global
  "Globally available variables

   (pg/t:get -/Global {:where {:id \"GLOBAL_VAR\"}})
   => (contains {:value \"test_value\"})"
  {:added "0.1"}
  [:value    {:type :text   :required true}])
)

;;
;; Type Definition
;;

(defn Type
  "Mixin for defining types"
  [{:keys [dimension name track rls addons]}]
  (let [suffix (case dimension
                 0 "Class0DType"
                 1 "Class1DType"
                 2 "Class2DType")
        class-type-sym (symbol (str "-/" suffix))

        ;; Track logic
        track-val (case track
                    :data [(symbol "-/TrackingMin")]
                    []) ;; TODO: handle other track types if known

        ;; RLS logic
        access (merge {:admin :all
                       :auth  :select}
                      (when (= rls :public)
                        {:anon :select}))

        ;; Addons logic
        rev? (some #{:rev} addons)

        ;; Prepend logic
        ;; Note: The expected output has quoted list for :generated-always-as
        ;; '((++ "Global" -/EnumClassTableType))
        class-table-override (cond-> {:sql {:raw [:generated-always-as (list (list '++ name (symbol "-/EnumClassTableType"))) :stored]}}
                               rev? (assoc :foreign {:rev {:ns (symbol "-/Rev") :column :class-context}}))

        prepend-vec [(symbol "-/IdClass")
                     [class-type-sym
                      :class-table class-table-override]]

        ;; Append logic
        append-vec (cond-> [(symbol "-/DataType")]
                     rev? (conj (symbol "-/RevPart")))]

    {:public true
     :track track-val
     :prepend prepend-vec
     :append append-vec
     :api/meta {:sb/rls true
                :sb/access access}}))
