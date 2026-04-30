(ns std.lang.model-annex.spec-ruby-test
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-ruby :as spec-ruby]
            [std.lang.model-annex.spec-ruby.rewrite :as rewrite]
            [std.lang.model-annex.spec-xtalk.fn-ruby :as fn-ruby])
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
  => "arr = [1,2,3]\nm = {\"a\" => 1, \"b\" => 2}")

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

(fact "Ruby callable rewrite does not rewrap ruby-method-ref forms"
  (rewrite/rewrite-callable-form
   '(xt.lang.common-data/swap-key
     record
     "ref_links"
     (ruby-method-ref xt.lang.common-data/obj-assign-with)
     [ref-links xt/x:obj-assign])
   #{})
  => '(xt.lang.common-data/swap-key
       record
       "ref_links"
       (ruby-method-ref xt.lang.common-data/obj-assign-with)
       [ref-links (ruby-method-ref xt/x:obj-assign)]))

(fact "Ruby callable rewrite preserves namespaced constant values"
  (rewrite/rewrite-callable-form
   '(xt/x:get-key xt.db.schema.base-scope/Scopes s)
   #{})
  => '(xt/x:get-key xt.db.schema.base-scope/Scopes s))

(fact "Ruby destructuring preserves hyphenated map keys"
  (rewrite/rewrite-callable-form
   '(var #{order-by} opts)
   #{})
  => '(do*
        (var ruby_destructure__ opts)
        (var order-by (x:get-key ruby_destructure__ "order-by" nil))))

(fact "Ruby x:has-key? guards nil receivers"
  (fn-ruby/ruby-tf-x-has-key? '(x:has-key? obj "a"))
  => '(and (not= nil obj)
           (. obj (key? "a")))

  (fn-ruby/ruby-tf-x-has-key? '(x:has-key? obj "a" 1))
  => '(and (not= nil obj)
           (. obj (key? "a"))
           (== (. obj ["a"]) 1)))

(fact "Ruby x:get-key and x:set-key guard nil receivers"
  (fn-ruby/ruby-tf-x-get-key '(x:get-key obj "a" nil))
  => '(:? (== nil obj)
          nil
          (:? (== nil (. obj ["a"]))
              nil
              (. obj ["a"])))

  (fn-ruby/ruby-tf-x-set-key '(x:set-key obj "a" 1))
  => '(do
        (when (== nil obj)
          (:= obj {}))
        (:= (. obj ["a"]) 1)
        obj))

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
  => "{\"a\" => 1, \"b\" => 2}"

  (l/emit-as :ruby '[{:ref-links {} :first-name "Root"}])
  => "{\"ref_links\" => {}, \"first_name\" => \"Root\"}")

^{:refer std.lang.model-annex.spec-ruby/ruby-defn :added "4.1"}
(fact "emit ruby function definition"

  (l/emit-as :ruby '[(defn add [a b] (return (+ a b)))])
  => "def add(a,b)\n  return a + b\nend")

^{:refer std.lang.model-annex.spec-ruby/ruby-fn :added "4.1"}
(fact "basic transform for ruby blocks"

  (spec-ruby/ruby-fn '(fn [a] (+ a 1)))
  => '(:- "->(" "a" ") {\n" "(do (+ a 1))" "\n}"))
