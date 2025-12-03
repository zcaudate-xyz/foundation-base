(ns rt.basic.impl.process-c-test
  (:use code.test)
  (:require [rt.basic.impl.process-c :refer :all]
            [std.lang :as l]))

(l/script- :c
  {:runtime :oneshot})

(defn.c ^{:- [:int]}
  add
  [:int a
   :int b]
  (return (+ a b)))

(defn.c ^{:- [:int]}
  sub
  [:int a
   :int b]
  (return (- a b)))

(defn.c ^{:- [:char :*]}
  hello
  []
  (return "hello world"))

(defn.c ^{:- [:int]}
  main
  []
  (return (-/sub (-/add 1 2)
                 10)))


^{:refer rt.basic.impl.process-c/CANARY :adopt true :added "4.0"}
(fact "EVALUATE tcc in c"
  ^:hidden
  
  (str (!.c (printf "hello world")))
  => "\nhello world"
  
  [(-/add 1 2)
   (!.c
     (-/add 1 2))]
  => [3 3]

  [(-/sub 1 2)
   (!.c
    (-/sub 1 2))]
  => [-1 -1]

  (!.c
   (-/add 1 (-/sub 3 4)))
  => 0

  [(-/hello)
   (!.c
    (-/hello))]
  => ["hello world" "hello world"]

  [(-/main)
   (!.c
    (-/main))]
  => [-7 -7])

^{:refer rt.basic.impl.process-c/get-format-string :added "4.0"}
(fact "gets the format string given entry"
  ^:hidden
  
  (get-format-string @hello)
  => "\"%s\""

  (get-format-string @add)
  => "%d")

^{:refer rt.basic.impl.process-c/transform-form-format :added "4.0"}
(fact "formats the form"
  ^:hidden
  
  (transform-form-format `-/add
                         {:emit {:input {:pointer -/add
                                         :args [1 2]}}})
  => '(printf "%d" (rt.basic.impl.process-c-test/add 1 2)))

^{:refer rt.basic.impl.process-c/transform-form :added "4.0"}
(fact "transforms the form for tcc output"
  ^:hidden
  
  (transform-form '[(printf "hello world")] {})
  => '(:- "void main(){\n " (do (printf "hello world")) "\n}"))
