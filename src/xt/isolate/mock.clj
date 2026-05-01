(ns xt.isolate.mock
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-trace :as trace]
             [xt.lang.spec-promise :as spec-promise]
             [xt.isolate.endpoint :as endpoint]
             [xt.isolate.frame :as frame]]})

;;
;; mock.clj - in-process mock transport for testing
;;
;; The mock endpoint simulates an isolate running in the same process.
;; It implements the same transport contract as any real transport:
;;
;;   isolate.emit  - sends ResponseFrames to registered outbound listeners
;;   transport.send   - drives endpoint-process directly (no IPC needed)
;;   transport.listen - registers a client handler for outbound frames
;;
;; Because it is purely in-process it works with any xtalk target
;; language (JS, Lua, Python, ...) without spawning workers or opening
;; sockets.
;;

(defspec.xt mock-endpoint-send
  [:fn [xt.isolate.spec/MockEndpointRecord
        [:or :xt/str xt.isolate.spec/RequestFrame]]
   :xt/any])

(defspec.xt mock-endpoint
  [:fn [[:xt/maybe [:fn [:xt/any] :xt/any]]]
   xt.isolate.spec/MockEndpointRecord])

(defspec.xt create-endpoint
  [:fn [[:xt/maybe [:fn [:xt/any] :xt/any]]
        [:xt/maybe xt.isolate.spec/RouteMap]
        [:xt/maybe :xt/bool]]
   xt.isolate.spec/MockEndpointRecord])

(defspec.xt make-transport
  [:fn [xt.isolate.spec/MockEndpointRecord]
   xt.isolate.spec/TransportMap])

;;
;; mock-endpoint-send
;;
;; Drives the endpoint's frame processor directly - no IPC needed.
;;

(defn.xt mock-endpoint-send
  "sends a frame directly to the mock endpoint's processor"
  {:added "4.0"}
  [mock message]
  (try
    (cond (xt/x:is-string? message)
          (endpoint/endpoint-process mock
                                     {:op   "eval"
                                      :id   nil
                                      :body message})
          :else
          (endpoint/endpoint-process mock message))
    (catch e (trace/TRACE! (. e ["stack"]) "SEND.ERROR"))))

;;
;; mock-endpoint
;;
;; Builds a minimal isolate-shaped object with:
;;   ::         - type tag
;;   listeners  - list of outbound handlers (registered by the client)
;;   emit       - function that broadcasts to all listeners
;;

(defn.xt mock-endpoint
  "creates a bare mock endpoint that forwards events to registered listeners"
  {:added "4.0"}
  [listener]
  (var listeners (:? (xt/x:not-nil? listener) [listener] []))
  (var isolate {"::" "isolate.mock"
                :listeners listeners})
  (var emit (fn [event]
              (xt/for:array [l (. isolate ["listeners"])]
                (when (xt/x:not-nil? l)
                  (l event)))))
  (xt/x:set-key isolate "emit" emit)
  (return isolate))

;;
;; create-endpoint
;;
;; Convenience constructor that:
;;   1. Creates a mock-endpoint
;;   2. Installs baseline routes (+ any extra routes)
;;   3. Optionally suppresses the EV_INIT broadcast
;;

(defn.xt create-endpoint
  "creates and initialises a fully configured mock endpoint"
  {:added "4.0"}
  [listener routes suppress]
  (var isolate (-/mock-endpoint listener))
  (endpoint/routes-init (or routes {}) isolate)
  (when (not suppress)
    (spec-promise/x:with-delay
     0
     (fn []
       (return (endpoint/endpoint-init-signal isolate {:done true})))))
  (return isolate))

;;
;; make-transport
;;
;; Returns a transport capability map whose send drives the mock endpoint.
;; Use this when you want a ClientRecord backed by a mock endpoint.
;;

(defn.xt make-transport
  "builds a transport capability map backed by the given mock endpoint"
  {:added "4.0"}
  [mock]
  (var send   (fn [frame]
                (-/mock-endpoint-send mock frame)))
  (var listen (fn [handler]
                (. (. mock ["listeners"]) (push handler))))
  (return {:send   send
           :listen listen}))
