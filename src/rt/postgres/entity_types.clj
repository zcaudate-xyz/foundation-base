(ns rt.postgres.entity-types
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.postgres :as pg]))

(defonce +app+
  (atom {}))

(defonce ^:dynamic *ns-str* "-")

(defn get-app
  [& [ns]]
  (let [ns (or ns (h/ns-sym))]
    (first (:application
            (:static (h/suppress
                      (l/rt:module
                       (l/rt ns :postgres))))))))

(defn get-app-ns-str
  [& [ns]]
  (get @+app+ (get-app ns)))

(defn init-app
  [& [ns schema]]
  (let [ns  (or ns (h/ns-sym))
        app (get-app ns)]
    (swap! +app+ assoc app (name ns))))


;;
;; Default Types
;;

(defn type-id-v1
  []
  {:type :uuid :primary "primary"
   :sql {:default '(rt.postgres/uuid-generate-v1)}})

(defn type-id-v4
  []
  {:type :uuid :primary "primary"
   :sql {:default '(rt.postgres/uuid-generate-v4)}})

(defn type-id-text
  [ns-str]
  {:type :citext :primary "primary"
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 100]]}})

(defn type-name
  [ns-str]
  {:type :citext :required true :scope :-/info
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 36]]
         :unique ["name"]}})

(defn type-image
  [ns-str]
  {:type :image
   :sql  {:process [[(symbol ns-str "as-jsonb")]]
          :default "{}"}
   :profiles {:web {:type "image"}}})

(defn type-color
  [ns-str]
  {:type :citext :required true :scope :-/info
   :sql {:default    (list (symbol ns-str "color-rand"))
         :constraint (list (symbol ns-str "color-check") #{"color"})}
   :profile {:web {:edit #{:create :modify}
                   :type "color"}}})

(defn type-tags
  [ns-str]
  {:type :array :scope :-/info
   :sql {:process [[(symbol ns-str "as-jsonb-array")]]
         :default "[]"}
   :profile {:web {:edit #{:create :modify}
                   :type "chip"}}})

(defn type-log
  [ns-str]
  {:type :array :required true
   :scope :-/detail
   :sql  {:process [[(symbol ns-str "as-jsonb-array")]]
          :default "[]"}
   :map  {:status  {:type :text}
          :message {:type :text}
          :error   {:type :text}}})

(defn type-boolean
  [& [default]]
  {:type :boolean :required true
   :sql {:default (not (not default))}})

(defn type-class-table
  [ns-str]
  {:type :enum :scope :-/system
   :enum {:ns (symbol ns-str "EnumClassTableType")}})

(defn type-class-context
  [ns-str]
  {:type :enum :scope :-/system
   :enum {:ns  (symbol ns-str "EnumClassContextType")}})

(defn type-ref
  [ns-str table-str]
  {:type :ref :required true
   :ref {:ns (symbol ns-str table-str)}})

(defn type-class-ref
  []
  {:type :uuid :required true})

(defonce +fields+
  (atom {}))

(defn default-fields
  [ns-str]
  {:id            {:priority     0  :field    (type-id-v4)}
   :class-table   {:priority     1  :field    (type-class-table ns-str)}
   :class-context {:priority     2  :field    (type-class-context ns-str)}
   :name          {:priority     5  :field    (type-name  ns-str)}
   :icon          {:priority    10  :field    (type-image ns-str)}
   :picture       {:priority    11  :field    (type-image ns-str)}
   :background    {:priority    12  :field    (type-image ns-str)}
   :color         {:priority    20  :field    (type-color ns-str)}
   :tags          {:priority    25  :field    (type-tags  ns-str)}
   :is-active     {:priority    30  :field    (type-boolean true)}
   :is-public     {:priority    31  :field    (type-boolean true)}
   :is-official   {:priority    80  :field    (type-boolean false)}
   :is-onboarded  {:priority    81  :field    (type-boolean false)}
   :rev           {:priority    99  :field    (type-ref ns-str "Rev")  :type :2d/entity}
   :log           {:priority   100  :field    (type-log   ns-str)}})

;;
;; Default Addons
;;

(defonce +addons+
  (atom {}))

(defn addons-init
  [& [m ns-str]]
  (reset! +addons+ (merge m (if ns-str
                              (default-fields ns-str)))))

(defn addons-add
  [field addon]
  (swap! +addons+ assoc field addon))

(defn addons-remove
  [field]
  (swap! +addons+ dissoc field))


;;
;; Default Tracking
;;

(defn config-tracking
  [track]
  (case track
    (:track/record :track/data)
    {:name   "data"
     :in     {:create {:time-created :time
                       :time-updated :time
                       :op-created :id
                       :op-updated :id}
              :modify {:time-updated :time
                       :op-updated :id}}
     :ignore #{:delete}}

    :track/temp
    {:name    "temp"
     :in      {:create  {:time-created :time
                         :time-updated :time}
               :modify {:time-updated :time}}
     :ignore  #{:delete}}
    
    :track/log
    {:name    "log"
     :in      {:create {:op-created :id :time-created :time}}
     :disable #{:modify}
     :ignore  #{:delete}}

    :track/none {}))

(defn config-tracking-columns
  [track]
  (case track
    :track/record [:op-created   {:type :uuid}
                   :op-updated   {:type :uuid}
                   :time-created {:type :time}
                   :time-updated {:type :time}
                   :__deleted__  {:type :boolean
                                  :scope :-/hidden
                                  :sql {:default false}}]
    
    :track/data    [:op-created   {:type :uuid}
                    :op-updated   {:type :uuid}
                    :time-created {:type :time}
                    :time-updated {:type :time}]
    
    :track/log     [:op-created   {:type :uuid}
                    :time-created {:type :time}]

    :track/temp    [:time-created {:type :time}
                    :time-updated {:type :time}]

    :track/none     []))

(defn config-access
  [access]
  (if (not= access :none)
    {:sb/rls true
     :sb/access {:admin :all
                 :auth  (case access
                          (:access/hidden) :none
                          (:access/system
                           :access/auth
                           :access/public)  :select)
                 :anon  (case access
                          (:access/hidden
                           :access/system)  :none
                          (:access/auth
                           :access/public)  :select)}}))

(defn with-priority
  [arr priority]
  (mapv (fn [m]
          (if (map? m)
            (assoc m :priority priority)
            m))
        arr))

;;
;; E
;;

(def ESpec
  {:id        #{:id/none :id/v4 :id/v1  :id/text map?}
   :class     #{:none
                :0d/entity :0d/log
                :1d/entity :1d/log
                :2d/entity :2d/log}
   :track    #{:track/none
               :track/data
               :track/log
               :track/record
               :track/time}
   :access   #{:access/public
               :access/auth
               :access/system
               :access/hidden
               :none}
   :addons   #{:name}})

(defn check-E
  [m]
  (let [errors (reduce-kv (fn [acc k allowed]
                            (if (contains? m k)
                              (let [v (get m k)]
                                (cond
                                  (= k :addons)
                                  (let [vs (if (coll? v) v [v])
                                        bad (remove allowed vs)]
                                    (if (seq bad)
                                      (assoc acc k {:invalid bad :allowed allowed})
                                      acc))
                                  
                                  :else
                                  (let [preds (filter fn? allowed)
                                        lits  (set (remove fn? allowed))]
                                    (if (or (contains? lits v)
                                            (some #(% v) preds))
                                      acc
                                      (assoc acc k {:value v :allowed allowed})))))
                              acc))
                          {}
                          ESpec)]
    (if (seq errors)
      (throw (ex-info "Invalid inputs for E" {:errors errors :input m}))
      m)))

(defn E
  [{:keys [id class track access columns fields addons ns-str]
    :or {ns-str  (or *ns-str* (get-app-ns-str)
                     (h/error "No ns-str found." {:ns (h/ns-sym)}))
         class   :none
         access  :access/auth
         columns []}
    :as m}]
  (let [[id-in
         track-in] (case (name class)
                     "log"    [:id/v1   :track/log]
                     "entity" [:id/v4   :track/data]
                     "none"   [:id/none :track/none])
        [id track] [(or id id-in) (or track track-in)]
        id   (cond (map? id) id
                   :else (case id
                           :id/v1   (type-id-v1)
                           :id/v4   (type-id-v4)
                           :id/text (type-id-text ns-str)
                           :id/none nil))
        
        access-val   (config-access access)
        track-val    (config-tracking track)
        track-cols   (config-tracking-columns track)
        class-cols   []]
    {:api/meta access-val
     :public (if (#{:none :access/hidden} access)
               false true)
     :track track-val
     :columns [(vec (concat
                     (if id (with-priority [:id id] 0))
                     (with-priority track-cols 200)))]}))


(comment
  
  ;; having
  {:class  :0d/entity
   :entity {:table   "Global"
            :context "Global"}}

  ;; created 
  :class-table    {:type :enum
                   :enum {:ns -/EnumClassTableType}
                   :generated "Global"}
  :class-context {:type :enum
                  :enum {:ns -/EnumClassTableType}
                  :generated "Global"})

;;




(comment

  
  (deftype.pg ^{:! (et/E {:id  :id/v1
                          :access :access/system})}
    Op
    [:time         {:type :time    :required true
                    :sql {:default (pg/time-us)}}
     :tag          {:type :citext  :required true}
     :data         {:type :map
                    :web {:example {:email "test@test.com"
                                    :password "password"}}}
     :user-id      {:type :uuid}])

  (deftype.pg ^{:! (et/E {:id       :id/none
                          :track    :track/data
                          :access   :access/auth})}
    Metadata
    {:added "0.1"}
    [:id        {:type :jsonb :primary "default"}
     :entry     {:type :jsonb :required true}])


  (deftype.pg ^{:! (et/E {:class  :2d/entity
                          :access :access/system
                          :fields []})}
    Rev
    {:added "0.1"}
    [])

  (deftype.pg ^{:! (et/E {:class  :2d/log
                          :entity {:in -/Rev}
                          :access :access/system})}
    RevLog
    {:added "0.1"}
    []
    {:partition-by [:list :class-table]})

  (deftype.pg ^{:! (et/E {:class  :0d/entity
                          :entity {:table   "Global"
                                   :context "Global"}
                          :fields []
                          :addons {:rev -/Rev}})}
    Global
    {:added "0.1"}
    [:value    {:type :text   :required true}])

  
  



  )
