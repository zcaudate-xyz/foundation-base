(ns code.manage.unit.template
  (:require [code.framework :as base]
            [code.project :as project]
            [std.task :as task]
            [std.lib :as h]
            [std.lib.result :as res]))

(defn source-namespaces
  "returns all source namespaces
 
   (count (source-namespaces {} (project/project)))
   ;; 358
   => number?"
  {:added "3.0"}
  ([_ project]
   (let [inclusions [(str (project/file-suffix) "$")]]
     (sort (keys (project/all-files (:source-paths project) {:include inclusions} project))))))

(defn test-namespaces
  "returns all test namespaces
 
   (count (test-namespaces {} (project/project)))
   ;;321
   => number?"
  {:added "3.0"}
  ([_ project]
   (let [inclusions [(str (munge (project/test-suffix))
                          (project/file-suffix)
                          "$")]]
     (sort (keys (project/all-files (:test-paths project) {:include inclusions} project))))))

(defn empty-status
  "constructs a function that outputs on empty
 
   ((empty-status :warn :empty) [])
   ;;#result.warn{:data :empty}
   => std.lib.result.Result"
  {:added "3.0"}
  ([status message]
   (fn [arr]
     (if (empty? arr)
       (res/result {:status status
                    :data message})
       arr))))

(defn code-default-columns
  "creates columns for default code operations"
  {:added "3.0"}
  ([color]
   (code-default-columns :data color))
  ([key color]
   (code-default-columns key color nil))
  ([key color format]
   [{:key    :key
     :align  :left}
    {:key    :count
     :format "(%s)"
     :length 8
     :align  :center
     :color  #{:bold}}
    {:key    key
     :format format
     :align  :left
     :length 80
     :color  color}]))

(def code-info-columns
  (fn
    ([color]
     (code-info-columns :data color))
    ([key color]
     (code-default-columns key color
                           (fn [items]
                             (->> items
                                  (map (juxt (comp #(get % :row 0) meta)
                                             identity))
                                  (sort-by first)
                                  (vec)))))))

(def code-default
  {:construct {:input    (fn [_] *ns*)
               :lookup   (fn [_ project] (project/file-lookup project))
               :env      (fn [_] (project/project))}
   :params    {:print {:result true
                       :summary true}}
   :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
   :main      {:count 4}
   :item      {:list     (fn [lookup _] (sort (keys lookup)))
               :pre      project/sym-name
               :display  (empty-status :info :ok)}
   :result    {:ignore   empty?
               :keys    {:count count}
               :columns (code-default-columns #{:yellow})}
   :summary  {:aggregate {:total [:count + 0]}}})

;;
;;
;;

(defn empty-result
  "constructs a function that outputs on an empty key
 
   ((empty-result :data :warn :empty)
    {:data []})
   ;;#result.warn{:data :empty}
   => std.lib.result.Result"
  {:added "3.0"}
  ([key status message]
   (fn [m]
     (let [result (get m key)]
       (if (empty? result)
         (res/result {:status status
                      :data message})
         result)))))

(defn code-transform-result
  "creates result for code transform operations"
  {:added "3.0"}
  ([key]
   {:ignore (comp empty? key)
    :keys   {:count     (comp count key)
             :functions key}}))

(defn code-transform-columns
  "creates columns for code transform operations"
  {:added "3.0"}
  ([color]
   (code-transform-columns :functions color))
  ([key color]
   (concat [{:key    :key
             :align  :left}]
           (if key
             [{:key    :functions
               :align  :left
               :length 60
               :color  color}])
           [{:key    :inserts
             :length 8
             :align  :center
             :color  #{:bold}}
            {:key    :deletes
             :length 8
             :align  :center
             :color  #{:bold}}
            {:key    :count
             :format "(%s)"
             :length 8
             :align  :center
             :color  #{:bold}}
            {:key    :verified
             :length 20
             :align  :left
             :color  #{:bold}}])))

(def base-transform-result
  {:columns (code-transform-columns nil #{})
   :ignore (fn [{:keys [deletes inserts verify] :as m}]
             (and  (true? verify)
                   (zero? (+ (or inserts 0)
                             (or deletes 0)))))
   :keys {:count (fn [{:keys [deletes inserts] :as m}]
                   (+ (or inserts 0)
                      (or deletes 0)))}})

(def code-transform
  {:construct {:input    (fn [_] *ns*)
               :lookup   (fn [_ project] (project/file-lookup project))
               :env      (fn [_] (project/project))}
   :params    {:print {:item     true
                       :result   true
                       :summary  true}}
   :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
   :main      {:count 4}
   :item      {:list    source-namespaces
               :pre      project/sym-name
               :display (empty-result :changed :info :no-change)}
   :result    {:ignore empty?
               :keys {:count count
                      :inserts   :inserts
                      :deletes   :deletes
                      :updated   :updated
                      :verified   :verified}
               :columns (code-transform-columns #{:bold :cyan})}
   :summary  {:aggregate {:deletes   [:deletes + 0]
                          :inserts   [:inserts + 0]
                          :written   [:updated #(if %2 (inc %1) %1) 0]
                          :total     [:count + 0]}}})

(defn line-string
  "constructs a line string"
  {:added "3.0"}
  ([arr]
   (if (empty? arr)
     :empty
     (mapv (comp #(apply format "(%s-%s)" %) (juxt :row :end-row)) arr))))

(defn line-count
  "calculates number of lines"
  {:added "3.0"}
  ([arr]
   (->> arr
        (map (fn [{:keys [row end-row]}]
               (inc (- end-row row))))
        (apply +))))

(def code-locate
  {:construct {:input    (fn [_] *ns*)
               :lookup   (fn [_ project] (project/file-lookup project))
               :env      (fn [_] (project/project))}
   :params    {:print {:function true
                       :item     false
                       :result   true
                       :summary  true}}
   :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
   :main {:count 4}
   :item {:list source-namespaces
          :pre      project/sym-name
          :display line-string}
   :result {:ignore empty?
            :keys {:source line-string
                   :count line-count}
            :columns (code-default-columns :source #{:cyan :bold})}
   :summary  {:aggregate {:total [:count + 0]}}})
