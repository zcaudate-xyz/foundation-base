(ns std.lang.model-annex.spec-ruby
  (:require [clojure.string]
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
             [std.lang.model-annex.spec-ruby.rewrite :as rewrite]
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
                       (str (common/*emit-fn* (if (keyword? k)
                                                (name k)
                                                k)
                                              grammar mopts)
                              " => "
                              (common/*emit-fn* v grammar mopts)))
                       m)]
    (str "{" (clojure.string/join ", " entries) "}")))

(defn ruby-emit-args
  [args grammar mopts]
  (str "("
       (clojure.string/join ", "
                            (common/emit-array args grammar mopts))
       ")"))

(defn- ruby-callable-form?
  [form]
  (cond
    (and (seq? form)
         (#{'fn 'fn.inner} (first form)))
    true

    (and (seq? form)
         (= 'quote (first form))
         (= 2 (count form)))
    (ruby-callable-form? (second form))

    (and (seq? form)
         (= 1 (count form)))
    (ruby-callable-form? (first form))

    :else
    false))

(defn ruby-invoke
  [[f & args] grammar mopts]
  (let [target (common/emit-wrapping f grammar mopts)]
    (str target
         (when (ruby-callable-form? f)
           ".call")
         (ruby-emit-args args grammar mopts))))

(defn- ruby-zero-arg-call?
  [prop]
  (and (seq? prop)
       (symbol? (first prop))
       (not= 'call (first prop))
       (empty? (rest prop))))

(defn- ruby-dot-entry
  [prop grammar mopts]
  (cond
    (ruby-zero-arg-call? prop)
    (str "." (common/emit-symbol (first prop) grammar mopts))

    (collection/form? prop)
    (let [sym    (first prop)
          sym    (if (string? sym) (symbol sym) sym)
          _      (assert (symbol? sym))
          braces (meta sym)]
      (str "."
           (common/emit-symbol sym grammar mopts)
           (if (not-empty braces)
             (common/*emit-fn* braces grammar mopts)
             "")
           (ruby-emit-args (rest prop) grammar mopts)))

    :else
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

(defn ruby-defn-
  [form grammar mopts]
  (top/emit-top-level :defn form grammar mopts))

(defn ruby-defn
  [[_ sym args & body]]
  (list* 'defn- sym args (rewrite/rewrite-callable-body args body)))

(defn ruby-fn
  "basic transform for ruby blocks
   (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
   => '(fn.inner [a] (+ a 1))"
  {:added "4.1"}
  ([[_ args & body]]
   (let [body     (rewrite/rewrite-callable-body args body)
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
           :throw      {:raw "raise" :emit :prefix}
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
                    :invoke    {:custom #'ruby-invoke}
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
                               :control {:catch   {:raw  "rescue Exception =>"
                                                   :body {:start "" :end ""}}
                                         :finally {:raw "ensure"
                                                   :body {:start "" :end ""}}}}}
         :token   {:nil       {:as "nil"}
                   :boolean   {:as (fn [b] (if b "true" "false"))}
                   :string    {:quote :double}
                    :symbol    {:custom #'ruby-symbol
                                :global #'ruby-symbol-global
                                :replace (assoc helper/+sym-replace+ \? "?")}}
         :data    {:vector    {:start "[" :end "]" :space ""}
                   :map       {:custom #'ruby-map}}
         :rewrite {:staging [#'rewrite/ruby-rewrite-stage]}
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
