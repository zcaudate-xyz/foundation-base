(ns js.cell-v3.kernel.protocol
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]]})

(defn.js frame
  "constructs a protocol frame"
  {:added "4.0"}
  [op id body meta extra]
  (return (j/assign {:op op
                     :id id
                     :body body
                     :meta (or meta {})}
                    (or extra {}))))

(defn.js hello
  "constructs a hello frame"
  {:added "4.0"}
  [id capabilities meta]
  (return (-/frame "hello"
                   id
                   {:capabilities capabilities}
                   meta
                   {})))

(defn.js call
  "constructs a call frame"
  {:added "4.0"}
  [id action body meta]
  (return (-/frame "call"
                   id
                   body
                   meta
                   {:action action})))

(defn.js result
  "constructs a result frame"
  {:added "4.0"}
  [id status body meta ref]
  (return (-/frame "result"
                   id
                   body
                   meta
                   {:status status
                    :ref ref})))

(defn.js emit
  "constructs an emit frame"
  {:added "4.0"}
  [id signal status body meta ref]
  (return (-/frame "emit"
                   id
                   body
                   meta
                   {:signal signal
                    :status status
                    :ref ref})))

(defn.js subscribe
  "constructs a subscribe frame"
  {:added "4.0"}
  [id signal meta]
  (return (-/frame "subscribe"
                   id
                   nil
                   meta
                   {:signal signal})))

(defn.js unsubscribe
  "constructs an unsubscribe frame"
  {:added "4.0"}
  [id signal meta]
  (return (-/frame "unsubscribe"
                   id
                   nil
                   meta
                   {:signal signal})))

(defn.js task
  "constructs a task frame"
  {:added "4.0"}
  [id ref status body meta]
  (return (-/frame "task"
                   id
                   body
                   meta
                   {:ref ref
                    :status status})))

(defn.js frame-op
  "gets the frame op"
  {:added "4.0"}
  [frame]
  (return (k/get-key frame "op")))

(defn.js frame-id
  "gets the frame id"
  {:added "4.0"}
  [frame]
  (return (k/get-key frame "id")))

(defn.js frame-action
  "gets the frame action"
  {:added "4.0"}
  [frame]
  (return (k/get-key frame "action")))

(defn.js frame-signal
  "gets the frame signal"
  {:added "4.0"}
  [frame]
  (return (k/get-key frame "signal")))
