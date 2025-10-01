(ns std.lang.base.script-control
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.registry :as reg]
            [std.lang.base.book-entry :as e]
            [std.lang.base.util :as ut]
            [std.lang.base.runtime :as rt]
            [std.string :as str]
            [std.json :as json]
            [std.lib :as h])
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
   #_(h/prn lang key config)
   (let [_ (if-let [ns (get @reg/+registry+ [lang key])] (require ns))
         sp          (h/p:space std.lib.context.space/*namespace*)
         ctx         (ut/lang-context lang)
         placement   (get @(:state sp) ctx)
         started?    (boolean (:instance placement))
         
         new-config  (merge (:config placement)
                            (-> (h/p:registry-get ctx)
                                (get-in [:rt key :config]))
                            config)

         changed?    (or (not= new-config 
                               (:config placement))
                         (not= (:key placement) key))
         _  (when (and started? changed?)
              (h/p:space-rt-stop sp ctx))
         _  (if (or (empty? placement) changed?)
              (h/p:space-context-set sp ctx key config))
         rt (h/p:space-rt-start sp ctx)]
     rt)))

(defn script-rt-stop
  "stops the current runtime"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity script-rt-stop] (ut/lang-rt-list)))
  ([lang]
   (script-rt-stop lang nil))
  ([lang ns]
   (let [ctx          (ut/lang-context lang)
         space        (h/p:space-resolve ns)
         placement    (h/p:space-context-get space ctx)
         started? (:instance placement)
         _  (if started?
              (h/p:space-rt-stop space ctx))]
     started?)))

(defn script-rt-restart
  "restarts a given runtime"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity script-rt-restart] (ut/lang-rt-list)))
  ([lang]
   (script-rt-restart lang nil))
  ([lang ns]
   (let [ctx        (ut/lang-context lang)
         space      (h/p:space-resolve ns)
         placement  (h/p:space-context-get space ctx)
         started?   (:instance placement)
         _  (if started?
              (h/p:space-rt-stop space ctx))
         rt (h/p:space-rt-start space ctx)]
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
        out  (h/p:rt-invoke-ptr rt (ut/lang-pointer lang {:module (:module rt)})
                                args)
        _    (when (not has-rt)
               (script-rt-stop lang)
               (h/p:space-context-unset (ut/lang-context lang)))]
    out))

(defn script-rt-oneshot
  "for use with the defmacro.! function"
  {:added "4.0"}
  [default {:keys [lang] :as ptr} args]
  (script-rt-oneshot-eval default lang [(cons ptr args)]))
