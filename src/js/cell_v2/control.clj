(ns js.cell-v2.control
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.route :as route]]})

(def$.js ACT_EVAL "@/eval")
(def$.js ACT_TRIGGER "@/trigger")
(def$.js ACT_TRIGGER_ASYNC "@/trigger-async")
(def$.js ACT_FINAL_SET "@/final-set")
(def$.js ACT_FINAL_STATUS "@/final-status")
(def$.js ACT_EVAL_ENABLE "@/eval-enable")
(def$.js ACT_EVAL_DISABLE "@/eval-disable")
(def$.js ACT_EVAL_STATUS "@/eval-status")
(def$.js ACT_ROUTE_LIST "@/route-list")
(def$.js ACT_ROUTE_ENTRY "@/route-entry")
(def$.js ACT_PING "@/ping")
(def$.js ACT_PING_ASYNC "@/ping-async")
(def$.js ACT_ECHO "@/echo")
(def$.js ACT_ECHO_ASYNC "@/echo-async")
(def$.js ACT_ERROR "@/error")
(def$.js ACT_ERROR_ASYNC "@/error-async")

(defn.js control-state
  "gets or creates the control state"
  {:added "4.0"}
  [system]
  (var root (. system ["state"]))
  (var state (. root ["control"]))
  (when (k/nil? state)
    (:= state {:eval true})
    (k/set-key root "control" state))
  (when (k/nil? (k/get-key state "eval"))
    (k/set-key state "eval" true))
  (return state))

(defn.js route-info
  "returns the public metadata for a route entry"
  {:added "4.0"}
  [entry]
  (when (k/nil? entry)
    (return nil))
  (var args (k/get-key entry "args"))
  (when (k/nil? args)
    (:= args []))
  (return {:args args
           :async (== true (k/get-key entry "async"))}))

(defn.js control-route-options
  "constructs standard control-route options"
  {:added "4.0"}
  [args is-async hidden]
  (var out {:args (or args [])
            :async (== true is-async)
            :kind "control"})
  (when hidden
    (k/set-key out "hidden" true))
  (return out))

(defn.js register-control-entry
  "registers a standard control route"
  {:added "4.0"}
  [system action handler args is-async hidden]
  (return (route/register-route (. system ["routes"])
                                action
                                handler
                                (-/control-route-options args is-async hidden))))

(defn.js transport-next-id
  "returns the next id for a transport-like context"
  {:added "4.0"}
  [tx prefix]
  (var n (+ 1 (or (. tx ["counter"])
                  0)))
  (:= (. tx ["counter"]) n)
  (return (k/cat (or prefix
                     (. tx ["id"])
                     "transport")
                 "-"
                 n)))

(defn.js transport-subscribed?
  "checks if a transport is already forwarding a signal"
  {:added "4.0"}
  [tx signal]
  (return (or (== true (. tx ["forwardAll"]))
              (. tx ["subscriptions"] ["*"])
              (. tx ["subscriptions"] [signal]))))

(defn.js transport-send
  "sends a frame through a transport-like context"
  {:added "4.0"}
  [tx frame]
  (var send-fn (or (. tx ["send"])
                   (k/get-key (. tx ["channel"]) "send")))
  (cond send-fn
        (return (send-fn frame tx))

        (k/get-key (. tx ["channel"]) "postMessage")
        (return (j/postMessage (. tx ["channel"]) frame))

        :else
        (k/err "ERR - Transport cannot send frame")))

(defn.js publish-signal
  "emits a signal on the system and mirrors it to the inbound transport when needed"
  {:added "4.0"}
  [system ctx signal status body meta]
  (var out (event/signal-event signal status body meta))
  (event/emit (. system ["events"]) out)
  (var tx (. ctx ["transport"]))
  (when (and tx
             (not (-/transport-subscribed? tx signal)))
    (-/transport-send tx
                      (protocol/emit (-/transport-next-id tx "emit")
                                     signal
                                     status
                                     body
                                     (or meta {})
                                     nil)))
  (return out))

(defn.js set-control-state
  "applies a control-state update"
  {:added "4.0"}
  [system ctx suppress update-fn]
  (var state (-/control-state system))
  (cond (k/get-key state "final")
        (return {:status "error"
                 :body "Worker State is Final."})

        :else
        (do (update-fn state)
            (when (not suppress)
              (-/publish-signal system
                                ctx
                                event/EV_STATE
                                "ok"
                                state
                                {}))
            (return state))))

(defn.js trigger-route
  "emits a trigger event"
  {:added "4.0"}
  [system ctx op signal status body]
  (-/publish-signal system ctx signal status body {})
  (return {:op op
           :topic signal
           :status status
           :body body}))

(defn.js trigger-route-async
  "emits a trigger event after a delay"
  {:added "4.0"}
  [system ctx op signal status body ms]
  (return (j/future-delayed [ms]
            (return (-/trigger-route system ctx op signal status body)))))

(defn.js eval-route
  "evaluates code when debug eval is enabled"
  {:added "4.0"}
  [system code]
  (var state (-/control-state system))
  (when (== false (k/get-key state "eval"))
    (return {:status "error"
             :body "Not enabled - EVAL"}))
  (try
    (return (k/eval code))
    (catch err
      (return {:status "error"
               :body err}))))

(defn.js route-list-route
  "lists public control and application routes"
  {:added "4.0"}
  [system]
  (var out [])
  (k/for:array [route-id (route/list-routes (. system ["routes"]))]
    (var entry (route/get-route (. system ["routes"])
                                route-id))
    (when (not (k/get-key entry "hidden"))
      (x:arr-push out route-id)))
  (return out))

(defn.js route-entry-route
  "gets public metadata for a route"
  {:added "4.0"}
  [system route-id]
  (return (-/route-info (route/get-route (. system ["routes"])
                                         route-id))))

(defn.js register-control-routes
  "registers the standard worker control routes on a system"
  {:added "4.0"}
  [system]
  (-/control-state system)
  (-/register-control-entry system
                            -/ACT_EVAL
                            (fn [ctx code]
                              (return (-/eval-route system code)))
                            ["body"]
                            false
                            true)
  (-/register-control-entry system
                            -/ACT_TRIGGER
                            (fn [ctx op signal status body]
                              (return (-/trigger-route system ctx op signal status body)))
                            ["op" "topic" "status" "body"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_TRIGGER_ASYNC
                            (fn [ctx op signal status body ms]
                              (return (-/trigger-route-async system ctx op signal status body ms)))
                            ["op" "topic" "status" "body" "ms"]
                            true
                            false)
  (-/register-control-entry system
                            -/ACT_FINAL_SET
                            (fn [ctx suppress]
                              (return (-/set-control-state system
                                                           ctx
                                                           suppress
                                                           (fn [state]
                                                             (k/set-key state "final" true)))))
                            ["suppress"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_FINAL_STATUS
                            (fn [ctx]
                              (return (k/get-key (-/control-state system) "final")))
                            []
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_EVAL_ENABLE
                            (fn [ctx suppress]
                              (return (-/set-control-state system
                                                           ctx
                                                           suppress
                                                           (fn [state]
                                                             (k/set-key state "eval" true)))))
                            ["suppress"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_EVAL_DISABLE
                            (fn [ctx suppress]
                              (return (-/set-control-state system
                                                           ctx
                                                           suppress
                                                           (fn [state]
                                                             (k/set-key state "eval" false)))))
                            ["suppress"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_EVAL_STATUS
                            (fn [ctx]
                              (return (k/get-key (-/control-state system) "eval")))
                            []
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_ROUTE_LIST
                            (fn [ctx]
                              (return (-/route-list-route system)))
                            []
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_ROUTE_ENTRY
                            (fn [ctx route-id]
                              (return (-/route-entry-route system route-id)))
                            ["name"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_PING
                            (fn [ctx]
                              (return ["pong" (k/now-ms)]))
                            []
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_PING_ASYNC
                            (fn [ctx ms]
                              (return (j/future-delayed [ms]
                                        (return ["pong" (k/now-ms)]))))
                            ["ms"]
                            true
                            false)
  (-/register-control-entry system
                            -/ACT_ECHO
                            (fn [ctx arg]
                              (return [arg (k/now-ms)]))
                            ["arg"]
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_ECHO_ASYNC
                            (fn [ctx arg ms]
                              (return (j/future-delayed [ms]
                                        (return [arg (k/now-ms)]))))
                            ["arg" "ms"]
                            true
                            false)
  (-/register-control-entry system
                            -/ACT_ERROR
                            (fn [ctx]
                              (return {:status "error"
                                       :body ["error" (k/now-ms)]}))
                            []
                            false
                            false)
  (-/register-control-entry system
                            -/ACT_ERROR_ASYNC
                            (fn [ctx ms]
                              (return (j/future-delayed [ms]
                                        (return {:status "error"
                                                 :body ["error" (k/now-ms)]}))))
                            ["ms"]
                            true
                            false)
  (return system))
