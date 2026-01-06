(ns rt.postgres.entity
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]
            [std.lang.base.grammar-spec :as grammar-spec]
            [rt.postgres.entity-util :as ut]))

(h/intern-in ut/init-addons
             ut/init-default-ns-str
             ut/get-addon
             ut/add-addon)

;;
;; E
;;

(def LinkSpec
  {:entity   {:2d/log   #{:2d/base}
              :1d/log   #{:1d/entry :1d/simple :1d/data}
              :2d/entry #{:2d/base}
              :1d/entry #{:1d/base}
              :1d/data  #{:1d/simple}}
   :addons   {:1d/entry #{:2d/base :1d/base :1d/simple}
              :0d/entry #{:0d/base :0d/entry :0d/data :2d/base :1d/base :1d/simple}
              :0d/data  #{:0d/base :0d/entry :0d/data}
              :0d/base  #{:0d/base :0d/entry :0d/data}}})

(def ESpec
  {:id        #{:id/none :id/v4 :id/v1  :id/text map?}
   :class     #{:none
                :0d/base  :0d/entry :0d/data
                :1d/base  :1d/entry :1d/log :1d/simple :1d/data
                :2d/base  :2d/entry :2d/log}
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

(defn E-entity-class-fields
  [{:keys [class entity ns-str]
    :as m}]
  (let [{:keys [for unique]} entity
        _    (when-not for
               (h/error "Need a :for keyword" {:input m}))
        [key ref] (if (vector? for)
                    for
                    [(keyword (str/spear-case (name (ut/normalise-ref for)))) for])
        ref (ut/normalise-ref ref)
        unique (or unique
                   (if (not= "log" (name class))
                     [(str/snake-case (name key))]))
        ref-field-base {:type :ref
                        :required true
                        :ref {:ns ref}
                        :sql {:cascade true
                              :unique unique}}]
    (merge
     (if (not (#{:1d/log
                 :1d/entry}
               class))
       {:class-context {:foreign {key {:ns ref :column :class-context}}}})
     {:class-table   {:foreign {key {:ns ref :column :class-table}}}
      key (merge ref-field-base
                 (if (= class :2d/log)
                   {:primary "default"}))})))

(defn E-addon-columns-single
  [v]
  (let [ref-fn (fn [key ref]
                 {:key key
                  :field 
                  {:type :ref :required true
                   :ref {:ns ref}}})
        addon  (cond (vector? v)
                     (let [[key ref priority custom] v]
                       (h/merge-nested
                        (ref-fn key ref)
                        {:priority priority}
                        custom))

                     (keyword? v)
                     (ut/get-addon v)
                     
                     (map? v)
                     (let [addon  (ut/get-addon (:key v))]
                       (merge addon
                              (dissoc v :ref)
                              (if (:ref v)
                                (ref-fn (:key v) (:ref v)))))
                     
                     :else (h/error "Addon Not Valid" {:input v}))]
    addon))

(defn E-addon-columns-match
  [class {:keys [key field] :as addon}]
  (let [ref-class    (:class (:api/input @@(resolve (-> field :ref :ns))))
        _ (if-not (contains? (set (get (:addons LinkSpec)
                                       class))
                             ref-class)
            (h/error "Not a valid addon:" {:class class
                                           :ref   ref-class
                                           :addon addon}))
        base  {:class-table   {:foreign {key {:ns (-> field :ref :ns) :column :class-table}}}
               :class-context {:foreign {key {:ns (-> field :ref :ns) :column :class-context}}}}]
    (cond (= "0d"
             (namespace class)
             (namespace ref-class))
          {}

          (= "1d"
             (namespace class)
             (namespace ref-class))
          (dissoc base :class-context)

          :else
          (case [class ref-class]
            [:1d/entry :2d/base]   {:class-table   {:foreign {key {:ns (-> field :ref :ns) :column :class-context}}}
                                    :class-link    {:foreign {key {:ns (-> field :ref :ns) :column :class-table}}}} 
            [:0d/entry :2d/base]   base  
            [:0d/entry :1d/base]   (dissoc base :class-context)
            [:0d/entry :1d/simple] (dissoc base :class-context)
            [:0d/data  :1d/simple] (dissoc base :class-context)))))

(defn E-addon-columns
  [{:keys [class addons]
    :as m}]
  (let [addons     (map E-addon-columns-single addons)
        addon-cols (reduce (fn [out {:keys [field unique priority key]}]
                             (let [is-ref (-> field :type (= :ref))
                                   unique (or unique
                                              (if is-ref
                                                [(str/snake-case (name key))]))]
                               (assoc out key (cond-> field
                                                :then  (assoc :priority priority)
                                                unique (assoc-in [:sql :unique] unique)))))
                           {}
                           addons)
        addon-class-cols (->> addons
                              (filter (fn [{:keys [field]}]
                                        (if (= (:type field) :ref)
                                          [])))
                              (reduce (fn [out addon]
                                        (h/merge-nested
                                         out
                                         (E-addon-columns-match class addon)))
                                      {}))]
    [addon-cols
     addon-class-cols]))

(defn E-class-link-columns
  [{:keys [class link]
    :as m}]
  (if (not link)
    {}
    (let [{:keys [for as-key unique]} link
          _    (when-not for
                 (h/error "Need a :for keyword" {:input m}))
          [key ref] (if (vector? for)
                      for
                      [(or as-key (keyword (str/spear-case (name (ut/normalise-ref for)))))
                       for])
          ref (ut/normalise-ref ref)
          unique     (or unique (if (or (= :1d/entry class)
                                        (= :1d/data class))
                                  [(str/snake-case (name key))]))
          table-base {:class-table   {:foreign {key {:ns ref :column :class-table}}}
                      key  {:type :ref
                            :required true
                            :ref {:ns ref}
                            :sql (cond-> {:cascade true}
                                   unique (assoc :unique unique))}}]
      (case class
        :1d/entry    table-base
        :1d/data     table-base
        :1d/log      table-base
        (:2d/log
         :2d/entry)  (h/merge-nested
         table-base
         {key   {:primary "default"}
          :class-context {:foreign {key {:ns ref :column :class-context}}}})))))

;;
;;  :2d/base :2d/log will always have  :class-context and :class-table 
;;  :0d/entry will also always have :class-context and :class-table
;;  :1d/base  will also always have :class-context and :class-table
;;  
;;  we need to 

(defn E-class-columns
  [{:keys [symname class entity ns-str]
    :or {ns-str ut/*ns-str*
         class   :none
         symname "Global"}
    :as m}]
  (let [{:keys [context]
         :or {context "Global"}} entity
        base {:class-table    (ut/type-class ns-str 1)
              :class-link     (ut/type-class ns-str 2)
              :class-context  (ut/type-class ns-str 3)
              :class-ref      (ut/type-class-ref {:sql {:unique ["class"]}}
                                                 4)}
        base-select (case class
                      (:0d/base
                       :0d/data)  {}
                      :0d/entry   {:class-table   {:generated symname}
                                   :class-context {:generated context}}
                      
                      :1d/simple  {:class-table   {:primary "default"}
                                   :class-ref     {}}
                      
                      :1d/base    {:class-table   {:primary "default"
                                                   :sql {:unique ["class"]}}
                                   :class-context {:generated  context}
                                   :class-ref     {}}
                      
                      :1d/entry   {:class-table   {:primary "default"}
                                   :class-link    {:generated symname}
                                   :class-context {:generated  context}}
                      
                      :1d/log     {:class-table   {:primary "default"}}

                      :1d/data    {:class-table   {:primary "default"}}
                      
                      :2d/base    {:class-table   {:primary "default"
                                                   :sql {:unique ["class"]}}
                                   :class-context {:primary "default"
                                                   :sql {:unique ["class"]}}
                                   :class-ref     {}}
                      :2d/entry   {:class-table   {:primary "default"}
                                   :class-context {:primary "default"}}
                      :2d/log     {:class-table   {:primary "default"}
                                   :class-context {:primary "default"}}
                      {})
        base-class (h/merge-nested
                    base-select
                    (select-keys base (keys base-select)))
        base-link  (if (#{:1d/entry
                          :1d/log
                          :1d/data
                          :2d/log
                          :2d/entry} class)
                     (E-class-link-columns m))]
    (h/merge-nested base-class
                    base-link)))

(defn E-class-merge
  [m id-cols track-cols class-cols addon-cols]
  (let [{:keys [class columns]} m
        final-cols    (h/merge-nested
                       (merge id-cols track-cols class-cols addon-cols)
                       columns)
        class-uniques (->> final-cols
                           (vals)
                           (keep (fn [{:keys [sql]}]
                                   (:unique sql)))
                           (mapcat h/seqify)
                           (set)
                           (vec))
        col-uniques   (if (seq class-uniques)
                        {:class-table   {:sql {:unique class-uniques}}
                         :class-context {:sql {:unique class-uniques}}})]
    (h/merge-nested final-cols
                    (case class
                      (:1d/log :1d/simple :1d/data)  (dissoc col-uniques :class-context)
                      (:0d/entry :0d/data :0d/base) {}
                      col-uniques))))

(defn E-main-track
  [{:keys [id class track ns-str]
    :as m}]
  (let [[id-in
         track-in] (case (name class)
                     "log"    [:id/v1     :track/log]
                     "simple" [:id/v4     :track/log]
                     "data"   [:id/v4     :track/data]
                     "base"   [:id/v4     :track/log]
                     "entry"  [:id/v4     :track/data]
                     "none"   [:id/none   :track/none])
        [id track] [(or id id-in) (or track track-in)]
        id   (cond (map? id) id
                   :else (case id
                           :id/v1   (ut/type-id-v1)
                           :id/v4   (ut/type-id-v4)
                           :id/text (ut/type-id-text ns-str)
                           :id/none nil))
        track-val    (ut/get-tracking track)
        track-cols   (apply hash-map (ut/fill-priority (ut/get-tracking-columns track) 200))
        id-cols      (if id {:id (assoc id :priority 0)})]
    [id-cols track-cols track-val]))

(defn E-main
  [{:keys [id class entity track access raw ns-str application]
    :as m}]
  (let [[id-cols
         track-cols
         track-val]   (E-main-track m)
        access-val    (ut/get-access access)
        class-cols   (E-class-columns m)
        [addon-cols
         addon-class-cols]  (E-addon-columns m)
        final-cols   (E-class-merge m id-cols track-cols
                                    (h/merge-nested class-cols
                                                    addon-class-cols)
                                    addon-cols)]
    {:api/meta   access-val
     :api/input (dissoc m :ns-str :application)
     :public (if (#{:none :access/hidden} access)
               false true)
     :track track-val
     :raw (->> final-cols
               (sort-by (fn [[k v]]
                          [(or (:priority v) 50)
                           (or (:priority-index v) 0)]))
               vec)}))

(defn E-main-spec
  [{:spec/keys [addon]}]
  (if (and addon grammar-spec/*symbol*)
    (let [{:keys [key priority]} addon]
      (ut/add-addon key
                    (ut/type-ref (namespace grammar-spec/*symbol*)
                                 (name grammar-spec/*symbol*)
                                 priority)
                    priority))))

(defn E
  [{:keys [id link addons track access raw columns ns-str application]
    :as m}]
  (let [normalise-fn (fn [m]
                       (h/prewalk (fn [m]
                                    (if (h/pointer? m)
                                      (ut/normalise-ref m)
                                      m))
                                  m))
        m (merge {:symname  (if grammar-spec/*symbol* (name grammar-spec/*symbol*))
                  :class   :none
                  :access  :access/auth
                  :columns []
                  :ns-str  (or (ut/default-ns-str application)
                               ut/*ns-str* 
                               (h/error "No ns-str found." {:ns (h/ns-sym)}))}
                 m
                 {:addons (normalise-fn addons)
                  :link (normalise-fn link)})
        
        _   (E-check-input m)
        out (E-main m)
        _   (E-main-spec m)]
    out))


