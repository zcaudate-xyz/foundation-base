(ns std.lang.model-annex.spec-ruby.rewrite
  (:require [clojure.set :as set]
            [std.lang.base.util :as ut]
            [std.lang.rewrite.common :as common]
            [std.lang.rewrite.destructure :as destruct]))

(defn- callable-form?
  [form]
  (and (seq? form)
       (#{'fn 'fn.inner} (first form))))

(declare rewrite-callable-form)

(defn- callable-var-binding
  [form]
  (when (and (seq? form)
             (= 'var (first form))
             (symbol? (second form)))
     (second form)))

(defn- vector-destructure-target?
  [target]
  (and (vector? target)
       (seq target)
       (every? symbol? target)))

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
    (destruct/destructure-bindings target temp ut/sym-default-str)

    (vector-destructure-target? target)
    (vector-destructure-bindings target temp)

    :else
    []))

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

(defn rewrite-callable-form
  [form callables]
  (cond
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

(defn- iterator-symbol
  [sym]
  (gensym (str (name (or sym 'ruby_generator))
               "__iter__")))

(declare rewrite-generator-form)

(defn- rewrite-generator-body
  [args body iterator]
  (let [callables (into #{}
                        (concat (filter symbol? args)
                                (collect-callable-vars body)))]
    (mapv #(rewrite-generator-form % iterator callables) body)))

(defn- rewrite-generator-form
  [form iterator callables]
  (cond
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
        (let [[tag args & body] form]
          (apply list tag args (rewrite-callable-body callables args body)))

        (= 'yield head)
        (list '. iterator
              (list '<< (rewrite-generator-form (second form) iterator callables)))

        (and (symbol? head)
             (contains? callables head))
        (list '. head
              (apply list 'call
                     (map #(rewrite-generator-form % iterator callables)
                          (rest form))))

        :else
        (apply list (map #(rewrite-generator-form % iterator callables) form))))

    (vector? form)
    (mapv #(rewrite-generator-form % iterator callables) form)

    (map? form)
    (into {} (map (fn [[k v]]
                    [(rewrite-generator-form k iterator callables)
                     (rewrite-generator-form v iterator callables)]))
          form)

    (set? form)
    (set (map #(rewrite-generator-form % iterator callables) form))

    :else
    form))

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
                               (rewrite-generator-body args body iterator))))))))

(defn- rewrite-runtime-form
  [form callables]
  (if (and (seq? form)
           (= 'defgen (first form)))
    (rewrite-defgen form)
    (rewrite-callable-form form callables)))

(defn rewrite-callable-forms
  [forms]
  (let [callables (collect-callable-vars forms)]
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
  [forms]
  (-> forms
      rewrite-callable-forms
      mark-inline-defs))

(defn ruby-rewrite-stage
  [form opts]
  (if-not (runtime-eval? opts)
    form
    (cond
      (vector? form)
      (common/with-form-meta form (rewrite-runtime-forms form))

      (and (seq? form)
           (#{'do 'do*} (first form)))
      (common/with-form-meta form
        (apply list (first form)
               (rewrite-runtime-forms (vec (rest form)))))

      :else
      (common/with-form-meta form
        (first (rewrite-runtime-forms [form]))))))
