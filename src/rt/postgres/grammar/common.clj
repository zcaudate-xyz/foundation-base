(ns rt.postgres.grammar.common
  (:require [rt.postgres.grammar.meta :as meta]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.tf :as tf]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.script :as script]
            [std.lang.base.pointer :as ptr]
            [std.string :as str]
            [std.lib :as h]))

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
                    (str/split (str table) #"\."))
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

(defn pg-hydrate
  "hydrate function for top level entries"
  {:added "4.0"}
  ([[op sym & body] grammar mopts]
   (let [reserved (h/qualified-keys (get-in grammar [:reserved op])
                                    :static)
         static (merge (pg-hydrate-module-static (:module mopts))
                       reserved)]
     [static (apply list op (with-meta sym (merge (meta sym) static))
                    body)])))

(defn pg-string
  "constructs a pg string"
  {:added "4.0"}
  ([s]
   (-> (pr-str s)
       (str/replace #"'" "''")
       (str/replace #"^\"" "'")
       (str/replace #"\"$" "'")
       (str/replace #"\\\"" "\"")
       (str/replace #"\\\\" "\\\\"))))

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
           (h/error "Not Allowed" {:value e}))
         
         :else
         (let [v (first e)]
           (cond (string? v) (str "\"" v "\"")

                 
                 (and (symbol? v)
                      (re-find #"\w-\w+" (str v)))
                 (common/*emit-fn*  (tf/pg-tf-js [nil e]) grammar mopts)
                 
                 :else
                 (h/error "Not Allowed" {:value e}))))))

(defn pg-array
  "creates an array object
 
   (common/pg-array '(array 1 2 3 4 5)
                    g/+grammar+
                    {})
   => \"ARRAY[1,2,3,4,5]\""
  {:added "4.0"}
  ([[_ & arr] grammar mopts]
   (let [str-array (common/emit-array arr grammar mopts common/*emit-fn*)]
     (str "ARRAY[" (str/join "," str-array) "]"))))


(defn pg-invoke-typecast
  "emits a typecast call"
  {:added "4.0"}
  [form grammar mopts]
  (let [val   (last form)
        types (str/join (map (fn [v]
                               (cond (keyword? v)
                                     (str/upper-case (h/strn v))
                                     
                                     (or (and (h/form? v)
                                              (not= '. (first v)))
                                         (vector? v))
                                     (h/strn v)
                                     
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
   (let [tok #{(str/replace (h/strn tok) #"\." "_")}]
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
         module (book/get-module book sym-module)
         {:keys [section] :as e} (or (get-in module [:code sym-id])
                                     (get-in module [:fragment sym-id])
                                     (h/error "Token Not found."
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
        vals  (list 'quote (map h/strn array))]
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
  [[_ sym doc? attr? [table & cols] :as form]]
  (let [[{:keys [doc]
          :as mdefn} [_ sym [table & cols] body]] (grammar-spec/format-defn form)]
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

;;
;; defpartition
;;

(defn pg-partition-name
  "constructs partition name"
  {:added "4.0"}
  [base val stack]
  (let [parts (cons base (cons val stack))]
    (clojure.string/join "_" (map h/strn parts))))

(defn pg-partition-quote-id
  "quotes an identifier if needed"
  [s]
  (str "\"" s "\""))

(defn pg-partition-full-name
  [schema table]
  (if schema
    (str schema "." (pg-partition-quote-id table))
    (pg-partition-quote-id table)))

(defn pg-partition-def
  "recursive definition for partition"
  {:added "4.0"}
  [parent-sym base-name current-spec remaining-specs stack]
  (let [{:keys [use in default]} current-spec
        next-spec (first remaining-specs)
        next-col  (when-let [u (:use next-spec)] (clojure.string/replace (name u) "-" "_"))
        parent-ns (namespace parent-sym)
        [schema-prefix table-name] (if parent-ns
                                     [nil base-name]
                                     (let [parts (clojure.string/split (name parent-sym) #"\.")]
                                       (if (> (count parts) 1)
                                         [(first parts) (last parts)]
                                         [nil base-name])))
        
        full-parent (if parent-ns
                      (str parent-ns "." (pg-partition-quote-id table-name))
                      (pg-partition-full-name schema-prefix table-name))]
    
    (if default
      (let [new-name (str table-name "_default")
            full-new (if parent-ns
                       (str parent-ns "." (pg-partition-quote-id new-name))
                       (pg-partition-full-name schema-prefix new-name))]
        (list
         (vec (concat [:create-table :if-not-exists (list 'raw full-new)
                       :partition-of (list 'raw full-parent) :default]
                      (if next-col
                        [:partition-by :list (list 'quote (list (symbol next-col)))]
                        [])
                      #_[\;]))))
      
      (mapcat (fn [val]
                (let [new-name (pg-partition-name table-name val stack)
                      new-sym  (cond parent-ns (symbol parent-ns new-name)
                                     schema-prefix (symbol (str schema-prefix "." new-name))
                                     :else (symbol new-name))
                      full-new (if parent-ns
                                 (str parent-ns "." (pg-partition-quote-id new-name))
                                 (pg-partition-full-name schema-prefix new-name))]
                  
                  (concat
                   [(vec (concat [:create-table :if-not-exists (list 'raw full-new)
                                  :partition-of (list 'raw full-parent)
                                  :for :values :in (list 'quote (list val))]
                                 (if next-col
                                   [:partition-by :list (list 'quote (list (symbol next-col)))]
                                   [])
                                 #_[\;]))]
                   (if (seq remaining-specs)
                     (pg-partition-def new-sym table-name next-spec (rest remaining-specs) (cons val stack))
                     nil))))
              in))))

(defn pg-defpartition
  "defpartition block"
  {:added "4.0"}
  [[_ sym [parent] specs]]
  (let [base-name (name parent)
        statements (pg-partition-def parent base-name (first specs) (rest specs) [])]
     (apply list 'do statements)))
