(ns code.test.base.listener
  (:require [code.test.base.runtime :as rt]
            [code.test.base.print :as print]
            [std.lib :as h]))

(defn summarise-verify
  "extract the comparison into a valid format"
  {:added "3.0"}
  ([result]
   {:status    (if (and (= :success (-> result :status))
                        (= true (-> result :data)))
                 :success
                 :failed)
    :path     (-> result :meta :path)
    :name     (-> result :meta :refer)
    :ns       (-> result :meta :ns)
    :line     (-> result :meta :line)
    :desc     (-> result :meta :desc)
    :form     (-> result :actual :form)
    :check    (-> result :checker :form)
    :actual   (-> result :actual :data)
    :replace  (-> result :replace)
    :compare  (-> result :compare)
    :original (-> result :original)
    :parent   (-> result :meta :parent-form)}))

(defn summarise-evaluate
  "extract the form into a valid format"
  {:added "3.0"}
  ([result]
   {:status   (-> result :status)
    :path     (-> result :meta :path)
    :name     (-> result :meta :refer)
    :ns       (-> result :meta :ns)
    :line     (-> result :meta :line)
    :desc     (-> result :meta :desc)
    :form     (-> result :form)
    :actual   (-> result :data)
    :replace  (-> result :replace)
    :original (-> result :original)
    :parent   (-> result :meta :parent-form)}))

(defn form-printer
  "prints out result for each form"
  {:added "3.0"}
  ([{:keys [result]}]
   (when (and (-> result :status (= :exception))
              (print/*options* :print-thrown))
     (h/beep)
     (print/print-thrown (summarise-evaluate result)))))

(defn check-printer
  "prints out result per check"
  {:added "3.0"}
  ([{:keys [result]}]
   (when (or (and (-> result :status (= :exception))
                  (print/*options* :print-failure))
             (and (-> result :data (= false))
                  (print/*options* :print-failure)))
     (h/beep)
     (print/print-failure (summarise-verify result))) (if (and (-> result :data (= true))
                                                               (print/*options* :print-success))
                                                        (print/print-success (summarise-verify result)))))

(defn form-error-accumulator
  "accumulator for thrown errors"
  {:added "3.0"}
  ([{:keys [result]}]
   (when rt/*errors*
     (if (-> result :status (= :exception))
       (swap! rt/*errors* update-in [:exception] conj result)))))

(defn check-error-accumulator
  "accumulator for errors on checks"
  {:added "3.0"}
  ([{:keys [result]}]
   (when rt/*errors*
     (if (or (-> result :status (= :exception))
             (-> result :data (= false)))
       (swap! rt/*errors* update-in [:failed] conj result)))))

(defn fact-printer
  "prints out results after every fact"
  {:added "3.0"}
  ([{:keys [meta results skipped]}]
   (if (and (print/*options* :print-facts)
            (not skipped))
     (print/print-fact meta results))))

(defn fact-accumulator
  "accumulator for fact results"
  {:added "3.0"}
  ([{:keys [id meta results]}]
   (reset! rt/*accumulator* {:id id :meta meta :results results})))

(defn bulk-printer
  "prints out the end summary"
  {:added "3.0"}
  ([{:keys [results]}]
   (if (print/*options* :print-bulk)
     (print/print-summary results)) (when rt/*errors*
                                      (h/local :println "-------------------------")
                                      (when-let [failed (:failed @rt/*errors*)]
                                        (doseq [result failed]
                                          (print/print-failure (summarise-verify result))))
                                      (when-let [exceptions (:exception @rt/*errors*)]
                                        (doseq [result exceptions]
                                          (print/print-thrown (summarise-evaluate result))))
                                      (h/local :println ""))))

(defn install-listeners
  "installs all listeners"
  {:added "3.0"}
  ([]
   (do (h/signal:install :test/form-printer  {:test :form}  form-printer)
       (h/signal:install :test/check-printer {:test :check} check-printer)
       (h/signal:install :test/form-error-accumulator {:test :form} form-error-accumulator)
       (h/signal:install :test/check-error-accumulator {:test :check} check-error-accumulator)
       (h/signal:install :test/fact-printer {:test :fact} fact-printer)
       (h/signal:install :test/fact-accumulator {:test :fact} fact-accumulator)
       (h/signal:install :test/bulk-printer {:test :bulk} bulk-printer)
       true)))
