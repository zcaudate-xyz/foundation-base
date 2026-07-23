(ns xt.substrate.base-request
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-space :as space]]})

(defspec.xt RequestResolve
  [:fn [:xt/any] :xt/any])

(defspec.xt RequestReject
  [:fn [:xt/any] :xt/any])

(defspec.xt RequestHandler
  [:fn [space/NodeSpace
        [:xt/maybe [:xt/array :xt/any]]
        frame/NodeFrame
        :xt/any]
       :xt/any])

(defspec.xt PendingEntry
  [:xt/record
   ["resolve" RequestResolve]
   ["reject" RequestReject]
   ["request" frame/NodeFrame]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt ensure-promise
  [:fn [:xt/any] :xt/promise])

(defspec.xt add-pending
  [:fn [:xt/any
        frame/NodeFrame
        RequestResolve
        RequestReject
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       PendingEntry])

(defspec.xt remove-pending
  [:fn [:xt/any :xt/str] [:xt/maybe PendingEntry]])

(defspec.xt settle-pending
  [:fn [:xt/any frame/NodeFrame] [:xt/maybe PendingEntry]])

(defspec.xt invoke-handler
  [:fn [:xt/any frame/NodeFrame] :xt/promise])

(defspec.xt response-body
  [:fn [:xt/any] :xt/promise])

(defn.xt ensure-promise
  "wraps sync values in a native host promise; leaves thenables/native promises as-is"
  {:added "4.1"}
  [value]
  (if (or (promise/x:promise-native? value)
          (and (xt/x:is-object? value)
               (xt/x:is-function? (. value ["then"]))))
    (return value)
    (return (promise/x:promise-run value))))

(defn.xt add-pending
  "adds a pending request entry to a node"
  {:added "4.1"}
  [node request resolve reject meta]
  (var #{pending} node)
  (var #{id} request)
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
  (var #{pending} node)
  (var entry (xt/x:get-key pending request-id))
  (xt/x:del-key pending request-id)
  (return entry))

(defn.xt settle-pending
  "settles a pending request using a response frame"
  {:added "4.1"}
  [node response]
  (var #{reply-to} response)
  (var entry (-/remove-pending node reply-to))
  (when (xt/x:nil? entry)
    (return nil))
  (var #{resolve reject} entry)
  (if (== (. response ["status"]) frame/STATUS_OK)
    (resolve (. response ["data"]))
    (reject response))
  (return entry))

(defn.xt invoke-handler
  "invokes a node handler for a request"
  {:added "4.1"}
  [node request]
  (var #{action} request)
  (var entry (xt/x:get-key (. node ["handlers"])
                           action))
  (when (xt/x:nil? entry)
    (xt/x:err (xt/x:cat "handler not found - " action)))
  (var handler (. entry ["fn"]))
  (var current-space (space/ensure-space node
                                         (. request ["space"])
                                         nil))
  (return
   (-/ensure-promise
    (xt/x:apply handler
                [current-space
                 (. request ["args"])
                 request
                 node]))))

(defn.xt response-body
  "normalises a response frame into request output"
  {:added "4.1"}
  [response]
  (return
   (promise/x:promise-then
    (-/ensure-promise response)
    (fn [frame]
      (if (== (. frame ["status"])
              frame/STATUS_OK)
        (return (. frame ["data"]))
        (xt/x:throw frame))))))
