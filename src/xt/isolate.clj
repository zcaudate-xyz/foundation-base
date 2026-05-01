(ns xt.isolate
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-space :as rt :with [defsingleton.xt]]
             [xt.isolate.frame :as frame]
             [xt.isolate.client :as client]
             [xt.isolate.endpoint :as endpoint]
             [xt.isolate.mock :as mock]]})

;;
;; Public top-level singletons
;;

(defsingleton.xt GD
  "default isolate client"
  {:added "4.0"}
  []
  (return nil))

(defsingleton.xt GX
  "named isolate client registry"
  {:added "4.0"}
  []
  (return {}))

;;
;; Registry helpers
;;

(defn.xt GX-val
  "gets a named client from the registry"
  {:added "4.0"}
  [key]
  (return (xt/x:get-key (-/GX) key)))

(defn.xt GX-set
  "stores a named client in the registry"
  {:added "4.0"}
  [key val]
  (xt/x:set-key (-/GX) key val)
  (return val))

;;
;; Frame construction (re-exported for convenience)
;;

(def.xt EV_INIT    frame/EV_INIT)
(def.xt EV_STATE   frame/EV_STATE)
(def.xt rand-id    frame/rand-id)
(def.xt req-call   frame/req-call)
(def.xt req-notify frame/req-notify)
(def.xt req-frame  frame/req-frame)
(def.xt resp-ok    frame/resp-ok)
(def.xt resp-error frame/resp-error)
(def.xt resp-event frame/resp-event)

;;
;; Client API (re-exported for convenience)
;;

(def.xt client-create       client/client-create)
(def.xt client-call         client/client-call)
(def.xt client-notify       client/client-notify)
(def.xt client-subscribe    client/client-subscribe)
(def.xt client-subscriptions client/client-subscriptions)
(def.xt client-unsubscribe  client/client-unsubscribe)
(def.xt client-active       client/client-active)

;;
;; Endpoint API (re-exported for convenience)
;;

(def.xt routes-baseline    endpoint/routes-baseline)
(def.xt routes-init        endpoint/routes-init)
(def.xt endpoint-process   endpoint/endpoint-process)
(def.xt endpoint-init      endpoint/endpoint-init)
(def.xt endpoint-init-signal endpoint/endpoint-init-signal)
(def.xt get-state          endpoint/get-state)
(def.xt get-routes         endpoint/get-routes)

;;
;; Mock transport (re-exported for convenience)
;;

(def.xt mock-endpoint      mock/mock-endpoint)
(def.xt create-endpoint    mock/create-endpoint)
(def.xt mock-endpoint-send mock/mock-endpoint-send)
(def.xt make-transport     mock/make-transport)

;;
;; make-client
;;
;; High-level constructor that creates a mock-backed client.
;; Useful in tests and for in-process evaluation.
;;

(defn.xt make-client
  "creates a client backed by a mock endpoint"
  {:added "4.0"}
  [routes]
  (var isolate (mock/create-endpoint nil routes true))
  (var transport (mock/make-transport isolate))
  (var cl (client/client-create transport))
  (endpoint/endpoint-init isolate nil)
  (return cl))
