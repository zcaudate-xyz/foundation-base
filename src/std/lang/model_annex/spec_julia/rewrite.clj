(ns std.lang.model-annex.spec-julia.rewrite
  (:require [std.lang.base.util :as ut]
              [std.lang.rewrite.conditional :as condrw]
              [std.lang.rewrite.destructure :as destruct]
              [std.lang.rewrite.hoist :as hoist]
              [std.lang.rewrite.fn :as fnrw]
              [std.lang.rewrite.statement :as stmt]
              [std.lang.rewrite.truthy :as truthy]
              [std.lang.rewrite.unpack :as unpack]
              [std.lang.rewrite.walk :as walk]
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

(def ^:private with-form-meta
  walk/with-form-meta)

(defn- julia-boolish-form?
  [form]
  (truthy/boolish-form? form
                        {:boolish-ops +julia-boolish-ops+
                         :recursive-not? true
                         :recursive-and-or? true}))

(defn- julia-truthy-form
  [source form]
  (truthy/truthy-form source form julia-boolish-form?))

(defn- rewrite-fn
  [form grammar]
  (fnrw/rewrite-fn-form form
                        #(julia-rewrite-statements % grammar)))

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
   julia-truthy-form))

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

(defn- rewrite-var-statement
  [form grammar]
  (let [[tag target & args] form]
    (cond
      (empty? args)
      form

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
