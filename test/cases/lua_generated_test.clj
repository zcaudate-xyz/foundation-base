(ns cases.lua-generated-test
  (:use code.test)
  (:require [std.lang :as l]))

(fact "Lua Emitter: Primitives and Data Types"
  (l/emit-as :lua ['123]) => "123"
  (l/emit-as :lua ['3.14]) => "3.14"
  (l/emit-as :lua ['"hello world"]) => "\"hello world\""
  (l/emit-as :lua ['true]) => "true"
  (l/emit-as :lua ['false]) => "false"
  (l/emit-as :lua ['nil]) => "nil"
  (l/emit-as :lua [[':my-keyword]]) => "\"my-keyword\"")

(fact "Lua Emitter: Operators"
  (l/emit-as :lua ['(+ 1 2)]) => "1 + 2"
  (l/emit-as :lua ['(- 10 5)]) => "10 - 5"
  (l/emit-as :lua ['(* 2 3)]) => "2 * 3"
  (l/emit-as :lua ['(/ 10 2)]) => "10 / 2"
  (l/emit-as :lua ['(mod 10 3)]) => "10 % 3"
  (l/emit-as :lua ['(== a b)]) => "a == b"
  (l/emit-as :lua ['(!= a b)]) => "a ~= b"
  (l/emit-as :lua ['(and x y)]) => "x and y"
  (l/emit-as :lua ['(or x y)]) => "x or y"
  (l/emit-as :lua ['(not x)]) => "not x"
  (l/emit-as :lua ['(cat "a" "b")]) => "\"a\" .. \"b\""
  (l/emit-as :lua ['(len my-table)]) => "#my_table")

(fact "Lua Emitter: Variables and Assignment"
  (l/emit-as :lua ['(var a 10)]) => "local a = 10"
  (l/emit-as :lua ['(var b)]) => "local b"
  (l/emit-as :lua ['(:= a 20)]) => "a = 20"
  (l/emit-as :lua ['(var a b (:= c 1))]) => "local a, b, c = 1")

(fact "Lua Emitter: Data Structures (Tables)"
  (l/emit-as :lua ['[1 2 3]]) => "{1,2,3}"
  (l/emit-as :lua ['{:a 1 :b "two"}]) => "{[a]=1,[b]\"two\"}"
  (l/emit-as :lua ['(. my-table "key")]) => "my_table[\"key\"]"
  (l/emit-as :lua ['(:= (. t "x") 100)]) => "t[\"x\"] = 100")

(fact "Lua Emitter: Control Flow"
  (l/emit-as :lua ['(if condition (do-this))])
  => "if condition then\n  do_this()\nend"

  (l/emit-as :lua ['(if x (return 1) (return 2))])
  => "if x then\n  return 1\nelse\n  return 2\nend"

  (l/emit-as :lua ['(while true (print i))])
  => "while true do\n  print(i)\nend"

  (l/emit-as :lua ['(for [i 1 10] (print i))])
  => "for i=1,10 do\n  print(i)\nend"

  (l/emit-as :lua ['(for [v arr] (print v))])
  => "for i=1,#arr do\n  local v = arr[i]\n  print(v)\nend")

(fact "Lua Emitter: Functions"
  (l/emit-as :lua ['(defn my-add [a b] (return (+ a b)))])
  => "function my_add(a,b)\n  return a + b\nend"

  (l/emit-as :lua ['(var f (fn [x] (return (* x x))))])
  => "local f = function(x)\n  return x * x\nend"

  (l/emit-as :lua ['(my-add 1 2)]) => "my_add(1,2)")