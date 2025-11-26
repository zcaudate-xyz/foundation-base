(ns std.lib.future-start
  (:require [std.lib.future :as f])
  (:import (clojure.lang IFn IDeref)))

(defrecord Startable [state]
  IFn
  (invoke [this]
    (let [[old-state new-state] (swap-vals! state
                                     (fn [{:keys [status] :as m}]
                                       (if (= status :pending)
                                         (assoc m :status :running)
                                         m)))]
      (if (= (:status old-state) :pending)
        (let [f (f/future ((:thunk @state) {:instance this}))]
          (f/on:complete f (fn [res err]
                             (reset! state (if err
                                             {:status :error :err err}
                                             {:status :success :res res}))))
          f)
        (if-let [res (get old-state :res)]
          (f/completed res)
          (f/failed (or (:err old-state) (ex-info "Startable has already been run and failed." {})))))))

  IDeref
  (deref [this]
    (let [{:keys [status res err]} @state]
      (case status
        :pending (throw (ex-info "Startable not started." {}))
        :running (throw (ex-info "Startable running." {}))
        :success res
        :error (throw err)))))

(defmethod print-method Startable
  ([v ^java.io.Writer w]
   (.write w (str "#startable" @v))))

(defn startable?
  "checks if object is a startable"
  {:added "4.0"}
  ([obj]
   (instance? Startable obj)))

(defn startable
  "Creates a startable future.

   (def t (startable (fn [_] 1)))
   (t)
   => future?

   @(t)
   => 1"
  {:added "4.0"}
  ([thunk]
   (Startable. (atom {:status :pending
                      :thunk thunk}))))
