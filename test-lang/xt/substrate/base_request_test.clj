(ns xt.substrate.base-request-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-request :as req]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-request :as req]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate :as node]
             [xt.substrate.base-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-request/ensure-promise :added "4.1"}
(fact "normalises raw values to promises"

  (!.js
    [(promise/x:promise-native? (req/ensure-promise {:ok true}))])
  => [true]

  (!.lua
    [(promise/x:promise-native? (req/ensure-promise {:ok true}))])
  => [true]

  (!.py
    [(promise/x:promise-native? (req/ensure-promise {:ok true}))])
  => [true])

^{:refer xt.substrate.base-request/add-pending :added "4.1"
  :setup [(def +out+
            (just-in
             [true
              1
              (just-in [{"ok" true}])
              empty?
              empty?]))]}
(fact "tracks and settles pending request entries"

  (!.js
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
     {:transport_id "peer-a"})
    (var before (xt/x:obj-keys (. n ["pending"])))
    (req/settle-pending
     n
     (frame/response-ok-frame (. request ["id"]) "room/a" {:ok true} nil))
    [(xt/x:is-string? (. request ["id"]))
     (xt/x:len before)
     resolved
     rejected
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+

  (!.lua
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
     {:transport_id "peer-a"})
    (var before (xt/x:obj-keys (. n ["pending"])))
    (req/settle-pending
     n
     (frame/response-ok-frame (. request ["id"]) "room/a" {:ok true} nil))
    [(xt/x:is-string? (. request ["id"]))
     (xt/x:len before)
     resolved
     rejected
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+

  (!.py
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
     {:transport_id "peer-a"})
    (var before (xt/x:obj-keys (. n ["pending"])))
    (req/settle-pending
     n
     (frame/response-ok-frame (. request ["id"]) "room/a" {:ok true} nil))
    [(xt/x:is-string? (. request ["id"]))
     (xt/x:len before)
     resolved
     rejected
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+)

^{:refer xt.substrate.base-request/remove-pending :added "4.1"
  :setup [(def +out+ (just-in [true empty?]))]}
(fact "removes pending request entries by id"

  (!.js
    (var n (node/node-create {}))
    (var request (frame/request-frame "room/a" "echo" [] nil))
    (req/add-pending
     n
     request
     (fn [value] (return value))
     (fn [value] (return value))
     {:transport_id "peer-a"})
    (var removed (req/remove-pending n (. request ["id"])))
    [(xt/x:is-string? (. (. removed ["request"]) ["id"]))
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+

  (!.lua
    (var n (node/node-create {}))
    (var request (frame/request-frame "room/a" "echo" [] nil))
    (req/add-pending
     n
     request
     (fn [value] (return value))
     (fn [value] (return value))
     {:transport_id "peer-a"})
    (var removed (req/remove-pending n (. request ["id"])))
    [(xt/x:is-string? (. (. removed ["request"]) ["id"]))
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+

  (!.py
    (var n (node/node-create {}))
    (var request (frame/request-frame "room/a" "echo" [] nil))
    (req/add-pending
     n
     request
     (fn [value] (return value))
     (fn [value] (return value))
     {:transport_id "peer-a"})
    (var removed (req/remove-pending n (. request ["id"])))
    [(xt/x:is-string? (. (. removed ["request"]) ["id"]))
     (xt/x:obj-keys (. n ["pending"]))])
  => +out+)

^{:refer xt.substrate.base-request/settle-pending :added "4.1"
  :setup [(def +out+ (just-in [true [true] empty?]))]}
(fact "settles pending requests using response reply ids"

  (!.js
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
  => +out+

  (!.lua
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
  => +out+

  (!.py
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
  => +out+)

^{:refer xt.substrate.base-request/invoke-handler :added "4.1"}
(fact "invokes shared handlers against the selected space"

  (!.js
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

  (!.js
    (var out
      (req/response-body
       (frame/response-ok-frame "req-1"
                                "room/a"
                                {:ok true}
                                nil)))
    [(promise/x:promise-native? out)])
  => [true]

  (!.lua
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

  (!.lua
    (var out
      (req/response-body
       (frame/response-ok-frame "req-1"
                                "room/a"
                                {:ok true}
                                nil)))
    [(promise/x:promise-native? out)])
  => [true]

  (!.py
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

  (!.py
    (var out
      (req/response-body
       (frame/response-ok-frame "req-1"
                                "room/a"
                                {:ok true}
                                nil)))
    [(promise/x:promise-native? out)])
  => [true])

^{:refer xt.substrate.base-request/response-body :added "4.1"}
(fact "normalises ok responses into promise results"

  (!.js
    [(promise/x:promise-native?
      (req/response-body
       (frame/response-ok-frame "req-1" "room/a" {:ok true} nil)))])
  => [true]

  (!.lua
    [(promise/x:promise-native?
      (req/response-body
       (frame/response-ok-frame "req-1" "room/a" {:ok true} nil)))])
  => [true]

  (!.py
    [(promise/x:promise-native?
      (req/response-body
       (frame/response-ok-frame "req-1" "room/a" {:ok true} nil)))])
  => [true])

(comment
  (s/snapto '[xt.substrate.base-request])
  (s/seedgen-langremove '[xt.substrate.base-request] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.substrate.base-request] {:lang [:lua :python] :write true}))
