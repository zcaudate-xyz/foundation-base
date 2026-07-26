tahto/model/spec_xtalk_test.clj:1:(ns tahto.model.spec-xtalk-test
  (:require [tahto.core :as l])
  (:use code.test))

tahto/model/spec_xtalk_test.clj:5:^{:refer tahto.model.spec-xtalk/CANARY :adopt true :added "4.0"}
(fact "This is the cross language language"

  (l/emit-as
   :xtalk ['(fn [x y] (+ (. x [1]) 2 3))])
  => "function (x,y){\n  x[1] + 2 + 3;\n}")
