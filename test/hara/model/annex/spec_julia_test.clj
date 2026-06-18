(ns hara.model.annex.spec-julia-test
  (:require [hara.lang.script :as script]
            [hara.model.annex.spec-julia.rewrite :as rewrite]
            [hara.common.util :as ut]
            [hara.model.annex.spec-julia :refer :all])
  (:refer-clojure :exclude [for import])
  (:use code.test))

(script/script- :julia)

(fact "Basic Julia generation"
  (!.jl
   (var a 10)
   (return a))
  => "a = 10\nreturn a"

  (!.jl
   (defn hello [a b]
     (return (+ a b))))
  => "function hello(a,b)\n  return a + b\nend"

  (!.jl
   (if true
     (println "Yes")
     (println "No")))
  => "if true\n  println(\"Yes\")\nelse\n  println(\"No\")\nend"

  (!.jl
    (for [i :in (to 1 1 3)]
      (println i)))
   => "for i in 1:3\n  println(i)\nend"

   (!.jl
    (for:iter [e iter]
      e))
   => "for e in iter\n  e\nend"

   (!.jl
     (push! arr 1))
   => "push!(arr,1)"

   (!.jl
    (delete! obj "a"))
    => "delete!(obj,\"a\")")

(fact "Julia staging rewrite normalizes truthy tests and set destructuring"
  (let [out (rewrite/julia-rewrite-stage
             '(when curr
                (return curr))
             {:grammar +grammar+})]
    [(= 'when (first out))
     (= '(and (x:not-nil? curr) (not= false curr))
        (second out))
     (= '(return curr) (nth out 2))])
  => [true true true]

  (let [out (rewrite/julia-rewrite-stage
             '(var #{spaces watch} g)
             {:grammar +grammar+})
        [_ bind extract1 extract2] out
        temp (second bind)]
    [(= 'do* (first out))
     (= 'var (first bind))
     (symbol? temp)
     (= 'g (last bind))
     (= extract1 (list 'var 'spaces (list 'x:get-key temp "spaces" nil)))
     (= extract2 (list 'var 'watch (list 'x:get-key temp "watch" nil)))])
  => [true true true true true true])

(fact "Xtalk Julia mappings"
  (!.jl (x:print "Hello"))
  => "println(\"Hello\")"

  (!.jl (x:len [1 2 3]))
  => "length(Any[1,2,3])"

  (!.jl (x:cat "a" "b"))
  => "\"a\" * \"b\""

  (!.jl (x:offset 10))
  => "11"

  (!.jl (x:get-idx arr (x:offset 0)))
  => "arr[1]"

  (!.jl (x:set-idx arr (x:offset 1) value))
  => "arr[2] = value"

  (!.jl (x:get-key (dict :a 1) "a" 0))
  => "get(Dict(\"a\" => 1),\"a\",0)"

  (!.jl (x:random))
  => "rand()"

  (!.jl (x:m-sin 1))
  => "sin(1)"

  (!.jl (x:str-join ", " ["a" "b"]))
  => "join(Any[\"a\",\"b\"],\", \")"

  (!.jl (x:arr-push [1] 2))
  => "push!(Any[1],2)")

^{:refer hara.model.annex.spec-julia/tf-local :added "4.0"}
(fact "a more flexible `var` replacement"

  (tf-local '(local a 1))
  => '(var* :local a := 1)

  (tf-local '(local a := 1))
  => '(var* :local a := 1))

^{:refer hara.model.annex.spec-julia/julia-map-key :added "3.0"}
(fact "custom julia map key"

  (julia-map-key 123 +grammar+ {})
  => "123"

  (julia-map-key "123" +grammar+ {})
  => "\"123\""


  (julia-map-key "abc" +grammar+ {})
  => "\"abc\""

  (julia-map-key :abc +grammar+ {})
  => "\"abc\"")

^{:refer hara.model.annex.spec-julia/julia-symbol-global :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia/tf-for-array :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia/tf-for-object :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia/tf-for-iter :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia/tf-for-index :added "4.0"}
(fact "for index transform"
 
  (tf-for-index '(for:index [i [0 10 2]]
                             i))
  => '(for [i :in (to 0 2 10)] i))

^{:refer hara.model.annex.spec-julia/tf-dict :added "4.0"}
(fact "dict transform"

  (tf-dict '(dict :a 1 :b 2))
  => '(Dict (=> "a" 1) (=> "b" 2)))

^{:refer hara.model.annex.spec-julia/emit-to :added "4.1"}
(fact "emits a Julia range expression"
  (emit-to [:to 1 1 10] +grammar+ {})
  => "1:10"

  (emit-to [:to 1 2 10] +grammar+ {})
  => "1:2:10")

^{:refer hara.model.annex.spec-julia/julia-module-link :added "4.0"}
(fact "gets the absolute julia based module"

  (julia-module-link 'kmi.common {:root-ns 'kmi.hello})
  => "./common"

  (julia-module-link 'kmi.exchange
                   {:root-ns 'kmi :target "src"})
  => "./kmi/exchange")

^{:refer hara.model.annex.spec-julia/julia-module-export :added "4.0"}
(fact "outputs the julia module export form"

  (julia-module-export 'kmi.common {:root-ns 'kmi.hello})
  => nil

  (julia-module-export {:code {'a {:op :defn} 'b {:op :def}}} {})
  => anything #_(contains '(export (a b))))
