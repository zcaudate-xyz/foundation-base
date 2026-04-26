(ns xt.lang.spec-os-python-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-os/x:file-slurp :added "4.1"}
(fact "reads file content through callbacks in python"
  (notify/wait-on :python
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:file-slurp path opts cb))))
    (slurp-fn "project.clj"
              {}
              (fn [err out]
                (repl/notify out))))
  => string?)

^{:refer xt.lang.spec-os/x:file-spit :added "4.1"}
(fact "writes file content through callbacks in python"
  (notify/wait-on :python
    (var spit-fn
         (fn [path content opts cb]
           (return
            (spec-os/x:file-spit path content opts cb))))
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:file-slurp path opts cb))))
    (spit-fn "test-scratch/out.python.tmp"
             "hello world"
             {}
             (fn [err out]
               (slurp-fn "test-scratch/out.python.tmp"
                         {}
                         (fn [err out]
                           (repl/notify out))))))
  => "hello world")
