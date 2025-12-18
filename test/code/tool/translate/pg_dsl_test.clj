(ns code.tool.translate.pg-dsl-test
  (:require [code.test :as t]
            [code.tool.translate.pg-dsl :as sut]))

(t/fact "translate select"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList
        [{:ResTarget
          {:val {:A_Const {:val {:Integer {:ival 1}} :location 7}}
           :location 7}}],
        :limitOption "LIMIT_OPTION_DEFAULT",
        :op "SETOP_NONE"}}}]})
  => [[:select 1]])

(t/fact "translate select from where (with op mapping)"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList [{:ResTarget {:val {:ColumnRef {:fields [{:A_Star {}}]}}}}]
        :fromClause [{:RangeVar {:relname "users" :schemaname "public"}}]
        :whereClause {:A_Expr {:kind "AEXPR_OP"
                               :name [{:String {:str "="}}]
                               :lexpr {:ColumnRef {:fields [{:String {:str "id"}}]}}
                               :rexpr {:A_Const {:val {:Integer {:ival 1}}}}}}
        }}}]})
  => [[:select '* :from 'public.users :where '(= id 1)]])

(t/fact "translate create table"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:CreateStmt
       {:relation {:RangeVar {:relname "items" :schemaname "public"}}
        :tableElts [{:ColumnDef {:colname "id"
                                 :typeName {:TypeName {:names [{:String {:str "int4"}}]}}}}
                    {:ColumnDef {:colname "name"
                                 :typeName {:TypeName {:names [{:String {:str "text"}}]}}}}
                    ]}}}]})
  => [[:create :table 'public.items
       '[id int4] (symbol ",") '[name text]]])

(t/fact "translate select multi columns (comma)"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList [{:ResTarget {:val {:ColumnRef {:fields [{:String {:str "a"}}]}}}}
                     {:ResTarget {:val {:ColumnRef {:fields [{:String {:str "b"}}]}}}}
                     ]
        :fromClause [{:RangeVar {:relname "t"}}]}}}]})
  => [[:select 'a (symbol ",") 'b :from 't]])

(t/fact "translate IS NULL"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList [{:ResTarget {:val {:ColumnRef {:fields [{:String {:str "a"}}]}}}}]
        :fromClause [{:RangeVar {:relname "t"}}]
        :whereClause {:NullTest {:arg {:ColumnRef {:fields [{:String {:str "a"}}]}}
                                 :nulltesttype 0}} ;; IS NULL
        }}}]})
  => [[:select 'a :from 't :where '(is a nil)]])

(t/fact "translate CASE"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList [{:ResTarget {:val {:CaseExpr {:args [{:CaseWhen {:expr {:ColumnRef {:fields [{:String {:str "a"}}]}}
                                                                      :result {:A_Const {:val {:Integer {:ival 1}}}}}}]
                                                   :defresult {:A_Const {:val {:Integer {:ival 0}}}}}}}}]
        :fromClause [{:RangeVar {:relname "t"}}]}}}]})
  => [[:select '(case a 1 :else 0) :from 't]])

(t/fact "translate JOIN"
  (sut/translate
   {:version 170007,
    :stmts
    [{:stmt
      {:SelectStmt
       {:targetList [{:ResTarget {:val {:A_Star {}}}}]
        :fromClause [{:JoinExpr {:jointype 0
                                 :larg {:RangeVar {:relname "a"}}
                                 :rarg {:RangeVar {:relname "b"}}
                                 :quals {:A_Expr {:name [{:String {:str "="}}]
                                                  :lexpr {:ColumnRef {:fields [{:String {:str "a"}} {:String {:str "id"}}]}}
                                                  :rexpr {:ColumnRef {:fields [{:String {:str "b"}} {:String {:str "id"}}]}}}}}}]}}}]})
  => [[:select '* :from '[a :join b :on (= a.id b.id)]]])
