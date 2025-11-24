(ns code.translate.js-dsl-test
  (:use code.test)
  (:require [code.translate.js-dsl :as sut]
            [std.lib :as h]
            [std.lang :as l]
            [std.json :as json]))

(fact "translate literals"

  (sut/translate-node {:type "NumericLiteral" :value 1})
  => 1

  (sut/translate-node {:type "StringLiteral" :value "hello"})
  => "hello"

  (sut/translate-node {:type "BooleanLiteral" :value true})
  => true

  (sut/translate-node {:type "NullLiteral"})
  => nil)

(fact "translate identifier"

  (sut/translate-node {:type "Identifier" :name "foo"})
  => 'foo)

(fact "translate program"

  (sut/translate-node {:type "Program"
                       :body [{:type "NumericLiteral" :value 1}
                              {:type "Identifier" :name "x"}]})
  => [1 'x])

(fact "translate binary expression"
  (sut/translate-node {:type "BinaryExpression"
                       :operator "+"
                       :left {:type "NumericLiteral" :value 1}
                       :right {:type "NumericLiteral" :value 2}})
  => '(+ 1 2))

(fact "translate assignment expression"
  (sut/translate-node {:type "AssignmentExpression"
                       :operator "="
                       :left {:type "Identifier" :name "x"}
                       :right {:type "NumericLiteral" :value 1}})
  => '(:= x 1)

  (sut/translate-node {:type "AssignmentExpression"
                       :operator "+="
                       :left {:type "Identifier" :name "x"}
                       :right {:type "NumericLiteral" :value 1}})
  => '(+= x 1))

(fact "translate logical expression"
  (sut/translate-node {:type "LogicalExpression"
                       :operator "&&"
                       :left {:type "Identifier" :name "a"}
                       :right {:type "Identifier" :name "b"}})
  => '(&& a b))

(fact "translate conditional expression"
  (sut/translate-node {:type "ConditionalExpression"
                       :test {:type "BooleanLiteral" :value true}
                       :consequent {:type "NumericLiteral" :value 1}
                       :alternate {:type "NumericLiteral" :value 2}})
  => '(if true 1 2))

(fact "translate unary expression"
  (sut/translate-node {:type "UnaryExpression"
                       :operator "!"
                       :argument {:type "BooleanLiteral" :value true}})
  => '(! true))

(fact "translate call expression"
  (sut/translate-node {:type "CallExpression"
                       :callee {:type "Identifier" :name "foo"}
                       :arguments [{:type "NumericLiteral" :value 1}]})
  => '(foo 1))

(fact "translate member expression"
  (sut/translate-node {:type "MemberExpression"
                       :object {:type "Identifier" :name "foo"}
                       :property {:type "Identifier" :name "bar"}
                       :computed false})
  => '(. foo bar)

  (sut/translate-node {:type "MemberExpression"
                       :object {:type "Identifier" :name "foo"}
                       :property {:type "StringLiteral" :value "bar"}
                       :computed true})
  => '(get foo "bar"))

(fact "translate variable declaration"
  (sut/translate-node {:type "VariableDeclaration"
                       :kind "var"
                       :declarations [{:type "VariableDeclarator"
                                       :id {:type "Identifier" :name "x"}
                                       :init {:type "NumericLiteral" :value 1}}]})
  => '(var (x 1))

  ;; Test null initialization
  (sut/translate-node {:type "VariableDeclaration"
                       :kind "var"
                       :declarations [{:type "VariableDeclarator"
                                       :id {:type "Identifier" :name "x"}
                                       :init {:type "NullLiteral"}}]})
  => '(var (x nil)))

(fact "translate function declaration"
  (sut/translate-node {:type "FunctionDeclaration"
                       :id {:type "Identifier" :name "foo"}
                       :params [{:type "Identifier" :name "x"}]
                       :body {:type "BlockStatement"
                              :body [{:type "ReturnStatement"
                                      :argument {:type "Identifier" :name "x"}}]}})
  => '(defn foo [x] (do (return x))))

(fact "translate arrow function"
  (sut/translate-node {:type "ArrowFunctionExpression"
                       :params [{:type "Identifier" :name "x"}]
                       :body {:type "BlockStatement"
                              :body [{:type "ReturnStatement"
                                      :argument {:type "Identifier" :name "x"}}]}})
  => '(fn [x] (do (return x)))

  (sut/translate-node {:type "ArrowFunctionExpression"
                       :params [{:type "Identifier" :name "x"}]
                       :body {:type "Identifier" :name "x"}})
  => '(fn [x] (return x))

  (meta (sut/translate-node {:type "ArrowFunctionExpression"
                             :params []
                             :body {:type "BlockStatement" :body []}
                             :async true}))
  => {:async true})

(fact "translate if statement"
  (sut/translate-node {:type "IfStatement"
                       :test {:type "BooleanLiteral" :value true}
                       :consequent {:type "BlockStatement" :body []}
                       :alternate {:type "BlockStatement" :body []}})
  => '(if true (do) (do))

  (sut/translate-node {:type "IfStatement"
                       :test {:type "BooleanLiteral" :value true}
                       :consequent {:type "BlockStatement" :body []}})
  => '(if true (do)))

(fact "translate loop statements"
  (sut/translate-node {:type "WhileStatement"
                       :test {:type "BooleanLiteral" :value true}
                       :body {:type "BlockStatement" :body []}})
  => '(while true (do))

  (sut/translate-node {:type "ForStatement"
                       :init {:type "VariableDeclaration" :kind "let" :declarations [{:type "VariableDeclarator" :id {:type "Identifier" :name "i"} :init {:type "NumericLiteral" :value 0}}]}
                       :test {:type "BinaryExpression" :operator "<" :left {:type "Identifier" :name "i"} :right {:type "NumericLiteral" :value 10}}
                       :update {:type "AssignmentExpression" :operator "++" :left {:type "Identifier" :name "i"} :right nil}
                       :body {:type "BlockStatement" :body []}})
  => '(for [(let (i 0)) (< i 10) (++ i nil)] (do)))

(fact "translate object expression"
  (sut/translate-node {:type "ObjectExpression"
                       :properties [{:type "ObjectProperty"
                                     :key {:type "Identifier" :name "a"}
                                     :value {:type "NumericLiteral" :value 1}}
                                    {:type "ObjectProperty"
                                     :key {:type "StringLiteral" :value "b"}
                                     :value {:type "NumericLiteral" :value 2}}
                                    {:type "ObjectProperty"
                                     :key {:type "NumericLiteral" :value 1}
                                     :value {:type "NumericLiteral" :value 3}}]})
  => {:a 1 :b 2 1 3})

(fact "translate array expression"
  (sut/translate-node {:type "ArrayExpression"
                       :elements [{:type "NumericLiteral" :value 1}
                                  {:type "NumericLiteral" :value 2}]})
  => [1 2])

(fact "translate new expression"
  (sut/translate-node {:type "NewExpression"
                       :callee {:type "Identifier" :name "Date"}
                       :arguments []})
  => '(new Date))

(fact "translate from files"
  
  (let [read-ast (fn [file] (json/read (slurp (str "test-data/code.translate/" file))
                                       json/+keyword-mapper+))]
    (sut/translate-node (read-ast "math.json"))
    => '[(const (result (+ (* 2 3) 4)))]

    (sut/translate-node (read-ast "logic.json"))
    => '[(defn check [val] (do (if (> val 10) (do (return true)) (do (return false)))))]

    (sut/translate-node (read-ast "obj.json"))
    => '[(const (config {:host "localhost" :port 8080}))
         (. console log (. config host))]))
