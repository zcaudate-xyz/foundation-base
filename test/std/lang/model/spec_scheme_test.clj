(ns std.lang.model.spec-scheme-test
  (:require [std.lang :as l]
            [std.lang.model.spec-scheme :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-scheme/emit-scheme :added "4.0"}
(fact "emits code into scheme schema"
  (emit-scheme '(defn hello [x] (return (== x nil))) {})
  => "(define (hello x) (equal? x '()))")

^{:refer std.lang.model.spec-scheme/emit-scheme :added "4.0"}
(fact "emits scheme data structures"
  (emit-scheme {:a 1 :b [2 3]} {})
  => "(hash \"a\" 1 \"b\" #(2 3))")

^{:refer std.lang.model.spec-scheme/+book+ :added "4.1"}
(fact "emits xtalk through the scheme backend"
  (l/emit-as :scheme '[(x:print (x:cat "a" "b"))])
  => #"\(display \(string-append \"a\" \"b\"\)\)")
