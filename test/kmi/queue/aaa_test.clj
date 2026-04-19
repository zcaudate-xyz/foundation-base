(ns kmi.queue.aaa-test
  (:require [rt.redis]
            [std.lang :as l])
  (:use code.test))

;; This is run before kmi.queue.common-test in order to
;; not get a connection error for the namespace

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17003
            :bench true}
   :require [[xt.lang.common-lib :as k :include [:fn]]
             [kmi.redis :as r]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

