(ns xt.event.node-request
  (:require [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.node-frame :as frame]
             [xt.event.node-space :as space]]})

(defspec.xt RequestHandler :xt/any)

(defn.xt ensure-promise
  "wraps sync values in a native host promise"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))

(defn.xt add-pending
  "adds a pending request entry to a node"
  {:added "4.1"}
  [node request resolve reject meta]
  (var pending (xt/x:get-key node "pending"))
  (var id (xt/x:get-key request "id"))
  (var entry {:resolve resolve
              :reject reject
              :request request
              :meta (or meta {})})
  (xt/x:set-key pending id entry)
  (return entry))

(defn.xt remove-pending
  "removes a pending request entry"
  {:added "4.1"}
  [node request-id]
  (var pending (xt/x:get-key node "pending"))
  (var entry (xt/x:get-key pending request-id))
  (xt/x:del-key pending request-id)
  (return entry))

(defn.xt settle-pending
  "settles a pending request using a response frame"
  {:added "4.1"}
  [node response]
  (var reply-to (or (xt/x:get-key response "reply-to")
                    (xt/x:get-key response "reply_to")))
  (var entry (-/remove-pending node reply-to))
  (when (xt/x:nil? entry)
    (return nil))
  (var resolve (xt/x:get-key entry "resolve"))
  (var reject (xt/x:get-key entry "reject"))
  (if (== (xt/x:get-key response "status") frame/STATUS_OK)
    (resolve (xt/x:get-key response "data"))
    (reject response))
  (return entry))

(defn.xt invoke-handler
  "invokes a node handler for a request"
  {:added "4.1"}
  [node request]
  (var action (xt/x:get-key request "action"))
  (var entry (xt/x:get-key (xt/x:get-key node "handlers")
                           action))
  (when (xt/x:nil? entry)
    (xt/x:err (xt/x:cat "handler not found - " action)))
  (var handler (xt/x:get-key entry "fn"))
  (var current-space (space/ensure-space node
                                         (xt/x:get-key request "space")
                                         nil))
  (return
   (-/ensure-promise
    (handler current-space
             (xt/x:get-key request "args")
             request
             node))))

(defn.xt response-body
  "normalises a response frame into request output"
  {:added "4.1"}
  [response]
  (return
   (promise/x:promise-then
    (-/ensure-promise response)
    (fn [frame]
      (if (== (xt/x:get-key frame "status")
              frame/STATUS_OK)
        (return (xt/x:get-key frame "data"))
        (xt/x:throw frame))))))
