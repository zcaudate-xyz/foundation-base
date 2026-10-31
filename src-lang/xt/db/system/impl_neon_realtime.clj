(ns xt.db.system.impl-neon-realtime
  (:require [lang.core :as l]))
(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.db.system.impl-common :as common]
             [xt.net.addon-neon :as addon]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as http]]})

(defn.xt current?
  [impl conn-id entry]
  (return (and (xt/x:get-key entry "active" nil)
               (== (xt/x:get-key entry "generation" nil) (xtd/get-in impl ["state" "connections" conn-id "generation"])))))

(defn.xt cache-impl
  [impl]
  (var f (or (xtd/get-in impl ["metadata" "caching_fn"])
             (xtd/get-in impl ["client" "defaults" "caching_fn"])))
  (return (:? (xt/x:is-function? f) (f) nil)))

(defn.xt sequence-after?
  [next previous]
  (return (or (xt/x:nil? previous)
              (> (xt/x:len next) (xt/x:len previous))
              (and (== (xt/x:len next) (xt/x:len previous))
                   (xt/x:str-comp previous next)))))

(defn.xt apply-events
  "Applies a bridge batch once; the cursor advances only after all events apply."
  [impl entry batch]
  (var caching (-/cache-impl impl))
  (when (xt/x:nil? caching) (xt/x:err "Neon caching_fn is required for subscriptions"))
  (xt/for:array [event (xt/x:get-key batch "events" nil)]
    (var topic (xt/x:get-key event "topic" nil))
    (var seq (xt/x:get-key event "seq" nil))
    (when (-/sequence-after? seq (xtd/get-in entry ["seen" topic]))
      (common/sync-process-payload caching (xt/x:get-key event "payload" nil))
      (xtd/set-in entry ["seen" topic] seq)))
  (xt/x:set-key entry "cursor" (xt/x:get-key batch "cursor" nil))
  (return true))

(defn.xt poll-loop
  [impl conn-id entry]
  (when (not (-/current? impl conn-id entry)) (return (promise/x:promise-run nil)))
  (var client (xt/x:get-key impl "client" nil))
  (var input (addon/bridge-input client "/events/poll" {"connection" (xt/x:get-key entry "connection" nil) "cursor" (xt/x:get-key entry "cursor" nil)}))
  (return (-> (fetch/request-http client input)
    (promise/x:promise-then
     (fn [response]
       (when (not (-/current? impl conn-id entry)) (return nil))
       (var normalized (http/response-normalize response))
       (var #{status body} normalized)
       (cond (== status 200)
             (do (-/apply-events impl entry body)
                 (xt/x:set-key entry "status" "live")
                 (xt/x:set-key entry "delay" 250)
                 (return (xt/x:async-run (fn [] (return (-/poll-loop impl conn-id entry))))))
             (== status 409)
             (do (var resync (xtd/get-in impl ["client" "defaults" "resync"]))
                 (xt/x:set-key entry "status" "resync_required")
                 (when (not (xt/x:is-function? resync))
                   (xt/x:set-key entry "active" false)
                   (return nil))
                 (return (-> (addon/bridge-request client "/events/subscribe" {"topics" (xt/x:get-key entry "topics" nil)})
                   (promise/x:promise-then (fn [subscription]
                     (xt/x:set-key entry "connection" (xt/x:get-key subscription "connection" nil))
                     (xt/x:set-key entry "cursor" (xt/x:get-key subscription "cursor" nil))
                     (when (not (-/current? impl conn-id entry)) (return nil))
                     (return (resync impl (xt/x:get-key entry "topics" nil)))))
                   (promise/x:promise-then (fn [_]
                     (xt/x:set-key entry "status" "connecting")
                     (when (-/current? impl conn-id entry) (return (xt/x:async-run (fn [] (return (-/poll-loop impl conn-id entry))))))
                     (return nil))))))
             (or (== status 401) (== status 403))
             (do (xt/x:set-key entry "active" false)
                 (var cache (-/cache-impl impl))
                 (when cache (common/clear-db cache))
                 (xt/x:set-key entry "status" (or (xt/x:get-key body "code" nil) "error"))
                 (return nil))
             :else (xt/x:err "Neon poll failed"))))
    (promise/x:promise-catch
     (fn [err]
       (when (not (-/current? impl conn-id entry)) (return nil))
       (when (== "resync_required" (xt/x:get-key entry "status" nil))
         (xt/x:set-key entry "active" false)
         (xt/x:set-key entry "status" "resync_failed")
         (return (-> (addon/bridge-request client "/events/unsubscribe" {"connection" (xt/x:get-key entry "connection" nil)})
           (promise/x:promise-catch (fn [_] (return nil))))))
       (xt/x:set-key entry "status" "retrying")
       (var delay (xt/x:get-key entry "delay" nil))
       (xt/x:set-key entry "delay" (:? (< delay 15000) (* delay 2) 30000))
       (return (addon/backoff delay (fn [] (return (xt/x:async-run (fn [] (return (-/poll-loop impl conn-id entry)))))))))))))

(defn.xt close-entry
  [impl entry]
  (xt/x:set-key entry "active" false)
  (when (xt/x:get-key entry "connection" nil)
    (return (-> (addon/bridge-request (xt/x:get-key impl "client" nil) "/events/unsubscribe" {"connection" (xt/x:get-key entry "connection" nil)})
                (promise/x:promise-catch (fn [_] (return nil))))))
  (return (promise/x:promise-run nil)))

(defn.xt subscribe
  [impl conn-id topics]
  (when (xt/x:nil? (-/cache-impl impl)) (xt/x:err "Neon caching_fn is required for subscriptions"))
  (var previous (xtd/get-in impl ["state" "connections" conn-id]))
  (var wanted {})
  (when previous
    (xt/for:array [topic (xt/x:get-key previous "topics" nil)] (xt/x:set-key wanted topic true)))
  (xt/for:array [topic topics] (xt/x:set-key wanted topic true))
  (var merged (xt/x:obj-keys wanted))
  (when (and previous (xt/x:get-key previous "active" nil)
             (== (xt/x:len merged) (xt/x:len (xt/x:get-key previous "topics" nil))))
    (return (promise/x:promise-run true)))
  (var generation (+ 1 (or (xtd/get-in impl ["state" "generation"]) 0)))
  (xtd/set-in impl ["state" "generation"] generation)
  (var cursor (:? previous (xt/x:get-key previous "cursor" nil) nil))
  (var entry {"generation" generation "topics" merged "active" true "status" "connecting" "seen" {} "delay" 250 "cursor" cursor})
  (xtd/set-in impl ["state" "connections" conn-id] entry)
  (var client (xt/x:get-key impl "client" nil))
  (var closing (:? previous (-/close-entry impl previous) (promise/x:promise-run nil)))
  (return (-> closing
    (promise/x:promise-then (fn [_] (return (addon/bridge-request client "/events/subscribe" {"topics" merged "cursor" cursor}))))
    (promise/x:promise-catch (fn [err]
      (var data (xt/x:ex-data err))
      (var resync (xtd/get-in impl ["client" "defaults" "resync"]))
      (when (not (and (== 409 (xtd/get-in data ["status"])) (xt/x:is-function? resync))) (throw err))
      (return (-> (addon/bridge-request client "/events/subscribe" {"topics" merged})
        (promise/x:promise-then (fn [subscription]
          (xt/x:set-key entry "connection" (xt/x:get-key subscription "connection" nil))
          (when (not (-/current? impl conn-id entry)) (return (-/close-entry impl entry)))
          (return (-> (promise/x:promise-run (resync impl merged))
            (promise/x:promise-then (fn [_] (return subscription)))))))))))
    (promise/x:promise-then (fn [result]
      (xt/x:set-key entry "connection" (xt/x:get-key result "connection" nil))
      (xt/x:set-key entry "cursor" (xt/x:get-key result "cursor" nil))
      (when (not (-/current? impl conn-id entry)) (return (-/close-entry impl entry)))
      (xt/x:set-key entry "task"
        (promise/x:promise-new (fn [resolve reject]
          (xt/x:async-run (fn []
            (-> (-/poll-loop impl conn-id entry)
              (promise/x:promise-then resolve)
              (promise/x:promise-catch reject)))))))
      (return true)))
    (promise/x:promise-catch (fn [err]
      (xt/x:set-key entry "status" "error")
      (-/close-entry impl entry)
      (throw err))))))

(defn.xt unsubscribe
  [impl conn-id topics]
  (var entry (xtd/get-in impl ["state" "connections" conn-id]))
  (when (xt/x:nil? entry) (return (promise/x:promise-run true)))
  (var removed {})
  (xt/for:array [topic topics] (xt/x:set-key removed topic true))
  (var remaining [])
  (xt/for:array [topic (xt/x:get-key entry "topics" nil)]
    (when (not (xt/x:get-key removed topic)) (xt/x:arr-push remaining topic)))
  (when (> (xt/x:len remaining) 0)
    (xt/x:set-key entry "topics" remaining)
    (return (-/subscribe impl conn-id remaining)))
  (xt/x:del-key (xtd/get-in impl ["state" "connections"]) conn-id)
  (return (-/close-entry impl entry)))

(defn.xt stop
  [impl]
  (var tasks [])
  (xt/for:object [[_ entry] (xtd/get-in impl ["state" "connections"])]
    (xt/x:arr-push tasks (-/close-entry impl entry)))
  (xtd/set-in impl ["state" "connections"] {})
  (return (promise/x:promise-all tasks)))
