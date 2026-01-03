(ns rt.postgres.entity
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]
            [std.lang.base.grammar-spec :as grammar-spec]
            [rt.postgres.entity-util :as ut]))

;;
;; E
;;

(def LinkSpec
  {:entity   {:2d/log   #{:2d/base}
              :1d/log   #{:1d/entry}
              :1d/entry #{:1d/base}}
   :addons   {:1d/entry #{:2d/base}
              :0d/entry #{:2d/base :1d/base}}})

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
;;  :2d/base :2d/log will always have  :class-context and :class-table 
;;  :0d/entry will also always have :class-context and :class-table
;;  :1d/base  will also always have :class-context and :class-table
;;  we need to 


;; 

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
                   :ref {:ns (ut/normalise-ref ref)}}})
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

(defn E-addon-columns
  [{:keys [addons]
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
                              (reduce (fn [out {:keys [key field]}]
                                        (h/merge-nested
                                         out
                                         (if (not= class :1d/log)
                                           {:class-context {:foreign {key {:ns (-> field :ref :ns) :column :class-context}}}})
                                         {:class-table   {:foreign {key {:ns (-> field :ref :ns) :column :class-table}}}}))
                                      {}))]
    [addon-cols
     addon-class-cols]))



;;
;;  :2d/base :2d/log will always have  :class-context and :class-table 
;;  :0d/entry will also always have :class-context and :class-table
;;  :1d/base  will also always have :class-context and :class-table, so will :1d/entry
;;  all :0d/entry :1d/entry need to interact with :2d/base
;;  :2d/log interacots with :2d/base so does need :class-context
;;  :1d/log only interacts with :1d/entry so does not need :class-context

;; 

(defn E-class-columns
  [{:keys [symname class entity ns-str]
    :or {ns-str ut/*ns-str*
         class   :none
         symname "Global"}
    :as m}]
  (let [{:keys [context]
         :or {context "Global"}} entity
        entry-fields (if (#{:1d/entry
                            :1d/log
                            :2d/entry
                            :2d/log} class)
                       (E-entity-class-fields m))
        base (cond-> (merge
                      {:class-table   (assoc (ut/type-class-table ns-str)   :priority 2)}
                      (if (not= class :1d/log)
                        {:class-context (assoc (ut/type-class-context ns-str) :priority 3)}))
               :then (h/merge-nested entry-fields)
               (#{:1d/base
                  :2d/base} class)  (assoc :class-ref
                                           (assoc (ut/type-class-ref)
                                                  :priority 4
                                                  :sql {:unique ["class"]})))]
    (case (namespace class)
      "0d" (h/merge-nested base
                           {:class-table   {:generated symname}
                            :class-context {:generated context}})
      "1d" (h/merge-nested base
                           {:class-table   {:primary "default"}}
                           (if (not= class :1d/log)
                             {:class-context {:generated  context}})
                           (if (= class :1d/base)
                             {:class-table   {:sql {:unique ["class"]}}}))
      
      "2d"  (h/merge-nested base
                            {:class-table   {:primary "default"}
                             :class-context {:primary "default"}}
                            (if (= class :2d/base)
                              {:class-table   {:sql {:unique ["class"]}}
                               :class-context {:sql {:unique ["class"]}}}))
      {})))


(defn E-class-merge
  [m id-cols track-cols class-cols addon-cols]
  (let [{:keys [class columns]} m
        final-cols    (merge id-cols track-cols class-cols addon-cols)
        class-uniques (case (namespace class)
                        ("1d" "2d") (->> final-cols
                                         (vals)
                                         (keep (fn [{:keys [sql]}]
                                                 (:unique sql)))
                                         (mapcat h/seqify)
                                         (set)
                                         (vec))
                        nil)]
    (cond-> final-cols 
      class-uniques (h/merge-nested
                     {:class-table   {:sql {:unique class-uniques}}}
                     (if (not= class :1d/log)
                       {:class-context {:sql {:unique class-uniques}}})))))

(defn E-main
  [{:keys [id class entity track access raw ns-str application]
    :as m}]
  (let [
        [id-in
         track-in] (case (name class)
                     "log"    [:id/v1   :track/log]
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
        
        access-val   (ut/get-access access)
        track-val    (ut/get-tracking track)
        track-cols   (apply hash-map (ut/fill-priority (ut/get-tracking-columns track) 200))
        
        id-cols      (if id {:id (assoc id :priority 0)})
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



(defn E
  [{:keys [id class entity track access raw columns ns-str application]
    :as m}]
  (let [m (merge {:symname  (if grammar-spec/*symbol* (name grammar-spec/*symbol*))
                  :class   :none
                  :access  :access/auth
                  :columns []
                  :ns-str  (or (ut/default-ns-str application)
                               ut/*ns-str* 
                               (h/error "No ns-str found." {:ns (h/ns-sym)}))}
                 m)
        _ (E-check-input m)]
    (E-main m)))


