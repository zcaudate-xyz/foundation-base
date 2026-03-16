(ns rt.postgres.analyze.defn
  "Static analysis for defn.pg forms.
   
   Extracts function name, parameters, metadata, body references from forms like:
   
     (defn.pg ^{:%% :sql :- [:uuid]}
       get-user-id
       [:citext i-handle]
       (pg/t:id -/User {:where {:handle i-handle}}))
   
     (defn.pg create-user
       \"creates a user entry\"
       {:added \"0.1\"}
       [:uuid i-user-id :jsonb m :jsonb o-op]
       (let [...] (return o-user)))"
  (:require [clojure.string :as str]))

;;
;; Parameter parsing
;;

(def pg-types
  "Known PostgreSQL parameter types in the DSL."
  #{:uuid :text :citext :jsonb :bigint :integer :numeric :boolean
    :timestamp :date :time :bytea :smallint :real :double-precision
    :serial :oid :void :record :trigger})

(defn parse-params
  "Parses the parameter vector from a defn.pg form.
   Parameters alternate between type keywords and names:
   [:uuid i-user-id :jsonb m :text i-name]
   
   Returns a vector of {:name symbol :type keyword} maps.
   Also handles the alternate form with parens:
   ([:text i-tag :uuid i-user-id :jsonb i-data] ...)"
  [args]
  (when (and (vector? args) (seq args))
    (let [;; Handle variadic args
          pairs (partition 2 args)]
      (->> pairs
           (mapv (fn [[type-kw param-name]]
                   (cond-> {:name param-name
                            :type type-kw}
                     ;; Check naming conventions
                     (and (symbol? param-name)
                          (str/starts-with? (str param-name) "i-"))
                     (assoc :convention :input)
                     
                     (and (symbol? param-name)
                          (str/starts-with? (str param-name) "o-"))
                     (assoc :convention :output)
                     
                     (and (symbol? param-name)
                          (str/starts-with? (str param-name) "v-"))
                     (assoc :convention :variable))))))))

;;
;; Body analysis - walks the form tree to find references
;;

(defn collect-symbols
  "Recursively collects all symbols from a form."
  [form]
  (cond
    (symbol? form) [form]
    (list? form)   (mapcat collect-symbols form)
    (vector? form) (mapcat collect-symbols form)
    (map? form)    (mapcat collect-symbols (concat (keys form) (vals form)))
    (set? form)    (mapcat collect-symbols form)
    (seq? form)    (mapcat collect-symbols form)
    :else []))

(defn type-reference?
  "Returns true if a symbol looks like a type reference (e.g., -/User, tsb/Rev)."
  [sym]
  (and (symbol? sym)
       (namespace sym)
       (let [n (name sym)]
         (and (not (str/starts-with? n "t:"))
              (not (str/starts-with? n "g:"))
              (Character/isUpperCase (first n))))))

(defn function-reference?
  "Returns true if a symbol looks like a function reference (e.g., -/create-user, tsb/insert-rev)."
  [sym]
  (and (symbol? sym)
       (namespace sym)
       (let [n (name sym)]
         (and (not (str/blank? n))
              (Character/isLowerCase (first n))))))

(defn pg-operation?
  "Returns true if a symbol is a pg/t:* or pg/g:* operation."
  [sym]
  (and (symbol? sym)
       (let [ns-part (namespace sym)
             n (name sym)]
         (and ns-part
              (or (str/starts-with? n "t:")
                  (str/starts-with? n "g:"))))))

(defn extract-body-refs
  "Walks the body forms and extracts references to types, functions, and pg operations.
   Returns a map with:
   - :type-refs     - set of type symbols referenced (e.g., -/User)
   - :fn-refs       - set of function symbols referenced (e.g., tsb/insert-rev)
   - :pg-ops        - set of pg operations used (e.g., pg/t:insert)
   - :local-refs    - set of current-module references (prefixed with -/)"
  [body]
  (let [syms (collect-symbols body)
        qualified (filter #(and (symbol? %) (namespace %)) syms)]
    {:type-refs (set (filter type-reference? qualified))
     :fn-refs (set (filter function-reference? qualified))
     :pg-ops (set (filter pg-operation? qualified))
     :local-refs (set (filter #(= "-" (namespace %)) qualified))}))

;;
;; Main analysis
;;

(defn analyze-defn
  "Analyzes a defn.pg form and returns a map with:
   - :name        - function symbol name
   - :docstring   - documentation string
   - :attr-map    - {:added ...} map
   - :metadata    - metadata from ^{...}
   - :language    - PL language (:default, :sql, :js, :python, :lua)
   - :return-type - return type annotation from :- key
   - :api-flags   - API exposure flags from :api/flags
   - :props       - function properties (e.g., [:security :definer])
   - :params      - vector of parameter maps
   - :body        - raw body forms
   - :body-refs   - references extracted from body
   - :param-count - number of parameters
   
   Handles both regular and multi-arity forms."
  [form]
  (when (and (list? form) (= 'defn.pg (first form)))
    (let [parts (rest form)
          ;; Find the symbol name
          sym (first (filter symbol? parts))
          sym-meta (or (meta sym) {})
          
          ;; Extract after symbol
          after-sym (rest (drop-while #(not (= % sym)) parts))
          
          ;; Docstring (optional)
          docstring (when (string? (first after-sym))
                      (first after-sym))
          after-doc (if docstring (rest after-sym) after-sym)
          
          ;; Attr map like {:added "0.1"} (optional)
          attr-map (when (and (map? (first after-doc))
                              (not (vector? (first after-doc))))
                     (first after-doc))
          after-attr (if attr-map (rest after-doc) after-doc)
          
          ;; Handle both regular and multi-arity
          ;; Regular: [:type arg ...] body...
          ;; Multi-arity (rare): ([:type arg ...] body...) 
          first-elem (first after-attr)
          [args body] (cond
                        ;; Regular form: vector of params followed by body
                        (vector? first-elem)
                        [first-elem (rest after-attr)]
                        
                        ;; Multi-arity: first elem is a list starting with vector
                        (and (list? first-elem) (vector? (first first-elem)))
                        [(first first-elem) (rest first-elem)]
                        
                        :else [nil (seq after-attr)])
          
          ;; Parse metadata
          language (or (:%% sym-meta) :default)
          return-type (:- sym-meta)
          api-flags (:api/flags sym-meta)
          props (:props sym-meta)
          
          ;; Parse parameters
          params (parse-params args)
          
          ;; Extract body references
          body-refs (extract-body-refs body)]
      
      {:name sym
       :docstring docstring
       :attr-map attr-map
       :metadata (dissoc sym-meta :line :column :file)
       :language language
       :return-type return-type
       :api-flags api-flags
       :props props
       :params (vec (or params []))
       :body (vec body)
       :body-refs body-refs
       :param-count (count (or params []))
       :is-sql (= :sql language)
       :is-query (= :sb/query (:expose api-flags))
       :is-auth (= :sb/auth (:expose api-flags))})))

;;
;; Parameter queries
;;

(defn input-params
  "Returns parameters following i-* naming convention."
  [analysis]
  (filter #(= :input (:convention %)) (:params analysis)))

(defn output-params
  "Returns parameters following o-* naming convention."
  [analysis]
  (filter #(= :output (:convention %)) (:params analysis)))

;;
;; Validation
;;

(defn validate-defn
  "Validates a defn.pg analysis result. Returns a vector of issues."
  [analysis]
  (let [{:keys [name params language body-refs body]} analysis
        issues (transient [])]
    
    ;; Check function naming convention (should be lowercase with dashes)
    (when (and name (Character/isUpperCase (first (str name))))
      (conj! issues {:level :warning
                     :message (str "Function name '" name "' should be lowercase")
                     :name name}))
    
    ;; Check for empty parameter list
    (when (empty? params)
      (conj! issues {:level :info
                     :message (str "Function '" name "' has no parameters")
                     :name name}))
    
    ;; Check parameter types are recognized
    (doseq [{:keys [type] param-name :name} params]
      (when (and (keyword? type) (not (contains? pg-types type)))
        (conj! issues {:level :warning
                       :message (str "Parameter '" param-name "' in function '" name
                                     "' has unrecognized type :" (clojure.core/name type))
                       :name name
                       :param param-name})))
    
    ;; Check SQL functions have exactly one body expression
    (when (and (= :sql language) (> (count body) 1))
      (conj! issues {:level :error
                     :message (str "SQL function '" name "' should have exactly one body expression, found " (count body))
                     :name name}))
    
    ;; Check for missing return in non-SQL functions
    (when (and (not= :sql language)
               (seq body)
               (not (some #(and (list? %) (= 'return (first %)))
                          (collect-symbols (last body)))))
      ;; Only a soft warning - the function might use let bindings with return inside
      nil)
    
    ;; Check naming conventions for params
    (doseq [{param-name :name :keys [convention]} params]
      (when (and (symbol? param-name)
                 (not convention)
                 (not= 'm param-name))
        (conj! issues {:level :info
                       :message (str "Parameter '" param-name "' in function '" name
                                     "' does not follow i-/o-/v- naming convention")
                       :name name
                       :param param-name})))
    
    (persistent! issues)))
