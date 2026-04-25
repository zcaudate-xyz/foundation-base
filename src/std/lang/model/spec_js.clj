(ns std.lang.model.spec-js
  (:require [clojure.string]
            [std.html :as html]
            [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.preprocess-base :as preprocess-base]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-js.jsx :as jsx]
            [std.lang.model.spec-js.meta :as meta]
            [std.lang.model.spec-js.qml :as qml]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-js :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [std.lib.walk :as walk]))

(def ^:dynamic *template-fn* #'jsx/emit-jsx)

(defn emit-html
  "emits html"
  {:added "4.0"}
  ([arr _ mopts]
   (data/emit-maybe-multibody ["\n" ""] (html/html arr))))

(defn js-regex
  "outputs the js regex"
  {:added "4.0"}
  ([^java.util.regex.Pattern re]
   (str "/" (.pattern re) "/")))

(defn js-map-key
  "emits a map key"
  {:added "4.0"}
  ([key grammar mopts]
   (cond (or (keyword? key)
             (string? key))
         (data/default-map-key key grammar mopts)

         :else
         (str "[" (common/*emit-fn* key grammar mopts) "]"))))

(defn js-vector
  "emits a js vector"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [o (first arr)]
     (cond (empty? arr) "[]"
           
           (keyword? o)
           (*template-fn* arr grammar mopts)
           
           :else
           (data/emit-coll :vector arr grammar mopts)))))

(defn js-map
  "emits a js map"
  {:added "4.0"}
  [m grammar mopts]
  (let [rest (get m ':..)
        syms (get m ':#)
        out-syms  (map #(common/*emit-fn* % grammar mopts)
                       syms)
        out-keys  (map (fn [pair]
                         (data/emit-map-entry pair grammar mopts))
                       (dissoc m :# :..))
        out-rest  (if rest
                    (map #(str "..."
                               (common/*emit-fn* %
                                                 grammar
                                                 mopts)) 
                         (if (vector?  rest)
                           rest
                           [rest])))]
    (data/emit-coll-layout :map common/*indent*
                           (concat out-syms out-keys out-rest)
                           grammar mopts)))

(defn js-set
  "emits a js set"
  {:added "4.0"}
  ([arr grammar mopts]
   (cond (vector? (first arr))
         (common/*emit-fn* (apply list 'tab (first arr))
                           grammar
                           mopts)
         
         :else
         (f/->> arr
                (sort-by (fn [e]
                           (if (map? e)
                             [1 (f/strn e)]
                             [0 (f/strn e)])))
                (map (fn [e]
                       (cond (map? e)
                             (->> e
                                  (map (fn [pair]
                                         (data/emit-map-entry pair grammar mopts)))
                                  (clojure.string/join ","))
                             
                             (or (symbol? e)
                                 (string? e)
                                 (and (collection/form? e)
                                      (#{:..} (first e))))
                             (common/*emit-fn* e grammar mopts)
                             
                             :else
                             (f/error "Not allowed" {:entry e}))))
                (clojure.string/join ",")
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
   (let [{:keys [module] :as mopts}  (preprocess-base/macro-opts)
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
  "custom for:object code"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (let [[binding method] (cond (= k '_) [v  'Object.values]
                               (= v '_) [k  'Object.keys]
                               :else [[k v] 'Object.entries])]
    (apply list 'for [(list 'var* :let binding) :of (list method m)]
           body)))

(defn tf-for-array
  "custom for:array code"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (template/$ (for [(var* :let ~i := 0) (< ~i (. ~arr length)) (:++ ~i)]
             (var* :let ~v (. ~arr [~i]))
             ~@body)))
    (template/$ (for [(var* :let ~e) :of (% ~arr)]
           ~@body))))

(defn tf-for-iter
  "custom for:iter code"
  {:added "4.0"}
  [[_ [e it] & body]]
  (apply list 'for [(list 'var* :let e) :of (list '% it)]
         body))

(defn tf-for-return
  "for return transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (let [return-run? (and (seq? statement)
                         (= 'x:return-run (first statement)))
        success* (if (and return-run? final)
                   (list 'return success)
                   success)
        error* (if (and return-run? final)
                 (list 'return error)
                 error)
        out (if return-run?
              (let [[_ runner] statement]
                (template/$
                 (do (var ~res nil)
                     (var ~err nil)
                     (try
                       (~runner
                        (fn [value]
                          (:= ~res value)
                          (:= ~err nil))
                        (fn [value]
                          (:= ~res nil)
                          (:= ~err value)))
                       (if ~err
                         ~error*
                         ~success*)
                       (catch ~err ~error*)))))
              (let [cb (list 'fn [err res]
                             (list 'if err
                                   error
                                   success))]
                (walk/prewalk (fn [x]
                                (if (= x '(x:callback))
                                  cb
                                  x))
                              statement)))]
    (cond->> out
      (and final (not return-run?)) (list 'return))))

(defn tf-for-try
  "for try transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (template/$ (try
         (var ~res := ~statement)
         ~success
         (catch ~err ~error))))

(defn tf-for-async
  "for async transform"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error finally]}]]
  (let [return-run? (and (seq? statement)
                         (= 'x:return-run (first statement)))
        base (if return-run?
               (let [[_ runner] statement]
                 (template/$
                  (new Promise
                       (fn [resolve reject]
                         (try
                           (~runner resolve reject)
                           (catch ~err
                             (reject ~err)))))))
               (template/$
                (new Promise
                     (fn [resolve reject]
                       (resolve ~statement)))))]
    (template/$ (. ~base
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
                                     finally))])))))

(defn js-tf-prototype-get
  [[_ obj]]
  (list 'Object.getPrototypeOf obj))

(defn js-tf-prototype-set
  [[_ obj prototype]]
  (list 'Object.setPrototypeOf obj prototype))

(defn js-tf-prototype-create
  [[_ m]]
  (template/$
   (do (var out {})
       (for:object
        [[k f] ~m]
        (if (x:is-function? f)
          (:= (. out [k])
              (fn [...args]
                (return 
                 (f this ...args))))
          (:= (. out [k]) f)))
       (return out))))

(def +features+
  (-> (grammar/build :exclude [:pointer
                               :block
                               :data-range
                               :functional-core])
      (grammar/build:override
       {:var         {:symbol '#{var*}}
        :mul         {:value true}
        :defn        {:symbol '#{defn defn- defelem}}
        :with-global {:value true :raw "globalThis"}
        :defclass    {:macro  #'js-defclass    :emit :macro}
        :for-object  {:macro  #'tf-for-object  :emit :macro}
        :for-array   {:macro  #'tf-for-array   :emit :macro}
        :for-iter    {:macro  #'tf-for-iter    :emit :macro}
        :for-return  {:macro  #'tf-for-return  :emit :macro}
        :for-try     {:macro  #'tf-for-try     :emit :macro}
        :for-async   {:macro  #'tf-for-async   :emit :macro}
        :prototype-get       {:macro #'js-tf-prototype-get     :emit :macro}
        :prototype-set       {:macro #'js-tf-prototype-set     :emit :macro}
        :prototype-create    {:macro #'js-tf-prototype-create  :emit :macro
                              :op-spec {:allow-blocks true}}
        :prototype-tostring  {:emit :unit  :default "toString"}})
      (grammar/build:override fn/+js+)
      (grammar/build:extend
       {:property   {:op :property  :symbol  '#{property}   :assign ":" :raw "property" :value true :emit :def-assign}
        :teq        {:op :teq       :symbol  '#{===}        :raw "===" :emit :bi}
        :tneq       {:op :tneq      :symbol  '#{not==}      :raw "!==" :emit :bi}
        :delete     {:op :delete    :symbol  '#{del}        :raw "delete" :value true :emit :prefix}
        :typeof     {:op :typeof    :symbol  '#{typeof}     :raw "typeof" :emit :prefix}
        :instanceof {:op :instof    :symbol  '#{instanceof} :raw "instanceof" :emit :bi}
        :undef      {:op :undef     :symbol  '#{undefined}  :raw "undefined" :value true :emit :throw}
        :nan        {:op :nan       :symbol  '#{NaN} :raw "NaN" :value true :emit :throw}
        :vargs      {:op :vargs     :symbol  '#{...} :raw "...vargs" :value true :emit :throw}
        :var-let    {:op :var-let   :symbol  '#{var}     :macro  #'tf-var-let :emit :macro}
        :var-const  {:op :var-const :symbol  '#{const}   :macro  #'tf-var-const :emit :macro}})))

(def +template+
  (->> {:banned #{:keyword}
        :allow   {:assign  #{:symbol :vector :set :map}}
        :highlight '#{return break del tab reject}
        :default  {:common    {:namespace-full "$$"}
                   :function  {:raw "function" :space ""}}
        :token    {:nil       {:as "null"}
                   :regex     {:custom #'js-regex}
                   :string    {}
                   :symbol    {:global #'js-symbol-global}}
        :block    {:for       {:parameter {:sep ";"}}}
        :data     {:vector    {:custom #'js-vector}
                   :set       {:custom #'js-set}
                   :map       {:custom #'js-map}
                   :map-entry {:key-fn #'js-map-key}}
        :function {:defgen    {:raw "function*"}
                   :fn.inner  {:raw ""}}
        :define   {:defglobal {:raw ""}
                   :def       {:raw "var"}
                   :declare   {:raw "var"}}
        :xtalk    {:notify    {:custom true}}}
       (collection/merge-nested (emit/default-grammar))))

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
