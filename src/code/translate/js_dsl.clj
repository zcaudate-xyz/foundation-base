(ns code.translate.js-dsl
  (:require [std.lib :as h]
            [clojure.string :as str]))

(declare translate-node)

(defn translate-args [args]
  (mapv translate-node args))

(defn translate-node [node]
  (let [type (:type node)]
    (case type
      "File" (translate-node (:program node))
      "Program" (mapv translate-node (:body node))

      "Identifier" (symbol (:name node))

      "NumericLiteral" (:value node)
      "StringLiteral" (:value node)
      "BooleanLiteral" (:value node)
      "NullLiteral" nil

      "ExpressionStatement" (translate-node (:expression node))

      "BinaryExpression"
      (list (symbol (:operator node))
            (translate-node (:left node))
            (translate-node (:right node)))

      "AssignmentExpression"
      (let [op (:operator node)]
        (if (= op "=")
          (list ':=
                (translate-node (:left node))
                (translate-node (:right node)))
          (list (symbol op)
                (translate-node (:left node))
                (if (:right node)
                  (translate-node (:right node))
                  nil))))

      "LogicalExpression"
      (list (symbol (:operator node))
            (translate-node (:left node))
            (translate-node (:right node)))

      "ConditionalExpression"
      (list 'if
            (translate-node (:test node))
            (translate-node (:consequent node))
            (translate-node (:alternate node)))

      "UnaryExpression"
      (list (symbol (:operator node))
            (translate-node (:argument node)))

      "CallExpression"
      (let [callee (translate-node (:callee node))
            args   (translate-args (:arguments node))]
        (if (and (seq? callee) (= '. (first callee)))
          (apply list (concat callee args))
          (apply list callee args)))

      "MemberExpression"
      (let [obj (translate-node (:object node))
            prop (translate-node (:property node))
            computed (:computed node)]
        (if computed
          (list 'get obj prop)
          (list '. obj (if (symbol? prop) prop (list 'quote prop)))))

      "VariableDeclaration"
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
                      declarations))))

      "FunctionDeclaration"
      (let [id (translate-node (:id node))
            params (translate-args (:params node))
            body (translate-node (:body node))]
        (list 'defn id params body))

      "ArrowFunctionExpression"
      (let [params (translate-args (:params node))
            body (translate-node (:body node))
            async (:async node)]
        (cond-> (if (= "BlockStatement" (:type (:body node)))
                  (list 'fn params (translate-node (:body node)))
                  (list 'fn params (list 'return body)))
          async (with-meta {:async true})))

      "ReturnStatement"
      (list 'return (translate-node (:argument node)))

      "BlockStatement"
      (cons 'do (translate-args (:body node)))

      "IfStatement"
      (let [test (translate-node (:test node))
            consequent (translate-node (:consequent node))
            alternate (if (:alternate node)
                        (translate-node (:alternate node))
                        nil)]
        (if alternate
          (list 'if test consequent alternate)
          (list 'if test consequent)))

      "ForStatement"
      (let [init (if (:init node) (translate-node (:init node)) nil)
            test (if (:test node) (translate-node (:test node)) nil)
            update (if (:update node) (translate-node (:update node)) nil)
            body (translate-node (:body node))]
        (list 'for (vector init test update) body))

      "WhileStatement"
      (let [test (translate-node (:test node))
            body (translate-node (:body node))]
        (list 'while test body))

      "ObjectExpression"
      (let [props (:properties node)]
        (apply hash-map
               (mapcat (fn [p]
                       [(if (:computed p)
                          (translate-node (:key p))
                          (let [key-node (:key p)
                                key-val (or (:name key-node) (:value key-node))]
                            (if (number? key-val)
                              key-val
                              (keyword key-val))))
                        (translate-node (:value p))])
                     props)))

      "ArrayExpression"
      (mapv translate-node (:elements node))

      "NewExpression"
      (let [callee (translate-node (:callee node))
            args   (translate-args (:arguments node))]
        (apply list 'new callee args))

      "TemplateLiteral"
      (let [quasis (:quasis node)
            expressions (:expressions node)
            parts (interleave (map (comp :raw :value) quasis)
                              (concat (map translate-node expressions) [nil]))]
        (apply list 'str (remove nil? parts)))

      "ClassDeclaration"
      (let [id (translate-node (:id node))
            super-class (if (:superClass node) (translate-node (:superClass node)) nil)
            body (:body (:body node))
            methods (mapv (fn [m]
                            (let [kind (:kind m)
                                  key (translate-node (:key m))
                                  params (translate-args (:params m))
                                  body (translate-node (:body m))]
                              (list key params body)))
                          body)]
        (concat (list 'defclass id (if super-class [super-class] []))
                methods))

      "TryStatement"
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
                  nil)))

      "ThrowStatement"
      (list 'throw (translate-node (:argument node)))

      "UpdateExpression"
      (let [op (:operator node)
            arg (translate-node (:argument node))
            prefix (:prefix node)]
        (list (keyword op) arg))

      "ImportDeclaration"
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
                  [])))

      "ExportNamedDeclaration"
      (let [declaration (:declaration node)]
        (if declaration
          (list 'export (translate-node declaration))
          (list 'export (mapv translate-node (:specifiers node)))))

      "ArrayPattern"
      (mapv translate-node (:elements node))

      "JSXElement"
      (let [opening (:openingElement node)
            tag (translate-node (:name opening))
            attrs (reduce (fn [m attr]
                            (assoc m
                                   (keyword (:name (:name attr)))
                                   (if (:value attr)
                                     (translate-node (:value attr))
                                     true)))
                          {}
                          (:attributes opening))
            children (->> (:children node)
                          (mapv translate-node)
                          (remove (fn [n] (and (string? n) (str/blank? n))))
                          (vec))]
        (apply list 'React.createElement tag attrs children))

      "JSXText"
      (:value node)

      "JSXExpressionContainer"
      (translate-node (:expression node))

      "JSXIdentifier"
      (:name node)

      "ThisExpression"
      'this

      ;; Fallback
      (h/error "Unknown node type" {:type type :node node}))))
