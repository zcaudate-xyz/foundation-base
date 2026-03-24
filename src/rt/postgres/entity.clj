(ns rt.postgres.entity
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [rt.postgres :as pg]
            [rt.postgres.entity-util :as ut]
            [std.lang :as l]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]
            [std.string.case :as case]))

(f/intern-in ut/init-addons
             ut/init-default-ns-str
             ut/get-addon
             ut/add-addon)

(def ESpec
  {:id     #{:id/none :id/v4 :id/v1 :id/text map?}
   :class  #{:none
             :0d/base :0d/entry :0d/log
             :1d/base :1d/entry :1d/log
             :2d/base :2d/entry :2d/log}
   :addons #{keyword? vector? map?}
   :track  #{:track/none
             :track/data
             :track/log
             :track/record
             :track/time}
   :access #{:access/public
             :access/auth
             :access/system
             :access/hidden
             :none}})

(def ^:private +structural-columns+
  #{:class-table :class-link :class-context :class-ref})

(def ^:private +coord->column+
  {:table :class-table
   :context :class-context
   :id :class-ref
   :link :class-link})

(def ^:private +compatibility+
  {:entity { [2 :log]   #{[2 :base]}
             [1 :log]   #{[1 :entry] [1 :base]}
             [2 :entry] #{[2 :base]}
             [1 :entry] #{[1 :base]}}
   :addons { [1 :entry] #{[2 :base] [1 :base]}
             [0 :entry] #{[0 :base] [0 :entry] [1 :base] [2 :base]}
             [0 :base]  #{[0 :base] [0 :entry]}}})

(defn parse-class
  [class]
  (if (= :none class)
    [nil :none]
    (let [depth (namespace class)
          role  (name class)]
      [(Long/parseLong (subs depth 0 1))
       (keyword role)])))

(defn basis-for
  [[depth role] basis-kind]
  (case [depth role basis-kind]
    [0 :base  :minimal] #{}
    [0 :base  :expanded] #{}
    [0 :entry :minimal] #{}
    [0 :entry :expanded] #{:table :context}
    [0 :log   :minimal] #{}
    [0 :log   :expanded] #{}

    [1 :base  :minimal] #{:table :id}
    [1 :base  :expanded] #{:table :context :id}
    [1 :entry :minimal] #{:table}
    [1 :entry :expanded] #{:table :context :link}
    [1 :log   :minimal] #{:table}
    [1 :log   :expanded] #{:table}

    [2 :base  :minimal] #{:table :context :id}
    [2 :base  :expanded] #{:table :context :id}
    [2 :entry :minimal] #{:table :context}
    [2 :entry :expanded] #{:table :context}
    [2 :log   :minimal] #{:table :context}
    [2 :log   :expanded] #{:table :context}

    #{}))

(defn local-support-columns
  [[depth role] basis-kind relation]
  (case [relation depth role basis-kind]
    [:addon 0 :base  :minimal] []
    [:addon 0 :entry :minimal] []
    [:addon 0 :entry :expanded] [:class-table :class-context]
    [:addon 1 :base  :minimal] [:class-table]
    [:addon 1 :base  :expanded] [:class-table :class-context]
    [:addon 1 :entry :minimal] [:class-table]
    [:addon 1 :entry :expanded] [:class-table :class-link]
    [:addon 1 :log   :minimal] [:class-table]
    [:addon 2 :base  :minimal] [:class-table :class-context]
    [:addon 2 :entry :minimal] [:class-table :class-context]
    [:addon 2 :log   :minimal] [:class-table :class-context]

    [:entity 0 :base  :minimal] []
    [:entity 0 :entry :minimal] []
    [:entity 0 :entry :expanded] [:class-table :class-context]
    [:entity 1 :base  :minimal] [:class-table]
    [:entity 1 :base  :expanded] [:class-table :class-context]
    [:entity 1 :entry :minimal] [:class-table]
    [:entity 1 :entry :expanded] [:class-table]
    [:entity 1 :log   :minimal] [:class-table]
    [:entity 2 :base  :minimal] [:class-table :class-context]
    [:entity 2 :entry :minimal] [:class-table :class-context]
    [:entity 2 :log   :minimal] [:class-table :class-context]

    [:link 1 :entry :minimal] [:class-table]
    [:link 1 :entry :expanded] [:class-table]
    [:link 1 :log   :minimal] [:class-table]
    [:link 2 :entry :minimal] [:class-table :class-context]
    [:link 2 :log   :minimal] [:class-table :class-context]

    []))

(defn target-support-columns
  [[depth _]]
  (case depth
    0 []
    1 [:class-table]
    2 [:class-table :class-context]
    []))

(defn project-support-columns
  [relation source-tuple basis-kind target-tuple]
  (let [local-cols  (local-support-columns source-tuple basis-kind relation)
        remote-cols (target-support-columns target-tuple)
        remote-cols (if (and (= relation :addon)
                             (= source-tuple [1 :entry])
                             (= target-tuple [2 :base]))
                      [:class-context :class-table]
                      remote-cols)]
    (when (> (count remote-cols) (count local-cols))
      (throw (ex-info "Not enough local support columns"
                      {:relation relation
                       :source source-tuple
                       :basis basis-kind
                       :target target-tuple
                       :local-cols local-cols
                       :remote-cols remote-cols})))
    (zipmap local-cols remote-cols)))

(defn column->coord
  [column]
  (first (keep (fn [[coord k]]
                 (when (= column k)
                   coord))
               +coord->column+)))

(defn required-basis-for-plan
  [projection]
  (->> (keys projection)
       (map column->coord)
       (remove nil?)
       set))

(defn allowed-target?
  [relation source-tuple target-tuple]
  (contains? (get-in +compatibility+ [relation source-tuple] #{})
             target-tuple))

(defn public-input->basis
  [input]
  (or (:basis input)
      (basis-for (parse-class (:class input)) :expanded)))

(defn target-info
  [ref]
  (let [resolved (resolve ref)]
    (when resolved
      (let [input (:api/input @@resolved)]
        {:class (:class input)
         :tuple (parse-class (:class input))
         :basis (public-input->basis input)}))))

(defn E-check-input
  [m]
  (let [valid? (fn [allowed v]
                 (let [preds (filter fn? allowed)
                       lits  (set (remove fn? allowed))]
                   (or (contains? lits v)
                       (some #(% v) preds))))
        errors (reduce-kv (fn [acc k allowed]
                            (if (contains? m k)
                              (let [v (get m k)]
                                (cond
                                  (= k :addons)
                                  (let [vs (cond (nil? v) []
                                                 (coll? v) v
                                                 :else [v])
                                        bad (remove #(valid? allowed %) vs)]
                                    (if (seq bad)
                                      (assoc acc k {:invalid bad :allowed allowed})
                                      acc))

                                  :else
                                  (if (valid? allowed v)
                                    acc
                                    (assoc acc k {:value v :allowed allowed}))))
                              acc))
                          {}
                          ESpec)]
    (if (seq errors)
      (throw (ex-info "Invalid inputs for E" {:errors errors :input m}))
      m)))

(defn E-known-addon-keys
  [application]
  (let [application (or application
                        (ut/default-application (env/ns-sym)))]
    (set (keys (get @ut/+addons+ application)))))

(defn E-addon-bool-shorthand
  [m]
  (let [addon-keys  (E-known-addon-keys (:application m))
        [extra out] (reduce-kv (fn [[extra out] k v]
                                 (if (and (contains? addon-keys k)
                                          (or (true? v)
                                              (false? v)
                                              (nil? v)))
                                   [(cond-> extra
                                      (true? v) (conj k))
                                    (dissoc out k)]
                                   [extra out]))
                               [[] m]
                               m)
        addons      (vec (distinct (concat (or (:addons out) [])
                                           (sort-by name extra))))]
    (assoc out :addons addons)))

(defn E-addon-columns-single
  [v]
  (let [ref-fn (fn [key ref]
                 {:key key
                  :field {:type :ref
                          :required true
                          :ref {:ns ref}}})]
    (cond (vector? v)
          (let [[key ref priority custom] v]
            (collection/merge-nested
             (ref-fn key ref)
             {:priority priority}
             custom))

          (keyword? v)
          (ut/get-addon v)

          (map? v)
          (let [addon (ut/get-addon (or (:type v) (:key v)))]
            (merge addon
                   (dissoc v :ref)
                   (if (:ref v)
                     (ref-fn (:key v) (:ref v)))))

          :else
          (f/error "Addon Not Valid" {:input v}))))

(defn basis-kind-for
  [m]
  (if (= (:basis m) (basis-for (parse-class (:class m)) :minimal))
    :minimal
    :expanded))

(defn plan-addon-relation
  [m addon]
  (let [source-tuple (parse-class (:class m))
        target       (target-info (-> addon :field :ref :ns))
        _            (when-not (allowed-target? :addons source-tuple (:tuple target))
                       (f/error "Not a valid addon:"
                                {:class (:class m)
                                 :target (:class target)
                                 :addon addon}))
        projection   (project-support-columns :addon
                                              source-tuple
                                              (basis-kind-for m)
                                              (:tuple target))]
    (into {}
          (map (fn [[local remote]]
                 [local {:foreign {(:key addon) {:ns (-> addon :field :ref :ns)
                                                 :column remote}}}]))
          projection)))

(defn plan-entity-relation
  [m]
  (let [source-tuple (parse-class (:class m))
        basis-kind   (basis-kind-for m)
        entity       (:entity m)
        {:keys [for unique]} entity
        _    (when-not for
               (f/error "Need a :for keyword" {:input m}))
        [key ref] (if (vector? for)
                    for
                    [(keyword (case/spear-case (name (ut/normalise-ref for)))) for])
        ref         (ut/normalise-ref ref)
        remote-cols (if (contains? #{[1 :entry] [1 :log]} source-tuple)
                      [:class-table]
                      [:class-table :class-context])
        local-cols  (local-support-columns source-tuple basis-kind :entity)
        _           (when (> (count remote-cols) (count local-cols))
                      (throw (ex-info "Not enough local support columns for entity relation"
                                      {:class (:class m)
                                       :basis (:basis m)
                                       :entity entity
                                       :local-cols local-cols
                                       :remote-cols remote-cols})))
        projection  (zipmap local-cols remote-cols)
        unique      (or unique
                        (if (not= "log" (name (:class m)))
                          [(case/snake-case (name key))]))
        ref-field-base {:type :ref
                        :required true
                        :ref {:ns ref}
                        :sql {:cascade true
                              :unique unique}}]
    (merge
     (into {}
           (map (fn [[local remote]]
                  [local {:foreign {key {:ns ref
                                         :column remote}}}]))
           projection)
     {key (cond-> ref-field-base
            (= (:class m) :2d/log) (assoc :primary "default"))})))

(defn plan-link-relation
  [m]
  (let [source-tuple (parse-class (:class m))
        basis-kind   (basis-kind-for m)
        {:keys [link]} m]
    (if (not link)
      {}
      (let [{:keys [for as-key unique]} link
            _    (when-not for
                   (f/error "Need a :for keyword" {:input m}))
            [key ref] (if (vector? for)
                        for
                        [(or as-key (keyword (case/spear-case (name (ut/normalise-ref for)))))
                         for])
            ref        (ut/normalise-ref ref)
            target     (target-info ref)
            projection (project-support-columns :link
                                                source-tuple
                                                basis-kind
                                                (:tuple target))
            unique     (or unique
                           (if (= (:class m) :1d/entry)
                             [(case/snake-case (name key))]))
            table-base (merge
                        (into {}
                              (map (fn [[local remote]]
                                     [local {:foreign {key {:ns ref
                                                            :column remote}}}]))
                              projection)
                        {key {:type :ref
                              :required true
                              :ref {:ns ref}
                              :sql (cond-> {:cascade true}
                                     unique (assoc :unique unique))}})]
        (cond-> table-base
          (contains? #{:2d/log :2d/entry} (:class m))
          (collection/merge-nested
           {key {:primary "default"}}))))))

(defn coordinate-column
  [ns-str coord]
  (case coord
    :table (ut/type-class ns-str 1)
    :link (ut/type-class ns-str 2)
    :context (ut/type-class ns-str 3)
    :id (ut/type-class-ref {:sql {:unique ["class"]}} 4)))

(defn E-class-columns
  [{:keys [symname entity ns-str basis]
    :or {ns-str ut/*ns-str*
         symname "Global"}
    :as m}]
  (let [[depth role] (parse-class (:class m))
        {:keys [context]
         :or {context "Global"}} entity
        basis-map     (into {}
                           (map (fn [coord]
                                  [(get +coord->column+ coord)
                                   (coordinate-column ns-str coord)]))
                           basis)
        generated-map (case [depth role]
                        [0 :entry] (cond-> {}
                                      (contains? basis :table)
                                      (assoc :class-table {:generated symname})
                                      (contains? basis :context)
                                      (assoc :class-context {:generated context}))
                        [1 :base]  (cond-> {}
                                      (contains? basis :table)
                                      (assoc :class-table {:primary "default"})
                                      (contains? basis :context)
                                      (assoc :class-context {:generated context}))
                        [1 :entry] (cond-> {}
                                      (contains? basis :table)
                                      (assoc :class-table {:primary "default"})
                                      (contains? basis :link)
                                      (assoc :class-link {:generated symname})
                                      (contains? basis :context)
                                      (assoc :class-context {:generated context}))
                        [1 :log]   {:class-table {:primary "default"}}
                        [2 :base]  {:class-table {:primary "default"
                                                  :sql {:unique ["class"]}}
                                    :class-context {:primary "default"
                                                    :sql {:unique ["class"]}}}
                        [2 :entry] {:class-table {:primary "default"}
                                    :class-context {:primary "default"}}
                        [2 :log]   {:class-table {:primary "default"}
                                    :class-context {:primary "default"}}
                        {})]
    (collection/merge-nested basis-map generated-map)))

(defn E-basis-compatible?
  [{:keys [entity addons link]
    :as m}
   basis]
  (let [m (assoc m :basis basis)]
    (and
     (or (not entity)
         (do (plan-entity-relation m)
             true))
     (or (not link)
         (do (plan-link-relation m)
             true))
     (every? (fn [addon]
               (if (not= :ref (-> addon :field :type))
                 true
                 (do (plan-addon-relation m addon)
                     true)))
             (map E-addon-columns-single addons)))))

(defn E-resolve-basis
  [{:keys [class]
    :as m}]
  (let [parsed   (parse-class class)
        minimal  (basis-for parsed :minimal)
        expanded (basis-for parsed :expanded)]
    (if (= minimal expanded)
      minimal
      (if (try
            (E-basis-compatible? m minimal)
            (catch Throwable _
              false))
        minimal
        expanded))))

(defn E-addon-columns
  [{:keys [addons]
    :as m}]
  (let [addons           (map E-addon-columns-single addons)
        addon-cols       (reduce (fn [out {:keys [field unique priority key]}]
                                   (let [is-ref (-> field :type (= :ref))
                                         unique (or unique
                                                    (if is-ref
                                                      [(case/snake-case (name key))]))]
                                     (assoc out key
                                            (cond-> field
                                              :then  (assoc :priority priority)
                                              unique (assoc-in [:sql :unique] unique)))))
                                 {}
                                 addons)
        addon-class-cols (->> addons
                              (filter (fn [{:keys [field]}]
                                        (= :ref (:type field))))
                              (reduce (fn [out addon]
                                        (collection/merge-nested
                                         out
                                         (plan-addon-relation m addon)))
                                      {}))]
    [addon-cols addon-class-cols]))

(defn E-class-merge
  [m id-cols track-cols class-cols addon-cols]
  (let [[depth role] (parse-class (:class m))
        {:keys [columns basis]} m
        final-cols    (collection/merge-nested
                       (merge id-cols track-cols class-cols addon-cols)
                       columns)
        class-uniques (->> final-cols
                           vals
                           (keep (fn [{:keys [sql]}]
                                   (:unique sql)))
                           (mapcat collection/seqify)
                           set
                           vec)
        col-uniques   (cond
                        (or (zero? (or depth 0))
                            (empty? class-uniques))
                        {}

                        (contains? basis :context)
                        {:class-table {:sql {:unique class-uniques}}
                         :class-context {:sql {:unique class-uniques}}}

                        :else
                        {:class-table {:sql {:unique class-uniques}}})]
    (collection/merge-nested final-cols col-uniques)))

(defn E-main-track
  [{:keys [id class track ns-str]
    :as m}]
  (let [[_
         role]    (parse-class class)
        [id-in
         track-in] (case role
                     :log   [:id/v1 :track/log]
                     :base  [:id/v4 :track/log]
                     :entry [:id/v4 :track/data]
                     [:id/none :track/none])
        [id track] [(or id id-in) (or track track-in)]
        id         (cond (map? id) id
                         :else (case id
                                 :id/v1   (ut/type-id-v1)
                                 :id/v4   (ut/type-id-v4)
                                 :id/text (ut/type-id-text ns-str)
                                 :id/none nil))
        track-val  (ut/get-tracking track)
        track-cols (apply hash-map (ut/fill-priority (ut/get-tracking-columns track) 200))
        id-cols    (if id {:id (assoc id :priority 0)})]
    [id-cols track-cols track-val]))

(defn E-main
  [{:keys [entity access]
    :as m}]
  (let [[id-cols
         track-cols
         track-val]       (E-main-track m)
        access-val        (ut/get-access access)
        class-cols        (cond-> (E-class-columns m)
                            entity (collection/merge-nested
                                    (plan-entity-relation m)))
        [addon-cols
         addon-class-cols] (E-addon-columns m)
        final-cols        (E-class-merge m
                                         id-cols
                                         track-cols
                                         (collection/merge-nested class-cols
                                                                  addon-class-cols
                                                                  (plan-link-relation m))
                                         addon-cols)]
    {:api/meta  access-val
     :api/input (dissoc m :ns-str :application)
     :public    (if (#{:none :access/hidden} access)
                  false
                  true)
     :track     track-val
     :raw       (->> final-cols
                     (sort-by (fn [[_ v]]
                                [(or (:priority v) 50)
                                 (or (:priority-index v) 0)]))
                     vec)}))

(defn E-main-spec
  [{:spec/keys [addon]}]
  (if (and addon grammar-spec/*symbol*)
    (let [{:keys [key priority]} addon]
      (ut/add-addon key
                    (ut/type-ref (or (namespace grammar-spec/*symbol*)
                                     (name (env/ns-sym)))
                                 (name grammar-spec/*symbol*))
                    priority))))

(defn E
  [{:keys [application]
    :as m}]
  (let [normalise-fn (fn [m]
                       (walk/prewalk (fn [m]
                                       (if (ptr/pointer? m)
                                         (ut/normalise-ref m)
                                         m))
                                     m))
        m            (merge {:symname (if grammar-spec/*symbol*
                                        (name grammar-spec/*symbol*))
                             :class   :none
                             :access  :access/auth
                             :columns []
                             :ns-str  (or (ut/default-ns-str application)
                                          ut/*ns-str*
                                          (f/error "No ns-str found." {:ns (env/ns-sym)}))}
                            m)
        m            (E-addon-bool-shorthand m)
        m            (assoc m
                            :addons (normalise-fn (:addons m))
                            :link   (normalise-fn (:link m))
                            :entity (normalise-fn (:entity m)))
        _            (E-check-input m)
        m            (assoc m :basis (E-resolve-basis m))
        out          (E-main m)
        _            (E-main-spec m)]
    out))
