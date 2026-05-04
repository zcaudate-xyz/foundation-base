(ns xt.event.node-frame-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

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
      true])

^{:refer xt.event.node-frame/frame :added "4.1"}
(fact "defaults meta and space when building frames directly"

  (!.js
    (var base (frame/frame "request" "req-1" nil nil {:action "echo"}))
    (var err  (frame/response-error-frame "req-1" "space/a" {:message "boom"} nil))
    [(. base ["space"])
     (. base ["meta"])
     (. err ["status"])
     (. err ["error"] ["message"])])
  => ["$node" {} "error" "boom"])
