(ns xt.lang.spec-os-test
  (:use code.test)
  (:require [clojure.set :as set]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-os :as spec-os]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-promise]]})

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
         (fn [command root cb]
           (return
            (spec-os/x:shell command root cb))))
    (shell-fn "printf hello"
              (spec-os/x:pwd)
              (fn [err out]
                (repl/notify out))))
  => string?

  (notify/wait-on :python
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

  (!.js
    (var resolve-fn
         (fn [path]
           (return
            (spec-os/x:file-resolve (spec-os/x:pwd) path))))
    (resolve-fn "project.clj"))
  => (str (std.fs/file "project.clj"))

  (!.lua
    (var resolve-fn
         (fn [path]
           (return
            (spec-os/x:file-resolve (spec-os/x:pwd) path))))
    (resolve-fn "project.clj"))
  => (str (std.fs/file "project.clj"))

  (!.py
    (var resolve-fn
         (fn [path]
           (return
            (spec-os/x:file-resolve (spec-os/x:pwd) path))))
    (resolve-fn "project.clj"))
  => (str (std.fs/file "project.clj")))

^{:refer xt.lang.spec-os/x:file-read :added "4.1"}
(fact "reads file content as promised bytes"

  (notify/wait-on :js
    (promise/x:promise-then
     (spec-os/x:file-read (xt/x:cat (spec-os/x:pwd) "/" ".gitignore"))
     (fn [out]
       (repl/notify (xt/x:str-decode out)))))
  => string?

  (notify/wait-on :lua
    (promise/x:promise-then
     (spec-os/x:file-read (xt/x:cat (spec-os/x:pwd) "/" ".gitignore"))
     (fn [out]
       (repl/notify (xt/x:str-decode out)))))
  => string?

  (notify/wait-on :python
    (promise/x:promise-then
     (spec-os/x:file-read (xt/x:cat (spec-os/x:pwd) "/" ".gitignore"))
     (fn [out]
       (repl/notify (xt/x:str-decode out)))))
  => string?)

^{:refer xt.lang.spec-os/x:file-write :added "4.1"}
(fact "writes bytes and resolves before a promised read"

  (notify/wait-on :js
    (promise/x:promise-then
     (spec-os/x:file-write "/tmp/spec-os-out-js.tmp"
                           (xt/x:str-encode "hello world"))
     (fn [_]
       (return
        (promise/x:promise-then
         (spec-os/x:file-read "/tmp/spec-os-out-js.tmp")
         (fn [out]
           (repl/notify (xt/x:str-decode out))))))))
  => "hello world"

  (notify/wait-on :lua
    (promise/x:promise-then
     (spec-os/x:file-write "/tmp/spec-os-out-lua.tmp"
                           (xt/x:str-encode "hello world"))
     (fn [_]
       (return
        (promise/x:promise-then
         (spec-os/x:file-read "/tmp/spec-os-out-lua.tmp")
         (fn [out]
           (repl/notify (xt/x:str-decode out))))))))
  => "hello world"

  (notify/wait-on :python
    (promise/x:promise-then
     (spec-os/x:file-write "/tmp/spec-os-out-python.tmp"
                           (xt/x:str-encode "hello world"))
     (fn [_]
       (return
        (promise/x:promise-then
         (spec-os/x:file-read "/tmp/spec-os-out-python.tmp")
         (fn [out]
           (repl/notify (xt/x:str-decode out))))))))
  => "hello world")

(comment
  
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:dart] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-os] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.spec-os {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-os {:lang [:lua :python] :write true}))
