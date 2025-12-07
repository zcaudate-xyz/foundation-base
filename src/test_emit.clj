(ns test-emit
  (:require [std.lang :as l]
            [std.lib :as h]))

(println "Testing catch...")
(try
  (println (l/emit-as :python '(try (pass) (catch [Exception] (pass)))))
  (catch Exception e (println "Catch failed:" e)))

(println "Testing with...")
(try
  (println (l/emit-as :python '(with [f] (pass))))
  (catch Exception e (println "With failed:" e)))

(println "Testing for...")
(try
  (println (l/emit-as :python '(for [i :in range] (pass))))
  (catch Exception e (println "For failed:" e)))

(println "Testing defn...")
(try
  (println (l/emit-as :python '(defn foo [x] (pass))))
  (catch Exception e (println "Defn failed:" e)))

(println "Testing full try...")
(try
  (println (l/emit-as :python
    '(try
       (var x 1)
       (catch [IndexError] (pass)))))
  (catch Exception e (println "Full try failed:" e)))
