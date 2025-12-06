(ns code.translate.c-dsl-test
  (:require [code.translate.c-dsl :as c-dsl]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.model.spec-c :as c]
            [code.test :refer :all]))

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
