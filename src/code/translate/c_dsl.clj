(ns code.translate.c-dsl
  (:require [std.lib :as h]
            [clojure.string :as str]))

(defmulti translate-node :kind)

(defn translate-args [args]
  (mapv translate-node args))

(defmethod translate-node :default [node]
  (h/error "Unknown node type" {:type (:kind node) :node node}))

(defmethod translate-node nil [_]
  nil)

(defmethod translate-node "TranslationUnitDecl" [node]
  (mapv translate-node (:inner node)))

(defmethod translate-node "FunctionDecl" [node]
  (let [name (:name node)
        ret-type (get-in node [:type :qualType]) ;; Simplified
        inner (:inner node)
        params (filter #(= "ParmVarDecl" (:kind %)) inner)
        body (first (filter #(= "CompoundStmt" (:kind %)) inner))]
    (if body
      (concat (list 'defn (symbol name) (vec (mapcat translate-node params)))
              (translate-node body))
      ;; Declaration only
      (list 'declare (symbol name) (vec (mapcat translate-node params))))))

(defmethod translate-node "ParmVarDecl" [node]
  (let [name (:name node)
        type (get-in node [:type :qualType])] ;; Simplified
    [(symbol type) (symbol name)]))

(defmethod translate-node "CompoundStmt" [node]
  (mapv translate-node (:inner node)))

(defmethod translate-node "ReturnStmt" [node]
  (let [inner (:inner node)]
    (if (seq inner)
      (list 'return (translate-node (first inner)))
      (list 'return))))

(defmethod translate-node "VarDecl" [node]
  (let [name (:name node)
        type (get-in node [:type :qualType])
        init (:inner node)]
    (if (seq init)
      (list 'var (symbol name) (translate-node (first init))) ;; Assuming init is the first child
      (list 'var (symbol name)))))

(defmethod translate-node "IntegerLiteral" [node]
  (let [val (:value node)]
    (if (string? val) (read-string val) val)))

(defmethod translate-node "FloatingLiteral" [node]
  (let [val (:value node)]
    (if (string? val) (read-string val) val)))

(defmethod translate-node "CharacterLiteral" [node]
  (let [val (:value node)]
    (if (number? val) (char val) val))) ;; Clang might give int code

(defmethod translate-node "StringLiteral" [node]
  (:value node))

(defmethod translate-node "BinaryOperator" [node]
  (let [opcode (:opcode node)
        left (first (:inner node))
        right (second (:inner node))]
    (list (symbol opcode) (translate-node left) (translate-node right))))

(defmethod translate-node "UnaryOperator" [node]
  (let [opcode (:opcode node)
        operand (first (:inner node))
        postfix? (:isPostfix node)] ;; Clang AST usually has isPostfix boolean
    (if postfix?
      (list (keyword opcode) (translate-node operand)) ;; Use keyword for postfix to distinguish if needed, or consistent with js-dsl
      (list (symbol opcode) (translate-node operand)))))

(defmethod translate-node "DeclRefExpr" [node]
  (symbol (get-in node [:referencedDecl :name])))

(defmethod translate-node "ImplicitCastExpr" [node]
  ;; Skip implicit casts for now, just translate the inner expression
  (translate-node (first (:inner node))))

(defmethod translate-node "CStyleCastExpr" [node]
  (let [type (get-in node [:type :qualType])
        expr (first (:inner node))]
    (list 'cast (symbol type) (translate-node expr))))

(defmethod translate-node "ParenExpr" [node]
  ;; In DSL, parens are implicit in structure, but if needed for precedence we might just return the inner
  (translate-node (first (:inner node))))

(defmethod translate-node "IfStmt" [node]
  (let [children (:inner node)
        cond (or (get node "cond") (get node :cond) (first children))
        then (or (get node "then") (get node :then) (second children))
        else (or (get node "else") (get node :else) (nth children 2 nil))]
    (if else
      (list 'if (translate-node cond) (translate-node then) (translate-node else))
      (list 'if (translate-node cond) (translate-node then)))))

(defmethod translate-node "WhileStmt" [node]
  (let [children (:inner node)
        cond (or (get node "cond") (get node :cond) (first children))
        body (or (get node "body") (get node :body) (second children))]
    (list 'while (translate-node cond) (translate-node body))))

(defmethod translate-node "ForStmt" [node]
  (let [inner (:inner node)
        init (or (get node "init") (get node :init) (nth inner 0 nil))
        cond (or (get node "cond") (get node :cond) (nth inner 1 nil))
        inc  (or (get node "inc") (get node :inc)  (nth inner 2 nil))
        body (or (get node "body") (get node :body) (nth inner 3 nil))]
    (list 'for [(translate-node init) (translate-node cond) (translate-node inc)]
          (translate-node body))))

(defmethod translate-node "CallExpr" [node]
  (let [inner (:inner node)
        callee (first inner)
        args (rest inner)]
    (apply list (translate-node callee) (map translate-node args))))

(defmethod translate-node "ArraySubscriptExpr" [node]
  (let [inner (:inner node)
        base (first inner)
        idx (second inner)]
    (list 'get (translate-node base) (translate-node idx))))

(defmethod translate-node "MemberExpr" [node]
  (let [base (first (:inner node))
        member-name (:name node) ;; Or referencedDecl.name
        is-arrow (:isArrow node)]
    (if is-arrow
      (list '-> (translate-node base) (symbol member-name))
      (list '. (translate-node base) (symbol member-name)))))

(defmethod translate-node "RecordDecl" [node]
  (let [name (:name node)
        kind (:tagUsed node) ;; "struct" or "union" or "class"
        fields (filter #(= "FieldDecl" (:kind %)) (:inner node))]
    (list (symbol kind) (symbol name)
          (vec (mapcat (fn [f]
                         [(symbol (get-in f [:type :qualType]))
                          (symbol (:name f))])
                       fields)))))

(defmethod translate-node "EnumDecl" [node]
  (let [name (:name node)
        constants (filter #(= "EnumConstantDecl" (:kind %)) (:inner node))]
    (list 'enum (symbol name)
          (apply list (map (fn [c] (symbol (:name c))) constants)))))

(defmethod translate-node "TypedefDecl" [node]
  (let [name (:name node)
        underlying-type (get-in node [:type :qualType]) ;; or type -> type...
        type-str (or (:underlyingType node) (get-in node [:type :qualType]))]
    (list 'typedef (symbol type-str) (symbol name))))
