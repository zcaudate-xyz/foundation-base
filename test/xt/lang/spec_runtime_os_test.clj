(ns xt.lang.spec-runtime-os-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-runtime-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-runtime-os/x:slurp-file :added "4.1"}
(fact "reads file content through callbacks"
  
  (notify/wait-on :js
    (xt/x:slurp-file "project.clj"
                     {}
                     (fn [err res]
                       (repl/notify res))))
  => string?)

^{:refer xt.lang.spec-runtime-os/x:spit-file :added "4.1"}
(fact "writes file content through callbacks"

  ^{:seedgen/base true}
  (let [path "test-scratch/out.tmp"
        out  (notify/wait-on :js
               (xt/x:spit-file "test-scratch/out.tmp"
                               "hello world"
                               {}
                               (fn [err _]
                                 (xt/x:slurp-file "test-scratch/out.tmp"
                                                  {}
                                                  (fn [inner-err res]
                                                    (repl/notify res))))))]
    (.delete (java.io.File. path))
    out)
  => "hello world")

^{:refer xt.lang.spec-runtime-os/x:shell :added "4.1"}
(fact "executes shell commands asynchronously"
  
  (notify/wait-on :js
    (do:> (xt/x:shell "printf hello" {}
                      {:success (fn [res]
                                  (repl/notify res))
                       :error   (fn [err]
                                  (repl/notify "ERR"))})))
  => #"hello")

^{:refer xt.lang.spec-runtime-os/x:shell :added "4.1"}
(fact "supports transitional shell callback arglists"
  
  (notify/wait-on :js
    (do:> (xt/x:shell "printf hello"
                      {:success (fn [res]
                                  (repl/notify res))
                       :error   (fn [err]
                                  (repl/notify "ERR"))})))
  => #"hello")


^{:refer xt.lang.spec-runtime-os/x:thread-spawn :added "4.1"}
(fact "spawns js promise-backed threads"

  (notify/wait-on :js
    (xt/x:thread-spawn (fn []
                         (repl/notify "OK"))))
  => "OK")

^{:refer xt.lang.spec-runtime-os/x:thread-join :added "4.1"}
(fact "throws for unsupported js thread joins"

  (!.js
    (xt/x:thread-join {}))
  => (throws))

^{:refer xt.lang.spec-runtime-os/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :js
    (xt/x:with-delay 20
                     (fn []
                       (repl/notify "OK"))
                     ))
  => "LATER")

^{:refer xt.lang.spec-runtime-os/x:start-interval :added "4.1"}
(fact "starts an interval handle"

  ^{:seedgen/base {:python {:suppress true}}}
  (notify/wait-on :js
    (var it nil)
    (:= it
        (xt/x:start-interval
         50
         (fn []
           (xt/x:stop-interval it)
           (repl/notify "hello")))))
  => "hello")

^{:refer xt.lang.spec-runtime-os/x:stop-interval :added "4.1"}
(fact "stops an interval handle"

  ^{:seedgen/base {:python {:suppress true}}}
  (!.js
    (var it (xt/x:start-interval (fn []) 50))
    (xt/x:stop-interval it))
  => nil)
