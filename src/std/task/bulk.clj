(ns std.task.bulk
  (:require [std.print :as print]
            [std.string :as str]
            [std.lib :as h]
            [std.lib.result :as res]))

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

(defn bulk-process-item
  "bulk operation processing each item"
  {:added "3.0"}
  ([f
    {:keys [idx total input output display display-fn]}
    {:keys [print] :as params} lookup env args]
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
         {:keys [status data time]} result
         _  (if (:item print)
              (let [index (format "%s/%s" (inc idx) total)
                    item  (if (= status :return)
                            (display-fn data)
                            result)
                    time  (format "%.2fs" (/ time 1000.0))]
                (print/print-row [index key item time] display)))
         _ (if output (vreset! output [key result]))]
     [key result])))

(defn bulk-items-parallel
  "bulk operation processing in parallel"
  {:added "3.0"}
  ([f inputs {:keys [idxs] :as context} params lookup env args]
   (->> (pmap (fn [idx input]
                (let [output  (volatile! nil)
                      out-str (h/with-out-str
                                (bulk-process-item f (assoc context
                                                            :idx idx
                                                            :input input
                                                            :output output)
                                                   params lookup env args))]
                  [@output out-str]))
              idxs
              inputs)
        (mapv (fn [[output out-str]]
                (print/print out-str)
                output)))))

(defn bulk-items-single
  "bulk operation processing in single"
  {:added "3.0"}
  ([f inputs {:keys [idxs] :as context} params lookup env args]
   (mapv (fn [idx input]
           (bulk-process-item f (assoc context
                                       :idx idx
                                       :input input)
                              params lookup env args))
         idxs
         inputs)))

(defn bulk-items
  "processes each item given a input"
  {:added "3.0"}
  ([task f inputs {:keys [print parallel random] :as params} lookup env args]
   (when (:item print)
     (print/print "\n")
     (print/print-subtitle (format "ITEMS (%s)" (count inputs))))
   (cond (empty? inputs) []

         :else
         (let [inputs     (if random (shuffle inputs) inputs)
               total      (count inputs)
               index-len  (let [digits (if (pos? total)
                                         (inc (long (Math/log10 total)))
                                         1)]
                            (+ 2 (* 2 digits)))
               input-len  (->> inputs (map (comp count str)) (apply max) (+ 2))
               display-fn (or (-> task :item :display) identity)
               display    (bulk-display index-len input-len)
               context    {:total  total
                           :idxs   (range total)
                           :display display
                           :display-fn display-fn}]
           (if (:item print) (print/print "\n"))
           (if parallel
             (bulk-items-parallel f inputs context params lookup env args)
             (bulk-items-single f inputs context params lookup env args))))))

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

(defn prepare-columns
  "prepares columns for printing
 
   (prepare-columns [{:key :name}
                     {:key :data}]
                    [{:name \"Chris\"
                      :data \"1\"}
                     {:name \"Bob\"
                      :data \"100\"}])
   => [{:key :name, :id :name, :length 7}
       {:key :data, :id :data, :length 5}]"
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
                                       (into result)))
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

(defn bulk
  "process and output results for a group of inputs"
  {:added "3.0"}
  ([task f inputs {:keys [print package title return] :as params} lookup env & args]
   (let [params   (assoc params :bulk true)
         _          (when (and (or (:function print) (:item print) (:result print) (:summary print))
                               title)
                      (print/print-title (if (fn? title)
                                           (title params env)
                                           title)))
         start     (h/time-ms)
         items     (bulk-items task f inputs params lookup env args)
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
                   (or return :results)
                   package))))
