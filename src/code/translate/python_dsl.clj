(ns code.translate.python-dsl
  (:require [std.lib :as h]))

(defmulti translate-node :type)

(defmethod translate-node :default [node]
  (h/error "Unknown node type" {:type (:type node) :node node}))

(defmethod translate-node "Module" [node]
  (mapv translate-node (:body node)))

(defmethod translate-node "Name" [node]
  (symbol (:id node)))

(defmethod translate-node "Constant" [node]
  (:value node))

(defmethod translate-node "Expr" [node]
  (translate-node (:value node)))

(defmethod translate-node "UnaryOp" [node]
  (let [op (:op node)
        operand (translate-node (:operand node))
        op-sym (case (:type op)
                 "UAdd" '+
                 "USub" '-
                 "Not" 'not
                 "Invert" 'bit-not
                 (symbol (:type op)))]
    (list op-sym operand)))

(defmethod translate-node "BinOp" [node]
  (let [op (:op node)
        left (translate-node (:left node))
        right (translate-node (:right node))
        op-sym (case (:type op)
                 "Add" '+
                 "Sub" '-
                 "Mult" '*
                 "Div" '/
                 "FloorDiv" 'floor-div
                 "Mod" '%
                 "Pow" '**
                 "LShift" '<<
                 "RShift" '>>
                 "BitOr" 'bit-or
                 "BitXor" 'bit-xor
                 "BitAnd" 'bit-and
                 "MatMult" 'mat-mult
                 (symbol (:type op)))]
    (list op-sym left right)))

(defmethod translate-node "BoolOp" [node]
  (let [op (:op node)
        values (mapv translate-node (:values node))
        op-sym (case (:type op)
                 "And" 'and
                 "Or" 'or
                 (symbol (:type op)))]
    (cons op-sym values)))

(defmethod translate-node "Compare" [node]
  (let [left (translate-node (:left node))
        ops (mapv :type (:ops node))
        comparators (mapv translate-node (:comparators node))]
    (if (= 1 (count ops))
      (let [op-sym (case (first ops)
                     "Eq" '==
                     "NotEq" '!=
                     "Lt" '<
                     "LtE" '<=
                     "Gt" '>
                     "GtE" '>=
                     "Is" 'is
                     "IsNot" 'is-not
                     "In" 'in
                     "NotIn" 'not-in
                     (symbol (first ops)))]
        (list op-sym left (first comparators)))
      (apply list 'chain-compare left (interleave ops comparators)))))

(defmethod translate-node "Call" [node]
  (let [func (translate-node (:func node))
        args (mapv translate-node (:args node))
        keywords (mapv (fn [k]
                         (if (:arg k)
                           [(keyword (:arg k)) (translate-node (:value k))]
                           ;; arg is nil -> **kwargs
                           ;; We can represent **kwargs as (:** value)
                           [(:** (translate-node (:value k)))]))
                       (:keywords node))]
    (if (seq keywords)
      (apply list func (concat args (apply concat keywords)))
      (apply list func args))))

(defmethod translate-node "Attribute" [node]
  (let [value (translate-node (:value node))
        attr (symbol (:attr node))]
    (list '. value attr)))

(defmethod translate-node "Subscript" [node]
  (let [value (translate-node (:value node))
        slice (translate-node (:slice node))]
    (list 'get value slice)))

(defmethod translate-node "List" [node]
  (mapv translate-node (:elts node)))

(defmethod translate-node "Tuple" [node]
  (apply list 'tuple (mapv translate-node (:elts node))))

(defmethod translate-node "Dict" [node]
  (let [keys (mapv translate-node (:keys node))
        values (mapv translate-node (:values node))]
    (apply hash-map (interleave keys values))))

(defmethod translate-node "Set" [node]
  (set (mapv translate-node (:elts node))))

(defmethod translate-node "Assign" [node]
  (let [targets (mapv translate-node (:targets node))
        value (translate-node (:value node))]
    (if (= 1 (count targets))
      (list ':= (first targets) value)
      (cons ':= (concat targets [value])))))

(defmethod translate-node "AugAssign" [node]
  (let [target (translate-node (:target node))
        op (:op node)
        value (translate-node (:value node))
        op-sym (case (:type op)
                 "Add" '+=
                 "Sub" '-=
                 "Mult" '*=
                 "Div" (symbol "/=")
                 "FloorDiv" (symbol "floor-div=")
                 "Mod" (symbol "%=")
                 "Pow" '**=
                 "LShift" '<<=
                 "RShift" '>>=
                 "BitOr" '|=
                 "BitXor" 'bit-xor=
                 "BitAnd" '&=
                 "MatMult" 'mat-mult=
                 (symbol (str (:type op) "=")))]
    (list op-sym target value)))

(defmethod translate-node "Return" [node]
  (if (:value node)
    (list 'return (translate-node (:value node)))
    (list 'return)))

(defmethod translate-node "Pass" [_]
  '(pass))

(defmethod translate-node "Break" [_]
  '(break))

(defmethod translate-node "Continue" [_]
  '(continue))

(defmethod translate-node "If" [node]
  (let [test (translate-node (:test node))
        body (cons 'do (mapv translate-node (:body node)))
        orelse (mapv translate-node (:orelse node))]
    (if (seq orelse)
      (list 'if test body (cons 'do orelse))
      (list 'if test body))))

(defmethod translate-node "While" [node]
  (let [test (translate-node (:test node))
        body (cons 'do (mapv translate-node (:body node)))
        orelse (mapv translate-node (:orelse node))]
    (if (seq orelse)
      (list 'while test body (cons 'do orelse))
      (list 'while test body))))

(defmethod translate-node "For" [node]
  (let [target (translate-node (:target node))
        iter (translate-node (:iter node))
        body (cons 'do (mapv translate-node (:body node)))
        orelse (mapv translate-node (:orelse node))]
    (if (seq orelse)
      (list 'for [target :in iter] body (cons 'do orelse))
      (list 'for [target :in iter] body))))

(defmethod translate-node "AsyncFor" [node]
  (let [target (translate-node (:target node))
        iter (translate-node (:iter node))
        body (cons 'do (mapv translate-node (:body node)))
        orelse (mapv translate-node (:orelse node))]
    (if (seq orelse)
      (list 'for:async [target :in iter] body (cons 'do orelse))
      (list 'for:async [target :in iter] body))))

(defmethod translate-node "FunctionDef" [node]
  (let [name (symbol (:name node))
        args (translate-node (:args node))
        body (cons 'do (mapv translate-node (:body node)))
        decorator-list (mapv translate-node (:decorator_list node))]
    (if (seq decorator-list)
       (with-meta (list 'defn name args body)
                  {:decorators decorator-list})
       (list 'defn name args body))))

(defmethod translate-node "AsyncFunctionDef" [node]
  (let [name (symbol (:name node))
        args (translate-node (:args node))
        body (cons 'do (mapv translate-node (:body node)))
        decorator-list (mapv translate-node (:decorator_list node))
        form (list 'defn name args body)
        form (with-meta form (merge (meta form) {:async true}))]
    (if (seq decorator-list)
       (with-meta form (merge (meta form) {:decorators decorator-list}))
       form)))

(defmethod translate-node "arguments" [node]
  (let [args (mapv (fn [a] (symbol (:arg a))) (:args node))
        posonlyargs (mapv (fn [a] (symbol (:arg a))) (:posonlyargs node))
        kwonlyargs (mapv (fn [a] (symbol (:arg a))) (:kwonlyargs node))

        vararg (if (:vararg node) [(symbol (str "*" (:arg (:vararg node))))] [])
        kwarg (if (:kwarg node) [(symbol (str "**" (:arg (:kwarg node))))] [])

        defaults (mapv translate-node (:defaults node))
        ;; kw_defaults can contain nil
        kw_defaults (mapv (fn [d] (if d (translate-node d) nil)) (:kw_defaults node))

        ;; Handle defaults for standard args
        num-args (count args)
        num-defaults (count defaults)
        start-defaults-idx (- num-args num-defaults)

        args-with-defaults
        (map-indexed (fn [i arg]
                       (if (>= i start-defaults-idx)
                         (list arg (nth defaults (- i start-defaults-idx)))
                         arg))
                     args)

        ;; Handle defaults for kwonly args
        ;; kw_defaults is a list matching kwonlyargs length, can contain nil if no default
        kwonly-with-defaults
        (map (fn [arg default]
               (if default
                 (list arg default)
                 arg))
             kwonlyargs
             kw_defaults)

        all-pos-args (concat posonlyargs (if (seq posonlyargs) [(symbol "/")] [])
                             args-with-defaults)]

    (vec (concat all-pos-args vararg kwonly-with-defaults kwarg))))

(defmethod translate-node "Lambda" [node]
  (let [args (translate-node (:args node))
        body (translate-node (:body node))]
    (list 'fn args body)))

(defmethod translate-node "ClassDef" [node]
  (let [name (symbol (:name node))
        bases (mapv translate-node (:bases node))
        keywords (mapv (fn [k]
                         (if (:arg k)
                           [(keyword (:arg k)) (translate-node (:value k))]
                           ;; arg is nil -> **kwargs
                           [(:** (translate-node (:value k)))]))
                       (:keywords node))
        body (cons 'do (mapv translate-node (:body node)))
        decorator-list (mapv translate-node (:decorator_list node))
        form (concat (list 'defclass name bases) (rest body))]
    (cond-> form
      (seq decorator-list) (with-meta (merge (meta form) {:decorators decorator-list}))
      (seq keywords)       (with-meta (merge (meta form) {:keywords keywords})))))

(defmethod translate-node "Import" [node]
  (cons 'do
        (mapv (fn [alias]
                (let [name (symbol (:name alias))
                      asname (if (:asname alias) (symbol (:asname alias)) nil)]
                  (if asname
                    (list 'import name :as asname)
                    (list 'import name))))
              (:names node))))

(defmethod translate-node "ImportFrom" [node]
  (let [module (if (:module node) (symbol (:module node)) nil)
        level (:level node) ;; int
        names (:names node)]
    (cons 'do
          (mapv (fn [alias]
                  (let [name (symbol (:name alias))
                        asname (if (:asname alias) (symbol (:asname alias)) nil)

                        ;; Construct module path with relative levels
                        ;; level 0: absolute
                        ;; level 1: .module
                        ;; level 2: ..module
                        dots (apply str (repeat level "."))
                        full-module (if module
                                      (symbol (str dots module))
                                      (if (> level 0)
                                        (symbol dots)
                                        nil))]

                    (if asname
                      (list 'from full-module 'import [name :as asname])
                      (list 'from full-module 'import name))))
                names))))

(defmethod translate-node "Slice" [node]
  (let [lower (if (:lower node) (translate-node (:lower node)) nil)
        upper (if (:upper node) (translate-node (:upper node)) nil)
        step (if (:step node) (translate-node (:step node)) nil)]
    (list 'slice lower upper step)))

(defmethod translate-node "ListComp" [node]
  (let [elt (translate-node (:elt node))
        generators (mapv translate-node (:generators node))]
    (list 'list-comp elt generators)))

(defmethod translate-node "SetComp" [node]
  (let [elt (translate-node (:elt node))
        generators (mapv translate-node (:generators node))]
    (list 'set-comp elt generators)))

(defmethod translate-node "DictComp" [node]
  (let [key (translate-node (:key node))
        value (translate-node (:value node))
        generators (mapv translate-node (:generators node))]
    (list 'dict-comp key value generators)))

(defmethod translate-node "GeneratorExp" [node]
  (let [elt (translate-node (:elt node))
        generators (mapv translate-node (:generators node))]
    (list 'gen-expr elt generators)))

(defmethod translate-node "comprehension" [node]
  (let [target (translate-node (:target node))
        iter (translate-node (:iter node))
        ifs (mapv translate-node (:ifs node))
        is-async (:is_async node)]
    {:target target :iter iter :ifs ifs :async is-async}))

(defmethod translate-node "Try" [node]
  (let [body (cons 'do (mapv translate-node (:body node)))
        handlers (mapv translate-node (:handlers node))
        orelse (mapv translate-node (:orelse node))
        finalizer (mapv translate-node (:finalizer node))]
    (concat (list 'try body)
            handlers
            (if (seq orelse)
              [(list 'else (cons 'do orelse))]
              [])
            (if (seq finalizer)
              [(list 'finally (cons 'do finalizer))]
              []))))

(defmethod translate-node "ExceptHandler" [node]
  (let [type (if (:type node) (translate-node (:type node)) nil)
        name (if (:name node) (symbol (:name node)) nil)
        body (cons 'do (mapv translate-node (:body node)))]
    (cond
      (and type name) (list 'catch [type :as name] body)
      type            (list 'catch [type] body)
      :else           (list 'catch [] body))))

(defmethod translate-node "With" [node]
  (let [items (:items node)
        body (cons 'do (mapv translate-node (:body node)))]
    (if (= 1 (count items))
      (let [item (first items)
            context (translate-node (:context_expr item))
            optional (if (:optional_vars item) (translate-node (:optional_vars item)) nil)]
        (if optional
          (list 'with (list context :as optional) body)
          (list 'with context body)))
      (list 'with (mapv (fn [item]
                          (let [context (translate-node (:context_expr item))
                                optional (if (:optional_vars item) (translate-node (:optional_vars item)) nil)]
                            (if optional
                              (list context :as optional)
                              context)))
                        items)
            body))))

(defmethod translate-node "AsyncWith" [node]
  (let [items (:items node)
        body (cons 'do (mapv translate-node (:body node)))]
    (if (= 1 (count items))
      (let [item (first items)
            context (translate-node (:context_expr item))
            optional (if (:optional_vars item) (translate-node (:optional_vars item)) nil)]
        (if optional
          (list 'with:async (list context :as optional) body)
          (list 'with:async context body)))
      (list 'with:async (mapv (fn [item]
                                (let [context (translate-node (:context_expr item))
                                      optional (if (:optional_vars item) (translate-node (:optional_vars item)) nil)]
                                  (if optional
                                    (list context :as optional)
                                    context)))
                              items)
            body))))

(defmethod translate-node "Delete" [node]
  (cons 'del (mapv translate-node (:targets node))))

(defmethod translate-node "Global" [node]
  (cons 'global (mapv symbol (:names node))))

(defmethod translate-node "Nonlocal" [node]
  (cons 'nonlocal (mapv symbol (:names node))))

(defmethod translate-node "Await" [node]
  (list 'await (translate-node (:value node))))

(defmethod translate-node "Yield" [node]
  (if (:value node)
    (list 'yield (translate-node (:value node)))
    (list 'yield)))

(defmethod translate-node "YieldFrom" [node]
  (list 'yield-from (translate-node (:value node))))

(defmethod translate-node "Raise" [node]
  (let [exc (if (:exc node) (translate-node (:exc node)) nil)
        cause (if (:cause node) (translate-node (:cause node)) nil)]
    (cond
      (and exc cause) (list 'raise exc :from cause)
      exc             (list 'raise exc)
      :else           (list 'raise))))

(defmethod translate-node "Assert" [node]
  (let [test (translate-node (:test node))
        msg (if (:msg node) (translate-node (:msg node)) nil)]
    (if msg
      (list 'assert test msg)
      (list 'assert test))))

(defmethod translate-node "Starred" [node]
  (list (symbol "*") (translate-node (:value node))))

;; F-strings and Assignment Expression
(defmethod translate-node "JoinedStr" [node]
  (let [values (mapv translate-node (:values node))]
    (cons 'str values)))

(defmethod translate-node "FormattedValue" [node]
  (translate-node (:value node)))

(defmethod translate-node "NamedExpr" [node]
  (let [target (translate-node (:target node))
        value (translate-node (:value node))]
    (list ':= target value)))
