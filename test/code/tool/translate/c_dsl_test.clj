(ns code.tool.translate.c-dsl-test
  (:use code.test)
  (:require [code.tool.translate.c-dsl :as c-dsl]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.model.spec-c :as c]))

(fact "Translate basic function declaration"
  (let [ast {:kind "TranslationUnitDecl"
             :inner [{:kind "FunctionDecl"
                      :name "add"
                      :type {:qualType "int (int, int)"}
                      :inner [{:kind "ParmVarDecl" :name "a" :type {:qualType "int"}}
                              {:kind "ParmVarDecl" :name "b" :type {:qualType "int"}}
                              {:kind "CompoundStmt"
                               :inner [{:kind "ReturnStmt"
                                        :inner [{:kind "BinaryOperator"
                                                 :opcode "+"
                                                 :inner [{:kind "ImplicitCastExpr"
                                                          :inner [{:kind "DeclRefExpr"
                                                                   :referencedDecl {:name "a"}}]}
                                                         {:kind "ImplicitCastExpr"
                                                          :inner [{:kind "DeclRefExpr"
                                                                   :referencedDecl {:name "b"}}]}]}]}]}]}]}]
    (c-dsl/translate-node ast)
    => '[(defn add [int a int b]
           (return (+ a b)))]))

(fact "Translate variable declaration"
  (let [ast {:kind "CompoundStmt"
             :inner [{:kind "VarDecl"
                      :name "x"
                      :type {:qualType "int"}
                      :inner [{:kind "IntegerLiteral" :value "42"}]}]}]
    (c-dsl/translate-node ast)
    => '[(var x 42)]))

(fact "Translate control flow"
  (let [ast {:kind "CompoundStmt"
             :inner [{:kind "IfStmt"
                      :inner [{:kind "BinaryOperator" :opcode ">"
                               :inner [{:kind "DeclRefExpr" :referencedDecl {:name "x"}}
                                       {:kind "IntegerLiteral" :value "0"}]}
                              {:kind "CompoundStmt" :inner []}
                              {:kind "CompoundStmt" :inner []}]}]}]
    (c-dsl/translate-node ast)
    => '[(if (> x 0) [] [])]))

(fact "Translate struct definition"
  (let [ast {:kind "RecordDecl"
             :tagUsed "struct"
             :name "Point"
             :inner [{:kind "FieldDecl" :name "x" :type {:qualType "int"}}
                     {:kind "FieldDecl" :name "y" :type {:qualType "int"}}]}]
    (c-dsl/translate-node ast)
    => '(struct Point [int x int y])))

(fact "Translate enum definition"
  (let [ast {:kind "EnumDecl"
             :name "Color"
             :inner [{:kind "EnumConstantDecl" :name "RED"}
                     {:kind "EnumConstantDecl" :name "GREEN"}
                     {:kind "EnumConstantDecl" :name "BLUE"}]}]
    (c-dsl/translate-node ast)
    => '(enum Color (RED GREEN BLUE))))

(fact "Translate typedef"
  (let [ast {:kind "TypedefDecl"
             :name "MyInt"
             :underlyingType "int"}]
    (c-dsl/translate-node ast)
    => '(typedef int MyInt)))

(fact "Translate member access"
  (let [ast {:kind "MemberExpr"
             :name "x"
             :isArrow false
             :inner [{:kind "DeclRefExpr" :referencedDecl {:name "p"}}]}]
    (c-dsl/translate-node ast)
    => '(. p x))
  (let [ast {:kind "MemberExpr"
             :name "x"
             :isArrow true
             :inner [{:kind "DeclRefExpr" :referencedDecl {:name "p"}}]}]
    (c-dsl/translate-node ast)
    => '(-> p x)))

(fact "Translate cast"
  (let [ast {:kind "CStyleCastExpr"
             :type {:qualType "double"}
             :inner [{:kind "IntegerLiteral" :value "1"}]}]
    (c-dsl/translate-node ast)
    => '(cast double 1)))


^{:refer code.tool.translate.c-dsl/translate-node :added "4.1"}
(fact "translates various C AST nodes to DSL"
  (c-dsl/translate-node {:kind "IntegerLiteral" :value "123"})
  => 123
  
  (c-dsl/translate-node {:kind "FloatingLiteral" :value "12.34"})
  => 12.34
  
  (c-dsl/translate-node {:kind "StringLiteral" :value "\"hello\""})
  => "\"hello\""

  (c-dsl/translate-node {:kind "ReturnStmt" :inner []})
  => '(return)

  (c-dsl/translate-node {:kind "UnaryOperator" :opcode "-" :inner [{:kind "IntegerLiteral" :value "1"}]})
  => '(- 1)

  (c-dsl/translate-node {:kind "UnaryOperator" :opcode "++" :isPostfix true :inner [{:kind "DeclRefExpr" :referencedDecl {:name "i"}}]})
  => '(:++ i)
  
  (c-dsl/translate-node {:kind "WhileStmt" :inner [{:kind "IntegerLiteral" :value "1"}
                                                  {:kind "CompoundStmt" :inner []}]})
  => '(while 1 [])

  (c-dsl/translate-node {:kind "CallExpr" :inner [{:kind "DeclRefExpr" :referencedDecl {:name "foo"}}
                                                 {:kind "IntegerLiteral" :value "1"}
                                                 {:kind "IntegerLiteral" :value "2"}]})
  => '(foo 1 2)

  (c-dsl/translate-node {:kind "ArraySubscriptExpr" :inner [{:kind "DeclRefExpr" :referencedDecl {:name "arr"}}
                                                           {:kind "IntegerLiteral" :value "0"}]})
  => '(get arr 0))

^{:refer code.tool.translate.c-dsl/translate-args :added "4.1"}
(fact "translates a list of arguments/nodes"
  (c-dsl/translate-args [{:kind "IntegerLiteral" :value "1"}
                         {:kind "IntegerLiteral" :value "2"}])
  => [1 2])
