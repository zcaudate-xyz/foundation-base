(ns code.ai.base-summarise
  (:require [std.lib :as h]
            [std.lib.bin :as bin]
            [std.string :as str])
  (:import (java.io FileInputStream)
           (opennlp.tools.tokenize TokenizerModel TokenizerME)
           (opennlp.tools.postag POSModel POSTaggerME)))

;; --- 1. Load Models using Java Interop ---

(defn create-tokenizer
  [& [path]]
  (->> (h/sys:resource (or path "code/ai/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin"))
       (bin/input-stream)
       (new TokenizerModel)
       (new TokenizerME)))

(defn create-pos-tagger
  [& [path]]
  (->> (h/sys:resource (or path "code/ai/opennlp-en-ud-ewt-pos-1.3-2.5.4.bin"))
       (bin/input-stream)
       (new POSModel)
       (new POSTaggerME)))

(def ^:dynamic *tokenizer*
  (delay (create-tokenizer)))

(def ^:dynamic *pos-tagger*
  (delay (create-pos-tagger)))

(defn classify-text
  [text])


(defn find-best-phrase
  "Finds the most frequent keyphrase between min-words and max-words."
  [text min-words max-words]
  (let [             ; Use .(method object args) for Java method calls
        tokens (.tokenize @*tokenizer* text)
        tags   (seq (.tag @*pos-tagger* tokens)) ; .tag also returns an array
        tagged-pairs (map vector tokens tags)] ; Combine into pairs: [["word" "TAG"] ...]
    (std.lib/prn tagged-pairs)
    (->> (get-keyphrases tagged-pairs)
         (map str/lower-case)           ; Normalize to lowercase
         (frequencies) ; -> {"natural language processing" 3, "key task" 2, ...}
         ;; Filter by our desired length
         (filter (fn [[phrase _]]
                   (let [word-count (count (str/split phrase #"\s+"))]
                     (and (>= word-count min-words)
                          (<= word-count max-words)))))
         ;; Find the most frequent one
         (apply max-key (fn [[_ freq]] freq) nil)
         (first)))) ; Get just the phrase

;; --- 3. Run the Example ---

(defn -main []
  (let [long-text "Natural language processing (NLP) is a subfield of artificial intelligence. 
                   Modern NLP algorithms are based on deep learning. 
                   This subfield of artificial intelligence is concerned with computers. 
                   A key task in natural language processing is sentence segmentation. 
                   Another key task is part-of-speech tagging. 
                   Natural language processing is a very exciting field."]

    (println "--- Original Text ---")
    (println long-text)

    (let [best-phrase (find-best-phrase long-text 3 4)]
      (println "\n--- Best 3-4 Word Description (via Java Interop) ---")
      (println best-phrase)
      best-phrase)))
