(ns code.test.base.process
  (:require [code.test.checker.common :as checker]
            [code.test.base.match  :as match]
            [code.test.base.runtime :as rt]
            [std.lib :as h]
            [std.lib.result :as res]))

(defn evaluate
  "converts a form to a result
 
   (->> (evaluate {:form '(+ 1 2 3)})
        (into {}))
   => (contains {:status :success, :data 6, :form '(+ 1 2 3), :from :evaluate})"
  {:added "3.0"}
  ([{:keys [form value]}]
   (let [out (if value
               value
               (try
                 {:status :success :data (eval form)}
                 (catch Throwable t
                   {:status :exception :data t})))]
     (res/result (assoc out :form form :from :evaluate)))))

(defmulti process
  "processes a form or a check
   (defn view-signal [op]
     (let [output (atom nil)]
       (h/signal:with-temp [:test (fn [{:keys [result]}]
                                    (reset! output (into {} result)))]
                           (process op)
                           @output)))
 
   (view-signal {:type :form
                 :form '(+ 1 2 3)
                 :meta {:line 10 :col 3}})
   => (contains {:status :success,
                 :data 6,
                 :form '(+ 1 2 3),
                 :from :evaluate,
                 :meta {:line 10, :col 3}})
 
   ((contains {:status :success,
               :data true,
               :checker base/checker?
               :actual 6,
               :from :verify,
               :meta nil})
    (view-signal {:type :test-equal
                  :input  {:form '(+ 1 2 3)}
                 :output {:form 'even?}}))
   => true"
  {:added "3.0"}
  :type)

(defn attach-meta
  "attaches metadata to the result"
  {:added "3.0"}
  ([m meta]
   (cond-> m
     (not rt/*eval-meta*) (assoc :meta meta)
     rt/*eval-meta* (assoc :meta rt/*eval-meta*
                           :original meta))))

(defmethod process :form
  ([{:keys [form meta] :as op}]
   (let [result (-> (evaluate op)
                    (attach-meta meta))
         _    (intern *ns* (with-meta '*last* {:dynamic true})
                      (:data result))]
     (h/signal {:test :form :result (assoc result :replace rt/*eval-replace*)})
     result)))

(defmethod process :test-equal
  ([{:keys [input output meta] :as op}]
   (let [{:keys [guard before after]} rt/*eval-check*
         _ (before)
         actual   (evaluate input)
         expected (evaluate output)
         checker  (assoc (checker/->checker (res/result-data expected))
                         :form (:form expected))
         result   (-> (checker/verify checker actual)
                      (attach-meta meta))
         compare (cond (map? (:data result))
                       (:data result)

                       (and (not (:data result))
                            (coll? (:data actual))
                            (coll? (:data expected)))
                       nil
                       #_(diff/diff (:data actual)
                                    (:data expected)))
         _    (intern *ns* (with-meta '*last* {:dynamic true})
                      (:data actual))
         _    (after)]
     (h/signal {:test :check :result (assoc result
                                            :replace rt/*eval-replace*
                                            :compare compare)})
     (when rt/*results*
       (swap! rt/*results* conj result))
     (if (and guard (not (:data result)))
       (h/error "Guard failed" {}))
     result)))

(defn collect
  "makes sure that all returned verified results are true
   (->> (compile/split '[(+ 1 1) => 2
                         (+ 1 2) => 3])
        (mapv process)
        (collect {}))
   => true"
  {:added "3.0"}
  ([meta results]
   (let [results (if rt/*results* @rt/*results* results)]
     (h/signal {:id rt/*run-id* :test :fact :meta meta :results results})
     (and (->> results
               (filter #(-> % :from (= :verify)))
               (mapv :data)
               (every? true?))
          (->> results
               (filter #(and (-> % :from (= :evaluate))
                             (-> % :type (= :exception))))
               (empty?))))))

(defn skip
  "returns the form with no ops evaluated"
  {:added "3.0"}
  ([meta]
   (h/signal {:id rt/*run-id* :test :fact :meta meta :results [] :skipped true}) :skipped))

(defn run-single
  "runs a single check form"
  {:added "3.0"}
  ([{:keys [unit refer] :as meta} body]
   (if (or (match/match-options {:unit unit
                                 :refer refer}
                                rt/*settings*)
           (not rt/*run-id*)
           rt/*eval-mode*)
     (->> (mapv process body)
          (collect meta))
     (skip meta))))
