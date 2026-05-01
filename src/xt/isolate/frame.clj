(ns xt.isolate.frame
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.lang.common-trace :as trace]]})

;;
;; Constants
;;

(defspec.xt EV_INIT  :xt/str)
(defspec.xt EV_STATE :xt/str)

;;
;; Spec declarations for frame construction helpers
;;

(defspec.xt rand-id
  [:fn [[:xt/maybe :xt/str] :xt/int] :xt/str])

(defspec.xt check-topic
  [:fn [:xt/any :xt/str :xt/any] :xt/bool])

(defspec.xt req-call
  [:fn [:xt/str :xt/any]
   xt.isolate.spec/RequestFrame])

(defspec.xt req-notify
  [:fn [:xt/str :xt/any]
   xt.isolate.spec/RequestFrame])

(defspec.xt req-frame
  [:fn [:xt/str
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe xt.isolate.spec/AnyMap]
        [:xt/maybe xt.isolate.spec/AnyMap]]
   xt.isolate.spec/RequestFrame])

(defspec.xt resp-ok
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   xt.isolate.spec/ResponseFrame])

(defspec.xt resp-error
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   xt.isolate.spec/ResponseFrame])

(defspec.xt resp-event
  [:fn [:xt/str :xt/any]
   xt.isolate.spec/ResponseFrame])

;;
;; Constant values
;;

(def$.xt EV_INIT  "@isolate/::INIT")

(def$.xt EV_STATE "@isolate/::STATE")

;;
;; Helpers
;;

(defn.xt rand-id
  "prepares a rand-id"
  {:added "4.0"}
  [prefix n]
  (return (+ (or prefix "")
             (str/str-rand n))))

(defn.xt check-topic
  "checks that a pred matches a topic and event"
  {:added "4.0"}
  [pred topic event]
  (var check false)
  (try
    (var t (:? (xt/x:nil? pred)
               true

               (xt/x:is-boolean? pred)
               pred

               (xt/x:is-function? pred)
               (pred topic event)

               (xt/x:is-object? pred)
               (xt/x:get-key pred topic)

               :else
               (== topic pred)))
    (:= check (or (== true t)
                  (and (xt/x:is-function? t) (t event))
                  false))
    (catch err (trace/LOG! {:stack   (. err ["stack"])
                            :message (. err ["message"])})))
  (return check))

;;
;; Frame constructors
;;

(defn.xt req-frame
  "constructs a generic request frame"
  {:added "4.0"}
  [op id body meta extra]
  (return (xt/x:obj-assign {:op   op
                            :id   id
                            :body body
                            :meta (or meta {})}
                           (or extra {}))))

(defn.xt req-call
  "constructs a call request frame"
  {:added "4.0"}
  [route body]
  (return {:op    "call"
           :route route
           :body  body}))

(defn.xt req-notify
  "constructs a notify (fire-and-forget) request frame"
  {:added "4.0"}
  [route body]
  (return {:op    "notify"
           :route route
           :body  body}))

(defn.xt resp-ok
  "constructs an ok response frame"
  {:added "4.0"}
  [op id body]
  (return {:op     op
           :id     id
           :status "ok"
           :body   body}))

(defn.xt resp-error
  "constructs an error response frame"
  {:added "4.0"}
  [op id body]
  (return {:op     op
           :id     id
           :status "error"
           :body   body}))

(defn.xt resp-event
  "constructs a broadcast event frame"
  {:added "4.0"}
  [topic body]
  (return {:op     "stream"
           :status "ok"
           :topic  topic
           :body   body}))
