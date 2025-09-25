(ns rt.postgres.grammar.form-deftype
  (:require [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.grammar.common :as common]
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


(defn pg-deftype-ref-link
  "creates the ref entry"
  {:added "4.0"}
  ([col {:keys [ns link current column] :or {column :id}} {:keys [snapshot] :as mopts}]
   (let [{:keys [lang module section id]} link
         book   (snap/get-book snapshot lang)
         r-en   (book/get-base-entry book module id section)
         {:keys [type]
          :as r-ref}  (h/-> (nth (:form r-en) 2)
          (apply hash-map %)
          (get column)
          (or {:type :uuid}))]
     [(str/snake-case (str (h/strn col) "_id"))
      [(common/pg-type-alias type)]
      [(list (common/pg-base-token #{(name ns)} (:static/schema r-en))
             #{(name column)})]])))

(defn pg-deftype-ref-current
  "creates the ref entry"
  {:added "4.0"}
  ([col {:keys [ns current column] :or {column :id}} {:keys [snapshot] :as mopts}]
   (let [{:keys [id schema type]}  current]
     [(str/snake-case (str (h/strn col) "_id"))
      [(common/pg-type-alias type)]
      [(list (list '. #{schema} #{id})
             #{(name column)})]])))

(comment
  {:id (second ref)
   :schema (first ref)
   :type (nth ref 2)})

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
   (let [{:keys [cascade default constraint]} sql
         cargs (cond (nil? constraint) []
                     (map? constraint) [:constraint (symbol (h/strn (:name constraint)))
                                        :check (list 'quote (list (:check constraint)))]
                     :else [:check (list 'quote (list constraint))])]
     (cond-> form
       cascade (conj :on-delete-cascade)
       (not (nil? default)) (conj :default default)
       :then (concat cargs)
       :then vec))))

(defn pg-deftype-col-fn
  "formats the column on deftype"
  {:added "4.0"}
  ([[col {:keys [type primary scope sql required unique enum] :as m}] mopts]
   (let [[col-name col-attrs ref-toks]
         (if (= type :ref)
           (pg-deftype-ref col (:ref m) mopts)
           [(str/snake-case (h/strn col))
            [(common/pg-type-alias type)]])
         col-attrs (cond-> col-attrs
                     (= type :enum) (pg-deftype-enum-col enum mopts)
                     primary  (conj :primary-key)
                     required (conj :not-null)
                     unique   (conj :unique)
                     (= type :ref)     (conj :references ref-toks)
                     sql (pg-deftype-col-sql sql))]
     (vec (concat [#{col-name}] col-attrs)))))

(defn pg-deftype-uniques
  "collect unique keys on deftype"
  {:added "4.0"}
  ([cols]
   (let [groups (->> (keep (fn [[k {:keys [type sql]}]]
                             (if (:unique sql)
                               (if (vector? (:unique sql))
                                 (mapv (fn [s]
                                         [s {:key  k :type type}])
                                       (:unique sql))
                                 [[(:unique sql) {:key  k :type type}]])))
                           cols)
                     (mapcat identity)
                     (group-by first)
                     (h/map-vals (partial map second)))]
     (mapv (fn [keys]
             (let [kcols (map (fn [{:keys [key type]}]
                                (if (= :ref type)
                                  #{(str (str/snake-case (name key)) "_id")}
                                  #{(str/snake-case (name key))}))
                              keys)]
               (list '% [:unique (list 'quote kcols)])))
           (vals groups)))))

(defn pg-deftype-indexes
  "create index statements"
  {:added "4.0"}
  ([cols ttok]
   (let [c-indexes (keep (fn [[k {:keys [type sql]}]]
                           (let [{:keys [index]} sql]
                             (if index
                               (merge (if (true? index)
                                        {}
                                        index)
                                      {:key k :type type}))))
                         cols)
         key-fn    (fn [{:keys [key type]}]
                     (if (= :ref type)
                       #{(str (str/snake-case (name key)) "_id")}
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

(defn pg-deftype
  "creates a deftype statement"
  {:added "4.0"}
  ([[_ sym spec params]]
   (let [mopts (preprocess/macro-opts)
         {:static/keys [schema]
          :keys [final existing]} (meta sym)
         col-spec (mapv vec (partition 2 spec))
         cols     (mapv #(pg-deftype-col-fn % mopts) col-spec)
         ttok     (common/pg-full-token sym schema)
         tuniques (pg-deftype-uniques col-spec)
         tindexes (pg-deftype-indexes col-spec ttok)
         tconstraints (->> (:constraints params)
                           (mapv (fn [[k val]]
                                   (list '% [:constraint (symbol (h/strn k))
                                             :check (list 'quote (list val))]))))]
     (if (not existing)
       `(do ~@(if-not final [[:drop-table :if-exists ttok :cascade]] [])
            [:create-table :if-not-exists ~ttok \(
             \\ (\|  ~(vec (interpose
                            (list :- ",\n")
                            (concat cols
                                    tuniques
                                    tconstraints))))
             \\ \)]
            ~@tindexes)
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
  [[op sym spec params]]
  (let [{:keys [prepend append track public] :as msym} (meta sym)
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

(defn pg-deftype-hydrate
  "hydrates the form with linked ref information"
  {:added "4.0"}
  ([[op sym spec params] grammar {:keys [module
                                         book
                                         snapshot]
                                  :as mopts}]
   (let [capture (volatile! nil)
         spec  (->> (partition 2 spec)
                    (mapcat (fn [[k {:keys [type primary ref sql scope] :as attrs}]]
                              (let [sql   (cond-> sql
                                            (:process sql) (assoc :process
                                                                  (h/prewalk
                                                                   (fn [x]
                                                                     (if (symbol? x)
                                                                       (h/var-sym (resolve x))
                                                                       x))
                                                                   (:process sql))))
                                    attrs (cond-> attrs
                                            sql (assoc :sql sql))]
                                (if primary
                                  (if @capture
                                    (h/error "Only one primary allowed." {:prev @capture
                                                                          :curr attrs})
                                    (vreset! capture (assoc (select-keys attrs [:type :enum :ref])
                                                            :id k))))
                                (cond (and (= :ref type)
                                           (vector? ref))
                                      [k {:type :ref,
                                          :required true,
                                          :ref (merge {:ns  (str (first ref) "." (second ref))
                                                       :current {:id (second ref)
                                                                 :schema (first ref)
                                                                 :type (nth ref 2)}}
                                                      (nth ref 3))
                                          :scope :-/ref}]
                                      
                                      (= :ref type)
                                      (let [[link check] (if (and (= "-" (namespace (:ns ref)))
                                                                  (= (name sym) (name (:ns ref))))
                                                           [{:section :code
                                                             :lang  :postgres
                                                             :module (:id module)
                                                             :id (symbol (name sym))} false]
                                                           [(select-keys @(or (resolve (:ns ref))
                                                                              (h/error "Not found" {:input ref}))
                                                                         [:id :module :lang :section])
                                                            true])
                                            attrs (update attrs :ref
                                                          merge {:ns   (keyword (name (:ns ref)))
                                                                 :link link})
                                            _ (if check (pg-deftype-hydrate-check-link snapshot link :table))]
                                        [k attrs])
                                      
                                      (= :enum type)
                                      (let [enum-var  (or (resolve (-> attrs :enum :ns))
                                                          (h/error "Not found" {:input (:enum attrs)}))
                                            link      (select-keys @enum-var
                                                                   [:id :module :lang :section])
                                            _ (pg-deftype-hydrate-check-link snapshot link :enum)
                                            attrs (assoc-in attrs [:enum :ns] (ut/sym-full link))]
                                        [k attrs])
                                      
                                      :else
                                      [k attrs]))))
                    vec)
         presch  (schema/schema [(keyword (str sym)) spec])
         hmeta  (assoc (common/pg-hydrate-module-static module)
                       :static/schema-seed presch
                       :static/schema-primary (or @capture
                                                  (h/error "Primary not available")))]
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
