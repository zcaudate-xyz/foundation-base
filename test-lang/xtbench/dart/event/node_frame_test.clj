(ns xtbench.dart.event.node-frame-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-frame/rand-id :added "4.1"}
(fact "creates ids with the requested prefix"

  (!.dt
    (var id (frame/rand-id "sub-" 4))
    [(xt/x:str-len id)
     (xt/x:is-string? id)])
  => [8 true])

^{:refer xt.event.node-frame/frame :added "4.1"}
(fact "defaults meta and space when building frames directly"

  (!.dt
    (var base (frame/frame "request" "req-1" nil nil {:action "echo"}))
    (var err  (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. base ["space"])
     (. base ["meta"])
     (. err ["status"])
     (. err ["error"] ["message"])])
  => ["__NODE__" {} "error" "boom"])

^{:refer xt.event.node-frame/request-frame :added "4.1"}
(fact "constructs request, response, and stream frames"

  (!.dt
    (var request (frame/request-frame "space/a"
                                      "echo"
                                      [1 2]
                                      {:label "req"}))
    (var response (frame/response-ok-frame
                   (. request ["id"])
                   (. request ["space"])
                   {:ok true}
                   {:label "res"}))
    (var stream (frame/stream-frame
                 "space/a"
                 "event/updated"
                 {:value 9}
                 {:label "evt"}
                 {:request_id (. request ["id"])}))
    [(xt/x:is-string? (frame/rand-id "x-" 4))
     (. request ["kind"])
     (. request ["space"])
     (. request ["action"])
     (. request ["args"])
     (frame/request-frame? request)
     (. response ["kind"])
     (xt/x:is-string? (. response ["reply_to"]))
     (. response ["status"])
     (frame/response-frame? response)
     (. stream ["kind"])
     (. stream ["signal"])
     (xt/x:is-string? (. stream ["cause"] ["request_id"]))
     (frame/stream-frame? stream)])
  => [true
      "request"
      "space/a"
      "echo"
      [1 2]
      true
      "response"
      true
      "ok"
      true
      "stream"
      "event/updated"
      true
      true])

^{:refer xt.event.node-frame/response-frame :added "4.1"}
(fact "constructs raw response frames with reply ids"

  (!.dt
    (var response (frame/response-frame "req-1" "space/a" "ok" {:ok true} nil {:tag "v"}))
    [(. response ["kind"])
     (. response ["reply_to"])
     (. response ["status"])
     (. response ["data"] ["ok"])
     (. response ["meta"] ["tag"])])
  => ["response" "req-1" "ok" true "v"])

^{:refer xt.event.node-frame/response-ok-frame :added "4.1"}
(fact "constructs successful response frames"

  (!.dt
    (var response (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
    [(. response ["status"])
     (. response ["data"] ["ok"])
     (xt/x:nil? (. response ["error"]))])
  => ["ok" true true])

^{:refer xt.event.node-frame/response-error-frame :added "4.1"}
(fact "constructs errored response frames"

  (!.dt
    (var response (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. response ["status"])
     (xt/x:nil? (. response ["data"]))
     (. response ["error"] ["message"])])
  => ["error" true "boom"])

^{:refer xt.event.node-frame/stream-frame :added "4.1"}
(fact "constructs stream frames with optional causes"

  (!.dt
    (var stream (frame/stream-frame "space/a" "event/ping" {:value 1} {:tag "v"} {:request_id "req-1"}))
    [(. stream ["kind"])
     (. stream ["signal"])
     (. stream ["data"] ["value"])
     (. stream ["meta"] ["tag"])
     (. stream ["cause"] ["request_id"])])
  => ["stream" "event/ping" 1 "v" "req-1"])

^{:refer xt.event.node-frame/request-frame? :added "4.1"}
(fact "detects request frames"

  (!.dt
    [(frame/request-frame? (frame/request-frame "space/a" "echo" [] nil))
     (frame/request-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))])
  => [true false])

^{:refer xt.event.node-frame/response-frame? :added "4.1"}
(fact "detects response frames"

  (!.dt
    [(frame/response-frame? (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
     (frame/response-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false])

^{:refer xt.event.node-frame/stream-frame? :added "4.1"}
(fact "detects stream frames"

  (!.dt
    [(frame/stream-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))
     (frame/stream-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false])

(comment
  (s/snapto '[xt.event.node-frame])
  (s/seedgen-langremove '[xt.event.node-frame] {:lang [:lua :python] :write true})
  (s/seedgen-langadd '[xt.event.node-frame] {:lang [:lua :python] :write true}))
