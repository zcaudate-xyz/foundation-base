(ns lang.model.annex.spec-postgres.common
  (:require [clojure.string]
            [lang.runtime.postgres.base.application :as app]
            [lang.model.annex.spec-postgres.meta :as meta]
            [lang.model.annex.spec-postgres.tf :as tf]
            [lang.base.book :as book]
            [lang.base.emit :as emit]
            [lang.base.emit-common :as common]
            [lang.base.emit-fn :as fn]
            [lang.base.emit-preprocess :as preprocess] [lang.base.preprocess-base :as preprocess-base]
            [lang.base.grammar :as grammar]
            [lang.base.grammar-spec :as grammar-spec]
            [lang.base.emit-template :as impl-template]
            [lang.core.library-snapshot :as snap]
            [lang.core.pointer :as ptr]
            [lang.core.script :as script]
            [lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.string.case :as case]))

;;
;; type alias
;;

(def +pg-query-alias+
  {:neq '(:- "!=")
   :gt  '(:- ">")
   :gte '(:- ">=")
   :lt  '(:- "<")
   :lte '(:- "<=")
   :eq  '(:- "=")})

(def +pg-type-alias+
  '{:map    :jsonb
    :array  :jsonb
    :long   :bigint
    :enum   :text
    :image  :jsonb
    :time   :bigint})

(defn pg-type-alias
  "gets the type alias"
  {:added "4.0"}
  ([type]
   (or (get +pg-type-alias+ type)
       type)))

(def +pg-jsonb-shapes+
  "Explicit JSONB shapes accepted by deftype column metadata."
  #{:map :array :opaque})

(def +pg-jsonb-types+
  "Types and aliases emitted as PostgreSQL jsonb."
  #{:jsonb :map :array :image})

(defn pg-jsonb-type?
  "Returns true when a deftype column type emits PostgreSQL jsonb."
  [type]
  (contains? +pg-jsonb-types+ type))

(defn- pg-jsonb-invalid
  [message data]
  (f/error message data))

(declare validate-pg-jsonb-metadata)

(defn validate-pg-jsonb-metadata
  "Validates JSONB shape metadata without changing the emitted SQL type.

   :map and :array remain aliases for PostgreSQL jsonb. The metadata is only
   consumed by typed parsers and downstream schema conversion."
  [{:keys [type shape] :as attrs}]
  (when-not (map? attrs)
    (pg-jsonb-invalid "JSONB metadata must be a map." {:options attrs}))
  (let [map-present? (contains? attrs :map)
        map-schema   (:map attrs)
        items-present? (contains? attrs :items)
        items-schema (:items attrs)
        implied      (case type
                       :map :map
                       :array :array
                       nil)]
    (when (and (contains? attrs :shape)
               (not (contains? +pg-jsonb-shapes+ shape)))
      (pg-jsonb-invalid
       "Invalid JSONB shape. Expected :map, :array, or :opaque."
       {:shape shape :options attrs}))
    (when (and (contains? attrs :shape)
               (not (pg-jsonb-type? type)))
      (pg-jsonb-invalid
       "JSONB shape metadata requires a JSONB column type."
       {:shape shape :type type :options attrs}))
    (when (and implied
               (contains? attrs :shape)
               (not= implied shape))
      (pg-jsonb-invalid
       "JSONB shape conflicts with the column type alias."
       {:shape shape :type type :expected implied :options attrs}))
    (when map-present?
      (when-not (pg-jsonb-type? type)
        (pg-jsonb-invalid
         "Nested :map metadata requires a JSONB column type."
         {:type type :options attrs}))
      (when-not (map? map-schema)
        (pg-jsonb-invalid
         "Nested :map metadata must be a map of key schemas."
         {:map map-schema :options attrs}))
      (when (and (contains? attrs :shape)
                 (not= :map shape))
        (pg-jsonb-invalid
         "Nested :map metadata requires the :map JSONB shape."
         {:shape shape :options attrs}))
      (when (and implied (not= :map implied))
        (pg-jsonb-invalid
         "Nested :map metadata conflicts with the column type alias."
         {:type type :options attrs}))
      (doseq [[key entry] map-schema]
        (when-not (or (keyword? key) (symbol? key) (string? key))
          (pg-jsonb-invalid
           "JSONB map schema keys must be keywords, symbols, or strings."
           {:key key :options attrs}))
        (when-not (map? entry)
          (pg-jsonb-invalid
           "JSONB map schema entries must be metadata maps."
           {:key key :entry entry :options attrs}))
        (validate-pg-jsonb-metadata entry)))
    (when items-present?
      (when-not (pg-jsonb-type? type)
        (pg-jsonb-invalid
         "Array item metadata requires a JSONB column type."
         {:type type :options attrs}))
      (when-not (or (= :array shape)
                    (= :array implied))
        (pg-jsonb-invalid
         "Array item metadata requires the :array JSONB shape."
         {:shape shape :type type :options attrs}))
      (when map-present?
        (pg-jsonb-invalid
         "Array item metadata cannot be combined with nested :map metadata."
         {:options attrs}))
      (when-not (map? items-schema)
        (pg-jsonb-invalid
         "Array item metadata must be a metadata map."
         {:items items-schema :options attrs}))
      (validate-pg-jsonb-metadata items-schema))
    attrs))

(defn pg-deftype-ref-name
  "gets the ref name"
  {:added "4.0"}
  ([col {:keys [raw]}]
   (if raw raw (case/snake-case (str (f/strn col) "_id")))))

(defn pg-sym-meta
  "returns the sym meta"
  {:added "4.0"}
  [sym]
  (let [{:keys [props dbtype]
         return :-
         language :%%
         :or {return [:jsonb]
              language  :default
              props []}
         :as msym} (meta sym)]
    (assoc (dissoc msym :%% :props)
           :- return
           :static/return return
           :static/language language
           :static/props props)))

(defn pg-format
  "formats a form, extracting static components"
  {:added "4.0"}
  [[op sym & body]]
  (let [msym (pg-sym-meta sym)]
    [msym
     (apply list op (with-meta sym msym) body)]))

(defn pg-policy-format
  "TODO"
  {:added "4.0"}
  ([form]
   (let [[mdefn [op sym [table] policy]] (grammar-spec/format-defn form)
         mname    (str sym " - " (:doc mdefn))
         [mschema
          mtable] (if-let [v (resolve table)]
                    [nil (str (:id @v))]
                    (clojure.string/split (str table) #"\."))
         msym   (assoc (meta sym)
                       :static/policy-name mname
                       :static/policy-table  mtable
                       :static/policy-schema mschema)]
     [(merge mdefn msym)
      (list op
            (with-meta sym msym)
            (:doc mdefn)
            [table]
            policy)])))

(defn pg-hydrate-module-static
  "gets the static module"
  {:added "4.0"}
  [module]
  (let [{:keys [static]} module
        {:keys [application all]} static
        {:keys [schema]} all]
    {:static/schema (first schema)
     :static/application application}))

(defn- pg-resolve-table-symbol
  [table sym-meta mopts]
  (when (symbol? table)
    (let [source-ns (or (:namespace mopts)
                        (get-in mopts [:entry :namespace]))
          ns-obj    (or (cond (instance? clojure.lang.Namespace source-ns)
                              source-ns

                              (symbol? source-ns)
                              (find-ns source-ns))
                        *ns*)
          source-module-id (some-> (get-in sym-meta [:api/meta :db/module])
                                   str
                                   symbol)
          modules          (or (get-in mopts [:book :modules])
                               (get-in mopts [:snapshot :postgres :book :modules]))
          source-module    (get modules source-module-id)
          linked-module    (get-in source-module [:link (some-> table namespace symbol)])
          table-var        (or (ns-resolve ns-obj table)
                               (when linked-module
                                 (ns-resolve ns-obj
                                             (symbol (str linked-module)
                                                     (name table)))))]
      (some-> table-var
              f/var-sym))))

(defn- pg-hydrate-api-meta
  [sym-meta mopts]
  (if-let [table (get-in sym-meta [:api/meta :table])]
    (if-let [table-sym (pg-resolve-table-symbol table sym-meta mopts)]
      (assoc-in sym-meta [:api/meta :table] table-sym)
      sym-meta)
    sym-meta))

(defn pg-hydrate
  "hydrate function for top level entries"
  {:added "4.0"}
  ([[op sym & body] grammar mopts]
   (let [reserved (collection/qualified-keys (get-in grammar [:reserved op])
                                      :static)
         sym-meta (pg-hydrate-api-meta (meta sym) mopts)
         hmeta    (merge (pg-hydrate-module-static (:module mopts))
                         reserved
                         (select-keys sym-meta [:api/meta]))]
     [hmeta (apply list op (with-meta sym (merge sym-meta hmeta))
                   body)])))

(defn pg-current-module-link?
  "checks whether a link points to the active module"
  {:added "4.1"}
  [current-module {:keys [module]}]
  (and current-module
       module
       (= (:id current-module) module)))

(defn pg-link-symbol
  "creates a fully qualified symbol for a link"
  {:added "4.1"}
  [{:keys [module id]}]
  (when (and module id)
    (ut/sym-full (symbol (name module))
                 (symbol (name id)))))

(defn pg-resolve-entry
  "resolves a postgres link against the live module first, then the snapshot book, then vars"
  {:added "4.1"}
  [link {:keys [book snapshot module lang] :as _mopts}]
  (let [lang          (or lang
                          (:lang link)
                          :postgres)
        book          (or book
                          (and snapshot
                               (snap/get-book snapshot lang)))
        normalize-entry (fn [entry]
                          (cond (nil? entry) nil
                                (book/book-entry? entry) entry
                                (or (:context entry)
                                    (:context/fn entry))
                                (ptr/get-entry entry)
                                :else entry))
        module-id      (or (:id module) module)
        current-entry  (or (when (and book
                                      module-id
                                      (pg-current-module-link? module link))
                             (some-> (book/get-code-entry-view book
                                                               (ut/sym-full module-id
                                                                            (:id link)))
                                     normalize-entry))
                           (when-let [entry (and (pg-current-module-link? module link)
                                                 (get-in module [:code (:id link)]))]
                             (when (map? entry)
                               (normalize-entry entry))))
        snapshot-entry (some->> (and book
                                     (book/get-base-entry book
                                                          (:module link)
                                                          (:id link)
                                                          (:section link)))
                                normalize-entry)
        resolved-entry (some-> link
                               pg-link-symbol
                               resolve
                               deref
                               normalize-entry)
        complete?     (fn [entry]
                        (or (:static/schema-seed entry)
                            (:static/schema-primary entry)
                            (:static/application entry)))
        materialize   (fn [entry entry-module]
                        (cond (nil? entry) nil
                              (complete? entry) entry
                              (nil? book) entry
                              :else
                              (impl-template/materialize-code-entry book
                                                                    (dissoc entry :static/code.cache)
                                                                    {:lang lang
                                                                     :module (or entry-module
                                                                                 (get-in book [:modules (:module entry)]))})))
        current-entry (materialize current-entry module)
        snapshot-entry (materialize snapshot-entry
                                    (get-in book [:modules (:module link)]))
        entry         (or (and current-entry
                               (complete? current-entry)
                               current-entry)
                          (and snapshot-entry
                               (complete? snapshot-entry)
                               snapshot-entry)
                          resolved-entry
                          snapshot-entry
                          current-entry)]
    [book entry]))

(defn pg-string
  "constructs a pg string"
  {:added "4.0"}
  ([s]
   (-> (pr-str s)
       (clojure.string/replace #"'" "''")
       (clojure.string/replace #"^\"" "'")
       (clojure.string/replace #"\"$" "'")
       (clojure.string/replace #"\\\"" "\"")
       (clojure.string/replace #"\\\\" "\\\\"))))

(defn pg-uuid
  "constructs a pg uuid"
  {:added "4.0"}
  ([u]
   (str "'" (str u) "'::uuid")))

(defn pg-map
  "creates a postgres json object"
  {:added "4.0"}
  ([m grammar mopts]
   (common/*emit-fn* (tf/pg-tf-js [nil m]) grammar mopts)))

(defn pg-set
  "makes a set object"
  {:added "4.0"}
  ([e grammar mopts]
   (cond (< 1 (count e))
         (if (every? symbol? e)
           (common/*emit-fn*  (tf/pg-tf-js [nil e]) grammar mopts)
           (f/error "Not Allowed" {:value e}))
         
         :else
         (let [v (first e)]
           (cond (string? v) (str "\"" v "\"")

                 
                 (and (symbol? v)
                      (re-find #"\w-\w+" (str v)))
                 (common/*emit-fn*  (tf/pg-tf-js [nil e]) grammar mopts)
                 
                 :else
                 (f/error "Not Allowed" {:value e}))))))

(defn pg-array
  "creates an array object
 
   (common/pg-array '(array 1 2 3 4 5)
                    g/+grammar+
                    {})
   => \"ARRAY[1,2,3,4,5]\""
  {:added "4.0"}
  ([[_ & arr] grammar mopts]
   (let [str-array (common/emit-array arr grammar mopts common/*emit-fn*)]
     (str "ARRAY[" (clojure.string/join "," str-array) "]"))))


(defn pg-invoke-typecast
  "emits a typecast call"
  {:added "4.0"}
  [form grammar mopts]
  (let [val   (last form)
        types (clojure.string/join (map (fn [v]
                               (cond (keyword? v)
                                     (clojure.string/upper-case (f/strn v))
                                     
                                     (or (and (collection/form? v)
                                              (not= '. (first v)))
                                         (vector? v))
                                     (f/strn v)
                                     
                                     :else
                                     (common/*emit-fn* v grammar mopts)))
                             (butlast form)))]
    (str "(" (common/*emit-fn*  val grammar mopts) ")" "::" types)))

(defn pg-typecast
  "creates a typecast"
  {:added "4.0"}
  ([[_ sym & args] grammar mopts]
   (-> (concat args [sym])
       (pg-invoke-typecast grammar mopts))))

(defn pg-do-assert
  "creates an assert form"
  {:added "4.0"}
  [[_ chk [tag data]] grammar mopts]
  (common/*emit-fn* (tf/pg-tf-assert [nil chk [tag data]])
                    grammar mopts))

;;
;; type tokens
;;

(defn pg-base-token
  "creates a base token"
  {:added "4.0"}
  ([tok schtok]
   (let [schtok (if (string? schtok)
                  #{schtok}
                  schtok)]
     (if (and schtok
              (not= schtok #{"public"}))
       (list '. schtok tok)
       tok))))

(defn pg-full-token
  "creates a full token (for types and enums)"
  {:added "4.0"}
  ([tok schtok]
   (let [tok #{(clojure.string/replace (f/strn tok) #"\." "_")}]
     (pg-base-token tok schtok))))

;;
;; linked symbol (defn and deftype)
;;

(defn pg-entry-literal
  "creates an entry literal"
  {:added "4.0"}
  ([entry]
   (let [{:static/keys [schema]
          :keys [op id]} entry]
     (str schema "." id))))

(defn pg-entry-token
  "gets the entry token"
  {:added "4.0"}
  ([entry]
   (let [{:static/keys [schema]
          :keys [op id]} entry]
     (cond (= op 'defconst)
           (:id (last (:form entry)))
           
           :else
           (pg-base-token (case op
                            def      #{(str id)}
                            defn     (symbol (str id))
                            deftype  #{(str id)}
                            defenum  #{(str id)}
                            defrole  #{(str id)}
                            (symbol (str id)))
                          schema)))))

(defn pg-linked-token
  "gets the linked token given symbol"
  {:added "4.0"}
  ([sym mopts]
   (let [{:keys [lang snapshot]} mopts
          book (snap/get-book snapshot lang)
          [sym-module sym-id] (ut/sym-pair sym)
          {:keys [section] :as e} (or (book/get-code-entry-view book sym)
                                      (get-in (book/get-module book sym-module) [:fragment sym-id])
                                      (f/error "Token Not found."
                                               {:input sym
                                                :module sym-module
                                                :sym-id sym-id
                                                :opts mopts}))]
      (case section
        :fragment (:form e)
        :code (pg-entry-token e)))))

(defn pg-linked
  "emits the linked symbol"
  {:added "4.0"}
  ([sym grammar opts]
   (-> (pg-linked-token sym opts)
       (common/*emit-fn*  grammar opts))))

(defn block-do-block
  "initates do block"
  {:added "4.0"}
  ([form]
   `[:do :$$
     \\ :begin
     \\ (\| ~form)
     \\ :end \;
     \\ :$$ :language "plpgsql" \;]))

(defn block-do-suppress
  "initates suppress block"
  {:added "4.0"}
  ([form]
   `[:do :$$
     \\ :begin
     \\ (\| ~form) 
     \\ :exception :when-others-then
     \\ :end \;
     \\ :$$ :language "plpgsql" \;]))

(defn block-loop-block
  "emits loop block"
  {:added "4.0"}
  ([_ & forms]
   `[:loop
     \\ (\| (~'do ~@forms))
     \\ :end-loop]))

(defn block-while-block
  "emits while block"
  {:added "4.0"}
  ([condition & forms]
   `[:while ~condition :loop
     \\ (\| (~'do ~@forms))
     \\ :end-loop \;]))

(defn block-case-block
  "emits case block"
  {:added "4.0"}
  ([value & args]
   (let [args  (partition 2 args)
         block (mapcat (fn [[chk body]]
                         (if (= :else chk)
                           [:ELSE (list :% body) \\]
                           [:WHEN (list :% chk) :THEN (list :% body) \\]))
                       args)]
     (list '% (vec (concat [:case value \\]
                           [(apply list \| block)]
                           [:end]))))))


;;
;; defenum
;;

(defn pg-defenum
  "defenum block"
  {:added "4.0"}
  [[_ sym array]]
  (let [{:static/keys [schema]} (meta sym)
        ttok  (pg-full-token sym schema)
        vals  (if (and (seq? array)
                       (= '!:eval (first array)))
                (list '!:eval
                      (list 'list
                            (list 'quote 'quote)
                            (list 'clojure.core/map
                                  'std.lib.foundation/strn
                                  (second array))))
                (list 'quote (map f/strn array)))]
    `[:do :$$
      \\ :begin
      \\ (\| (~'do [:create-type ~ttok :as-enum ~vals]))
      \\ :exception :when-others-then
      \\ :end \;
      \\ :$$ :language "plpgsql" \;]))

;;
;; defindex
;;

(defn pg-defindex
  "defindex block"
  {:added "4.0"}
  [[_ sym doc? attr? [table & cols] & body :as form]]
  (let [[{:keys [doc]
          :as mdefn} [_ sym [table & cols] & body]] (grammar-spec/format-defn form)]
    (vec (concat
          [:create-index :if-not-exists
           sym
           :on table (list 'quote (list  (vec cols)))]
          body
          [\;]))))

;;
;; defpolicy
;;

(defn pg-defpolicy
  "defpolicy block"
  {:added "4.0"}
  [[_ sym doc? attr? [table] body :as form]]
  
  (let [[{:keys [doc]
          :as mdefn} [_ sym [table] body]] (grammar-spec/format-defn form)]
    (list
     'do
     [:drop-policy-if-exists #{(str sym " - " doc)} :on table]
     (vec (concat [:create-policy #{(str sym " - " doc)} :on table \\]
                  body)))))

;;
;; defpublication
;;

(defn pg-publication-format
  "formats publication"
  {:added "4.0"}
  ([form]
   (let [[mdefn [op sym args body]] (grammar-spec/format-defn form)]
     [(merge mdefn (meta sym))
      (list op (with-meta sym (meta sym)) (:doc mdefn) args body)])))

(defn pg-defpublication
  "defpublication block"
  {:added "4.0"}
  [[_ sym doc? attr? args body :as form]]
  (let [[{:keys [doc] :as mdefn} [_ sym args body]] (grammar-spec/format-defn form)]
    (list 'do
          [:drop-publication-if-exists sym]
          (vec (concat [:create-publication sym]
                       (cond (or (empty? args)
                                 (= args [:all]))
                             [:for :all :tables]

                             :else
                             [:for :table (list 'quote args)])
                       body)))))

;;
;; defsubscription
;;

(defn pg-subscription-format
  "formats subscription"
  {:added "4.0"}
  ([form]
   (let [[mdefn [op sym args body]] (grammar-spec/format-defn form)]
     [(merge mdefn (meta sym))
      (list op (with-meta sym (meta sym)) (:doc mdefn) args body)])))

(defn pg-defsubscription
  "defsubscription block"
  {:added "4.0"}
  [[_ sym doc? attr? [conn pub] body :as form]]
  (let [[{:keys [doc] :as mdefn} [_ sym [conn pub] body]] (grammar-spec/format-defn form)]
    (list 'do
          [:drop-subscription-if-exists sym]
          (vec (concat [:create-subscription sym
                        :connection conn
                        :publication pub]
                       body)))))

;;
;; deftrigger
;;

(defn pg-deftrigger
  "TODO"
  {:added "4.0"}
  [[_ sym doc? attr? [table] body :as form]]
  (let [[{:keys [doc]
          :as mdefn}
         [_ sym [table] body]] (grammar-spec/format-defn form)
        {:static/keys [return]} (meta sym)]
    (list
     'do
     [:drop-trigger-if-exists sym :on table]
     (vec (concat [:create-trigger sym]
                  return
                  [:on table \\]
                  body)))))

(defn pg-defblock
  "creates generic defblock"
  {:added "4.0"}
  [[_ sym array]]
  (let [{:static/keys [schema
                       return]} (meta sym)
        ttok  (pg-full-token sym schema)]
    `(~'do [:create ~@return ~ttok ~@array])))
