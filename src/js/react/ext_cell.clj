(ns js.react.ext-cell
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.core :as j]
             [js.cell :as cl]
             [js.react.ext-view :as ext-view]]})

(def.js TYPES
  {:input    [cl/view-get-input  "current"]
   :output   [cl/view-get-output "current"]
   :pending  [cl/view-get-output]
   :elapsed  [cl/view-get-output]
   :disabled [cl/view-get-output]
   :success  [cl/view-success nil "output"]})

(defn.js initCellBase
  "initialises cell listeners"
  {:added "4.0"}
  [#{context
     path
     setResult
     getResult
     resultRef
     meta,
     pred}]
  (r/init []
    (var listener-id (j/randomId 4))
    (cl/add-listener
     path
     listener-id
     (fn []
       (var nresult (getResult))
       (when (not (== (r/curr resultRef) nresult))
         (setResult nresult)))
     meta
     pred
     context)
    (return
     (fn []
       (cl/remove-listener path listener-id context)))))

(defn.js listenCell
  "listen to parts of the cell"
  {:added "4.0"}
  [path type meta context]
  (var [tfn tkey tevent] (k/get-key -/TYPES type))
  (:= tevent (or tevent type))
  (var getResult (fn []
                   (var out (tfn path context))
                   (return (k/clone-shallow
                            (:? tkey (. out [tkey]) out)))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (-/initCellBase #{context
                    path
                    setResult
                    getResult
                    resultRef
                    meta
                    {:pred (fn:> [event] (== (. event ["type"]) (+ "view." tevent)))}})
  (return result))

(defn.js listenCellOutput
  "listens to the cell output"
  {:added "4.0"}
  [path types meta context]
  (var getOutput (fn:> (k/obj-clone
                        (or (cl/view-get-output path context)
                            {}))))
  
  (var [output setOutput] (r/local getOutput))
  (var outputRef (r/useFollowRef output))
  (var pred
       (fn [event]
         (return
          (k/arr-some
           types
           (fn [type]
             (return (== (. event ["type"])
                         (+ "view." type))))))))
  (-/initCellBase #{meta pred context path
                    {:setResult setOutput
                     :getResult getOutput
                     :resultRef outputRef}})
  (return output))

(defn.js listenCellThrottled
  "listens to the throttled output"
  {:added "4.0"}
  [path delay meta context]
  (var getResult (fn:> (k/clone-shallow
                        (cl/view-success path context))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (r/init []
    (var listener-id (j/randomId 4))
    (var [setThrottled throttle] (ext-view/throttled-setter setResult delay))
    (var nresult (getResult))
    (cl/add-listener
     path
     listener-id
     (fn []
       (var nresult (getResult))
       (when (not (== (r/curr resultRef) nresult))
         (setThrottled nresult)))
     meta
     (fn [event]
       (return (== "view.output"
                   (. event ["type"]))))
     context)
    (return
     (fn []
       (k/set-key throttle "mounted" false)
       (cl/remove-listener path listener-id context))))
  (return result))

(defn.js listenRawEvents
  "listens to the raw cell events"
  {:added "4.0"}
  [pred handler context]
  (var listener-id (r/id))
  (r/init []
    (cl/add-raw-callback listener-id
                         pred
                         handler
                         context)
    (return (fn:> (cl/remove-raw-callback listener-id context))))
  (return listener-id))

