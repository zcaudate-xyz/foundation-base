(ns std.lang.model.spec-ruby-test
  (:use code.test)
  (:require [std.lang.model.spec-ruby :as spec-ruby]
            [std.lang :as l]
            [std.lib :as h]))

(fact "Ruby Basic Emit"
  (l/emit-as :ruby
   '[(do
      (var a 1)
      (var b 2)
      (puts (+ a b)))])
  => "a = 1\n\nb = 2\n\nputs (a + b)\n")

(fact "Ruby Control Flow"
  (l/emit-as :ruby
   '[(if true
      (puts "true")
      (puts "false"))])
  => "if true\n  puts \"true\"\n\nend\nelse\n  puts \"false\"\n\nend")

(fact "Ruby Functions"
  (l/emit-as :ruby
   '[(defn add [a b]
      (return (+ a b)))])
  => "def add(a, b)\nreturn a + b\n\nend")

(fact "Ruby Arrays and Maps"
  (l/emit-as :ruby
   '[(do
      (var arr [1 2 3])
      (var m {:a 1 :b 2}))])
  => "arr = [1,2,3]\n\nm = {:a => 1, :b => 2}\n")

(fact "Ruby String Operations"
  (l/emit-as :ruby
   '[(do
      (var s "hello")
      (puts (. s (upcase))))])
  => "s = \"hello\"\n\nputs s.upcase()\n")

(fact "Ruby XTalk Support"
  (l/emit-as :ruby
   '[(do
      (x:print "hello")
      (x:cat "a" "b"))])
  => "puts \"hello\"\n\n\"a\" + \"b\"\n")


^{:refer std.lang.model.spec-ruby/ruby-symbol :added "4.1"}
(fact "emit ruby symbol"
  (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
  => ":a"
  (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
  => "a")

^{:refer std.lang.model.spec-ruby/ruby-fn :added "4.1"}
(fact "basic transform for ruby blocks"
  (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
  => '(fn.inner [a] (+ a 1)))

^{:refer std.lang.model.spec-ruby/ruby-defn :added "4.1"}
(fact "emit ruby function definition"
  (l/emit-as :ruby '[(defn add [a b] (return (+ a b)))])
  => "def add(a, b)\nreturn a + b\n\nend")

^{:refer std.lang.model.spec-ruby/ruby-var :added "4.1"}
(fact "emit ruby variable"
  (spec-ruby/ruby-var '(var a 1))
  => '(:= a 1))

^{:refer std.lang.model.spec-ruby/ruby-map :added "4.1"}
(fact "emit ruby hash"
  (l/emit-as :ruby '[{:a 1 :b 2}])
  => "{:a => 1, :b => 2}")
