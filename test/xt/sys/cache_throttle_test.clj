(ns xt.sys.cache-throttle-test
  (:require [rt.nginx.config :as config]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-notify :as notify]
             [xt.lang.base-repl :as repl]
             [xt.sys.cache-throttle :as throttle]
             [xt.sys.cache-common :as cache]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :config  {:exec ["resty" "--http-conf" (config/create-resty-params) "-e"]}
   :require [[xt.lang.base-lib :as k]
             [xt.sys.cache-throttle :as throttle]
             [xt.sys.cache-common :as cache]
             [lua.nginx :as n]]})

(fact:global
 {:setup    [(l/rt:restart)
             (notify/wait-on [:js 5000]
               (:= (!:G window)  (require "window"))
               (:= (!:G LocalStorage)  (. (require "node-localstorage")
                                          LocalStorage))
               (:= window.localStorage (new LocalStorage "./test-scratch/localstorage"))
               (repl/notify true))]
  :teardown [(l/rt:stop)]})

^{:refer xt.sys.cache-throttle/throttle-key :added "0.1"}
(fact "creates the throttle key"
  ^:hidden
  
  (!.js
   (throttle/throttle-key (throttle/throttle-create "SYNC_FRAME"
                                              (fn [])
                                              nil)
                          "active"))
  => "__throttle__:SYNC_FRAME:active"

  (!.lua
   (throttle/throttle-key (throttle/throttle-create "SYNC_FRAME"
                                              (fn [])
                                              nil)
                          "active"))
  => "__throttle__:SYNC_FRAME:active")

^{:refer xt.sys.cache-throttle/throttle-create :added "0.1"}
(fact "creates a throttle"
  ^:hidden

  (set (!.js
        (k/obj-keys (throttle/throttle-create "SYNC_FRAME"
                                              (fn [])
                                              nil))))
  => #{"handler" "tag" "now_fn"}
  
  (set (!.lua
        (k/obj-keys (throttle/throttle-create "SYNC_FRAME"
                                              (fn [])
                                              nil))))
  => #{"handler" "tag" "now_fn"})

^{:refer xt.sys.cache-throttle/throttle-run-async :added "0.1"}
(fact "runs a throttle"
  ^:hidden
  
  (notify/wait-on :js
   (:= (!:G THT) (throttle/throttle-create "SYNC_FRAME"
                                           (fn []
                                             (return (j/future-delayed [100]
                                                       (repl/notify true))))
                                           nil))
   (throttle/throttle-run-async THT "default"))
  => true

  (!.lua
   (:= (!:G THT) (throttle/throttle-create "SYNC_FRAME"
                                        (fn []
                                          (n/sleep 0.1))
                                        nil))
   (type (throttle/throttle-run-async THT "default")))
  => "thread")

^{:refer xt.sys.cache-throttle/throttle-run :added "0.1"}
(fact "runs a throttle"
  ^:hidden
  
  (!.lua
   (:= (!:G THT) (throttle/throttle-create "SYNC_FRAME"
                                        (fn []
                                          (n/sleep 0.1))
                                        nil))
   [(k/nil? (throttle/throttle-run THT "default"))
    (k/nil? (throttle/throttle-run THT "default"))
    (k/nil? (throttle/throttle-run THT "default"))])
  => [false true true]

  (!.js
   (:= (!:G THT) (throttle/throttle-create "SYNC_FRAME"
                                           (fn []
                                             (return (j/future-delayed [100]
                                                       nil)))
                                           nil))
   [(k/nil? (throttle/throttle-run THT "default"))
    (k/nil? (throttle/throttle-run THT "default"))
    (k/nil? (throttle/throttle-run THT "default"))])
  => [false true true])

