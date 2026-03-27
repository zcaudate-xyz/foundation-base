(ns js.cell.kernel.base-protocol
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]]})

(defn.js frame-op
  "constructs a protocol frame"
  {:added "4.0"}
  [op id body meta extra]
  (return (j/assign {:op op
                     :id id
                     :body body
                     :meta (or meta {})}
                    (or extra {}))))

(defn.js call-op
  "constructs a call frame"
  {:added "4.0"}
  [id action body meta]
  (return (-/frame "call"
                   id
                   body
                   meta
                   {:action action})))
