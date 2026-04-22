(ns
 xtbench.lua.sys.cache-throttle-test
 (:require
  [rt.nginx.script :as script]
  [xt.lang.common-notify :as notify]
  [std.lang :as l])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :config
  {:exec
   ["resty"
    "--http-conf"
    "client_body_buffer_size 1m;\nvariables_hash_max_size 2048;\nvariables_hash_bucket_size 128;\nlua_shared_dict GLOBAL 20k;\nlua_shared_dict WS_DEBUG 20k;\nlua_shared_dict ES_DEBUG 20k;"
    "-e"]},
  :require
  [[xt.lang.common-data :as xtd]
   [xt.lang.common-lib :as k]
   [xt.sys.cache-throttle :as throttle]
   [xt.sys.cache-common :as cache]
   [lua.nginx :as n]]})

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
    [:lua 5000]
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
 (!.lua
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
  (!.lua
   (xtd/obj-keys (throttle/throttle-create "SYNC_FRAME" (fn []) nil))))
 =>
 #{"handler" "tag" "now_fn"})

^{:refer xt.sys.cache-throttle/throttle-run-async, :added "0.1"}
(fact
 "runs a throttle"
 ^{:hidden true}
 (!.lua
  (:=
   (!:G THT)
   (throttle/throttle-create "SYNC_FRAME" (fn [] (n/sleep 0.1)) nil))
  (type (throttle/throttle-run-async THT "default")))
 =>
 "thread")

^{:refer xt.sys.cache-throttle/throttle-run, :added "0.1"}
(fact
 "runs a throttle"
 ^{:hidden true}
 (!.lua
  (:=
   (!:G THT)
   (throttle/throttle-create "SYNC_FRAME" (fn [] (n/sleep 0.1)) nil))
  [(k/nil? (throttle/throttle-run THT "default"))
   (k/nil? (throttle/throttle-run THT "default"))
   (k/nil? (throttle/throttle-run THT "default"))])
 =>
 [false true true])
