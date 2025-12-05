(ns code.test.benchmark-test
  (:use code.test)
  (:require [code.test.benchmark :refer :all]))

(fact "benchmarks a simple expression"
  (bench (Thread/sleep 10) {:samples 5 :warmup 1})
  => (contains {:n 5
                :mean number?
                :unit "ns"}))
