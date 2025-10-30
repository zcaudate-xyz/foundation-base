(ns lib.lucene.impl.index
  (:require [std.fs :as fs]
            [std.lib :as h :refer [definvoke]]
            [lib.lucene.impl.document :as doc]
            [lib.lucene.impl.query :as query]
            [lib.lucene.impl.analyzer :as analyzer])
  (:import (org.apache.lucene.analysis Analyzer)
           (org.apache.lucene.store NIOFSDirectory ByteBuffersDirectory Directory)
           (org.apache.lucene.search Query)
           (org.apache.lucene.index Term IndexWriter IndexWriterConfig IndexReader DirectoryReader)
           (org.apache.lucene.analysis.standard StandardAnalyzer)
           (org.apache.lucene.analysis.core KeywordAnalyzer)
           (org.apache.lucene.analysis CharArraySet)
           (org.apache.lucene.analysis.miscellaneous PerFieldAnalyzerWrapper)
           (org.apache.lucene.search IndexSearcher ScoreDoc TopDocs)))

(defmulti directory
  "creates a lucene directory (store
 
   (directory {:store :memory})
   => org.apache.lucene.store.ByteBuffersDirectory"
  {:added "3.0"}
  :store)

(definvoke directory-memory
  "creates a ram directory
 
   (directory-memory {})
   => org.apache.lucene.store.ByteBuffersDirectory"
  {:added "3.0"}
  [:method {:multi directory
            :val :memory}]
  ([_]
   (ByteBuffersDirectory.)))

(definvoke directory-disk
  "creates a disk based directory
 
   (directory-disk {:path \"test-scratch/lib.lucene/index\"})
   => org.apache.lucene.store.NIOFSDirectory"
  {:added "3.0"}
  [:method {:multi directory
            :val :disk}]
  ([{:keys [path]}]
   (NIOFSDirectory. (fs/path path))))

(defn writer
  "creates an IndexWriter
 
   (writer (directory {:store :memory}))
   => org.apache.lucene.index.IndexWriter"
  {:added "3.0"}
  (^org.apache.lucene.index.IndexWriter
   [directory]
   (writer directory (analyzer/analyzer {:type :standard})))
  (^org.apache.lucene.index.IndexWriter
   [directory ^Analyzer analyzer]
   (IndexWriter. directory (IndexWriterConfig. analyzer))))

(defn reader
  "creates an IndexReader
 
   (reader (doto (directory {:store :memory})
             (add-entry-directory (analyzer/analyzer {:type :standard})
                                  {}
                                  {:id \"hello\"}
                                  nil)))
   => org.apache.lucene.index.StandardDirectoryReader"
  {:added "3.0"}
  (^org.apache.lucene.index.IndexReader
   [^Directory directory]
   (DirectoryReader/open directory)))

(defn close
  "closes the writer or reader"
  {:added "3.0"}
  ([^Directory directory]
   (.close directory)))

(defmulti add-entry
  "adds an entry to the index"
  {:added "3.0"}
  (fn [store analyser template entry opts] (type store)))

(definvoke add-entry-writer
  "adds an entry given writer"
  {:added "3.0"}
  [:method {:multi add-entry
            :val IndexWriter}]
  ([^IndexWriter writer _ template entry opts]
   (cond (vector? entry)
         (doseq [m entry]
           (.addDocument writer ^Document (doc/from-map m template)))

         (map? entry)
         (.addDocument writer ^Document (doc/from-map entry template)))))

(definvoke add-entry-directory
  "adds an entry given directory"
  {:added "3.0"}
  [:method {:multi add-entry
            :val Directory}]
  ([^Directory index analyzer template entry opts]
   (with-open [w (writer index analyzer)]
     (add-entry-writer w analyzer template entry opts))))

(defn query-term
  "creates a query term
 
   (query-term {:id \"hello\"})
   => org.apache.lucene.index.Term"
  {:added "3.0"}
  ([terms]
   (let [[k v] (first terms)]
     (Term. (h/strn k) (h/strn v)))))

(defmulti update-entry
  "updates an entry to the index"
  {:added "3.0"}
  (fn [store analyser template terms entry opts] (type store)))

(definvoke update-entry-writer
  "updates an entry given writer"
  {:added "3.0"}
  [:method {:multi update-entry
            :val IndexWriter}]
  ([^IndexWriter writer analyzer template terms entry opts]
   (.updateDocument writer
                    (query-term terms)
                    (doc/from-map entry template))))

(definvoke update-entry-directory
  "updates an entry given directory"
  {:added "3.0"}
  [:method {:multi update-entry
            :val Directory}]
  ([^Directory index analyzer template terms entry opts]
   (with-open [w (writer index analyzer)]
     (update-entry-writer w analyzer template terms entry opts))))

(defmulti remove-entry
  "removes an entry to the index"
  {:added "3.0"}
  (fn [store analyzer terms opts] (type store)))

(definvoke remove-entry-writer
  "removes an entry given writer"
  {:added "3.0"}
  [:method {:multi remove-entry
            :val IndexWriter}]
  ([^IndexWriter writer analyzer terms opts]
   (.deleteDocuments writer
                     ^"[Lorg.apache.lucene.search.Query;"
                     (into-array [(query/query terms {:analyzer analyzer})]))))

(definvoke remove-entry-directory
  "removes an entry given directory"
  {:added "3.0"}
  [:method {:multi remove-entry
            :val Directory}]
  ([^Directory index analyzer terms opts]
   (with-open [w (writer index analyzer)]
     (remove-entry-writer w analyzer terms opts))))

(defmulti search
  "search through the index"
  {:added "3.0"}
  (fn [store analyzer terms opts] (type store)))

(definvoke search-reader
  "search using the reader"
  {:added "3.0"}
  [:method {:multi search
            :val IndexReader}]
  ([^IndexReader reader analyzer terms {:keys [mode page-offset page-results max-results]
                                        :or {page-offset 0 max-results 40 page-results 20}}]
   (let [^IndexSearcher searcher (IndexSearcher. reader)
         query (query/query terms {:mode (or mode :query) :analyzer analyzer})
         ^TopDocs hits (.search searcher query (int max-results))
         start (* page-offset page-results)
         end   (min (+ start page-results) (.value (.totalHits hits)) max-results)]
     (with-meta
       (vec
        (for [^ScoreDoc hit (map (partial aget (.scoreDocs hits))
                                 (range start end))]
          (let [doc-id (.doc hit)
                m (doc/to-map (.doc searcher doc-id))
                score (.score hit)]
            (with-meta m {:score score :doc-id doc-id}))))
       {:total-hits (.totalHits hits)}))))

(definvoke search-directory
  "search using the directory"
  {:added "3.0"}
  [:method {:multi search
            :val Directory}]
  ([^Directory index analyzer terms opts]
   (with-open [w (reader index)]
     (search-reader w analyzer terms opts))))


