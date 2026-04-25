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
          (and (not (contains? +python-lambda-disallowed-types+ (:type entry)))
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

(def python-rewrite-expression
  (:rewrite-expression +python-rewriter+))

(def python-rewrite-statement
  (:rewrite-statement +python-rewriter+))

(def python-rewrite-statements
  (:rewrite-statements +python-rewriter+))

(def python-rewrite-stage
  (:rewrite-stage +python-rewriter+))
