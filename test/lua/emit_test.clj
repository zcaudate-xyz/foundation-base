;; test/lua/emit_test.clj
(ns test.lua.emit-test
  (:require [std.lang :as l]))

(l/script :lua
  {:macro-only true}
  
  (def +emit-tests+
  [
   {:input '(+ 1 2 3) :expected "1 + 2 + 3"}
   {:input '(def x 10) :expected "local x = 10"}
   {:input '(str "hello" " " "world") :expected "str('hello',' ','world')"}
   {:input '{:a 1 :b 2} :expected "{a=1,b=2}"}
   {:input '[1 2 3] :expected "{1,2,3}"}
   {:input '(for [i (range 1 4)] (print i)) :expected "for i, range(1,4) do\n  print(i)\nend"}
   {:input '(if (and true false) "yes" "no") :expected "if true and false then\n  'yes'\nelse\n  'no'\nend"}
   {:input '(fn [x] (* x x)) :expected "function (x)\n  x * x\nend"}
   ])