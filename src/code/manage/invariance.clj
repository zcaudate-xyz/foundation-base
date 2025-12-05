(ns code.manage.invariance
  (:require [std.block.parse :as parse]
            [std.block.base :as base]
            [std.block.type :as type]
            [std.lib :as h]
            [code.project :as project]))

(def special-forms
  #{"def" "defn" "defn-" "let" "if" "if-let" "when" "when-let"
    "cond" "case" "loop" "recur" "fn" "try" "catch" "finally"
    "throw" "do" "quote" "var" "." ".." "->" "->>" "doto"})

(defn normalize
  "normalizes a block for invariance calculation"
  {:added "4.0"}
  ([block]
   (let [tag   (base/block-tag block)
         type  (base/block-type block)
         str-val (base/block-string block)]
     (cond
       ;; Ignore whitespace/comments
       (or (type/void-block? block)
           (type/comment-block? block))
       nil

       ;; Containers
       (base/container? block)
       (let [children (base/block-children block)
             norm-children (vec (keep normalize children))]
         (if (= tag :list)
           (let [head (first norm-children)
                 special? (and (map? head)
                               (= (:type head) :token)
                               (contains? special-forms (:val head)))]
             (if special?
               {:type :list :head (:val head) :children norm-children}
               {:type :list :head :_ :children norm-children}))
           {:type tag :children norm-children}))

       ;; Tokens
       (= type :token)
       (if (contains? special-forms str-val)
         {:type :token :val str-val}
         {:type :token :val :_})

       :else
       {:type :unknown :tag tag}))))

(defn score
  "calculates a complexity score for a normalized node"
  {:added "4.0"}
  ([norm-node]
   (if (map? norm-node)
     (reduce + 1 (map score (:children norm-node)))
     0)))

(defn structure-hash
  "calculates a hash for the normalized structure"
  {:added "4.0"}
  ([norm-node]
   (hash norm-node)))

(defn analyse-string
  "analyzes a string of code"
  {:added "4.0"}
  ([s]
   (let [root (parse/parse-root s)
         children (base/block-children root)]
     (for [child children
           :let [norm (normalize child)]
           :when norm]
       {:score (score norm)
        :hash  (structure-hash norm)
        :norm  norm}))))

(defn analyse-file
  "analyzes a file"
  {:added "4.0"}
  ([path]
   (try
     (let [content (slurp path)
           root    (parse/parse-root content)
           children (base/block-children root)]
       (->> children
            (keep (fn [child]
                    (let [norm (normalize child)]
                      (if norm
                        (let [s (score norm)
                              h (structure-hash norm)]
                          {:file (str path)
                           :score s
                           :hash h})))))
            (vec)))
     (catch Throwable t
       (println "Error analysing file:" path (.getMessage t))
       []))))

(defn project-analysis
  "analyzes the entire project"
  {:added "4.0"}
  ([]
   (let [files (project/all-files ["src" "test"] {:include [".clj$"]})] ;; Explicitly passing paths
     (println "Found" (count files) "Clojure files.")
     (let [analysis (->> (vals files)
                         (mapcat analyse-file)
                         (vec))]
       (println "Analysis complete. " (count analysis) " forms processed.")
       analysis))))

(defn -main []
  (let [analysis (project-analysis)]
    (h/p (take 10 analysis))))
