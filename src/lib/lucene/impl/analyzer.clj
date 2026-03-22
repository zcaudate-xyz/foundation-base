(ns lib.lucene.impl.analyzer
  (:require [std.lib.invoke :refer [definvoke]])
  (:import (org.apache.lucene.analysis Analyzer) (org.apache.lucene.analysis.standard StandardAnalyzer) (org.apache.lucene.analysis.core KeywordAnalyzer) (org.apache.lucene.analysis CharArraySet) (org.apache.lucene.analysis.miscellaneous PerFieldAnalyzerWrapper)))

(defn analyzer-char-set
  "creates an analyzer char set
 
   (analyzer-char-set [])
   => #{}"
  {:added "3.0"}
  (^CharArraySet [stop-words]
   (analyzer-char-set stop-words false))
  (^CharArraySet [^java.util.Collection stop-words ^Boolean ignore-case]
   (CharArraySet. stop-words ignore-case)))

(defmulti analyzer
  "creates an analyzer
 
   (analyzer {:type :standard})
   => org.apache.lucene.analysis.standard.StandardAnalyzer"
  {:added "3.0"}
  :type)

(definvoke analyzer-standard
  "creates a standard analyzer
 
   (analyzer-standard {:stop-words [\"and\"]
                       :ignore-case false})
   => org.apache.lucene.analysis.standard.StandardAnalyzer"
  {:added "3.0"}
  [:method {:multi analyzer
            :val :standard}]
  ([{:keys [stop-words ignore-case]}]
   (if stop-words
     (StandardAnalyzer. (analyzer-char-set stop-words (or ignore-case false)))
     (StandardAnalyzer.))))

(definvoke analyzer-keyword
  "creates a keyword analyzer
 
   (analyzer-keyword {})
   => org.apache.lucene.analysis.core.KeywordAnalyzer"
  {:added "3.0"}
  [:method {:multi analyzer
            :val :keyword}]
  ([_]
   (KeywordAnalyzer.)))

(definvoke analyzer-field
  "creates a field analyzer
 
   (analyzer-field {:type :field
                    :default {:type :standard
                              :stop-words []}
                    :fields {:time {:type :keyword}}})
   => org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper"
  {:added "3.0"}
  [:method {:multi analyzer
            :val :field}]
  ([{:keys [default fields]}]
   (PerFieldAnalyzerWrapper. (analyzer default)
                             (reduce-kv (fn [out k m]
                                          (assoc out (name k) (analyzer m)))
                                        {}
                                        fields))))
