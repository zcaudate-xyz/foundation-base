(ns code.tool.translate.js-dsl
  (:require [std.lib :as h]
            [clojure.string :as str]))

(defmulti translate-node (fn [node] (if (nil? node) :nil (:type node))))

(defn translate-args [args]
  (mapv translate-node args))

(defmethod translate-node :nil [_] nil)

(defmethod translate-node :default [node]
  (h/error "Unknown node type" {:type (:type node) :node node}))

(defmethod translate-node "File" [node]
  (translate-node (:program node)))

(defmethod translate-node "Program" [node]
  (mapv translate-node (:body node)))

(defmethod translate-node "Identifier" [node]
  (symbol (:name node)))

(defmethod translate-node "NumericLiteral" [node]
  (:value node))

(defmethod translate-node "RegExpLiteral" [node]
  (list 'new 'RegExp (:pattern node) (:flags node)))

(defmethod translate-node "StringLiteral" [node]
  (:value node))

(defmethod translate-node "BooleanLiteral" [node]
  (:value node))

(defmethod translate-node "NullLiteral" [_]
  nil)

(defmethod translate-node "ExpressionStatement" [node]
  (translate-node (:expression node)))

(defmethod translate-node "BinaryExpression" [node]
  (list (symbol (:operator node))
        (translate-node (:left node))
        (translate-node (:right node))))

(defmethod translate-node "AssignmentExpression" [node]
  (let [op (:operator node)]
    (if (= op "=")
      (list ':=
            (translate-node (:left node))
            (translate-node (:right node)))
      (list (symbol op)
            (translate-node (:left node))
            (if (:right node)
              (translate-node (:right node))
              nil)))))

(defmethod translate-node "AssignmentPattern" [node]
  (list 'default
        (translate-node (:left node))
        (translate-node (:right node))))

(defmethod translate-node "LogicalExpression" [node]
  (list (symbol (:operator node))
        (translate-node (:left node))
        (translate-node (:right node))))

(defmethod translate-node "ConditionalExpression" [node]
  (list 'if
        (translate-node (:test node))
        (translate-node (:consequent node))
        (translate-node (:alternate node))))

(defmethod translate-node "UnaryExpression" [node]
  (list (symbol (:operator node))
        (translate-node (:argument node))))

(defmethod translate-node "CallExpression" [node]
  (let [callee (translate-node (:callee node))
        args   (translate-args (:arguments node))]
    (if (and (seq? callee) (= '. (first callee)))
      (if (= callee '(. React createElement))
        (apply list args)
        (apply list (concat callee args)))
      (apply list callee args))))

(defmethod translate-node "MemberExpression" [node]
  (let [obj (translate-node (:object node))
        prop (translate-node (:property node))
        computed (:computed node)]
    (if computed
      (list 'get obj prop)
      (list '. obj (if (symbol? prop) prop (list 'quote prop))))))

(defmethod translate-node "OptionalMemberExpression" [node]
  (let [obj (translate-node (:object node))
        prop (translate-node (:property node))
        computed (:computed node)]
    (if computed
      (list 'get? obj prop)
      (list '?. obj (if (symbol? prop) prop (list 'quote prop))))))

(defmethod translate-node "OptionalCallExpression" [node]
  (let [callee (translate-node (:callee node))
        args   (translate-args (:arguments node))]
    (apply list '?. callee args)))

(defmethod translate-node "VariableDeclaration" [node]
  (let [declarations (:declarations node)
        kind (:kind node)] ;; var, let, const
    (if (and (= kind "const")
             (= 1 (count declarations))
             (#{ "ArrowFunctionExpression" "FunctionExpression"} (:type (:init (first declarations)))))
      (let [d (first declarations)
            id (translate-node (:id d))
            init (:init d)
            params (translate-args (:params init))
            body-node (:body init)
            body (translate-node body-node)
            async (:async init)]
        (cond-> (if (= "BlockStatement" (:type body-node))
                  (list 'defn.js id params body)
                  (list 'defn.js id params (list 'return body)))
          async (with-meta {:async true})))
      (cons 'var
            (mapv (fn [d]
                    (let [id (translate-node (:id d))]
                      (if (:init d)
                        (list id (translate-node (:init d)))
                        id)))
                  declarations)))))

(defmethod translate-node "FunctionDeclaration" [node]
  (let [id (translate-node (:id node))
        params (translate-args (:params node))
        body (translate-node (:body node))]
    (list 'defn.js id params body)))

(defmethod translate-node "FunctionExpression" [node]
  (let [id (if (:id node) (translate-node (:id node)) nil)
        params (translate-args (:params node))
        body (translate-node (:body node))
        async (:async node)]
    (cond-> (if (= "BlockStatement" (:type (:body node)))
              (if id
                (list 'fn id params (translate-node (:body node)))
                (list 'fn params (translate-node (:body node))))
              (if id
                (list 'fn id params (list 'return body))
                (list 'fn params (list 'return body))))
      async (with-meta {:async true}))))

(defmethod translate-node "ArrowFunctionExpression" [node]
  (let [params (translate-args (:params node))
        body (translate-node (:body node))
        async (:async node)]
    (cond-> (if (= "BlockStatement" (:type (:body node)))
              (list 'fn params (translate-node (:body node)))
              (list 'fn params (list 'return body)))
      async (with-meta {:async true}))))

(defmethod translate-node "ReturnStatement" [node]
  (if (:argument node)
    (list 'return (translate-node (:argument node)))
    (list 'return)))

(defmethod translate-node "BreakStatement" [node]
  (if (:label node)
    (list 'break (translate-node (:label node)))
    '(break)))

(defmethod translate-node "AwaitExpression" [node]
  (list 'await (translate-node (:argument node))))

(defmethod translate-node "BlockStatement" [node]
  (cons 'do (translate-args (:body node))))

(defmethod translate-node "IfStatement" [node]
  (let [test (translate-node (:test node))
        consequent (translate-node (:consequent node))
        alternate (if (:alternate node)
                    (translate-node (:alternate node))
                    nil)]
    (if alternate
      (list 'if test consequent alternate)
      (list 'if test consequent))))

(defmethod translate-node "ForStatement" [node]
  (let [init (if (:init node) (translate-node (:init node)) nil)
        test (if (:test node) (translate-node (:test node)) nil)
        update (if (:update node) (translate-node (:update node)) nil)
        body (translate-node (:body node))]
    (list 'for (vector init test update) body)))

(defmethod translate-node "ForOfStatement" [node]
  (let [left-node (:left node)
        left (if (= "VariableDeclaration" (:type left-node))
               (translate-node (:id (first (:declarations left-node))))
               (translate-node left-node))
        right (translate-node (:right node))
        body (translate-node (:body node))
        is-await (:await node)]
    (if is-await
      (list 'for:await [left right] body)
      (list 'for:of [left right] body))))

(defmethod translate-node "WhileStatement" [node]
  (let [test (translate-node (:test node))
        body (translate-node (:body node))]
    (list 'while test body)))

(defmethod translate-node "ObjectExpression" [node]
  (let [props (:properties node)]
    (apply hash-map
           (mapcat (fn [p]
                     (if (= "SpreadElement" (:type p))
                       [:& (translate-node (:argument p))]
                       [(if (:computed p)
                          (translate-node (:key p))
                          (let [key-node (:key p)
                                key-val (or (:name key-node) (:value key-node))]
                            (if (number? key-val)
                              key-val
                              (keyword key-val))))
                        (translate-node (:value p))]))
                   props))))

(defmethod translate-node "ArrayExpression" [node]
  (mapv translate-node (:elements node)))

(defmethod translate-node "NewExpression" [node]
  (let [callee (translate-node (:callee node))
        args   (translate-args (:arguments node))]
    (apply list 'new callee args)))

(defmethod translate-node "TemplateLiteral" [node]
  (let [quasis (:quasis node)
        expressions (:expressions node)
        parts (interleave (map (comp :raw :value) quasis)
                          (concat (map translate-node expressions) [nil]))]
    (apply list 'str (remove nil? parts))))

(defmethod translate-node "ClassDeclaration" [node]
  (let [id (translate-node (:id node))
        super-class (if (:superClass node) (translate-node (:superClass node)) nil)
        body (:body (:body node))
        methods (mapv (fn [m]
                        (cond (= "PropertyDefinition" (:type m))
                              (let [key (translate-node (:key m))
                                    value (if (:value m) (translate-node (:value m)) nil)]
                                (list 'var key value))

                              (= "ClassMethod" (:type m))
                              (let [key (translate-node (:key m))
                                    params (translate-args (:params m))
                                    body (translate-node (:body m))]
                                (list key params body))

                              :else
                              (let [kind (:kind m)
                                    key (translate-node (:key m))
                                    value (:value m)
                                    params (translate-args (:params value))
                                    body (translate-node (:body value))]
                                (list key params body))))
                      body)]
    (concat (list 'defclass id (if super-class [super-class] []))
            methods)))

(defmethod translate-node "TryStatement" [node]
  (let [block (translate-node (:block node))
        handler (:handler node)
        finalizer (:finalizer node)]
    (concat (list 'try block)
            (if handler
              (list (list 'catch (translate-node (:param handler))
                          (translate-node (:body handler))))
              nil)
            (if finalizer
              (list (list 'finally (translate-node finalizer)))
              nil))))

(defmethod translate-node "ThrowStatement" [node]
  (list 'throw (translate-node (:argument node))))

(defmethod translate-node "UpdateExpression" [node]
  (let [op (:operator node)
        arg (translate-node (:argument node))
        prefix (:prefix node)]
    (list (keyword op) arg)))

(defmethod translate-node "ImportDeclaration" [node]
  (let [source (:value (:source node))
        specifiers (:specifiers node)
        default-spec (first (filter #(= "ImportDefaultSpecifier" (:type %)) specifiers))
        named-specs (filter #(= "ImportSpecifier" (:type %)) specifiers)
        ns-spec (first (filter #(= "ImportNamespaceSpecifier" (:type %)) specifiers))]
    (concat (list 'import source)
            (if default-spec
              [:default (translate-node (:local default-spec))]
              [])
            (if (seq named-specs)
              [:named (apply hash-map (mapcat (fn [s]
                                                [(translate-node (:imported s))
                                                 (translate-node (:local s))])
                                              named-specs))]
              [])
            (if ns-spec
              [:as (translate-node (:local ns-spec))]
              []))))

(defmethod translate-node "ExportAllDeclaration" [node]
  (if (= "type" (:exportKind node))
    nil
    (list 'export :all (:value (:source node)))))

(defmethod translate-node "ExportDefaultDeclaration" [node]
  (list 'export :default (translate-node (:declaration node))))

(defmethod translate-node "ExportNamedDeclaration" [node]
  (if (= "type" (:exportKind node))
    nil
    (let [declaration (:declaration node)]
      (if declaration
        (if-let [trans (translate-node declaration)]
          (list 'export trans)
          nil)
        (list 'export (mapv translate-node (:specifiers node)))))))

(defmethod translate-node "ExportSpecifier" [node]
  [(translate-node (:local node)) (translate-node (:exported node))])

(defmethod translate-node "ArrayPattern" [node]
  (mapv translate-node (:elements node)))

(defmethod translate-node "ObjectPattern" [node]
  (let [props (:properties node)
        rest-prop (first (filter #(= "RestElement" (:type %)) props))
        other-props (remove #(= "RestElement" (:type %)) props)
        map-form (apply hash-map (mapcat translate-node other-props))]
    (if rest-prop
      (assoc map-form :& (translate-node (:argument rest-prop)))
      map-form)))

(defmethod translate-node "ObjectProperty" [node]
  (let [key (translate-node (:key node))
        value (translate-node (:value node))]
    [key value]))

(defmethod translate-node "SwitchStatement" [node]
  (let [disc (translate-node (:discriminant node))
        cases (mapcat translate-node (:cases node))]
    (apply list 'case disc cases)))

(defmethod translate-node "SwitchCase" [node]
  (let [test (if (:test node) (translate-node (:test node)) 'default)
        cons (cons 'do (mapv translate-node (:consequent node)))]
    [test cons]))

(defmethod translate-node "TSInterfaceDeclaration" [_] nil)
(defmethod translate-node "TSTypeAliasDeclaration" [_] nil)
(defmethod translate-node "TSTypeAnnotation" [_] nil)
(defmethod translate-node "TSTypeParameterInstantiation" [_] nil)

(defmethod translate-node "TSAsExpression" [node]
  (translate-node (:expression node)))

(defmethod translate-node "TSTypeAssertion" [node]
  (translate-node (:expression node)))

(defmethod translate-node "TSNonNullExpression" [node]
  (translate-node (:expression node)))

(defmethod translate-node "TSQualifiedName" [node]
  (symbol (str (translate-node (:left node)) "." (translate-node (:right node)))))

(defmethod translate-node "SpreadElement" [node]
  (list '... (translate-node (:argument node))))

(defmethod translate-node "JSXElement" [node]
  (let [opening (:openingElement node)
        tag (translate-node (:name opening))

        process-attr (fn [attr]
                       (if (= "JSXSpreadAttribute" (:type attr))
                         {:spread (translate-node (:argument attr))}
                         {:name (keyword (:name (:name attr)))
                          :value (if (:value attr)
                                   (translate-node (:value attr))
                                   true)}))

        raw-attrs (map process-attr (:attributes opening))
        regular-attrs (filter :name raw-attrs)
        spreads (map :spread (filter :spread raw-attrs))

        attrs-map (reduce (fn [m item]
                            (assoc m (:name item) (:value item)))
                          {}
                          regular-attrs)

        final-attrs (if (seq spreads)
                      (assoc attrs-map :& (if (= 1 (count spreads))
                                            (first spreads)
                                            (vec spreads)))
                      attrs-map)

        children (->> (:children node)
                      (mapv translate-node)
                      (remove (fn [n] (or (nil? n) (and (string? n) (str/blank? n)))))
                      (vec))]
    (apply list tag final-attrs children)))

(defmethod translate-node "JSXFragment" [node]
  (let [children (->> (:children node)
                      (mapv translate-node)
                      (remove (fn [n] (or (nil? n) (and (string? n) (str/blank? n)))))
                      (vec))]
    (apply list 'React.Fragment {} children)))

(defmethod translate-node "JSXEmptyExpression" [_] nil)

(defmethod translate-node "JSXText" [node]
  (:value node))

(defmethod translate-node "JSXExpressionContainer" [node]
  (translate-node (:expression node)))

(defmethod translate-node "JSXIdentifier" [node]
  (:name node))

(defmethod translate-node "JSXMemberExpression" [node]
  (list '. (translate-node (:object node)) (translate-node (:property node))))

(defmethod translate-node "ThisExpression" [_]
  'this)

(defn translate-import-entry [node]
  (let [source (:value (:source node))
        specifiers (:specifiers node)
        default-spec (first (filter #(= "ImportDefaultSpecifier" (:type %)) specifiers))
        named-specs (filter #(= "ImportSpecifier" (:type %)) specifiers)
        ns-spec (first (filter #(= "ImportNamespaceSpecifier" (:type %)) specifiers))]
    (vec
     (concat [source]
             (if ns-spec
               [:as (translate-node (:local ns-spec))]
               [])
             (if default-spec
               [:default (translate-node (:local default-spec))]
               [])
             (if (seq named-specs)
               [:named (apply hash-map (mapcat (fn [s]
                                                 [(translate-node (:imported s))
                                                  (translate-node (:local s))])
                                               named-specs))]
               [])))))

(defn translate-file [node ns-name]
  (let [program (if (= "File" (:type node)) (:program node) node)
        body (:body program)
        imports (filter #(= "ImportDeclaration" (:type %)) body)
        others (remove #(= "ImportDeclaration" (:type %)) body)

        import-entries (mapv translate-import-entry imports)
        other-forms (->> (mapv translate-node others)
                         (remove nil?)
                         vec)

        script-config (cond-> {}
                        (seq import-entries) (assoc :import import-entries))]

    (list (list 'ns ns-name
                '(:require [std.lang :as l]
                           [std.lib :as h]))

          (apply list 'l/script :js
                 script-config
                 other-forms))))
