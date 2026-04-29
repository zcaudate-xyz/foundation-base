(ns std.lang.model-annex.spec-ruby.rewrite
  (:require [clojure.set :as set]
            [std.lang.rewrite.common :as common]))

(defn- callable-form?
  [form]
  (and (seq? form)
       (#{'fn 'fn.inner} (first form))))

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
