(ns std.dispatch.core
  (:require [std.protocol.dispatch :as protocol.dispatch]
            [std.protocol.component :as protocol.component]
            [std.concurrent :as cc]
            [std.dispatch.common :as common]
            [std.dispatch.hooks :as hooks]
            [std.lib :as h :refer [defimpl]]))

(defn submit-dispatch
  "submits to the core dispatch"
  {:added "3.0"}
  ([{:keys [runtime handler] :as dispatch} entry]
   (let [_   (hooks/on-submit dispatch entry)
         task-fn (common/handle-fn dispatch entry)
         executor  @(:executor runtime)]
     (if executor
       (try (hooks/on-queued dispatch entry)
            (h/future:run task-fn {:pool executor})
            (catch Throwable t
              (hooks/on-error dispatch entry t)))
       (h/error  "No executor dispatch present")))))

(defimpl CoreDispatch [type runtime handler]
  :prefix "common/"
  :suffix "-dispatch"
  :string common/to-string
  :protocols  [std.protocol.component/IComponent
               protocol.component/IComponentProps
               protocol.component/IComponentQuery
               :body    {-track-path [:dispatch :core]}
               protocol.dispatch/IDispatch
               :method  {-submit submit-dispatch}
               :body    {-bulk?  false}]
  :interfaces [clojure.lang.IFn
               :method  {invoke {[dispatch entry] submit-dispatch}}])

(def create-dispatch-typecheck identity
  #_(types/<dispatch:core> :strict))

(defn create-dispatch
  "creates a core dispatch
 
   ;; POOL SIZE 2, NO SLEEP
   (test-scaffold +test-config+ 100 10)
   => #(-> % count (= 100))
 
   ;; POOL SIZE 2, WITH SLEEP
   (test-scaffold (assoc +test-config+
                         :handler (fn [_ _] (Thread/sleep 200)))
                  5
                  100)
   => #(-> % count (= 2))
 
   ;; POOL SIZE 50, WITH SLEEP
   (test-scaffold (-> +test-config+
                      (assoc :handler (fn [_ _] (Thread/sleep 200)))
                      (assoc-in [:options :pool :size] 50))
                  80
                  30)
   => #(-> % count (>= 50))"
  {:added "3.0"}
  ([m]
   (-> (assoc m :type :core)
        create-dispatch-typecheck
       (common/create-map)
       (map->CoreDispatch))))

(defmethod protocol.dispatch/-create :core
  ([m]
   (create-dispatch m)))
