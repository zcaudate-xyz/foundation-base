(ns
 xtbench.js.sys.cache-throttle-test
 (:require
  [rt.nginx.script :as script]
  [xt.lang.common-notify :as notify]
  [std.lang :as l])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.lang.common-data :as xtd]
   [xt.lang.common-lib :as k]
   [xt.lang.common-repl :as repl]
   [xt.sys.cache-throttle :as throttle]
   [xt.sys.cache-common :as cache]
   [js.core :as j]]})

(defn
 create-resty-params
 "creates default resty params"
 {:added "4.0"}
 ([]
  (script/write
   [[:client-body-buffer-size "1m"]
    [:variables-hash-max-size 2048]
    [:variables-hash-bucket-size 128]
    [:lua-shared-dict [:GLOBAL "20k"]]
    [:lua-shared-dict [:WS_DEBUG "20k"]]
    [:lua-shared-dict [:ES_DEBUG "20k"]]])))

(fact:global
 {:setup
  [(l/rt:restart)
   (notify/wait-on
    [:js 5000]
    (:= (!:G window) {})
    (:=
     (!:G LocalStorage)
     (. (require "node-localstorage") LocalStorage))
    (:=
     window.localStorage
     (new LocalStorage "./test-scratch/localstorage"))
    (repl/notify true))],
  :teardown [(l/rt:stop)]})

^{:refer xt.sys.cache-throttle/throttle-key, :added "0.1"}
(fact
 "creates the throttle key"
 ^{:hidden true}
 (!.js
  (throttle/throttle-key
   (throttle/throttle-create "SYNC_FRAME" (fn []) nil)
   "active"))
 =>
 "__throttle__:SYNC_FRAME:active")

^{:refer xt.sys.cache-throttle/throttle-create, :added "0.1"}
(fact
 "creates a throttle"
 ^{:hidden true}
 (set
  (!.js
   (xtd/obj-keys (throttle/throttle-create "SYNC_FRAME" (fn []) nil))))
 =>
 #{"handler" "tag" "now_fn"})

^{:refer xt.sys.cache-throttle/throttle-run-async, :added "0.1"}
(fact
 "runs a throttle"
 ^{:hidden true}
 (notify/wait-on
  :js
  (:=
   (!:G THT)
   (throttle/throttle-create
    "SYNC_FRAME"
    (fn [] (return (j/future-delayed [100] (repl/notify true))))
    nil))
  (throttle/throttle-run-async THT "default"))
 =>
 true)

^{:refer xt.sys.cache-throttle/throttle-run, :added "0.1"}
(fact
 "runs a throttle"
 ^{:hidden true}
 (!.js
  (:=
   (!:G THT)
   (throttle/throttle-create
    "SYNC_FRAME"
    (fn [] (return (j/future-delayed [100] nil)))
    nil))
  [(k/nil? (throttle/throttle-run THT "default"))
   (k/nil? (throttle/throttle-run THT "default"))
   (k/nil? (throttle/throttle-run THT "default"))])
 =>
 [false true true])
