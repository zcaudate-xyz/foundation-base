(ns code.doc.render.util
  (:require [clojure.string]
            [markdown.core :as markdown]
            [std.string.common :as common]))

(defn adjust-indent
  "adjusts indents of multiple lines
 
   (adjust-indent \"\\n    a\\n    b\\n    c\"
                  2)
   => \"\\n  a\\n  b\\n  c\""
  {:added "3.0"}
  ([s spaces]
   (->> (clojure.string/split-lines s)
        (map (fn [line]
               (if (and (< spaces (count line))
                        (re-find #"^\s+$" (subs line 0 spaces)))
                 (subs line spaces)
                 line)))
        (clojure.string/join "\n"))))

(defn basic-html-escape
  "escape html tags for output
 
   (basic-html-escape \"<>\")
   => \"&lt;&gt;\""
  {:added "3.0"}
  ([data]
   (clojure.string/escape data {\< "&lt;" \> "&gt;" \& "&amp;" \" "&quot;" \\ "&#92;"})))

(defn basic-html-unescape
  "unescape html output for rendering"
  {:added "3.0"}
  ([^String data]
   (let [out (-> data
                 (.replaceAll "&amp;quot;" "&quot;")
                 (.replaceAll "&amp;lt;" "&lt;")
                 (.replaceAll "&amp;gt;" "&gt;")
                 (.replaceAll "&amp;amp;" "&amp;"))]
     out)))

(defn join-string
  "join string in the form of vector or string
 
   (join-string \"hello\") => \"hello\"
 
   (join-string [\"hello\" \" \" \"world\"]) => \"hello world\""
  {:added "3.0"}
  ([data]
   (cond (string? data)
         data

         (vector? data)
         (common/joinl data))))

(defn markup
  "creates html from markdown script
 
   (markup \"#title\")
   => \"<h1>title</h1>\""
  {:added "3.0"}
  ([data]
   (markdown/md-to-html-string data)))
