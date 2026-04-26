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

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:slurp-file path opts cb))))
    (xt/for:return [[out err] (slurp-fn "project.clj"
                                        {}
                                        (xt/x:callback)) ]
      {:success (repl/notify out)})
    (return true))
  => string?)

^{:refer xt.lang.spec-runtime-os/x:spit-file :added "4.1"}
(fact "writes file content through callbacks"

  ^{:seedgen/base {:all {:suppress true}}}
  (let [path "test-scratch/out.tmp"
        out  (notify/wait-on :js
               (spec-os/x:spit-file "test-scratch/out.tmp"
                               "hello world"
                               {}
                               (fn [err _]
                                 (spec-os/x:slurp-file "test-scratch/out.tmp"
                                                  {}
                                                  (fn [inner-err res]
                                                    (repl/notify res))))))]
    (.delete (java.io.File. path))
    out)
  => "hello world")

^{:refer xt.lang.spec-runtime-os/x:shell :added "4.1"}
(fact "executes shell commands asynchronously"
  
  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (xt/for:return [[out err] (spec-os/x:shell "printf hello"
                                               {}
                                               (xt/x:callback))]
      {:success (repl/notify out)
       :error   (repl/notify "ERR")})
    (return true))
  => #"hello")

^{:refer xt.lang.spec-runtime-os/x:shell :added "4.1"}
(fact "supports transitional shell callback arglists"
  
  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (do:> (spec-os/x:shell "printf hello"
                           {:success (fn [res]
                                       (repl/notify res))
                            :error   (fn [err]
                                       (repl/notify "ERR"))})))
  => #"hello")


^{:refer xt.lang.spec-runtime-os/x:thread-spawn :added "4.1"}
(fact "spawns js promise-backed threads"

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (spec-os/x:thread-spawn (fn []
                              (repl/notify "OK"))))
  => "OK")

^{:refer xt.lang.spec-runtime-os/x:thread-join :added "4.1"}
(fact "throws for unsupported js thread joins"

  ^{:seedgen/base {:all {:suppress true}}}
  (!.js
    (spec-os/x:thread-join {}))
  => (throws))

^{:refer xt.lang.spec-runtime-os/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (spec-os/x:with-delay 20
                          (fn []
                            (repl/notify "OK"))))
  => "LATER")

^{:refer xt.lang.spec-runtime-os/x:start-interval :added "4.1"}
(fact "starts an interval handle"

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (var it nil)
    (:= it
        (spec-os/x:start-interval
         50
         (fn []
           (spec-os/x:stop-interval it)
           (repl/notify "hello")))))
  => "hello")

^{:refer xt.lang.spec-runtime-os/x:stop-interval :added "4.1"}
(fact "stops an interval handle"

  ^{:seedgen/base {:all {:suppress true}}}
  (!.js
    (var it (spec-os/x:start-interval (fn []) 50))
    (spec-os/x:stop-interval it))
  => nil)
