(ns xt.substrate.base-frame
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]]})

(defspec.xt NodeFrame
  [:xt/record
   ["kind" :xt/str]
   ["id" :xt/str]
   ["space" :xt/str]
   ["meta" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["action" [:xt/maybe :xt/str]]
   ["args" [:xt/maybe [:xt/array :xt/any]]]
   ["reply_to" [:xt/maybe :xt/str]]
   ["status" [:xt/maybe :xt/str]]
   ["data" [:xt/maybe :xt/any]]
   ["error" [:xt/maybe :xt/any]]
   ["signal" [:xt/maybe :xt/str]]
   ["cause" [:xt/maybe :xt/any]]])

(defspec.xt rand-id
  [:fn [[:xt/maybe :xt/str] :xt/int] :xt/str])

(defspec.xt frame
  [:fn [:xt/str
        :xt/str
        [:xt/maybe :xt/str]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeFrame])

(defspec.xt request-frame
  [:fn [[:xt/maybe :xt/str]
        :xt/str
        [:xt/maybe [:xt/array :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeFrame])

(defspec.xt response-frame
  [:fn [:xt/str
        [:xt/maybe :xt/str]
        :xt/str
        :xt/any
        [:xt/maybe :xt/any]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeFrame])

(defspec.xt response-ok-frame
  [:fn [:xt/str
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeFrame])

(defspec.xt response-error-frame
  [:fn [:xt/str
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       NodeFrame])

(defspec.xt stream-frame
  [:fn [[:xt/maybe :xt/str]
        :xt/str
        :xt/any
        [:xt/maybe [:xt/dict :xt/str :xt/any]]
        [:xt/maybe :xt/any]]
       NodeFrame])

(defspec.xt request-frame?
  [:fn [NodeFrame] :xt/bool])

(defspec.xt response-frame?
  [:fn [NodeFrame] :xt/bool])

(defspec.xt stream-frame?
  [:fn [NodeFrame] :xt/bool])

(def$.xt KIND_REQUEST  "request")
(def$.xt KIND_RESPONSE "response")
(def$.xt KIND_STREAM   "stream")

(def$.xt STATUS_OK    "ok")
(def$.xt STATUS_ERROR "error")

(def$.xt SPACE_NODE "__NODE__")

(defn.xt rand-id
  "creates a simple random id"
  {:added "4.1"}
  [prefix n]
  (return (xt/x:cat (or prefix "")
                    (str/str-rand n))))

(defn.xt frame
  "constructs a base node frame"
  {:added "4.1"}
  [kind id space meta extra]
  (return
   (xt/x:obj-assign
    {:kind kind
     :id id
     :space (or space -/SPACE_NODE)
     :meta (or meta {})}
    (or extra {}))))

(defn.xt request-frame
  "constructs a request frame"
  {:added "4.1"}
  [space action args meta]
  (:= meta (or meta {}))
  (return
    (-/frame -/KIND_REQUEST
             (or (xt/x:get-key meta "id")
                 (-/rand-id "req-" 6))
            space
            meta
            {:action action
             :args (or args [])})))

(defn.xt response-frame
  "constructs a response frame"
  {:added "4.1"}
  [reply-to space status data error meta]
  (:= meta (or meta {}))
  (return
    (-/frame -/KIND_RESPONSE
             (or (xt/x:get-key meta "id")
                 (-/rand-id "res-" 6))
            space
            meta
            {:reply_to reply-to
             :status status
             :data data
             :error error})))

(defn.xt response-ok-frame
  "constructs a successful response frame"
  {:added "4.1"}
  [reply-to space data meta]
  (return (-/response-frame reply-to space -/STATUS_OK data nil meta)))

(defn.xt response-error-frame
  "constructs an errored response frame"
  {:added "4.1"}
  [reply-to space error meta]
  (return (-/response-frame reply-to space -/STATUS_ERROR nil error meta)))

(defn.xt stream-frame
  "constructs a publish frame"
  {:added "4.1"}
  [space signal data meta cause]
  (:= meta (or meta {}))
  (return
    (-/frame -/KIND_STREAM
             (or (xt/x:get-key meta "id")
                 (-/rand-id "evt-" 6))
            space
            meta
            {:signal signal
             :data data
             :cause cause})))

(defn.xt request-frame?
  "checks if a frame is a request"
  {:added "4.1"}
  [frame]
  (return (== -/KIND_REQUEST
              (xt/x:get-key frame "kind"))))

(defn.xt response-frame?
  "checks if a frame is a response"
  {:added "4.1"}
  [frame]
  (return (== -/KIND_RESPONSE
              (xt/x:get-key frame "kind"))))

(defn.xt stream-frame?
  "checks if a frame is a stream"
  {:added "4.1"}
  [frame]
  (return (== -/KIND_STREAM
              (xt/x:get-key frame "kind"))))
