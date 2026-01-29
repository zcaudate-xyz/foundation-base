(ns code.tool.translate.pg-dsl
  (:require [std.lib :as h]
            [clojure.string :as str]))

(defn get-type [node]
  (cond (map? node) (name (first (keys node)))
        (vector? node) :list
        :else :value))

(defmulti translate-node
  "Translates a Postgres AST node to std.lang DSL"
  get-type)

(defn translate-list [nodes]
  (mapv translate-node nodes))

(defn intersperse-comma [nodes]
  (if (empty? nodes)
    []
    (->> (translate-list nodes)
         (interpose (symbol ","))
         vec)))

(defmethod translate-node :default [node]
  (if (map? node)
    (h/error "Unknown node type" {:type (get-type node) :node node})
    node))

(defmethod translate-node :list [node]
  (mapv translate-node node))

(defmethod translate-node :value [node]
  node)

;; Wrapper unwrapper
(defn unwrap [node]
  (if (and (map? node) (= 1 (count node)))
    (val (first node))
    node))

;;
;; Basic Types
;;

(defmethod translate-node "Integer" [node]
  (:ival (unwrap node)))

(defmethod translate-node "String" [node]
  (:str (unwrap node)))

(defmethod translate-node "Null" [node]
  nil)

;;
;; Statements
;;

(defmethod translate-node "SelectStmt" [node]
  (let [{:keys [distinctClause targetList fromClause whereClause
                groupClause havingClause sortClause limitCount limitOffset]} (unwrap node)

        out (cond-> [:select]
              distinctClause (conj :distinct)

              targetList (into (intersperse-comma targetList))

              fromClause (into (vec (cons :from (intersperse-comma fromClause))))

              whereClause (into [:where (translate-node whereClause)])

              groupClause (into (vec (concat [:group :by] (intersperse-comma groupClause))))

              havingClause (into [:having (translate-node havingClause)])

              sortClause (into (vec (concat [:order :by] (intersperse-comma sortClause))))

              limitCount (into [:limit (translate-node limitCount)])

              limitOffset (into [:offset (translate-node limitOffset)]))]
    out))

(defmethod translate-node "CreateStmt" [node]
  (let [{:keys [relation tableElts if_not_exists]} (unwrap node)]
    (vec (concat [:create :table]
                 (if if_not_exists [:if :not :exists] [])
                 [(translate-node relation)]
                 (if (seq tableElts)
                   (intersperse-comma tableElts)
                   [])))))

;;
;; Components
;;

(defmethod translate-node "ResTarget" [node]
  (let [{:keys [name val]} (unwrap node)
        val-trans (translate-node val)]
    (if name
      [val-trans :as (symbol name)]
      val-trans)))

(defmethod translate-node "RangeVar" [node]
  (let [{:keys [schemaname relname alias]} (unwrap node)
        base (if schemaname
               (symbol (str schemaname "." relname))
               (symbol relname))]
    (if alias
      [base (symbol (:aliasname (unwrap alias)))]
      base)))

(defmethod translate-node "TypeName" [node]
  (let [{:keys [names]} (unwrap node)
        type-parts (mapv translate-node names)]
    (symbol (str/join "." type-parts))))

(defmethod translate-node "ColumnDef" [node]
  (let [{:keys [colname typeName constraints]} (unwrap node)
        type-sym (translate-node typeName)]
    [(symbol colname) type-sym]))

(defmethod translate-node "SortBy" [node]
  (let [{:keys [node sortby_dir sortby_nulls]} (unwrap node)
        base (translate-node node)
        dir (case sortby_dir
              1 :asc
              2 :desc
              nil)
        nulls (case sortby_nulls
                1 :first
                2 :last
                nil)]
    (cond-> base
      dir (list dir)
      nulls (list :nulls nulls))))

;;
;; Expressions
;;

(def op-mapping
  {"~"  're
   "~*" 're:*
   "||" '||
   "->" :->
   "->>" :->>
   "#>>" :#>>})

(defmethod translate-node "A_Const" [node]
  (let [val (:val (unwrap node))]
    (translate-node val)))

(defmethod translate-node "ColumnRef" [node]
  (let [{:keys [fields]} (unwrap node)
        parts (mapv translate-node fields)]
    (symbol (str/join "." parts))))

(defmethod translate-node "A_Star" [_]
  '*)

(defmethod translate-node "A_Expr" [node]
  (let [{:keys [kind name lexpr rexpr]} (unwrap node)
        raw-op (translate-node (first name))
        op (get op-mapping raw-op (symbol raw-op))
        l (translate-node lexpr)
        r (translate-node rexpr)]
    (list op l r)))

(defmethod translate-node "BoolExpr" [node]
  (let [{:keys [boolop args]} (unwrap node)
        op (case boolop
             "AND_EXPR" 'and
             "OR_EXPR" 'or
             "NOT_EXPR" 'not)]
    (apply list op (translate-list args))))

(defmethod translate-node "NullTest" [node]
  (let [{:keys [arg nulltesttype]} (unwrap node)
        arg-trans (translate-node arg)]
    (case nulltesttype
      0 (list 'is arg-trans nil)
      1 (list 'is :not arg-trans nil))))

(defmethod translate-node "CaseExpr" [node]
  (let [{:keys [arg args defresult]} (unwrap node)
        implicit-arg (if arg (translate-node arg) nil)
        clauses (mapcat translate-node args)
        else-clause (if defresult [:else (translate-node defresult)] [])]
    (apply list 'case
           (concat (if implicit-arg [implicit-arg] [])
                   clauses
                   else-clause))))

(defmethod translate-node "CaseWhen" [node]
  (let [{:keys [expr result]} (unwrap node)]
    [(translate-node expr) (translate-node result)]))

(defmethod translate-node "FuncCall" [node]
  (let [{:keys [funcname args]} (unwrap node)
        names (mapv translate-node funcname)
        fn-sym (symbol (str/join "." names))
        args-trans (translate-list args)]
    (apply list fn-sym args-trans)))

(defmethod translate-node "TypeCast" [node]
  (let [{:keys [arg typeName]} (unwrap node)
        type-sym (translate-node typeName)
        final-type (if (and (symbol? type-sym) (not (str/includes? (str type-sym) ".")))
                     (keyword (str type-sym))
                     type-sym)]
    (list '++ (translate-node arg) final-type)))

(defmethod translate-node "JoinExpr" [node]
  (let [{:keys [jointype isNatural larg rarg usingClause quals]} (unwrap node)
        join-kw (case jointype
                  0 :join
                  1 :left-join
                  2 :full-join
                  3 :right-join
                  :join)

        l (translate-node larg)
        r (translate-node rarg)

        on-part (cond quals [:on (translate-node quals)]
                      usingClause [:using (translate-list usingClause)]
                      :else [])]
    (vec (concat [l join-kw r] on-part))))

(defmethod translate-node "SubLink" [node]
  (let [{:keys [subLinkType testexpr subselect]} (unwrap node)
        sub (translate-node subselect)
        test (if testexpr (translate-node testexpr) nil)]
    (case subLinkType
      "EXISTS_SUBLINK" (list 'exists sub)
      "ALL_SUBLINK"    (list (symbol "all") sub)
      "ANY_SUBLINK"    (list (symbol "any") sub)
      "EXPR_SUBLINK"   sub
      (list 'sublink subLinkType sub))))

;;
;; Entry Point
;;

(defn translate-stmt [stmt-map]
  (translate-node (:stmt stmt-map)))

(defn translate [json-ast]
  (mapv translate-stmt (:stmts json-ast)))
