(ns std.task.unit
  (:require [std.print :as print]
            [std.lib.result :as res]))

(defn process-item
  "processes a single item, abstracted from bulk"
  {:added "4.0"}
  ([{:keys [f idx total input output display display-fn print params lookup env args]}]
   (let [start        (System/currentTimeMillis)
         [key result] (try (apply f input params lookup env args)
                           (catch Throwable e
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
