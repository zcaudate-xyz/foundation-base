(ns code.fs-task
  (:require [std.task :as task]
            [std.lib :as h]
            [std.fs :as fs]
            [std.lib.result :as res]
            [std.text.diff :as diff]
            [clojure.string :as str]))

;;;
;;; TEMPLATES
;;;

(defn fs-list-files
  "Helper for listing files, respects :recursive param."
  [root-dir {:keys [recursive] :as params}]
  (filter fs/file? (fs/list root-dir {:recursive recursive})))

(def base-fs-template
  "Base template for filesystem operations."
  {:construct {:input (fn [_] ".")
               :lookup (fn [_ _] {})
               :env    (fn [_] {})}
   :params    {:print {:item true
                       :result true
                       :summary true}
               :recursive true
               :return :summary}
   :arglists '([] [root-dir] [root-dir params])
   :main      {:argcount 1}
   :item      {:list fs-list-files}
   :result    {:columns [{:key :key :length 80}]}
   :summary   {:aggregate {:files [[:key] (fn [acc _] (inc acc)) 0]}}})

(def fs-transform-result
  "Default :result for transform tasks. Shows diff summary."
  {:keys   (fn [result] (dissoc result :deltas))
   :ignore (fn [result] (and (zero? (:inserts result))
                             (zero? (:deletes result))))
   :columns
   [{:key :key :length 60}
    {:key :inserts :length 10 :color #{:green}}
    {:key :deletes :length 10 :color #{:red}}]
   :summary {:aggregate
             {:total-inserts [[:inserts] + 0]
              :total-deletes [[:deletes] + 0]}}})

(def fs-locate-result
  "Default :result for locate tasks. Shows match count and lines."
  {:keys   (fn [result] {:count (count (:matches result))
                         :lines (str/join "," (map :line (:matches result)))}) ; Corrected: escaped comma
   :ignore (fn [result] (empty? (:matches result)))
   :columns
   [{:key :key :length 60}
    {:key :count :length 10 :color #{:yellow}}
    {:key :lines :length 40}]
   :summary {:aggregate {:total-matches [[:count] + 0]}}})


(defmethod task/task-defaults :fs
  ([_] base-fs-template))

(defmethod task/task-defaults :fs.transform
  ([_] 
   (h/merge-nested
    base-fs-template
    {:params {:write true}
     :result fs-transform-result})))

(defmethod task/task-defaults :fs.locate
  ([_] 
   (h/merge-nested
    base-fs-template
    {:result fs-locate-result})))


;;;
;;; TASKS
;;;

(h/definvoke list-files
  "Lists files in a directory."
  {:added "4.0"}
  [:task {:template :fs
          :params {:title "LIST FILES"}
          :main   {:fn (fn [file-path]
                         {:path file-path})}
          :result {:columns [{:key :key :length 100}]}}])


(defn- touch-file-impl
  "Main logic for touching a file."
  [file-path {:keys [write] :as params}]
  (let [original-content (slurp file-path)
        ;; Simulate a change for demonstration
        new-content (str original-content " ")] ; Corrected: escaped space
    (if write
      (spit file-path original-content))
    (diff/summary (diff/diff original-content new-content))))

(h/definvoke touch-files
  "Modifies the timestamp of files in a directory."
  {:added "4.0"}
  [:task {:template :fs.transform
          :params {:title "TOUCH FILES"}
          :main   {:fn touch-file-impl}}])


(defn- grep-files-impl
  "Main logic for grepping a file."
  [file-path {:keys [query] :as params}]
  (let [lines (fs/read-all-lines file-path)]
    {:matches (keep-indexed (fn [i line]
                              (when (and query (str/includes? line query))
                                {:line (inc i) :content line}))
                            lines)}))

(h/definvoke grep-files
  "Finds lines in files matching a query string."
  {:added "4.0"}
  [:task {:template :fs.locate
          :params {:title "GREP FILES"}
          :main   {:fn grep-files-impl}}])

(comment
  ;; To run these examples, load this namespace and then:

  ;; List all files recursively (default) in the 'src' directory
  ;; (list-files "src")

  ;; List files non-recursively
  ;; (list-files "src" {:recursive false})

  ;; Touch files (simulated change) in the current directory
  ;; (touch-files "." {:write false})

  ;; Grep for the string "task" in all files under 'src/code'
  ;; (grep-files "src/code" {:query "task"})
  )