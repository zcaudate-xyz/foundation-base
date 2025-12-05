(ns std.pipe
  (:require [std.protocol.invoke :as protocol.invoke]
            [std.lib :as h]
            [std.lib.stream :as s]
            [std.lib.stream.iter :as i]
            [std.lib.stream.async :as s.async]
            [std.lib.result :as res]
            [std.print :as print]
            [std.string :as str]
            [std.pipe.util :as ut]
            [std.pipe.display :as display]))

(declare pipe)

(defmulti pipe-defaults
  "creates default settings for pipe task groups"
  {:added "4.0"}
  identity)

(defmethod pipe-defaults :default
  ([_]
   {:main {:arglists '([] [entry])}}))

(h/definvoke invoke-intern-pipe
  "creates a pipe task"
  {:added "4.0"}
  [:method {:multi protocol.invoke/-invoke-intern
            :val :pipe}]
  ([_ name config body]
   (let [template (:template config)
         ;; Try to get arglists from defaults if possible, but handle failure safely
         defaults (try (pipe-defaults template) (catch Throwable _ {}))

         arglists (or (:arglists config)
                      (:arglists defaults)
                      '([& args]))

         doc      (:doc config)

         meta-map (merge (meta name)
                         {:doc doc
                          :arglists (list 'quote arglists)})]
      `(def ~name
         (with-meta
           (fn [& args#]
             (let [defaults# (pipe-defaults ~template)
                   task#     (h/merge-nested defaults# ~config)
                   task#     (assoc task# :name ~(str name))]
               (apply std.pipe/pipe task# args#)))
           ~meta-map)))))

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
  [f {:keys [idx total display display-fn]} {:keys [print] :as params} lookup env args]
  (fn [input]
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
      (when (:item print)
        (let [index (format "%s/%s" (inc idx) total)
              item  (if (= status :return)
                      (display-fn data)
                      result)
              time  (format "%.2fs" (/ time 1000.0))]
          (print/print-row [index key item time] display)))
      [key result])))

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
           task (assoc (first tasks) :main {:fn comp-fn})]
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
         ;; Single Invocation
         (let [f (wrap-input f task)] ;; Recursively calls pipe if input becomes a list
           (apply f input params lookup env func-args))

         ;; Bulk / Pipe Execution
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

                            :else input)

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
               context    {:total  total
                           :display display
                           :display-fn display-fn}

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
               ;; Source: inputs
               ;; Pipeline:
               ;; 1. map-indexed: adds index
               ;; 2. async: executes task (returns Mono)
               ;; 3. map realize: waits for result
               ;; 4. collect: gathers all results

               pipeline-fn (fn [[idx input]]
                             ((exec-item f (assoc context :idx idx) params lookup env func-args) input))

               ;; If parallel, we wrap in i:async which does pmap-like execution.
               ;; If not parallel, we just map it.
               ;; However, to use std.lib.stream.async primitives, we use i:async for concurrency.

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
                         (:package params))))))))
