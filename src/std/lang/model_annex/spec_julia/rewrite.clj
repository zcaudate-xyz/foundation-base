(ns std.lang.model-annex.spec-julia.rewrite
  (:require [std.lang.base.util :as ut]
            [std.lang.rewrite.hoist :as hoist]
            [std.lang.rewrite.lift-named-lambda :as lift]
            [std.lib.collection :as collection]))

(def ^:private +julia-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "julia_callback__"}))

(def ^:private +julia-boolish-ops+
  '#{<
     <=
     ==
     >
     >=
     not=
     isa
     x:eq
     x:neq
     x:lt
     x:lte
     x:gt
     x:gte
     x:nil?
     x:not-nil?
     x:zero?
     x:neg?
     x:pos?
     x:even?
     x:odd?
     x:has-key?
     x:is-array?
     x:is-boolean?
     x:is-function?
     x:is-integer?
     x:is-number?
     x:is-object?
     x:is-string?
     x:iter-eq
     x:iter-has?
     x:iter-native?})

(declare julia-rewrite-expression)
(declare julia-rewrite-statement)
(declare julia-rewrite-statements)
(declare julia-rewrite-conditional-expression)

(defn- with-form-meta
  [source out]
  (lift/with-form-meta source out))

(defn- boolish-form?
  [form]
  (cond
    (instance? Boolean form)
    true

    (and (collection/form? form)
         (= 'not (first form)))
    (boolish-form? (second form))

    (and (collection/form? form)
         (#{'and 'or} (first form)))
    (every? boolish-form? (rest form))

    (and (collection/form? form)
         (contains? +julia-boolish-ops+ (first form)))
    true

    :else
    false))

(defn- truthy-check-form
  [value]
  (list 'and
        (list 'x:not-nil? value)
        (list 'not= false value)))

(defn- truthy-form
  [source form]
  (if (boolish-form? form)
    form
    (with-form-meta
      source
      (truthy-check-form form))))

(defn- truthy-or-form
  [source value fallback]
  (with-form-meta
    source
    (list :?
          (truthy-check-form value)
          value
          fallback)))

(defn- destructure-target?
  [form]
  (and (set? form)
       (seq form)
       (every? symbol? form)))

(defn- destructure-symbols
  [target]
  (sort-by ut/sym-default-str target))

(defn- destructure-value
  [temp sym]
  (list 'x:get-key temp (ut/sym-default-str sym) nil))

(defn- rewrite-expression-coll
  [items grammar]
  (map #(julia-rewrite-expression % grammar) items))

(defn- rewrite-expression-map
  [form grammar]
  (into (empty form)
        (map (fn [[k v]]
               [(julia-rewrite-expression k grammar)
                (julia-rewrite-expression v grammar)]))
        form))

(defn- rewrite-fn
  [form grammar]
  (let [[name args body] (lift/fn-parts form)]
    (with-form-meta
      form
      (apply list 'fn
             (concat (when name [name])
                     [args]
                     (-> body
                         (julia-rewrite-statements grammar)
                         lift/splice-do*
                         lift/wrap-body))))))

(defn- rewrite-binding-vector
  [binding grammar]
  (if (and (vector? binding)
           (<= 2 (count binding)))
    (let [[lhs rhs & more] binding]
      (with-form-meta
        binding
        (vec (concat [lhs (julia-rewrite-expression rhs grammar)]
                     more))))
    binding))

(defn- rewrite-for-statement
  [form grammar]
  (let [[tag binding & body] form]
    (with-form-meta
      form
      (apply list tag
             (concat [(rewrite-binding-vector binding grammar)]
                     (julia-rewrite-statements body grammar))))))

(defn- rewrite-cond-statement
  [form grammar]
  (with-form-meta
    form
    (apply list 'cond
           (mapcat (fn [[test body]]
                     (if (= :else test)
                       [test
                        (julia-rewrite-statement body grammar)]
                       [(julia-rewrite-conditional-expression test grammar)
                        (julia-rewrite-statement body grammar)]))
                   (partition 2 (rest form))))))

(defn- rewrite-branch-control
  [form grammar]
  (let [[tag & args] form]
    (with-form-meta
      form
      (case tag
        else
        (apply list tag
               (julia-rewrite-statements args grammar))

        (let [[test & body] args]
          (apply list tag
                 (concat [(julia-rewrite-conditional-expression test grammar)]
                         (julia-rewrite-statements body grammar))))))))

(defn- rewrite-branch-statement
  [form grammar]
  (with-form-meta
    form
    (apply list 'br*
           (map #(rewrite-branch-control % grammar) (rest form)))))

(defn- rewrite-or-expression
  [form grammar]
  (let [args* (vec (rewrite-expression-coll (rest form) grammar))]
    (cond
      (empty? args*)
      nil

      (= 1 (count args*))
      (first args*)

      (every? boolish-form? args*)
      (with-form-meta
        form
        (apply list 'or args*))

      :else
      (reduce (fn [fallback value]
                (truthy-or-form form value fallback))
              (peek args*)
              (reverse (pop args*))))))

(defn- rewrite-ternary-expression
  [form grammar]
  (let [[_ test then else] form
        test*             (julia-rewrite-conditional-expression test grammar)
        then*             (julia-rewrite-expression then grammar)
        else*             (julia-rewrite-expression else grammar)]
    (with-form-meta
      form
      (list :?
            test*
            then*
            else*))))

(defn- unpack-form?
  [form]
  (and (collection/form? form)
       (= 'x:unpack (first form))
       (= 2 (count form))))

(defn- rewrite-invoke-expression
  [form grammar]
  (let [head    (first form)
        head*   (if (collection/form? head)
                  (julia-rewrite-expression head grammar)
                  head)
        args*   (map (fn [arg]
                       (if (unpack-form? arg)
                         (list '... (julia-rewrite-expression (second arg) grammar))
                         (julia-rewrite-expression arg grammar)))
                     (rest form))]
    (with-form-meta
      form
      (apply list head* args*))))

(defn- rewrite-expression-list
  [form grammar]
  (case (first form)
    quote
    form

    (do do*)
    (with-form-meta
      form
      (list (rewrite-fn (apply list 'fn '[] (rest form)) grammar)))

    fn
    (rewrite-fn form grammar)

    or
    (rewrite-or-expression form grammar)

    :?
    (rewrite-ternary-expression form grammar)

    (rewrite-invoke-expression form grammar)))

(defn- rewrite-conditional-expression-list
  [form grammar]
  (case (first form)
    quote
    form

    fn
    (rewrite-fn form grammar)

    or
    (with-form-meta
      form
      (apply list 'or
             (map #(julia-rewrite-conditional-expression % grammar) (rest form))))

    and
    (with-form-meta
      form
      (apply list 'and
             (map #(julia-rewrite-conditional-expression % grammar) (rest form))))

    not
    (with-form-meta
      form
      (list 'not
            (julia-rewrite-conditional-expression (second form) grammar)))

    :?
    (let [[_ test then else] form]
      (with-form-meta
        form
        (list :?
              (julia-rewrite-conditional-expression test grammar)
              (julia-rewrite-expression then grammar)
              (julia-rewrite-expression else grammar))))

    (let [head  (first form)
          head* (if (collection/form? head)
                  (julia-rewrite-expression head grammar)
                  head)]
      (with-form-meta
        form
        (apply list head*
               (map #(julia-rewrite-expression % grammar) (rest form)))))))

(defn julia-rewrite-conditional-expression
  [form grammar]
  (let [form* (cond
                (collection/form? form)
                (rewrite-conditional-expression-list form grammar)

                (vector? form)
                (with-form-meta form (vec (map #(julia-rewrite-expression % grammar) form)))

                (set? form)
                (with-form-meta form (set (map #(julia-rewrite-expression % grammar) form)))

                (map? form)
                (with-form-meta form (rewrite-expression-map form grammar))

                :else
                form)]
    (truthy-form form form*)))

(defn julia-rewrite-expression
  [form grammar]
  (cond
    (collection/form? form)
    (rewrite-expression-list form grammar)

    (vector? form)
    (with-form-meta form (vec (rewrite-expression-coll form grammar)))

    (set? form)
    (with-form-meta form (set (rewrite-expression-coll form grammar)))

    (map? form)
    (with-form-meta form (rewrite-expression-map form grammar))

    :else
    form))

(defn- rewrite-do-statement
  [form grammar]
  (let [[tag & body] form
        body (if (= 'do* (first body))
               (rest body)
               body)]
    (with-form-meta
      form
      (apply list tag
             (-> body
                 (julia-rewrite-statements grammar)
                 lift/splice-do*)))))

(defn- rewrite-destructuring-var
  [form grammar]
  (let [[tag target & args] form
        bound   (last args)
        leading (butlast args)
        temp    (gensym "julia_destructure__")
        bound*  (julia-rewrite-expression bound grammar)]
    (with-form-meta
      form
      (apply list 'do*
             (concat [(apply list tag temp (concat leading [bound*]))]
                     (map (fn [sym]
                            (apply list tag sym
                                   (concat leading
                                           [(destructure-value temp sym)])))
                          (destructure-symbols target)))))))

(defn- rewrite-var-statement
  [form grammar]
  (let [[tag target & args] form]
    (cond
      (empty? args)
      form

      (destructure-target? target)
      (rewrite-destructuring-var form grammar)

      :else
      (let [bound   (last args)
            leading (butlast args)]
        (with-form-meta
          form
          (apply list tag target
                 (concat leading
                         [(julia-rewrite-expression bound grammar)])))))))

(defn- rewrite-return-statement
  [form grammar]
  (let [[tag & args] form]
    (with-form-meta
      form
      (apply list tag
             (map #(julia-rewrite-expression % grammar) args)))))

(defn- rewrite-if-statement
  [form grammar]
  (let [[tag test then & [else]] form]
    (with-form-meta
      form
      (apply list tag
             (cond-> [(julia-rewrite-conditional-expression test grammar)
                      (julia-rewrite-statement then grammar)]
               else (conj (julia-rewrite-statement else grammar)))))))

(defn- rewrite-when-statement
  [form grammar]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (julia-rewrite-conditional-expression test grammar)
             (julia-rewrite-statements body grammar)))))

(defn- rewrite-while-statement
  [form grammar]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (julia-rewrite-conditional-expression test grammar)
             (julia-rewrite-statements body grammar)))))

(defn- rewrite-defn-statement
  [form grammar]
  (let [[tag name args & body] form]
    (with-form-meta
      form
      (apply list tag name args
             (-> body
                 (julia-rewrite-statements grammar)
                 lift/splice-do*
                 lift/wrap-body)))))

(defn julia-rewrite-statement
  [form grammar]
  (cond
    (not (collection/form? form))
    (julia-rewrite-expression form grammar)

    :else
    (case (first form)
      (do do*)      (rewrite-do-statement form grammar)
      (var var* :=) (rewrite-var-statement form grammar)
      cond          (rewrite-cond-statement form grammar)
      br*           (rewrite-branch-statement form grammar)
      (for:index for:object for:array for:iter)
      (rewrite-for-statement form grammar)
      return        (rewrite-return-statement form grammar)
      if            (rewrite-if-statement form grammar)
      when          (rewrite-when-statement form grammar)
      while         (rewrite-while-statement form grammar)
      (defn defn- defgen)
      (rewrite-defn-statement form grammar)
      fn
      (rewrite-fn form grammar)
      (julia-rewrite-expression form grammar))))

(defn julia-rewrite-statements
  [forms grammar]
  (map #(julia-rewrite-statement % grammar) forms))

(defn julia-rewrite-stage
  [form {:keys [grammar] :as opts}]
  (let [form ((:rewrite-stage +julia-rewriter+) form opts)]
    (cond
      (collection/form? form)
      (julia-rewrite-statement form grammar)

      (vector? form)
      (with-form-meta form (mapv #(julia-rewrite-statement % grammar) form))

      :else
      form)))
