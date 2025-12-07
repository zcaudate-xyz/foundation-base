(ns code.tool.measure.algo-clojure-test
  (:use code.test)
  (:require [code.tool.measure.algo-clojure :as sut]))

^{:refer code.tool.measure.algo-clojure/score-code :added "4.1"}
(fact "score-code calculates complexity"
  (sut/score-code ['(if a b c)]) => number?
  (sut/score-code ['(defn f [x] (if x 1 0))]) => number?)

^{:refer code.tool.measure.algo-clojure/count-code :added "4.1"}
(fact "count-code counts atoms"
  (sut/count-code ['(a b c)]) => 4 ;; 3 atoms + 1 list
  (sut/count-code ['a]) => 1)

^{:refer code.tool.measure.algo-clojure/read-all-forms :added "4.1"}
(fact "read-all-forms reads string"
  (sut/read-all-forms "(def a 1) (def b 2)") => ['(def a 1) '(def b 2)])

^{:refer code.tool.measure.algo-clojure/generate-metrics :added "4.1"}
(fact "generate-metrics returns map"
  (sut/generate-metrics "(def a 1)") => (contains {:complexity number? :surface number?}))
