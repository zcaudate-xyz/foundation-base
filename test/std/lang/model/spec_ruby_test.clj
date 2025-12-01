(ns std.lang.model.spec-ruby-test
  (:use code.test)
  (:require [std.lang.model.spec-ruby :as spec-ruby]
            [std.lang :as l]
            [std.lib :as h]))

(fact "Ruby Basic Emit"
  (l/emit-script
   '(do
      (var a 1)
      (var b 2)
      (puts (+ a b)))
   {:lang :ruby})
  => "a = 1\n\nb = 2\n\nputs (a + b)\n")

(fact "Ruby Control Flow"
  (l/emit-script
   '(if true
      (puts "true")
      (puts "false"))
   {:lang :ruby})
  => "if true\n  puts \"true\"\n\nend\nelse\n  puts \"false\"\n\nend")

(fact "Ruby Functions"
  (l/emit-script
   '(defn add [a b]
      (return (+ a b)))
   {:lang :ruby})
  => "def add(a, b)\nreturn a + b\n\nend")

(fact "Ruby Arrays and Maps"
  (l/emit-script
   '(do
      (var arr [1 2 3])
      (var m {:a 1 :b 2}))
   {:lang :ruby})
  => "arr = [1,2,3]\n\nm = {:a => 1, :b => 2}\n")

(fact "Ruby String Operations"
  (l/emit-script
   '(do
      (var s "hello")
      (puts (. s (upcase))))
   {:lang :ruby})
  => "s = \"hello\"\n\nputs s.upcase()\n")

(fact "Ruby XTalk Support"
  (l/emit-script
   '(do
      (x:print "hello")
      (x:cat "a" "b"))
   {:lang :ruby})
  => "puts \"hello\"\n\n\"a\" + \"b\"\n")
