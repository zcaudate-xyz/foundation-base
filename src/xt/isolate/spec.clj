(ns xt.isolate.spec
  (:require [std.lang.typed.xtalk :refer [defspec.xt]]))

;;
;; Common primitive specs
;;

(defspec.xt AnyMap
  [:xt/dict :xt/str :xt/any])

(defspec.xt AnyList
  [:xt/array :xt/any])

(defspec.xt StringList
  [:xt/array :xt/str])

;;
;; Canonical Wire Frame
;;
;; RequestFrame is the cross-boundary truth for xt.isolate.
;; - op     : "call" | "notify" | "eval" – what kind of request
;; - id     : correlation id for call/response pairs
;; - route  : endpoint action route  (replaces xt.cell "action")
;; - topic  : broadcast event topic  (replaces xt.cell "signal")
;; - body   : the request payload
;; - meta   : optional metadata map
;;

(defspec.xt RequestFrame
  [:xt/record
   ["op"    :xt/str]
   ["id"    [:xt/maybe :xt/str]]
   ["route" [:xt/maybe :xt/str]]
   ["topic" [:xt/maybe :xt/str]]
   ["body"  :xt/any]
   ["meta"  [:xt/maybe AnyMap]]])

;;
;; ResponseFrame is emitted back to the caller.
;; - op     : mirrors request op
;; - id     : correlation id
;; - route  : mirrors request route
;; - topic  : event topic (used for "stream" ops)
;; - status : "ok" | "error"
;; - body   : the response payload
;;

(defspec.xt ResponseFrame
  [:xt/record
   ["op"     :xt/str]
   ["id"     [:xt/maybe :xt/str]]
   ["route"  [:xt/maybe :xt/str]]
   ["topic"  [:xt/maybe :xt/str]]
   ["status" [:xt/maybe :xt/str]]
   ["body"   :xt/any]])

;;
;; Transport capability map
;;
;; The transport is the language-agnostic boundary between the client
;; and the isolate.  It is expressed as a plain map so that any
;; language or runtime can provide an implementation.
;;
;; Required keys:
;;   :send!   (fn [frame])   – send a frame to the other side
;;   :listen! (fn [handler]) – register a handler for incoming frames
;; Optional keys:
;;   :close!  (fn [])        – tear down the transport
;;

(defspec.xt TransportMap
  [:xt/record
   ["send"   [:fn [RequestFrame] :xt/any]]
   ["listen" [:fn [[:fn [ResponseFrame] :xt/any]] :xt/any]]
   ["close"  [:xt/maybe [:fn [] :xt/any]]]])

;;
;; Active call tracking for the client
;;

(defspec.xt ActiveCallEntry
  [:xt/record
   ["resolve" [:fn [:xt/any] :xt/any]]
   ["reject"  [:fn [:xt/any] :xt/any]]
   ["input"   :xt/any]
   ["time"    :xt/int]])

(defspec.xt ActiveCallMap
  [:xt/dict :xt/str ActiveCallEntry])

;;
;; Subscription tracking for the client
;;

(defspec.xt SubscriptionEntry
  [:xt/record
   ["key"     :xt/str]
   ["pred"    :xt/any]
   ["handler" [:fn [:xt/any :xt/any] :xt/any]]])

(defspec.xt SubscriptionMap
  [:xt/dict :xt/str SubscriptionEntry])

;;
;; Client record – the outer async interface to an isolate
;;

(defspec.xt ClientRecord
  [:xt/record
   ["::"           :xt/str]
   ["id"           :xt/str]
   ["transport"    TransportMap]
   ["active"       ActiveCallMap]
   ["subscriptions" SubscriptionMap]])

;;
;; Endpoint (isolate inner) records
;;

(defspec.xt EndpointState
  [:xt/record
   ["eval"  :xt/bool]
   ["final" [:xt/maybe :xt/bool]]])

(defspec.xt RouteEntry
  [:xt/record
   ["handler"  :xt/any]
   ["is_async" :xt/bool]
   ["args"     StringList]])

(defspec.xt RouteMap
  [:xt/dict :xt/str RouteEntry])

;;
;; Mock endpoint record for testing
;;

(defspec.xt MockEndpointRecord
  [:xt/record
   ["::"      :xt/str]
   ["listeners" AnyList]
   ["emit"    [:xt/maybe [:fn [:xt/any] :xt/any]]]])
