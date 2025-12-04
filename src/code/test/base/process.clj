(ns code.test.base.process
  (:require [code.test.checker.common :as checker]
            [code.test.base.match  :as match]
            [code.test.base.context :as context]
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
   (if value
     value
     (let [eval-fn  (fn []
                      (try
                        {:status :success :data (eval form)}
                        (catch Throwable t
                          {:status :exception :data t})))
           f    (future (eval-fn))
           out  (deref f context/*timeout* {:status :timeout :data context/*timeout*})
           _    (when (= (:status out) :timeout)
                  (future-cancel f))]
       (res/result (assoc out :type :code/test :form form :from :evaluate))))))

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

(defmethod process :form
  ([{:keys [form original meta] :as op}]
   (let [result (-> (evaluate op)
                    (assoc :meta meta
                           :original original))
         _    (intern *ns* (with-meta '*last* {:dynamic true})
                      result)]
     (h/signal {:test :form :result result})
     (when context/*results*
       (swap! context/*results* conj result))
     result)))

(defmethod process :test-equal
  ([{:keys [input output meta] :as op}]
   (let [{:keys [guard before after]} context/*eval-check*
         _ (before)
         actual   (evaluate input)
         expected (evaluate output)
         checker  (assoc (checker/->checker (res/result-data expected))
                         :form (:form expected))
         result   (-> (checker/verify checker actual)
                      (assoc :meta meta))
         _    (intern *ns* (with-meta '*last* {:dynamic true})
                      (:data actual))
         _    (after)]
     (h/signal {:test :check :result result})
     (when context/*results*
       (swap! context/*results* conj result))
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
   (let [results (if context/*results* @context/*results* results)]
     (h/signal {:id context/*run-id* :test :fact :meta meta :results results})
     (and (->> results
               (filter #(-> % :from (= :verify)))
               (mapv :data)
               (every? true?))
          (->> results
               (filter #(and (-> % :from (= :evaluate))
                             (-> % :status #{:exception
                                             :timeout})))
               (empty?))))))

(defn skip-check
  "returns the form with no ops evaluated"
  {:added "3.0"}
  ([meta]
   (h/signal {:id context/*run-id* :test :fact :meta meta :results [] :skipped true}) :skipped))

(defn run-check
  "runs a single check form"
  {:added "3.0"}
  ([{:keys [unit refer] :as meta} body]
   (let [timeout (or (:timeout meta) context/*timeout-global*)]
     (binding [context/*timeout* timeout
               context/*results* (atom [])]
       (if (or (match/match-options {:unit unit
                                     :refer refer}
                                    context/*settings*)
               (not context/*run-id*)
               context/*eval-mode*)
         (->> (mapv process body)
              (collect meta))
         (skip-check meta))))))
