(ns hara.model.spec-dart.rewrite
  (:require [hara.lang.rewrite.conditional :as condrw]
              [hara.lang.rewrite.hoist :as hoist]
              [hara.lang.rewrite.fn :as fnrw]
              [hara.lang.rewrite.statement :as stmt]
              [hara.typed.xtalk-analysis :as xtalk-analysis]
              [hara.lang.rewrite.truthy :as truthy]
              [hara.lang.rewrite.unpack :as unpack]
              [hara.lang.rewrite.walk :as walk]
              [std.lib.collection :as collection]))

(def ^:private +dart-rewriter+
  (hoist/create-rewriter
   {:symbol-prefix "dart_callback__"
    :lambda-compatible? (fn [_ _] true)}))

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
(declare rewrite-fn)

(def ^:private with-form-meta
  walk/with-form-meta)

(defn- dart-optional-input?
  [input]
  (= :maybe (get-in input [:type :kind])))

(defn- dart-function-var-symbol?
  [sym]
  (and (symbol? sym)
       (let [n (name sym)]
         (or (.startsWith n "dart_callback__")
             (.endsWith n "-fn")
             (.endsWith n "_fn")
             (#{"handler"
                "resolve"
                "reject"
                "process"
                "guard"
                "wrapper"}
              n)))))

(defn- dart-cast-function
  [form]
  (list :%
        (list :- "(")
        form
        (list :- " as Function)")))

(defn- dart-pad-optional-args
  [head args]
  (if-not (symbol? head)
    args
    (try
      (let [fn-def (xtalk-analysis/resolve-function-def head)
            optional-count (some->> fn-def
                                    :inputs
                                    reverse
                                    (take-while dart-optional-input?)
                                    count)
            missing-count  (when fn-def
                             (- (count (:inputs fn-def))
                                (count args)))]
        (if (and fn-def
                 (pos? optional-count)
                 (pos? missing-count)
                 (<= missing-count optional-count))
          (concat args (repeat missing-count nil))
          args))
      (catch Throwable _
        args))))

(defn- dart-boolish-form?
  [form]
  (truthy/boolish-form? form
                        {:boolish-ops +dart-boolish-ops+
                          :dot-boolish-calls +dart-dot-boolish-calls+}))

(defn- simple-truthy-source?
  [form]
  (or (symbol? form)
      (keyword? form)
      (string? form)
      (number? form)
      (boolean? form)
      (nil? form)))

(defn- dart-wrap-truthy-check
  [source form grammar]
  (if (simple-truthy-source? form)
    (truthy/wrap-truthy-check source form)
    (let [value (gensym "dart_truthy__")]
      (with-form-meta
        source
        (list (rewrite-fn
               (list 'fn []
                     (list 'var value form)
                     (list 'return
                           (truthy/truthy-check-form value)))
               grammar))))))

(defn- dart-truthy-form
  [source form grammar]
  (truthy/truthy-form source
                      form
                      dart-boolish-form?
                      #(dart-wrap-truthy-check %1 %2 grammar)))

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

(defn- dart-normalize-let
  [form]
  (let [[_ bindings & body] form]
    (with-form-meta
      form
      (apply list
             'do
             (concat (map (fn [[sym val]]
                            (if (= '_ sym)
                              val
                              (list 'var sym := val)))
                          (partition 2 bindings))
                     body)))))

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

(defn- rewrite-and-step
  [source lhs rhs grammar]
  (if (simple-truthy-source? lhs)
    (with-form-meta
      source
      (list ':?
            (dart-truthy-form source lhs grammar)
            rhs
            lhs))
    (let [value (gensym "dart_and__")]
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
  (let [args* (walk/rewrite-coll (rest form)
                                 #(dart-rewrite-expression % grammar))]
    (with-form-meta
      form
      (cond
        (empty? args*)
        true

        (= 1 (count args*))
        (first args*)

        (every? dart-boolish-form? args*)
        (apply list 'and args*)

        :else
        (reduce #(rewrite-and-step form %1 %2 grammar)
                (first args*)
                (rest args*))))))

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
        apply?      (or unpack?
                        (dart-function-var-symbol? head*)
                        (dart-function-var-symbol? head))
        args        (if unpack?
                      args
                      (dart-pad-optional-args head args))
        args*       (unpack/rewrite-args args
                                         #(dart-rewrite-expression % grammar)
                                         identity
                                         #(list :.. %))]
    (with-form-meta
      form
      (if apply?
        (list 'Function.apply
              (if (or (dart-function-var-symbol? head*)
                      (dart-function-var-symbol? head))
                (dart-cast-function head*)
                head*)
              (vec args*))
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

    (let let*)
    (dart-rewrite-expression (dart-normalize-let form) grammar)

    fn
    (rewrite-fn form grammar)

    and
    (rewrite-and-expression form grammar)

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
   #(dart-truthy-form %1 %2 grammar)))

(defn- dart-global-assign?
  "Matches direct assignment to a !:G global: (= (!:G key) value)."
  [form]
  (and (collection/form? form)
       (= ':= (first form))
       (collection/form? (second form))
       (= '!:G (first (second form)))
       (= 2 (count (second form)))))

(defn- dart-rewrite-global-assign
  "Rewrites (= (!:G key) value) to (x:set-key !:G (str key) value),
   which emits the valid Dart __globals__[\"key\"] = value."
  [form grammar]
  (let [[_ [_ key] value] form]
    (dart-rewrite-statement (list 'x:set-key '!:G (str key) value)
                            grammar)))

(defn- dart-global-read?
  "Matches a bare (!:G key) expression."
  [form]
  (and (collection/form? form)
       (= '!:G (first form))
       (= 2 (count form))))

(defn- dart-rewrite-global-read
  "Rewrites bare (!:G key) to (x:get-key !:G (str key)),
   so nil values (including deleted keys) round-trip as nil."
  [form grammar]
  (let [[_ key] form]
    (dart-rewrite-expression (list 'x:get-key '!:G (str key))
                             grammar)))

(defn dart-rewrite-expression
  [form grammar]
  (or (when (dart-global-read? form)
        (dart-rewrite-global-read form grammar))
      (walk/rewrite-form form
                         #(rewrite-expression-list % grammar)
                         #(dart-rewrite-expression % grammar))))

(defn- rewrite-do-statement
  [form grammar]
  (stmt/rewrite-do-statement (if (= 'do* (first form))
                               (with-form-meta form
                                 (apply list 'do (rest form)))
                               form)
                             #(dart-rewrite-statements % grammar)))

(defn- rewrite-var-statement
  [form grammar]
  (stmt/rewrite-var-statement form
                              #(dart-rewrite-expression % grammar)))

(defn- rewrite-return-statement
  [form grammar]
  (stmt/rewrite-return-statement form
                                 #(dart-rewrite-expression % grammar)))

(defn- rewrite-throw-statement
  [form grammar]
  (list 'throw (dart-rewrite-expression (second form) grammar)))

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

    (dart-global-assign? form)
    (dart-rewrite-global-assign form grammar)

     :else
     (case (first form)
         (do do*)      (rewrite-do-statement form grammar)
         (let let*)    (dart-rewrite-statement (dart-normalize-let form) grammar)
         (var var* :=) (rewrite-var-statement form grammar)
         cond          (rewrite-cond-statement form grammar)
         br*           (rewrite-branch-statement form grammar)
        (for:index for:object for:array for:iter)
        (rewrite-for-statement form grammar)
        for:async     (rewrite-for-async-form form grammar)
        return        (rewrite-return-statement form grammar)
        throw         (rewrite-throw-statement form grammar)
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
      (and (collection/form? form)
           (#{'do 'do*} (first form)))
      (dart-rewrite-expression form grammar)

      (collection/form? form)
      (dart-rewrite-statement form grammar)

      (vector? form)
      (with-form-meta form (mapv #(dart-rewrite-statement % grammar) form))

      :else
      form)))
