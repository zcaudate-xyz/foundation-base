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

(t/fact "translate select from where"
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
  => [[:select '* :from '[public.users] :where '(= id 1)]])

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
       ['id 'int4]
       ['name 'text]]])
