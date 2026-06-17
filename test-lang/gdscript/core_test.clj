(ns gdscript.core-test
  (:require [hara.lang :as l]
            [hara.model.spec-gdscript])
  (:use code.test))

(fact "emits basic GDScript syntax"
  (l/emit-as :gdscript '[(:= a 1)])
  => "a = 1"

  (l/emit-as :gdscript '[(+ 1 2)])
  => "1 + 2"

  (l/emit-as :gdscript '[(var x 10)])
  => "var x = 10"

  (l/emit-as :gdscript '[(defn add [a b] (return (+ a b)))])
  => "func add(a,b):\n  return a + b"

  (l/emit-as :gdscript '[(if (> a 0) (return 1) (return 2))])
  => "if a > 0:\n  return 1\nelse:\n  return 2"

  (l/emit-as :gdscript '[(for [i :in (range 10)] (print i))])
  => "for i in range(10):\n  print(i)")

(fact "emits xtalk host bindings"
  (l/emit-as :gdscript '[(x:len [1 2 3])])
  => "[1,2,3].size()"

  (l/emit-as :gdscript '[(x:cat "hello" " " "world")])
  => "\"hello\" + \" \" + \"world\""

  (l/emit-as :gdscript '[(x:arr-push arr 4)])
  => "arr.append(4)")
