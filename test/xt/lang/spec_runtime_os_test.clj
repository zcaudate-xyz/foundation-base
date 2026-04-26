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

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-runtime-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
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
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:slurp-file path opts cb))))
    (xt/for:return [[out err] (slurp-fn "project.clj"
                                        {}
                                        (xt/x:callback))]
      {:success (repl/notify out)})
    (return true))
  => string?

  
  (notify/wait-on :python
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:slurp-file path opts cb))))
    (xt/for:return [[out err] (slurp-fn "project.clj"
                                        {}
                                        (xt/x:callback))]
      {:success (repl/notify out)})
    (return true))
  => string?
  
  (notify/wait-on :lua
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:slurp-file path opts cb))))
    (xt/for:return [[out err] (slurp-fn "project.clj"
                                        {}
                                        (xt/x:callback))]
      {:success (repl/notify out)})
    (return true))
  )

^{:refer xt.lang.spec-runtime-os/x:spit-file :added "4.1"}
(fact "writes file content through callbacks"
  
  (notify/wait-on :js
    (var spit-fn
         (fn [path content opts cb]
           (return
            (spec-os/x:spit-file path content opts cb))))
    (var slurp-fn
         (fn [path opts cb]
           (return
            (spec-os/x:slurp-file path opts cb))))
    (xt/for:return [[out err] (spit-fn "test-scratch/out.tmp"
                                       "hello world"
                                       {}
                                       (xt/x:callback))]
      {:success (do (xt/for:return [[out err] (slurp-fn "test-scratch/out.tmp"
                                                        {}
                                                        (xt/x:callback))]
                      {:success (repl/notify out)}))})
    (return true))
  => "hello world")

^{:refer xt.lang.spec-runtime-os/x:shell :added "4.1"}
(fact "supports transitional shell callback arglists"


  (notify/wait-on :js
    (var shell-fn
         (fn [command opts cb]
           (return
            (spec-os/x:shell path opts cb))))
    (xt/for:return [[out err] (shell-fn "printf hello"
                                        {}
                                        (xt/x:callback))]
      {:success (repl/notify out)})
    (return true))
  => string?)

^{:refer xt.lang.spec-runtime-os/x:with-delay :added "4.1"}
(fact "delays asynchronous js computations"

  (notify/wait-on :js
    (spec-os/x:with-delay 20
                          (fn []
                            (repl/notify "OK"))))
  => "LATER"
  
  (notify/wait-on :python
    (spec-os/x:with-delay 20
                          (fn []
                            (repl/notify "OK"))))
  => "LATER"

  (notify/wait-on :lua
    (spec-os/x:with-delay 20
                          (fn []
                            (repl/notify "OK"))))
  => "LATER")

(comment
  
  (s/seedgen-benchadd '[xt.lang.spec-runtime-os] {:lang [:dart] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-runtime-os] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.spec-runtime-os {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-runtime-os {:lang [:lua :python] :write true}))
