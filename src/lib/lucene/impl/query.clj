(ns lib.lucene.impl.query
  (:require [lib.lucene.impl.analyzer :as analyzer]
            [std.lib.foundation :as f])
  (:import (org.apache.lucene.queryparser.classic QueryParser) (org.apache.lucene.analysis Analyzer) (org.apache.lucene.search BooleanClause BooleanClause$Occur BooleanQuery BooleanQuery$Builder BoostQuery ConstantScoreQuery DisjunctionMaxQuery Query WildcardQuery) (org.apache.lucene.index Term) (org.apache.lucene.util QueryBuilder)))

(defprotocol FormParsable
  (parse-form [form opts]))

(defn parse-form-query
  "parses a query
 
   (parse-form-query :<query> nil)
   => :<query>"
  {:added "3.0"}
  ([query opts]
   query))

(defn parse-form-seq
  "parses a sequential form"
  {:added "3.0"}
  ([seq opts]
   (let [qb (BooleanQuery$Builder.)]
     (doseq [q (keep #(parse-form % opts) seq)]
       (.add qb q BooleanClause$Occur/MUST))
     (.build qb))))

(defn parse-form-set
  "parses a set form"
  {:added "3.0"}
  ([set opts]
   (let [qb (BooleanQuery$Builder.)]
     (doseq [q (keep #(parse-form % opts) set)]
       (.add qb q BooleanClause$Occur/SHOULD))
     (.build qb))))

(defn parse-form-map
  "parses a map form"
  {:added "3.0"}
  ([map opts]
   (let [qb (BooleanQuery$Builder.)]
     (doseq [q (keep (fn [[k v]] (parse-form v (assoc opts :key k))) map)]
       (.add qb q BooleanClause$Occur/MUST))
     (.build qb))))

(defn ^Query parse-string
  "helper for `parse-form-string`
 
   (parse-string -analyzer- \"id\" \"hello\")
   ;; #object[org.apache.lucene.search.TermQuery 0x487193be \"id:hello\"]
   => org.apache.lucene.search.TermQuery"
  {:added "3.0"}
  ([^Analyzer analyzer ^String default-field-name ^String query-string]
   (let [^QueryParser qp (QueryParser. default-field-name analyzer)]
     (doto qp
       (.setSplitOnWhitespace true)
       (.setAutoGeneratePhraseQueries true))
     (.parse qp query-string))))

(defn parse-form-string
  "parses a string"
  {:added "3.0"}
  ([^String s {:keys [^QueryBuilder builder mode key] :as opts}]
   (let [^String k (f/strn key)]
     (case mode
       :query (.createBooleanQuery builder k s)
       :phrase-query (.createPhraseQuery builder k s)
       :wildcard-query (WildcardQuery. (Term. k s))
       :qp-query (parse-string (.getAnalyzer builder) k s)
       (throw (ex-info (str "Invalid mode " mode) {:mode mode}))))))

(extend-protocol FormParsable
  Query
  (parse-form [query _]
    (parse-form-query query nil))

  clojure.lang.Sequential
  (parse-form [seq opts]
    (parse-form-seq seq opts))

  clojure.lang.IPersistentSet
  (parse-form [set opts]
    (parse-form-set set opts))

  clojure.lang.IPersistentMap
  (parse-form [map opts]
    (parse-form-map map opts))

  String
  (parse-form [s opts]
    (parse-form-string s opts)))

(defn ^Query query
  "compiles a query
 
   (query #{{:id \"hello\"} {:id \"world\"}})
   ;; #object[org.apache.lucene.search.BooleanQuery 0x3dd55e15 \"(+id:world) (+id:hello)\"]
   => org.apache.lucene.search.BooleanQuery"
  {:added "3.0"}
  ([form]
   (query form {}))
  ([form {:keys [analyzer mode] :as opts
          :or {analyzer (analyzer/analyzer {:type :standard})
               mode :query}}]
   (parse-form form {:builder (QueryBuilder. analyzer)
                     :mode mode
                     :key nil})))

(comment
  (parse [{:id "hello"}]))
