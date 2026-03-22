(ns lib.jdbc.resultset
  (:require [clojure.string :as str]
            [lib.jdbc.protocol :as protocol])
  (:import java.sql.PreparedStatement java.sql.ResultSetMetaData java.sql.ResultSet))

(defn result-set->lazyseq
  "Function that wraps result in a lazy seq. This function
   is part of public api but can not be used directly (you should pass
   this function as parameter to `query` function).
 
   Required parameters:
     rs: ResultSet instance.
 
   Optional named parameters:
     :identifiers -> function that is applied for column name
                     when as-arrays? is false
     :as-rows?    -> by default this function return a lazy seq of
                     records (map), but in certain circumstances you
                     need results as a lazy-seq of vectors. With this keywork
                     parameter you can enable this behavior and return a lazy-seq
                     of vectors instead of records (maps)."
  {:added "4.0"}
  [conn ^ResultSet rs {:keys [identifiers as-rows? header?]
                       :or {identifiers str/lower-case
                            as-rows? false
                            header? false}
                       :as options}]
  (let [^ResultSetMetaData metadata (.getMetaData rs)
        idseq (range 1 (inc (.getColumnCount metadata)))
        labels (mapv (fn [^long i] (.getColumnLabel metadata i)) idseq)
        keyseq (mapv (comp keyword identifiers) labels)
        values (fn []
                 (mapv (fn [^long i]
                         (-> (.getObject rs i)
                             (protocol/-from-sql-type conn metadata i)))
                       idseq))
        rows (fn thisfn []
               (when (.next rs)
                 (cons (values) (lazy-seq (thisfn)))))
        records (fn thisfn []
                  (when (.next rs)
                    (-> (zipmap keyseq (values))
                        (cons (lazy-seq (thisfn))))))
        header (mapv identifiers labels)]
    (if-not as-rows?
      (records)
      (if-not header?
        (rows)
        (cons header (lazy-seq (rows)))))))

(defn result-set->vector
  "Function that evaluates a result into one clojure persistent
   vector. Accept same parameters as `result-set->lazyseq`."
  {:added "4.0"}
  [conn ^ResultSet rs options]
  (vec (result-set->lazyseq conn rs options)))
