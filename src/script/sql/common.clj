(ns script.sql.common
  (:require [clojure.string]
            [std.lib.foundation :as f])
  (:import (java.util ArrayList)))

(defn- ansi-quote [s] (str \" s \"))

(def ^:dynamic *options*
  {:table-fn  (comp ansi-quote str/snake-case h/strn)
   :column-fn (comp ansi-quote str/snake-case h/strn)})

(defn sql:type
  "constructs sql type from `std.lib.schema` type
 
   (sql:type :string)
   => :text"
  {:added "3.0"}
  ([type]
   (case type
     :string  :text
     :long    :bigint
     :enum    :text
     :ref     :text
     :keyword :text
     nil      :text
     type)))

(defn sql:compare
  "implements equal comparator for more numbers
 
   (sql:compare {:a 1}
                {:a (bigdec 1)})
   => true"
  {:added "3.0"}
  ([m1 m2]
   (let [equal-fn (fn [v1 v2]
                    (cond (and (number? v1)
                               (number? v2))
                          (== v1 v2)

                          :else (= v1 v2)))
         changes (h/diff:changes m1 m2 [] equal-fn)]
     (empty? changes))))

(defn sql:parse
  "splits string into strings
 
   (sql:parse \"SELECT ? FROM ?\")
   => [\"SELECT \" \" FROM \" \"\"]"
  {:added "3.0"}
  ([sql]
   (let [sql (clojure.string/trim sql)
         len (count sql)
         v   (ArrayList.)]
     (loop [i        0
            prev-end 0
            in-str?  false
            in-meta? false]
       (if (< i len)
         (let [char (.charAt sql i)]
           (case char
             \' (recur (inc i) prev-end (not in-str?) in-meta?)
             \" (recur (inc i) prev-end in-str? (not in-meta?))
             \? (do (.add v (subs sql prev-end i))
                    (recur (inc i) (inc i) in-str? in-meta?))
             (recur (inc i) prev-end in-str? in-meta?)))
         (doto v
           (.add (subs sql prev-end i))))))))

(defn sql:escape
  "escapes a string for sql usage
 
   (sql:escape \"aoe\")
   => \"aoe\"
 
   (sql:escape \"ao'e\")
   => \"ao''e\""
  {:added "3.0"}
  ([^String sql]
   (-> sql
       (.replaceAll "\\\\" "\\\\\\\\")
       (.replaceAll "\\'" "\\'\\'"))))

(defn sql:entry
  "constructs a quoted entry
 
   (sql:entry \"hello\")
   => \"'hello'\""
  {:added "3.0"}
  ([input]
   (if (nil? input)
     "NULL"
     (f/-> (str input)
           (sql:escape)
           (str \' % \')))))

(defn sql:format
  "formats `?` placeholders with escaped arguments
 
   (sql:format \"SELECT * FROM table WHERE id = ?\"
               \"u-0\")
   => \"SELECT * FROM table WHERE id = 'u-0'\""
  {:added "3.0"}
  ([sql & args]
   (let [entries (sql:parse sql)
         _ (assert (= (dec (count entries)) (count args)))
         args    (map sql:entry args)]
     (->> (interleave entries (concat args [""]))
          (clojure.string/join)))))
