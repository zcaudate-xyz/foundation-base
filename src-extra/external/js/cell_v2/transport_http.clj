(ns js.cell-v2.transport-http
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.cell-v2.transport :as transport]]})

(defn.js normalize-response
  "normalizes an http response into a frame array"
  {:added "4.0"}
  [transport response]
  (var decode (or (. transport ["decode"])
                  (fn [payload]
                    (cond (k/is-string? payload)
                          (return (k/json-decode payload))

                          :else
                          (return payload)))))
  (var out (decode response transport))
  (cond (k/nil? out)
        (return [])

        (k/arr? out)
        (return out)

        :else
        (return [out])))

(defn.js apply-response
  "applies http response frames back into the transport"
  {:added "4.0"}
  [transport response]
  (var frames (-/normalize-response transport response))
  (k/for:array [frame frames]
    (transport/receive-frame transport frame nil))
  (return frames))

(defn.js make-http-transport
  "creates a request-reply http transport"
  {:added "4.0"}
  [request opts]
  (:= opts (or opts {}))
  (var tx (transport/make-transport nil opts))
  (:= (. tx ["request"]) request)
  (:= (. tx ["encode"]) (. opts ["encode"]))
  (:= (. tx ["decode"]) (. opts ["decode"]))
  (:= (. tx ["send"])
      (fn [frame]
        (var encode (or (. tx ["encode"])
                        (fn [payload]
                          (return payload))))
        (var payload (encode frame tx))
        (var out (request payload tx))
        (if (transport/promise? out)
          (return (. out
                     (then (fn [response]
                             (return (-/apply-response tx response))))
                     (catch (fn [err]
                              (return (-/apply-response
                                       tx
                                       {:op "result"
                                        :id (. frame ["id"])
                                        :status "error"
                                        :body (transport/error-body err)
                                        :meta {}
                                        :ref nil}))))))
          (return (-/apply-response tx out)))))
  (when (. opts ["system"])
    (transport/bind-system tx
                           (. opts ["system"])
                           {:forwardAll (. opts ["forwardAll"])
                            :listenerId (. opts ["listenerId"])}))
  (return tx))
