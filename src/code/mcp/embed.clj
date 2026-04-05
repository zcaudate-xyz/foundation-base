(ns code.mcp.embed
  (:require [clojure.string :as str]))

(defprotocol IEmbeddings
  (-embed-texts [this texts opts]))

(defn tokenize
  [text]
  (re-seq #"[A-Za-z0-9_-]+"
          (str/lower-case (or text ""))))

(defn l2-norm
  [values]
  (Math/sqrt (reduce + (map #(* % %) values))))

(defn unit-vector
  [values]
  (let [norm (double (l2-norm values))]
    (if (zero? norm)
      (vec values)
      (mapv #(/ (double %) norm) values))))

(defn cosine-similarity
  [left right]
  (let [pairs (map vector left right)
        numerator (reduce + (map (fn [[l r]] (* (double l) (double r))) pairs))
        denominator (* (double (l2-norm left))
                       (double (l2-norm right)))]
    (if (zero? denominator)
      0.0
      (/ numerator denominator))))

(defn token->bucket
  [dimensions token]
  (mod (Math/abs (long (hash token))) dimensions))

(defrecord HashEmbedder [dimensions]
  IEmbeddings
  (-embed-texts [_ texts _]
    (mapv (fn [text]
            (let [weights (reduce (fn [output token]
                                    (let [idx (token->bucket dimensions token)]
                                      (update output idx (fnil + 0.0) 1.0)))
                                  (vec (repeat dimensions 0.0))
                                  (tokenize text))]
              (unit-vector weights)))
          texts)))

(defn create-hash-embedder
  ([]
   (create-hash-embedder {}))
  ([{:keys [dimensions]
     :or {dimensions 128}}]
   (->HashEmbedder dimensions)))

(defn embed-texts
  ([provider texts]
   (embed-texts provider texts nil))
  ([provider texts opts]
   (cond
     (satisfies? IEmbeddings provider)
     (-embed-texts provider texts opts)

     (fn? provider)
     (mapv provider texts)

     :else
     (throw (ex-info "Unsupported embedding provider"
                     {:provider provider})))))

(defn embed-text
  ([provider text]
   (embed-text provider text nil))
  ([provider text opts]
   (first (embed-texts provider [text] opts))))
