(ns xtbench.r.lang.spec-os-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-os/x:pwd :added "4.1"}
(fact "gets the current pwd"

  (!.R
    (spec-os/x:pwd))
  => string?)

^{:refer xt.lang.spec-os/x:shell :added "4.1"}
(fact "supports transitional shell callback arglists"

  (notify/wait-on :r
    (var shell-fn
         (fn [command root cb]
           (return
            (spec-os/x:shell command root cb))))
    (shell-fn "printf hello"
              (spec-os/x:pwd)
              (fn [err out]
                (repl/notify out))))
  => string?)

^{:refer xt.lang.spec-os/x:file-resolve :added "4.1"}
(fact "file-resolve"

  (!.R
    (var resolve-fn
         (fn [path]
           (return
            (spec-os/x:file-resolve (spec-os/x:pwd) path))))
    (resolve-fn "project.clj"))
  => (str (std.fs/file "project.clj")))

^{:refer xt.lang.spec-os/x:file-slurp :added "4.1"}
(fact "reads file content through callbacks"

  (notify/wait-on :r
    (var slurp-fn
         (fn [path cb]
           (return
            (spec-os/x:file-slurp path cb))))
    (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "project.clj")
              (fn [err out]
                (repl/notify out))))
  => string?)

^{:refer xt.lang.spec-os/x:file-spit :added "4.1"}
(fact "writes file content through callbacks"

  (notify/wait-on :r
    (var spit-fn
         (fn [path content cb]
           (return
            (spec-os/x:file-spit path content cb))))
    (var slurp-fn
         (fn [path cb]
           (return
            (spec-os/x:file-slurp path cb))))
    (spit-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
             "hello world"
             (fn [err out]
               (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
                         (fn [err out]
                           (repl/notify out))))))
  => "hello world")

(comment
  
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:dart] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.spec-os {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-os
{:lang [:lua :python] :write true}))
