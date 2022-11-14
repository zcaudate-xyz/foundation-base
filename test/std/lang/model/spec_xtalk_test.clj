(ns std.lang.model.spec-xtalk-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:refer std.lang.model.spec-xtalk/CANARY :adopt true :added "4.0"}
(fact "This is the cross language language"

  (l/emit-as
   :xtalk ['(fn [x y] (+ (. x [1]) 2 3))])
  => "function (x,y){\n  x[1] + 2 + 3;\n}")
