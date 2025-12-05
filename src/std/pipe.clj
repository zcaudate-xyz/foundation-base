(ns std.pipe
  (:require [std.lib :as h]
            [std.lib.stream :as s]
            [std.lib.stream.iter :as i]
            [std.lib.stream.async :as s.async]
            [std.lib.result :as res]
            [std.lib.signal :as signal]
            [std.print :as print]
            [std.string :as str]
            [std.pipe.util :as ut]
            [std.pipe.display :as display]))

(declare pipe)

(defn wrap-input
  "enables execution of task with single or multiple inputs"
  {:added "3.0"}
  ([f task]
   (fn [input params lookup env & args]
     (cond (= :list input)
           (let [list-fn  (or (-> task :item :list)
                              (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
             (apply pipe task (list-fn lookup env) (assoc params :bulk true) lookup env args))

           (and (or (keyword? input)
                    (vector? input)
                    (set? input)
                    (h/form? input))
                (-> task :item :list))
           (let [inputs (ut/select-inputs task lookup env input)]
             (apply pipe task inputs (assoc params :bulk true) lookup env args))

           :else
           (apply f input params lookup env args)))))

;;
;; Stream-based Bulk Logic
;;

(defn- exec-item
  [f {:keys [idx total display display-fn]} {:keys [print context] :as params} lookup env args]
  (fn [input]
    (let [start        (System/currentTimeMillis)
          _            (signal/signal {:type :task :status :start :input input :idx idx :total total :context context})
          ;; Merge context into params for the task function
          task-params  (merge params context)
          [key result] (try (apply f input task-params lookup env args)
                            (catch Exception e
                              (print/println ">>" (.getMessage e))
                              (let [end   (System/currentTimeMillis)]
                                [input (res/result {:status :error
                                                    :time (- end start)
                                                    :start start
                                                    :end   end
                                                    :data :errored})])))
          end    (System/currentTimeMillis)
          result (assoc result :time (- end start) :start start :end end)
          {:keys [status data time]} result]
      (signal/signal {:type :task :status status :input input :result result :idx idx :total total :context context})
      (when (:item print)
        (let [index (format "%s/%s" (inc idx) total)
              item  (if (= status :return)
                      (display-fn data)
                      result)
              time  (format "%.2fs" (/ time 1000.0))]
          (print/print-row [index key item time] display)))
      [key result])))

(defn- pipe-single
  [task f input params lookup env args]
  (let [f (wrap-input f task)] ;; Recursively calls pipe if input becomes a list
    (apply f input params lookup env args)))

(defn- pipe-bulk
  [task f input inputs params lookup env func-args]
  (let [inputs (if (:random params) (shuffle inputs) inputs)
        _      (when (:item (:print params))
                 (print/print "\n")
                 (print/print-subtitle (format "ITEMS (%s)" (count inputs))))

        ;; Setup Display
        total      (count inputs)
        index-len  (let [digits (if (pos? total)
                                  (inc (long (Math/log10 total)))
                                  1)]
                     (+ 2 (* 2 digits)))
        input-len  (->> inputs (map (comp count str)) (apply max) (+ 2))
        display-fn (or (-> task :item :display) identity)
        display    (display/bulk-display index-len input-len)
        context    (merge (:context params)
                          {:total  total
                           :display display
                           :display-fn display-fn})

        _ (if (:item (:print params)) (print/print "\n"))

        ;; Setup Title
        title (:title params)
        _ (when (and (or (-> params :print :function)
                         (-> params :print :item)
                         (-> params :print :result)
                         (-> params :print :summary))
                     title)
            (print/print-title (if (fn? title)
                                 (title params env)
                                 title)))

        start (h/time-ms)

        ;; Pipeline Construction
        pipeline-fn (fn [[idx input]]
                      ((exec-item f (assoc context :idx idx) params lookup env func-args) input))

        pipeline (cond (= (:mode params) :fifo)
                       (i/i:map pipeline-fn)

                       (:parallel params)
                       (s.async/i:async pipeline-fn)

                       :else
                       (i/i:map pipeline-fn))

        ;; Execution
        items (->> (s/stream inputs
                             (i/i:map-indexed vector)
                             pipeline
                             (i/i:map s.async/realize) ;; Ensure future is realized
                             (java.util.ArrayList.))
                   (vec))

        elapsed   (h/elapsed-ms start)
        warnings  (display/bulk-warnings params items)
        errors    (display/bulk-errors params items)
        results   (display/bulk-results task params items)
        summary   (display/bulk-summary task params items results warnings errors elapsed)]

    (display/bulk-package task
                          {:items items
                           :warnings warnings
                           :errors errors
                           :results results
                           :summary summary}
                          (or (:return params) :results)
                          (:package params))))

(defn pipe
  "executes the task using a stream pipeline"
  {:added "4.0"}
  ([task & args]
   (if (vector? task)
     (let [tasks task
           comp-fn (fn [input params lookup env & args]
                     (reduce (fn [val task]
                               (let [[f _] (ut/main-function (-> task :main :fn) (or (-> task :main :argcount) 3))]
                                 (apply f val params lookup env args)))
                             input
                             tasks))
           task (assoc (first tasks) :main {:fn comp-fn :argcount 4})] ;; Set argcount 4 for comp-fn
       (apply pipe task args))
     (let [idx (h/index-at #{:args} args)
           _    (if (and (neg? idx) (-> task :main :args?))
                  (throw (ex-info "Require `:args` keyword to specify additional arguments"
                                  {:input args})))
           [task-args func-args] (if (neg? idx)
                                   [args []]
                                   [(take idx args) (drop (inc idx) args)])
           [input params lookup env] (apply ut/task-inputs task task-args)

           ;; Setup Main Function
           f  (let [[main _] (ut/main-function (-> task :main :fn) (or (-> task :main :argcount) 3))]
                (ut/wrap-execute main task))
           params (h/merge-nested (:params task) params)]

       (if-not (:bulk params)
         (pipe-single task f input params lookup env func-args)
         (let [inputs (cond (= :list input)
                            (let [list-fn (or (-> task :item :list)
                                              (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
                              (list-fn lookup env))

                            (and (or (keyword? input)
                                     (vector? input)
                                     (set? input)
                                     (h/form? input))
                                 (-> task :item :list))
                            (ut/select-inputs task lookup env input)

                            :else input)]
           (pipe-bulk task f input inputs params lookup env func-args)))))))
