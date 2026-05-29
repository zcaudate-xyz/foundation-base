(ns demo-xtdb-backbone.build-scratch-v0-test
  (:require [demo-xtdb-backbone.build.scratch-v0 :as build])
  (:use code.test))

(fact "the scratch-v0 sql build declares the expected sql artifact"
  (every? (set build/+expected-files+)
          ["sql/scratch_v0.sql"])
  => true)
