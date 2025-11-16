(ns xt.lang.event-animate
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt new-derived
  "creates a new derived value"
  {:added "4.0"}
  [impl f arr]
  (var #{create-val
         add-listener
         set-value
         get-value} impl)
  (var thunk-fn (fn:> [] (f (k/unpack (k/arr-map arr get-value)))))
  (var derived  (create-val (thunk-fn)))
  (k/for:array [v arr]
    (add-listener
     v
     (fn [v]
       (var nval (thunk-fn))
       (when (not= nval (get-value derived))
         (set-value derived nval)))))
  (return derived))

(defn.xt listen-single
  "listens to a single observer"
  {:added "4.0"}
  [impl
   ref
   ind
   f]
  (:= f (or f (fn:> {})))
  (var #{add-listener
         set-props
         get-value} impl)
  (var trigger-fn
       (fn []
         (var props (f (get-value ind)))
         (when (and ref (. ref ["current"]))
           (set-props (. ref ["current"]) props))
         (return props)))
  (add-listener ind trigger-fn)
  (return (trigger-fn)))

(defn.xt listen-array
  "listens to array for changes"
  {:added "4.0"}
  [impl
   ref
   arr
   f]
  (:= f (or f (fn:> {})))
  (var #{add-listener
         set-props
         get-value} impl)
  (var trigger-fn
       (fn []
         (let [props (f (k/unpack (k/arr-map arr get-value)))]
           (when (and ref (. ref ["current"]))
             (set-props (. ref ["current"]) props))
           (return props))))
  (k/for:array [ind arr]
    (add-listener ind trigger-fn))
  (return (trigger-fn)))

(defn.xt get-map-paths
  "gets map paths"
  {:added "4.0"}
  [impl
   m
   parent
   all]
  (:= parent (or parent []))
  (:= all (or all []))
  (var #{is-animated} impl)
  (k/for:object [[k x] m]
    (cond (is-animated x)
          (x:arr-push all [[(k/unpack parent)] k x])
          
          (k/obj? x)
          (do (x:arr-push all [[(k/unpack parent)] k false])
              (var nparent [(k/unpack parent)])
              (x:arr-push nparent k)
              (-/get-map-paths impl x nparent all))))
  (return all))

(defn.xt get-map-input
  "gets the map input"
  {:added "4.0"}
  [impl paths]
  (var #{get-value} impl)
  (var out {})
  (k/for:array [e paths]
    (var [path key v] e)
    (var val (:? (not v) {} (get-value v)))
    (cond (k/is-empty? path)
          (k/set-key out key val)
          
          :else
          (k/set-key (k/get-in out path)
                     key val)))
  (return out))

(defn.xt listen-map
  "listens to a map of indicators"
  {:added "4.0"}
  [impl ref m f]
  (:= f (or f (fn:> {})))
  (var #{add-listener
         set-props} impl)
  (var paths      (-/get-map-paths impl m))
  (var animated   (k/arr-keepf paths
                               k/last
                               k/last))
  (var trigger-fn
       (fn []
         (var input (-/get-map-input impl paths))
         (var props (f input))
         (when (and ref (. ref ["current"]))
           (set-props (. ref ["current"])  props))
         (return props)))
  (k/for:array [ind animated]
    (add-listener ind trigger-fn))
  (return (trigger-fn)))

(defn.xt listen-transformations
  "converts to the necessary listeners"
  {:added "4.0"}
  [impl ref indicators transformations get-chord]
  (var #{is-animated
         set-props} impl)
  (cond (k/nil? transformations)
        (return {})
        
        (k/fn? transformations)
        (cond (k/nil? indicators)
              (return)
              
              (is-animated indicators)
              (return (-/listen-single impl ref indicators
                                       (fn [v]
                                         (return (transformations v (get-chord))))))

              :else
              (return (-/listen-map impl ref indicators (fn [m]
                                                          (return (transformations m (get-chord)))))))
        
        :else
        (do (var out {})
            (k/for:object [[key subtransformations] transformations]
              (var subindicators (. indicators [key]))
              (var subout (-/listen-transformations impl ref subindicators subtransformations get-chord))
              (k/set-key out key subout))
            (return out))))

(defn.xt new-progressing
  "creates a new progressing element"
  {:added "4.0"}
  ([]
   (return {:running   false
            :animation nil
            :queued []})))


(defn.xt run-with-cancel
  "runs with cancel"
  {:added "4.0"}
  [impl animate-fn progressing progress-fn]
  (var #{running
         animation} progressing)
  (var #{stop-transition} impl)
  (when (and running
             animation)
    (stop-transition animation)
    (k/obj-assign progressing {:running false
                               :animation nil}))
  (var anim (animate-fn (fn [finished]
                          (k/obj-assign progressing {:running false
                                                     :animation nil})
                         (when progress-fn
                           (progress-fn {:status "stopped"
                                         :finished finished})))))
  (k/obj-assign progressing {:animation anim})
  (when progress-fn
    (progress-fn {:status "running"}))
  (return progressing))

;;
;; Chained
;;

(defn.xt animate-chained-cleanup
  "runs with cleanup"
  {:added "4.0"}
  [impl progressing progress-fn]
  (var out (k/obj-assign progressing (-/new-progressing)))
  (when progress-fn
    (progress-fn {:status "cleanup"}))
  (return out))

(defn.xt animate-chained-one
  "runs with single chain"
  {:added "4.0"}
  [impl progressing progress-fn]
  (var #{queued}  progressing)
  (var queued-fn  (k/first queued))
  (when (not queued-fn)
    (return (-/animate-chained-cleanup impl
                                       progressing
                                       progress-fn)))
  
  (var anim (queued-fn (fn:> (-/animate-chained-cleanup impl
                                                        progressing
                                                        progress-fn))))
  (k/obj-assign progressing {:running  true
                             :animation anim})
  (when progress-fn
    (progress-fn {:status "running"}))
  (return progressing))

(defn.xt animate-chained-all
  "runs with all chained"
  {:added "4.0"}
  [impl progressing progress-fn]
  (var #{queued}  progressing)
  
  (when (== 0 (k/len queued))
    (return (-/animate-chained-cleanup impl progressing progress-fn)))
  (var queued-fn (k/first queued))
  (x:arr-pop-first queued)
  (when queued-fn
    (var anim (queued-fn (fn:> [res]
                           (-/animate-chained-all impl progressing progress-fn))))
    (when anim
      (k/obj-assign progressing {:running  true
                                 :animation anim})
      (when progress-fn
        (progress-fn {:status "running"}))))
  (return progressing))

(defn.xt run-with-chained
  "runs chained"
  {:added "4.0"}
  [impl type animate-fn progressing progress-fn]
  (var callback-fn
       (:? (== type "chained-one")
           (fn []
             (return (-/animate-chained-one impl progressing progress-fn)))
           :else
           (fn []
             (return (-/animate-chained-all impl progressing progress-fn)))))
  (var #{running queued} progressing)
  (cond (not running)
        (do (var anim (animate-fn callback-fn))
            (when anim
              (k/obj-assign progressing {:running  true
                                         :animation anim})))
        
        (and (== type "chained-one")
             (k/first queued))
        (do)
        
        :else
        (x:arr-push queued animate-fn))
  
  (return progressing))

(defn.xt run-with
  "runs effects"
  {:added "4.0"}
  [impl type animate-fn progressing progress-fn]
  (cond (== type "cancel")
        (return (-/run-with-cancel impl animate-fn progressing progress-fn))

        (or (== type "chained-one")
            (== type "chained-all"))
        (return (-/run-with-chained impl type animate-fn progressing progress-fn))

        :else
        (k/err (+ "Not a valid type: " type))))

;;
;; 
;;

(defn.xt make-binary-transitions
  "makes a binary transition"
  {:added "4.0"}
  [impl
   initial
   tparams]
  (:= tparams (or tparams {}))
  (var #{create-val
         create-transition} impl)
  (var indicator    (create-val (:? initial 1 0)))
  (var zero-fn      (create-transition indicator
                                       tparams
                                       [1 0]
                                       k/identity))
  (var one-fn       (create-transition indicator
                                       tparams
                                       [0 1]
                                       k/identity))
  (var #{check} tparams)
  (return {:indicator indicator
           :zero-fn zero-fn
           :one-fn one-fn
           :check-fn (or check k/identity)}))

(defn.xt make-binary-indicator
  "makes a binary indicator"
  {:added "4.0"}
  [impl
   initial
   tparams
   type
   progressing
   progress-fn]
  (:= tparams (or tparams {}))
  (var #{indicator
         zero-fn
         one-fn
         check-fn}  (-/make-binary-transitions
                     impl
                     initial
                     tparams))
  (return
   {:indicator indicator
    :trigger-fn
    (fn [flag]
      (when progress-fn
        (progress-fn {:status "started"}))
      (if (check-fn flag)
        (return (-/run-with impl type one-fn progressing progress-fn))
        (return (-/run-with impl type zero-fn progressing progress-fn))))}))

(defn.xt make-linear-indicator
  "makes a linear indicator"
  {:added "4.0"}
  [impl
   initial
   get-prev
   set-prev
   tparams
   type
   progressing
   progress-fn
   check-fn]
  (var #{create-val
         create-transition} impl)
  (var indicator (create-val initial))
  (return
   {:indicator indicator
    :trigger-fn
    (fn [value]
      (when (or (k/nil? check-fn)
                (check-fn value))
        (var t-fn (create-transition indicator
                                     tparams
                                     [(get-prev) value]
                                     k/identity))
        (when progress-fn
          (progress-fn {:status "started"}))
        (var out (-/run-with impl type t-fn progressing progress-fn))
        (set-prev value)
        (return out)))}))

(defn.xt make-circular-indicator
  "makes a circular indicator"
  {:added "4.0"}
  [impl
   initial
   get-prev
   set-prev
   tparams
   type
   modulo
   progressing
   progress-fn
   check-fn]
  (var #{create-val
         create-transition} impl)
  (var indicator (create-val initial))
  (return
   {:indicator indicator
    :trigger-fn
    (fn [value]
      (when (or (k/nil? check-fn)
                (check-fn value))
        (var pval (get-prev))
        (var offset (k/mod-offset pval value (or modulo 360)))
        (var nval (+ pval offset))
        (var t-fn (create-transition indicator
                                     tparams
                                     [pval nval]
                                     k/identity))
        (when progress-fn
          (progress-fn {:status "started"}))
        (var out (-/run-with impl type t-fn progressing progress-fn))
        (set-prev value)
        (return out)))}))

