(ns std.lang.model.spec-go-test
  (:require [std.lang.model.spec-go :as spec-go]
            [std.lang :as l]
            [std.lib :as h]
            [std.lang.base.emit :as emit]
            [code.test :as t]))

(t/fact "test go emission"
  (l/emit-as :go ['(var a 1)])
  => "var a = 1"

  (l/emit-as :go ['(defn ^{:- [:int]} hello [:int a] (return (+ a 1)))])
  => "func hello(a int)  int {\n  return a + 1\n}"

  (l/emit-as :go ['(defstruct Person [[name string] [age int]])])
  => "type Person struct {\n  name string\n  age int\n}"

  (l/emit-as :go ['(go (hello 1))])
  => "go hello(1)"

  (l/emit-as :go ['(defer (hello 1))])
  => "defer hello(1)"

  (l/emit-as :go ['(:& x)])
  => "&x"

  (l/emit-as :go ['(:* x)])
  => "*x"

  (l/emit-as :go ['(<- ch)])
  => "<-ch"

  (l/emit-as :go ['(<- ch 1)])
  => "ch <- 1"

  (l/emit-as :go ['(make (chan int))])
  => "make(chan int)"

  (l/emit-as :go ['[:> slice int]])
  => "[]int"

  (l/emit-as :go ['[:> map string int]])
  => "map[string]int"
)
