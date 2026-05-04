(ns hara.runtime.basic.impl.process-c-test
  (:require [hara.runtime.basic.impl.process-c :refer :all]
            [hara.runtime.basic.type-common :as common]
            [hara.lang :as l])
  (:use code.test))

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

(def CANARY-TCC
  (common/program-exists? "tcc"))

^{:refer hara.runtime.basic.impl.process-c-test/CANARY-TCC :guard true :adopt true :added "4.0"}
(fact "EVALUATE tcc in c"

  (if CANARY-TCC
    [(str (!.c (printf "hello world")))
     [(-/add 1 2)
      (!.c
       (-/add 1 2))]
     [(-/sub 1 2)
      (!.c
       (-/sub 1 2))]
     (!.c
      (-/add 1 (-/sub 3 4)))
     [(-/hello)
      (!.c
       (-/hello))]
     [(-/main)
      (!.c
       (-/main))]]
    :tcc-unavailable)
  => (any ["\nhello world"
           [3 3]
           [-1 -1]
           0
           ["hello world" "hello world"]
           [-7 -7]]
          :tcc-unavailable))

^{:refer hara.runtime.basic.impl.process-c/get-format-string :added "4.0"}
(fact "gets the format string given entry"

  (get-format-string @hello)
  => "\"%s\""

  (get-format-string @add)
  => "%d")

^{:refer hara.runtime.basic.impl.process-c/transform-form-format :added "4.0"}
(fact "formats the form"

  (transform-form-format `-/add
                         {:emit {:input {:pointer -/add
                                         :args [1 2]}}})
  => '(printf "%d" (hara.runtime.basic.impl.process-c-test/add 1 2)))

^{:refer hara.runtime.basic.impl.process-c/transform-form :added "4.0"}
(fact "transforms the form for tcc output"

  (transform-form '[(printf "hello world")] {})
  => '(:- "#include <stdio.h>\nint main(){\n " (do (printf "hello world")) "\nreturn 0;\n}"))
