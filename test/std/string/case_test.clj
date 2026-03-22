(ns std.string.case-test
  (:require [std.string.case :as case :refer :all]
            [std.string.wrap :refer [wrap]])
  (:use code.test))

^{:refer std.string.case/re-sub :added "3.0" :adopt true}
(fact "substitute a pattern by applying a function"

  (#'case/re-sub "aTa" @#'case/+hump-pattern+ (fn [_] "$"))
  => "$a")

^{:refer std.string.case/separate-humps :added "3.0" :adopt true}
(fact "separate words that are camel cased"

  (#'case/separate-humps "aTaTa")
  => "a Ta Ta")

^{:refer std.string.case/camel-case :added "3.0"}
(fact "converts a string-like object to camel case representation"

  (camel-case "hello-world")
  => "helloWorld"

  ((wrap camel-case) 'hello_world)
  => 'helloWorld)

^{:refer std.string.case/upper-camel-case :added "4.0"}
(fact "convert string to uppercase camel"

  (upper-camel-case "hello-world")
  => "HelloWorld")

^{:refer std.string.case/capital-sep-case :added "3.0"}
(fact "converts a string-like object to captital case representation"

  (capital-sep-case "hello world")
  => "Hello World"

  (str ((wrap capital-sep-case) :hello-world))
  => ":Hello World")

^{:refer std.string.case/lower-sep-case :added "3.0"}
(fact "converts a string-like object to a lower case representation"

  (lower-sep-case "helloWorld")
  => "hello world"

  ((wrap lower-sep-case) 'hello-world)
  => (symbol "hello world"))

^{:refer std.string.case/pascal-case :added "3.0"}
(fact "converts a string-like object to a pascal case representation"

  (pascal-case "helloWorld")
  => "HelloWorld"

  ((wrap pascal-case) :hello-world)
  => :HelloWorld)

^{:refer std.string.case/phrase-case :added "3.0"}
(fact "converts a string-like object to snake case representation"

  (phrase-case "hello-world")
  => "Hello world")

^{:refer std.string.case/dot-case :added "3.0"}
(fact "creates a dot-case representation"

  (dot-case "hello-world")
  => "hello.world"

  ((wrap dot-case) 'helloWorld)
  => 'hello.world)

^{:refer std.string.case/snake-case :added "3.0"}
(fact "converts a string-like object to snake case representation"

  (snake-case "hello-world")
  => "hello_world"

  ((wrap snake-case) 'helloWorld)
  => 'hello_world)

^{:refer std.string.case/spear-case :added "3.0"}
(fact "converts a string-like object to spear case representation"

  (spear-case "hello_world")
  => "hello-world"

  ((wrap spear-case) 'helloWorld)
  => 'hello-world)

^{:refer std.string.case/upper-sep-case :added "3.0"}
(fact "converts a string-like object to upper case representation"

  (upper-sep-case "hello world")
  => "HELLO WORLD"

  (str ((wrap upper-sep-case) 'hello-world))
  => "HELLO WORLD")

^{:refer std.string.case/typeless= :added "3.0"}
(fact "compares two representations "

  (typeless= "helloWorld" "hello_world")
  => true

  ((wrap typeless=) :a-b-c "a b c")
  => true

  ((wrap typeless=) 'getMethod :get-method)
  => true)
