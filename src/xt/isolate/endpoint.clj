(ns xt.isolate.endpoint
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-trace :as trace]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-space :as rt :with [defsingleton.xt]]
             [xt.isolate.frame :as frame]]})

;;
;; endpoint.clj – inner isolate execution layer
;;
;; The endpoint is the "inside" of an isolate.  It:
;;   - maintains a RouteMap of named handlers
;;   - maintains an EndpointState
;;   - processes RequestFrames dispatched by the client
;;   - emits ResponseFrames back through an injected emit! function
;;
;; emit! is a plain function provided at startup time – there is no
;; dependency on postMessage, WebWorker, or any JS-specific API.
;;

;;
;; Spec declarations
;;

(defspec.xt ENDPOINT_STATE
  [:fn [] xt.isolate.spec/EndpointState])

(defspec.xt ENDPOINT_ROUTES
  [:fn [] xt.isolate.spec/RouteMap])

(defspec.xt get-state
  [:fn [:xt/any] xt.isolate.spec/EndpointState])

(defspec.xt get-routes
  [:fn [:xt/any] xt.isolate.spec/RouteMap])

(defspec.xt set-routes
  [:fn [xt.isolate.spec/RouteMap [:xt/maybe :xt/any]] :xt/any])

(defspec.xt fn-bind
  [:fn [:xt/any [:fn [:xt/any] :xt/any]] :xt/any])

(defspec.xt fn-emit-event
  [:fn [:xt/any :xt/str :xt/str :xt/any] :xt/any])

(defspec.xt fn-emit-event-async
  [:fn [:xt/any :xt/str :xt/str :xt/any :xt/int] :xt/any])

(defspec.xt fn-set-state
  [:fn [:xt/any xt.isolate.spec/EndpointState
        [:fn [xt.isolate.spec/EndpointState] :xt/any]
        [:xt/maybe :xt/bool]]
   xt.isolate.spec/EndpointState])

(defspec.xt fn-set-final
  [:fn [:xt/any [:xt/maybe :xt/bool]]
   xt.isolate.spec/EndpointState])

(defspec.xt fn-get-final
  [:fn [:xt/any] [:xt/maybe :xt/bool]])

(defspec.xt fn-set-eval
  [:fn [:xt/any :xt/bool [:xt/maybe :xt/bool]]
   xt.isolate.spec/EndpointState])

(defspec.xt fn-get-eval
  [:fn [] :xt/bool])

(defspec.xt fn-get-route-list
  [:fn [] xt.isolate.spec/StringList])

(defspec.xt fn-get-route-entry
  [:fn [:xt/str] [:xt/maybe xt.isolate.spec/RouteEntry]])

(defspec.xt fn-ping
  [:fn [] [:tuple :xt/str :xt/int]])

(defspec.xt fn-ping-async
  [:fn [:xt/int] :xt/any])

(defspec.xt fn-echo
  [:fn [:xt/any] [:tuple :xt/any :xt/int]])

(defspec.xt fn-echo-async
  [:fn [:xt/any :xt/int] :xt/any])

(defspec.xt fn-error
  [:fn [] :xt/any])

(defspec.xt fn-error-async
  [:fn [:xt/int] :xt/any])

(defspec.xt routes-baseline
  [:fn [[:xt/maybe :xt/any]] xt.isolate.spec/RouteMap])

(defspec.xt routes-init
  [:fn [xt.isolate.spec/RouteMap :xt/any] :xt/any])

(defspec.xt endpoint-handle-async
  [:fn [:xt/any [:fn [xt.isolate.spec/AnyList] :xt/any]
        :xt/str
        [:xt/maybe :xt/str]
        xt.isolate.spec/AnyList]
   :xt/any])

(defspec.xt endpoint-process-call
  [:fn [:xt/any
        xt.isolate.spec/RequestFrame
        [:fn [xt.isolate.spec/ResponseFrame] :xt/any]]
   :xt/any])

(defspec.xt endpoint-process
  [:fn [:xt/any xt.isolate.spec/RequestFrame]
   :xt/any])

(defspec.xt endpoint-init
  [:fn [:xt/any [:xt/maybe [:fn [xt.isolate.spec/RequestFrame]
                            xt.isolate.spec/RequestFrame]]]
   :xt/bool])

(defspec.xt endpoint-init-signal
  [:fn [:xt/any :xt/any]
   :xt/any])

;;
;; Singletons – namespace-scoped state for the isolate
;;

(defsingleton.xt ^{:ns "@isolate"}
  ENDPOINT_STATE
  "gets the isolate endpoint state"
  {:added "4.0"}
  []
  (return {:eval true}))

(defsingleton.xt ^{:ns "@isolate"}
  ENDPOINT_ROUTES
  "gets the isolate route table"
  {:added "4.0"}
  []
  (return {}))

;;
;; State accessors
;;

(defn.xt get-state
  "returns the current endpoint state"
  {:added "4.0"}
  [isolate]
  (return (-/ENDPOINT_STATE)))

(defn.xt get-routes
  "returns the current route table; prefers an isolate-local copy"
  {:added "4.0"}
  [isolate]
  (return (or (and isolate (. isolate routes))
              (-/ENDPOINT_ROUTES))))

(defn.xt set-routes
  "installs routes into the route table"
  {:added "4.0"}
  [routes isolate]
  (cond isolate
        (do (xt/x:set-key isolate "routes" routes)
            (return isolate))
        :else
        (return (-/ENDPOINT_ROUTES-reset routes))))

;;
;; Utility helpers
;;

(defn.xt fn-bind
  "binds an isolate instance as the first argument of a handler"
  {:added "4.0"}
  [isolate f]
  (return (fn [...args]
            (return (f isolate ...args)))))

;;
;; State mutation helpers (callable as routes)
;;

(defn.xt fn-set-state
  "helper to mutate state and optionally broadcast an event"
  {:added "4.0"}
  [isolate state set-fn suppress]
  (cond (xt/x:get-key state "final")
        (throw "Endpoint State is Final.")
        :else
        (do (set-fn state)
            (when (not suppress)
              (var emit (. isolate ["emit"]))
              (when emit
                (emit (frame/resp-event frame/EV_STATE state))))
            (return state))))

(defn.xt ^{:isolate/route "@isolate/emit-event"
           :isolate/static false}
  fn-emit-event
  "emits an event from the isolate to all subscribers"
  {:added "4.0"}
  [isolate op topic body]
  (var emit (. isolate ["emit"]))
  (return (emit {:op     op
                 :topic  topic
                 :status "ok"
                 :body   body})))

(defn.xt ^{:isolate/route "@isolate/emit-event-async"
           :isolate/static false
           :isolate/is-async true}
  fn-emit-event-async
  "emits an event after a delay"
  {:added "4.0"}
  [isolate op topic body ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-emit-event isolate op topic body))))))

(defn.xt ^{:isolate/route "@isolate/set-final"
           :isolate/static false}
  fn-set-final
  "marks the isolate state as final (no more mutations)"
  {:added "4.0"}
  [isolate suppress]
  (return (-/fn-set-state isolate
                          (-/ENDPOINT_STATE)
                          (fn [state]
                            (xt/x:set-key state "final" true))
                          suppress)))

(defn.xt ^{:isolate/route "@isolate/get-final"
           :isolate/static false}
  fn-get-final
  "returns the final flag of the endpoint state"
  {:added "4.0"}
  [isolate]
  (return (. (-/ENDPOINT_STATE) ["final"])))

(defn.xt ^{:isolate/route "@isolate/set-eval"
           :isolate/static false}
  fn-set-eval
  "enables or disables eval in the isolate"
  {:added "4.0"}
  [isolate status suppress]
  (return (-/fn-set-state isolate
                          (-/ENDPOINT_STATE)
                          (fn [state]
                            (xt/x:set-key state "eval" status))
                          suppress)))

(defn.xt ^{:isolate/route "@isolate/get-eval"
           :isolate/static true}
  fn-get-eval
  "returns the eval-enabled flag"
  {:added "4.0"}
  []
  (return (. (-/ENDPOINT_STATE) ["eval"])))

(defn.xt ^{:isolate/route "@isolate/get-route-list"
           :isolate/static true}
  fn-get-route-list
  "lists available route names"
  {:added "4.0"}
  []
  (return (xt/x:obj-keys (-/ENDPOINT_ROUTES))))

(defn.xt ^{:isolate/route "@isolate/get-route-entry"
           :isolate/static true}
  fn-get-route-entry
  "returns a single route entry"
  {:added "4.0"}
  [name]
  (return (. (-/ENDPOINT_ROUTES)
             [name])))

(defn.xt ^{:isolate/route "@isolate/ping"
           :isolate/static true}
  fn-ping
  "pings the isolate"
  {:added "4.0"}
  []
  (return ["pong" (xt/x:now-ms)]))

(defn.xt ^{:isolate/route "@isolate/ping.async"
           :isolate/static true
           :isolate/is-async true}
  fn-ping-async
  "pings the isolate after a delay"
  {:added "4.0"}
  [ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-ping))))))

(defn.xt ^{:isolate/route "@isolate/echo"
           :isolate/static true}
  fn-echo
  "echoes the first argument"
  {:added "4.0"}
  [arg]
  (return [arg (xt/x:now-ms)]))

(defn.xt ^{:isolate/route "@isolate/echo.async"
           :isolate/static true
           :isolate/is-async true}
  fn-echo-async
  "echoes the first argument after a delay"
  {:added "4.0"}
  [arg ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-echo arg))))))

(defn.xt ^{:isolate/route "@isolate/error"
           :isolate/static true}
  fn-error
  "throws an error"
  {:added "4.0"}
  []
  (throw ["error" (xt/x:now-ms)]))

(defn.xt ^{:isolate/route "@isolate/error.async"
           :isolate/static true
           :isolate/is-async true}
  fn-error-async
  "throws an error after a delay"
  {:added "4.0"}
  [ms]
  (return (spec-promise/x:with-delay
           ms
           (fn []
             (return (-/fn-error))))))

;;
;; routes-baseline
;;
;; Returns the default route table built from the annotated functions
;; in this namespace.  Optionally binds stateful handlers to an isolate.
;;

(defn.xt routes-baseline
  "returns the baseline route table for an isolate"
  {:added "4.0"}
  [isolate]
  (var bind-handler (fn [f]
                      (return (:? isolate
                                  (-/fn-bind isolate f)
                                  f))))
  ;; (@! (cons 'tab +baselines+))
  (return
   (tab
     ["@isolate/emit-event"
      {:handler   (bind-handler xt.isolate.endpoint/fn-emit-event)
       :is-async  false
       :args      ["op" "topic" "body"]}]
     ["@isolate/emit-event-async"
      {:handler   (bind-handler xt.isolate.endpoint/fn-emit-event-async)
       :is-async  true
       :args      ["op" "topic" "body" "ms"]}]
     ["@isolate/set-final"
      {:handler   (bind-handler xt.isolate.endpoint/fn-set-final)
       :is-async  false
       :args      ["suppress"]}]
     ["@isolate/get-final"
      {:handler   (bind-handler xt.isolate.endpoint/fn-get-final)
       :is-async  false
       :args      []}]
     ["@isolate/set-eval"
      {:handler   (bind-handler xt.isolate.endpoint/fn-set-eval)
       :is-async  false
       :args      ["status" "suppress"]}]
     ["@isolate/get-eval"
      {:handler   xt.isolate.endpoint/fn-get-eval
       :is-async  false
       :args      []}]
     ["@isolate/get-route-list"
      {:handler   xt.isolate.endpoint/fn-get-route-list
       :is-async  false
       :args      []}]
     ["@isolate/get-route-entry"
      {:handler   xt.isolate.endpoint/fn-get-route-entry
       :is-async  false
       :args      ["name"]}]
     ["@isolate/ping"
      {:handler   xt.isolate.endpoint/fn-ping
       :is-async  false
       :args      []}]
     ["@isolate/ping.async"
      {:handler   xt.isolate.endpoint/fn-ping-async
       :is-async  true
       :args      ["ms"]}]
     ["@isolate/echo"
      {:handler   xt.isolate.endpoint/fn-echo
       :is-async  false
       :args      ["arg"]}]
     ["@isolate/echo.async"
      {:handler   xt.isolate.endpoint/fn-echo-async
       :is-async  true
       :args      ["arg" "ms"]}]
     ["@isolate/error"
      {:handler   xt.isolate.endpoint/fn-error
       :is-async  false
       :args      []}]
     ["@isolate/error.async"
      {:handler   xt.isolate.endpoint/fn-error-async
       :is-async  true
       :args      ["ms"]}])))

(defn.xt routes-init
  "installs baseline routes merged with extra routes into an isolate"
  {:added "4.0"}
  [routes isolate]
  (return
   (-/set-routes (xt/x:obj-assign (-/routes-baseline isolate)
                                  (or routes {}))
                 isolate)))

;;
;; endpoint-handle-async
;;
;; Wraps an async route handler – the promise resolves and
;; pipes the result (or error) back to the caller via emit!.
;;

(defn.xt endpoint-handle-async
  "handles an async route call, piping the result back through emit!"
  {:added "4.0"}
  [isolate f op id body]
  (var emit (. isolate ["emit"]))
  (return (. (f (:.. body))
             (then  (fn [ret]
                      (emit (frame/resp-ok op id ret))))
             (catch (fn [err]
                      (when (. err ["stack"])
                        (trace/TRACE! (. err ["stack"]) "ERR"))
                      (emit (frame/resp-error op id err)))))))

;;
;; endpoint-process-call
;;
;; Handles "call" and "notify" frames by looking up the route and
;; invoking the handler.
;;

(defn.xt endpoint-process-call
  "processes a call/notify frame by dispatching to the route handler"
  {:added "4.0"}
  [isolate input emit-fn]
  (var #{op id body route} input)
  (var route-entry (. (-/get-routes isolate)
                      [route]))
  (when (== nil route-entry)
    (return (emit-fn (frame/resp-error op id (xt/x:cat "route not found - " route)))))
  (var route-async (. route-entry ["is_async"]))
  (var route-fn    (. route-entry ["handler"]))
  (var f (:? route-async
              lib/identity
              emit-fn))
  (try
    (:= body (or body []))
    (when (xt/x:is-array? body)
      (:= body (xt/x:unpack body)))
    (var out (:? route-async
                 (-/endpoint-handle-async isolate route-fn op id body)
                 (route-fn (:.. body))))
    (return (f (frame/resp-ok op id out)))
    (catch err
        (return (f (frame/resp-error op id err))))))

;;
;; endpoint-process
;;
;; Top-level frame dispatcher for the endpoint.
;;

(defn.xt endpoint-process
  "processes an incoming request frame dispatching by op"
  {:added "4.0"}
  [isolate input]
  (var #{op} input)
  (var emit (. isolate ["emit"]))
  (var emit-fn (fn [x] (return (emit x))))
  (cond (== op "call")
        (return (-/endpoint-process-call isolate input emit-fn))

        (== op "notify")
        (return (-/endpoint-process-call isolate input lib/identity))

        :else
        (emit-fn (frame/resp-error op nil input))))

;;
;; endpoint-init
;;
;; Registers the transport listener so that incoming frames are
;; processed by endpoint-process.
;;

(defn.xt endpoint-init
  "initialises the endpoint by registering its frame listener"
  {:added "4.0"}
  [isolate input-fn]
  (:= input-fn (or input-fn lib/identity))
  (var listen (. isolate ["listen"]))
  (listen (fn [data]
            (cond (xt/x:is-string? data)
                  (-/endpoint-process isolate
                                      (input-fn {:op   "eval"
                                                 :id   nil
                                                 :body data}))
                  :else
                  (-/endpoint-process isolate (input-fn data)))))
  (return true))

;;
;; endpoint-init-signal
;;
;; Broadcasts the EV_INIT event so the client knows the isolate is ready.
;;

(defn.xt endpoint-init-signal
  "emits the init event to signal that the endpoint is ready"
  {:added "4.0"}
  [isolate body]
  (var emit (. isolate ["emit"]))
  (return (emit (frame/resp-event frame/EV_INIT body))))
