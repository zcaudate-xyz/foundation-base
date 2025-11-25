(ns std.text.index
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [std.text.index.stemmer :as stemmer]
            [std.text.index.porter :as porter]))

(defrecord Index [data stemmer])

(defn make-index
  "creates a index for search
 
   (make-index)"
  {:added "3.0"}
  ([]
   (make-index porter/stem))
  ([stemmer-func]
   (agent (Index. {} stemmer-func))))

  ;; ## Functions that take stems


(defn add-entry
  "helper function for `index`"
  {:added "3.0"}
  ([index stem key weight]
   (send index assoc-in [:data stem key] weight)))

(defn remove-entries
  "helper function for `unindex"
  {:added "3.0"}
  ([index key stems]
   (send index
         (fn [{:keys [data] :as m}]
           (assoc m :data
                  (->> (set stems)
                       (reduce (fn [data stem]
                                 (if-let [stem-val (get data stem)]
                                   (let [stem-val (dissoc stem-val key)]
                                     (if (seq stem-val)
                                       (assoc data stem stem-val)
                                       (dissoc data stem)))
                                   data))
                               data)))))))

;; ## Functions that take blocks of text

(defn index-text
  "adds text to index"
  {:added "3.0"}
  ([index key txt weight]
   (index-text index key [[txt 1]]))
  ([index key data]
   (->>
    (let [data (if (coll? data) data [[data 1]])]
      (for [[text-block multiplier] data]
        (into {}
              (->> (stemmer/stems text-block (:stemmer @index))
                   (frequencies)
                   (map (fn [[stem num]] [stem (* num multiplier)]))))))
    (apply merge-with +)
    (map (fn [[stem num]] (add-entry index stem key num)))
    (dorun))))

(defn unindex-text
  "removes text from index"
  {:added "3.0"}
  ([index key txt]
   (remove-entries index key (stemmer/stems txt (:stemmer @index))) nil))

(defn unindex-all
  "clears index"
  {:added "3.0"}
  ([index key]
   (send index
         (fn [{:keys [data] :as m}]
           (assoc m :data
                  (into {}
                        (keep (fn [[stem docs]]
                                (let [docs (dissoc docs key)]
                                  (when (seq docs)
                                    [stem docs]))))
                        data))))))

(defn query
  "queries index for results"
  {:added "3.0"}
  ([index query-string]
   (let [all-stems (stemmer/stems query-string (:stemmer @index))]
     (into {}
           (for [stem all-stems]
             [stem (get-in @index [:data stem])])))))

(defn merge-and
  "merges results using and"
  {:added "3.0"}
  ([query-results]
   (let [ids (set (mapcat (comp keys second) query-results))
         all-vals (vals query-results)]
     (into {}
           (filter #(> (second %) 0)
                   (for [id ids]
                     [id (apply *
                                (for [result-set all-vals]
                                  (get result-set id 0)))]))))))

(defn merge-or
  "merges results using or"
  {:added "3.0"}
  ([query-results]
   (apply merge-with + (vals query-results))))

(defn search
  "searchs index for text"
  {:added "3.0"}
  ([index term]
   (search index term :and))
  ([index term merge-style]
   (let [merge-func (if (= merge-style :and) merge-and merge-or)]
     (reverse (sort-by second (merge-func (query index term)))))))

;; ## Disk persistence

(defn save-index
  "saves index to file"
  {:added "3.0"}
  ([index] ; save to previous filename
   (if-let [file (:file-storage-name @index)]
     (binding [*out* (io/writer file)]
       (clojure.core/prn (:data @index)))
     (throw (Exception. "No filename specified")))
   index)
  ([index filename-or-file] ; save to specified filename
   (send index assoc :file-storage-name filename-or-file)
   (await index)
   (save-index index)))

(defn load-index
  "loads index from file"
  {:added "3.0"}
  ([index filename-or-file]
   (send index assoc :data (edn/read-string (slurp filename-or-file))) (send index assoc :file-storage-name filename-or-file)))

