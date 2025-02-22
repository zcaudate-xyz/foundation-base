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
    :image  :jsonb})

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
                            defrole  #{(str id)})
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
                                     (h/error "Not found." {:input sym}))]
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
  [[_ sym array]]
  (let [{:static/keys [schema]} (meta sym)
        ttok  (pg-full-token sym schema)]
    `(~'do [:create-index :if-not-exists ~ttok ~@array])))

(defn pg-defblock
  "creates generic defblock"
  {:added "4.0"}
  [[_ sym array]]
  (let [{:static/keys [schema
                       return]} (meta sym)
        ttok  (pg-full-token sym schema)]
    `(~'do [:create ~@return ~ttok ~@array])))

