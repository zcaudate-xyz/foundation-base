(ns std.lang.model-annex.spec-ruby
  (:require [clojure.set :as set]
             [clojure.string]
             [std.lang.base.book :as book]
             [std.lang.base.emit :as emit]
             [std.lang.base.emit-common :as common]
             [std.lang.base.emit-data :as data]
             [std.lang.base.emit-helper :as helper]
             [std.lang.base.emit-preprocess :as preprocess] [std.lang.base.preprocess-base :as preprocess-base]
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
                       (str (if (keyword? k)
                              (str "\"" (name k) "\"")
                              (common/*emit-fn* k grammar mopts))
                             " => "
                             (common/*emit-fn* v grammar mopts)))
      m)]
    (str "{" (clojure.string/join ", " entries) "}")))

(declare rewrite-callable-form)

(defn- callable-var-binding
  [form]
  (when (and (seq? form)
             (= 'var (first form))
             (symbol? (second form))
             (seq? (last form))
             (= 'fn (first (last form))))
    (second form)))

(defn- collect-callable-vars
  [forms]
  (letfn [(walk [form]
            (cond
              (seq? form)
              (let [head (first form)]
                (cond
                  (#{'fn 'fn.inner} head)
                  #{}

                  :else
                  (reduce set/union
                          (cond-> #{}
                            (callable-var-binding form)
                            (conj (callable-var-binding form)))
                          (map walk form))))

              (vector? form)
              (reduce set/union #{} (map walk form))

              (map? form)
              (reduce set/union #{} (mapcat walk form))

              (set? form)
              (reduce set/union #{} (map walk form))

              :else
              #{}))]
    (reduce set/union #{} (map walk forms))))

(defn rewrite-callable-body
  ([args body]
   (rewrite-callable-body #{} args body))
  ([inherited args body]
   (let [callables (into (into (set inherited)
                               (set (filter symbol? args)))
                         (collect-callable-vars body))]
    (mapv #(rewrite-callable-form % callables) body))))

(defn rewrite-callable-form
  [form callables]
  (cond
    (seq? form)
    (let [head (first form)]
      (cond
        (and (= '. head)
             (= '!:G (second form))
             (vector? (nth form 2 nil))
             (= 1 (count (nth form 2)))
             (symbol? (first (nth form 2)))
             (re-matches #"[A-Z][A-Z0-9_]*"
                         (name (first (nth form 2)))))
        (list '. '!:G [(name (first (nth form 2)))])

        (#{'fn 'fn.inner} head)
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
  ([[_ & args]]
   (let [[args & body] args
          body     (rewrite-callable-body args body)
          grammar  preprocess-base/*macro-grammar*
          mopts    preprocess-base/*macro-opts*
          args-str (clojure.string/join ", "
                                        (map #(common/*emit-fn* % grammar mopts)
                                            args))
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
         :for-object {:macro #'tf-for-object :emit :macro}
         :for-array  {:macro #'tf-for-array  :emit :macro}
         :for-iter   {:macro #'tf-for-iter   :emit :macro}
         :for-index  {:macro #'tf-for-index  :emit :macro}
         :defn       {:symbol #{'defn}   :macro #'ruby-defn :emit :macro}
         :defgen     {:symbol #{'defgen} :macro #'ruby-defn :emit :macro}
         :with-global {:value true :raw "($__globals__ ||= {})"}
          :and        {:raw "&&"}
          :or         {:raw "||"}
          :not        {:raw "!" :emit :prefix}
          :eq         {:raw "=="}
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
                    :invoke    {:start "("}
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
