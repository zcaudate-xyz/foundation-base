(ns hara.model.annex.spec-julia.rewrite
  (:require [hara.common.util :as ut]
              [hara.lang.rewrite.conditional :as condrw]
              [hara.lang.rewrite.destructure :as destruct]
              [hara.lang.rewrite.hoist :as hoist]
              [hara.lang.rewrite.fn :as fnrw]
              [hara.lang.rewrite.statement :as stmt]
              [hara.lang.rewrite.truthy :as truthy]
              [hara.lang.rewrite.unpack :as unpack]
              [hara.lang.rewrite.walk :as walk]
              [std.lib.collection :as collection]))

(def ^:private +julia-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "julia_callback__"
    :lambda-compatible? (fn [_ _] true)}))

(def ^:private +julia-statement-heads+
  '#{do
     do*
     let
     let*
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
     x:throw
     x:err
     break
     continue
     defn
     defn-
     defgen
     fn})

(def ^:private +julia-boolish-ops+
  '#{and
     not
     <
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
(declare rewrite-fn)

(def ^:private with-form-meta
  walk/with-form-meta)

(defn- julia-boolish-form?
  [form]
  (truthy/boolish-form? form
                        {:boolish-ops +julia-boolish-ops+
                         :recursive-not? true
                         :recursive-and-or? true}))

(defn- simple-truthy-source?
  [form]
  (or (symbol? form)
      (keyword? form)
      (string? form)
      (number? form)
      (boolean? form)
      (nil? form)))

(defn- julia-wrap-truthy-check
  [source form grammar]
  (if (simple-truthy-source? form)
    (truthy/wrap-truthy-check source form)
    (let [value (gensym "julia_truthy__")]
      (with-form-meta
        source
        (list (rewrite-fn
               (list 'fn []
                     (list 'var value form)
                     (list 'return
                           (truthy/truthy-check-form value)))
               grammar))))))

(defn- julia-truthy-form
  [source form grammar]
  (truthy/truthy-form source
                      form
                      julia-boolish-form?
                      #(julia-wrap-truthy-check %1 %2 grammar)))

(defn- ensure-return
  [stmt]
  (cond
    (and (collection/form? stmt)
         (contains? +julia-statement-heads+ (first stmt)))
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
                        #(julia-rewrite-statements % grammar)
                        {:prepare-body ensure-tail-return}))

(defn- rewrite-for-statement
  [form grammar]
  (stmt/rewrite-for-statement form
                              #(walk/rewrite-binding-vector %
                                                           (fn [v]
                                                             (julia-rewrite-expression v grammar)))
                              #(julia-rewrite-statements % grammar)))

(defn- rewrite-cond-statement
  [form grammar]
  (stmt/rewrite-cond-statement form
                               #(julia-rewrite-conditional-expression % grammar)
                               #(julia-rewrite-statement % grammar)))

(defn- rewrite-branch-control
  [form grammar]
  (stmt/rewrite-branch-control form
                               #(julia-rewrite-conditional-expression % grammar)
                               #(julia-rewrite-statements % grammar)))

(defn- rewrite-branch-statement
  [form grammar]
  (stmt/rewrite-branch-statement form
                                 #(rewrite-branch-control % grammar)))

(defn- rewrite-or-expression
  [form grammar]
  (let [args* (vec (walk/rewrite-coll (rest form)
                                      #(julia-rewrite-expression % grammar)))]
    (cond
      (empty? args*)
      nil

      (= 1 (count args*))
      (first args*)

      (every? julia-boolish-form? args*)
      (with-form-meta
        form
        (apply list 'or args*))

      :else
      (reduce (fn [fallback value]
                (truthy/truthy-or-form form value fallback))
              (peek args*)
              (reverse (pop args*))))))

(defn- rewrite-and-step
  [source lhs rhs grammar]
  (if (simple-truthy-source? lhs)
    (with-form-meta
      source
      (list ':?
            (julia-truthy-form source lhs grammar)
            rhs
            lhs))
    (let [value (gensym "julia_and__")]
      (with-form-meta
        source
        (list (rewrite-fn
               (list 'fn []
                     (list 'var value lhs)
                     (list 'return
                           (list ':?
                                 (truthy/truthy-check-form value)
                                 rhs
                                 value)))
               grammar))))))

(defn- rewrite-and-expression
  [form grammar]
  (let [args* (vec (walk/rewrite-coll (rest form)
                                      #(julia-rewrite-expression % grammar)))]
    (cond
      (empty? args*)
      true

      (= 1 (count args*))
      (first args*)

      (every? julia-boolish-form? args*)
      (with-form-meta
        form
        (apply list 'and args*))

      :else
      (reduce #(rewrite-and-step form %1 %2 grammar)
              (first args*)
              (rest args*)))))

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

(defn- rewrite-invoke-expression
  [form grammar]
  (let [head    (first form)
        head*   (if (collection/form? head)
                  (julia-rewrite-expression head grammar)
                  head)
        args*   (unpack/rewrite-args (rest form)
                                     #(julia-rewrite-expression % grammar)
                                     identity
                                     #(list '... %))]
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

    and
    (rewrite-and-expression form grammar)

    or
    (rewrite-or-expression form grammar)

    :?
    (rewrite-ternary-expression form grammar)

    (rewrite-invoke-expression form grammar)))

(defn- rewrite-conditional-expression-list
  [form grammar]
  (condrw/rewrite-conditional-expression-list
   form
   #(rewrite-fn % grammar)
   #(julia-rewrite-conditional-expression % grammar)
   #(julia-rewrite-expression % grammar)))

(defn julia-rewrite-conditional-expression
  [form grammar]
  (condrw/rewrite-conditional-expression
   form
   #(rewrite-conditional-expression-list % grammar)
   #(julia-rewrite-expression % grammar)
   #(julia-truthy-form %1 %2 grammar)))

(defn julia-rewrite-expression
  [form grammar]
  (walk/rewrite-form form
                     #(rewrite-expression-list % grammar)
                     #(julia-rewrite-expression % grammar)))

(defn- rewrite-do-statement
  [form grammar]
  (stmt/rewrite-do-statement form
                             #(julia-rewrite-statements % grammar)
                             (fn [body]
                               (if (= 'do* (first body))
                                 (rest body)
                                 body))))

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
                     (map (fn [[sym value]]
                            (apply list tag sym
                                   (concat leading
                                           [value])))
                          (destruct/destructure-bindings target temp ut/sym-default-str)))))))

(defn- julia-global-assign?
  [target]
  (and (collection/form? target)
       (= '!:G (first target))
       (= 2 (count target))))

(defn- rewrite-var-statement
  [form grammar]
  (let [[tag target & args] form]
    (cond
      (empty? args)
      form

      (and (= tag :=)
           (julia-global-assign? target))
      (let [[_ key] target]
        (with-form-meta
          form
          (list ':=
                (list '. 'XT_GLOBALS [(ut/sym-default-str key)])
                (julia-rewrite-expression (last args) grammar))))

      (destruct/destructure-target? target)
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
  (stmt/rewrite-return-statement form
                                 #(julia-rewrite-expression % grammar)))

(defn- rewrite-if-statement
  [form grammar]
  (stmt/rewrite-if-statement form
                             #(julia-rewrite-conditional-expression % grammar)
                             #(julia-rewrite-statement % grammar)))

(defn- rewrite-when-statement
  [form grammar]
  (stmt/rewrite-when-statement form
                               #(julia-rewrite-conditional-expression % grammar)
                               #(julia-rewrite-statements % grammar)))

(defn- rewrite-while-statement
  [form grammar]
  (stmt/rewrite-while-statement form
                                #(julia-rewrite-conditional-expression % grammar)
                                #(julia-rewrite-statements % grammar)))

(defn- rewrite-defn-statement
  [form grammar]
  (stmt/rewrite-defn-statement form
                               #(julia-rewrite-statements % grammar)))

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
