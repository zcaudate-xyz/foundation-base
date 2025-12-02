(ns js.lib.d3-test
  (:use code.test)
  (:require [js.lib.d3 :as d3]))

^{:refer js.lib.d3/d3-macro-fn :added "4.0" :unchecked true}
(fact "rewrite function"

  (d3/d3-macro-fn "d3.bisector" {:a 1 :b 2})
  => '(. (d3.bisector) (a 1) (b 2)))

^{:refer js.lib.d3/d3-macro :added "4.0" :unchecked true}
(fact "creates a macro form")

^{:refer js.lib.d3/d3-tmpl :added "4.0" :unchecked true}
(fact "creates fragment or macro"

  (d3/d3-tmpl 'minindex)
  => '(def$.js minindex d3.minindex)

  (d3/d3-tmpl '[bin #{value domain  thresholds}])
  => '(defmacro.js bin ([& [m]] (d3-macro-fn "d3.bin" m))))
