(ns code.tool.translate.python-dsl-test
  (:use code.test)
  (:require [code.tool.translate.python-dsl :as py-dsl]))

(fact "translate-node basics"
  (py-dsl/translate-node {:type "Name" :id "x"}) => 'x
  (py-dsl/translate-node {:type "Constant" :value 1}) => 1
  (py-dsl/translate-node {:type "Constant" :value "s"}) => "s")

(fact "translate-node operators"
  (py-dsl/translate-node {:type "BinOp" :op {:type "Add"} :left {:type "Name" :id "a"} :right {:type "Name" :id "b"}})
  => '(+ a b)
  
  (py-dsl/translate-node {:type "Compare" :left {:type "Name" :id "a"} :ops [{:type "Eq"}] :comparators [{:type "Name" :id "b"}]})
  => '(== a b))

(fact "translate-node control flow"
  (py-dsl/translate-node {:type "If" :test {:type "Constant" :value true} :body [] :orelse []})
  => '(if true (do))

  (py-dsl/translate-node {:type "While" :test {:type "Constant" :value true} :body [] :orelse []})
  => '(while true (do)))

(fact "translate-node functions"
  (py-dsl/translate-node {:type "FunctionDef" :name "foo" :args {:type "arguments" :args [] :posonlyargs [] :kwonlyargs [] :defaults [] :kw_defaults []} :body [] :decorator_list []})
  => '(defn foo [] (do)))

(fact "translate-node class"
  (py-dsl/translate-node {:type "ClassDef" :name "MyClass" :bases [] :keywords [] :body [] :decorator_list []})
  => '(defclass MyClass []))

^{:refer code.tool.translate.python-dsl/translate-node :added "4.1"}
(fact "translates various Python AST nodes to DSL"
  ^:hidden

  (py-dsl/translate-node {:type "Call" :func {:type "Name" :id "print"} :args [{:type "Constant" :value "hello"}] :keywords []})
  => '(print "hello")

  (py-dsl/translate-node {:type "List" :elts [{:type "Constant" :value 1} {:type "Constant" :value 2}]})
  => [1 2]

  (py-dsl/translate-node {:type "Dict" :keys [{:type "Constant" :value "k"}] :values [{:type "Constant" :value "v"}]})
  => {"k" "v"}
  
  (py-dsl/translate-node {:type "Return" :value {:type "Constant" :value 1}})
  => '(return 1)
  
  (py-dsl/translate-node {:type "Import" :names [{:name "math"}]})
  => '(do (import math))
  
  (py-dsl/translate-node {:type "ImportFrom" :module "math" :level 0 :names [{:name "sqrt"}]})
  => '(do (from math import sqrt)))
