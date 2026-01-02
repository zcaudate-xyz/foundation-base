(ns rt.postgres.grammar.form-deftype
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar.form-defpartition :as form-defpartition]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.lang.base.util :as ut]
            [std.lib.schema :as schema]
            [std.string :as str]
            [std.lib :as h]))

;;

;; deftype

;;



(def pg-deftype-ref-name common/pg-deftype-ref-name)



(defn pg-deftype-enum-col
  "creates the enum column"
  {:added "4.0"}
  ([col enum mopts]
   (conj (vec (butlast col)) (common/pg-linked-token (:ns enum) mopts))))

;;
;;
;;


(defn pg-deftype-ref-link
  "creates the ref entry for"
  {:added "4.0"}
  ([col {:keys [ns link current column] :as m :or {column :id}} {:keys [snapshot] :as mopts}]
   (let [{:keys [lang module section id]} link
         book   (snap/get-book snapshot lang)
         r-en   (book/get-base-entry book module id section)
         {:keys [type]
          :as r-ref}  (h/-> (nth (:form r-en) 2)
          (apply hash-map %)
          (get column)
          (or {:type :uuid}))]
     [(pg-deftype-ref-name col m)
      [(common/pg-type-alias type)]
      [(list (common/pg-base-token #{(name ns)} (:static/schema r-en))
             #{(name column)})]])))

(defn pg-deftype-ref-current
  "creates the ref entry for"
  {:added "4.0"}
  ([col {:keys [current column] :as m :or {column :id}} {:keys [snapshot] :as mopts}]
   (let [{:keys [id schema type]}  current]
     [(pg-deftype-ref-name col m)
      [(common/pg-type-alias type)]
      [(list (list '. #{schema} #{id})
             #{(name column)})]])))

(defn pg-deftype-ref
  "creates the ref entry"
  {:added "4.0"}
  ([col {:keys [ns link current] :as m} mopts]
   (cond current
         (pg-deftype-ref-current col m mopts)

         :else
         (pg-deftype-ref-link col m mopts))))

(defn pg-deftype-col-sql
  "formats the sql on deftype"
  {:added "4.0"}
  ([form sql]
   (let [{:keys [cascade default constraint generated raw]} sql
         cargs (cond (nil? constraint) []
                     (map? constraint) [:constraint (symbol (h/strn (:name constraint)))
                                        :check (list 'quote (list (:check constraint)))]
                     :else [:check (list 'quote (list constraint))])]
     (cond-> form
       cascade (conj :on-delete-cascade)
       (not (nil? default)) (conj :default default)
       generated (conj :generated :always :as (list 'quote (list generated)) :stored)
       raw   (concat raw)
       :then (concat cargs)
       :then vec))))

(defn pg-deftype-col-fn
  "formats the column on deftype"
  {:added "4.0"}
  ([[col {:keys [type primary scope sql required unique enum ref] :as m}] mopts]
   (let [sql (if (and (= type :ref) (:group ref))
               (dissoc sql :cascade)
               sql)
         [col-name col-attrs ref-toks]
         (if (= type :ref)
           (pg-deftype-ref col (:ref m) mopts)
           [(str/snake-case (h/strn col))
            [(common/pg-type-alias type)]])
         col-attrs (cond-> col-attrs
                     (= type :enum) (pg-deftype-enum-col enum mopts)
                     (true? primary)  (conj :primary-key)
                     required (conj :not-null)
                     unique   (conj :unique)
                     (and (= type :ref)
                          (not (:group ref)))     (conj :references ref-toks)
                     sql (pg-deftype-col-sql sql))]
     (vec (concat [#{col-name}] col-attrs)))))

(defn pg-deftype-uniques
  "collect unique keys on deftype"
  {:added "4.0"}
  ([cols]
   (let [groups (->> (keep (fn [[k {:keys [type sql ref]}]]
                             (if (:unique sql)
                               (if (vector? (:unique sql))
                                 (mapv (fn [s]
                                         [s {:key  k :type type :ref ref}])
                                       (:unique sql))
                                 [[(:unique sql) {:key  k :type type :ref ref}]])))
                           cols)
                     (mapcat identity)
                     (group-by first)
                     (h/map-vals (partial map second)))]
     (mapv (fn [keys]
             (let [kcols (map (fn [{:keys [key type ref]}]
                                (if (= :ref type)
                                  #{(pg-deftype-ref-name key ref)}
                                  #{(str/snake-case (name key))}))
                              keys)]
               (list '% [:unique (list 'quote kcols)])))
           (vals groups)))))

(defn pg-deftype-primaries
  "TODO"
  {:added "4.0"}
  ([schema-primary]
   (let [schema-primary (if (map? schema-primary)
                          [schema-primary]
                          schema-primary)]
     (if (< 1 (count schema-primary))
       [(list :-
              [:primary-key
               (list 'quote
                     (map (fn [{:keys [id type ref]}]
                            (if (= :ref type)
                              (symbol (pg-deftype-ref-name id ref))
                              (symbol (str/snake-case (name id)))))
                          schema-primary))])]))))

(defn pg-deftype-indexes
  "create index statements"
  {:added "4.0"}
  ([cols ttok]
   (let [c-indexes (keep (fn [[k {:keys [type sql ref]}]]
                           (let [{:keys [index]} sql]
                             (if index
                               (merge (if (true? index)
                                        {}
                                        index)
                                      {:key k :type type :ref ref}))))
                         cols)
         key-fn    (fn [{:keys [key type ref]}]
                     (if (= :ref type)
                       #{(pg-deftype-ref-name key ref)}
                       #{(str/snake-case (name key))}))
         g-indexes (group-by :group c-indexes)
         s-indexes (->> (get g-indexes nil)
                        (mapv (fn [{:keys [using where] :as m}]
                                `(~'% [:create-index
                                       :on
                                       ~ttok
                                       ~@(if using [:using using])
                                       ~(list 'quote (list (key-fn m)))
                                       ~@(if where [\\ :where where])]))))
         g-indexes (dissoc g-indexes nil)
         _ (if (not-empty g-indexes) (h/error "TODO"))]
     s-indexes)))



(defn pg-deftype-foreign-groups
  "collects foreign key groups"
  {:added "4.0"}
  ([col-spec]
   (->> col-spec
        (mapcat (fn [[col {:keys [type ref foreign sql]}]]
                  (concat
                   (when (and (= :ref type) (:group ref))
                     [[(:group ref)
                       {:local-col (pg-deftype-ref-name col ref)
                        :remote-col (or (:column ref) :id)
                        :ns (:ns ref)
                        :link (:link ref)
                        :cascade (-> sql :cascade)}]])
                   (when foreign
                     (for [[group f-spec] foreign]
                       [group
                        {:local-col (if (= type :ref)
                                      (pg-deftype-ref-name col ref)
                                      (str/snake-case (name col)))
                         :remote-col (:column f-spec)
                         :ns (:ns f-spec)
                         :link (:link f-spec)
                         :cascade (-> sql :cascade)}])))))
        (group-by first)
        (h/map-vals (partial map second)))))

(defn pg-deftype-gen-constraint
  "generates a foreign key constraint"
  {:added "4.0"}
  ([sym [group entries] mopts]
   (let [table-name (str/snake-case (name sym))
         c-name (str "fk_" table-name "_" (name group))

         sample (first entries)
         _ (when (not (apply = (map :ns entries)))
             (h/error "All entries in a foreign group must point to same namespace" {:group group :entries entries}))

         {:keys [link]} sample
         book (if (and link (:snapshot mopts))
                (snap/get-book (:snapshot mopts) (:lang link)))
         r-en (if book
                (book/get-base-entry book (:module link) (:id link) (:section link)))

         remote-schema (:static/schema r-en)
         remote-table (name (:id link))

         local-cols (map (comp symbol :local-col) entries)
         remote-cols (map (comp symbol name :remote-col) entries)
         
         cascade (some :cascade entries)]

     (list '% (cond-> [:constraint (symbol c-name)
                       :foreign-key (list 'quote local-cols)
                       :references (common/pg-base-token #{remote-table} remote-schema)
                       (list 'quote remote-cols)]
                cascade (conj :on-delete-cascade))))))

(defn pg-deftype-foreigns
  "creates foreign key constraints"
  {:added "4.0"}
  ([sym col-spec mopts]
   (let [groups (pg-deftype-foreign-groups col-spec)]
     (mapv (fn [entry]
             (pg-deftype-gen-constraint sym entry mopts))
           groups))))

(defn pg-deftype-spec-normalize
  "normalizes the spec, inferring groups"
  {:added "4.0"}
  [col-spec]
  (let [foreign-groups (->> col-spec
                            (mapcat (fn [[_ {:keys [foreign]}]]
                                      (keys foreign)))
                            set)]
    (mapv (fn [[k {:keys [type ref foreign] :as attrs}]]
            (if (and (= :ref type)
                     (not (:group ref))
                     (contains? foreign-groups k))
              [k (assoc-in attrs [:ref :group] k)]
              [k attrs]))
          col-spec)))

(defn pg-deftype-partition
  "creates partition by statement"
  {:added "4.0"}
  ([params]
   (pg-deftype-partition params []))
  ([params col-spec]
   (if-let [partition (:partition-by params)]
     (let [[method cols] (cond (map? partition)
                               [(:strategy partition) (:columns partition)]

                               (vector? partition)
                               [(first partition) (rest partition)]

                               :else
                               (h/error "Not Valid Definition" {:partition partition
                                                                :col-spec col-spec}))
           col-map (into {} col-spec)
           cols (doall (map (fn [col]
                              (let [col-key (if (set? col) (first col) col)
                                    attrs (get col-map col-key)]
                                (if (and (not attrs)
                                         (not (set? col)))
                                  (h/error "Partition Column Not Found" {:column col
                                                                         :available (keys col-map)}))
                                (hash-set (if attrs
                                            (if (= (:type attrs) :ref)
                                              (pg-deftype-ref-name col-key (:ref attrs))
                                              (str/snake-case (name col-key)))
                                            (str/snake-case (name col-key))))))
                              cols))]
       (list :partition-by method (list 'quote cols))))))

(defn pg-deftype
  "creates a deftype statement"
  {:added "4.0"}
  ([form]
   (let [mopts (preprocess/macro-opts)
         [mdefn [_ sym spec params]] (grammar-spec/format-defn form)
         params (if (seq? params) (first params) params)
         {:static/keys [schema schema-primary]
          :keys [final existing]} (meta sym)
         col-spec (mapv vec (partition 2 spec))
         col-spec (pg-deftype-spec-normalize col-spec)
         cols     (mapv #(pg-deftype-col-fn % mopts) col-spec)
         ttok     (common/pg-full-token sym schema)
         tuniques (pg-deftype-uniques col-spec)
         tprimaries (pg-deftype-primaries schema-primary)
         tindexes   (pg-deftype-indexes col-spec ttok)
         tforeigns  (pg-deftype-foreigns sym col-spec mopts)
         tpartition (pg-deftype-partition params col-spec)
         tcustom      (:custom params)
         tconstraints (->> (:constraints params)
                           (mapv (fn [[k val]]
                                   (list '% [:constraint (symbol (h/strn k))
                                             :check (list 'quote (list val))]))))

         tcomment (if (and (:doc mdefn) (not (str/blank? (:doc mdefn))))
                    [[:comment :on :table ttok :is (:doc mdefn)]])

         ccomments (keep (fn [[col attrs]]
                           (if-let [doc (get-in attrs [:sql :comment])]
                             [:comment :on :column (list '. ttok #{(str/snake-case (name col))})
                              :is doc]))
                         col-spec)

         tpartition-default (if-let [default-part (get-in params [:partition-by :default])]
                              (let [{:keys [in name]} default-part
                                    suffix (or name "$DEFAULT")
                                    base-name (clojure.core/name sym)
                                    part-name (form-defpartition/pg-partition-name base-name suffix [])
                                    part-token (if in
                                                 (list '. #{in} #{part-name})
                                                 #{part-name})]
                                [:create-table :if-not-exists part-token
                                 :partition-of ttok :default]))]
     (if (not existing)
       `(do ~@(if-not final [[:drop-table :if-exists ttok :cascade]] [])
            [:create-table :if-not-exists ~ttok \(
             \\ (\|  ~(vec (interpose
                            (list :- ",\n")
                            (concat cols
                                    tprimaries
                                    tuniques
                                    tconstraints
                                    tcustom
                                    tforeigns))))
             \\ \)
             ~@tpartition]
            ~@tindexes
            ~@tcomment
            ~@ccomments
            ~@(if tpartition-default [tpartition-default] []))
       ""))))



(defn pg-deftype-process-type
  "processes the type definition"
  {:added "4.0"}
  [spec {:keys [columns] :as msym}]
  (let [cols (if (vector? columns)
               (mapcat (fn [e]
                         (cond (symbol? e) @(resolve e)
                               (list? e)   (eval e)
                               :else       [e]))
                       columns))
        
        raw-spec (concat
                  (if cols (apply concat cols))
                  spec)
        
        sorted-spec (->> (partition 2 raw-spec)
                         (map vec)
                         (sort-by (fn [[k {:keys [priority]}]]
                                    (or priority 50)))
                         (mapcat identity))]
    [sorted-spec msym]))

(defn pg-deftype-process-generated
  "processes generated columns"
  {:added "4.0"}
  [{:keys [type generated] :as attrs}]
  (if generated
    (let [raw-val (if (= type :enum)
                    (list '++ generated (get-in attrs [:enum :ns]))
                    generated)]
      (-> attrs
          (dissoc :generated)
          (assoc :ignore true)
          (assoc-in [:sql :raw]
                    [:generated :always :as (list 'quote (list raw-val)) :stored])))
    attrs))

(defn pg-deftype-format
  "formats an input form"
  {:added "4.0"}
  [form]
  (let [[mdefn [op sym spec params]] (grammar-spec/format-defn form)
        [spec {:keys [track public] :as msym}] (pg-deftype-process-type spec (meta sym))
        spec (->> spec
                  (partition 2)
                  (map vec)
                  (mapcat (fn [[k {:keys [type primary ref sql scope generated] :as attrs}]]
                            (let [attrs (pg-deftype-process-generated attrs)
                                  {:keys [type primary ref sql scope]} attrs
                                  _     (or type (h/error "type cannot be null" {:attrs attrs}))
                                  _     (if scope (schema/check-scope scope))
                                  scope (or scope (cond primary
                                                        :-/id
                                                        
                                                        (= :ref type)
                                                        :-/ref
                                                        
                                                        :else
                                                        :-/data))
                                  attrs (assoc attrs :scope scope)]
                              [k attrs])))
                  vec)
        track (if (vector? track)
                (first track)
                track)
        track  (if (symbol? track)
                 @(resolve track)
                 track)
        fmeta {:static/tracker (if track (tracker/map->Tracker track))
               :static/public public
               :static/dbtype :table}
        qmeta (h/qualified-keys msym)]
    [(merge fmeta qmeta)
     (list op (with-meta sym (merge msym fmeta qmeta))
           spec
           params)]))


