(ns code.translate.js-dsl-test
  (:use code.test)
  (:require [code.translate.js-dsl :as sut]))

(fact "translates literals"
  (sut/translate-node {:type "NumericLiteral" :value 1})
  => 1

  (sut/translate-node {:type "StringLiteral" :value "hello"})
  => "hello"

  (sut/translate-node {:type "BooleanLiteral" :value true})
  => true

  (sut/translate-node {:type "NullLiteral"})
  => nil)

(fact "translates identifiers"
  (sut/translate-node {:type "Identifier" :name "x"})
  => 'x)

(fact "translates binary expressions"
  (sut/translate-node {:type "BinaryExpression"
                       :operator "+"
                       :left {:type "Identifier" :name "a"}
                       :right {:type "NumericLiteral" :value 1}})
  => '(+ a 1))

(fact "translates assignment"
  (sut/translate-node {:type "AssignmentExpression"
                       :operator "="
                       :left {:type "Identifier" :name "x"}
                       :right {:type "NumericLiteral" :value 1}})
  => '(:= x 1))

(fact "translates function declaration"
  (sut/translate-node {:type "FunctionDeclaration"
                       :id {:type "Identifier" :name "foo"}
                       :params [{:type "Identifier" :name "a"}]
                       :body {:type "BlockStatement"
                              :body [{:type "ReturnStatement"
                                      :argument {:type "Identifier" :name "a"}}]}})
  => '(defn foo [a] (do (return a))))

(fact "translates call expression"
  (sut/translate-node {:type "CallExpression"
                       :callee {:type "Identifier" :name "foo"}
                       :arguments [{:type "NumericLiteral" :value 1}]})
  => '(foo 1))

(fact "translates member expression"
  (sut/translate-node {:type "MemberExpression"
                       :object {:type "Identifier" :name "obj"}
                       :property {:type "Identifier" :name "prop"}
                       :computed false})
  => '(. obj prop)

  (sut/translate-node {:type "MemberExpression"
                       :object {:type "Identifier" :name "obj"}
                       :property {:type "StringLiteral" :value "prop"}
                       :computed true})
  => '(get obj "prop"))

(fact "translates template literal"
  (sut/translate-node {:type "TemplateLiteral"
                       :quasis [{:type "TemplateElement" :value {:raw "a"}}
                                {:type "TemplateElement" :value {:raw "b"}}]
                       :expressions [{:type "Identifier" :name "x"}]})
  ;; This might need a specific macro or just string concatenation
  ;; For now, let's assume a simple list form or string interpolation if supported
  ;; But standard JS DSL might not have a direct template literal equivalent other than str
  => '(str "a" x "b"))

(fact "translates class declaration"
  (sut/translate-node {:type "ClassDeclaration"
                       :id {:type "Identifier" :name "MyClass"}
                       :superClass {:type "Identifier" :name "Base"}
                       :body {:type "ClassBody"
                              :body [{:type "MethodDefinition"
                                      :kind "constructor"
                                      :key {:type "Identifier" :name "constructor"}
                                      :value {:type "FunctionExpression"
                                              :params [{:type "Identifier" :name "a"}]
                                              :body {:type "BlockStatement"
                                                     :body []}}}]}})
  => '(defclass MyClass [Base]
        (constructor [a] (do))))

(fact "translates try statement"
  (sut/translate-node {:type "TryStatement"
                       :block {:type "BlockStatement" :body []}
                       :handler {:type "CatchClause"
                                 :param {:type "Identifier" :name "e"}
                                 :body {:type "BlockStatement" :body []}}})
  => '(try (do) (catch e (do))))

(fact "translates throw statement"
  (sut/translate-node {:type "ThrowStatement"
                       :argument {:type "NewExpression"
                                  :callee {:type "Identifier" :name "Error"}
                                  :arguments [{:type "StringLiteral" :value "oops"}]}})
  => '(throw (new Error "oops")))

(fact "translates update expression"
  (sut/translate-node {:type "UpdateExpression"
                       :operator "++"
                       :argument {:type "Identifier" :name "i"}
                       :prefix true})
  => '(:++ i)

  (sut/translate-node {:type "UpdateExpression"
                       :operator "--"
                       :argument {:type "Identifier" :name "i"}
                       :prefix false})
  => '(:-- i))

(fact "translates import declaration"
  (sut/translate-node {:type "ImportDeclaration"
                       :source {:type "StringLiteral" :value "lib"}
                       :specifiers [{:type "ImportDefaultSpecifier"
                                     :local {:type "Identifier" :name "Lib"}}
                                    {:type "ImportSpecifier"
                                     :imported {:type "Identifier" :name "foo"}
                                     :local {:type "Identifier" :name "bar"}}]})
  ;; This is tricky as JS DSL might handle imports differently (e.g. via metadata or specific forms)
  ;; For now, let's map to a `import` form if it exists, or just a raw representation
  => '(import "lib" :default Lib :named {foo bar}))

(fact "translates export named declaration"
  (sut/translate-node {:type "ExportNamedDeclaration"
                       :declaration {:type "VariableDeclaration"
                                     :kind "const"
                                     :declarations [{:type "VariableDeclarator"
                                                     :id {:type "Identifier" :name "x"}
                                                     :init {:type "NumericLiteral" :value 1}}]}})
  => '(export (var* :const x 1)))

