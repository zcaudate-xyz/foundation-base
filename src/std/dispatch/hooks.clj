(ns std.dispatch.hooks)

(defn counter
  "creates the executor counter
 
   (counter)"
  {:added "3.0"}
  ([]
   {:submit   (atom 0)
    :queued   (atom 0)
    :process  (atom 0)
    :complete (atom 0)
    :error    (atom 0)
    :poll     (atom 0)
    :skip     (atom 0)
    :batch    (atom 0)}))

(defn inc-counter
  "increment the executor counter"
  {:added "3.0"}
  ([{:keys [options runtime] :as dispatch} key]
   (if-not (false? (:counter options))
     (swap! (get (:counter runtime) key) inc))))

(defn update-counter
  "updates the executor counter"
  {:added "3.0"}
  ([{:keys [options runtime] :as dispatch} key f & args]
   (if-not (false? (:counter options))
     (apply swap! (get (:counter runtime) key) f args))))

(defn handle-entry
  "processes the hook on each stage"
  {:added "3.0"}
  ([{:keys [hooks] :as dispatch} key & args]
   (let [handler (get hooks key)]
     (if handler (apply handler dispatch args)))))

(defn on-submit
  "helper for the submit stage"
  {:added "3.0"}
  ([dispatch entry]
   (inc-counter dispatch :submit)
   (handle-entry dispatch :on-submit entry)))

(defn on-queued
  "helper for the queued stage"
  {:added "3.0"}
  ([dispatch entry]
   (inc-counter dispatch :queued)
   (handle-entry dispatch :on-queued entry)))

(defn on-batch
  "helper for the on-batch stage"
  {:added "3.0"}
  ([dispatch]
   (inc-counter dispatch :batch)
   (handle-entry dispatch :on-batch)))

(defn on-process
  "helper for the process stage"
  {:added "3.0"}
  ([dispatch entry]
   (inc-counter dispatch :process)
   (handle-entry dispatch :on-process entry)))

(defn on-process-bulk
  "helper for the process stage"
  {:added "3.0"}
  ([dispatch entries]
   (update-counter dispatch :process + (count entries)) (handle-entry dispatch :on-process entries)))

(defn on-skip
  "helper for the skip stage"
  {:added "3.0"}
  ([dispatch entry]
   (inc-counter dispatch :skip) (handle-entry dispatch :on-skip entry)))

(defn on-poll
  "helper for the poll stage"
  {:added "3.0"}
  ([dispatch group]
   (inc-counter dispatch :poll) (handle-entry dispatch :on-poll group)))

(defn on-error
  "helper for the error stage"
  {:added "3.0"}
  ([dispatch entry error]
   (inc-counter dispatch :error) (handle-entry dispatch :on-error entry error)))

(defn on-complete
  "helper for the complete stage"
  {:added "3.0"}
  ([dispatch entry result]
   (inc-counter dispatch :complete) (handle-entry dispatch :on-complete entry result)))

(defn on-complete-bulk
  "helper for the complete stage"
  {:added "3.0"}
  ([dispatch entries result]
   (update-counter dispatch :complete + (count entries)) (handle-entry dispatch :on-complete entries result)))

(defn on-shutdown
  "helper for the shutdown stage"
  {:added "3.0"}
  ([dispatch]
   (handle-entry dispatch :on-shutdown)))

(defn on-startup
  "helper for the startup stage"
  {:added "3.0"}
  ([dispatch]
   (handle-entry dispatch :on-startup)))
