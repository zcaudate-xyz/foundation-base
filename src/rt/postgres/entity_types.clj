(ns rt.postgres.entity-types
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]
            [std.lang.base.grammar-spec :as grammar-spec]))

(defonce +app+
  (atom {}))

(def ^:dynamic *ns-str* "-")

(defn default-application
  [& [module]]
  (first (:application
          (:static (h/suppress
                    (l/rt:module
                     (l/rt (or module
                               (h/ns-sym))
                           :postgres)))))))

(defn default-ns-str
  [& [application]]
  (get @+app+ (or application
                  (default-application (h/ns-sym)))))

(defn init-default-ns-str
  [& [application ns-str]]
  (let [ns-str (or ns-str (name (h/ns-sym)))
        application (or application (default-application (h/ns-sym)))]
    (when application 
      (swap! +app+ assoc application ns-str))))




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

(defn type-class-key
  [ns-str]
  {:type :citext :required true
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 100]]}})


(defn normalise-ref
  [ptr]
  (cond (h/pointer? ptr)
        (symbol (name (:module ptr))
                (name (:id ptr)))

        (symbol? ptr) ptr

        (var? ptr) (symbol (name (.getName (.ns ptr)))
                           (name (.sym ptr)))))

(defonce +fields+
  (atom {}))

(defn default-fields
  [ns-str]
  {:id            {:priority     0  :field    (type-id-v4)}
   :class-table   {:priority     1  :field    (type-class-table ns-str)}
   :class-context {:priority     2  :field    (type-class-context ns-str)}
   :class-ref     {:priority     3  :field    (type-class-ref)}
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
   :log           {:priority   100  :field    (type-log   ns-str)}})

;;
;; Default Addons
;;

(defonce +addons+
  (atom {}))

(defn init-addons
  [& [m application]]
  (let [application   (or application (default-application (h/ns-sym)))
        ns-str   (default-ns-str application)]
    (when application 
      (swap! +addons+ assoc application (merge m (default-fields ns-str))))))

(defn get-addon
  [key]
  (get-in @+addons+ [(default-application (h/ns-sym))
                     key]))

(defn add-addon
  [key field priority]
  (let [application (default-application (h/ns-sym))]
    (swap! +addons+ assoc-in [application key] {:field field
                                                :priority priority})))

(defn addons-remove
  [key]
  (swap! +addons+ update-in [(default-application (h/ns-sym))] dissoc key))


;;
;; Default Tracking
;;

(defn get-tracking
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

(defn get-tracking-columns
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

(defn get-access
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
  (let [curr (volatile! -1)]
    (mapv (fn [m]
            (if (map? m)
              (assoc m :priority priority :priority-index (vswap! curr inc))
              m))
          arr)))

;;
;; E
;;

(def ESpec
  {:id        #{:id/none :id/v4 :id/v1  :id/text map?}
   :class     #{:none
                :0d/entry :0d/log
                :1d/base :1d/entry :1d/log
                :2d/base :2d/entry :2d/log}
   :track    #{:track/none
               :track/data
               :track/log
               :track/record
               :track/time}
   :access   #{:access/public
               :access/auth
               :access/system
               :access/hidden
               :none}})

(defn E-check-input
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


;;
;; 
;;

(defn E-class-entry-fields
  [{:keys [symname class entity ns-str]
    :or {symname "Global"}
    :as m}]
  (let [{:keys [for]} entity
        _    (when-not for
               (h/error "Need a :for keyword" {:input m}))
        [key ref] (if (vector? for)
                    for
                    [(keyword (str/lower-case symname)) for])
        ref (normalise-ref ref)]
    {:class-table   {:foreign {key {:ns ref :column :class-table}}}
     :class-context {:foreign {key {:ns ref :column :class-context}}}
     key {:type :ref :primary "default"
          :required true
          :ref {:ns ref}
          :sql {:cascade true}}}))

(defn E-class-columns
  [{:keys [symname class entity ns-str]
    :or {ns-str *ns-str*
         class   :none
         symname "Global"}
    :as m}]
  (let [{:keys [context]
         :or {context "Global"}} entity
        entry-fields (if (#{:1d/entry
                            :1d/log
                            :2d/entry
                          :2d/log} class)
                       (E-class-entry-fields m))
        base (cond-> {:class-table   (assoc (type-class-table ns-str)   :priority 2)
                      :class-context (assoc (type-class-context ns-str) :priority 3)}
               :then (h/merge-nested entry-fields)
               (#{:1d/base
                  :2d/base} class)  (assoc :class-ref  (assoc (type-class-ref)
                  :priority 4
                  :sql {:unique ["class"]})))]
    (case (namespace class)
      "0d" (h/merge-nested base
                           {:class-table   {:generated symname}
                            :class-context {:generated context}})
      "1d" (h/merge-nested base
                           {:class-table   {:primary "default"}
                            :class-context {:generated  context}}
                           (if (= class :1d/base)
                             {:class-table   {:sql {:unique ["class"]}}}))
      
      "2d"  (h/merge-nested base
                            {:class-table   {:primary "default"}
                             :class-context {:primary "default"}}
                            (if (= class :2d/base)
                              {:class-table   {:sql {:unique ["class"]}}
                               :class-context {:sql {:unique ["class"]}}}))
      {})))

(defn E-main
  [{:keys [id class entity track access raw columns ns-str application]
    :as m}]
  (let [
        [id-in
         track-in] (case (name class)
         "log"    [:id/none   :track/log]
         "base"   [:id/v4     :track/log]
         "entry"  [:id/v4     :track/data]
         "none"   [:id/none   :track/none])
        [id track] [(or id id-in) (or track track-in)]
        id   (cond (map? id) id
                   :else (case id
                           :id/v1   (type-id-v1)
                           :id/v4   (type-id-v4)
                           :id/text (type-id-text ns-str)
                           :id/none nil))
        
        access-val   (get-access access)
        track-val    (get-tracking track)
        track-cols   (get-tracking-columns track)
        class-cols   (E-class-columns m)
        all-cols     (merge (if id {:id (assoc id :priority 0)})
                            (apply hash-map (with-priority track-cols 200))
                            class-cols)]
    {:api/meta   access-val
     :api/input (dissoc m :ns-str :application)
     :public (if (#{:none :access/hidden} access)
               false true)
     :track track-val
     :raw (->> all-cols
               (sort-by (fn [[k v]]
                          [(or (:priority v) 50)
                           (or (:priority-index v) 0)]))
               vec)}))

(defn E
  [{:keys [id class entity track access raw columns ns-str application]
    :as m}]
  (let [m (merge {:symname  (if grammar-spec/*symbol* (name grammar-spec/*symbol*))
                  :class   :none
                  :access  :access/auth
                  :columns []
                  :ns-str  (or (default-ns-str application)
                               *ns-str* 
                               (h/error "No ns-str found." {:ns (h/ns-sym)}))}
                 m)
        _ (E-check-input m)]
    (E-main m)))


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


  (deftype.pg ^{:! (et/E {:class  :2d/entry
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

  (deftype.pg ^{:! (et/E {:class  :0d/entry
                          :entity {:table   "Global"
                                   :context "Global"}
                          :fields []
                          :addons {:rev -/Rev}})}
    Global
    {:added "0.1"}
    [:value    {:type :text   :required true}])

  
  



  )


(comment

  (def Class2DType
  [:class-table       {:type :enum :required true  :scope :-/info :primary "default"
                       :enum {:ns `-/EnumClassTableType}
                       :sql  {:unique ["class"]}}
   :class-context     {:type :enum :required true :scope :-/info :primary "default"
                       :enum {:ns `-/EnumClassContextType}
                       :sql  {:unique ["class"]}}
   :class-ref         {:type :uuid :required true
                       :sql  {:unique ["class"]}}])


(def Class2DEntryType
  [:class-table          {:type :enum :required true  :scope :-/info :primary "default"
                          :enum {:ns `-/EnumClassTableType}}
   :class-context        {:type :enum :required true :scope :-/info  :primary "default"
                          :enum {:ns `-/EnumClassContextType}}]))
