(ns std.timeseries.process
  (:require [std.lib :as h]
            [std.timeseries.common :as common]
            [std.timeseries.compute :as compute]
            [std.timeseries.types :as types]
            [std.timeseries.range :as range]

            [std.string :as str]
            [std.string.path :as path]
            [std.math :as math]
            [std.lib.walk :as walk]))

(defn prep-merge
  "prepares the merge functions and options
 
   (prep-merge {:sample [[0 :1m] :range]} {:unit :ms})
   => (contains-in [fn? {:size {:type :to, :start [:array 0],
                                :end [:time 60000]},
                         :strategy :range}])"
  {:added "3.0"}
  ([{:keys [sample aggregate none]
     :or {aggregate :mean none 0}} time-opts]
   (let [aggregate-fn (if (fn? aggregate)
                        aggregate
                        (or (get compute/+aggregations+ aggregate)
                            (throw (ex-info "Invalid aggregate choice" {:input aggregate}))))
         sample-opts (common/parse-sample-expr sample time-opts)]
     [aggregate-fn sample-opts])))

(defn create-merge-fn
  "creates a merge function
 
   (def merge-fn (create-merge-fn {:sample 9} {:key identity :order :asc}))
 
   (long (merge-fn (range 90)))
   => 44"
  {:added "3.0"}
  ([{:keys [sample aggregate none] :as m} time-opts]
   (let [[aggregate-fn sample-opts] (prep-merge m time-opts)]
     (fn [arr]
       (let [arr (common/process-sample arr sample-opts (assoc time-opts :sort false))]
         (or (cond-> (aggregate-fn arr)
               (:fn m) ((:fn m)))
             none))))))

(defn create-custom-fns
  "create custom functions
 
   (create-custom-fns [{:keys [:bench.stats.lag :bench.stats.time]
                        :sample 10
                        :aggregate :max}]
                      {:order :desc :key :start})
   => (contains {:bench.stats.lag fn?
                 :bench.stats.time fn?})"
  {:added "3.0"}
  ([custom time-opts]
   (reduce (fn [out {:keys [keys] :as m}]
             (let [merge-fn (create-merge-fn m time-opts)]
               (reduce (fn [out k] (assoc out k merge-fn))
                       out
                       keys)))
           {}
           custom)))

(defn map-merge-fn
  "creates a map merge function
 
   (map-merge-fn {:time    {:aggregate :first}
                  :default {:sample 4}
                  :custom  [{:keys [:bench.stats.lag]
                             :sample 10
                             :aggregate :max}
                            {:keys [:bench.stats.time]
                             :sample 4
                             :aggregate :mean}]}
                {:order :desc :key :start})
   => fn?"
  {:added "3.0"}
  ([{:keys [default time custom skip template]} {time-key :key :as time-opts}]
   (let [default-fn (create-merge-fn default time-opts)
         time-fn    (create-merge-fn (merge {:aggregate :first} time) time-opts)
         custom-fns (create-custom-fns custom time-opts)]
     (fn [arr]
       (let [arr     (if (or skip (not template))
                       arr
                       (mapv (:flat template) arr))
             mkeys   (apply disj (set (keys (first arr)))
                            time-key (keys custom-fns))
             mdata   (if (or (nil? custom) default)
                       (reduce (fn [m k]
                                 (assoc m k (default-fn (map k arr))))
                               {}
                               mkeys))
             cdata   (h/map-entries (fn [[k f]]
                                      [k (f (map k arr))])
                                    custom-fns)
             tdata   {time-key (time-fn (map time-key arr))}]
         (merge mdata cdata tdata))))))

(defn time-merge-fn
  "creates a merge function for time"
  {:added "3.0"}
  ([{:keys [time]} time-opts]
   (create-merge-fn (merge {:aggregate first} time) time-opts)))

(defn parse-transform-expr
  "parses the transform expr"
  {:added "3.0"}
  ([{:keys [interval time default custom] :as m} type time-opts]
   (let [interval   (range/parse-range-unit interval time-opts)
         merge-fn   ((case type
                       :map map-merge-fn
                       :time time-merge-fn) m time-opts)]
     (assoc m :interval interval :merge-fn merge-fn))))

(defn transform-interval
  "transform array based on resolution"
  {:added "3.0"}
  ([arr {:keys [interval template merge-fn]} type time-opts]
   (let [len (count arr)
         empty (-> template :raw :empty)
         interval (range/range-op :to interval arr time-opts)
         [tag val] interval
         arr   (case tag
                 :time   (let [{:keys [key order]} time-opts
                               {:keys [op-fn comp-fn]} (common/order-fns order)
                               start  (key (first arr))
                               steps  (quot (math/abs (- start (key (last arr))))
                                            val)
                               sorted (group-by (fn [m]
                                                  (quot (math/abs (- start (key m)))
                                                        val))
                                                arr)]
                           (mapv (fn [i]
                                   (or (get sorted i)
                                       (case type
                                         :map  [(assoc empty key (op-fn start (* i val)))]
                                         :time [(op-fn start (* i val))])))
                                 (range steps)))
                 :ratio  (let [num (math/ceil (* len val))]
                           (partition num arr))
                 :array (partition val arr))]
     (mapv merge-fn arr))))

(defn process-transform
  "processes the transform stage"
  {:added "3.0"}
  ([arr {:keys [transform template] time-opts :time}]
   (cond (or (empty? arr)
             (nil? transform))
         arr

         :else
         (let [type  (if (map? (first arr)) :map :time)
               template (when (= type :map)
                          (or template
                              (common/create-template (first arr))))
               {:keys [interval]} transform
               transform (assoc transform
                                :interval (or interval 1)
                                :template template)
               {:keys [interval] :as transform} (parse-transform-expr transform type time-opts)]
           (cond (not= interval [:array 1])
                 (transform-interval arr transform type time-opts)

                 :else
                 (if (and template
                          (not (:skip transform)))
                   (map (:flat transform) arr)
                   arr))))))

(defmethod common/sampling-parser :interval
  ([_]
   (fn [{interval :size :as opts} time-opts]
     (assoc opts :interval interval :skip true))))

(defmethod common/sampling-fn :interval
  ([_]
   (fn [arr opts time-opts]
     (process-transform arr {:transform opts :time time-opts}))))

(defn process-compute
  "processes the indicator stage
 
   (->> (process-compute [{:start 0
                           :bench.stats.lag 0.7
                           :bench.stats.time 0.3}
                          {:start 2
                           :bench.stats.lag 0.3
                           :bench.stats.time 0.5}
                          {:start 3
                           :bench.stats.lag 0.4
                          :bench.stats.time 0.3}
                          {:start 4
                           :bench.stats.lag 0.5
                           :bench.stats.time 0.3}]
                         {:compute {:lag  [:s/norm :bench.stats.lag]
                                    :time [:s/norm :bench.stats.time]}})
        (map (juxt :lag :time)))
   => [[1.0 0.6] [0.4285714285714286 1.0] [0.5714285714285715 0.6] [0.7142857142857143 0.6]]"
  {:added "3.0"}
  ([arr {:keys [compute time]}]
   (cond (or (empty? arr)
             (nil? compute))
         arr

         :else
         (compute/compute arr compute))))

(defn process
  "processes time series"
  {:added "3.0"}
  ([arr {:keys [time transform computes] :as opts}]
   (types/<process> opts :strict)
   (let [{:keys [key order]} time
         order  (or order
                    (if (< (key (first arr))
                           (key (last arr)))
                      :asc
                      :desc))
         time-opts (common/parse-time-expr (assoc time :order order))
         opts   (assoc opts :time time-opts)
         arr    (range/process-range arr opts)
         arr    (process-transform arr opts)
         output (process-compute arr opts)]
     output)))
