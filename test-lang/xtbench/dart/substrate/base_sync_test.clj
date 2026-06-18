(ns xtbench.dart.substrate.base-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
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

  (notify/wait-on :dart
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

  (notify/wait-on :dart
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

  (notify/wait-on :dart
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

^{:refer xt.substrate.base-sync/node-opts :added "4.1"}
(fact "gets node.sync opts from node metadata"

  (!.dt
    [(sync/node-opts (event-node/node-create {"id" "n1"}))
     (sync/node-opts (event-node/node-create {"id" "n2"
                                             "meta" {"node.sync" {"mode" "live"}}}))])
  => [{} {"mode" "live"}])

^{:refer xt.substrate.base-sync/set-node-opts :added "4.1"}
(fact "stores node.sync opts on node metadata"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/set-node-opts n {"mode" "live"})
    (sync/node-opts n))
  => {"mode" "live"})

^{:refer xt.substrate.base-sync/merge-node-opts :added "4.1"}
(fact "merges node.sync opts into existing metadata"

  (!.dt
    (var n (event-node/node-create {"id" "n"
                                    "meta" {"node.sync" {"mode" "open"}}}))
    (sync/merge-node-opts n {"cursor" 2})
    (sync/node-opts n))
  => {"mode" "open"
      "cursor" 2})

^{:refer xt.substrate.base-sync/request-payload :added "4.1"}
(fact "gets the first request payload or {}"

  (!.dt
    [(sync/request-payload [{"id" "task-1"}])
     (sync/request-payload [])
     (sync/request-payload nil)])
  => [{"id" "task-1"} {} {}])

^{:refer xt.substrate.base-sync/ensure-promise :added "4.1"}
(fact "wraps sync values in a promise and preserves native promises"

  (notify/wait-on :dart
    (promise/x:promise-all
     [(sync/ensure-promise 42)
      (sync/ensure-promise (promise/x:promise-run 84))])
    (promise/x:promise-then
     (promise/x:promise-all
      [(sync/ensure-promise 42)
       (sync/ensure-promise (promise/x:promise-run 84))])
     (repl/>notify)))
  => [42 84])

^{:refer xt.substrate.base-sync/sync-state :added "4.1"}
(fact "gets node.sync state from space metadata"

  (!.dt
    (var space (node-space/create-space {"spaces" {}} "room/a" {"meta" {"node.sync" {"cursor" 3}}}))
    (sync/sync-state space))
  => {"cursor" 3})

^{:refer xt.substrate.base-sync/ensure-sync-state :added "4.1"}
(fact "creates default node.sync state when missing"

  (!.dt
    (var space (node-space/create-space {"spaces" {}} "room/a" nil))
    [(sync/ensure-sync-state space)
     (sync/sync-state space)])
  => [{"::" "node.sync.state" "cursor" 0 "last_error" nil}
      {"::" "node.sync.state" "cursor" 0 "last_error" nil}])

^{:refer xt.substrate.base-sync/get-cursor :added "4.1"}
(fact "gets the current sync cursor"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (var before (sync/get-cursor n "room/a"))
    (sync/set-cursor n "room/a" 5)
    [before
     (sync/get-cursor n "room/a")])
  => [0 5])

^{:refer xt.substrate.base-sync/set-cursor :added "4.1"}
(fact "sets the sync cursor"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    [(sync/set-cursor n "room/a" 4)
     (sync/get-cursor n "room/a")])
  => [4 4])

^{:refer xt.substrate.base-sync/next-cursor :added "4.1"}
(fact "increments the sync cursor"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    [(sync/next-cursor n "room/a")
     (sync/next-cursor n "room/a")
     (sync/get-cursor n "room/a")])
  => [1 2 2])

^{:refer xt.substrate.base-sync/subscribe-signals :added "4.1"}
(fact "uses configured subscribe signals or defaults"

  (!.dt
    (var a (event-node/node-create {"id" "a"}))
    (var b (event-node/node-create {"id" "b"
                                    "meta" {"node.sync" {"subscribe_signals" ["node.sync/delta"]}}}))
    [(sync/subscribe-signals a)
     (sync/subscribe-signals b)])
  => [["node.sync/delta" "node.sync/reset" "node.sync/error"]
      ["node.sync/delta"]])

^{:refer xt.substrate.base-sync/snapshot-value :added "4.1"}
(fact "uses either the custom snapshot getter or the current space state"

  (notify/wait-on :dart
    (var space {"id" "room/a" "state" {"count" 2}})
    (var node (event-node/node-create {"id" "n"
                                       "meta" {"node.sync"
                                               {"get_snapshot"
                                                (fn [space _request _node]
                                                  (return {"count" (+ (. space ["state"] ["count"]) 1)}))}}}))
    (promise/x:promise-all
     [(sync/snapshot-value space nil node)
      (sync/snapshot-value space nil (event-node/node-create {"id" "m"}))])
    (promise/x:promise-then
     (promise/x:promise-all
      [(sync/snapshot-value space nil node)
       (sync/snapshot-value space nil (event-node/node-create {"id" "m"}))])
     (repl/>notify)))
  => [{"count" 3}
      {"count" 2}])

^{:refer xt.substrate.base-sync/subscribe-transport :added "4.1"}
(fact "subscribes a transport to all sync signals"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/subscribe-transport n "peer-a" "room/a" "sub-a" {"request_id" "req-1"})
    [(router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)
     (router/list-subscriptions n "room/a" sync/SIGNAL_RESET)
     (router/list-subscriptions n "room/a" sync/SIGNAL_ERROR)])
  => [["peer-a"] ["peer-a"] ["peer-a"]])

^{:refer xt.substrate.base-sync/unsubscribe-transport :added "4.1"}
(fact "removes a transport from all sync signals"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
    (sync/unsubscribe-transport n "peer-a" "room/a")
    [(router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)
     (router/list-subscriptions n "room/a" sync/SIGNAL_RESET)
     (router/list-subscriptions n "room/a" sync/SIGNAL_ERROR)])
  => [[] [] []])

^{:refer xt.substrate.base-sync/handle-snapshot :added "4.1"}
(fact "returns a sync snapshot response"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (promise/x:promise-then
     (sync/handle-snapshot (node-space/get-space n "room/a") [{}] {"id" "req-1"} n)
     (repl/>notify)))
  => {"protocol" "node.sync"
      "mode" "snapshot"
      "space" "room/a"
      "cursor" 0
      "snapshot" {"count" 2}})

^{:refer xt.substrate.base-sync/handle-unsubscribe :added "4.1"}
(fact "unsubscribes the caller transport from sync signals"

  (!.dt
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
    (var out (sync/handle-unsubscribe
              (node-space/get-space n "room/a")
              [{"subscription_id" "sub-a"}]
              {"id" "req-1" "meta" {"transport_id" "peer-a"}}
              n))
    [out
     (router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)])
  => [{"protocol" "node.sync"
       "mode" "unsubscribe"
       "space" "room/a"
       "transport_id" "peer-a"
       "subscription_id" "sub-a"
       "ok" true}
      []])

^{:refer xt.substrate.base-sync/install :added "4.1"}
(fact "installs sync request handlers on a node"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/install n {"mode" "live"})
    [(xt/x:not-nil? (event-node/get-handler n sync/ACTION_OPEN))
     (xt/x:not-nil? (event-node/get-handler n sync/ACTION_SNAPSHOT))
     (xt/x:not-nil? (event-node/get-handler n sync/ACTION_RESUME))
     (xt/x:not-nil? (event-node/get-handler n sync/ACTION_UNSUBSCRIBE))
     (. (sync/node-opts n) ["mode"])])
  => [true true true true "live"])

^{:refer xt.substrate.base-sync/install-triggers :added "4.1"}
(fact "installs sync stream triggers on a node"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/install-triggers n {"apply_delta" (fn [state payload _stream _node] (return payload))})
    [(xt/x:not-nil? (event-node/get-trigger n sync/SIGNAL_DELTA))
     (xt/x:not-nil? (event-node/get-trigger n sync/SIGNAL_RESET))
     (xt/x:not-nil? (event-node/get-trigger n sync/SIGNAL_ERROR))
     (xt/x:is-function? (. (sync/node-opts n) ["apply_delta"]))])
  => [true true true true])

^{:refer xt.substrate.base-sync/uninstall :added "4.1"}
(fact "removes sync handlers and triggers from a node"

  (!.dt
    (var n (event-node/node-create {"id" "n"}))
    (sync/install n nil)
    (sync/install-triggers n nil)
    (sync/uninstall n)
    [(event-node/get-handler n sync/ACTION_OPEN)
     (event-node/get-trigger n sync/SIGNAL_DELTA)])
  => [nil nil])

^{:refer xt.substrate.base-sync/open :added "4.1"}
(fact "issues an open request through substrate request handling"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (promise/x:promise-then
     (sync/open n "room/a" {"subscribe" false} nil)
     (fn [out]
       (repl/notify
        {"protocol" (. out ["protocol"])
         "mode" (. out ["mode"])
         "space" (. out ["space"])
         "cursor" (. out ["cursor"])
         "snapshot" (. out ["snapshot"])
         "subscribed" (. out ["subscribed"])
         "has_subscription_id" (xt/x:not-nil? (. out ["subscription_id"]))}))))
  => {"protocol" "node.sync"
      "mode" "open"
      "space" "room/a"
      "cursor" 0
      "snapshot" {"count" 2}
      "subscribed" false
      "has_subscription_id" true})

^{:refer xt.substrate.base-sync/snapshot :added "4.1"}
(fact "issues a snapshot request through substrate request handling"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 3}}}}))
    (sync/install n nil)
    (promise/x:promise-then
     (sync/snapshot n "room/a" {} nil)
     (repl/>notify)))
  => {"protocol" "node.sync"
      "mode" "snapshot"
      "space" "room/a"
      "cursor" 0
      "snapshot" {"count" 3}})

^{:refer xt.substrate.base-sync/resume :added "4.1"}
(fact "issues a resume request through substrate request handling"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 4}}}}))
    (sync/install n nil)
    (-> (sync/publish-delta n "room/a" {"count" 4} nil)
        (promise/x:promise-then
         (fn [_]
           (return (sync/resume n "room/a" {"cursor" 1
                                            "subscribe" false}
                                nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"protocol" (. out ["protocol"])
             "mode" (. out ["mode"])
             "space" (. out ["space"])
             "cursor" (. out ["cursor"])
             "subscribed" (. out ["subscribed"])
             "has_subscription_id" (xt/x:not-nil? (. out ["subscription_id"]))})))))
  => {"protocol" "node.sync"
      "mode" "resume"
      "space" "room/a"
      "cursor" 1
      "subscribed" false
      "has_subscription_id" true})

^{:refer xt.substrate.base-sync/unsubscribe :added "4.1"}
(fact "issues an unsubscribe request through substrate request handling"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 2}}}}))
    (sync/install n nil)
    (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
    (-> (event-node/attach-transport n "peer-a" {"send_fn" (fn [out]
                                                              (return
                                                               (event-node/receive-frame
                                                                n
                                                                out
                                                                {"transport_id" "peer-a"})))})
        (promise/x:promise-then
         (fn [_]
           (return
            (sync/unsubscribe n "room/a" {"subscription_id" "sub-a"}
                              {"transport_id" "peer-a"}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"response" out
             "delta" (router/list-subscriptions n "room/a" sync/SIGNAL_DELTA)})))))
  => {"response" {"protocol" "node.sync"
                  "mode" "unsubscribe"
                  "space" "room/a"
                  "transport_id" "peer-a"
                  "subscription_id" "sub-a"
                  "ok" true}
      "delta" []})

^{:refer xt.substrate.base-sync/publish-delta :added "4.1"}
(fact "publishes a delta stream and advances the cursor"

  (notify/wait-on :dart
    (var sent nil)
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (-> (event-node/attach-transport n "peer-a" {"send_fn" (fn [out]
                                                              (:= sent out)
                                                              (return out))})
        (promise/x:promise-then
         (fn [_]
           (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
           (return (sync/publish-delta n "room/a" {"step" 2} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"cursor" (sync/get-cursor n "room/a")
             "signal" (. sent ["signal"])
             "step" (. sent ["data"] ["delta"] ["step"])})))))
  => {"cursor" 1
      "signal" "node.sync/delta"
      "step" 2})

^{:refer xt.substrate.base-sync/publish-reset :added "4.1"}
(fact "publishes a reset stream and advances the cursor"

  (notify/wait-on :dart
    (var sent nil)
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (-> (event-node/attach-transport n "peer-a" {"send_fn" (fn [out]
                                                              (:= sent out)
                                                              (return out))})
        (promise/x:promise-then
         (fn [_]
           (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
           (return (sync/publish-reset n "room/a" {"count" 10} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"cursor" (sync/get-cursor n "room/a")
             "signal" (. sent ["signal"])
             "count" (. sent ["data"] ["snapshot"] ["count"])})))))
  => {"cursor" 1
      "signal" "node.sync/reset"
      "count" 10})

^{:refer xt.substrate.base-sync/publish-error :added "4.1"}
(fact "publishes an error stream without advancing the cursor"

  (notify/wait-on :dart
    (var sent nil)
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/set-cursor n "room/a" 3)
    (-> (event-node/attach-transport n "peer-a" {"send_fn" (fn [out]
                                                              (:= sent out)
                                                              (return out))})
        (promise/x:promise-then
         (fn [_]
           (sync/subscribe-transport n "peer-a" "room/a" "sub-a" nil)
           (return (sync/publish-error n "room/a" {"message" "boom"} nil))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"cursor" (sync/get-cursor n "room/a")
             "signal" (. sent ["signal"])
             "message" (. sent ["data"] ["error"] ["message"])})))))
  => {"cursor" 3
      "signal" "node.sync/error"
      "message" "boom"})

^{:refer xt.substrate.base-sync/handle-delta :added "4.1"}
(fact "applies a delta stream to the local space state"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers
     n
     {"apply_delta"
      (fn [state payload _stream _node]
        (return {"count" (+ (. state ["count"])
                            (. payload ["delta"] ["step"]))}))})
    (promise/x:promise-then
     (sync/handle-delta (node-space/get-space n "room/a")
                        {"signal" "node.sync/delta"
                         "space" "room/a"
                         "data" {"cursor" 1
                                 "delta" {"step" 2}}}
                        n)
     (fn [_]
       (repl/notify
        {"state" (event-node/get-space-state n "room/a")
         "cursor" (sync/get-cursor n "room/a")}))))
  => {"state" {"count" 3}
      "cursor" 1})

^{:refer xt.substrate.base-sync/handle-reset :added "4.1"}
(fact "applies a reset stream to the local space state"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers n nil)
    (promise/x:promise-then
     (sync/handle-reset (node-space/get-space n "room/a")
                        {"signal" "node.sync/reset"
                         "space" "room/a"
                         "data" {"cursor" 2
                                 "snapshot" {"count" 10}}}
                        n)
     (fn [_]
       (repl/notify
        {"state" (event-node/get-space-state n "room/a")
         "cursor" (sync/get-cursor n "room/a")}))))
  => {"state" {"count" 10}
      "cursor" 2})

^{:refer xt.substrate.base-sync/handle-error :added "4.1"}
(fact "applies an error stream and stores the last error"

  (notify/wait-on :dart
    (var n (event-node/node-create {"id" "n"
                                    "spaces" {"room/a" {"state" {"count" 1}}}}))
    (sync/install-triggers
     n
     {"apply_error"
      (fn [_state payload _stream _node]
        (return (. payload ["error"] ["message"])))} )
    (promise/x:promise-then
     (sync/handle-error (node-space/get-space n "room/a")
                        {"signal" "node.sync/error"
                         "space" "room/a"
                         "data" {"cursor" 3
                                 "error" {"message" "boom"}}}
                        n)
     (fn [out]
       (repl/notify
        {"error" out
         "cursor" (sync/get-cursor n "room/a")
         "last_error" (. (sync/ensure-sync-state (node-space/get-space n "room/a"))
                         ["last_error"])}))))
  => {"error" "boom"
      "cursor" 3
      "last_error" "boom"})
