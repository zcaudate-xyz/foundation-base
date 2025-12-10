(ns code.tool.translate.js-dsl-test
  (:use code.test)
  (:require [code.tool.translate.js-dsl :as js-dsl]
            [std.lib :as h]))

(fact "translate-node basics"
  (js-dsl/translate-node {:type "Identifier" :name "foo"})
  => 'foo

  (js-dsl/translate-node {:type "NumericLiteral" :value 123})
  => 123
  
  (js-dsl/translate-node {:type "StringLiteral" :value "bar"})
  => "bar"
  
  (js-dsl/translate-node {:type "BooleanLiteral" :value true})
  => true
  
  (js-dsl/translate-node {:type "NullLiteral"})
  => nil)

(fact "translate-node operators"
  (js-dsl/translate-node {:type "BinaryExpression" :operator "+" :left {:type "Identifier" :name "a"} :right {:type "Identifier" :name "b"}})
  => '(+ a b)
  
  (js-dsl/translate-node {:type "AssignmentExpression" :operator "=" :left {:type "Identifier" :name "a"} :right {:type "NumericLiteral" :value 1}})
  => '(:= a 1))

(fact "translate-node function"
  (js-dsl/translate-node {:type "FunctionDeclaration" :id {:type "Identifier" :name "f"} :params [{:type "Identifier" :name "x"}] :body {:type "BlockStatement" :body []}})
  => '(defn.js f [x] (do)))

(fact "translate-node control flow"
  (js-dsl/translate-node {:type "IfStatement" :test {:type "BooleanLiteral" :value true} :consequent {:type "BlockStatement" :body []}})
  => '(if true (do)))

(fact "translate-node object and array"
  (js-dsl/translate-node {:type "ArrayExpression" :elements [{:type "NumericLiteral" :value 1}]})
  => [1]
  
  (js-dsl/translate-node {:type "ObjectExpression" :properties [{:type "ObjectProperty" :key {:type "Identifier" :name "a"} :value {:type "NumericLiteral" :value 1}}]})
  => {:a 1})

(fact "translate-node jsx"
  (js-dsl/translate-node
   {:type "JSXElement" 
    :openingElement {:name {:type "JSXIdentifier" :name "div"} :attributes []}
    :children []})
  => '(div {})
  
  (js-dsl/translate-node
   {:type "JSXElement" 
    :openingElement {:name {:type "JSXIdentifier" :name "span"} 
                     :attributes [{:type "JSXAttribute" :name {:name "class"} :value {:type "StringLiteral" :value "foo"}}]}
    :children [{:type "JSXText" :value "Hello"}]})
  => '(span {:class "foo"} "Hello"))

;; Preserved tests from previous iterations/code review suggestions if they existed in memory but not file
;; (Since I see no other tests in the file I restored, I assume these cover the basics)

^{:refer code.tool.translate.js-dsl/translate-node :added "4.1"}
(fact "translates various JS AST nodes to DSL"
  (js-dsl/translate-node {:type "ReturnStatement" :argument {:type "Identifier" :name "x"}})
  => '(return x)

  (js-dsl/translate-node {:type "ThisExpression"})
  => 'this
  
  ;; Add back missing coverage mentioned in review: ClassDeclaration
  (js-dsl/translate-node {:type "ClassDeclaration" 
                          :id {:type "Identifier" :name "MyClass"} 
                          :body {:type "ClassBody" :body []}})
  => '(defclass MyClass [])

  ;; Add back missing coverage: TryStatement
  (js-dsl/translate-node {:type "TryStatement"
                          :block {:type "BlockStatement" :body []}
                          :handler {:type "CatchClause" 
                                    :param {:type "Identifier" :name "e"}
                                    :body {:type "BlockStatement" :body []}}})
  => '(try (do) (catch e (do))))

^{:refer code.tool.translate.js-dsl/translate-args :added "4.1"}
(fact "translates list of argument nodes"

  (js-dsl/translate-args [{:type "Identifier" :name "a"} {:type "Identifier" :name "b"}])
  => ['a 'b])

^{:refer code.tool.translate.js-dsl/translate-import-entry :added "4.1"}
(fact "translates import declaration to import vector"

  (js-dsl/translate-import-entry {:source {:value "react"} :specifiers [{:type "ImportDefaultSpecifier" :local {:type "Identifier" :name "React"}}]})
  => ["react" :default 'React])

^{:refer code.tool.translate.js-dsl/translate-file :added "4.1"}
(fact "translates entire file structure"

  (js-dsl/translate-file {:type "File" :program {:body [{:type "VariableDeclaration" :kind "const" :declarations [{:id {:type "Identifier" :name "x"} :init {:type "NumericLiteral" :value 1}}]}]}} 'my.ns)
  => '((ns my.ns (:require [std.lang :as l] [std.lib :as h]))
       (l/script :js {})

       (var (x 1))))
