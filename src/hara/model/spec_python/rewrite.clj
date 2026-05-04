(ns hara.model.spec-python.rewrite
  (:require [hara.lang.rewrite.hoist :as hoist]
  	    [hara.lang.rewrite.inline-do :as inline]
            [hara.lang.rewrite.walk :as walk]
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

(defn- python-lambda-entry
  [form grammar]
  (when (collection/form? form)
    (let [op (first form)]
      (or (get-in grammar [:reserved op])
          (when (and (symbol? op)
                     (namespace op))
            (get-in grammar [:reserved (symbol (name op))]))))))

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
        (let [entry (python-lambda-entry form grammar)]
          (if-let [macro-fn (:macro entry)]
            (let [expanded (macro-fn form)]
              (if (= expanded form)
                (and (not (contains? +python-lambda-disallowed-ops+ (:op entry)))
                     (not (contains? +python-lambda-disallowed-types+ (:type entry)))
                     (not (contains? +python-lambda-disallowed-emits+ (:emit entry)))
                     (every? #(python-lambda-expr? % grammar) (rest form)))
                (python-lambda-expr? expanded grammar)))
            (and (not (contains? +python-lambda-disallowed-ops+ (:op entry)))
                 (not (contains? +python-lambda-disallowed-types+ (:type entry)))
                 (not (contains? +python-lambda-disallowed-emits+ (:emit entry)))
                 (every? #(python-lambda-expr? % grammar) (rest form)))))

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

(def ^:private with-form-meta
  walk/with-form-meta)

(def python-rewrite-expression
  (:rewrite-expression +python-rewriter+))

(def python-rewrite-statement
  (:rewrite-statement +python-rewriter+))

(def python-rewrite-statements
  (:rewrite-statements +python-rewriter+))

(declare python-normalize-form)

(defn- python-handler-form?
  [tag form]
  (and (collection/form? form)
       (= tag (first form))))

(defn- python-value-catch-binding?
  [binding]
  (and (symbol? binding)
       (not (re-find #"^[A-Z]" (name binding)))))

(defn- python-catch-binding
  [binding err-sym]
  (with-form-meta binding
    ['Exception :as err-sym]))

(defn- python-catch-value
  [err-sym]
  (list ':?
        (list 'hasattr err-sym "__xt_value__")
        (list '. err-sym '__xt_value__)
        err-sym))

(defn- python-rewrite-throw
  [form]
  (let [[_ value] form
        value (python-normalize-form value)
        value-sym (gensym "py_throw_value__")
        err-sym (gensym "py_throw_err__")
        native? (list 'isinstance value-sym 'BaseException)]
    (with-form-meta form
      (list 'do
            (list 'var value-sym ':= value)
            (list 'var err-sym ':=
                  (list ':? native?
                        value-sym
                        (list 'Exception value-sym)))
            (list 'when
                  (list 'not native?)
                  (list 'setattr err-sym "__xt_value__" value-sym))
            (list 'throw err-sym)))))

(defn- python-rewrite-handler
  [form]
  (case (first form)
    catch
    (let [[_ binding & body] form]
      (if (python-value-catch-binding? binding)
        (let [err-sym (gensym "py_catch_err__")]
          (with-form-meta form
            (apply list 'catch
                   (python-catch-binding binding err-sym)
                   (concat [(list 'var binding ':=
                                  (python-catch-value err-sym))]
                           (map python-normalize-form body)))))
        (with-form-meta form
          (apply list 'catch
                 binding
                 (map python-normalize-form body)))))

    finally
    (with-form-meta form
      (apply list 'finally
             (map python-normalize-form (rest form))))

    form))

(defn- python-rewrite-try
  [form]
  (let [[_ & items] form
        [body handlers] (split-with (fn [x]
                                      (not (or (python-handler-form? 'catch x)
                                               (python-handler-form? 'finally x))))
                                    items)]
    (with-form-meta form
      (apply list 'try
             (concat (map python-normalize-form body)
                     (map python-rewrite-handler handlers))))))

(defn- python-rewrite-list
  [form]
  (cond
    (not (collection/form? form))
    form

    (= 'quote (first form))
    form

    (= 'throw (first form))
    (python-rewrite-throw form)

    (= 'try (first form))
    (python-rewrite-try form)

    :else
    (with-form-meta form
      (apply list (map python-normalize-form form)))))

(defn python-normalize-form
  [form]
  (walk/rewrite-form form
                     python-rewrite-list
                     python-normalize-form))

(defn python-rewrite-stage
  [form opts]
  (-> ((:rewrite-stage +python-rewriter+) form opts)
      (python-normalize-form)
      (inline/rewrite-inline-do)))
