(ns js.cell.base-util
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]]})

(def$.js EV_INIT    "@/::INIT")

(def$.js EV_STATE   "@/::STATE")

(defn.js rand-id
  "prepares a rand-id"
  {:added "4.0"}
  [prefix n]
  (return (+ (or prefix "")
             (j/randomId n))))

(defn.js check-event
  "checks that trigger matches topic and event"
  {:added "4.0"}
  [pred topic event ctx]
  (var check false)
  (try
    (var t (:? (k/nil? pred)
               true

               (k/is-boolean? pred)
               pred
               
               (k/fn? pred)
               (pred topic ctx)
               
               (k/obj? pred)
               (k/get-key pred topic)
               
               :else
               (== topic pred)))
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

