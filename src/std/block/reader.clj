(ns std.block.reader
  (:require [clojure.tools.reader.reader-types :as types]
            [std.block.base :as base]
            [std.block.check :as check]
            [std.protocol.block :as protocol.block]
            [std.string :as str])
  (:refer-clojure :exclude [peek next slurp]))

(defn create
  "create reader from string
 
   (type (create \"hello world\"))
   => clojure.tools.reader.reader_types.IndexingPushbackReader"
  {:added "3.0"}
  ([string]
   (-> (types/string-push-back-reader string 2)
       (types/indexing-push-back-reader 2))))

(defn reader-position
  "returns the position of the reader
 
   (-> (create \"abc\")
       step-char
       step-char
       reader-position)
   => [1 3]"
  {:added "3.0"}
  ([reader]
   [(types/get-line-number reader)
    (types/get-column-number reader)]))

(defn throw-reader
  "throws a reader message
 
   (throw-reader (create \"abc\")
                 \"Message\"
                 {:data true})
   => (throws)"
  {:added "3.0"}
  ([reader message & input]
   (let [[row col :as pos] (reader-position reader)
         message (str message (format " [row %s, column %s]" row col))]
     (throw (ex-info message
                     {:data (vec input)
                      :row row
                      :col col})))))

(defn step-char
  "moves reader one char forward
 
   (-> (create \"abc\")
       step-char
       read-char
       str)
   => \"b\""
  {:added "3.0"}
  ([reader]
   (doto reader
     (types/read-char))))

(defn read-char
  "reads single char and move forward
 
   (->> read-char
        (read-repeatedly (create \"abc\"))
        (take 3)
        (apply str))
   => \"abc\""
  {:added "3.0"}
  ([reader]
   (types/read-char reader)))

(defn ignore-char
  "returns nil and moves reader one char forward
 
   (->> ignore-char
        (read-repeatedly (create \"abc\"))
        (take 3)
        (apply str))
   => \"\""
  {:added "3.0"}
  ([reader]
   (types/read-char reader) nil))

(defn unread-char
  "move reader one char back, along with char
 
   (-> (create \"abc\")
       (step-char)
       (unread-char \\A)
       (reader/slurp))
   => \"Abc\""
  {:added "3.0"}
  ([reader ch]
   (doto reader
     (types/unread ch))))

(defn peek-char
  "returns the current reader char with moving
 
   (->> (read-times (create \"abc\")
                    peek-char
                    3)
        (apply str))
   => \"aaa\""
  {:added "3.0"}
  ([reader]
   (types/peek-char reader)))

(defn read-while
  "reads input while the predicate is true
 
   (read-while (create \"abcde\")
               (fn [ch]
                 (not= (str ch) \"d\")))
   => \"abc\""
  {:added "3.0"}
  ([reader pred & [eof?]]
   (let [buf (StringBuilder.)
         eof? (if (nil? eof?)
                (not (pred nil))
                eof?)]
     (loop []
       (if-let [c (read-char reader)]
         (if (pred c)
           (do
             (.append buf c)
             (recur))
           (do
             (unread-char reader c)
             (str buf)))
         (if eof?
           (str buf)
           (throw-reader reader "Unexpected EOF.")))))))

(defn read-until
  "reads inputs until the predicate is reached
 
   (read-until (create \"abcde\")
               (fn [ch]
                 (= (str ch) \"d\")))
   => \"abc\""
  {:added "3.0"}
  ([reader pred]
   (read-while reader
               (complement pred)
               (pred nil))))

(defn read-times
  "reads input repeatedly
 
   (->> (read-times (create \"abcdefg\")
                    #(str (read-char %) (read-char %))
                    2))
   => [\"ab\" \"cd\"]"
  {:added "3.0"}
  ([reader read-fn n]
   (->> (repeatedly #(read-fn reader))
        (take n))))

(defn read-repeatedly
  "reads input repeatedly
 
   (->> (read-repeatedly (create \"abcdefg\")
                         #(str (read-char %) (read-char %))
                         empty?)
        (take 5))
   => [\"ab\" \"cd\" \"ef\" \"g\"]"
  {:added "3.0"}
  ([reader read-fn]
   (read-repeatedly reader read-fn nil?))
  ([reader read-fn stop-fn]
   (->> (repeatedly #(read-fn reader))
        (take-while (complement stop-fn))
        (doall))))

(defn read-include
  "reads up to a given predicate
 
   (read-include (create \"  a\")
                 read-char (complement check/voidspace?))
   => [[\\space \\space] \\a]"
  {:added "3.0"}
  ([reader read-fn]
   (read-repeatedly reader read-fn nil?))
  ([reader read-fn stop-fn]
   (let [main (repeatedly #(read-fn reader))
         toks (take-while (complement stop-fn) main)
         ntoks (count toks)]
     [toks (nth main ntoks)])))

(defn slurp
  "reads rest of input from reader
 
   (reader/slurp (reader/step-char (create \"abc efg\")))
   => \"bc efg\""
  {:added "3.0"}
  ([reader]
   (apply str (read-repeatedly reader read-char))))

(defn read-to-boundary
  "reads to an input boundary
 
   (read-to-boundary (create \"abc efg\"))
   => \"abc\""
  {:added "3.0"}
  ([reader]
   (read-to-boundary reader []))
  ([reader allowed]
   (let [allowed  (set allowed)
         allow-fn (fn [ch]
                    (or (allowed ch)
                        (not (check/voidspace-or-boundary? ch))))]
     (-> reader
         (read-while allow-fn)))))
