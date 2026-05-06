(ns xt.event.node-frame-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-frame/rand-id :added "4.1"}
(fact "creates ids with the requested prefix"

  (!.js
    (var id (frame/rand-id "sub-" 4))
    [(xt/x:str-len id)
     (xt/x:is-string? id)])
  => [8 true]

  (!.lua
    (var id (frame/rand-id "sub-" 4))
    [(xt/x:str-len id)
     (xt/x:is-string? id)])
  => [8 true]

  (!.py
    (var id (frame/rand-id "sub-" 4))
    [(xt/x:str-len id)
     (xt/x:is-string? id)])
  => [8 true])

^{:refer xt.event.node-frame/frame :added "4.1"}
(fact "defaults meta and space when building frames directly"

  (!.js
    (var base (frame/frame "request" "req-1" nil nil {:action "echo"}))
    (var err  (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. base ["space"])
     (. base ["meta"])
     (. err ["status"])
     (. err ["error"] ["message"])])
  => ["__NODE__" {} "error" "boom"]

  (!.lua
    (var base (frame/frame "request" "req-1" nil nil {:action "echo"}))
    (var err  (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. base ["space"])
     (. base ["meta"])
     (. err ["status"])
     (. err ["error"] ["message"])])
  => ["__NODE__" {} "error" "boom"]

  (!.py
    (var base (frame/frame "request" "req-1" nil nil {:action "echo"}))
    (var err  (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. base ["space"])
     (. base ["meta"])
     (. err ["status"])
     (. err ["error"] ["message"])])
  => ["__NODE__" {} "error" "boom"])

^{:refer xt.event.node-frame/request-frame :added "4.1"}
(fact "constructs request, response, and stream frames"

  (!.js
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
                 {:request-id (. request ["id"])}))
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
      true]

  (!.lua
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
                 {:request-id (. request ["id"])}))
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
      true]

  (!.py
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
                 {:request-id (. request ["id"])}))
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

  (!.js
    (var response (frame/response-frame "req-1" "space/a" "ok" {:ok true} nil {:tag "v"}))
    [(. response ["kind"])
     (. response ["reply_to"])
     (. response ["status"])
     (. response ["data"] ["ok"])
     (. response ["meta"] ["tag"])])
  => ["response" "req-1" "ok" true "v"]

  (!.lua
    (var response (frame/response-frame "req-1" "space/a" "ok" {:ok true} nil {:tag "v"}))
    [(. response ["kind"])
     (. response ["reply_to"])
     (. response ["status"])
     (. response ["data"] ["ok"])
     (. response ["meta"] ["tag"])])
  => ["response" "req-1" "ok" true "v"]

  (!.py
    (var response (frame/response-frame "req-1" "space/a" "ok" {:ok true} nil {:tag "v"}))
    [(. response ["kind"])
     (. response ["reply_to"])
     (. response ["status"])
     (. response ["data"] ["ok"])
     (. response ["meta"] ["tag"])])
  => ["response" "req-1" "ok" true "v"])

^{:refer xt.event.node-frame/response-ok-frame :added "4.1"}
(fact "constructs successful response frames"

  (!.js
    (var response (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
    [(. response ["status"])
     (. response ["data"] ["ok"])
     (xt/x:nil? (. response ["error"]))])
  => ["ok" true true]

  (!.lua
    (var response (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
    [(. response ["status"])
     (. response ["data"] ["ok"])
     (xt/x:nil? (. response ["error"]))])
  => ["ok" true true]

  (!.py
    (var response (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
    [(. response ["status"])
     (. response ["data"] ["ok"])
     (xt/x:nil? (. response ["error"]))])
  => ["ok" true true])

^{:refer xt.event.node-frame/response-error-frame :added "4.1"}
(fact "constructs errored response frames"

  (!.js
    (var response (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. response ["status"])
     (xt/x:nil? (. response ["data"]))
     (. response ["error"] ["message"])])
  => ["error" true "boom"]

  (!.lua
    (var response (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. response ["status"])
     (xt/x:nil? (. response ["data"]))
     (. response ["error"] ["message"])])
  => ["error" true "boom"]

  (!.py
    (var response (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. response ["status"])
     (xt/x:nil? (. response ["data"]))
     (. response ["error"] ["message"])])
  => ["error" true "boom"])

^{:refer xt.event.node-frame/stream-frame :added "4.1"}
(fact "constructs stream frames with optional causes"

  (!.js
    (var stream (frame/stream-frame "space/a" "event/ping" {:value 1} {:tag "v"} {:request-id "req-1"}))
    [(. stream ["kind"])
     (. stream ["signal"])
     (. stream ["data"] ["value"])
     (. stream ["meta"] ["tag"])
     (. stream ["cause"] ["request_id"])])
  => ["stream" "event/ping" 1 "v" "req-1"]

  (!.lua
    (var stream (frame/stream-frame "space/a" "event/ping" {:value 1} {:tag "v"} {:request-id "req-1"}))
    [(. stream ["kind"])
     (. stream ["signal"])
     (. stream ["data"] ["value"])
     (. stream ["meta"] ["tag"])
     (. stream ["cause"] ["request_id"])])
  => ["stream" "event/ping" 1 "v" "req-1"]

  (!.py
    (var stream (frame/stream-frame "space/a" "event/ping" {:value 1} {:tag "v"} {:request-id "req-1"}))
    [(. stream ["kind"])
     (. stream ["signal"])
     (. stream ["data"] ["value"])
     (. stream ["meta"] ["tag"])
     (. stream ["cause"] ["request_id"])])
  => ["stream" "event/ping" 1 "v" "req-1"])

^{:refer xt.event.node-frame/request-frame? :added "4.1"}
(fact "detects request frames"

  (!.js
    [(frame/request-frame? (frame/request-frame "space/a" "echo" [] nil))
     (frame/request-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))])
  => [true false]

  (!.lua
    [(frame/request-frame? (frame/request-frame "space/a" "echo" [] nil))
     (frame/request-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))])
  => [true false]

  (!.py
    [(frame/request-frame? (frame/request-frame "space/a" "echo" [] nil))
     (frame/request-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))])
  => [true false])

^{:refer xt.event.node-frame/response-frame? :added "4.1"}
(fact "detects response frames"

  (!.js
    [(frame/response-frame? (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
     (frame/response-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false]

  (!.lua
    [(frame/response-frame? (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
     (frame/response-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false]

  (!.py
    [(frame/response-frame? (frame/response-ok-frame "req-1" "space/a" {:ok true} nil))
     (frame/response-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false])

^{:refer xt.event.node-frame/stream-frame? :added "4.1"}
(fact "detects stream frames"

  (!.js
    [(frame/stream-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))
     (frame/stream-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false]

  (!.lua
    [(frame/stream-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))
     (frame/stream-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false]

  (!.py
    [(frame/stream-frame? (frame/stream-frame "space/a" "event/ping" {} nil nil))
     (frame/stream-frame? (frame/request-frame "space/a" "echo" [] nil))])
  => [true false])
