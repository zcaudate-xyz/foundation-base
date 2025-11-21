(ns code.file-task
  (:require [std.task :as task]
            [std.lib :as h]
            [std.fs :as fs]))

(defn- process-single-file
  [file-path opts lookup env]
  (let [file (fs/file file-path)]
    {:path file-path
     :size (.length file)}))

(defmethod task/task-defaults :file.process
  ([_]
   {:construct {:input   (fn [opts]
                           (h/prn opts))
                :lookup  (fn [opts env]
                           (h/prn opts env)
                           (fs/list "./src/code/dev" {:recursive true
                                                      :include [".clj$"]}))
                :env     (fn [opts] (h/prn opts)
                           {:root "."})}
    :params    {:print {:item true
                        :result true
                        :summary true}
                :return :summary}
    :arglists '([] [root-dir] [root-dir params])
    :main      {:arglists '([] [key] [key params] [key params lookup] [key params lookup env])
                :count 4}
    :item      {:list    (fn [lookup env]
                           (keys lookup))}
    :result    {:columns [{:key :path :length 80}
                          {:key :size :length 10 :align :right}]}
    :summary   {:aggregate {:total-size [[:size] + 0]}}}))

(h/definvoke process-files
  "processes all files in a directory"
  {:added "4.0"}
  [:task {:template :file.process
          :params {:title "PROCESS FILES"}
          :main {:fn #'process-single-file}}])

(comment
  (process-files :all)
  )

(comment
  ;; To run this example, you can load this namespace and then run:
  ;; (process-files "src")
  ;; or to process the current directory:
  ;; (process-files)
  


  (defmethod task/task-defaults :publish
    ([_]
     {:construct {:input    (fn [_] :list)
                  :lookup   (fn [_ project]
                              (executive/all-pages project))
                  :env      make-project}
      :params    {:print {:item true
                          :result true
                          :summary true}
                  :return :summary}
      :main      {:arglists '([] [key] [key params])
                  :count 2}
      :item      {:list     (fn [lookup _] (sort (keys lookup)))
                  :display  (fn [data] (format "%.2f s" (/ (:time data) 1000.0)))}
      :result    {:keys    {:path :path
                            :updated :updated}
                  :columns [{:key    :key
                             :align  :left}
                            {:key    :updated
                             :align  :left
                             :length 10
                             :color  #{:bold}}
                            {:key    :path
                             :align  :left
                             :length 60
                             :color  #{:green}}]}
      :summary  {:written   [:updated #(if %2 (inc %1) %1) 0]}})))
