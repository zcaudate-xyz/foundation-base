(ns xtbench.dart.event.node-request-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.node-frame :as frame]
             [xt.event.node :as node]
             [xt.event.node-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-request/ensure-promise :added "4.1"}
(fact "normalises raw values to promises"

  (!.dt
    [(promise/x:promise-native? (req/ensure-promise {:ok true}))])
  => [true])

^{:refer xt.event.node-request/add-pending :added "4.1"}
(fact "tracks and settles pending request entries"

  (!.dt
    (var n (node/node-create {}))
    (var resolved [])
    (var rejected [])
    (var request (frame/request-frame "room/a" "echo" [1 2] nil))
    (req/add-pending
     n
     request
     (fn [value]
       (xt/x:arr-push resolved value))
     (fn [value]
       (xt/x:arr-push rejected value))
     {:transport-id "peer-a"})
    (var before (xt/x:obj-keys (. n ["pending"])))
    (req/settle-pending
     n
     (frame/response-ok-frame (. request ["id"]) "room/a" {:ok true} nil))
    [(xt/x:is-string? (. request ["id"]))
     (xt/x:len before)
     [(. (xt/x:get-idx resolved 0) ["ok"])]
     rejected
     (xt/x:obj-keys (. n ["pending"]))])
  => [true
      1
      [true]
      []
      []])

^{:refer xt.event.node-request/remove-pending :added "4.1"}
(fact "removes pending request entries by id"

  (!.dt
    (var n (node/node-create {}))
    (var request (frame/request-frame "room/a" "echo" [] nil))
    (req/add-pending
     n
     request
     (fn [value] (return value))
     (fn [value] (return value))
     {:transport-id "peer-a"})
    (var removed (req/remove-pending n (. request ["id"])))
    [(xt/x:is-string? (. (. removed ["request"]) ["id"]))
     (xt/x:obj-keys (. n ["pending"]))])
  => [true []])

^{:refer xt.event.node-request/settle-pending :added "4.1"}
(fact "settles pending requests using response reply ids"

  (!.dt
    (var n (node/node-create {}))
    (var resolved [])
    (var request (frame/request-frame "room/a" "echo" [] nil))
    (req/add-pending
     n
     request
     (fn [value]
       (xt/x:arr-push resolved (. value ["ok"])))
     (fn [value]
       (xt/x:arr-push resolved false))
     nil)
    (var entry
      (req/settle-pending
       n
       (frame/response-ok-frame (. request ["id"]) "room/a" {:ok true} nil)))
    [(xt/x:is-string? (. (. entry ["request"]) ["id"]))
     resolved
     (xt/x:obj-keys (. n ["pending"]))])
  => [true [true] []])

^{:refer xt.event.node-request/invoke-handler :added "4.1"}
(fact "invokes shared handlers against the selected space"

  (!.dt
    (var n (node/node-create {}))
    (var calls [])
    (node/register-handler
     n
     "echo"
     (fn [space args request node]
       (xt/x:arr-push calls
                      [(. space ["id"])
                       args
                       (. request ["action"])])
       (return {:space (. space ["id"])
                :args args
                :action (. request ["action"])}))
     nil)
    (var out
      (req/invoke-handler
       n
       (frame/request-frame "room/a" "echo" [1 2] nil)))
    [(promise/x:promise-native? out)
     calls])
  => [true
      [["room/a" [1 2] "echo"]]]

  (!.dt
    (var out
      (req/response-body
       (frame/response-ok-frame "req-1"
                                "room/a"
                                {:ok true}
                                nil)))
    [(promise/x:promise-native? out)])
  => [true])

^{:refer xt.event.node-request/response-body :added "4.1"}
(fact "normalises ok responses into promise results"

  (!.dt
    [(promise/x:promise-native?
      (req/response-body
       (frame/response-ok-frame "req-1" "room/a" {:ok true} nil)))])
  => [true])
