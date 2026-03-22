(ns std.lib.extend-test
  (:require [std.lib.extend :refer :all])
  (:use code.test))

^{:refer std.lib.extend/extend-single :added "3.0"}
(fact "Transforms a protocol template into an extend-type expression"

  (extend-single 'Type
                 'IProtocol
                 '[(op [x y] (% x y))]
                 '[op-object])
  => '(clojure.core/extend-type Type IProtocol (op [x y] (op-object x y))))

^{:refer std.lib.extend/extend-entry :added "3.0"}
(fact "Helper function for extend-all "

  (extend-entry 'Magma
                '[(op ([x y] (% x y)))]
                '[Number        [op-number]])
  => '[(clojure.core/extend-type Number Magma (op ([x y] (op-number x y))))])

^{:refer std.lib.extend/extend-all :added "3.0"}
(fact "Transforms a protocl template into multiple extend-type expresions"

  (macroexpand-1
   '(extend-all Magma
                [(op ([x y] (% x y)))]

                Number        [op-number]
                [List Vector] [op-list]))
  => '(do (clojure.core/extend-type Number Magma (op ([x y] (op-number x y))))
          (clojure.core/extend-type List Magma (op ([x y] (op-list x y))))
          (clojure.core/extend-type Vector Magma (op ([x y] (op-list x y))))))

(comment

  (code.manage/import))