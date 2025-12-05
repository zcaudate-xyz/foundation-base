(ns std.pipe
  (:require [std.lib :as h]
            [std.lib.stream :as s]
            [std.lib.stream.iter :as i]
            [std.lib.stream.async :as s.async]
            [std.lib.result :as res]
            [std.print :as print]
            [std.string :as str]))

;;
;; Helpers from std.task.process
;;

(defn main-function
  "creates a main function to be used for execution"
  {:added "4.0"}
  ([func count]
   (let [fcounts (h/arg-count func)
         fcount  (if-not (empty? fcounts)
                   (apply min fcounts)
                   4)
         args?   (> fcount count)
         main    (cond (= count 4) (fn [input params lookup env & args]
                                     (apply func input params lookup env args))
                       (= count 3) (fn [input params _ env & args]
                                     (apply func input params env args))
                       (= count 2) (fn [input params _ _ & args]
                                     (apply func input params args))
                       (= count 1) (fn [input _ _ _ & args]
                                     (apply func input args))
                       :else (throw (ex-info "`count` is a value between 1 to 4" {:count count})))]
     [main args?])))

(defn select-filter
  "matches given a range of filters"
  {:added "4.0"}
  [selector id]
  (cond (or (fn?  selector)
            (var? selector))
        (h/suppress (selector id))

        (or (string? selector)
            (symbol? selector)
            (keyword? selector))
        (.startsWith ^String (str id) (str selector))

        (h/regexp? selector)
        (boolean (re-find selector (str id)))

        (set? selector) (selector id)

        (h/form? selector)  (every? #(select-filter % id)
                                    selector)

        (vector? selector) (some #(select-filter % id)
                                 selector)

        (number? selector) (= selector id)

        :else
        (throw (ex-info "Selector not valid" {:selector selector}))))

(defn select-inputs
  "selects inputs based on matches"
  {:added "4.0"}
  ([task lookup env selector]
   (let [list-fn    (or (-> task :item :list)
                        (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
     (cond (= selector :all)
           (list-fn lookup env)

           :else
           (->> (list-fn lookup env)
                (filter #(select-filter selector %)))))))

(declare pipe)

(defn wrap-execute
  "enables execution of task with transformations"
  {:added "3.0"}
  ([f task]
   (fn [input params lookup env & args]
     (let [pre-fn    (or (-> task :item :pre) identity)
           post-fn   (or (-> task :item :post) identity)
           output-fn (or (-> task :item :output) identity)
           input  (pre-fn input)
           result (apply f input params lookup env args)
           result (post-fn result)]
       (if (:bulk params)
         [input (res/->result input result)]
         (output-fn result))))))

(defn wrap-input
  "enables execution of task with single or multiple inputs"
  {:added "3.0"}
  ([f task]
   (fn [input params lookup env & args]
     (cond (= :list input)
           (let [list-fn  (or (-> task :item :list)
                              (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
             (list-fn lookup env))

           (or (keyword? input)
               (vector? input)
               (set? input)
               (h/form? input))
           (let [inputs (select-inputs task lookup env input)]
             (apply pipe task inputs (assoc params :bulk true) lookup env args))

           :else
           (apply f input params lookup env args)))))

(defn task-inputs
  "constructs inputs to the task given a set of parameters"
  {:added "4.0"}
  ([task]
   (let [input-fn (or (-> task :construct :input) (constantly nil))]
     (task-inputs task (input-fn task) task)))
  ([task input]
   (let [input-fn (or (-> task :construct :input) (constantly nil))
         [input params] (cond (map? input)
                              [(input-fn task) input]

                              :else [input {}])]
     (task-inputs task input params)))
  ([task input params]
   (let [env-fn (or (-> task :construct :env) (constantly {}))]
     (task-inputs task input params (env-fn (merge task params)))))
  ([task input params env]
   (let [lookup-fn (or (-> task :construct :lookup) (constantly {}))]
     (task-inputs task input params (lookup-fn task (merge env params)) env)))
  ([task input params lookup env]
   [input params lookup env]))

;;
;; Helpers from std.task.bulk
;;

(defn bulk-display
  "constructs bulk display options"
  {:added "3.0"}
  ([index-len input-len]
   {:padding 1
    :spacing 1
    :columns [{:id :index :length index-len
               :color #{:blue}
               :align :right}
              {:id :input :length input-len}
              {:id :data  :length 60 :color #{:white}}
              {:id :time  :length 10 :color #{:bold}}]}))

(defn prepare-columns
  "prepares columns for printing"
  {:added "3.0"}
  ([columns outputs]
   (mapv (fn [{:keys [length key] :as column}]
           (let [id    key
                 length (cond (number? length)
                              length

                              :else
                              (->> outputs (map key) (map (comp count str)) (apply max) (+ 2)))]
             (assoc column :id key :length length)))
         columns)))

(defn bulk-warnings
  "outputs warnings that have been processed"
  {:added "3.0"}
  ([{:keys [print] :as params} items]
   (let [warnings (filter #(-> % second :status (= :warn)) items)]
     (when (and (:result print) (seq warnings))
       (print/print "\n")
       (print/print-subtitle (format "WARNINGS (%s)" (count warnings)))
       (print/print "\n")
       (print/print-column warnings :data #{:warn}))
     warnings)))

(defn bulk-errors
  "outputs errors that have been processed"
  {:added "3.0"}
  ([{:keys [print] :as params} items]
   (let [errors (filter #(-> % second :status #{:critical :error}) items)]
     (when (and (:result print) (seq errors))
       (print/print "\n")
       (print/print-subtitle (format "ERRORS (%s)" (count errors)))
       (print/print "\n")
       (print/print-column errors :data #{:error}))
     errors)))

(defn bulk-results
  "outputs results that have been processed"
  {:added "3.0"}
  ([task {:keys [print order-by] :as params} items]
   (let [ignore-fn (-> task :result :ignore)

         remove-fn (fn [[key {:keys [data status] :as result}]]
                     (or (#{:error :warn :info :critical} status)
                         (and ignore-fn
                              (ignore-fn data))))
         results   (remove remove-fn items)
         _         (when (:result print)
                     (print/print "\n")
                     (print/print-subtitle (format "RESULTS (%s)" (count results))))]
     (cond (empty? results)
           []

           :else
           (let [key-fns    (-> task :result :keys)
                 format-fns (-> task :result :format)
                 sort-by-fn (-> task :result :sort-by)
                 outputs  (mapv (fn [[key {:keys [id data] :as result}]]
                                  (->> key-fns
                                       (map (fn [[k f]] [k (f data)]))
                                       (into (assoc result :key key))))
                                results)
                 outputs  (if order-by
                            (clojure.core/sort-by order-by outputs)
                            outputs)
                 columns  (-> task :result :columns)
                 display  {:padding 1
                           :spacing 1
                           :columns (prepare-columns columns outputs)}
                 row-keys (map :key columns)]
             (when (:result print)
               (print/print "\n")
               (print/print-header row-keys display)
               (mapv (fn [output]
                       (let [row (mapv (fn [k]
                                         (let [data      (get output k)
                                               format-fn (get format-fns k)]
                                           (cond-> data format-fn (format-fn params))))
                                       row-keys)]
                         (print/print-row row display)))
                     outputs))
             outputs)))))

(defn bulk-summary
  "outputs summary of processed results"
  {:added "3.0"}
  ([task {:keys [print] :as params} items results warnings errors elapsed]
   (let [aggregate-fns (-> task :summary :aggregate)
         finalise-fn   (-> task :summary :finalise)
         cumulative    (apply + (map (comp :time second) items))
         summary (merge {:errors    (count errors)
                         :warnings  (count warnings)
                         :items     (count items)
                         :results   (count results)}
                        (->> aggregate-fns
                             (h/map-vals (fn [[sel acc init]]
                                           (reduce (fn [out v]
                                                     (let [sv (sel v)]
                                                       (if (nil? sv)
                                                         out
                                                         (acc out sv))))
                                                   init
                                                   results)))))
         summary (if finalise-fn
                   (finalise-fn summary items results)
                   summary)
         display (->> summary
                      (remove (comp #(and (number? %)
                                          (zero? %))
                                    second))
                      (into {}))
         _       (when (:summary print)
                   (print/print "\n")
                   (print/print-subtitle (format "SUMMARY %s"
                                                 (str (assoc display
                                                             :cumulative (h/format-ms cumulative)
                                                             :elapsed (h/format-ms elapsed)))))
                   (print/println))]
     (assoc summary :cumulative cumulative :elapsed elapsed))))

(defn bulk-package
  "packages results for return"
  {:added "3.0"}
  ([task {:keys [items warnings errors results summary] :as bundle} return package]
   (cond (= return :all)
         (bulk-package task bundle #{:items :warnings :errors :results :summary} package)

         (keyword? return)
         (first (vals (bulk-package task bundle #{return} package)))

         :else
         (let [items-fn    (or (-> task :item :output) identity)
               results-fn  (or (-> task :result :output) identity)]
           (reduce (fn [out kw]
                     (cond (#{:summary :warnings :errors} kw)
                           (assoc out kw (get bundle kw))

                           (= :items kw)
                           (cond->> (get bundle kw)
                             :then (map (fn [[key v]] [key (items-fn (:data v))]))
                             (not= package :vector) (into {})
                             :then (assoc out :items))

                           (= :results kw)
                           (cond->> (get bundle kw)
                             :then (map (fn [v] [(:key v) (results-fn (:data v))]))
                             (not= package :vector) (into {})
                             :then (assoc out :results))

                           :else out))
                   {}
                   return)))))

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
                                                    :data :errored})])))
          end    (System/currentTimeMillis)
          result (assoc result :time (- end start))
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
     (let [[head & tail] task
           input (first args)
           args  (rest args)
           out   (apply pipe head input args)]
       (if (empty? tail)
         out
         (apply pipe (vec tail) out args)))
     (let [idx (h/index-at #{:args} args)
           _    (if (and (neg? idx) (-> task :main :args?))
                  (throw (ex-info "Require `:args` keyword to specify additional arguments"
                                  {:input args})))
           [task-args func-args] (if (neg? idx)
                                   [args []]
                                   [(take idx args) (drop (inc idx) args)])
           [input params lookup env] (apply task-inputs task task-args)

           ;; Setup Main Function
           f  (let [[main _] (main-function (-> task :main :fn) (or (-> task :main :argcount) 3))]
                (wrap-execute main task))
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

                            (or (keyword? input)
                                (vector? input)
                                (set? input)
                                (h/form? input))
                            (select-inputs task lookup env input)

                            :else input)

               inputs (if (:random params) (shuffle inputs) inputs)
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
               display    (bulk-display index-len input-len)
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
               warnings  (bulk-warnings params items)
               errors    (bulk-errors params items)
               results   (bulk-results task params items)
               summary   (bulk-summary task params items results warnings errors elapsed)]

           (bulk-package task
                         {:items items
                          :warnings warnings
                          :errors errors
                          :results results
                          :summary summary}
                         (or (:return params) :results)
                         (:package params))))))))
