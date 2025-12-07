(ns code.tool.measure.algo-js-test
  (:use code.test)
  (:require [code.tool.measure.algo-js :as sut]
            [code.tool.translate.js-ast :as js-ast]
            [std.fs :as fs]))

^{:refer code.tool.measure.algo-js/score-ast :added "4.1"}
(fact "score-ast calculates complexity"
  (sut/score-ast {"type" "IfStatement"}) => number?
  (sut/score-ast {"type" "Program" "body" []}) => number?)

^{:refer code.tool.measure.algo-js/generate-metrics :added "4.1"}
(fact "generate-metrics returns map"
  (with-redefs [js-ast/translate-ast (fn [in out] (spit out "{\"type\":\"Program\"}"))]
    (sut/generate-metrics "var a = 1;"))
  => (contains {:complexity number? :surface number?}))
