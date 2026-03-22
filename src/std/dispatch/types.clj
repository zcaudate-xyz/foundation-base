(ns std.dispatch.types
  (:require [std.concurrent :as cc]
            [std.contract :as c :refer [defspec defmultispec defcase defcontract]]))

;;; contracts for pool and queue

(defspec <queue>
  [:or
   pos-int?
   [:fn cc/queue?]
   (c/lax {:size [:or zero? pos-int?]})])

(def +pool+
  {:size pos-int?
   :max pos-int?
   :keep-alive [:or zero? pos-int?]
   :queue <queue>})

(defspec <pool> +pool+)

;;; contract for concurrent.dispatch/executor-dispatch

(defmultispec <executor> :type)

(defcase <executor> :shared
  {:type :shared
   :id keyword?})

(defcase <executor> :pool
  {:type :pool}
  (c/lax <pool> [:queue]))

(defcase <executor> :scheduled
  {:type :scheduled
   :size pos-int?})

(defcase <executor> :single
  {:type :single}
  (c/lax {:queue <queue>}))

(defcase <executor> :cached
  {:type :cached})

(defcontract cc/executor
  :inputs [{<executor> :strict}])

;;; contract for protocol.exector/-create

(def +dispatch:pool+
  [:or (-> (assoc +pool+ :type keyword?)
           (c/lax [:type :max :keep-alive :queue]))
   (c/as:schema {:type :cached})
   (c/as:schema {:type :shared :id keyword?})])

(def +dispatch+
  {:type keyword?
   :options {:pool +dispatch:pool+
             :counter boolean?}
   :handler (c/fn [dispatch event])
   (c/opt :hooks) (c/lax
                   {:on-startup  (c/fn [dispatch])
                    :on-shutdown (c/fn [dispatch])
                    :on-batch    (c/fn [dispatch])
                    :on-poll     (c/fn [dispatch group])
                    :on-skip     (c/fn [dispatch skip])
                    :on-submit   (c/fn [dispatch entry])
                    :on-queued   (c/fn [dispatch entry])
                    :on-process  (c/fn [dispatch entry])
                    :on-error    (c/fn [dispatch entry exception])
                    :on-complete (c/fn [dispatch entry result])})})

(def +dispatch:common+
  (-> +dispatch+
      (update :options c/lax [:counter])))

(def +options|interval+
  {:interval pos-int?})

(def +options|batch+
  {:max-batch pos-int?})

(def +options|group+
  {:group-fn (c/fn [dispatch entry])})

(def +options|delay+
  {:delay pos-int?})

(def +options|debounce+
  {:strategy '(:eager :delay :notify)
   :run-final boolean?})

(defspec <dispatch:core>
  (-> +dispatch:common+
      (update (c/opt :hooks)
              c/remove [:on-batch :on-skip :on-poll])))

(defspec <dispatch:queue>
  {:options {:queue (-> (merge +options|batch+
                               +options|interval+
                               +options|delay+)
                        (c/lax [:delay]))}}
  (-> +dispatch:common+
      (update (c/opt :hooks)
              c/remove [:on-queued :on-skip])
      (update :options
              c/lax    [:pool])))

(defspec <dispatch:debounce>
  {:options {:debounce (-> (merge +options|debounce+
                                  +options|group+
                                  +options|interval+
                                  +options|delay+)
                           (c/lax [:delay :run-final :group-fn]))}}
  (-> +dispatch:common+
      (update (c/opt :hooks) c/remove [:on-batch])))

(defspec <dispatch:hub>
  {:options {:hub (-> (merge +options|group+
                             +options|batch+
                             +options|interval+
                             +options|delay+)
                      (c/lax [:delay]))}}
  (-> +dispatch:common+
      (update (c/opt :hooks) c/remove [:on-skip])))

(defspec <dispatch:board>
  {:options {:board {:groups-fn (c/fn [dispatch entry])}}}
  (-> +dispatch:common+
      (update (c/opt :hooks) c/remove [:on-skip :on-batch])))

(defmultispec <dispatch> :type
  :core  <dispatch:core>
  :queue <dispatch:queue>
  :debounce <dispatch:debounce>
  :hub <dispatch:hub>
  :board <dispatch:board>)

