(ns script.markdown
  (:require [std.string :as str]
            [clojure.string :as cstr]))

(defn- parse-meta-line [line]
  (let [[k v] (cstr/split line #":" 2)]
    (when (and k v)
      [(keyword (str/trim k)) (str/trim v)])))

(defn parse-metadata
  "parses markdown metadata

   (parse-metadata \"title: hello\\n\\n# hello\")
   => {:title [\"hello\"]}"
  {:added "3.0"}
  ([text]
   (let [lines (str/split-lines text)
         meta-block (take-while #(re-find #"^[\w-]+\s*:" %) lines)]
     (if (seq meta-block)
       (reduce (fn [acc line]
                 (let [[k v] (parse-meta-line line)]
                   (if k
                     (update acc k (fnil conj []) v)
                     acc)))
               {}
               meta-block)
       {}))))

(defn- header? [line]
  (re-find #"^#+\s+" line))

(defn- parse-header [line]
  (let [[_ hashes content] (re-matches #"^(#+)\s+(.*)$" line)
        level (count hashes)]
    (str "<h" level ">" content "</h" level ">")))

(defn parse
  "parses markdown to html

   (parse \"# hello\")
   => \"<h1>hello</h1>\""
  {:added "3.0"}
  ([text]
   (let [lines (str/split-lines text)
         has-metadata? (and (seq lines) (re-find #"^[\w-]+\s*:" (first lines)))
         content-lines (if has-metadata?
                         (let [rest-lines (drop-while #(re-find #"^[\w-]+\s*:" %) lines)]
                           (if (and (seq rest-lines) (str/blank? (first rest-lines)))
                             (rest rest-lines)
                             rest-lines))
                         lines)]
     (->> content-lines
          (map (fn [line]
                 (cond
                   (header? line) (parse-header line)
                   (str/blank? line) ""
                   :else line)))
          (str/join "\n")
          (str/trim)))))
