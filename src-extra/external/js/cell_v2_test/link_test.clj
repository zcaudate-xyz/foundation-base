(ns js.cell-v2.link-test
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
             [js.cell-v2.event :as event]
             [js.cell-v2.link :as link]
             [js.cell-v2.transport-legacy :as legacy-transport]
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

(defn.js make-legacy-control-link
  "creates a legacy control link and backing system"
  {:added "4.0"}
  []
  (var pair (-/make-endpoint-pair))
  (var client-endpoint (. pair [0]))
  (var server-endpoint (. pair [1]))
  (var system (cell-v2/make-system {}))
  (control/register-control-routes system)
  (legacy-transport/make-legacy-worker-transport server-endpoint
                                                 {:id "server"
                                                  :system system
                                                  :forwardAll false})
  (return {:system system
           :link (link/make-legacy-worker-link client-endpoint
                                               {:id "legacy-client"})}))

^{:refer js.cell-v2.link/make-worker-link :added "4.0" :unchecked true}
(fact "creates a protocol worker link with active call tracking"
  (j/<! (do:>
         (var setup (-/make-control-link))
         (var lk (. setup ["link"]))
         (var pending (link/call-action lk "@/ping-async" [5] nil))
         (var active-before (k/len (k/obj-keys (link/link-active lk))))
         (. pending
            (then (fn [out]
                    (return {"active_before" active-before
                             "active_after" (k/len (k/obj-keys (link/link-active lk)))
                             "callbacks" (link/list-callbacks lk)
                             "out" out}))))))
  => {"active_before" 1
      "active_after" 0
      "callbacks" []
      "out" ["pong" integer?]})

^{:refer js.cell-v2.link/add-callback :added "4.0" :unchecked true}
(fact "subscribes protocol link callbacks to remote signals"
  (notify/wait-on :js
    (var setup (-/make-control-link))
    (var lk (. setup ["link"]))
    (var system (. setup ["system"]))
    (link/add-callback lk
                       "remote"
                       event/EV_REMOTE
                       (fn [input signal link-ref]
                         (repl/notify (k/get-key input "body"))))
    (cell-v2/emit-signal system
                         event/EV_REMOTE
                         {"request_id" "r1"}
                         nil
                         "ok"))
  => {"request_id" "r1"})

^{:refer js.cell-v2.link/make-worker-link :added "4.0" :unchecked true}
(fact "supports create-fn worker factories for protocol links"
  (j/<! (do:>
         (var lk
              (link/make-worker-link
               {:create-fn
                (fn [listener]
                  (var worker {})
                  (k/set-key worker
                             "postRequest"
                             (fn [frame]
                               (listener {:op "result"
                                          :id (. frame ["id"])
                                          :status "ok"
                                          :body {"echo" (. frame ["body"] ["input"])}
                                          :meta {}
                                          :ref nil})
                               (return frame)))
                  (return worker))}
                {:id "factory"}))
          (return (link/call-action lk "local/echo" ["hello"] nil))))
  => {"echo" ["hello"]})

^{:refer js.cell-v2.link/make-legacy-worker-link :added "4.0" :unchecked true}
(fact "preserves legacy eval call compatibility"
  (notify/wait-on :js
    (var setup (-/make-legacy-control-link))
    (var lk (. setup ["link"]))
    (. (link/call lk {:op "eval"
                      :body "1 + 1"})
       (then (fn [eval-out]
               (repl/notify eval-out)))))
  => 2)

^{:refer js.cell-v2.link/make-legacy-worker-link :added "4.0" :unchecked true}
(fact "preserves legacy route call compatibility"
  (notify/wait-on :js
    (var setup (-/make-legacy-control-link))
    (var lk (. setup ["link"]))
    (. (link/call lk {:op "route"
                      :route "@/ping"
                      :body []})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))
