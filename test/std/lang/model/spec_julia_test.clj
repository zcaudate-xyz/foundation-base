(ns std.lang.model.spec-julia-test
  (:use code.test)
  (:require [std.lang.model.spec-julia :refer :all]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lib :as h]))

(script/script- :julia)

^{:refer std.lang.model.spec-julia/tf-local :added "4.0"}
(fact "a more flexible `var` replacement"
  ^:hidden

  (tf-local '(local a 1))
  => '(var* :local a := 1)

  (tf-local '(local a := 1))
  => '(var* :local a := 1))

^{:refer std.lang.model.spec-julia/julia-map-key :added "3.0"}
(fact "custom julia map key"
  ^:hidden

  (julia-map-key 123 +grammar+ {})
  => "123"

  (julia-map-key "123" +grammar+ {})
  => "\"123\""


  (julia-map-key "abc" +grammar+ {})
  => "\"abc\""

  (julia-map-key :abc +grammar+ {})
  => "\"abc\"")

^{:refer std.lang.model.spec-julia/tf-for-iter :added "4.0"}
(fact  "for iter transform"
  ^:hidden

  (tf-for-iter '(for:iter [e iter]
                          e))
  => '(for [e :in iter] e))

^{:refer std.lang.model.spec-julia/tf-for-index :added "4.0"}
(fact "for index transform"
  ^:hidden

  (tf-for-index '(for:index [i [0 2 10]]
                            i))
  => '(for [i :in (:to 0 10 2)] i))

^{:refer std.lang.model.spec-julia/julia-module-link :added "4.0"}
(fact "gets the absolute julia based module"

  (julia-module-link 'kmi.common {:root-ns 'kmi.hello})
  => "./common"

  (julia-module-link 'kmi.exchange
                   {:root-ns 'kmi :target "src"})
  => "./kmi/exchange")

^{:refer std.lang.model.spec-julia/julia-module-export :added "4.0"}
(fact "outputs the julia module export form"
  ^:hidden

  (julia-module-export 'kmi.common {:root-ns 'kmi.hello})
  => nil)

(fact "Basic Julia generation"
  (!.julia
   (var a 10)
   (return a))
  => "a = 10\nreturn a"

  (!.julia
   (defn hello [a b]
     (return (+ a b))))
  => "function hello(a, b)\n  return a + b\nend"

  (!.julia
   (if true
     (println "Yes")
     (println "No")))
  => "if true\n  println(\"Yes\") end\nelse\n  println(\"No\") end\nend"

  (!.julia
   (for [i :in (list :to 1 3)]
     (println i)))
  => "for i in 1:3\n  println(i)\nend")

(fact "Xtalk Julia mappings"
  (!.julia (x:print "Hello"))
  => "println(\"Hello\")"

  (!.julia (x:len [1 2 3]))
  => "length([1, 2, 3])"

  (!.julia (x:cat "a" "b"))
  => "\"a\" * \"b\""

  (!.julia (x:get-key (dict :a 1) "a" 0))
  => "get(Dict(\"a\" => 1), \"a\", 0)"

  (!.julia (x:random))
  => "rand()"

  (!.julia (x:m-sin 1))
  => "sin(1)"

  (!.julia (x:str-join ", " ["a" "b"]))
  => "join([\"a\", \"b\"], \", \")"

  (!.julia (x:arr-push [1] 2))
  => "push!([1] , 2)")


^{:refer std.lang.model.spec-julia/tf-dict :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-julia/tf-push! :added "4.1"}
(fact "TODO")