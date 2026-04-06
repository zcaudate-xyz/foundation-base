(ns code.manage.transform.defn-props-test
  (:use code.test)
  (:require [code.manage.transform.defn-props :refer :all]
            [std.block.navigate :as nav]))

^{:refer code.manage.transform.defn-props/transform-props :added "4.1"}
(fact "converts spread props sets into explicit maps"
  (-> "#{[:a :b (:.. props)]}"
      nav/parse-string
      transform-props
      nav/root-string
      read-string)
  => {:# [:a :b] :.. 'props})

^{:refer code.manage.transform.defn-props/transform :added "4.1"}
(fact "applies the props transform across source forms"
  (-> "(def elem #{[:view (:.. props)]})"
      nav/parse-string
      transform
      nav/root-string
      read-string)
  => '(def elem {:# [:view] :.. props}))
