(ns std.timeseries.journal-test
  (:use code.test)
  (:require [std.timeseries.journal :refer :all]
            [std.timeseries.compute :as compute]
            [std.timeseries.common :as common]
            [std.lib :as h])
  (:refer-clojure :exclude [merge derive]))

^{:refer std.timeseries.journal/format-time :added "3.0"}
(fact "output the time according to format and time unit"

  (format-time 10000 {:unit :s :format "HH:mm:ss"})
  => #(.endsWith ^String % ":46:40"))

^{:refer std.timeseries.journal/template-keys :added "3.0"}
(fact "get keys for the template"

  (template-keys (common/create-template {:data {:in  ""
                                                 :out "ABC"}}))
  => [:data.in :data.out])

^{:refer std.timeseries.journal/entry-display :added "3.0"}
(fact "displays entry, formatting time"

  (entry-display {:data {:in  ""
                         :out "ABC"}
                  :time 100000}
                 {:meta {:time {:key :time :unit :s :format "HH:mm:ss"}}})
  => (contains {:data {:in "", :out "ABC"}, :time #(.endsWith ^String % ":46:40")}))

^{:refer std.timeseries.journal/create-template :added "3.0"}
(fact "creates a template a puts in the cache"

  (let [j (journal {})]
    (create-template j {:a 1})
    @(:template j))
  => map?)

^{:refer std.timeseries.journal/get-template :added "3.0"}
(fact "gets existing or creates a new template"

  (get-template (journal {:entries [{:a {:b 1}
                                     :s/time 0}]}))
  => map?)

^{:refer std.timeseries.journal/entries-seq :added "3.0"}
(fact "gets entries in time order"

  (-> (journal {:limit 2
                :entries  [{:a 2 :s/time 2}]
                :previous [{:a 1 :s/time 1} {:a 0 :s/time 0}]})
      (entries-seq))
  => [{:a 2, :s/time 2} {:a 1, :s/time 1}])

^{:refer std.timeseries.journal/entries-vec :added "3.0"}
(fact "gets entries in time order"

  (-> (journal {:meta {:time {:order :asc}}
                :limit 2
                :entries  [{:a 2 :s/time 2}]
                :previous [{:a 0 :s/time 0} {:a 1 :s/time 1}]})
      (entries-vec))
  => [{:a 1, :s/time 1} {:a 2, :s/time 2}])

^{:refer std.timeseries.journal/journal-entries :added "3.0"}
(fact "gets entries from the journal"

  (-> (journal {:meta {:time {:order :asc}
                       :entry {:flatten true :pre-flattened true}}
                :limit 2
                :entries  [{:a 2 :s/time 2}]
                :previous [{:a 0 :s/time 0} {:a 1 :s/time 1}]})
      (add-entry  {:a 3 :s/time 3})
      (add-entry  {:a 4 :s/time 4})
      ((juxt :entries journal-entries)))
  => [[{:a 4, :s/time 4}]
      [{:a 3, :s/time 3} {:a 4, :s/time 4}]])

^{:refer std.timeseries.journal/journal-info :added "3.0"}
(fact "returns info for the journal"

  (-> (journal {:meta {:time   {:key :start :order :desc}
                       :head   {:range [0 3]}
                       :hide   #{:meta :template :id}}})
      journal-info)
  => {:order :desc, :count 0, :head []})

^{:refer std.timeseries.journal/journal-invoke :added "3.0"}
(fact "invoke function for the journal"

  (journal-invoke (journal {}) :info)
  => (contains {:order :desc, :count 0, :head []}))

^{:refer std.timeseries.journal/map->Journal :added "3.0" :adopt true}
(fact "defines a journal object"

  (class (map->Journal {}))
  => std.timeseries.journal.Journal)

^{:refer std.timeseries.journal/journal :added "3.0"}
(fact "creates a new journal"

  (class (journal {:meta {:time {:unit :s :format "HH:mm:ss"}
                          :entry {:flatten false}
                          :head {:range [0 3]}}}))
  => std.timeseries.journal.Journal)

^{:refer std.timeseries.journal/add-time :added "3.0"}
(fact "adds time to entry if if doesn't exist"

  (add-time {} :t :s)
  => (contains {:t integer?}))

^{:refer std.timeseries.journal/update-journal-single :added "3.0"}
(fact "adds a single entry to the journal"

  (-> (journal {})
      (update-journal-single {:a 1})
      :entries)
  => coll?)

^{:refer std.timeseries.journal/add-entry :added "3.0"}
(fact "adds an entry to the journal"

  (-> (journal {:meta {:head {:range [0 3]}}})
      (add-entry {:start 1 :output {:value 1}})
      journal-info)
  => (contains-in {:id string?
                   :order :desc, :count 1,
                   :duration "0ms",
                   :template [:output.value :start],
                   :head [{:start 1, :output.value 1, :s/time string?}]}))

^{:refer std.timeseries.journal/update-journal-bulk :added "3.0"}
(fact "adds multiple entries to the journal"

  (-> (journal {})
      (update-journal-bulk [{:a 1} {:a 2}])
      :entries)
  => coll?)

^{:refer std.timeseries.journal/add-bulk :added "3.0"}
(fact "adds multiple entries to the journal" ^:hidden

  ;; Test for bulk + current over the limit
  (-> (journal {:meta {:time {:order :asc}
                       :entry {:flatten true :pre-flattened true}}
                :limit 3
                :entries  [{:a 2 :s/time 2} {:a 3 :s/time 3}]})
      (add-bulk  [{:a 4 :s/time 4} {:a 5 :s/time 5}])
      ((juxt :entries journal-entries)))
  [[{:a 5, :s/time 5}]
   [{:a 3, :s/time 3} {:a 4, :s/time 4} {:a 5, :s/time 5}]]

  ;; Test for bulk over the limit
  (-> (journal {:meta {:time {:order :asc}
                       :entry {:flatten true :pre-flattened true}}
                :limit 3
                :entries  [{:a 2 :s/time 2}]})
      (add-bulk  [{:a 3 :s/time 3} {:a 4 :s/time 4} {:a 5 :s/time 5}])
      ((juxt :entries journal-entries)))
  => [() [{:a 3, :s/time 3} {:a 4, :s/time 4} {:a 5, :s/time 5}]]

  ;; Test for bulk + current under limit
  (-> (journal {:meta {:time {:order :asc}
                       :entry {:flatten true :pre-flattened true}}
                :limit 3
                :entries  [{:a 2 :s/time 2}]})
      (add-bulk  [{:a 3 :s/time 3} {:a 4 :s/time 4}])
      ((juxt :entries journal-entries)))
  => [[{:a 2, :s/time 2} {:a 3, :s/time 3} {:a 4, :s/time 4}]
      [{:a 2, :s/time 2} {:a 3, :s/time 3} {:a 4, :s/time 4}]])

^{:refer std.timeseries.journal/update-meta :added "3.0"}
(fact "updates journal meta. used for display"

  (-> (journal {})
      (update-meta {:a 1})
      :meta)
  => (contains {:a 1}))

^{:refer std.timeseries.journal/select-series :added "3.0"}
(fact "select data from the series"

  (let [j (-> (journal {:meta {:time {:key :time}}})
              (add-bulk [{:a 1 :time 1} {:a 2 :time 2}]))]
    (select-series (journal-entries j) '(:a)))
  => '((2) (1)))

^{:refer std.timeseries.journal/select :added "3.0"}
(fact "selects data ferom the journal"

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
  => (contains {:start integer? :output.value number?}))

^{:refer std.timeseries.journal/derive :added "3.0"}
(fact "derives the journal given a select statement"

  (class (derive (journal {:entries [{:a 1 :s/time 1} {:a 2 :s/time 2}]}) {}))
  => std.timeseries.journal.Journal)

^{:refer std.timeseries.journal/merge-sorted :added "3.0"}
(fact "merges a series of arrays together"

  (merge-sorted [[1 3 4 6 9 10 15]
                 [2 3 6 7 8 9 10]
                 [1 3 6 7 8 9 10]
                 [1 2 3 4 8 9 10]])
  => [1 1 1 2 2 3 3 3 3 4 4 6 6 6 7 7 8 8 8 9 9 9 9 10 10 10 10 15] ^:hidden

  (-> (map reverse [[1 3 4 6 9 10 15]
                    [2 3 6 7 8 9 10]
                    [1 3 6 7 8 9 10]
                    [1 2 3 4 8 9 10]])
      (merge-sorted identity >))
  => [15 10 10 10 10 9 9 9 9 8 8 8 7 7
      6 6 6 4 4 3 3 3 3 2 2 1 1 1])

^{:refer std.timeseries.journal/merge :added "3.0"}
(fact "merges two journals of the same type together"

  (let [j1 (journal {:entries [{:a 1 :s/time 1}]})
        j2 (journal {:entries [{:a 2 :s/time 2}]})]
    (class (merge j1 j2)))
  => std.timeseries.journal.Journal)

(comment
  (first (-jnl- :range [0 1]))

  (./create-tests)
  (import '(javax.imageio ImageIO))
  (require '[clojure.java.io :as io]
           '[std.image :as img]
           '[std.image.awt.display :as d])

  (do (def -i- 10)
      (def -viewer- (d/create-viewer)))
  (def -p-
    (future
      (dotimes [-i- 100]
        (Thread/sleep 500)
        (-> (gp/plot [{:input (format "-2*sin(1*(-x + 0.5*%d))" -i-)}
                      {:input (format "4*sin(0.7*(x + 0.3*%d))" -i-)}]
                     {:autoscale true
                      :terminal [:gif :size '(400, 300)]})
            :output
            (img/read nil :awt)
            (img/display {:viewer -viewer-})))))

  (hara.shell/sh "cal")
  (hara.shell/sh "ls")
  (hara.shell/sh "ping www.baidu.com")
  (future-cancel -p-)

  (hara.shell/kill)

  (gp/plot {:autoscale true
            :terminal :svg})
  :output
  ((fn [^bytes b]
     (String. b)))

  (future
    (dotimes [i 1000]
      (Thread/sleep 400)
      (-> (:output @(hara.shell/in {:op :bytes :quiet true
                                    :line (format "plot sin((10*x + %d)/10)" i)})))))

  (future-cancel *1)

  (hara.shell/kill-all)

  (def -svg- (:output @(hara.shell/in  {:op :command :quiet true :line "plot sin(2*x)"})))

  (def -png- (:output @(hara.shell/in  {:op :bytes :quiet true :line "plot sin(2*x)"})))

  (def -gif- (:output @(hara.shell/in  {:op :bytes :quiet true :line "plot sin(5*x)"})))

  (use 'std.image)

  (time (def -img- (std.image/read -gif-)))
  (time (def -img- (std.image/read -png-)))
  (time (std.image/display -img-))
  (gp/cmd [[:set :terminal :gif :size '(400, 400)]])
  (gp/cmd [[]]))

(comment

  (compute/compile-form '(- :start [:series/first :start]))
  (clojure.core/let [G__54258 ((:first std.timeseries.compute/+aggregations+)
                               (clojure.core/filter clojure.core/identity (clojure.core/map (clojure.core/fn [output] :start) outputs)))]
    (clojure.core/map (clojure.core/fn [output] (- :start G__54258)) outputs)))

(comment

  (gp/plot (-jnl- :transform {:interval :5s
                              :time {:aggregate :first :fn #(long (/ % 1000))}}
                  :series [{:data '(:start :output.value)
                            :series/meta {:title "Output 1" :with :lines}}
                           {:data '(:start :start)
                            :series/meta {:title "Output 2" :with :lines}}])
           {:autoscale true})

  (gp/restart-gnuplot)

  (gp/plot (-jnl- '(:start :output.value))
           {:autoscale true})

  (-> (-jnl- '(:output.value :output.value)
             :sample :5s)
      (gp/plot {:autoscale true}))

  (-jnl-)
  (-> (-jnl- '(:start :output.value)
             :sample :10s)
      (gp/plot {:autoscale true}))

  (count (-jnl- {:sample :1s}))

  (gp/plot (take 2 (-jnl-)))

  (gp/plot {:with :lines :data (-jnl- :series :output.value
                                      ;;:sample :0.02m
                                      :transform {:interval 3
                                                  :default {:aggregate :mean}})})

  (gp/restart-gnuplot)

  (-jnl- {:range [:8m :10m]
          :transform {:resolution 2
                      :time    {:aggregate :first}
                      :default {:aggregate :mean}}})

  (./import)
  [fx.gnuplot :as gp]
  (require '[fx.gnuplot :as gp])

  (->> (-jnl- :range [:2m :5m] :series :output.value)
       (hash-map :with :lines :data)
       (gp/plot))

  (-jnl- :series :output.value)
  (gp/plot {:data (mapv :output.value (:entries -jnl-))})

  (def -j0- (->> [(util/time-ns) (journal {:meta {:time   {:key :start :order :desc}
                                                  :head   {:range [0 3]}
                                                  :hide   [:meta :template :id]}})]
                 (iterate (fn [[t journal]]
                            (let [t (+ t 3000000000 (rand-int 100000000))]
                              [t (add-entry journal {:start t
                                                     :output {:value (Math/sin
                                                                      (+ (/ t 100000000000)
                                                                         (rand)))}})])))
                 (map second)
                 (take 10000)
                 (last))))
