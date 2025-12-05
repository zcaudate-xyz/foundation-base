(ns jvm.namespace-test
  (:require [jvm.namespace :refer :all]
            [code.test :refer [fact]]))

(fact "namespace list all aliases task"
  (list-aliases '[jvm.namespace])
  => map?)
