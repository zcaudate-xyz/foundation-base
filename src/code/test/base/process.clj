(ns code.test.base.process
  (:require [code.test.base.context :as context]
            [code.test.base.match :as match]
            [code.test.base.runtime :as rt]
            [code.test.checker.common :as checker]
            [std.lib.foundation :as f]
            [std.lib.result :as res]
            [std.lib.signal :as signal]))

(def ^:private +skip-forms+
  '#{quote do let let* loop loop* recur fn fn* if if-not when when-not
     when-let when-first if-let cond case try catch finally comment})

(defn evaluate
  "converts a form to a result
 
   (->> (evaluate {:form '(+ 1 2 3)})
        (into {}))
  => (contains {:status :success, :data 6, :form '(+ 1 2 3), :from :evaluate})"
  {:added "3.0"}
  ([{:keys [form value meta]}]
   (if value
     value
     (let [eval-fn  (fn []
                      (try
                        {:status :success :data (rt/eval-in-ns (:ns meta) form)}
                        (catch Throwable t
                          {:status :exception :data t})))
           f    (future (eval-fn))
           out  (deref f context/*timeout* {:status :timeout :data context/*timeout*})
            _    (when (= (:status out) :timeout)
                   (future-cancel f))]
        (res/result (assoc out :type :code/test :form form :from :evaluate))))))

(defn infer-function
  "infers the target function from a form"
  {:added "4.1"}
  ([form]
   (cond (seq? form)
         (let [head (first form)]
           (cond (= 'quote head) nil
                 (and (symbol? head)
                      (not (+skip-forms+ head))) head
                 :else (some infer-function (rest form))))

         (map? form)
         (or (some infer-function (keys form))
             (some infer-function (vals form)))

         (coll? form)
         (some infer-function form))))

(defn attach-meta
  "attaches metadata to the result"
  {:added "4.1"}
  ([result meta form]
   (let [function (or (:function meta)
                      (:refer meta)
                      (infer-function form))
         meta     (cond-> (or meta {})
                    function (assoc :function function))]
     (assoc result :meta meta))))

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
                    (attach-meta meta form)
                    (assoc :original original))
          _    (intern *ns* (with-meta '*last* {:dynamic true})
                       result)]
      (signal/signal {:test :form :result result})
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
                       (attach-meta meta (:form input)))
          _    (intern *ns* (with-meta '*last* {:dynamic true})
                       (:data actual))
          _    (after)]
     (signal/signal {:test :check :result result})
     (when context/*results*
       (swap! context/*results* conj result))
     (if (and guard (not (:data result)))
       (f/error "Guard failed" {}))
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
     (signal/signal {:id context/*run-id* :test :fact :meta meta :results results})
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
   (signal/signal {:id context/*run-id* :test :fact :meta meta :results [] :skipped true}) :skipped))

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
