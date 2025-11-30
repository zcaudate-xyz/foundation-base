(ns std.dom.invoke-test
  (:use code.test)
  (:require [std.dom.invoke :refer :all]
            [std.dom.type :as type]
            [std.lib :refer [definvoke]]))

^{:refer std.dom.invoke/invoke-intern-dom :added "3.0"}
(fact "constructor for dom"

  (def res-react (invoke-intern-dom nil 'hello {:class :react
                                                :tag :test/hello-dom} [['dom '_] 1]))
  (first res-react) => 'clojure.core/let
  (second res-react) => vector?
  (seq? (nth res-react 3)) => true ;; component-install call
  
  (def res-value (invoke-intern-dom nil 'hello {:class :value
                                                :tag :test/hello-dom} [['_] 1]))
  (first res-value) => 'clojure.core/let
  (second res-value) => vector?
  (seq? (nth res-value 3)) => true ;; metaprops-add call
  )
