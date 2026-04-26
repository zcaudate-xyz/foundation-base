(ns std.lang.model.spec-python.rewrite
  (:require [std.lang.rewrite.hoist :as hoist]
            [std.lib.collection :as collection]))

(def ^:private +python-lambda-disallowed-emits+
  #{:abstract
    :assign
    :comment
    :def-assign
    :discard
    :do
    :do*
    :return
    :with-decorate
    :with-deref
    :with-eval
    :with-global
    :with-lang
    :with-module
    :with-rand
    :with-uuid})

(def ^:private +python-lambda-disallowed-types+
  #{:block
    :def
    :free})

(def ^:private +python-lambda-disallowed-ops+
  #{:throw})

(declare python-lambda-compatible?)

(defn- python-lambda-expr?
  [form grammar]
  (cond (or (nil? form)
            (symbol? form)
            (string? form)
            (keyword? form)
            (number? form)
            (instance? Boolean form))
        true

        (vector? form)
        (every? #(python-lambda-expr? % grammar) form)

        (set? form)
        (every? #(python-lambda-expr? % grammar) form)

        (map? form)
        (every? (fn [[k v]]
                  (and (python-lambda-expr? k grammar)
                       (python-lambda-expr? v grammar)))
                form)

        (and (collection/form? form)
             (= 'quote (first form)))
        true

        (and (collection/form? form)
             (= 'return (first form)))
        (let [[_ value & more] form]
          (and (empty? more)
               (python-lambda-expr? value grammar)))

        (and (collection/form? form)
             (= 'fn (first form)))
        (python-lambda-compatible? form grammar)

        (collection/form? form)
        (let [entry (get-in grammar [:reserved (first form)])]
          (and (not (contains? +python-lambda-disallowed-ops+ (:op entry)))
               (not (contains? +python-lambda-disallowed-types+ (:type entry)))
               (not (contains? +python-lambda-disallowed-emits+ (:emit entry)))
               (every? #(python-lambda-expr? % grammar) (rest form))))

        :else
        true))

(defn- python-lambda-compatible?
  [form grammar]
  (let [[_ head & tail] form
        [name body]      (if (symbol? head)
                           [head (rest tail)]
                           [nil tail])]
    (and (nil? name)
         (or (empty? body)
             (and (= 1 (count body))
                  (python-lambda-expr? (first body) grammar))))))

(def ^:private +python-rewriter+
  (hoist/create-rewriter
   {:fn-tags #{'fn 'fn.inner}
    :symbol-prefix "py_callback__"
    :lambda-compatible? python-lambda-compatible?}))

(defn- with-form-meta
  [source out]
  (if (instance? clojure.lang.IObj out)
    (with-meta out (meta source))
    out))

(defn- python-do-expression?
  [form]
  (and (collection/form? form)
       (#{'do 'do*} (first form))))

(declare python-rewrite-inline-do)

(defn- python-rewrite-inline-do-list
  [form]
  (let [rewritten (with-form-meta form
                    (apply list (map python-rewrite-inline-do form)))]
    (if (and (= 'return (first rewritten))
             (= 2 (count rewritten))
             (python-do-expression? (second rewritten)))
      (let [expr (second rewritten)
            body (rest expr)]
        (with-form-meta
          form
          (apply list 'do*
                 (concat (butlast body)
                         [(with-form-meta
                            rewritten
                            (list 'return (last body)))]))))
      rewritten)))

(defn- python-rewrite-inline-do
  [form]
  (cond
    (and (collection/form? form)
         (= 'quote (first form)))
    form

    (collection/form? form)
    (python-rewrite-inline-do-list form)

    (vector? form)
    (with-form-meta form (vec (map python-rewrite-inline-do form)))

    (set? form)
    (with-form-meta form (set (map python-rewrite-inline-do form)))

    (map? form)
    (with-form-meta
      form
      (into (empty form)
            (map (fn [[k v]]
                   [(python-rewrite-inline-do k)
                    (python-rewrite-inline-do v)]))
            form))

    :else
    form))

(def python-rewrite-expression
  (:rewrite-expression +python-rewriter+))

(def python-rewrite-statement
  (:rewrite-statement +python-rewriter+))

(def python-rewrite-statements
  (:rewrite-statements +python-rewriter+))

(defn python-rewrite-stage
  [form opts]
  (-> ((:rewrite-stage +python-rewriter+) form opts)
      (python-rewrite-inline-do)))
