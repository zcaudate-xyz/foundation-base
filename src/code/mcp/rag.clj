(ns code.mcp.rag
  (:require [clojure.string :as str]
            [code.mcp.embed :as embed]
            [std.text.index :as text-index]))

(defn create-store
  ([]
   (create-store {}))
  ([{:keys [embedder keyword-index vector-weight keyword-weight]
     :or {embedder (embed/create-hash-embedder)
          keyword-index (text-index/make-index)
          vector-weight 0.8
          keyword-weight 0.2}}]
   {:documents (atom {})
    :embedder embedder
    :keyword-index keyword-index
    :vector-weight vector-weight
    :keyword-weight keyword-weight}))

(defn normalize-document
  [{:keys [id text content metadata] :as document}]
  (let [body (or text content "")]
    {:id (str (or id (java.util.UUID/randomUUID)))
     :text body
     :metadata (or metadata {})
     :document document}))

(defn upsert-document
  [{:keys [documents embedder keyword-index] :as store} document]
  (let [{:keys [id text] :as normalized} (normalize-document document)
        embedding (embed/embed-text embedder text)]
    (text-index/unindex-all keyword-index id)
    (swap! documents assoc id (assoc normalized :embedding embedding))
    (text-index/index-text keyword-index id text)
    (await keyword-index)
    (get @documents id)))

(defn upsert-documents
  [store documents]
  (mapv #(upsert-document store %) documents))

(defn retrieve
  ([store query]
   (retrieve store query {}))
  ([{:keys [documents embedder keyword-index vector-weight keyword-weight]} query {:keys [limit]
                                                                                   :or {limit 5}}]
   (let [query-text (or query "")
         query-embedding (embed/embed-text embedder query-text)
         keyword-scores (into {} (text-index/search keyword-index query-text :or))
         include-all? (str/blank? query-text)]
     (->> @documents
          vals
          (map (fn [{:keys [id text metadata embedding] :as document}]
                 (let [vector-score (embed/cosine-similarity query-embedding embedding)
                       keyword-score (double (get keyword-scores id 0))
                       score (+ (* vector-weight vector-score)
                                (* keyword-weight keyword-score))]
                   {:id id
                    :text text
                    :metadata metadata
                    :score score
                    :vector-score vector-score
                    :keyword-score keyword-score
                    :document document})))
          (filter (fn [{:keys [score]}]
                    (or include-all? (pos? score))))
          (sort-by :score >)
          (take limit)
          vec))))

(defn retrieve-context
  [results]
  (->> results
       (map-indexed (fn [idx {:keys [id text]}]
                      (format "[%s] %s\n%s" (inc idx) id text)))
       (str/join "\n\n")))

(defn query-context
  ([store query]
   (query-context store query {}))
  ([store query opts]
   (retrieve-context (retrieve store query opts))))
