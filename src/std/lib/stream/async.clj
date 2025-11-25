(ns std.lib.stream.async
  (:require [std.protocol.stream :as protocol.stream]
            [std.lib.atom :as atom]
            [std.lib.future :as f]
            [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.generate :as gen]
            [std.lib.time :as time]
            [std.lib.stream :as stream]
            [std.lib.stream.iter :as i])
  (:refer-clojure :exclude [realized?])
  (:import (java.util.concurrent CompletableFuture
                                 CompletionStage
                                 BlockingQueue
                                 LinkedBlockingQueue)))

(defn blocking?
  "checks that object implements `IBLocking`
 
   (blocking? (q/queue))
   => true"
  {:added "3.0"}
  [obj]
  (satisfies? protocol.stream/IBlocking obj))

(defn take-element
  "takes an element from a queue"
  {:added "3.0"}
  ([queue]
   (protocol.stream/-take-element queue)))

(defn stage?
  "checks if object implements `IStage`
 
   (stage? (f/completed 1))
   => true"
  {:added "3.0"}
  [obj]
  (satisfies? protocol.stream/IStage obj))

(defn stage-unit
  "returns the base future unit
 
   (stage-unit (mono {:future |f|}))
   => |f|"
  {:added "3.0"}
  ([obj]
   (protocol.stream/-stage-unit obj)))

(defn stage-realize
  "returns the unwrapped result
 
   (stage-realize (f/future (Thread/sleep 10) 1))
   => 1"
  {:added "3.0"}
  ([obj]
   (protocol.stream/-stage-realize obj)))

(defn stage-realized?
  "checks if unwrapped result is realized
 
   (stage-realized? (mono {:future (f/completed (f/completed 1))}))
   => true"
  {:added "3.0"}
  ([obj]
   (protocol.stream/-stage-realized? obj)))

(defn blocking-seq
  "constructs a blocking seq"
  {:added "3.0"}
  ([queue]
   (gen/gen (loop []
              (let [e (try (take-element queue)
                           (catch Throwable t :queue/error))]
                (when-not (= e :queue/end)
                  (if (not= e :queue/error)
                    (gen/yield e))
                  (recur)))))))

(def +stream:async+
  '{java.util.concurrent.BlockingQueue
    {:produce blocking-seq :collect stream/collect-collection}
    java.util.concurrent.BlockingDeque
    {:produce blocking-seq :collect stream/collect-collection}})

(stream/extend-stream +stream:async+)

(extend-protocol protocol.stream/IBlocking
  BlockingQueue
  (-take-element [queue] (.take queue)))

(extend-protocol protocol.stream/IStage
  CompletableFuture
  (-stage-unit     [obj] obj)
  (-stage-realize   [obj] @obj)
  (-stage-realized? [obj] (f/future:complete? obj)))

(defn ipending?
  "true for promise and future
 
   (ipending? (promise)) => true"
  {:added "3.0"}
  ([obj]
   (instance? clojure.lang.IPending obj)))

(defn realize
  "retrieves linked future result
 
   (realize (f/future (f/future (f/future 3))))
   => 3"
  {:added "3.0"}
  ([obj]
   (cond (ipending? obj) @obj

         (stage? obj)
         (realize (stage-realize obj))

         :else
         obj)))

(defn realized?
  "checks if all linked futures are realized
 
   (realized? (f/completed (f/completed 1)))
   => true"
  {:added "3.0"}
  ([obj]
   (cond (ipending? obj)
         (clojure.core/realized? obj)

         (stage? obj)
         (if (stage-realized? obj)
           (realized? (stage-realize obj))
           false)

         :else true)))

(defn- mono-string
  ([{:keys [data]}]
   (str "#mono " @data)))

(defimpl Mono [future data parent]
  :string mono-string
  :protocols [std.protocol.stream/ISource
              :body {-produce  (stream/produce (realize future))}
              protocol.stream/IStage
              :body {-stage-unit      future
                     -stage-realized? (realized? future)
                     -stage-realize   (realize future)}]
  :interfaces [clojure.lang.IDeref
               :method {deref {[future] realize}}])

(defn mono?
  "checks if object is a mono
 
   (mono? (mono))"
  {:added "3.0"}
  [obj]
  (instance? Mono obj))

(defn mono
  "constructs a mono
 
   (mono {:future (f/completed 1)
          :data (atom {:name \"hello\"})
          :parent (mono)})
   => mono?"
  {:added "3.0"}
  ([]
   (mono {}))
  ([{:keys [future data parent]}]
   (Mono. future (or data (atom {})) parent)))

(defn wrap-stage
  "helper for unwrapping nested futures
 
   @@((wrap-stage inc) (f/completed (f/completed 1)))
   => 2"
  {:added "3.0"}
  ([f]
   (fn [obj]
     (cond (ipending? obj) (f @obj)

           (stage? obj)
           (f/on:success obj (wrap-stage f))

           :else
           (f obj)))))

(defn next-stage
  "constructs the next stage
 
   @(next-stage (mono {:future (f/completed 1)})
                inc
                {})
   => 2"
  {:added "3.0"}
  ([obj f m]
   (f/on:success (stage-unit obj) (fn [obj] (f (realize obj))) m)))

(defn i:async
  "constructs an async transducer
 
   (->> (sequence (i:async inc)
                  (range 5))
        (map deref))
   => '(1 2 3 4 5)"
  {:added "3.0"}
  ([]
   (i:async identity))
  ([f]
   (i:async f {}))
  ([f {:keys [pool timeout delay default] :as m}]
   (i/i:map
    (fn [obj]
      (cond (stage? obj)
            (next-stage obj f m)

            (ipending? obj)
            (f/future:run (fn [] (f @obj)) m)

            :else
            (f/future:run (fn [] (f obj)) m))))))

(defn wrap-time
  "wraps timing given the output
 
   ((wrap-time inc) 1)
   => (contains [number? 2])"
  {:added "3.0"}
  ([f]
   (fn [obj]
     (let [start  (time/time-ns)
           output (f obj)]
       [(time/elapsed-ns start) output]))))

(defn wrap-collectors
  "wrap collectors given async function"
  {:added "3.0"}
  ([f {:keys [data]} collectors]
   (reduce (fn [f {:keys [wrap path]}]
             (fn [obj]
               (let [[stat output] ((wrap f) obj)]
                 (swap! data assoc-in path stat)
                 output)))
           f
           collectors)))

(defn process-mono
  "chains monos together
 
   @(process-mono (mono) inc 1 {})
   => 2"
  {:added "3.0"}
  ([mono f obj m]
   (cond (mono? obj)
         (let [next (next-stage obj f m)]
           (assoc mono :future next :parent obj))

         (stage? obj)
         (assoc mono :future (next-stage obj f m))

         :else
         (let [obj (if (ipending? obj)
                     @obj
                     obj)]
           (assoc mono :future (f/future:run (fn [] (f obj)) m))))))

(defn i:step
  "constructs a step transducer"
  {:added "3.0"}
  ([]
   (i:step identity))
  ([f]
   (i:step f {}))
  ([f {:keys [pool timeout delay default collect] :as m}]
   (-> (fn [obj]
         (let [mono (if (mono? obj) obj (mono {}))
               f (wrap-collectors f mono collect)
               mono (process-mono mono f obj m)]
           mono))
       (i/i:map))))

(defn i:guard
  "constructs a guard transducer"
  {:added "3.0"}
  ([pred accept]
   (i:guard pred accept nil))
  ([pred accept reject]
   (i:guard pred accept reject {}))
  ([pred accept reject {:keys [pool timeout delay default collect] :as m}]
   (-> (fn [obj]
         (let [mono (if (mono? obj) obj (mono {:future (f/completed obj)}))
               f (fn [obj]
                   (if (pred obj)
                     [true  obj (stream/collect accept [mono])]
                     [false obj (stream/collect reject [mono])]))
               f (wrap-collectors f mono collect)]
           mono (process-mono mono f obj m)))
       (i/i:map))))

(stream/add-transforms :step  i:step
                       :async i:async
                       :guard i:guard)

(defn- flux-string
  ([{:keys [id queue]}]
   (str "#flux " {:id id :queue (coll/map-vals count @queue)})))

(defn produce-flux
  "takes from a flux
 
   (map realized? (take 2 (produce-flux (flux))))
   => '(false false)"
  {:added "3.0"}
  ([{:keys [id queue]}]
   (gen/gen (loop []
              (let [m (atom/swap-return! queue
                                      (fn [{:keys [received requested] :as q}]
                                        (cond (empty? received)
                                              (let [m (mono {:future (f/incomplete)})]
                                                [m (update q :requested conj m)])

                                              :else
                                              [(first received) (update q :received pop)])))
                    _ (swap! (:data m) assoc-in [:flux id :exit] (time/time-ns))]
                (gen/yield m)
                (recur))))))

(defn collect-flux
  "writes to a flux
 
   (collect-flux (flux) identity (range 5))"
  {:added "3.0"}
  ([{:keys [id queue] :as flux} xform supply]
   (let [t (time/time-ns)
         mono-fn (fn [val]
                   (let [m (cond (mono? val)
                                 val

                                 :else
                                 (mono {:future (f/completed val)}))]
                     (swap! (:data m) assoc-in [:flux id :enter] t)
                     m))
         queue-fn (fn [{:keys [received requested] :as q} m]
                    (cond (empty? requested)
                          [m (update q :received conj m)]

                          :else
                          (let [o (first requested)]
                            (swap! (:data o) assoc-in [:flux id :enter] t)
                            (f/on:complete (:future m)
                                           (fn [obj e]
                                             (if e
                                               (f/future:force (:future o) :exception e)
                                               (f/future:force (:future o) :value obj))))
                            [o (update q :requested pop)])))]
     (->> (xform supply)
          (map (fn [val]
                 (let [m   (mono-fn val)
                       ret (atom/swap-return! queue #(queue-fn % m))]
                   ret)))
          (doall))
     flux)))

(defimpl Flux [id queue]
  :suffix "-flux"
  :string flux-string
  :protocols [std.protocol.stream/ISink
              protocol.stream/ISource])

(defn flux?
  "checks if object is a flux
 
   (flux? (flux))
   => true"
  {:added "3.0"}
  [obj]
  (instance? Flux obj))

(defn flux
  "constructs a flux"
  {:added "3.0"}
  ([]
   (flux (h/sid)))
  ([id]
   (Flux. id (atom {:received  (coll/queue)
                    :requested (coll/queue)}))))
