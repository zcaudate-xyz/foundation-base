(ns xt.substrate.base-json
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-router :as router]]})

(defspec.xt frame-kind?
  [:fn [[:xt/maybe :xt/str]] :xt/bool])

(defspec.xt valid-frame?
  [:fn [:xt/any] :xt/bool])

(defspec.xt normalize-error
  [:fn [:xt/any] :xt/any])

(defspec.xt normalize-frame
  [:fn [:xt/any] :xt/any])

(defspec.xt encode-frame
  [:fn [:xt/any] :xt/str])

(defspec.xt decode-frame
  [:fn [:xt/any] :xt/any])

(defn.xt frame-kind?
  "checks whether the event kind is supported by the node JSON wire format"
  {:added "4.1"}
  [kind]
  (return (or (== kind frame/KIND_REQUEST)
              (== kind frame/KIND_RESPONSE)
              (== kind frame/KIND_STREAM)
              (== kind router/KIND_SUBSCRIBE)
              (== kind router/KIND_UNSUBSCRIBE))))

(defn.xt valid-frame?
  "checks whether a value is a valid node/router event frame"
  {:added "4.1"}
  [value]
  (when (not (xt/x:is-object? value))
    (return false))
  (var #{kind id space meta} value)
  (when (or (not (-/frame-kind? kind))
            (not (xt/x:is-string? id))
            (not (xt/x:is-string? space))
            (and (xt/x:not-nil? meta)
                 (not (xt/x:is-object? meta))))
    (return false))
  (cond (== kind frame/KIND_REQUEST)
        (return (and (xt/x:is-string? (. value ["action"]))
                     (or (xt/x:nil? (. value ["args"]))
                         (xt/x:is-array? (. value ["args"])))))

        (== kind frame/KIND_RESPONSE)
        (return (and (xt/x:is-string? (. value ["reply_to"]))
                     (xt/x:is-string? (. value ["status"]))))

        :else
        (return (xt/x:is-string? (. value ["signal"])))))

(defn.xt normalize-error
  "normalizes wire errors into plain JSON-safe objects"
  {:added "4.1"}
  [err]
  (cond (xt/x:nil? err)
        (return nil)

        (xt/x:is-string? err)
        (return {"message" err})

        (xt/x:is-object? err)
        (do
          (var out (xt/x:obj-clone err))
          (when (and (not (xt/x:has-key? out "message"))
                     (xt/x:has-key? out "error")
                     (xt/x:is-string? (. out ["error"])))
            (xt/x:set-key out "message" (. out ["error"])))
          (when (and (not (xt/x:has-key? out "message"))
                     (xt/x:has-key? out "status")
                     (xt/x:is-string? (. out ["status"])))
            (xt/x:set-key out "message" (. out ["status"])))
          (when (not (xt/x:has-key? out "message"))
            (xt/x:set-key out "message" (xt/x:to-string err)))
          (return out))

        :else
        (return {"message" (xt/x:to-string err)})))

(defn.xt normalize-frame
  "normalizes a frame for JSON transport emission"
  {:added "4.1"}
  [frame]
  (when (not (xt/x:is-object? frame))
    (return frame))
  (var out (xt/x:obj-clone frame))
  (when (and (== frame/KIND_RESPONSE (. out ["kind"]))
             (== frame/STATUS_ERROR (. out ["status"]))
             (xt/x:not-nil? (. out ["error"])))
    (xt/x:set-key out
                  "error"
                  (-/normalize-error (. out ["error"]))))
  (return out))

(defn.xt encode-frame
  "encodes a node/router frame as JSON"
  {:added "4.1"}
  [frame]
  (var out (-/normalize-frame frame))
  (when (not (-/valid-frame? out))
    (xt/x:err "invalid node json frame"))
  (return (xt/x:json-encode out)))

(defn.xt decode-frame
  "decodes JSON text into a validated node/router frame"
  {:added "4.1"}
  [input]
  (var out (:? (xt/x:is-string? input)
               (xt/x:json-decode input)
               input))
  (when (not (-/valid-frame? out))
    (xt/x:err "invalid node json frame"))
  (return out))
