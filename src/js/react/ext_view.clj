(ns js.react.ext-view
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-view :as event-view]
             [js.react :as r :include [:fn]]
             [js.core :as j]]
   :export [MODULE]})

(defn.js throttled-setter
  "creates a throttled setter which only updates after a delay"
  {:added "4.0"}
  [setResult delay]
  (var throttle {:val    nil
                 :thread nil
                 :mounted true})
  (var throttled-fn
       (fn [result]
         (var t (k/now-ms))
         (cond (k/not-nil? (k/get-key throttle "thread"))
               (k/set-key throttle "val" result)

               :else
               (do (k/set-key throttle "val" result)
                   (setResult result)
                   (k/set-key throttle "thread"
                              (j/future-delayed [delay]
                                (when (and (not= (k/get-key throttle "val")
                                                 result)
                                           (k/get-key throttle "mounted"))
                                  (setResult (k/get-key throttle "val")))
                                (k/del-key throttle "thread")))))))
  (return [throttled-fn throttle]))

(defn.js refresh-view
  "refreshes the view"
  {:added "4.0"}
  [view opts]
  (var [context disabled] (event-view/pipeline-prep view opts))
  (var #{acc} context)
  (return (. (event-view/pipeline-run
              context
              disabled
              (j/asyncFn)
              nil
              k/identity)
             (then (fn []
                     (return acc))))))

(defn.js refresh-args
  "refreshes the view view args"
  {:added "4.0"}
  [view args opts]
  
  (event-view/set-input view {:data args})
  (return (-/refresh-view view opts)))

(defn.js refresh-view-remote
  "refreshes view using remote function"
  {:added "4.0"}
  [view save-output opts]
  (when (k/get-in view ["pipeline" "remote" "handler"])
    (var [context disabled] (event-view/pipeline-prep view opts))
    (var #{acc} context)
    
    (return (. (event-view/pipeline-run-remote
                context
                save-output
                (j/asyncFn)
                nil
                k/identity)
               (then (fn:> acc))))))

(defn.js refresh-args-remote
  "refreshes view using remote function with new args"
  {:added "4.0"}
  [view args save-output opts]
  (event-view/set-input view {:data args})
  (return (-/refresh-view-remote view save-output opts)))

(defn.js refresh-view-sync
  "refreshes view using sync function"
  {:added "4.0"}
  [view save-output opts]
  (when (k/get-in view ["pipeline" "sync" "handler"])
    (var [context disabled] (event-view/pipeline-prep view opts))
    (var #{acc} context)
    (return (. (event-view/pipeline-run-sync
                context
                save-output
                (j/asyncFn)
                nil
                k/identity)
               (then (fn:> acc))))))

(defn.js refresh-args-sync
  "refreshes view using args function"
  {:added "4.0"}
  [view args save-output opts]
  (event-view/set-input view {:data args})
  (return (-/refresh-view-sync view save-output opts)))

(defn.js make-view
  "makes and initialises view"
  {:added "4.0"}
  [main-handler
   pipeline
   default-args
   default-output
   default-process
   options]
  (var view (event-view/create-view
             main-handler
             pipeline
             default-args
             default-output
             default-process
             options))
  (event-view/init-view view)
  (k/set-key view "init" (-/refresh-view view))
  (return view))

(defn.js makeViewRaw
  "makes a react compatible view without r/const"
  {:added "4.0"}
  [#{handler
     pipeline
     defaultArgs
     defaultOutput
     defaultProcess
     options}]
  (return
   (-/make-view handler
                (or pipeline {})
                defaultArgs
                defaultOutput
                defaultProcess
                options)))

(defn.js makeView
  "makes a react compatible view"
  {:added "4.0"}
  [#{handler
     pipeline
     defaultArgs
     defaultOutput
     defaultProcess
     options}]
  (return
   (r/const (-/makeViewRaw #{handler
                             pipeline
                             defaultArgs
                             defaultOutput
                             defaultProcess
                             options}))))

(def.js TYPES
  {:input    [event-view/get-input  "current"]
   :output   [event-view/get-output "current"]
   :pending  [event-view/get-output "pending"]
   :elapsed  [event-view/get-output "elapsed"]
   :disabled [event-view/get-output "disabled"]
   :success  [event-view/get-success nil "output"]})

(defn.js initViewBase
  "initialises the view listener"
  {:added "4.0"}
  [view dest-key #{setResult
                   getResult
                   resultRef
                   resultTag
                   meta
                   pred}]
  (var #{resultFn
         resultPrint} (or meta {}))
  (r/init []
    (var listener-id (j/randomId 4))
    (event-view/add-listener
     view
     listener-id
     (fn [event]
       (var nresult (getResult))
       (when (and (or (k/nil? resultTag)
                      (== resultTag (. event data tag)))
                  (or (not= "view.output"
                            (. event type))
                      (== (. event data type)
                          (or dest-key "output")))
                  (not (k/eq-nested (r/curr resultRef)
                                    nresult)))
         #_(when (== "pending" (. event type))
           (k/LOG! event))
         (setResult nresult))
       (when resultFn
         (resultFn event))
       (when (k/fn? resultPrint)
         (resultPrint #{resultTag nresult event})))
     meta
     pred)
    (return
     (fn:> (event-view/remove-listener view listener-id)))))

(defn.js listenView
  "creates the most basic views"
  {:added "4.0"}
  [view type meta dest-key tag-key]
  (var [tfn tkey tevent] (k/get-key -/TYPES type))
  (:= tevent (or tevent type))
  (var getResult (fn []
                   (var out (tfn view))
                   (return (k/clone-shallow
                            (:? tkey (. out [tkey]) out)))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (-/initViewBase view
                  dest-key
                  #{setResult
                    getResult
                    resultRef
                    
                    meta
                    {:resultTag tag-key
                     :resultFn  (k/get-in meta "resultFn")
                     :pred (fn [event]
                             (return (== (. event ["type"])
                                         (+ "view." tevent))))}})
  (return result))

(defn.js listenViewOutput
  "creates listeners on the output"
  {:added "4.0"}
  [view types meta dest-key tag-key]
  (var getOutput (fn:> (k/obj-clone (event-view/get-output view dest-key))))
  (var [output setOutput] (r/local getOutput))
  (var wrap (r/useIsMountedWrap))
  (var outputRef (r/useFollowRef output))
  (var pred
       (fn [event]
         (return
          (k/arr-some
           types
           (fn [type]
             (return (== (. event ["type"])
                         (+ "view." type))))))))
  (-/initViewBase view
                  dest-key
                  #{meta pred  
                    {:setResult (wrap setOutput)
                     :getResult getOutput
                     :resultRef outputRef
                     :resultTag tag-key
                     :resultFn  (k/get-in meta "resultFn")}})
  (return output))

(defn.js listenViewThrottled
  "creates the throttled listener"
  {:added "4.0"}
  [view delay meta dest-key]
  (var getResult (fn:> (k/clone-shallow
                        (event-view/get-success view))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (r/init []
    (var listener-id (j/randomId 4))
    (var [setThrottled throttle] (-/throttled-setter setResult delay))
    (event-view/add-listener
     view
     listener-id
     (fn []
       (var nresult (getResult))
       (when (not (== (r/curr resultRef)
                      nresult))
         (setThrottled nresult)))
     meta
     (fn [event]
       (return (== "view.output"
                   (. event ["type"])))))
    (return
     (fn []
       (k/set-key throttle "mounted" false)
       (event-view/remove-listener view listener-id))))
  (return result))

(defn.js wrap-pending
  "wraps function, setting pending flag"
  {:added "4.0"}
  [f with-pending]
  (if with-pending
    (return f)
    (return
     (fn [view ...args]
       (event-view/set-pending view true)
       (return
        (. (j/future (return (f view ...args)))
           (then (fn [res]
                   (event-view/set-pending view false)
                   (return res)))))))))

(defn.js refreshArgsFn
  [view args opts]
  (cond (k/arr-every args k/not-nil?)
        (return
         (. (-/refresh-args view args opts)
            (then
             (fn [acc]
               (var [ok data] (k/get-in acc ["main"]))
               (when (not ok) (throw data))
               (cond (== (. opts remote) "always")
                     (do (return
                          ((-/wrap-pending -/refresh-args-remote
                                           (. opts with-pending))
                           view args true opts)))
                     
                     (== (. opts remote) "none")
                     (return nil)
                     
                     :else
                     (when (or (k/nil? (. opts remote-check))
                               (. opts (remote-check args)))
                       (if (k/not-empty? data)
                         (return (-/refresh-args-sync view args false opts))
                         (return (-/refresh-args-remote view args true opts)))))))))

        :else
        (return (fn:> (event-view/set-output view nil)))))

(defn.js useRefreshArgs
  "refreshes args on the view"
  {:added "4.0"}
  [view args opts]
  (:= opts (or opts {}))
  (r/watch [(k/json-encode args)]
    (return
     (-/refreshArgsFn view args opts))))

(defn.js listenSuccess
  "listens to the successful output"
  {:added "4.0"}
  [view args opts meta tag-key]
  (:= opts (or opts {}))
  (var output (r/useStablized (-/listenView view "success" meta (. opts dest) tag-key)
                              (. opts stablized)))
  (-/useRefreshArgs view args opts)
  (return ((or (. opts then)
               k/identity)
           (or output (. opts default)))))

;;
;; HELPERS
;;

(defn.js handler-base
  "constructs a base handler"
  {:added "0.1"}
  [handler m]
  (return
   (k/obj-assign-nested
    {:handler handler
     :defaultArgs []
     :defaultInit {:disabled true}}
    m)))

(defn.js oneshot-fn
  "creates a oneshot function"
  {:added "0.1"}
  []
  (var v true)
  (return (fn [ctx]
            (when v
              (:= v false)
              (return true))
            (return false))))

(defn.js input-disabled?
  "checks if input has been disabled (context method)"
  {:added "0.1"}
  [#{input}]
  (return (or (k/nil? input)
              (. input ["disabled"]))))

(defn.js input-data
  "gets the input data (context method)"
  {:added "0.1"}
  [#{input}]
  (return (and input (. input ["data"]))))

(defn.js input-data-nil?
  "ensures that disabled flag or a nil input returns true"
  {:added "0.1"}
  [#{input}]
  (return (or (k/nil? input)
              (. input ["disabled"])
              (k/nil? (. input ["data"])))))

(defn.js output-empty?
  "checks that view is empty (context method)"
  {:added "0.1"}
  [#{view}]
  (return (k/is-empty? (event-view/get-current view))))

(def.js MODULE (!:module))

(comment
  
  (defn.js listenViewInit
    [view tags meta]
    (var initStart   (r/ref false))
    (var initTag     (r/ref false))
    (var [init setInit] (r/local false))
    (var out (-/listenViewOutput view
                                 ["pending" "output" "elapsed"]
                                 meta))
    (r/watch [out]
      (when (and (k/not-empty? (. out current))
                 (not init))
        (setInit true))
      (when (. out pending)
        (r/curr:set initStart true))
      (when (or (k/nil? tags)
                (. tags [(. out tag)]))
        (r/curr:set initTag true))
      (when (and (. out elapsed)
                 (r/curr initStart)
                 (r/curr initTag)
                 (not init))
        (setInit true)))
    (return init)))


