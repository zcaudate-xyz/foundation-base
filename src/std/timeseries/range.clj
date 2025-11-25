(ns std.timeseries.range
  (:require [std.lib :as h]
            [std.timeseries.common :as common]
            [std.math :as math]))

(defn parse-range-unit
  "categorising the unit range
 
   (parse-range-unit :start {})
   => [:array 0]
 
   (parse-range-unit :end {})
   => [:array -1]
 
   (parse-range-unit :1m {:unit :ms})
   => [:time 60000]
 
   (parse-range-unit :-1m {:unit :ms})
   => [:time -60000]
 
   (parse-range-unit 0.44 {})
   => [:ratio 0.44]"
  {:added "3.0"}
  ([x {:keys [unit]}]
   (cond (= :start x)
         [:array 0]

         (= :end x)
         [:array -1]

         (= :all x)
         [:array -1]

         (inst? x)
         [:absolute (common/from-ms (inst-ms x) unit)]

         (or (keyword? x)
             (string? x))
         [:time (common/parse-time x unit)]

         (integer? x)
         [:array x]

         (number? x)
         [:ratio x]

         :else
         (throw (ex-info "Invalid range expression" {:input x})))))

(defn parse-range-expr
  "parsing a range expression
 
   (parse-range-expr [:20s 0.8] {:unit :ms})"
  {:added "3.0"}
  ([range time-opts]
   (cond (map? range) range

         (= :all range) (parse-range-expr [0 -1] time-opts)

         :else
         (let [[start type? end?] range
               [start type end] (cond (nil? end?)
                                      [start :to type?]

                                      :else
                                      [start type? end?])
               start (parse-range-unit start time-opts)
               end   (parse-range-unit end time-opts)
               type  (cond (and (= (first end) :absolute)
                                (neg? (second start)))
                           :end

                           (= (first end) :absolute)
                           :to

                           :else type)]
           {:type  type
            :start start
            :end   end}))))

(defn range-wrap
  "function wrapper for range-start and range-end functions"
  {:added "3.0"}
  ([f start]
   (fn [arr params [tag val] time-opts]
     (cond (or (empty? arr)
               (nil? tag))
           (cond->> arr
             start (vector 0))

           :else
           (f arr params [tag val] time-opts)))))

(defn range-start
  "chooses the start of the array
 
   (range-start [1 2 3 4] {} [:array 2] {:order :asc :key identity})
   => [2 [3 4]]
 
   (range-start [-2 -1 0 1 2] {} [:time 2] {:order :asc :key identity})
   => [2 [0 1 2]]
 
   (range-start [-2 -1 0 1 2] {} [:absolute 0] {:order :asc :key identity})
   => [2 [0 1 2]]"
  {:added "3.0"}
  ([arr {:keys [length]} [tag val] {:keys [order key]}]
   (case tag
     :array  [val (drop val arr)]
     :ratio  (let [num (long (* val length))]
               [num (drop num arr)])
     (let [{:keys [comp-fn op-fn]} (common/order-fns order)
           t (case tag
               :absolute val
               :time (-> (key (first arr))
                         (op-fn val)))
           counter (h/counter -1)
           out (doall (drop-while (fn [v]
                                    (h/inc! counter)
                                    (comp-fn (key v) t)) arr))]
       [@counter out]))))

(defn range-end-for
  "ends the array range given :for option
 
   (range-end-for [2 3 4] {} [:array 2] {:order :asc :key identity})
   => [2 3]"
  {:added "3.0"}
  ([arr {:keys [length]} [tag val] {:keys [order key]}]
   (case tag
     :array (take val arr)
     :ratio  (cond (= val 1) arr
                   :else (let [num (math/ceil (* val length))]
                           (take num arr)))
     (let [{:keys [comp-eq-fn op-fn]} (common/order-fns order)
           t (case tag
               :absolute val
               :time (-> (key (first arr))
                         (op-fn val)))]
       (take-while #(comp-eq-fn (key %) t) arr)))))

(defn range-end-to
  "ends the array range given :to option
 
   (range-end-to [2 3 4] {:dropped 2} [:array 4] {:order :asc :key identity})
   => [2 3]"
  {:added "3.0"}
  ([arr {:keys [length dropped start]} [tag val] {:keys [order key]}]
   (let [len (count arr)]
     (case tag
       :array (take (- val dropped) arr)
       :ratio  (cond (= val 1) arr
                     :else (let [num (- (math/ceil (* val length)) dropped)]
                             (take num arr)))
       (let [{:keys [comp-eq-fn op-fn]} (common/order-fns order)
             t (case tag
                 :absolute val
                 :time  (op-fn start val))]
         (take-while #(comp-eq-fn (key %) t) arr))))))

(defn range-op
  "standardises units for negative inputs"
  {:added "3.0"}
  ([type [tag val] arr {:keys [key order]}]
   (cond (not (neg? val))
         [tag val]

         (= :for type)
         (throw (ex-info "Value cannot be negative in :for expressions"))

         :else
         (case tag
           :array [tag (let [len (count arr)
                             idx (+ len val)]
                         (if-not (pos? idx) 1 idx))]
           :ratio [tag (+ 1 val)]
           :time  [tag (let [t0 (key (first arr))
                             t1 (key (last arr))
                             op-fn (common/order-op order)
                             t' (op-fn t1 val)]
                         (case order
                           :asc  (- t' t0)
                           :desc (- t0 t')))]
           [tag val]))))

(defn select-range-standard
  "helper for standard range select"
  {:added "3.0"}
  ([arr {:keys [type start end] :as m} {:keys [key] :as time-opts}]
   (let [length (count arr)
         start  (range-op :to start arr time-opts)
         end    (range-op type end arr time-opts)
         start-fn   (range-wrap range-start true)
         end-fn     (range-wrap (case type
                                  :to  range-end-to
                                  :for range-end-for)
                                false)
         params {:length length :start (if-not (empty? arr)
                                         (key (first arr)))}]
     (let [[dropped arr] (start-fn arr params start time-opts)
           arr (end-fn arr (assoc params :dropped dropped) end time-opts)]
       (vec arr)))))

(defn select-range-end
  "helper for range select when a date value is at the end"
  {:added "3.0"}
  ([arr {:keys [start end] :as m} {:keys [key] :as time-opts}]
   (let [start-fn (range-wrap range-start true)
         end-fn (range-wrap range-end-to false)
         arr    (end-fn arr {} end time-opts)
         length (count arr)
         start  (range-op :to start arr time-opts)
         params {:length length :start (if-not (empty? arr)
                                         (key (first arr)))}
         [dropped arr] (start-fn arr params start time-opts)]
     (vec arr))))

(defn select-range
  "selects the range
 
   (select-range [1 2 3 4 5]
                 {:type :for :start [:array 1] :end [:array 3]}
                 {:order :asc :key identity})
   => [2 3 4]"
  {:added "3.0"}
  ([arr {:keys [type] :as range-opts} time-opts]
   (let [select-fn (if (= type :end)
                     select-range-end
                     select-range-standard)]
     (select-fn arr range-opts time-opts))))

(defn process-filter
  "processes items given a filter
 
   (process-filter  [1 2 3 4 5 6 7 8]
                    even?
                    {})
   => [2 4 6 8]"
  {:added "3.0"}
  ([arr filter-opts time-opts]
   (cond (nil? filter-opts)
         arr

         (fn? filter-opts)
         (filter filter-opts arr)

         (map? filter-opts)
         (filter (fn [m]
                   (->> filter-opts
                        (map (fn [[k v]]
                               (let [v0 (get m k)]
                                 (if (fn? v)
                                   (v v0)
                                   (= v v0)))))
                        (every? true?)))
                 arr)

         :else (throw (ex-info "Invalid filter" {:input filter-opts})))))

(defn process-range
  "range stage in the process pipeline
 
   (process-range (range 10000)
                  {:time    {:key identity :unit :s :order :asc}
                   :range   [:1m :5m]
                   :sample  [10 :linear]})
   => [60 86 112 138 164 190 216 242 268 300]
 
   (process-range (range 10000)
                  {:time    {:key identity :unit :ms :order :asc}
                   :range   [:-1s (Date. 7000)]
                   :sample  [10 :linear]})
   => [6000 6111 6222 6333 6444 6555 6666 6777 6888 7000]"
  {:added "3.0"}
  ([arr {:keys [range filter sample] time-opts :time}]
   (let [range-opts  (parse-range-expr range time-opts)
         sample-opts (common/parse-sample-expr sample time-opts)]
     (-> (select-range arr range-opts time-opts)
         (process-filter filter time-opts)
         (common/process-sample sample-opts time-opts)))))

(defmethod common/sampling-parser :range
  ([_]
   (fn [opts time-opts]
     (let [{range-opts :size} opts
           range-opts (parse-range-expr range-opts time-opts)]
       (assoc opts :size range-opts)))))

(defmethod common/sampling-fn :range
  ([_]
   (fn [arr opts time-opts]
     (let [{range-opts :size} opts
           range-opts (parse-range-expr range-opts time-opts)]
       (select-range arr range-opts time-opts)))))


