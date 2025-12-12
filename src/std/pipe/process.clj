(ns std.pipe.process
  (:require [std.lib :as h]
            [std.lib.stream :as s]
            [std.lib.stream.iter :as i]
            [std.lib.stream.async :as s.async]
            [std.lib.result :as res]
            [std.print :as print]
            [std.string :as str]
            [std.pipe.util :as ut]
            [std.pipe.display :as display]
            [std.pipe.monitor :as monitor]))

(def ^:dynamic *interrupt* false)

(declare invoke)

(defn- exec-item
  [f {:keys [idx total display display-fn monitor]} {:keys [print] :as params} lookup env args]
  (fn [input]
    (when monitor (monitor/update-monitor monitor input :start))
    (let [start        (System/currentTimeMillis)
          [key result] (try (apply f input params lookup env args)
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
      (when monitor
        (if (or (= status :error)
                (= status :critical))
          (monitor/update-monitor monitor input :fail result)
          (monitor/update-monitor monitor input :complete result)))
      (when (:item print)
        (let [index (format "%s/%s" (inc idx) total)
              item  (if (= status :return)
                      (display-fn data)
                      result)
              time  (format "%.2fs" (/ time 1000.0))]
          (print/print-row [index key item time] display)))
      [key result])))

(defn wrap-bulk
  "wraps the function to handle bulk execution"
  {:added "4.0"}
  ([f task]
   (fn [input params lookup env & func-args]
     (if-not (:bulk params)
       (apply f input params lookup env func-args)
       (let [inputs input ;; Input is assumed to be resolved collection if :bulk is true

             ;; Ensure input count for display calculation doesn't fail on empty input
             inputs (if (:random params) (shuffle inputs) inputs)
             total      (count inputs)

             _      (when (:item (:print params))
                      (print/print "\n")
                      (print/print-subtitle (format "ITEMS (%s)" total)))

             ;; Setup Display
             index-len  (let [digits (if (pos? total)
                                       (inc (long (Math/log10 total)))
                                       1)]
                          (+ 2 (* 2 digits)))

             ;; FIX: Avoid (apply max) on empty sequence
             input-len  (if (empty? inputs)
                          2
                          (->> inputs (map (comp count str)) (apply max) (+ 2)))

             display-fn (or (-> task :item :display) identity)
             display    (display/bulk-display index-len input-len)
             monitor    (when (:monitor params)
                          (monitor/create-monitor inputs display-fn))
             _          (when monitor
                          (monitor/monitor-loop monitor 100))
             context    {:total  total
                         :display display
                         :display-fn display-fn
                         :monitor monitor}

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
                       (:package params)))))))

(defn wrap-main
  "wraps the main function for the task"
  {:added "4.0"}
  ([task]
   (let [;; Setup Main Function
         fn-obj (-> task :main :fn)
         argcount (or (-> task :main :argcount)
                      (let [fcounts (h/arg-count fn-obj)]
                        (cond (empty? fcounts) 4
                              (seq fcounts) (apply min fcounts)
                              :else 3)))
         [main _] (ut/main-function fn-obj argcount)
         f (ut/wrap-execute main task)]
     (-> f
         (wrap-bulk task)))))

(defn resolve-input
  "resolves inputs for bulk execution"
  {:added "4.0"}
  [task input params lookup env]
  (cond (= :list input)
        (let [list-fn (or (-> task :item :list)
                          (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
          [(list-fn lookup env) (assoc params :bulk true)])

        (and (or (keyword? input)
                 (vector? input)
                 (set? input)
                 (h/form? input))
             (-> task :item :list))
        [(ut/select-inputs task lookup env input) (assoc params :bulk true)]

        :else
        [input params]))

(defn invoke
  "executes the task"
  {:added "4.0"}
  ([task & args]
   (let [idx (h/index-at #{:args} args)
         _    (if (and (neg? idx) (-> task :main :args?))
                (throw (ex-info "Require `:args` keyword to specify additional arguments"
                                {:input args})))
         [task-args func-args] (if (neg? idx)
                                 [args []]
                                 [(take idx args) (drop (inc idx) args)])
         [input params lookup env] (apply ut/task-inputs task task-args)
         params (h/merge-nested (:params task) params)

         ;; Resolve inputs if needed (handles selector logic)
         [input params] (if (:bulk params)
                          [input params] ;; Trust existing bulk flag (input is data)
                          (resolve-input task input params lookup env))

         f      (wrap-main task)]
     
     (apply f input params lookup env func-args))))
