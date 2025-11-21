(ns code.file-task
  (:require [std.task :as task]
            [std.lib :as h]
            [std.fs :as fs]))

(defn print-file
  [path params lookup env]
  #_(h/prn path key lookup env)
  {:path path :params params})

(defmethod task/task-defaults :file.internal
  ([_]
   {:construct {:input   (fn [_] :list)
                :lookup  (fn [opts env]
                           (let [root-path (fs/path (:root env))]
                             (->> (fs/list root-path
                                           env)
                                  (h/map-keys #(str (fs/relativize root-path %))))))
                :env     (fn [_] 
                           {:root "."
                            :include [fs/file?]})}
    :params    {:print {:item true
                        :result true
                        :summary true}
                :return :summary}
    :arglists '([] [path] [path params] [path params lookup] [path params lookup env])
    :main      {:arglists '([] [key] [key params] [key params lookup] [key params lookup env])
                :count 4}
    :item      {:list    (fn [lookup env]
                           (sort (keys lookup)))
                :display (fn [data] (format "%.2f s" (/ (h/time-ms) 1000.0)))}
    :result    {:keys    {:path :path
                          :params :params}
                #_#_:columns [{:key    :key
                             :align  :left}
                          {:key    :updated
                           :align  :left
                           :length 10
                           :color  #{:bold}}
                          {:key    :path
                           :align  :left
                           :length 60
                           :color  #{:green}}]
                :columns [{:key :path :length 50}
                          #_{:key :params :length 10 :align :right}]}}))



(h/definvoke process-files
  "processes all files in a directory"
  {:added "4.0"}
  [:task {:template :file.internal
          :params {:title (fn [params env]
                            (str "PROCESS FILES - " (fs/path (:root env))))}
          :main {:fn #'print-file}}])

(process-files :all)
(comment
  (process-files :all)
  :result    {:keys    {:path :path
                        :params :params}
              :columns [{:key    :key
                         :align  :left}
                        {:key    :path
                         :align  :left
                         :length 60
                         :color  #{:green}}]}
  
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
