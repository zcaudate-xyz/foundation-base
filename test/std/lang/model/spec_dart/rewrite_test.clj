(ns std.lang.model.spec-dart.rewrite-test
  (:require [clojure.string :as str]
            [std.lang :as l]
            [std.lang.model.spec-dart.rewrite :as rewrite])
  (:use code.test))

^{:refer std.lang.model.spec-dart.rewrite/dart-rewrite-stage :added "4.1"}
(fact "rewrites `or` and `:?` forms into dart-specific staging helpers"
  (rewrite/dart-rewrite-stage
   '(var key (or (xt/x:get-key cache key) "0"))
   nil)
  => '(var key (dart:or (xt/x:get-key cache key) "0"))

  (rewrite/dart-rewrite-stage
   '(var key (:? key-fn (key-fn input t) t))
   nil)
  => '(var key (dart:ternary key-fn (key-fn input t) t))

  (rewrite/dart-rewrite-stage
   '[(var out (or a b c))
     (return (:? pred out fallback))]
   nil)
  => '[(var out (dart:or a b c))
       (return (dart:ternary pred out fallback))])

^{:refer std.lang.model.spec-dart.rewrite/dart-rewrite-stage :added "4.1"}
(fact "dart emission uses rewritten helpers to preserve xt truthiness"
  (let [or-out      (l/emit-as :dart ['(var key (or value "fallback"))])
        ternary-out (l/emit-as :dart ['(var out (:? pred left right))])]
    [(str/includes? or-out "var key =")
     (str/includes? or-out "return \"fallback\";")
     (str/includes? or-out "!= null")
     (str/includes? or-out "!= false")
     (str/includes? ternary-out "var out =")
     (str/includes? ternary-out "return left;")
     (str/includes? ternary-out "return right;")])
  => [true true true true true true true])
