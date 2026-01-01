(ns rt.postgres.grammar.form-deftype
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.grammar.common :as common]
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

(defn pg-deftype-enum-col
  "creates the enum column"
  {:added "4.0"}
  ([col enum mopts]
   (conj (vec (butlast col)) (common/pg-linked-token (:ns enum) mopts))))

;;
;;
;;

(defn pg-deftype-ref-name
  "gets the ref name"
  {:added "4.0"}
  ([col {:keys [raw]}]
   (if raw raw (str/snake-case (str (h/strn col) "_id")))))

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
   (let [[col-name col-attrs ref-toks]
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

(defn pg-deftype-partition
  "creates partition by statement"
  {:added "4.0"}
  ([params]
   (pg-deftype-partition params []))
  ([params col-spec]
   (if-let [partition (:partition-by params)]
     (let [[method & cols] partition
           col-map (into {} col-spec)
           cols (map (fn [col]
                       (let [attrs (get col-map col)]
                         (if attrs
                           (if (= (:type attrs) :ref)
                             (pg-deftype-ref-name col (:ref attrs))
                             (str/snake-case (name col)))
                           (str/snake-case (name col)))))
                     cols)]
       (list :partition-by method (list 'quote cols))))))

(defn pg-deftype-foreign-groups
  "collects foreign key groups"
  {:added "4.0"}
  ([col-spec]
   (->> col-spec
        (mapcat (fn [[col {:keys [type ref foreign]}]]
                  (concat
                   (when (and (= :ref type) (:group ref))
                     [[(:group ref)
                       {:local-col (pg-deftype-ref-name col ref)
                        :remote-col (or (:column ref) :id)
                        :ns (:ns ref)
                        :link (:link ref)}]])
                   (when foreign
                     (for [[group f-spec] foreign]
                       [group
                        {:local-col (if (= type :ref)
                                      (pg-deftype-ref-name col ref)
                                      (str/snake-case (name col)))
                         :remote-col (:column f-spec)
                         :ns (:ns f-spec)
                         :link (:link f-spec)}])))))
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
         remote-cols (map (comp symbol name :remote-col) entries)]

     (list '% [:constraint (symbol c-name)
               :foreign-key (list 'quote local-cols)
               :references (list (common/pg-base-token #{remote-table} remote-schema)
                                 (list 'quote remote-cols))]))))

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
                         col-spec)]
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
            ~@ccomments)
       ""))))

(defn pg-deftype-fragment
  "parses the fragment contained by the symbol"
  {:added "4.0"}
  ([esym]
   (let [[esym & extras] (if (vector? esym) esym [esym])
         
         extras (apply hash-map extras)
         form @(or (resolve esym)
                   (h/error "Cannot resolve symbol." {:input esym}))]
     
     (->> (partition 2 form)
          (mapcat (fn [[k attr]]
                    [k (if-let [m (get extras k)]
                         (h/merge-nested attr m)
                         attr)]))))))

(defn pg-deftype-format
  "formats an input form"
  {:added "4.0"}
  [form]
  (let [[mdefn [op sym spec params]] (grammar-spec/format-defn form)
        {:keys [prepend append track public] :as msym} (meta sym)
        spec (->> (concat
                   (mapcat pg-deftype-fragment prepend)
                   spec
                   (mapcat pg-deftype-fragment append))
                  (partition 2)
                  (map vec)
                  (mapcat (fn [[k {:keys [type primary ref sql scope] :as attrs}]]
                            (let [_     (or type (h/error "type cannot be null" {:attrs attrs}))
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
        fmeta {:static/tracker (if track (tracker/map->Tracker @(resolve (first track))))
               :static/public public
               :static/dbtype :table}]
    [fmeta
     (list op (with-meta sym (merge msym fmeta))
           spec
           params)]))

(defn pg-deftype-hydrate-check-link
  "checks a link making sure it exists and is correct type"
  {:added "4.0"}
  [snapshot link type]
  (let [book (snap/get-book snapshot :postgres)
        {:static/keys [dbtype]
         :as entry}  (book/get-base-entry book
                                          (:module link)
                                          (:id link)
                                          (:section link))]
    (cond (not entry)
          (h/error "Entry not found." {:input link})

          (not= dbtype type)
          (h/error "Entry type not correct." {:type type
                                              :input entry})

          :else true)))

(defn pg-deftype-hydrate-link
  "resolves the link for hydration"
  {:added "4.0"}
  ([sym module {:keys [ns] :as ref}]
   (if (and (= "-" (namespace ns))
            (= (name sym) (name ns)))
     [{:section :code
       :lang  :postgres
       :module (:id module)
       :id (symbol (name sym))} false]
     [(select-keys @(or (resolve ns)
                        (h/error "Not found" {:input ref}))
                   [:id :module :lang :section])
      true])))

(defn pg-deftype-hydrate-process-sql
  "processes the sql attribute"
  {:added "4.0"}
  ([sql k attrs]
   (if (:process sql)
     (assoc sql :process
            (h/prewalk
             (fn [x]
               (if (symbol? x)
                 (h/var-sym (or (resolve x)
                                (h/error "Cannot resolve symbol"
                                         {:symbol x
                                          :col k
                                          :attrs attrs})))
                 x))
             (:process sql)))
     sql)))

(defn pg-deftype-hydrate-process-foreign
  "processes the foreign attribute"
  {:added "4.0"}
  ([foreign resolve-link-fn snapshot]
   (h/map-vals (fn [f-spec]
                 (if (:ns f-spec)
                   (let [[link check] (resolve-link-fn f-spec)]
                     (when check (pg-deftype-hydrate-check-link snapshot link :table))
                     (merge f-spec {:ns (keyword (name (:ns f-spec)))
                                    :link link}))
                   f-spec))
               foreign)))

(defn pg-deftype-hydrate-process-ref
  "processes the ref type"
  {:added "4.0"}
  ([k {:keys [ref] :as attrs} resolve-link-fn snapshot]
   (cond (vector? ref)
         [k {:type :ref,
             :required true,
             :ref (merge {:ns  (str (first ref) "." (second ref))
                          :current {:id (second ref)
                                    :schema (first ref)
                                    :type (nth ref 2)}}
                         (nth ref 3))
             :scope :-/ref}]

         :else
         (let [[link check] (resolve-link-fn ref)
               attrs (update attrs :ref
                             merge {:ns   (keyword (name (:ns ref)))
                                    :link link})
               _ (if check (pg-deftype-hydrate-check-link snapshot link :table))]
           [k attrs]))))

(defn pg-deftype-hydrate-process-enum
  "processes the enum type"
  {:added "4.0"}
  ([k attrs snapshot]
   (let [enum-var  (or (resolve (-> attrs :enum :ns))
                       (h/error "Not found" {:input (:enum attrs)}))
         link      (select-keys @enum-var
                                [:id :module :lang :section])
         _ (pg-deftype-hydrate-check-link snapshot link :enum)
         attrs (assoc-in attrs [:enum :ns] (ut/sym-full link))]
     [k attrs])))

(defn pg-deftype-hydrate-attr
  "hydrates a single attribute"
  {:added "4.0"}
  ([k {:keys [type primary ref sql scope foreign] :as attrs}
    {:keys [resolve-link-fn snapshot capture] :as mopts}]
   (let [sql     (if sql (pg-deftype-hydrate-process-sql sql k attrs))
         foreign (if foreign (pg-deftype-hydrate-process-foreign foreign resolve-link-fn snapshot))
         attrs   (cond-> attrs
                   sql (assoc :sql sql)
                   foreign (assoc :foreign foreign))]
     (if primary
       (vswap! capture conj (assoc (select-keys attrs [:type :enum :ref])
                                   :id k)))
     (cond (= :ref type)
           (pg-deftype-hydrate-process-ref k attrs resolve-link-fn snapshot)

           (= :enum type)
           (pg-deftype-hydrate-process-enum k attrs snapshot)

           :else
           [k attrs]))))

(defn pg-deftype-hydrate-spec
  "hydrates the spec"
  {:added "4.0"}
  ([spec mopts]
   (->> (partition 2 spec)
        (mapcat (fn [[k attrs]]
                  (pg-deftype-hydrate-attr k attrs mopts)))
        vec)))

(defn pg-deftype-hydrate
  "hydrates the form with linked ref information"
  {:added "4.0"}
  ([[op sym spec params] grammar {:keys [module
                                         book
                                         snapshot]
                                  :as mopts}]
   (let [resolve-link-fn (partial pg-deftype-hydrate-link sym module)
         capture (volatile! [])
         spec    (pg-deftype-hydrate-spec spec {:resolve-link-fn resolve-link-fn
                                                :snapshot snapshot
                                                :capture capture})
         presch  (schema/schema [(keyword (str sym)) spec])
         hmeta   (assoc (common/pg-hydrate-module-static module)
                        :static/schema-seed presch
                        :static/schema-primary (cond (empty? @capture)
                                                     (h/error "Primary not available")

                                                     (= 1 (count @capture))
                                                     (first @capture)

                                                     :else
                                                     @capture))]
     [hmeta
      (list op (with-meta sym
                 (merge (meta sym) hmeta))
            spec
            params)])))

(defn pg-deftype-hydrate-hook
  "updates the application schema"
  {:added "4.0"}
  ([entry]
   (let [{:static/keys [schema-seed
                        application
                        schema]
          :keys [id]} entry
         rec (get (into {} (map vec (partition 2 (:vec schema-seed))))
                  (keyword (str id)))]
     (doseq [name application]
       (when-not (= rec (get-in @app/*applications* [name :tables id]))
         (swap! app/*applications*
                (fn [m]
                  (-> m
                      (assoc-in [name :tables id] rec)
                      (assoc-in [name :pointers id] (select-keys entry [:id :module :lang :section])))))
         (app/app-rebuild name))))))
