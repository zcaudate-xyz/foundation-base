(ns std.lang.base.script-control
  (:require [std.json :as json]
            [std.lang.base.book-entry :as e]
            [std.lang.base.emit :as emit]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.registry :as reg]
            [std.lang.base.runtime :as rt]
            [std.lang.base.util :as ut]
            [std.lib.collection]
            [std.lib.context.pointer]
            [std.lib.context.registry]
            [std.lib.context.space]
            [std.lib.env])
  (:refer-clojure :exclude [test]))

(def +watch-keys+ [:context :lang :module :layout])

;;
;; Runtime
;;

(defn script-rt-get
  "gets the current runtime
 
   (script-rt-get :lua :default {})
   => rt/rt-default?
 
   (h/p:space-context-list)
   => (contains '[:lang/lua])
 
   (h/p:registry-rt-list :lang/lua)
   => (contains '(:default))
 
   
   (do (script-rt-stop :lua)
       (h/p:space-rt-active))
   => []"
  {:added "4.0"}
  ([lang key config]
   #_(std.lib.env/prn lang key config)
   (let [_ (if-let [ns (get @reg/+registry+ [lang key])] (require ns))
         sp          (std.lib.context.space/space std.lib.context.space/*namespace*)
         ctx         (ut/lang-context lang)
         placement   (get @(:state sp) ctx)
         started?    (boolean (:instance placement))
         
         new-config  (merge (:config placement)
                            (-> (std.lib.context.registry/registry-get ctx)
                                (get-in [:rt key :config]))
                            config)

         changed?    (or (not= new-config 
                               (:config placement))
                         (not= (:key placement) key))
         _  (when (and started? changed?)
              (std.lib.context.space/space:rt-stop sp ctx))
         _  (if (or (empty? placement) changed?)
              (std.lib.context.space/space:context-set sp ctx key config))
         rt (std.lib.context.space/space:rt-start sp ctx)]
     rt)))

(defn script-rt-stop
  "stops the current runtime"
  {:added "4.0"}
  ([]
   (std.lib.collection/map-juxt [identity script-rt-stop] (ut/lang-rt-list)))
  ([lang]
   (script-rt-stop lang nil))
  ([lang ns]
   (let [ctx          (ut/lang-context lang)
         space        (std.lib.context.space/space-resolve ns)
         placement    (std.lib.context.space/space:context-get space ctx)
         started? (:instance placement)
         _  (if started?
              (std.lib.context.space/space:rt-stop space ctx))]
     started?)))

(defn script-rt-restart
  "restarts a given runtime"
  {:added "4.0"}
  ([]
   (std.lib.collection/map-juxt [identity script-rt-restart] (ut/lang-rt-list)))
  ([lang]
   (script-rt-restart lang nil))
  ([lang ns]
   (let [ctx        (ut/lang-context lang)
         space      (std.lib.context.space/space-resolve ns)
         placement  (std.lib.context.space/space:context-get space ctx)
         started?   (:instance placement)
         _  (if started?
              (std.lib.context.space/space:rt-stop space ctx))
         rt (std.lib.context.space/space:rt-start space ctx)]
     rt)))

(defn script-rt-oneshot-eval
  "oneshot evals a statement"
  {:added "4.0"}
  [default lang args]
  (let [has-rt (get (set (ut/lang-rt-list))
                    lang)
        rt  (if has-rt
              (ut/lang-rt lang)
              (script-rt-get lang
                             default
                             {}))
        out  (std.lib.context.pointer/rt-invoke-ptr rt (ut/lang-pointer lang {:module (:module rt)})
                                args)
        _    (when (not has-rt)
               (script-rt-stop lang)
               (std.lib.context.space/space:context-unset (ut/lang-context lang)))]
    out))

(defn script-rt-oneshot
  "for use with the defmacro.! function"
  {:added "4.0"}
  [default {:keys [lang] :as ptr} args]
  (script-rt-oneshot-eval default lang [(cons ptr args)]))
