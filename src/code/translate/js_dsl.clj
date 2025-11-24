(ns code.translate.js-dsl
  (:require [std.lib :as h]))

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
        (apply list callee args))

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
        (cons (symbol kind)
              (mapv (fn [d]
                      (let [id (translate-node (:id d))]
                        (if (:init d)
                          (list id (translate-node (:init d)))
                          id)))
                    declarations)))

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

      ;; Fallback
      (h/error "Unknown node type" {:type type :node node}))))
