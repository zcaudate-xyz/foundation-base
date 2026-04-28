(ns std.lang.model-annex.spec-ruby-test
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-ruby :as spec-ruby])
  (:use code.test))

(fact "Ruby Basic Emit"
  (l/emit-as :ruby
   '[(do
      (var a 1)
      (var b 2)
      (puts (+ a b)))])
  => "a = 1\nb = 2\nputs (a + b)")

(fact "Ruby Control Flow"
  (l/emit-as :ruby
   '[(if true
       (puts "true")
       (puts "false"))])
  => "if true\n  puts \"true\"\nelse\n  puts \"false\"\nend")

(fact "Ruby Functions"
  (l/emit-as :ruby
   '[(defn add [a b]
      (return (+ a b)))])
  => "def add(a,b)\n  return a + b\nend")

(fact "Ruby Arrays and Maps"
  (l/emit-as :ruby
   '[(do
      (var arr [1 2 3])
      (var m {:a 1 :b 2}))])
  => "arr = [1,2,3]\nm = {:a => 1, :b => 2}")

(fact "Ruby String Operations"
  (l/emit-as :ruby
   '[(do
       (var s "hello")
       (puts (. s (upcase))))])
  => "s = \"hello\"\nputs s.upcase")

(fact "Ruby XTalk Support"
  (l/emit-as :ruby
   '[(do
      (x:print "hello")
      (x:cat "a" "b"))])
  => "puts \"hello\"\n\"a\" + \"b\"")

(fact "Ruby invoke, spread, and range forms stay structural"
  (l/emit-as :ruby
   '[(do
       (. obj (respond_to? :call))
       (. f (call (:.. args)))
       (. s [(to start -1)])
       (. s [(to-e start stop)]))])
  => "obj.respond_to?(:call)\nf.call(*args)\ns[start..-1]\ns[start...stop]")

^{:refer std.lang.model-annex.spec-ruby/ruby-symbol :added "4.1"}
(fact "emit ruby symbol"

  (spec-ruby/ruby-symbol :a spec-ruby/+grammar+ {})
  => ":a"
  (spec-ruby/ruby-symbol 'a spec-ruby/+grammar+ {})
  => "a"
  (spec-ruby/ruby-symbol 'respond_to? spec-ruby/+grammar+ {})
  => "respond_to?")

^{:refer std.lang.model-annex.spec-ruby/ruby-symbol-global :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-ruby/ruby-var :added "4.1"}
(fact "emit ruby variable"

  (spec-ruby/ruby-var '(var a 1))
  => '(:= a 1))

^{:refer std.lang.model-annex.spec-ruby/ruby-map :added "4.1"}
(fact "emit ruby hash"

  (l/emit-as :ruby '[{:a 1 :b 2}])
  => "{:a => 1, :b => 2}")

^{:refer std.lang.model-annex.spec-ruby/ruby-defn :added "4.1"}
(fact "emit ruby function definition"

  (l/emit-as :ruby '[(defn add [a b] (return (+ a b)))])
  => "def add(a,b)\n  return a + b\nend")

^{:refer std.lang.model-annex.spec-ruby/ruby-fn :added "4.1"}
(fact "basic transform for ruby blocks"

  (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
  => '(:- "->(" "a" ") {\n" "(do (+ a 1))" "\n}"))
