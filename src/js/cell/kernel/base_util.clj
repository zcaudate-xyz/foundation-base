(ns js.cell.kernel.base-util
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]]})


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
        [:xt/maybe js.cell.kernel.spec/AnyMap]
        [:xt/maybe js.cell.kernel.spec/AnyMap]]
   js.cell.kernel.spec/RequestFrame])

(defspec.xt req-call
  [:fn [:xt/str :xt/any]
   js.cell.kernel.spec/RequestFrame])

(defspec.xt req-eval
  [:fn [:xt/any [:xt/maybe :xt/bool]]
   js.cell.kernel.spec/RequestFrame])

(defspec.xt resp-ok
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   js.cell.kernel.spec/ResponseFrame])

(defspec.xt resp-error
  [:fn [:xt/str [:xt/maybe :xt/str] :xt/any]
   js.cell.kernel.spec/ResponseFrame])

(defspec.xt resp-stream
  [:fn [:xt/str :xt/any]
   js.cell.kernel.spec/ResponseFrame])

(def$.js EV_INIT    "@worker/::INIT")

(def$.js EV_STATE   "@worker/::STATE")

(defn.js rand-id
  "prepares a rand-id"
  {:added "4.0"}
  [prefix n]
  (return (+ (or prefix "")
             (j/randomId n))))

(defn.js check-event
  "checks that trigger matches signal and event"
  {:added "4.0"}
  [pred signal event ctx]
  (var check false)
  (try
    (var t (:? (k/nil? pred)
               true

               (k/is-boolean? pred)
               pred
               
               (k/fn? pred)
               (pred signal ctx)
               
               (k/obj? pred)
               (k/get-key pred signal)
               
               :else
               (== signal pred)))
    (:= check (or (== true t)
                  (and (k/fn? t) (t event ctx))
                  false))
    (catch err (k/LOG! {:stack   (. err ["stack"])
                        :message (. err ["message"])})))
  (return check))

(defn.js arg-encode
  "encodes functions in data tree"
  {:added "4.0"}
  [arg]
  (return (k/walk arg
                  (fn [x]
                    (if (k/fn? x)
                      (return ["fn" (k/to-string x)])
                      (return x)))
                  k/identity)))

(defn.js arg-decode
  "decodes function in data tree"
  {:added "4.0"}
  [arg]
  (return (k/walk arg
                  (fn [x]
                    (if (and (k/arr? x)
                             (== 2 (k/len x))
                             (== "fn" (k/first x)))
                      (return (k/eval (+ "(" (k/second x) ")")))
                      (return x)))
                  k/identity)))


;;
;;  Requests
;;

(defn.js req-frame
  "constructs a protocol frame"
  {:added "4.0"}
  [op id body meta extra]
  (return (j/assign {:op op
                     :id id
                     :body body
                     :meta (or meta {})}
                    (or extra {}))))

(defn.js req-call
  "constructs a call request"
  {:added "4.0"}
  [action body]
  (return {:op "call"
           :action action
           :body body}))

(defn.js req-eval
  "constructs an eval request"
  {:added "4.0"}
  [body is-async]
  (return {:op "eval"
           :body body
           :async is-async}))

(defn.js resp-ok
  "constructs an ok response"
  {:added "4.0"}
  [op id body]
  (return {:op op
           :id id
           :status "ok"
           :body body}))

(defn.js resp-error
  "constructs an error response"
  {:added "4.0"}
  [op id body]
  (return {:op op
           :id id
           :status "error"
           :body body}))

(defn.js resp-stream
  "constructs a stream response"
  {:added "4.0"}
  [signal body]
  (return {:op "stream"
           :status "ok"
           :signal signal
           :body body}))

