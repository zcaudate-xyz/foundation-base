(ns xt.lang.spec-os-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-os/x:pwd :added "4.1"}
(fact "gets the current pwd"
  
  (!.js
    (spec-os/x:pwd))
  => string?

  (!.lua
    (spec-os/x:pwd))
  => string?

  (!.py
    (spec-os/x:pwd))
  => string?)

^{:refer xt.lang.spec-os/x:shell :added "4.1"}
(fact "supports transitional shell callback arglists"

  (notify/wait-on :js
    (var shell-fn
         (fn [command root cb]
           (return
            (spec-os/x:shell command root cb))))
    (shell-fn "printf hello"
              (spec-os/x:pwd)
              (fn [err out]
                (repl/notify out))))
  => string?

 (notify/wait-on :lua
   (var shell-fn
        (fn [command opts cb]
          (return
           (spec-os/x:shell command opts cb))))
   (shell-fn "printf hello"
             (spec-os/x:pwd)
             (fn [err out]
               (repl/notify out))))
  => string?

  (l/with:print-all
    (notify/wait-on :python
      (var shell-fn
           (fn [command opts cb]
             (return
              (spec-os/x:shell command opts cb))))
      (shell-fn "printf hello"
                {:root (spec-os/x:pwd)}
                (fn [err out]
                  (repl/notify out)))))
  => string?)

^{:refer xt.lang.spec-os/x:file-resolve :added "4.1"}
(fact "file-resolve"
  
  (!.js
    (spec-os/x:file-resolve (spec-os/x:pwd) "project.clj"))
  => (str (std.fs/file "project.clj"))

  (!.lua
    (spec-os/x:file-resolve (spec-os/x:pwd) "project.clj"))
  => (str (std.fs/file "project.clj"))

  (!.py
    (spec-os/x:file-resolve (spec-os/x:pwd) "project.clj"))
  => (str (std.fs/file "project.clj")))

^{:refer xt.lang.spec-os/x:file-slurp :added "4.1"}
(fact "reads file content through callbacks"

  (l/with:print-all
    (notify/wait-on :js
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "project.clj")
                {}
                (fn [err out]
                  (repl/notify out)))))
  => string?

  (l/with:print-all
    (notify/wait-on :lua
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "project.clj")
                {}
                (fn [err out]
                  (repl/notify out)))))
  => string?

  (l/with:print-all
    (notify/wait-on :python
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (slurp-fn "project.clj"
                {}
                (fn [err out]
                  (repl/notify out)))))
  => string?)

^{:refer xt.lang.spec-os/x:file-spit :added "4.1"}
(fact "writes file content through callbacks"

  (l/with:print-all
    (notify/wait-on :js
      (var spit-fn
           (fn [path content opts cb]
             (return
              (spec-os/x:file-spit path content opts cb))))
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (spit-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
               "hello world"
               {}
               (fn [err out]
                 (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
                           {}
                           (fn [err out]
                             (repl/notify out)))))))
  => "hello world"

  (l/with:print-all
    (notify/wait-on :lua
      (var spit-fn
           (fn [path content opts cb]
             (return
              (spec-os/x:file-spit path content opts cb))))
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (spit-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
               "hello world"
               {}
               (fn [err out]
                 (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
                           {}
                           (fn [err out]
                             (repl/notify out)))))))
  => "hello world"

  (l/with:print-all
    (notify/wait-on :python
      (var spit-fn
           (fn [path content opts cb]
             (return
              (spec-os/x:file-spit path content opts cb))))
      (var slurp-fn
           (fn [path opts cb]
             (return
              (spec-os/x:file-slurp path opts cb))))
      (spit-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
               "hello world"
               {}
               (fn [err out]
                 (slurp-fn (xt/x:cat (spec-os/x:pwd) "/" "test-scratch/out.tmp")
                           {}
                           (fn [err out]
                             (repl/notify out)))))))
  => "hello world")

(comment
  
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:dart] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.spec-os {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-os
{:lang [:lua :python] :write true}))
