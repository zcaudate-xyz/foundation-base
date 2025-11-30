(ns scratch.test-invoke
  (:require [std.dom.invoke :as invoke]))

(println (invoke/invoke-intern-dom nil 'hello {:class :react
                                               :tag :test/hello-dom} [['dom '_] 1]))
