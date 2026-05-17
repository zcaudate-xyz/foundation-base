(ns xt.substrate.base-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-sync :as sync]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-sync :as sync]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-sync :as sync]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-sync/handle-open :added "4.1"}
(fact "open requests capture the caller transport and subscribe sync signals"

  (notify/wait-on :js
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (-> (event-node/attach-transport
         n
         "peer-a"
         {"send_fn" (fn [out]
                      (return out))})
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/receive-frame
             n
             (frame/request-frame
              "room/a"
              sync/ACTION_OPEN
              [{"subscribe" true
                "subscription_id" "sub-a"}]
              nil)
             {"transport_id" "peer-a"}))))
        (promise/x:promise-then
         (fn [response]
           (return
            (repl/notify
             {"response" (. response ["data"])
              "delta"    (router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)
              "reset"    (router/list-subscriptions n "room/a" sync/SIGNAL_RESET)
              "error"    (router/list-subscriptions n "room/a" sync/SIGNAL_ERROR)}))))))
  => {"response" {"protocol" "node.sync"
                  "mode" "open"
                  "space" "room/a"
                  "cursor" 0
                  "snapshot" {"count" 2}
                  "subscribed" true
                  "subscription_id" "sub-a"}
      "delta" ["peer-a"]
      "reset" ["peer-a"]
      "error" ["peer-a"]}

  (notify/wait-on :lua
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (-> (event-node/attach-transport
         n
         "peer-a"
         {"send_fn" (fn [out]
                      (return out))})
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/receive-frame
             n
             (frame/request-frame
              "room/a"
              sync/ACTION_OPEN
              [{"subscribe" true
                "subscription_id" "sub-a"}]
              nil)
             {"transport_id" "peer-a"}))))
        (promise/x:promise-then
         (fn [response]
           (return
            (repl/notify
             {"response" (. response ["data"])
              "delta"    (router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)
              "reset"    (router/list-subscriptions n "room/a" sync/SIGNAL_RESET)
              "error"    (router/list-subscriptions n "room/a" sync/SIGNAL_ERROR)}))))))
  => {"response" {"protocol" "node.sync"
                  "mode" "open"
                  "space" "room/a"
                  "cursor" 0
                  "snapshot" {"count" 2}
                  "subscribed" true
                  "subscription_id" "sub-a"}
      "delta" ["peer-a"]
      "reset" ["peer-a"]
      "error" ["peer-a"]}

  (notify/wait-on :python
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (-> (event-node/attach-transport
         n
         "peer-a"
         {"send_fn" (fn [out]
                      (return out))})
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/receive-frame
             n
             (frame/request-frame
              "room/a"
              sync/ACTION_OPEN
              [{"subscribe" true
                "subscription_id" "sub-a"}]
              nil)
             {"transport_id" "peer-a"}))))
        (promise/x:promise-then
         (fn [response]
           (return
            (repl/notify
             {"response" (. response ["data"])
              "delta"    (router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)
              "reset"    (router/list-subscriptions n "room/a" sync/SIGNAL_RESET)
              "error"    (router/list-subscriptions n "room/a" sync/SIGNAL_ERROR)}))))))
  => {"response" {"protocol" "node.sync"
                  "mode" "open"
                  "space" "room/a"
                  "cursor" 0
                  "snapshot" {"count" 2}
                  "subscribed" true
                  "subscription_id" "sub-a"}
      "delta" ["peer-a"]
      "reset" ["peer-a"]
      "error" ["peer-a"]})

^{:refer xt.substrate.base-sync/handle-resume :added "4.1"}
(fact "resume requests return either reset snapshots or live resume acknowledgements"

  (notify/wait-on :js
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 4}}}}))
    (sync/install n nil)
    (-> (sync/publish-delta n "room/a" {"count" 4} nil)
        (promise/x:promise-then
         (fn [_]
           (return
            (promise/x:promise-all
            [(sync/resume n "room/a" {"cursor" 0} {"id" "resume-stale"})
             (sync/resume n "room/a" {"cursor" 1} {"id" "resume-live"})]))))
        (promise/x:promise-then
         (repl/>notify))))
  => [{"protocol" "node.sync"
       "mode" "reset"
       "space" "room/a"
       "cursor" 1
       "snapshot" {"count" 4}
       "subscribed" false
       "subscription_id" "resume-stale"}
      {"protocol" "node.sync"
       "mode" "resume"
       "space" "room/a"
       "cursor" 1
       "subscribed" false
       "subscription_id" "resume-live"}]

  (notify/wait-on :lua
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 4}}}}))
    (sync/install n nil)
    (-> (sync/publish-delta n "room/a" {"count" 4} nil)
        (promise/x:promise-then
         (fn [_]
           (return
            (promise/x:promise-all
            [(sync/resume n "room/a" {"cursor" 0} {"id" "resume-stale"})
             (sync/resume n "room/a" {"cursor" 1} {"id" "resume-live"})]))))
        (promise/x:promise-then
         (repl/>notify))))
  => [{"protocol" "node.sync"
       "mode" "reset"
       "space" "room/a"
       "cursor" 1
       "snapshot" {"count" 4}
       "subscribed" false
       "subscription_id" "resume-stale"}
      {"protocol" "node.sync"
       "mode" "resume"
       "space" "room/a"
       "cursor" 1
       "subscribed" false
       "subscription_id" "resume-live"}]

  (notify/wait-on :python
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 4}}}}))
    (sync/install n nil)
    (-> (sync/publish-delta n "room/a" {"count" 4} nil)
        (promise/x:promise-then
         (fn [_]
           (return
            (promise/x:promise-all
            [(sync/resume n "room/a" {"cursor" 0} {"id" "resume-stale"})
             (sync/resume n "room/a" {"cursor" 1} {"id" "resume-live"})]))))
        (promise/x:promise-then
         (repl/>notify))))
  => [{"protocol" "node.sync"
       "mode" "reset"
       "space" "room/a"
       "cursor" 1
       "snapshot" {"count" 4}
       "subscribed" false
       "subscription_id" "resume-stale"}
      {"protocol" "node.sync"
       "mode" "resume"
       "space" "room/a"
       "cursor" 1
       "subscribed" false
       "subscription_id" "resume-live"}])

^{:refer xt.substrate.base-sync/apply-stream :added "4.1"}
(fact "sync triggers apply delta and reset payloads to local space state"

  (notify/wait-on :js
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers
     n
     {"apply_delta"
      (fn [state payload stream node]
        (return {"count" (+ (. state ["count"])
                            (. payload ["delta"] ["step"]))}))
      "apply_error"
      (fn [_state payload _stream _node]
        (return (. payload ["error"] ["message"])))} )
    (-> (sync/publish-delta n "room/a" {"step" 2} nil)
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-reset n "room/a" {"count" 10} nil))))
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-error n "room/a" {"message" "boom"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (repl/notify
             {"state"  (event-node/get-space-state n "room/a")
              "cursor" (sync/get-cursor n "room/a")
              "error"  (. (sync/ensure-sync-state
                           (node-space/get-space n "room/a"))
                          ["last_error"])}))))))
  => {"state" {"count" 10}
      "cursor" 2
      "error" "boom"}

  (notify/wait-on :lua
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers
     n
     {"apply_delta"
      (fn [state payload stream node]
        (return {"count" (+ (. state ["count"])
                            (. payload ["delta"] ["step"]))}))
      "apply_error"
      (fn [_state payload _stream _node]
        (return (. payload ["error"] ["message"])))} )
    (-> (sync/publish-delta n "room/a" {"step" 2} nil)
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-reset n "room/a" {"count" 10} nil))))
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-error n "room/a" {"message" "boom"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (repl/notify
             {"state"  (event-node/get-space-state n "room/a")
              "cursor" (sync/get-cursor n "room/a")
              "error"  (. (sync/ensure-sync-state
                           (node-space/get-space n "room/a"))
                          ["last_error"])}))))))
  => {"state" {"count" 10}
      "cursor" 2
      "error" "boom"}

  (notify/wait-on :python
    (var n (event-node/node-create
            {"spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers
     n
     {"apply_delta"
      (fn [state payload stream node]
        (return {"count" (+ (. state ["count"])
                            (. payload ["delta"] ["step"]))}))
      "apply_error"
      (fn [_state payload _stream _node]
        (return (. payload ["error"] ["message"])))} )
    (-> (sync/publish-delta n "room/a" {"step" 2} nil)
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-reset n "room/a" {"count" 10} nil))))
        (promise/x:promise-then
         (fn [_]
           (return (sync/publish-error n "room/a" {"message" "boom"} nil))))
        (promise/x:promise-then
         (fn [_]
           (return
            (repl/notify
             {"state"  (event-node/get-space-state n "room/a")
              "cursor" (sync/get-cursor n "room/a")
              "error"  (. (sync/ensure-sync-state
                           (node-space/get-space n "room/a"))
                          ["last_error"])}))))))
  => {"state" {"count" 10}
      "cursor" 2
      "error" "boom"})
