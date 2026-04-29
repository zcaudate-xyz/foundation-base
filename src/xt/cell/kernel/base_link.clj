(ns xt.cell.kernel.base-link
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt] [xt.lang.common-trace :as trace] [xt.cell.kernel.base-util :as util]]})

(defspec.xt link-listener-call
  [:fn [xt.cell.kernel.spec/ResponseFrame
        xt.cell.kernel.spec/ActiveCallMap]
   :xt/any])

(defspec.xt link-listener-event
  [:fn [xt.cell.kernel.spec/ResponseFrame
        xt.cell.kernel.spec/LinkCallbackMap]
   xt.cell.kernel.spec/StringList])

(defspec.xt link-listener
  [:fn [:xt/any
        xt.cell.kernel.spec/ActiveCallMap
        xt.cell.kernel.spec/LinkCallbackMap]
   :xt/any])

(defspec.xt link-create-worker
  [:fn [:xt/any
        xt.cell.kernel.spec/ActiveCallMap
        xt.cell.kernel.spec/LinkCallbackMap]
   :xt/any])

(defspec.xt link-create
  [:fn [:xt/any] xt.cell.kernel.spec/LinkRecord])

(defspec.xt link-active
  [:fn [xt.cell.kernel.spec/LinkRecord]
   xt.cell.kernel.spec/ActiveCallMap])

(defspec.xt add-callback
  [:fn [xt.cell.kernel.spec/LinkRecord
        :xt/str
        :xt/any
        [:fn [:xt/any :xt/any] :xt/any]]
   [:xt/array :xt/any]])

(defspec.xt list-callbacks
  [:fn [xt.cell.kernel.spec/LinkRecord]
   xt.cell.kernel.spec/StringList])

(defspec.xt remove-callback
  [:fn [xt.cell.kernel.spec/LinkRecord :xt/str]
   [:xt/array :xt/any]])

(defspec.xt call-id
  [:fn [xt.cell.kernel.spec/LinkRecord] :xt/str])

(defspec.xt call
  [:fn [xt.cell.kernel.spec/LinkRecord
        xt.cell.kernel.spec/RequestFrame]
   :xt/any])

(defn.xt link-listener-call
  "resolves a call to the link"
  {:added "4.0"}
  [data active]
  (var #{op id status body} data)
  (var entry (xt/x:get-key active id))
  (when (xt/x:not-nil? entry)
    (var #{input time resolve reject} entry)
    (:= input (or input {}))
    (try
      (cond (== status "ok")
            (cond (== op "call")
                  (do (xt/x:del-key active id) 
                      (return (resolve (util/arg-decode body))))
                  
                  (== op "eval")
                  (do (xt/x:del-key active id)
                      (var out (xt/x:json-decode body))
                      (var #{type value} out)
                      (cond (== type "data")
                            (return (resolve out.value))
                            
                            :else
                            (return (resolve out)))))
            :else
            (do (xt/x:del-key active id)
                (return (reject (xt/x:obj-assign {:action (. input ["action"])
                                                  :input (. input ["body"])
                                                  :start-time time
                                                  :end-time (xt/x:now-ms)}
                                                 data)))))
      (catch err (return (reject {:op op
                                  :id id
                                  :action (. input ["action"])
                                  :status "error"
                                  :message "Format Invalid"
                                  :input (. input ["body"])
                                  :start-time time
                                  :end-time (xt/x:now-ms)
                                  :body body}))))))

(defn.xt link-listener-event
  "notifies all registered callbacks"
  {:added "4.0"}
  [data callbacks]
  (var #{op id signal status body} data)
  (var out [])
  (xt/for:object [[p-id callback] callbacks]
    (var #{pred handler} callback)
    (when (util/check-event pred signal data)
      (try (handler data signal)
           (xt/x:arr-push out p-id)
           (catch err (trace/LOG! {:stack   (. err ["stack"])
                                   :message (. err ["message"])})))))
  (return out))

(defn.xt link-listener
  "constructs a link listener"
  {:added "4.0"}
  [e active callbacks]
  (var #{data} e)
  (var #{op id} data)
  (cond  (or (== op "eval")
             (== op "call"))
         (return (-/link-listener-call data active))

         (or (== op "stream"))
         (return (-/link-listener-event data callbacks))))

(defn.xt link-create-worker
  "helper function to create a worker"
  {:added "4.0"}
  [worker-url active callbacks]
  (cond (or (xt/x:is-string? worker-url)
            (xt/x:is-function? worker-url))
        (do (var worker (new Worker worker-url))
            (. worker (addEventListener
                       "message"
                       (fn:> [e] (-/link-listener e active callbacks))))
            (return worker))

        :else
        (do (var #{create-fn} worker-url)
            (return
             (create-fn 
              (fn [data]
                (-/link-listener {:data data} active callbacks)))))))

(defn.xt link-create
  "creates a link from url"
  {:added "4.0"}
  [worker-url]
  (var active {})
  (var callbacks {})
  (var worker (-/link-create-worker worker-url active callbacks))
  (return {"::" "cell.link"
           :id (util/rand-id "" 3)
           :worker worker
           :active active
           :callbacks callbacks}))

(defn.xt link-active
  "gets the calls that are active"
  {:added "4.0"}
  [link]
  (return (xt/x:get-key link "active")))

(defn.xt add-callback
  "adds a callback to the link"
  {:added "4.0"}
  [link key pred handler]
  (var prev (. link ["callbacks"] [key]))
  (:= (. link ["callbacks"] [key]) {:key key
                                    :pred pred
                                    :handler handler})
  (return [prev]))

(defn.xt list-callbacks
  "lists all callbacks on the link"
  {:added "4.0"}
  [link]
  (return (xt/x:obj-keys (. link ["callbacks"]))))

(defn.xt remove-callback
  "removes a callback on the link"
  {:added "4.0"}
  [link key]
  (var prev (. link ["callbacks"] [key]))
  (del (. link ["callbacks"] [key]))
  (return [prev]))

;;
;; CALL
;;

(defn.xt call-id
  "gets the call id"
  {:added "4.0"}
  [link]
  (var #{id active} link)
  (while true
    (var cid (util/rand-id (+ id "-") 3))
    (when (xt/x:nil? (xt/x:get-key active cid))
      (return cid))))

(defn.xt call
  "calls the link with an event"
  {:added "4.0"}
  [link event]
  (var #{worker active} link)
  (var cid (or (. event ["id"])
               (-/call-id link)))
  (:= (. event ["id"]) cid)
  (var input (util/arg-encode event))
  (var p (new Promise (fn [resolve reject]
                        (:= (. active [cid])
                            {:resolve (fn [data]
                                        (resolve data))
                             :reject (fn [data]
                                       (reject data))
                              :input  input
                              :time   (xt/x:now-ms)}))))
  (var #{postRequest} worker)
  (if postRequest
    (postRequest input)
    (. worker (postMessage input)))
  (return p))
