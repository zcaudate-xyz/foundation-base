(ns code.framework.docstring
  (:require [std.string :as str]
            [code.edit :as nav]
            [std.block :as block]
            [std.lib.zip :as zip]))

(defn strip-quotes
  "utility that strips quotes when not the result of a fact
 
   (strip-quotes [\"\\\"hello\\\"\"])
   => [\"hello\"]
 
   (strip-quotes [\"(str \\\"hello\\\")\" \" \" \"=>\" \" \" \"\\\"hello\\\"\"])
   => [\"(str \\\"hello\\\")\" \" \" \"=>\" \" \" \"\\\"hello\\\"\"]"
  {:added "3.0"}
  ([arr] (strip-quotes arr nil nil []))
  ([[x & more] p1 p2 out]
   (cond (nil? x)
         out

         :else
         (recur more x p1 (conj out (if (= p2 "=>")
                                      (if (str/has-quotes? x)
                                        (str/escape-newlines x)
                                        x)
                                      (str/strip-quotes x)))))))

(defn ->refstring
  "creates a refstring for use in html blocks
 
   (->> (nav/parse-root \"\\\"hello\\\"\\n  (+ 1 2)\\n => 3\")
        (iterate zip/step-right)
        (take-while zip/get)
        (map zip/get)
        (->refstring))
   => \"\\\"hello\\\"\\n  (+ 1 2)\\n => 3\""
  {:added "3.0"}
  ([docs]
   (->> docs
        (map (fn [node]
               (let [res (block/string node)]
                 (cond (and (not (block/void? node))
                            (not (block/comment? node))
                            (string? (block/value node)))
                       (str/escape-newlines res)

                       :else res))))
        (str/joinl))))

(defn ->docstring-tag
  "converts a string representation of block
 
   (->docstring-tag (block/block \\c))
   => \"'c'\""
  {:added "3.0"}
  ([block]
   (cond (= :string (block/tag block))
         (block/string block)

         (= :char (block/tag block))
         (str "'" (str (block/value block)) "'")

         :else
         (block/string block))))

(defn ->docstring
  "converts nodes to a docstring
 
   (->> (nav/parse-root \"\\n  (+ 1 2)\\n => 3\")
        (nav/down)
        (iterate zip/step-right)
        (take-while zip/get)
        (map zip/get)
        (->docstring))
   => \"\\n  (+ 1 2)\\n => 3\"
 
   (->docstring [(block/block \\e)])
   => \"'e'\""
  {:added "3.0"}
  ([nodes]
   (->> nodes
        (mapv (fn [block]
                (-> (->docstring-tag block)
                    (str/escape-escapes)
                    (str/escape-quotes))))
        (strip-quotes)
        (str/joinl))))

(defn append-node
  "Adds node as well as whitespace and newline on right
 
   (-> (nav/parse-string \"(+)\")
       (zip/step-inside)
       (append-node 2)
       (append-node 1)
       (nav/root-string))
   => \"(+\\n  1\\n  2)\""
  {:added "3.0"}
  ([nav node]
   (if node
     (-> nav
         (zip/step-right)
         (zip/insert-token-to-right node)
         (zip/insert-token-to-right (block/space))
         (zip/insert-token-to-right (block/space))
         (zip/insert-token-to-right (block/newline))
         (zip/step-left))
     nav)))

(defn ->docstring-node
  "converts nodes to a docstring node
 
   (->> (nav/navigator [\\e \\d \\newline])
        (zip/step-inside)
        (iterate zip/step-right)
        (take-while zip/get)
        (map zip/get)
        (->docstring-node \"\")
        (block/value))
   => \"'e' 'd' '\\n '\""
  {:added "3.0"}
  ([intro nodes]
   (let [docstring  (->docstring nodes)
         docstring  (-> (str intro docstring)
                        (str/trim))
         docstring  (str/split-lines docstring)]
     (->> docstring
          (map-indexed (fn [i s]
                         (str (if-not (or (zero? i)
                                          (= i (dec (count nodes))))
                                " ")
                              s)))
          (str/join "\n")
          (block/block)))))

(defn insert-docstring
  "inserts the meta information and docstring from tests"
  {:added "3.0"}
  ([nav nsp gathered]
   (let [sym   (nav/value nav)
         intro (get-in gathered [nsp sym :intro])
         nodes (get-in gathered [nsp sym :test :code])
         meta  (get-in gathered [nsp sym :meta])
         docstring  (if (or intro nodes)
                      (->docstring-node intro nodes))
         nav  (cond-> nav
                meta (append-node meta)
                docstring (append-node docstring))]
     nav)))

