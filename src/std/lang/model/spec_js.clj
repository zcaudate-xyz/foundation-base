(ns std.lang.model.spec-js
  (:require [std.lang.model.spec-js.meta :as meta]
            [std.lang.model.spec-js.jsx :as jsx]
            [std.lang.model.spec-js.qml :as qml]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.util :as ut]
	    [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-js :as fn]
            [std.html :as html]
            [std.string :as str]
            [std.lib :as h]))

(def ^:dynamic *template-fn* #'jsx/emit-jsx)

(defn emit-html
  "emits html"
  {:added "4.0"}
  ([arr _ nsp]
   (data/emit-maybe-multibody ["\n" ""] (html/html arr))))

(defn js-regex
  "outputs the js regex"
  {:added "4.0"}
  ([^java.util.regex.Pattern re]
   (str "/" (.pattern re) "/")))

(defn js-map-key
  "emits a map key"
  {:added "4.0"}
  ([key grammar nsp]
   (data/default-map-key key grammar nsp)))

(defn js-vector
  "emits a js vector"
  {:added "4.0"}
  ([arr grammar nsp]
   (let [o (first arr)]
     (cond (empty? arr) "[]"
           
           (keyword? o)
           (*template-fn* arr grammar nsp)
           
           :else
           (data/emit-coll :vector arr grammar nsp)))))

(defn js-set
  "emits a js set"
  {:added "4.0"}
  ([arr grammar mopts]
   (cond (vector? (first arr))
         (common/*emit-fn* (apply list 'tab (first arr))
                           grammar
                           mopts)
         
         :else
         (h/->> arr
                (sort-by (fn [e]
                           (if (map? e)
                             [1 (h/strn e)]
                             [0 (h/strn e)])))
                (map (fn [e]
                       (cond (map? e)
                             (->> e
                                  (map (fn [pair]
                                         (data/emit-map-entry pair grammar mopts)))
                                  (str/join ","))
                             
                             (or (symbol? e)
                                 (string? e)
                                 (and (h/form? e)
                                      (#{:..} (first e))))
                             (common/*emit-fn* e grammar mopts)
                             
                             :else
                             (h/error "Not allowed" {:entry e}))))
                (str/join ",")
                (str "{" % "}")))))

(defn- js-symbol-global
  [fsym grammar mopts]
  (list '. 'globalThis [(helper/emit-symbol-full
                         fsym
                         (namespace fsym)
                         grammar)]))

(defn js-defclass
  "creates a defclass function"
  {:added "4.0"}
  ([[_ sym inherit & body]]
   (let [{:keys [module] :as mopts}  (preprocess/macro-opts)
         body      (top/transform-defclass-inner body)
         sym-name  (symbol (if module (name (:id module)))
                           (name sym))
         supers (list 'quote (vec (remove keyword? inherit)))]
     `(:- :class ~sym-name :extends ~supers \{
          (\\
           \\ (\| (do ~@body))
           \\)
          \}))))

(defn tf-var-let
  "outputs the let keyword"
  {:added "4.0"}
  [[_ decl & args]]
  (list 'var* :let decl := (last args)))

(defn tf-var-const
  "outputs the const keyword"
  {:added "4.0"}
  [[_ decl & args]]
  (list 'var* :const decl := (last args)))

(defn tf-for-object
  "custom for:object code
 
   (tf-for-object '(for:object [[k v] {:a 1}]
                               [k v]))
   => '(for [(var* :let [k v]) :of (Object.entries {:a 1})] [k v])"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (let [[binding method] (cond (= k '_) [v  'Object.values]
                               (= v '_) [k  'Object.keys]
                               :else [[k v] 'Object.entries])]
    (apply list 'for [(list 'var* :let binding) :of (list method m)]
           body)))

(defn tf-for-array
  "custom for:array code
 
   (tf-for-array '(for:array [e [1 2 3 4 5]]
                              [k v]))
   => '(for [(var* :let e) :of (% [1 2 3 4 5])] [k v])
 
   (tf-for-array '(for:array [[i e] arr]
                             [k v]))
   => '(for [(var* :let i := 0) (< i (. arr length))
             (:++ i)] (var* :let e (. arr [i])) [k v])"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (h/$ (for [(var* :let ~i := 0) (< ~i (. ~arr length)) (:++ ~i)]
             (var* :let ~v (. ~arr [~i]))
             ~@body)))
    (h/$ (for [(var* :let ~e) :of (% ~arr)]
           ~@body))))

(defn tf-for-iter
  "custom for:iter code
   
   (tf-for-iter '(for:iter [e iter]
                           e))
   => '(for [(var* :let e) :of (% iter)] e)"
  {:added "4.0"}
  [[_ [e it] & body]]
  (apply list 'for [(list 'var* :let e) :of (list '% it)]
         body))

(defn tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (let [cb (list 'fn [err res]
                 (list 'if err
                       error
                       success))
        out (h/prewalk (fn [x]
                         (if (= x '(x:callback))
                           cb
                           x))
                       statement)]
    (cond->> out
      final (list 'return))))

(defn tf-for-try
  "for try transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (h/$ (try
         (var ~res := ~statement)
         ~success
         (catch ~err ~error))))

(defn tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (h/$ (. (new Promise (fn [resolve reject]
                         (resolve ~statement)))
          ~@(if success
              [(list 'then
                     (list 'fn [res]
                           success))])
          ~@(if error
              [(list 'catch
                     (list 'fn [err]
                           error))])
          ~@(if finally
              [(list 'finally
                     (list 'fn '[]
                           finally))]))))

(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block
                               :data-range])
      (grammar/build:override
       {:var        {:symbol '#{var*}}
        :mul        {:value true}
        :defn       {:symbol '#{defn defn- defelem}}
        :with-global {:value true :raw "globalThis"}
        :defclass   {:macro  #'js-defclass    :emit :macro}
        :for-object {:macro  #'tf-for-object  :emit :macro}
        :for-array  {:macro  #'tf-for-array   :emit :macro}
        :for-iter   {:macro  #'tf-for-iter    :emit :macro}
        :for-return {:macro  #'tf-for-return  :emit :macro}
        :for-try    {:macro  #'tf-for-try     :emit :macro}
        :for-async  {:macro  #'tf-for-async :emit :macro}})
      (grammar/build:override fn/+js+)
      (grammar/build:extend
       {:property   {:op :property  :symbol  '#{property}   :assign ":" :raw "property" :emit :def-assign}
        :teq        {:op :teq       :symbol  '#{===}        :raw "===" :emit :bi}
        :tneq       {:op :tneq      :symbol  '#{!==}        :raw "!==" :emit :bi}
        :delete     {:op :delete    :symbol  '#{delete}     :raw "delete" :value true :emit :prefix}
        :typeof     {:op :typeof    :symbol  '#{typeof}     :raw "typeof" :emit :prefix}
        :instanceof {:op :instof    :symbol  '#{instanceof} :raw "instanceof" :emit :bi}
        :undef      {:op :undef     :symbol  '#{undefined}  :raw "undefined" :value true :emit :throw}
        :nan        {:op :nan       :symbol  '#{NaN} :raw "NaN" :value true :emit :throw}
        :vargs      {:op :vargs     :symbol  '#{...} :raw "...vargs" :value true :emit :throw}
        :var-let    {:op :var-let   :symbol  '#{var}     :macro  #'tf-var-let :type :macro}
        :var-const  {:op :var-const :symbol  '#{const}   :macro  #'tf-var-const :type :macro}})))

(def +template+
  (->> {:banned #{:keyword}
        :allow   {:assign  #{:symbol :vector :set}}
        :highlight '#{return break delete tab reject}
        :default  {:common    {:namespace-full "$$"}
                   :function  {:raw "function" :space ""}}
        :token    {:nil       {:as "null"}
                   :regex     {:custom #'js-regex}
                   :string    {}
                   :symbol    {:global #'js-symbol-global}}
        :block    {:for       {:parameter {:sep ";"}}}
        :data     {:map-entry {:key-fn #'js-map-key}
                   :vector    {:custom #'js-vector}
                   :set       {:custom #'js-set}}
        :function {:defgen    {:raw "function*"}
                   :fn.inner  {:raw ""}}
        :define   {:defglobal {:raw ""}
                   :def       {:raw "var"}
                   :declare   {:raw "var"}}
        :xtalk    {:notify    {:custom true}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :js
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :js
              :parent :xtalk
              :meta meta/+meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))

