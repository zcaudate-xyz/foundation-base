(ns test-emit-2
  (:require [std.lang :as l]))

(println "Testing set!...")
(println (l/emit-as :python
           '(set! a 1)))

(println "Testing .__contains__...")
(println (l/emit-as :python
           '(. node (__contains__ "_type"))))
