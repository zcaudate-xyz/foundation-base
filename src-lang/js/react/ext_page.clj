(ns js.react.ext-page
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as xtt]
             [xt.event.base-model :as event-model]
             [xt.event.base-listener :as event-common]
             [xt.substrate.page-core :as page-core]
             [js.react :as r]
             [xt.lang.spec-promise :as promise]]})

(defn.js model-key
  "stable listener key for a page model path"
  {:added "4.1"}
  [space-id path]
  (return (xt/x:json-encode [space-id path])))

(defn.js get-model
  "gets the page model at the given path"
  {:added "4.1"}
  [node space-id path]
  (var [group model] (page-core/model-ensure node
                                             space-id
                                             (xt/x:first path)
                                             (xt/x:second path)))
  (return model))

(def.js TYPES
  {:input    [event-model/get-input  "current" "input"]
   :output   [event-model/get-output "current" "output"]
   :pending  [event-model/get-output nil "output"]
   :elapsed  [event-model/get-output nil "output"]
   :disabled [event-model/get-output nil "output"]
   :errored  [event-model/get-output nil "output"]
   :tag      [event-model/get-output nil "output"]
   :success  [event-model/get-success nil "output"]})

(defn.js throttled-setter
  "creates a throttled setter which only updates after a delay"
  {:added "4.1"}
  [setResult delay]
  (var throttle {:val     nil
                 :thread  nil
                 :mounted true})
  (var throttled-fn
       (fn [result]
         (var t (xt/x:now-ms))
         (cond (k/not-nil? (xt/x:get-key throttle "thread"))
               (xt/x:set-key throttle "val" result)

               :else
               (do (xt/x:set-key throttle "val" result)
                   (setResult result)
                   (xt/x:set-key throttle "thread"
                                 (promise/x:with-delay delay
                                                       (fn []
                                                         (when (and (not= (xt/x:get-key throttle "val")
                                                                          result)
                                                                    (xt/x:get-key throttle "mounted"))
                                                           (setResult (xt/x:get-key throttle "val")))
                                                         (xt/x:del-key throttle "thread"))))))))
  (return [throttled-fn throttle]))

(defn.js initModelBase
  "initialises a keyed listener on the substrate node for a page model"
  {:added "4.1"}
  [node space-id path
   #{setResult
     getResult
     resultRef
     resultTag
     meta
     pred}]
  (var key (-/model-key space-id path))
  (var #{resultFn
         resultPrint} (or meta {}))
  (r/init []
    (var listener-id (r/id))
    (event-common/add-keyed-listener
     node
     key
     listener-id
     "page"
     (fn [_id data _t _meta]
       (var event (xtd/obj-clone data))
       (var nresult (getResult))
       (when (and (or (k/nil? resultTag)
                      (== resultTag (. event ["data"] ["tag"])))
                  (not (xtt/eq-nested (r/curr resultRef)
                                      nresult)))
         (setResult nresult))
       (when resultFn
         (resultFn event))
       (when (k/is-function? resultPrint)
         (resultPrint #{resultTag nresult event})))
     meta
     pred)
    (return
     (fn:> (event-common/remove-keyed-listener node key listener-id)))))

(defn.js listenModel
  "listens to a single field of a substrate page model"
  {:added "4.1"}
  [node space-id path type meta]
  (var [tfn tkey tevent] (xt/x:get-key -/TYPES type))
  (:= tevent (or tevent type))
  (var getResult (fn []
                   (var model (-/get-model node space-id path))
                   (var out (tfn model))
                   (return (xtd/clone-shallow
                            (:? tkey (. out [tkey]) out)))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (-/initModelBase node space-id path
                   #{setResult
                     getResult
                     resultRef
                     meta
                     {:pred (fn [event]
                              (return (== (. event ["type"])
                                          (+ "model." tevent))))}})
  (return result))

(defn.js listenModelOutput
  "listens to the full output record of a substrate page model"
  {:added "4.1"}
  [node space-id path types meta]
  (var getOutput (fn:> (xtd/obj-clone (event-model/get-output (-/get-model node space-id path) nil))))
  (var [output setOutput] (r/local getOutput))
  (var wrap (r/useIsMountedWrap))
  (var outputRef (r/useFollowRef output))
  (var pred
       (fn [event]
         (return
          (xtd/arr-some
           types
           (fn [type]
             (return (== (. event ["type"])
                         (+ "model." type))))))))
  (-/initModelBase node space-id path
                   #{meta pred
                     {:setResult (wrap setOutput)
                      :getResult getOutput
                      :resultRef outputRef}})
  (return output))

(defn.js listenModelThrottled
  "listens to the success output, throttled"
  {:added "4.1"}
  [node space-id path delay meta]
  (var getResult (fn:> (xtd/clone-shallow
                        (event-model/get-success (-/get-model node space-id path) nil))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (r/init []
    (var listener-id (r/id))
    (var [setThrottled throttle] (-/throttled-setter setResult delay))
    (var key (-/model-key space-id path))
    (event-common/add-keyed-listener
     node
     key
     listener-id
     "page"
     (fn [_id data _t _meta]
       (var nresult (getResult))
       (when (not (== (r/curr resultRef)
                      nresult))
         (setThrottled nresult)))
     meta
     (fn [event]
       (return (== "model.output"
                   (. event ["type"])))))
    (return
     (fn []
       (xt/x:set-key throttle "mounted" false)
       (event-common/remove-keyed-listener node key listener-id))))
  (return result))

(defn.js refreshArgsFn
  "sets the model input from args"
  {:added "4.1"}
  [node space-id path args opts]
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (cond (xtd/arr-every args k/not-nil?)
        (return (page-core/model-set-input node
                                           space-id
                                           group-id
                                           model-id
                                           {:data args}
                                           (or opts {})))

        :else
        (return (page-core/model-set-input node
                                           space-id
                                           group-id
                                           model-id
                                           nil
                                           (or opts {})))))

(defn.js useRefreshArgs
  "refreshes page model input when args change"
  {:added "4.1"}
  [node space-id path args opts]
  (:= opts (or opts {}))
  (r/watch [(xt/x:json-encode args)]
    (return
     (-/refreshArgsFn node space-id path args opts))))

(defn.js refreshModel
  "refreshes a substrate page model"
  {:added "4.1"}
  [node space-id path event]
  (return (page-core/refresh-model node
                                   space-id
                                   (xt/x:first path)
                                   (xt/x:second path)
                                   (or event {})
                                   nil)))

(defn.js remoteCall
  "invokes the remote stage of a page model"
  {:added "4.1"}
  [node space-id path args save-output]
  (return (page-core/remote-call node
                                 space-id
                                 (xt/x:first path)
                                 (xt/x:second path)
                                 args
                                 save-output)))

(defn.js listenSuccess
  "listens to successful output and keeps args in sync"
  {:added "4.1"}
  [node space-id path args opts meta]
  (:= opts (or opts {}))
  (var output (r/useStablized (-/listenModel node space-id path "success" meta)
                              (. opts stablized)))
  (-/useRefreshArgs node space-id path args opts)
  (return ((or (. opts then)
               k/identity)
           (or output (. opts default)))))
