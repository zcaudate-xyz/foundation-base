(ns xt.cell.kernel.base-util
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-trace :as trace]]})


(defspec.xt EV_INIT :xt/str)
(defspec.xt EV_STATE :xt/str)

(defspec.xt rand-id
  [:fn [[:xt/maybe :xt/str] :xt/int] :xt/str])

(defspec.xt check-event
  [:fn [:xt/any :xt/str :xt/any :xt/any] :xt/bool])

(defspec.xt arg-encode
  [:fn [:xt/any] :xt/any])

(defspec.xt arg-decode
  [:fn [:xt/any] :xt/any])

(defspec.xt req-frame
  [:fn [:xt/str
        [:xt/maybe :xt/str]
        :xt/any
        [:xt/maybe xt.cell.kernel.spec/AnyMap]
        [:xt/maybe xt.cell.kernel.spec/AnyMap]]
   xt.cell.kernel.spec/RequestFrame])

(defspec.xt req-call
  [:fn [:xt/str :xt/any]
   xt.cell.kernel.spec/RequestFrame])

(defspec.xt req-eval
  [:fn [:xt/any [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/RequestFrame])

(defspec.xt resp-ok
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   xt.cell.kernel.spec/ResponseFrame])

(defspec.xt resp-error
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   xt.cell.kernel.spec/ResponseFrame])

(defspec.xt resp-stream
  [:fn [:xt/str :xt/any]
   xt.cell.kernel.spec/ResponseFrame])

(def.xt EV_INIT    "@worker/::INIT")

(def.xt EV_STATE   "@worker/::STATE")

(defn.xt rand-id
  "prepares a rand-id"
  {:added "4.0"}
  [prefix n]
  (return (xt/x:cat (or prefix "")
                    (str/str-rand n))))

(defn.xt check-event
  "checks that trigger matches signal and event"
  {:added "4.0"}
  [pred signal event ctx]
  (var check false)
  (try
    (var t (:? (xt/x:nil? pred)
               true

               (xt/x:is-boolean? pred)
               pred
               
               (xt/x:is-function? pred)
               (pred signal ctx)
               
               (xt/x:is-object? pred)
               (xt/x:get-key pred signal)
               
               :else
               (== signal pred)))
    (:= check (or (== true t)
                  (and (xt/x:is-function? t) (t event ctx))
                  false))
    (catch err (trace/LOG! {:stack   (. err ["stack"])
                            :message (. err ["message"])})))
  (return check))

(defn.xt arg-encode
  "encodes functions in data tree"
  {:added "4.0"}
  [arg]
  (return (xtd/tree-walk arg
                  (fn [x]
                    (if (xt/x:is-function? x)
                      (return ["fn" (xt/x:to-string x)])
                      (return x)))
                  (fn [x] (return x)))))

(defn.xt arg-decode
  "decodes function in data tree"
  {:added "4.0"}
  [arg]
  (return (xtd/tree-walk arg
                  (fn [x]
                    (if (and (xt/x:is-array? x)
                             (== 2 (xt/x:len x))
                             (== "fn" (xt/x:first x)))
                      (return (xt/x:eval (+ "(" (xt/x:second x) ")")))
                      (return x)))
                  (fn [x] (return x)))))


;;
;;  Requests
;;

(defn.xt req-frame
  "constructs a protocol frame"
  {:added "4.0"}
  [op id body meta extra]
  (return (xtd/obj-assign {:op op
                           :id id
                           :body body
                           :meta (or meta {})}
                          (or extra {}))))

(defn.xt req-call
  "constructs a call request"
  {:added "4.0"}
  [action body]
  (return {:op "call"
           :action action
           :body body}))

(defn.xt req-eval
  "constructs an eval request"
  {:added "4.0"}
  [body is-async]
  (return {:op "eval"
           :body body
           :async is-async}))

(defn.xt resp-ok
  "constructs an ok response"
  {:added "4.0"}
  [op id body]
  (return {:op op
           :id id
           :status "ok"
           :body body}))

(defn.xt resp-error
  "constructs an error response"
  {:added "4.0"}
  [op id body]
  (return {:op op
           :id id
           :status "error"
           :body body}))

(defn.xt resp-stream
  "constructs a stream response"
  {:added "4.0"}
  [signal body]
  (return {:op "stream"
           :status "ok"
           :signal signal
           :body body}))
