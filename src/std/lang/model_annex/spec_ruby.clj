(ns std.lang.model-annex.spec-ruby
  (:require [clojure.set :as set]
             [clojure.string]
             [std.lang.base.book :as book]
             [std.lang.base.emit :as emit]
             [std.lang.base.emit-common :as common]
             [std.lang.base.emit-helper :as helper]
             [std.lang.base.preprocess-base :as preprocess-base]
             [std.lang.base.emit-top-level :as top]
             [std.lang.base.grammar :as grammar]
             [std.lang.base.script :as script]
             [std.lang.base.util :as ut]
             [std.lang.model.spec-xtalk]
             [std.lang.model-annex.spec-xtalk.fn-ruby :as fn]
             [std.lib.collection :as collection]
             [std.lib.template :as template]))

(defn ruby-symbol
  "emit ruby symbol
   (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
    => \":a\"
    (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
    => \"a\""
  {:added "4.1"}
  [sym grammar mopts]
  (let [sym-name (when (symbol? sym) (name sym))]
    (cond (keyword? sym)
          (str ":" (name sym))

          (and (symbol? sym)
               (nil? (namespace sym))
               (re-matches #"[A-Z][A-Z0-9_]*" sym-name)
               (clojure.string/includes? sym-name "_"))
          (str "\"" sym-name "\"")

          (and (symbol? sym)
               (nil? (namespace sym)))
          (clojure.string/replace sym-name "-" "_")

          :else
          (common/emit-symbol sym grammar mopts))))

(defn ruby-symbol-global
  [key _grammar _mopts]
  (let [globals (list ':- "($__globals__ ||= {})")]
    (if (= key '!:G)
      globals
      (list '. globals [(ut/sym-default-str key)]))))

(defn ruby-var
  "emit ruby variable
   (spec-ruby/ruby-var '(var a 1))
    => '(:= a 1)"
  {:added "4.1"}
  [[_ sym & args]]
  (list ':= sym (last args)))

(defn ruby-map
  "emit ruby hash
   (l/emit-as :ruby '[{:a 1 :b 2}])
    => \"{\\\"a\\\" => 1, \\\"b\\\" => 2}\""
  {:added "4.1"}
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* k grammar mopts)
                             " => "
                             (common/*emit-fn* v grammar mopts)))
                      m)]
    (str "{" (clojure.string/join ", " entries) "}")))

(defn- callable-form?
  [form]
  (and (seq? form)
       (#{'fn 'fn.inner} (first form))))

(defn- ruby-zero-arg-call?
  [prop]
  (and (seq? prop)
       (symbol? (first prop))
       (not= 'call (first prop))
       (empty? (rest prop))))

(defn- ruby-dot-entry
  [prop grammar mopts]
  (if (ruby-zero-arg-call? prop)
    (str "." (common/emit-symbol (first prop) grammar mopts))
    (common/emit-index-entry prop grammar mopts)))

(defn ruby-dot
  [[_ obj & props]]
  (let [grammar preprocess-base/*macro-grammar*
        mopts   preprocess-base/*macro-opts*]
    (list ':- (str (common/emit-wrapping obj grammar mopts)
                   (apply str (map #(ruby-dot-entry % grammar mopts) props))))))

(defn ruby-emit-range
  [separator [_ start & more] grammar mopts]
  (let [[step end] (if (= 2 (count more))
                     more
                     [1 (first more)])]
    (assert end "Ruby range requires an end value")
    (assert (= 1 step) "Ruby range does not support custom step values")
    (str (common/*emit-fn* start grammar mopts)
         separator
         (common/*emit-fn* end grammar mopts))))

(declare rewrite-callable-form)

(defn- callable-var-binding
  [form]
  (when (and (seq? form)
             (= 'var (first form))
             (symbol? (second form))
             (callable-form? (last form)))
    (second form)))

(defn- collect-callable-vars
  [form]
  (cond
    (callable-form? form)
    #{}

    (seq? form)
    (let [binding (callable-var-binding form)]
      (cond-> (reduce set/union #{} (map collect-callable-vars form))
        binding (conj binding)))

    (vector? form)
    (reduce set/union #{} (map collect-callable-vars form))

    (map? form)
    (reduce set/union #{} (map collect-callable-vars (mapcat identity form)))

    (set? form)
    (reduce set/union #{} (map collect-callable-vars form))

    :else
    #{}))

(defn rewrite-callable-body
  ([args body]
   (rewrite-callable-body #{} args body))
  ([inherited args body]
   (let [callables (into (set inherited)
                         (concat (filter symbol? args)
                                 (collect-callable-vars body)))]
     (mapv #(rewrite-callable-form % callables) body))))

(defn- ruby-global-const-access?
  [form]
  (and (seq? form)
       (= '. (first form))
       (= '!:G (second form))
       (vector? (nth form 2 nil))
       (= 1 (count (nth form 2)))
       (symbol? (first (nth form 2)))
       (re-matches #"[A-Z][A-Z0-9_]*"
                   (name (first (nth form 2))))))

(defn rewrite-callable-form
  [form callables]
  (cond
    (ruby-global-const-access? form)
    (list '. '!:G [(name (first (nth form 2)))])

    (seq? form)
    (let [head (first form)]
      (cond
        (callable-form? form)
        (let [[tag args & body] form]
          (apply list tag args (rewrite-callable-body callables args body)))

        (and (symbol? head)
             (contains? callables head))
        (list '. head
              (apply list 'call
                     (map #(rewrite-callable-form % callables)
                          (rest form))))

        :else
        (apply list (map #(rewrite-callable-form % callables) form))))

    (vector? form)
    (mapv #(rewrite-callable-form % callables) form)

    (map? form)
    (into {} (map (fn [[k v]]
                    [(rewrite-callable-form k callables)
                     (rewrite-callable-form v callables)]))
          form)

    (set? form)
    (set (map #(rewrite-callable-form % callables) form))

    :else
    form))

(defn rewrite-callable-forms
  [forms]
  (let [callables (collect-callable-vars forms)]
    (mapv #(rewrite-callable-form % callables) forms)))

(defn ruby-defn-
  [form grammar mopts]
  (top/emit-top-level :defn form grammar mopts))

(defn ruby-defn
  [[_ sym args & body]]
  (list* 'defn- sym args (rewrite-callable-body args body)))

(defn ruby-fn
  "basic transform for ruby blocks
   (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
   => '(fn.inner [a] (+ a 1))"
  {:added "4.1"}
  ([[_ args & body]]
   (let [body     (rewrite-callable-body args body)
         grammar  preprocess-base/*macro-grammar*
         mopts    preprocess-base/*macro-opts*
         args-str (clojure.string/join ", "
                                       (common/emit-array args grammar mopts))
         body-str (common/*emit-fn* (cons 'do body) grammar mopts)]
     (list ':- "->(" args-str ") {\n" body-str "\n}"))))

(defn tf-for-array
  "transform for `for:array`"
  {:added "4.1"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i v] e]
      (template/$
       (do (var ~i 0)
           (while (< ~i (. ~arr length))
             (var ~v (. ~arr [~i]))
             ~@body
             (:= ~i (+ ~i 1))))))
    (let [idx (gensym "idx__")]
      (template/$
       (do (var ~idx 0)
           (while (< ~idx (. ~arr length))
             (var ~e (. ~arr [~idx]))
             ~@body
             (:= ~idx (+ ~idx 1))))))))

(defn tf-for-object
  "transform for `for:object`"
  {:added "4.1"}
  [[_ [[k v] m] & body]]
  (let [keys (gensym "keys__")
        idx  (gensym "idx__")
        key  (if (= k '_) (gensym "key__") k)]
    (template/$
     (do (var ~keys (. ~m keys))
         (var ~idx 0)
         (while (< ~idx (. ~keys length))
           (var ~key (. ~keys [~idx]))
           ~@(if (not= v '_)
               [(list 'var v (list '. m [key]))]
               [])
           ~@body
           (:= ~idx (+ ~idx 1)))))))

(defn tf-for-iter
  "transform for `for:iter`"
  {:added "4.1"}
  [[_ [e it] & body]]
  (apply list 'for [e :in it]
         (or (not-empty body)
             ['(nil)])))

(defn tf-for-index
  "transform for `for:index`"
  {:added "4.1"}
  [[_ [i [start stop step]] & body]]
  (let [step (or step 1)
        sign (if (and (number? step)
                      (neg? step))
               '>
               '<)]
    (template/$
     (do (var ~i ~start)
         (while (~sign ~i ~stop)
           ~@body
           (:= ~i (+ ~i ~step)))))))

(def +features+
  (-> (grammar/build :exclude [:pointer :block :data-range])
      (grammar/build:override
        {:var        {:macro #'ruby-var :emit :macro}
         :index      {:macro #'ruby-dot :emit :macro}
         :for-object {:macro #'tf-for-object :emit :macro}
         :for-array  {:macro #'tf-for-array  :emit :macro}
         :for-iter   {:macro #'tf-for-iter   :emit :macro}
         :for-index  {:macro #'tf-for-index  :emit :macro}
         :defn       {:symbol #{'defn}   :macro #'ruby-defn :emit :macro}
         :defgen     {:symbol #{'defgen} :macro #'ruby-defn :emit :macro}
         :spread     {:raw "*" :emit :pre}
         :with-global {:value true :raw "($__globals__ ||= {})"}
           :and        {:raw "&&"}
           :or         {:raw "||"}
           :not        {:raw "!" :emit :prefix}
           :eq         {:raw "=="}
          :pow        {:raw "**"}
          :fn         {:macro  #'ruby-fn   :emit :macro}
         :neq        {:raw "!="}
         :gt         {:raw ">"}
         :lt         {:raw "<"}
         :gte        {:raw ">="}
        :lte        {:raw "<="}})
        (grammar/build:override fn/+ruby+)
        (grammar/build:extend
         {:defn-      {:op :defn- :symbol #{'defn-} :type :block :emit #'ruby-defn-}
          :assign     {:op :assign :symbol #{':=} :raw "=" :emit :infix}
          :to         {:op :to :symbol #{'to} :emit (fn [form grammar mopts]
                                                      (ruby-emit-range ".." form grammar mopts))}
          :to-e       {:op :to-e :symbol #{'to-e} :emit (fn [form grammar mopts]
                                                          (ruby-emit-range "..." form grammar mopts))}
          :puts       {:op :puts :symbol #{'puts} :raw "puts" :emit :prefix}
          :nil?       {:op :nil? :symbol #{'nil?} :raw "nil?" :emit :postfix}
          :attr       {:op :attr :symbol #{'attr_accessor} :raw "attr_accessor" :emit :prefix}
         :end        {:op :end  :symbol #{'end}  :raw "end"  :emit :token}})))

(def +template+
  (->> {:banned #{}
         :allow   {:assign  #{:symbol}}
         :default {:common    {:statement ""}
                    :block     {:parameter {:start " " :end ""}
                                 :body      {:start "" :end "end" :append false}}
                    :function  {:raw "def"
                                 :body      {:start "" :end "end"}}}
         :block   {:while     {:body {:start "" :end "end"}}
                   :branch    {:wrap {:start "" :end "end"}
                               :control {:default {:parameter {:start " " :end ""}
                                                   :body {:append true :start "" :end ""}}
                                         :if      {:raw "if"}
                                         :elseif  {:raw "elsif"}
                                         :else    {:raw "else"}}}
                   :try      {:raw  "begin"
                              :wrap {:start "" :end "end"}
                              :body {:start "" :end ""}
                              :control {:catch  {:raw  "rescue Exception =>"
                                                 :body {:start "" :end ""}}}}}
         :token   {:nil       {:as "nil"}
                   :boolean   {:as (fn [b] (if b "true" "false"))}
                   :string    {:quote :double}
                    :symbol    {:custom #'ruby-symbol
                                :global #'ruby-symbol-global
                                :replace (assoc helper/+sym-replace+ \? "?")}}
        :data    {:vector    {:start "[" :end "]" :space ""}
                  :map       {:custom #'ruby-map}}
        :function {:defn      {:raw "def"
                               :body      {:start "" :end "end"}}}
        :define   {:def       {:raw "def"}
                   :defglobal {:raw "def"}}}
       (collection/merge-nested (emit/default-grammar))))


(comment
  :try     {:raw "BEGIN"
            :wrap    {:start "" :end "END;"}
            :body    {:start "" :end "EXCEPTION"}
            :control {:default {:parameter  {:start "" :end ""}
                                :body {:append true
                                       :start "" :end ""}}
                      :catch   {:raw "WHEN"
                                :parameter  {:start " " :end " THEN"}}}}

  :default {:comment   {:prefix "--"}
            :common    {:apply ":" :statement ""
                        :namespace-full "___"
                        :namespace-sep  "_"}
            :index     {:offset 1  :end-inclusive true}
            :return    {:multi true}
            :block     {:parameter {:start " " :end " "}
                        :body      {:start "" :end ""}}
            :function  {:raw "function"
                        :body      {:start "" :end "end"}}
            :infix     {:if  {:check "and" :then "or"}}
            :global    {:reference nil}})

(def +grammar+
  (grammar/grammar :rb
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :ruby
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
