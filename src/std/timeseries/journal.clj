(ns std.timeseries.journal
  (:require [std.lib :as h :refer [defimpl]]
            [std.timeseries.common :as common]
            [std.timeseries.types :as types]
            [std.timeseries.process :as process]
            [std.print.format :as format])
  (:refer-clojure :exclude [merge derive])
  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(defn format-time
  "output the time according to format and time unit
 
   (format-time 10000 {:unit :s :format \"HH:mm:ss\"})
   => #(.endsWith ^String % \":46:40\")"
  {:added "3.0"}
  ([val {:keys [unit format]}]
   (.format (SimpleDateFormat. format)
            (Date. (common/to-ms val unit)))))

(def +defaults+
  {:meta     {:hide  #{:meta}
              :entry {:flatten true :pre-flattened false}
              :head  {:range [0 10]}
              :time  {:key :s/time :unit :ns :order :desc :format "HH:mm:ss a"}}
   :select   {:range [:start :end]
              :group {:cluster 5
                      :time    {:aggregate :first}}}})

(defn template-keys
  "get keys for the template
 
   (template-keys (common/create-template {:data {:in  \"\"
                                                  :out \"ABC\"}}))
   => [:data.in :data.out]"
  {:added "3.0"}
  ([template]
   (if-let [flat (:flat (:raw template))]
     (sort (keys flat)))))

(defn entry-display
  "displays entry, formatting time
 
   (entry-display {:data {:in  \"\"
                          :out \"ABC\"}
                   :time 100000}
                  {:meta {:time {:key :time :unit :s :format \"HH:mm:ss\"}}})
   => (contains {:data {:in \"\", :out \"ABC\"}, :time #(.endsWith ^String % \":46:40\")})"
  {:added "3.0"}
  ([entry {:keys [meta]}]
   (-> entry
       (update (:key (:time meta)) #(format-time % (:time meta))))))

(defn create-template
  "creates a template a puts in the cache"
  {:added "3.0"}
  ([{:keys [template]} entry]
   (let [tmpl (common/create-template entry)]
     (vreset! template tmpl))))

(defn get-template
  "gets existing or creates a new template
 
   (get-template (journal {:entries [{:a {:b 1}
                                      :s/time 0}]}))
   => map?"
  {:added "3.0"}
  ([{:keys [entries template] :as journal}]
   (or @template
       (when (seq entries)
         (create-template journal (first entries))))))

(defn entries-seq
  "gets entries in time order
 
   (-> (journal {:limit 2
                 :entries  [{:a 2 :s/time 2}]
                 :previous [{:a 1 :s/time 1} {:a 0 :s/time 0}]})
       (entries-seq))
   => [{:a 2, :s/time 2} {:a 1, :s/time 1}]"
  {:added "3.0"}
  ([{:keys [limit entries previous]}]
   (if limit
     (concat entries (take (- limit (count entries)) previous))
     entries)))

(defn entries-vec
  "gets entries in time order
 
   (-> (journal {:meta {:time {:order :asc}}
                 :limit 2
                 :entries  [{:a 2 :s/time 2}]
                 :previous [{:a 0 :s/time 0} {:a 1 :s/time 1}]})
       (entries-vec))
   => [{:a 1, :s/time 1} {:a 2, :s/time 2}]"
  {:added "3.0"}
  ([{:keys [limit entries previous]}]
   (if limit
     (concat (if (seq previous)
               (subvec previous (count entries)))
             entries)
     entries)))

(defn journal-entries
  "gets entries from the journal
 
   (-> (journal {:meta {:time {:order :asc}
                        :entry {:flatten true :pre-flattened true}}
                 :limit 2
                 :entries  [{:a 2 :s/time 2}]
                 :previous [{:a 0 :s/time 0} {:a 1 :s/time 1}]})
       (add-entry  {:a 3 :s/time 3})
       (add-entry  {:a 4 :s/time 4})
       ((juxt :entries journal-entries)))
  => [[{:a 4, :s/time 4}]
       [{:a 3, :s/time 3} {:a 4, :s/time 4}]]"
  {:added "3.0"}
  ([journal]
   (case (-> journal :meta :time :order)
     :asc (entries-vec journal)
     :desc (entries-seq journal))))

(defn journal-info
  "returns info for the journal
 
   (-> (journal {:meta {:time   {:key :start :order :desc}
                        :head   {:range [0 3]}
                        :hide   #{:meta :template :id}}})
       journal-info)
   => {:order :desc, :count 0, :head []}"
  {:added "3.0"}
  ([{:keys [id meta] :as journal}]
   (let [{:keys [hide time head]} meta
         entries (journal-entries journal)
         head (process/process entries (clojure.core/merge {:time time} head))]
     (cond-> {:id id :meta meta :order (:order time) :count (count entries)}
       (and (not (:duration hide))
            (not (empty? entries))) (assoc :duration (format/t:ms (common/duration entries time)))
       (not (:template hide)) (assoc :template (template-keys (get-template journal)))
       (not (:head hide))     (assoc :head (mapv #(entry-display % journal) head))
       :then   (#(apply dissoc % hide))))))

(declare select update-meta)

(defn journal-invoke
  "invoke function for the journal"
  {:added "3.0"}
  ([journal]
   (journal-info (update-meta journal {:hide #{}})))
  ([journal arg]
   (cond (map? arg)
         (select journal arg)

         (= arg :info)
         (journal-info journal)

         :else
         (select journal {:series arg})))
  ([journal k v & more]
   (let [args (apply list k v more)
         args (if (odd? (count args))
                (cons :series args)
                args)]
     (select journal (apply hash-map args)))))

(defn- journal-string
  ([journal]
   (str "#jnl " (journal-info journal))))

(defimpl Journal [id meta template entries limit previous]
  :string  journal-string
  :invoke  journal-invoke
  :final   true

  clojure.lang.IDeref
  (deref [journal] (:entries journal)))

(h/suppress
 (prefer-method clojure.pprint/simple-dispatch
                clojure.lang.IPersistentMap
                clojure.lang.IDeref))

(defn journal
  "creates a new journal
 
   (journal {:meta {:time {:unit :s :format \"HH:mm:ss\"}
                    :entry {:flatten false}
                   :head {:range [0 3]}}})"
  {:added "3.0"}
  ([]
   (journal {}))
  ([{:keys [id meta entries previous] :as m
     :or {id (h/sid)}}]
   (let [{:keys [time] :as meta} (h/merge-nested (:meta +defaults+) meta)
         entries-fn (case (:order time)
                      :desc  #(apply list %)
                      :asc #(apply vector %))]
     (-> m
         (assoc :id id
                :meta meta
                :template (volatile! nil)
                :entries (entries-fn entries)
                :previous (entries-fn previous))
         (types/<journal> :strict)
         (map->Journal)))))

(defn add-time
  "adds time to entry if if doesn't exist
 
   (add-time {} :t :s)
   => (contains {:t integer?})"
  {:added "3.0"}
  ([entry key unit]
   (if-not (get entry key)
     (let [t (common/from-ns (h/time-ns) unit)]
       (assoc entry key t))
     entry)))

(defn update-journal-single
  "adds a single entry to the journal"
  {:added "3.0"}
  ([{:keys [limit entries] :as journal} entry]
   (if (or (nil? limit)
           (< (count entries) limit))
     (update journal :entries conj entry)
     (let [f (case (-> journal :meta :time :order)
               :asc vector :desc list)]
       (assoc journal :entries (f entry) :previous entries)))))

(defn add-entry
  "adds an entry to the journal
 
   (-> (journal {:meta {:head {:range [0 3]}}})
       (add-entry {:start 1 :output {:value 1}})
       journal-info)
   => (contains-in {:id string?
                    :order :desc, :count 1,
                    :duration \"0ms\",
                    :template [:output.value :start],
                    :head [{:start 1, :output.value 1, :s/time string?}]})"
  {:added "3.0"}
  ([{:keys [meta template] :as journal} entry]
   (let [{:keys [key unit]} (:time meta)
         {:keys [flatten pre-flattened]} (:entry meta)
         entry (if (and flatten (not pre-flattened))
                 (let [{:keys [flat]} (or @template
                                          (create-template journal entry))]
                   (flat entry))
                 entry)
         entry (add-time entry key unit)]
     (update-journal-single journal entry))))

(defn update-journal-bulk
  "adds multiple entries to the journal"
  {:added "3.0"}
  ([{:keys [limit entries] :as journal} new-entries]
   (let [len (count entries)
         f (case (-> journal :meta :time :order)
             :asc vector :desc list)]
     (cond (or (nil? limit)
               (<= (+ len (count new-entries)) limit))
           (update journal :entries #(apply conj % new-entries))

           (<= limit (count new-entries))
           (assoc journal
                  :entries  ()
                  :previous (->> new-entries
                                 (drop (- (count new-entries) limit))
                                 (into (f))))

           :else
           (let [split (- limit len)
                 previous (apply conj entries (take split new-entries))
                 current (apply f (drop split new-entries))]
             (assoc journal :entries current :previous previous))))))

(defn add-bulk
  "adds multiple entries to the journal"
  {:added "3.0"}
  ([{:keys [meta template] :as journal} entries]
   (let [{:keys [key unit]} (:time meta)
         {:keys [flatten pre-flattened]} (:entry meta)
         entries (if (and flatten (not pre-flattened))
                   (let [{:keys [flat]} (or @template
                                            (create-template journal (first entries)))]
                     (map flat entries))
                   entries)
         entries (map #(add-time % key unit) entries)]
     (update-journal-bulk journal entries))))

(defn update-meta
  "updates journal meta. used for display"
  {:added "3.0"}
  ([journal meta]
   (update journal :meta h/merge-nested meta)))

(defn select-series
  "select data from the series"
  {:added "3.0"}
  ([entries series]
   (cond (keyword? series)
         (mapv series entries)

         (vector? series)
         (mapv #(select-series entries %) series)

         (list? series)
         (apply mapv vector
                (select-series entries (vec series)))

         (map? series)
         (let [{:s/keys [meta]} series
               series (dissoc series :s/meta)]
           (clojure.core/merge meta (h/map-vals #(select-series entries %) series))))))

(defn select
  "selects data ferom the journal
 
   (def -jnl- (->> [0 (journal {:meta {:time   {:key :start :order :desc}
                                       :head   {:range [0 3]}
                                       :hide   #{:meta :template :id}}})]
                   (iterate (fn [[t journal]]
                              (let [t (+ t 1000000000 (rand-int 100000000))]
                                [t (add-entry journal {:start t
                                                       :output {:value (Math/sin
                                                                        (+ (/ t 100000000000)
                                                                           (rand)))}})])))
                   (map second)
                   (take 1000)
                   (last)))
 
   (select -jnl- {:range [:1m :5m] :sample 10})
   => (fn [entries]
        (= 10 (count entries)))
 
   (first (select -jnl- {:range [:1m :5m] :sample 10}))
   => (contains {:start integer? :output.value number?})"
  {:added "3.0"}
  ([journal]
   (select journal {}))
  ([{:keys [meta] :as journal} {:keys [range sample transform series template compute]
                                :or {range :all}}]
   (let [entries  (journal-entries journal)
         template (get-template journal)
         transform (if transform
                     (cond-> transform
                       (-> meta :entry :flatten) (assoc :skip true)))
         entries (process/process entries
                                  (cond-> {:time (:time meta)
                                           :range range
                                           :sample sample
                                           :template template
                                           :compute compute}
                                    transform (assoc :transform transform)))]
     (cond-> entries
       series (select-series series)))))

(defn derive
  "derives the journal given a select statement
 
   (derive -jnl- {:range [:1m :15m]
                  :sample 2000
                 :transform {:interval :0.1s
                              :time    {:aggregate :first}
                              :default {:aggregate :mean :sample 3}}})"
  {:added "3.0"}
  ([{:keys [meta] :as journal} {:keys [range sample transform]
                                :as params}]
   (let [{:keys [order]} (:time meta)
         subentries (select journal (dissoc params :series))
         subentries (case order
                      :asc (vec subentries)
                      :desc (apply list subentries))]
     (cond-> journal
       :then (assoc :entries subentries)
       transform (assoc-in [:meta :entry :flatten] true)))))

(defn merge-sorted
  "merges a series of arrays together
 
   (merge-sorted [[1 3 4 6 9 10 15]
                  [2 3 6 7 8 9 10]
                  [1 3 6 7 8 9 10]
                  [1 2 3 4 8 9 10]])
   => [1 1 1 2 2 3 3 3 3 4 4 6 6 6 7 7 8 8 8 9 9 9 9 10 10 10 10 15]"
  {:added "3.0"}
  ([coll]
   (merge-sorted coll identity))
  ([coll key-fn]
   (merge-sorted coll identity <))
  ([coll key-fn comp-fn]
   (->> coll
        (filter seq)
        (h/unfold (fn [s]
                    (if (seq s)
                      (let [[[mf & mn] r]
                            (reduce (fn [[m r] x]
                                      (if (comp-fn (key-fn (first x)) (key-fn (first m)))
                                        [x (cons m r)]
                                        [m (cons x r)]))
                                    [(first s) ()]
                                    (rest s))]
                        (list mf (if mn (cons mn r) r)))))))))

(defn merge
  "merges two journals of the same type together ^:hiddene

   (-> ((merge (derive -jnl- {:range [:1m :3m]
                              :transform {:interval 1
                                          :time     {:aggregate :middle}
                                          :default {:aggregate :mean :sample 3}}})
               (derive -jnl- {:range [:10m -1]
                              :transform {:interval :5s
                                          :time    {:aggregate :first}
                                          :default {:aggregate :mean}}}))
        {:sample :0.1s
         :transform {:interval :1s
                     :time {:aggregate :first
                            :fn #(long (/ % 100))}
                     :custom [{:keys [:output.value]
                               :aggregate :random
                               :fn #(long (* % 100))}]}
         :series [{:data '(:start :output.value)
                   :s/meta {:title \"Output 1\" :with :lines}}]}))"
  {:added "3.0"}
  ([])
  ([j0] j0)
  ([j0 j1]
   (if-not (and (= (:meta j0)
                   (:meta j1))
                (= (:raw @(:template j0))
                   (:raw @(:template j1))))
     (throw (ex-info "Meta and Template requires to be the same"))
     (let [{:keys [order key]} (-> j0 :meta :time)
           comp-fn (common/order-comp order)
           entries (cond->> (merge-sorted [(:entries j0) (:entries j1)] key comp-fn)
                     (= order :asc) (vec)
                     (= order :desc) (apply list))]
       (assoc j0 :entries entries)))))
