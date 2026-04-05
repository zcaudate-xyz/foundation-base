(ns code.test.base.listener
  (:require [code.test.base.context :as context]
            [code.test.base.print :as print]
            [code.test.base.runtime :as rt]
            [std.lib.env :as env]
            [std.lib.os :as os]
            [std.lib.signal :as signal]))

(defn- result-name
  [result]
  (str (or (-> result :meta :refer)
           (-> result :meta :function)
           (-> result :meta :desc)
           (-> result :meta :id))))

(defn summarise-verify
  "extract the comparison into a valid format"
  {:added "3.0"}
  ([result]
   #_(env/prfn :VERIFY result)
    {:status    (cond (and (= :success (-> result :status))
                           (= true (-> result :data)))
                      :success

                     (= :timeout (-> result :actual :status))
                     :timeout

                      :else
                      :failed)
     :path     (-> result :meta :path)
     :name     (result-name result)
     :function (-> result :meta :function)
     :ns       (-> result :meta :ns)
     :line     (-> result :meta :line)
    :desc     (-> result :meta :desc)
    :form     (-> result :actual :form)
    :check    (-> result :checker :form)
    :checker  (-> result :checker)
    :actual   (-> result :actual)
    :data     (-> result :actual :data)
    :parent   (-> result :meta :parent-form)}))

(defn summarise-evaluate
  "extract the form into a valid format"
  {:added "3.0"}
  ([result]
   #_(env/prfn :EVAL result)
    {:status   (-> result :status)
     :path     (-> result :meta :path)
     :name     (result-name result)
     :function (-> result :meta :function)
     :ns       (-> result :meta :ns)
     :line     (-> result :meta :line)
    :desc     (-> result :meta :desc)
    :form     (-> result :form)
    :original (-> result :original)
    :data     (-> result :data)}))

(defn form-printer
  "prints out result for each form"
  {:added "3.0"}
  ([{:keys [result]}]
   (let [summary (summarise-evaluate result)]
     (cond (-> result :status (= :exception))
           (when (context/*print* :print-throw)
             (when (not (context/*print* :no-beep))
               (os/beep))
             (print/print-throw summary))

           (-> result :status (= :timeout))
           (when (context/*print* :print-timeout)
             (when (not (context/*print* :no-beep))
               (os/beep))
             (print/print-timeout summary))))))

(defn check-printer
  "prints out result per check"
  {:added "3.0"}
  ([{:keys [result]}]
   (let [summary (summarise-verify result)]
     (cond (= :timeout (-> result :actual :status))
           (when (context/*print* :print-timeout)
             (when (not (context/*print* :no-beep))
               (os/beep))
             (print/print-timeout summary))

           (or (and (-> result :status (= :exception)))
               (and (-> result :data (= false))))
           (when (context/*print* :print-failed)
             (when (not (context/*print* :no-beep))
               (os/beep))
             (print/print-failed (summarise-verify result)))

           (and (-> result :data (= true))
                (context/*print* :print-success))
           (print/print-success (summarise-verify result))))))

(defn form-error-accumulator
  "accumulator for thrown errors"
  {:added "3.0"}
  ([{:keys [result]}]
   (when context/*errors*
     (if (-> result :status (= :exception))
       (swap! context/*errors* update-in [:exception] conj result))

     (if (-> result :status (= :timeout))
       (swap! context/*errors* update-in [:timeout] conj result)))))

(defn check-error-accumulator
  "accumulator for errors on checks"
  {:added "3.0"}
  ([{:keys [result]}]
   (when context/*errors*
     (if (or (-> result :status (= :exception))
             (-> result :data (= false)))
       (swap! context/*errors* update-in [:failed] conj result))

     (if (= :timeout (-> result :actual :status))
       (swap! context/*errors* update-in [:timeout] conj result)))))

(defn fact-printer
  "prints out results after every fact"
  {:added "3.0"}
  ([{:keys [meta results skipped]}]
   (if (and (context/*print* :print-facts)
            (not skipped))
     (print/print-fact meta results))))

(defn fact-accumulator
  "accumulator for fact results"
  {:added "3.0"}
  ([{:keys [id meta results]}]
   (reset! context/*accumulator* {:id id :meta meta :results results})))

(defn bulk-printer
  "prints out the end summary"
  {:added "3.0"}
  ([{:keys [results]}]
   (if (context/*print* :print-bulk)
     (print/print-summary results))))

(defn install-listeners
  "installs all listeners"
  {:added "3.0"}
  ([]
   (do (signal/signal:install :test/form-printer  {:test :form}  #'form-printer)
       (signal/signal:install :test/check-printer {:test :check} #'check-printer)
       (signal/signal:install :test/form-error-accumulator {:test :form} #'form-error-accumulator)
       (signal/signal:install :test/check-error-accumulator {:test :check} #'check-error-accumulator)
       (signal/signal:install :test/fact-printer {:test :fact} #'fact-printer)
       (signal/signal:install :test/fact-accumulator {:test :fact} #'fact-accumulator)
       (signal/signal:install :test/bulk-printer {:test :bulk} #'bulk-printer)
       true)))
