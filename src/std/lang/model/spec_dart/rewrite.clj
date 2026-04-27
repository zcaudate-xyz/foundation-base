(ns std.lang.model.spec-dart.rewrite
  (:require [std.lang.rewrite.hoist :as hoist]
            [std.lang.rewrite.lift-named-lambda :as lift]
            [std.lib.collection :as collection]))

(def ^:private +dart-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "dart_callback__"}))

(def ^:private +dart-boolish-ops+
  '#{and
     not
     or
     <
     <=
     ==
     >
     >=
     not=
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

(def ^:private +dart-statement-heads+
  '#{do
     do*
     var
     var*
     :=
     return
     if
     when
     while
     try
     for
     throw
     break
     continue
     defn
     defn-
     defgen
     fn})

(declare dart-rewrite-expression)
(declare dart-rewrite-statement)
(declare dart-rewrite-statements)
(declare dart-rewrite-conditional-expression)
(declare rewrite-for-async-form)

(defn- with-form-meta
  [source out]
  (lift/with-form-meta source out))

(defn- boolish-form?
  [form]
  (cond
    (instance? Boolean form)
    true

    (and (collection/form? form)
         (= '. (first form))
         (collection/form? (nth form 2 nil))
         (contains? '#{contains
                       containsKey
                       endsWith
                       moveNext
                       startsWith}
                    (first (nth form 2))))
    true

    (and (collection/form? form)
         (contains? +dart-boolish-ops+ (first form)))
    true

     :else
     false))

(defn- truthy-form
  [source form]
  (if (boolish-form? form)
    form
    (with-form-meta
      source
      (list 'and
            (list 'x:not-nil? form)
            (list 'not= false form)))))

(defn- unpack-form?
  [form]
  (and (collection/form? form)
       (= 'x:unpack (first form))
       (= 2 (count form))))

(defn- rewrite-expression-coll
  [items grammar]
  (map #(dart-rewrite-expression % grammar) items))

(defn- rewrite-expression-map
  [form grammar]
  (into (empty form)
        (map (fn [[k v]]
               [(dart-rewrite-expression k grammar)
                (dart-rewrite-expression v grammar)]))
        form))

(defn- rewrite-fn
  [form grammar]
  (let [[name args body] (lift/fn-parts form)]
    (with-form-meta
      form
      (apply list 'fn
             (concat (when name [name])
                     [args]
                     (letfn [(ensure-return [stmt]
                               (cond
                                 (and (collection/form? stmt)
                                      (contains? +dart-statement-heads+ (first stmt)))
                                 (if (#{'do 'do*} (first stmt))
                                   (let [prefix (butlast (rest stmt))
                                         tail   (last stmt)]
                                     (with-form-meta
                                       stmt
                                       (apply list (first stmt)
                                              (concat prefix
                                                      [(ensure-return tail)]))))
                                   stmt)

                                 :else
                                 (list 'return stmt)))]
                       (-> body
                           (dart-rewrite-statements grammar)
                           (#(if (= 1 (count %))
                               [(ensure-return (first %))]
                               %))
                           lift/splice-do*
                           lift/wrap-body)))))))

(defn- rewrite-or-expression
  [form grammar]
  (let [args* (rewrite-expression-coll (rest form) grammar)]
    (with-form-meta
      form
      (apply list (if (every? boolish-form? args*)
                    'or
                    'dart:or)
             args*))))

(defn- rewrite-ternary-expression
  [form grammar]
  (let [[_ test then else] form
        test*             (dart-rewrite-expression test grammar)
        then*             (dart-rewrite-expression then grammar)
        else*             (dart-rewrite-expression else grammar)]
    (with-form-meta
      form
      (list (if (boolish-form? test*)
              :?
              'dart:ternary)
            test*
            then*
            else*))))

(defn- rewrite-invoke-expression
  [form grammar]
  (let [head        (first form)
        head*       (if (collection/form? head)
                      (dart-rewrite-expression head grammar)
                      head)
        unpack?     (some unpack-form? (rest form))
        args*       (map (fn [arg]
                           (if (unpack-form? arg)
                             (list :.. (dart-rewrite-expression (second arg) grammar))
                             (dart-rewrite-expression arg grammar)))
                         (rest form))]
    (with-form-meta
      form
      (if unpack?
        (list 'Function.apply head* (vec args*))
        (apply list head* args*)))))

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

    for:async
    (rewrite-for-async-form form grammar)

    (rewrite-invoke-expression form grammar)))

(defn- rewrite-for-async-form
  [form grammar]
  (let [[_ [[res err] statement] {:keys [success error finally]}] form
        promise (list 'x:promise
                      (list 'fn []
                            (list 'return
                                  (dart-rewrite-expression statement grammar))))
        promise (list 'x:promise-then
                      promise
                      (list 'fn [res]
                            (dart-rewrite-statement success grammar)))
        promise (list 'x:promise-catch
                      promise
                      (list 'fn [err]
                            (dart-rewrite-statement error grammar)))]
    (with-form-meta
      form
      (if finally
        (list 'x:promise-finally
              promise
              (list 'fn []
                    (dart-rewrite-statement finally grammar)))
        promise))))

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
             (map #(dart-rewrite-conditional-expression % grammar) (rest form))))

    and
    (with-form-meta
      form
      (apply list 'and
             (map #(dart-rewrite-conditional-expression % grammar) (rest form))))

    not
    (with-form-meta
      form
      (list 'not
            (dart-rewrite-conditional-expression (second form) grammar)))

    :?
    (let [[_ test then else] form]
      (with-form-meta
        form
        (list :?
              (dart-rewrite-conditional-expression test grammar)
              (dart-rewrite-expression then grammar)
              (dart-rewrite-expression else grammar))))

    (let [head  (first form)
          head* (if (collection/form? head)
                  (dart-rewrite-expression head grammar)
                  head)]
      (with-form-meta
        form
        (apply list head*
               (map #(dart-rewrite-expression % grammar) (rest form)))))))

(defn dart-rewrite-conditional-expression
  [form grammar]
  (let [form* (cond
                (collection/form? form)
                (rewrite-conditional-expression-list form grammar)

                (vector? form)
                (with-form-meta form (vec (map #(dart-rewrite-expression % grammar) form)))

                (set? form)
                (with-form-meta form (set (map #(dart-rewrite-expression % grammar) form)))

                (map? form)
                (with-form-meta
                  form
                  (into (empty form)
                        (map (fn [[k v]]
                               [(dart-rewrite-expression k grammar)
                                (dart-rewrite-expression v grammar)]))
                        form))

                :else
                form)]
    (truthy-form form form*)))

(defn dart-rewrite-expression
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
  (with-form-meta
    form
    (apply list (first form)
           (-> form
               rest
               (dart-rewrite-statements grammar)
               lift/splice-do*))))

(defn- rewrite-var-statement
  [form grammar]
  (let [[tag sym & args] form]
    (if (empty? args)
      form
      (let [bound   (last args)
            leading (butlast args)]
        (with-form-meta
          form
          (apply list tag sym
                 (concat leading
                         [(dart-rewrite-expression bound grammar)])))))))

(defn- rewrite-return-statement
  [form grammar]
  (let [[tag & args] form]
    (with-form-meta
      form
      (apply list tag
             (map #(dart-rewrite-expression % grammar) args)))))

(defn- rewrite-if-statement
  [form grammar]
  (let [[tag test then & [else]] form]
    (with-form-meta
      form
      (apply list tag
             (cond-> [(dart-rewrite-conditional-expression test grammar)
                      (dart-rewrite-statement then grammar)]
               else (conj (dart-rewrite-statement else grammar)))))))

(defn- rewrite-when-statement
  [form grammar]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (dart-rewrite-conditional-expression test grammar)
             (dart-rewrite-statements body grammar)))))

(defn- rewrite-while-statement
  [form grammar]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (dart-rewrite-conditional-expression test grammar)
             (dart-rewrite-statements body grammar)))))

(defn- rewrite-defn-statement
  [form grammar]
  (let [[tag name args & body] form]
    (with-form-meta
      form
      (apply list tag name args
             (-> body
                 (dart-rewrite-statements grammar)
                 lift/splice-do*
                 lift/wrap-body)))))

(defn dart-rewrite-statement
  [form grammar]
  (cond
    (not (collection/form? form))
    (dart-rewrite-expression form grammar)

     :else
     (case (first form)
       (do do*)      (rewrite-do-statement form grammar)
       (var var* :=) (rewrite-var-statement form grammar)
       for:async     (rewrite-for-async-form form grammar)
       return        (rewrite-return-statement form grammar)
       if            (rewrite-if-statement form grammar)
       when          (rewrite-when-statement form grammar)
       while         (rewrite-while-statement form grammar)
      (defn defn- defgen)
      (rewrite-defn-statement form grammar)
      fn
      (rewrite-fn form grammar)
      (dart-rewrite-expression form grammar))))

(defn dart-rewrite-statements
  [forms grammar]
  (map #(dart-rewrite-statement % grammar) forms))

(defn dart-rewrite-stage
  [form {:keys [grammar] :as opts}]
  (let [form ((:rewrite-stage +dart-rewriter+) form opts)]
    (cond
      (collection/form? form)
      (dart-rewrite-statement form grammar)

      (vector? form)
      (with-form-meta form (mapv #(dart-rewrite-statement % grammar) form))

      :else
      form)))
