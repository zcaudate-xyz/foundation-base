(ns std.lang.model.spec-dart.rewrite
  (:require [std.lang.rewrite.conditional :as condrw]
             [std.lang.rewrite.hoist :as hoist]
             [std.lang.rewrite.fn :as fnrw]
             [std.lang.rewrite.statement :as stmt]
             [std.lang.rewrite.truthy :as truthy]
             [std.lang.rewrite.unpack :as unpack]
             [std.lang.rewrite.walk :as walk]
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
     cond
     when
     while
     try
     for
     for:async
     for:index
     for:object
     for:array
     for:iter
     br*
     throw
     break
     continue
     defn
     defn-
     defgen
     fn})

(def ^:private +dart-dot-boolish-calls+
  '#{contains
     containsKey
     endsWith
     moveNext
     startsWith})

(declare dart-rewrite-expression)
(declare dart-rewrite-statement)
(declare dart-rewrite-statements)
(declare dart-rewrite-conditional-expression)
(declare rewrite-for-async-form)

(def ^:private with-form-meta
  walk/with-form-meta)

(defn- dart-boolish-form?
  [form]
  (truthy/boolish-form? form
                        {:boolish-ops +dart-boolish-ops+
                         :dot-boolish-calls +dart-dot-boolish-calls+}))

(defn- dart-wrap-truthy
  [source form]
  (let [value-sym (gensym "truthy_")]
    (with-form-meta
      source
      (list (list 'fn [value-sym]
                  (list 'return
                        (truthy/truthy-check-form value-sym)))
            form))))

(defn- dart-truthy-form
  [source form]
  (truthy/truthy-form source form dart-boolish-form? dart-wrap-truthy))

(defn- ensure-return
  [stmt]
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
    (list 'return stmt)))

(defn- ensure-tail-return
  [stmts]
  (if (seq stmts)
    (concat (butlast stmts)
            [(ensure-return (last stmts))])
    stmts))

(defn- rewrite-fn
  [form grammar]
  (fnrw/rewrite-fn-form form
                        #(dart-rewrite-statements % grammar)
                        {:prepare-body ensure-tail-return}))

(defn- rewrite-for-statement
  [form grammar]
  (stmt/rewrite-for-statement form
                              #(walk/rewrite-binding-vector %
                                                           (fn [v]
                                                             (dart-rewrite-expression v grammar)))
                              #(dart-rewrite-statements % grammar)))

(defn- rewrite-cond-statement
  [form grammar]
  (stmt/rewrite-cond-statement form
                               #(dart-rewrite-conditional-expression % grammar)
                               #(dart-rewrite-statement % grammar)))

(defn- rewrite-branch-control
  [form grammar]
  (stmt/rewrite-branch-control form
                               #(dart-rewrite-conditional-expression % grammar)
                               #(dart-rewrite-statements % grammar)))

(defn- rewrite-branch-statement
  [form grammar]
  (stmt/rewrite-branch-statement form
                                 #(rewrite-branch-control % grammar)))

(defn- rewrite-or-expression
  [form grammar]
  (let [args* (walk/rewrite-coll (rest form)
                                 #(dart-rewrite-expression % grammar))]
    (with-form-meta
      form
      (apply list (if (every? dart-boolish-form? args*)
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
      (list (if (dart-boolish-form? test*)
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
        args        (rest form)
        unpack?     (unpack/any-unpack? args)
        args*       (unpack/rewrite-args args
                                         #(dart-rewrite-expression % grammar)
                                         identity
                                         #(list :.. %))]
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
  (condrw/rewrite-conditional-expression-list
   form
   #(rewrite-fn % grammar)
   #(dart-rewrite-conditional-expression % grammar)
   #(dart-rewrite-expression % grammar)))

(defn dart-rewrite-conditional-expression
  [form grammar]
  (condrw/rewrite-conditional-expression
   form
   #(rewrite-conditional-expression-list % grammar)
   #(dart-rewrite-expression % grammar)
   dart-truthy-form))

(defn dart-rewrite-expression
  [form grammar]
  (walk/rewrite-form form
                     #(rewrite-expression-list % grammar)
                     #(dart-rewrite-expression % grammar)))

(defn- rewrite-do-statement
  [form grammar]
  (stmt/rewrite-do-statement form
                             #(dart-rewrite-statements % grammar)))

(defn- rewrite-var-statement
  [form grammar]
  (stmt/rewrite-var-statement form
                              #(dart-rewrite-expression % grammar)))

(defn- rewrite-return-statement
  [form grammar]
  (stmt/rewrite-return-statement form
                                 #(dart-rewrite-expression % grammar)))

(defn- rewrite-if-statement
  [form grammar]
  (stmt/rewrite-if-statement form
                             #(dart-rewrite-conditional-expression % grammar)
                             #(dart-rewrite-statement % grammar)))

(defn- rewrite-when-statement
  [form grammar]
  (stmt/rewrite-when-statement form
                               #(dart-rewrite-conditional-expression % grammar)
                               #(dart-rewrite-statements % grammar)))

(defn- rewrite-while-statement
  [form grammar]
  (stmt/rewrite-while-statement form
                                #(dart-rewrite-conditional-expression % grammar)
                                #(dart-rewrite-statements % grammar)))

(defn- rewrite-defn-statement
  [form grammar]
  (stmt/rewrite-defn-statement form
                               #(dart-rewrite-statements % grammar)
                               ensure-tail-return))

(defn dart-rewrite-statement
  [form grammar]
  (cond
    (not (collection/form? form))
    (dart-rewrite-expression form grammar)

     :else
     (case (first form)
        (do do*)      (rewrite-do-statement form grammar)
        (var var* :=) (rewrite-var-statement form grammar)
        cond          (rewrite-cond-statement form grammar)
        br*           (rewrite-branch-statement form grammar)
        (for:index for:object for:array for:iter)
        (rewrite-for-statement form grammar)
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
