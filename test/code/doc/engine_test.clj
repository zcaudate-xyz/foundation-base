(ns code.doc.engine-test
  (:use code.test)
  (:require [code.doc.engine :refer :all]))

^{:refer code.doc.engine/wrap-hidden :added "3.0"}
(fact "helper function to not process elements with the `:hidden` tag"

  ((wrap-hidden identity) {:hidden true})
  => nil

  ((wrap-hidden identity) {:a 1})
  => {:a 1})

^{:refer code.doc.engine/engine :added "3.0"}
(fact "dynamically loads the templating engine for publishing"

  (set (keys (engine "winterfell")))
  => (contains [:page-element :render-chapter :nav-element]))
