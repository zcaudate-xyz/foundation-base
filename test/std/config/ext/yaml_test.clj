(ns std.config.ext.yaml-test
  (:require [script.yaml :as yaml]
            [std.config.ext.yaml :refer :all])
  (:use code.test))

^{:refer std.config.ext.yaml/resolve-type-yaml :added "3.0"}
(fact "resolves yaml config"

  (resolve-type-yaml nil (yaml/write {:a 1 :b 2}))
  => {:a 1, :b 2})
