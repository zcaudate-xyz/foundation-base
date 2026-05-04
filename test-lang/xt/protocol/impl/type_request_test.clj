(ns xt.protocol.impl.type-request-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.type-request :as req]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.impl.type-request :as req]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.impl.type-request :as req]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.type-request/request-runtime-create :added "4.1"}
(fact "creates wrapped request runtimes"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (. runtime ["::"]))
  => "type.request")

^{:refer xt.protocol.impl.type-request/request-runtime? :added "4.1"}
(fact "checks request runtime wrappers"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    [(req/request-runtime? runtime)
     (req/request-runtime? nil)])
  => [true false])

^{:refer xt.protocol.impl.type-request/require-request-runtime :added "4.1"}
(fact "returns validated request runtimes"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (. (req/require-request-runtime runtime) ["::"]))
  => "type.request")

^{:refer xt.protocol.impl.type-request/request :added "4.1"}
(fact "dispatches request calls through the wrapped implementation"

  (!.js
    (var calls [])
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (x:arr-push calls [space action args])
                      (return {:space space
                               :args args}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (var out (req/request runtime {} "room/a" "echo" [1 2] nil))
    [(. out ["space"])
     (. out ["args"])
     calls])
  => ["room/a" [1 2] [["room/a" "echo" [1 2]]]])

^{:refer xt.protocol.impl.type-request/receive-request :added "4.1"}
(fact "dispatches receive-request calls through the wrapped implementation"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return [(. frame ["action"])
                                       (. ctx ["transport-id"])]))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (req/receive-request runtime {} {"action" "echo"} {"transport-id" "peer-a"}))
  => ["echo" "peer-a"])

^{:refer xt.protocol.impl.type-request/receive-response :added "4.1"}
(fact "dispatches receive-response calls through the wrapped implementation"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return (. frame ["reply-to"])))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (req/receive-response runtime {} {"reply-to" "req-1"}))
  => "req-1")

^{:refer xt.protocol.impl.type-request/respond-ok :added "4.1"}
(fact "dispatches respond-ok calls through the wrapped implementation"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return [(. request ["id"])
                                  (. data ["ok"])
                                  (. ctx ["transport-id"])]))
           :respond_error (fn [node request error meta ctx]
                            (return error))}))
    (req/respond-ok runtime {} {"id" "req-1"} {"ok" true} nil {"transport-id" "peer-a"}))
  => ["req-1" true "peer-a"])

^{:refer xt.protocol.impl.type-request/respond-error :added "4.1"}
(fact "dispatches respond-error calls through the wrapped implementation"

  (!.js
    (var runtime
         (req/request-runtime-create
          {:request (fn [node space action args meta]
                      (return {:space space}))
           :receive_request (fn [node frame ctx]
                              (return frame))
           :receive_response (fn [node frame]
                               (return frame))
           :respond_ok (fn [node request data meta ctx]
                         (return data))
           :respond_error (fn [node request error meta ctx]
                            (return [(. request ["id"])
                                     (. error ["message"])
                                     (. ctx ["transport-id"])]))}))
    (req/respond-error runtime {} {"id" "req-1"} {"message" "boom"} nil {"transport-id" "peer-a"}))
  => ["req-1" "boom" "peer-a"])
