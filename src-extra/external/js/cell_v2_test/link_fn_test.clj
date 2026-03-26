(ns js.cell-v2.link-fn-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-notify :as notify]
             [xt.lang.base-repl :as repl]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.control :as control]
             [js.cell-v2.link :as link]
             [js.cell-v2.link-fn :as link-fn]
             [js.cell-v2.transport-worker :as worker-transport]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

(defn.js make-endpoint
  "creates a worker-like endpoint for tests"
  {:added "4.0"}
  []
  (var endpoint {:listeners []})
  (k/set-key endpoint
             "addEventListener"
             (fn [name handler capture]
               (x:arr-push (. endpoint ["listeners"]) handler)
               (return true)))
  (k/set-key endpoint
             "removeEventListener"
             (fn [name handler capture]
               (return true)))
  (return endpoint))

(defn.js connect-endpoints
  "connects two worker-like endpoints"
  {:added "4.0"}
  [left right]
  (k/set-key left
             "postMessage"
             (fn [payload]
               (k/for:array [handler (. right ["listeners"])]
                 (handler {:data payload}))
               (return payload)))
  (k/set-key right
             "postMessage"
             (fn [payload]
               (k/for:array [handler (. left ["listeners"])]
                 (handler {:data payload}))
               (return payload)))
  (return [left right]))

(defn.js make-endpoint-pair
  "creates a connected endpoint pair"
  {:added "4.0"}
  []
  (var left (-/make-endpoint))
  (var right (-/make-endpoint))
  (-/connect-endpoints left right)
  (return [left right]))

(defn.js make-control-link
  "creates a protocol control link and backing system"
  {:added "4.0"}
  []
  (var pair (-/make-endpoint-pair))
  (var client-endpoint (. pair [0]))
  (var server-endpoint (. pair [1]))
  (var system (cell-v2/make-system {}))
  (control/register-control-routes system)
  (worker-transport/make-worker-transport server-endpoint
                                          {:id "server"
                                           :system system
                                           :forwardAll false})
  (return {:system system
           :link (link/make-worker-link client-endpoint {:id "client"})}))

^{:refer js.cell-v2.link-fn/ping :added "4.0" :unchecked true}
(fact "provides ping and action wrappers over a worker link"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/ping lk)
            (then (fn [ping-out]
                    (. (link-fn/action lk "@/ping" [])
                       (then (fn [action-out]
                               (return {"ping" ping-out
                                        "action" action-out})))))))))
  => {"ping" ["pong" integer?]
      "action" ["pong" integer?]})

^{:refer js.cell-v2.link-fn/echo :added "4.0" :unchecked true}
(fact "provides echo wrappers"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/echo lk ["hello"])
            (then (fn [echo-out]
                    (. (link-fn/echo-async lk ["hello"] 5)
                       (then (fn [echo-async-out]
                               (return {"echo" echo-out
                                        "echo_async" echo-async-out})))))))))
  => {"echo" [["hello"] integer?]
      "echo_async" [["hello"] integer?]})

^{:refer js.cell-v2.link-fn/trigger :added "4.0" :unchecked true}
(fact "provides trigger wrappers and callback delivery"
  (notify/wait-on :js
    (var setup (-/make-control-link))
    (var lk (. setup ["link"]))
    (link/add-callback lk
                       "hello"
                       "hello"
                       (fn [input signal link-ref]
                         (repl/notify {"event" [signal
                                                (k/get-key input "body")]})))
    (. (link-fn/trigger lk "stream" "hello" "ok" "world")
       (then (fn [out]
               true))))
  => {"event" ["hello" "world"]})

^{:refer js.cell-v2.link-fn/eval-status :added "4.0" :unchecked true}
(fact "provides eval-state wrappers"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/eval-status lk)
            (then (fn [eval-status-0]
                    (. (link-fn/eval-disable lk true)
                       (then (fn [eval-disable-out]
                               (. (link-fn/eval-status lk)
                                  (then (fn [eval-status-1]
                                          (. (link-fn/eval-enable lk true)
                                             (then (fn [eval-enable-out]
                                                     (. (link-fn/eval lk "1 + 1")
                                                        (then (fn [eval-out]
                                                                (return {"eval_status_0" eval-status-0
                                                                         "eval_disable" eval-disable-out
                                                                         "eval_status_1" eval-status-1
                                                                         "eval_enable" eval-enable-out
                                                                         "eval_ok" eval-out})))))))))))))))))
  => {"eval_status_0" true
      "eval_disable" {"eval" false}
      "eval_status_1" false
      "eval_enable" {"eval" true}
      "eval_ok" 2})

^{:refer js.cell-v2.link-fn/final-status :added "4.0" :unchecked true}
(fact "provides final-state wrappers"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/final-status lk)
            (then (fn [final-status-0]
                    (. (link-fn/final-set lk true)
                       (then (fn [final-set-out]
                               (. (link-fn/final-status lk)
                                  (then (fn [final-status-1]
                                          (return {"final_status_0" final-status-0
                                                   "final_set" final-set-out
                                                   "final_status_1" final-status-1})))))))))))
  => {"final_status_0" nil
      "final_set" {"eval" true
                   "final" true}
      "final_status_1" true})

^{:refer js.cell-v2.link-fn/error :added "4.0" :unchecked true}
(fact "provides error wrappers"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/error lk)
            (catch (fn [error-out]
                     (. (link-fn/error-async lk 5)
                        (catch (fn [error-async-out]
                                 (return {"error" error-out
                                          "error_async" error-async-out})))))))))
  => {"error" {"status" "error"
               "body" ["error" integer?]
               "action" "@/error"}
      "error_async" {"status" "error"
                     "body" ["error" integer?]
                     "action" "@/error-async"}})

^{:refer js.cell-v2.link-fn/route-list :added "4.0" :unchecked true}
(fact "provides route inspection wrappers"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (. (link-fn/route-list lk)
            (then (fn [route-list-out]
                    (. (link-fn/route-entry lk "@/echo")
                       (then (fn [route-entry-out]
                               (return {"route_list" route-list-out
                                        "route_entry" route-entry-out})))))))))
  => {"route_list" ["@/trigger"
                    "@/trigger-async"
                    "@/final-set"
                    "@/final-status"
                    "@/eval-enable"
                    "@/eval-disable"
                    "@/eval-status"
                    "@/route-list"
                    "@/route-entry"
                    "@/ping"
                    "@/ping-async"
                    "@/echo"
                    "@/echo-async"
                    "@/error"
                    "@/error-async"]
      "route_entry" {"args" ["arg"]
                     "async" false}})
