(ns hara.model.spec-ruby
  (:require [clojure.string]
            [hara.lang.book :as book]
            [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.preprocess-base :as preprocess-base]
            [hara.common.emit-top-level :as top]
            [hara.common.grammar :as grammar]
            [hara.lang.script :as script]
            [hara.typed.xtalk-analysis :as xtalk-analysis]
            [hara.common.util :as ut]
            [hara.lang.rewrite.destructure :as destruct]
            [hara.model.spec-xtalk]
            [hara.model.spec-ruby.rewrite :as rewrite]
            [hara.model.annex.spec-xtalk.fn-ruby :as fn]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

(defn ruby-symbol
  "emit ruby symbol
   (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
    => \":a\"
    (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
    => \"a\""
  {:added "4.1"}
  [sym grammar mopts]
  (let [reserved #{"BEGIN" "END" "alias" "and" "begin" "break" "case" "class"
                   "def" "defined?" "do" "else" "elsif" "end" "ensure" "false"
                   "for" "if" "in" "module" "next" "nil" "not" "or" "redo"
                   "rescue" "retry" "return" "self" "super" "then" "true"
                   "undef" "unless" "until" "when" "while" "yield"}
        sym-name (when (symbol? sym) (name sym))
        local-name (when sym-name
                     (cond-> (clojure.string/replace sym-name "-" "_")
                       (contains? reserved sym-name) (str "_")))]
    (cond (keyword? sym)
          (str ":" (name sym))

          (and (symbol? sym)
               (nil? (namespace sym))
               (re-matches #"[A-Z][A-Z0-9_]*" sym-name)
               (clojure.string/includes? sym-name "_"))
          (str "\"" sym-name "\"")

          (and (symbol? sym)
               (nil? (namespace sym)))
          local-name

          :else
          (common/emit-symbol sym grammar mopts))))

(defn ruby-destructure-key
  "normalizes xtalk destructuring keys to the Ruby hash key format"
  {:added "4.1"}
  [sym]
  (-> (name sym)
      (clojure.string/replace "-" "_")))

(defn- ruby-qualified-symbol
  [sym]
  (let [{:keys [module]} (preprocess-base/macro-opts)
        module-id (:id module)]
    (cond
      (or (nil? sym)
          (namespace sym)
          (:inner (meta sym))
          (nil? module-id))
      sym

      :else
      (symbol (name module-id) (name sym)))))

(defn- ruby-optional-input?
  [input]
  (= :maybe (get-in input [:type :kind])))

(defn- ruby-apply-optional-defaults
  [sym args]
  (if-not (and sym (vector? args))
    args
    (try
      (let [qualified      (ruby-qualified-symbol sym)
            fn-def         (xtalk-analysis/resolve-function-def qualified)
            inferred-count (when fn-def
                             (count (take-while ruby-optional-input?
                                                (reverse (:inputs fn-def)))))
            optional-count (when (and inferred-count
                                      (pos? inferred-count))
                             inferred-count)]
        (if (and optional-count (pos? optional-count))
          (let [optional-args (take-last optional-count args)]
            (if (not (neg? (collection/index-at #{:=} args)))
              args
              (vec
               (concat (drop-last optional-count args)
                       (mapcat (fn [arg]
                                 [arg := nil])
                               optional-args)))))
          args))
      (catch Throwable _
        args))))

(defn ruby-method-ref
  [[_ sym]]
  (let [grammar preprocess-base/*macro-grammar*
        mopts   preprocess-base/*macro-opts*]
    (list ':-
          (str "method(:"
               (common/emit-symbol sym grammar mopts)
               ")"))))

(defn ruby-symbol-global
  [key _grammar _mopts]
  (let [globals (list ':- "($__globals__ ||= {})")]
    (if (= key '!:G)
      globals
      (list '. globals [(ut/sym-default-str key)]))))

(defn- ruby-vector-destructure-bindings
  [target temp]
  (keep-indexed (fn [idx sym]
                  (when (not= '_ sym)
                    [sym (list 'x:get-idx temp (list 'x:offset idx) nil)]))
                target))

(defn ruby-var
  "emit ruby variable
   (spec-ruby/ruby-var '(var a 1))
     => '(:= a 1)"
  {:added "4.1"}
  [[_ sym & args]]
  (let [bound (last args)]
    (cond
      (destruct/destructure-target? sym)
      (let [temp (gensym "ruby_var__")]
        (apply list 'do*
               (cons (list ':= temp bound)
                     (map (fn [[target value]]
                            (list ':= target value))
                          (destruct/destructure-bindings sym temp ruby-destructure-key)))))

      (and (vector? sym)
           (seq sym)
           (every? symbol? sym))
      (let [temp (gensym "ruby_var__")]
        (apply list 'do*
               (cons (list ':= temp bound)
                     (map (fn [[target value]]
                            (list ':= target value))
                          (ruby-vector-destructure-bindings sym temp)))))

      :else
      (list ':= sym bound))))

(defn ruby-map
  "emit ruby hash
   (l/emit-as :ruby '[{:a 1 :b 2}])
       => \"{\\\"a\\\" => 1, \\\"b\\\" => 2}\""
  {:added "4.1"}
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* (if (keyword? k)
                                                (ut/sym-default-str k)
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

(defn ruby-div
  [[_ a b & more] grammar mopts]
  (let [start (str (common/emit-wrapping a grammar mopts)
                   ".fdiv("
                   (common/*emit-fn* b grammar mopts)
                   ")")]
    (reduce (fn [acc arg]
              (str acc " / " (common/emit-wrapping arg grammar mopts)))
            start
            more)))

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
         (when (or (ruby-callable-form? f)
                   (seq? f))
           ".call")
         (ruby-emit-args args grammar mopts))))

(defn- ruby-zero-arg-call?
  [prop]
  (and (seq? prop)
       (symbol? (first prop))
       (not= 'call (first prop))
       (empty? (rest prop))))

(defn- ruby-method-name
  [sym grammar mopts]
  (if (= '<< sym)
    "<<"
    (common/emit-symbol sym grammar mopts)))

(defn- ruby-dot-entry
  [prop grammar mopts]
  (cond
    (ruby-zero-arg-call? prop)
    (str "." (ruby-method-name (first prop) grammar mopts))

    (collection/form? prop)
    (let [sym    (first prop)
          sym    (if (string? sym) (symbol sym) sym)
          _      (assert (symbol? sym))
          braces (meta sym)]
      (str "."
           (ruby-method-name sym grammar mopts)
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
  (let [[tag name args & more] form]
    (top/emit-top-level
     :defn
     (list* tag name (ruby-apply-optional-defaults name args) more)
     grammar
     mopts)))

(defn ruby-defn
  [[_ sym args & body]]
  (list* 'defn- sym args (rewrite/rewrite-callable-body args body)))

(defn ruby-defgen
  [[_ sym args & body]]
  (let [iterator (gensym "__iter__")]
    (list 'defn- sym args
          (list 'return
                (list 'x:iter-generator
                      (apply list 'fn [iterator]
                             (rewrite/ruby-rewrite-generator-body args body iterator)))))))

(defn ruby-fn
  "basic transform for ruby blocks
   (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
    => '(fn.inner [a] (+ a 1))"
  {:added "4.1"}
  ([[_ & more]]
   (let [[args body] (if (symbol? (first more))
                       [(second more) (drop 2 more)]
                       [(first more) (rest more)])
         body     (rewrite/rewrite-callable-body args body)
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
  (let [arr*  (gensym "arr__")
        bound (if (vector? e) e [e])
        bound (vec (remove #{'_} bound))
        body  (rewrite/rewrite-callable-body bound body)]
    (if (vector? e)
      (let [[i v] e]
        (template/$
         (do (var ~arr* ~arr)
             (var ~i 0)
             (while (< ~i (. ~arr* length))
               (var ~v (. ~arr* [~i]))
               ~@body
               (:= ~i (+ ~i 1))))))
      (let [idx (gensym "idx__")]
        (template/$
         (do (var ~arr* ~arr)
             (var ~idx 0)
             (while (< ~idx (. ~arr* length))
               (var ~e (. ~arr* [~idx]))
               ~@body
               (:= ~idx (+ ~idx 1)))))))))

(defn tf-for-object
  "transform for `for:object`"
  {:added "4.1"}
  [[_ [[k v] m] & body]]
  (let [obj  (gensym "obj__")
        keys (gensym "keys__")
        idx  (gensym "idx__")
        key  (if (= k '_) (gensym "key__") k)
        bound (vec (remove #{'_} [key v]))
        body  (rewrite/rewrite-callable-body bound body)]
    (template/$
     (do (var ~obj (or ~m {}))
         (var ~keys (. ~obj keys))
         (var ~idx 0)
         (while (< ~idx (. ~keys length))
           (var ~key (. ~keys [~idx]))
           ~@(if (not= v '_)
               [(list 'var v (list '. obj [key]))]
               [])
           ~@body
           (:= ~idx (+ ~idx 1)))))))

(defn tf-for-iter
  "transform for `for:iter`"
  {:added "4.1"}
  [[_ [e it] & body]]
  (let [bound (vec (remove #{'_} [e]))
        body  (rewrite/rewrite-callable-body bound body)]
    (apply list 'for [e :in it]
           (or (not-empty body)
               [nil]))))

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
        :defn       {:symbol #{'defn}   :macro #'ruby-defn   :emit :macro}
        :defgen     {:symbol #{'defgen} :macro #'ruby-defgen :emit :macro}
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
        :div        {:emit #'ruby-div}
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
        :ruby-method-ref {:op :ruby-method-ref
                          :symbol #{'ruby-method-ref}
                          :macro #'ruby-method-ref
                          :emit :macro}
        :x-iter-generator {:op :x-iter-generator
                           :symbol #{'x:iter-generator}
                           :macro #'fn/ruby-tf-x-iter-generator
                           :emit :macro}
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
                  :map-entry {:assign " => " :space " " :keyword :string}
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
