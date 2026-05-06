(ns hara.model.spec-ruby.rewrite
  (:require [clojure.set :as set]
             [clojure.string :as string]
             [clojure.walk :as walk]
             [hara.common.util :as ut]
             [hara.lang.rewrite.common :as common]
             [hara.lang.rewrite.destructure :as destruct]))

(defn- callable-form?
  [form]
  (and (seq? form)
       (#{'fn 'fn.inner} (first form))))

(declare vector-destructure-target?)
(declare rewrite-callable-form)

(defn- callable-var-bindings
  [form]
  (when (and (seq? form)
             (= 'var (first form)))
    (let [target (second form)]
      (cond
        (symbol? target)
        #{target}

        (destruct/destructure-target? target)
        (into #{} (destruct/destructure-symbols target ut/sym-default-str))

        (vector-destructure-target? target)
        (into #{} (remove #{'_} target))

        :else
        nil))))

(defn- vector-destructure-target?
  [target]
  (and (vector? target)
       (seq target)
       (every? symbol? target)))

(defn- invalid-ruby-local-symbol?
  [sym]
  (and (symbol? sym)
       (nil? (namespace sym))
       (re-find #"[?!]" (name sym))))

(defn- ruby-local-symbol
  [sym]
  (if-not (invalid-ruby-local-symbol? sym)
    sym
    (symbol (-> (name sym)
                (string/replace "-" "_")
                (string/replace "?" "_p")
                (string/replace "!" "_bang")))))

(defn- destructure-target?
  [target]
  (or (destruct/destructure-target? target)
      (vector-destructure-target? target)))

(defn- vector-destructure-bindings
  [target temp]
  (keep-indexed (fn [idx sym]
                  (when (not= '_ sym)
                    [sym (list 'x:get-idx temp (list 'x:offset idx) nil)]))
                target))

(defn- destructure-bindings
  [target temp]
  (cond
    (destruct/destructure-target? target)
    (destruct/destructure-bindings target temp
                                   (fn [sym]
                                     (-> (name sym)
                                         (string/replace "-" "_"))))

    (vector-destructure-target? target)
    (vector-destructure-bindings target temp)

     :else
     []))

(declare normalize-ruby-local-body)

(defn- rename-destructure-target
  [target]
  (let [symbols (distinct (destruct/destructure-symbols target ut/sym-default-str))
        renames (into {}
                      (keep (fn [sym]
                              (let [sym* (ruby-local-symbol sym)]
                                (when (not= sym sym*)
                                  [sym sym*]))))
                      symbols)]
    [(walk/postwalk (fn [x]
                      (if (and (symbol? x)
                               (contains? renames x))
                        (get renames x)
                        x))
                    target)
     renames]))

(defn- rename-binding-target
  [target]
  (cond
    (symbol? target)
    (let [target* (ruby-local-symbol target)]
      [target* (cond-> {} (not= target target*) (assoc target target*))])

    (vector-destructure-target? target)
    [(mapv ruby-local-symbol target)
     (into {}
           (keep (fn [sym]
                   (when (not= '_ sym)
                     (let [sym* (ruby-local-symbol sym)]
                       (when (not= sym sym*)
                         [sym sym*])))))
           target)]

    (destruct/destructure-target? target)
    (rename-destructure-target target)

    :else
    [target {}]))

(declare normalize-ruby-local-form)

(defn- normalize-callable-args
  [args]
  (reduce (fn [[out env] arg]
            (if (symbol? arg)
              (let [arg* (ruby-local-symbol arg)]
                [(conj out arg*)
                 (cond-> env
                   (not= arg arg*) (assoc arg arg*))])
              [(conj out arg) env]))
          [[] {}]
          args))

(defn- normalize-ruby-local-form*
  [form env]
  (cond
    (symbol? form)
    [(get env form form) env]

    (seq? form)
    (let [head (first form)]
      (cond
        (callable-form? form)
        (let [[tag & more] form
              [name args body] (if (symbol? (first more))
                                 [(first more) (second more) (drop 2 more)]
                                 [nil (first more) (rest more)])
              name* (when name (ruby-local-symbol name))
              [args* arg-env] (normalize-callable-args args)
              env* (cond-> (merge env arg-env)
                     (and name (not= name name*)) (assoc name name*))
              body* (normalize-ruby-local-body body env*)]
          [(apply list tag
                  (concat (when name [name*])
                          [args*]
                          body*))
           env])

        (#{'defn 'defgen} head)
        (let [[tag name args & body] form
              [args* arg-env] (normalize-callable-args args)
              body* (normalize-ruby-local-body body (merge env arg-env))]
          [(apply list tag name args* body*) env])

        (= 'var head)
        (let [[target renames] (rename-binding-target (second form))
              args (drop 2 form)
              args* (map #(normalize-ruby-local-form % env) args)]
          [(apply list 'var target args*)
           (merge env renames)])

        (#{'do 'do*} head)
        [(apply list head (normalize-ruby-local-body (rest form) env))
         env]

        :else
        [(apply list
                (map #(normalize-ruby-local-form % env) form))
         env]))

    (vector? form)
    [(mapv #(normalize-ruby-local-form % env) form) env]

    (map? form)
    [(into {} (map (fn [[k v]]
                     [(normalize-ruby-local-form k env)
                      (normalize-ruby-local-form v env)]))
           form)
     env]

    (set? form)
    [(set (map #(normalize-ruby-local-form % env) form)) env]

    :else
    [form env]))

(defn- normalize-ruby-local-form
  [form env]
  (first (normalize-ruby-local-form* form env)))

(defn- normalize-ruby-local-body
  [forms env]
  (loop [remaining (seq forms)
         env env
         out []]
    (if-not remaining
      out
      (let [[form* env*] (normalize-ruby-local-form* (first remaining) env)]
        (recur (next remaining) env* (conj out form*))))))

(defn- normalize-ruby-local-forms
  [forms]
  (normalize-ruby-local-body forms {}))

(defn- collect-callable-vars
  [form]
  (cond
    (callable-form? form)
    #{}

    (seq? form)
    (let [bindings (callable-var-bindings form)]
      (cond-> (reduce set/union #{} (map collect-callable-vars form))
        bindings (set/union bindings)))

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

(declare rewrite-callable-value)

(defn- destructuring-var-form?
  [form]
  (and (seq? form)
       (= 'var (first form))
       (destructure-target? (second form))
       (seq (drop 2 form))))

(defn- expand-destructuring-var
  [form bound]
  (let [[tag target & args] form
        leading (butlast args)
        temp    (gensym "ruby_destructure__")]
    (common/with-form-meta
      form
      (apply list 'do*
             (concat [(apply list tag temp (concat leading [bound]))]
                     (map (fn [[sym value]]
                            (apply list tag sym (concat leading [value])))
                          (destructure-bindings target temp)))))))

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

(defn- ruby-constant-symbol?
  [form]
  (and (symbol? form)
       (namespace form)
        (re-matches #"[A-Z][A-Za-z0-9_]*" (name form))))

(defn- ruby-method-ref-form?
  [form]
  (and (seq? form)
       (= 'ruby-method-ref (first form))))

(defn rewrite-callable-form
  [form callables]
  (cond
    (ruby-method-ref-form? form)
    form

    (ruby-global-const-access? form)
    (list '. '!:G [(name (first (nth form 2)))])

    (destructuring-var-form? form)
    (rewrite-callable-form
     (expand-destructuring-var form
                               (rewrite-callable-form (last form) callables))
     callables)

    (seq? form)
    (let [head (first form)]
      (cond
        (callable-form? form)
        (let [[tag & more] form
              [name args body] (if (symbol? (first more))
                                 [(first more) (second more) (drop 2 more)]
                                 [nil (first more) (rest more)])]
          (apply list tag
                 (concat (when name [name])
                         [args]
                         (rewrite-callable-body callables args body))))

        (and (symbol? head)
             (contains? callables head))
        (list '. head
              (apply list 'call
                     (map #(rewrite-callable-value % callables)
                          (rest form))))

        :else
        (let [head* (if (seq? head)
                      (rewrite-callable-form head callables)
                      head)]
          (apply list head*
                 (map #(rewrite-callable-value % callables)
                      (rest form))))))

    (vector? form)
    (mapv #(rewrite-callable-value % callables) form)

    (map? form)
    (into {} (map (fn [[k v]]
                    [(rewrite-callable-value k callables)
                     (rewrite-callable-value v callables)]))
          form)

    (set? form)
    (set (map #(rewrite-callable-value % callables) form))

    :else
    form))

(defn rewrite-callable-value
  [form callables]
  (if (and (symbol? form)
           (namespace form)
           (not (ruby-constant-symbol? form)))
    (list 'ruby-method-ref form)
    (rewrite-callable-form form callables)))

(defn- iterator-symbol
  [sym]
  (gensym (str (name (or sym 'ruby_generator))
               "__iter__")))

(declare rewrite-generator-form)

(defn ruby-rewrite-generator-body
  [args body iterator]
  (let [callables (into #{}
                        (concat (filter symbol? args)
                                (collect-callable-vars body)))]
    (mapv #(rewrite-generator-form % iterator callables) body)))

(declare rewrite-generator-value)

(defn- rewrite-generator-form
  [form iterator callables]
  (cond
    (ruby-method-ref-form? form)
    form

    (ruby-global-const-access? form)
    (list '. '!:G [(name (first (nth form 2)))])

    (destructuring-var-form? form)
    (rewrite-generator-form
     (expand-destructuring-var form
                               (rewrite-generator-form (last form) iterator callables))
     iterator
     callables)

    (seq? form)
    (let [head (first form)]
      (cond
        (callable-form? form)
        (let [[tag & more] form
              [name args body] (if (symbol? (first more))
                                 [(first more) (second more) (drop 2 more)]
                                 [nil (first more) (rest more)])]
          (apply list tag
                 (concat (when name [name])
                         [args]
                         (rewrite-callable-body callables args body))))

        (= 'yield head)
        (list '. iterator
              (list '<< (rewrite-generator-value (second form) iterator callables)))

        (and (symbol? head)
             (contains? callables head))
        (list '. head
               (apply list 'call
                      (map #(rewrite-generator-value % iterator callables)
                           (rest form))))

        :else
        (let [head* (if (seq? head)
                      (rewrite-generator-form head iterator callables)
                      head)]
          (apply list head*
                 (map #(rewrite-generator-value % iterator callables)
                       (rest form))))))

    (vector? form)
    (mapv #(rewrite-generator-value % iterator callables) form)

    (map? form)
    (into {} (map (fn [[k v]]
                    [(rewrite-generator-value k iterator callables)
                     (rewrite-generator-value v iterator callables)]))
          form)

    (set? form)
    (set (map #(rewrite-generator-value % iterator callables) form))

    :else
    form))

(defn- rewrite-generator-value
  [form iterator callables]
  (if (and (symbol? form)
           (namespace form)
           (not (ruby-constant-symbol? form)))
    (list 'ruby-method-ref form)
    (rewrite-generator-form form iterator callables)))

(defn- rewrite-defgen
  [form]
  (let [[_ sym args & body] form
        iterator (iterator-symbol sym)]
    (common/with-form-meta
      form
      (list 'defn sym args
            (list 'return
                  (list 'x:iter-generator
                        (apply list 'fn [iterator]
                               (ruby-rewrite-generator-body args body iterator))))))))

(defn- rewrite-runtime-form
  [form callables]
  (if (and (seq? form)
           (= 'defgen (first form)))
    (rewrite-defgen form)
    (rewrite-callable-form form callables)))

(defn rewrite-callable-forms
  [forms]
  (let [forms     (normalize-ruby-local-forms forms)
        callables (collect-callable-vars forms)]
    (mapv #(rewrite-runtime-form % callables) forms)))

(defn- mark-inline-def
  [form]
  (if (and (seq? form)
           (= 'defn (first form))
           (symbol? (second form)))
    (apply list 'defn
           (with-meta (second form)
             (assoc (meta (second form)) :inner true))
           (drop 2 form))
    form))

(defn mark-inline-defs
  [forms]
  (mapv mark-inline-def forms))

(defn- runtime-eval?
  [{:keys [mopts]}]
  (boolean (get-in mopts [:emit :body :transform])))

(defn- rewrite-runtime-forms
  [forms opts]
  (cond-> (rewrite-callable-forms forms)
    (runtime-eval? opts) mark-inline-defs))

(defn ruby-rewrite-stage
  [form opts]
  (cond
    (vector? form)
    (common/with-form-meta form (rewrite-runtime-forms form opts))

    (and (seq? form)
         (#{'do 'do*} (first form)))
    (common/with-form-meta form
      (apply list (first form)
             (rewrite-runtime-forms (vec (rest form)) opts)))

    :else
    (common/with-form-meta form
      (first (rewrite-runtime-forms [form] opts)))))
