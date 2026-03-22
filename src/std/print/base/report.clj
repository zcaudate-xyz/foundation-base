(ns std.print.base.report
  (:require [std.lib.env]
            [std.print.format :as format]))

(defn print-header
  "prints a header for the row
 
   (-> (print-header [:id :name :value]
                     {:padding 0
                      :spacing 1
                     :columns [{:align :right :length 10}
                                {:align :center :length 10}
                                {:align :left :length 10}]})
       (print/with-out-str))"
  {:added "3.0"}
  ([keys {:keys [padding columns] :as params}]
   (std.lib.env/local :println (str (format/report:header keys params) "\n"))))

(defn print-title
  "prints the title
 
   (-> (print-title \"Hello World\")
       (print/with-out-str))"
  {:added "3.0" :tags #{:print}}
  ([title]
   (std.lib.env/local :println (format/report:title title))))

(defn print-subtitle
  "prints the subtitle
 
   (-> (print-subtitle \"Hello Again\")
       (print/with-out-str))"
  {:added "3.0"}
  ([subtitle]
   (std.lib.env/local :println (format/report:bold subtitle))))

(defn print-row
  "prints a row to output"
  {:added "3.0" :tags #{:print}}
  ([row params]
   (std.lib.env/local :println (format/report:row row params))))

(defn print-column
  "prints the column
 
   (-> (print-column [[:id.a {:data 100}] [:id.b {:data 200}]]
                     :data
                     #{})
      (print/with-out-str))"
  {:added "3.0"}
  ([items name color]
   (std.lib.env/local :println (format/report:column items name color))))

(defn print-summary
  "outputs the summary of results
 
   (-> (print-summary {:count 6 :files 2})
       (print/with-out-str))"
  {:added "3.0" :tags #{:print}}
  ([m]
   (std.lib.env/local :println (format/report:bold (str "SUMMARY " m)))))

(defn print-tree-graph
  "outputs the result of `format-tree`
 
   (-> (print-tree-graph '[{a \"1.1\"}
                           [{b \"1.2\"}
                            [{c \"1.3\"}
                             {d \"1.4\"}]]])
       (print/with-out-str))
   => string?"
  {:added "3.0"}
  ([tree]
   (std.lib.env/local :println (format/tree-graph tree))))
