(ns xt.substrate.base-json-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.base-frame :as frame]
             [xt.substrate.base-json :as node-json]
             [xt.substrate.base-router :as router]
             [xt.lang.spec-base :as xt]]})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-json/frame-kind? :added "4.1"}
(fact "recognises supported JSON wire kinds"
  (!.js
   [(node-json/frame-kind? frame/KIND_REQUEST)
    (node-json/frame-kind? frame/KIND_RESPONSE)
    (node-json/frame-kind? frame/KIND_STREAM)
    (node-json/frame-kind? router/KIND_SUBSCRIBE)
    (node-json/frame-kind? "unknown")])
  => [true true true true false])

^{:refer xt.substrate.base-json/valid-frame? :added "4.1"}
(fact "validates request, response and stream frames"
  (!.js
   [(node-json/valid-frame?
     {"kind" "request"
      "id" "req-1"
      "space" "room/a"
      "action" "demo/echo"
      "args" ["ping"]})
    (node-json/valid-frame?
     {"kind" "response"
      "id" "res-1"
      "space" "room/a"
      "reply_to" "req-1"
      "status" "ok"})
    (node-json/valid-frame?
     {"kind" "stream"
      "id" "evt-1"
      "space" "room/a"
      "signal" "demo/update"})
    (node-json/valid-frame?
     {"kind" "request"
      "id" "req-2"
      "space" "room/a"
      "action" "demo/echo"
      "args" {"bad" true}})
    (node-json/valid-frame?
     {"kind" "invalid"
      "id" "req-3"
      "space" "room/a"})])
  => [true true true false false])

^{:refer xt.substrate.base-json/normalize-error :added "4.1"}
(fact "normalises arbitrary errors into JSON-safe maps"
  (!.js
   [(node-json/normalize-error nil)
    (node-json/normalize-error "broken")
    (node-json/normalize-error {"error" "denied"})
    (node-json/normalize-error {"status" "error"})
    (node-json/normalize-error 42)])
  => [nil
      {"message" "broken"}
      {"error" "denied"
       "message" "denied"}
      {"status" "error"
       "message" "error"}
      {"message" "42"}])

^{:refer xt.substrate.base-json/normalize-frame :added "4.1"}
(fact "normalises response error payloads before emission"
  (!.js
   [(node-json/normalize-frame
     {"kind" "response"
      "id" "res-1"
      "space" "room/a"
      "reply_to" "req-1"
      "status" "error"
      "error" {"error" "denied"}})
    (node-json/normalize-frame
     {"kind" "request"
      "id" "req-1"
      "space" "room/a"
      "action" "demo/echo"
      "args" []})])
  => [{"kind" "response"
       "id" "res-1"
       "space" "room/a"
       "reply_to" "req-1"
       "status" "error"
       "error" {"error" "denied"
                "message" "denied"}}
      {"kind" "request"
       "id" "req-1"
       "space" "room/a"
       "action" "demo/echo"
       "args" []}])

^{:refer xt.substrate.base-json/encode-frame :added "4.1"}
(fact "encodes validated frames as JSON text"
  (!.js
   (xt/x:json-decode
    (node-json/encode-frame
     {"kind" "response"
      "id" "res-1"
      "space" "room/a"
      "reply_to" "req-1"
      "status" "error"
      "error" {"error" "denied"}})))
  => {"kind" "response"
      "id" "res-1"
      "space" "room/a"
      "reply_to" "req-1"
      "status" "error"
      "error" {"error" "denied"
               "message" "denied"}})

^{:refer xt.substrate.base-json/decode-frame :added "4.1"}
(fact "decodes JSON text and validates direct frame inputs"
  (!.js
   [(node-json/decode-frame
     "{\"kind\":\"stream\",\"id\":\"evt-1\",\"space\":\"room/a\",\"signal\":\"demo/update\",\"data\":{\"ok\":true}}")
    (node-json/decode-frame
     {"kind" "request"
      "id" "req-1"
      "space" "room/a"
      "action" "demo/echo"
      "args" ["ping"]})])
  => [{"kind" "stream"
       "id" "evt-1"
       "space" "room/a"
       "signal" "demo/update"
       "data" {"ok" true}}
      {"kind" "request"
       "id" "req-1"
       "space" "room/a"
       "action" "demo/echo"
       "args" ["ping"]}])
