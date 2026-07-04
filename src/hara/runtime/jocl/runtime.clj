(ns hara.runtime.jocl.runtime
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [hara.lang.runtime :as default]
            [hara.lang.pointer :as ptr]
            [hara.common.util :as ut]
            [std.lib.atom :as atom]
            [std.lib.collection :as coll]
            [std.lib.component :as component]
            [std.lib.context.pointer :as cptr]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.security :as security]
            [hara.runtime.jocl.exec :as exec]))

(defn kernel?
  "check that a code entry 
 
   (kernel? @sample)
   => true"
  {:added "3.0"}
  ([entry]
   (boolean (:rt/kernel entry))))

(defn init-exec-jocl
  "initialises the exec in the runtime"
  {:added "3.0"}
  ([{:keys [state]} {:keys [lang] :as ptr} entry]
   (let [id    (ut/sym-full ptr)
         [code name]  (exec/exec-source  ptr) ;; THIS NEEDS TO BE FIXED
         sha   (security/sha1 code)
         exec  (get @state id)
         {:keys [worksize args]} (:rt/kernel entry)
         worksize (if (coll/form? worksize)
                    (eval worksize)
                    worksize)
         [exec new?]  (if-not exec
                        [(-> (exec/exec {:source ptr
                                         :args args
                                         :worksize worksize})
                             (component/start))
                         true]
                        (if (= sha (:sha @(:state exec)))
                          [exec false]
                          (do (component/stop exec)
                              [(-> (exec/exec {:source ptr
                                               :args args
                                               :worksize worksize})
                                   (component/start))
                               true])))
         _ (if new?
             (swap! state assoc id exec))]
     exec)))

(defn init-ptr-jocl
  "initialises the pointer"
  {:added "3.0"}
  ([rt ptr]
   (let [[ptr entry] (if (cptr/pointer? ptr)
                       [ptr (ptr/get-entry ptr)
                        (ut/lang-pointer (:lang ptr)
                                         (select-keys ptr [:id :module :section]))
                        ptr])]
     (if (kernel? entry)
       (init-exec-jocl rt ptr entry)))))

(defn invoke-ptr-jocl
  "invokes a jocl ptr (cached kernel)"
  {:added "3.0"}
  ([{:keys [state] :as rt} ptr args]
   (let [entry (ptr/get-entry ptr)]
     (if (kernel? entry)
       (let [f (or (get @state (ut/sym-full ptr))
                   (do (init-ptr-jocl rt ptr)
                       (get @state (ut/sym-full ptr)))
                   (h/error "Function cannot be initialised" {:pointer ptr}))]
         (apply f args))
       entry))))

(defn stop-jocl
  "stops the runtime"
  {:added "3.0"}
  ([{:keys [state] :as rt}]
   (coll/map-vals component/stop (atom/swap-return! state
                        (fn [m] [m nil])))
   
   rt))

(defn- rt-jocl-string [{:keys [lang]}]
  (str "#rt:jocl " [lang]))

(defimpl JoclRuntime [id state]
  :string rt-jocl-string
  :protocols [protocol.context/IContext
              :prefix "default/default-"
              :body {-raw-eval string}
              :method {-invoke-ptr invoke-ptr-jocl
                       -init-ptr init-ptr-jocl}
              protocol.component/IComponent
              :suffix "-jocl"
              :exclude [-kill]
              :body {-start component}])

(defn jocl:create
  "creates a new runtime"
  {:added "3.0"}
  [{:keys [id] :as m
    :or {id  (h/sid)}}]
  (map->JoclRuntime (assoc m
                           :id id
                           :state (atom nil))))

(defn jocl
  "create and starts the runtime"
  {:added "3.0"}
  ([]
   (jocl {}))
  ([m]
   (-> (jocl:create m)
       (component/start))))

(def +init+
  (default/install-type!
   :c :jocl
   {:type :hara/lib.jocl
    :config {:bootstrap false}
    :instance {:create jocl:create}}))
