(ns code.doc
  (:require [code.doc.executive :as executive]
            [code.doc.manage :as manage]
            [code.project :as project]
            [std.config :as config]
            [std.lib.invoke :as invoke]
            [std.lib.result :as res]
            [std.task :as task]))

(def +config+ "config/publish.edn")

(defn make-project
  "makes a env for the publish task
 
   (make-project)
   => map?"
  {:added "3.0"}
  ([]
   (make-project nil))
  ([_]
   (let [project (project/project)]
     (assoc project
            :lookup  (project/file-lookup project)
            :publish (config/load (or (config/resolve (:publish project))
                                      +config+))))))

(defn make-audit-project
  "makes an env for code.doc coverage tasks"
  {:added "4.1"}
  ([]
   (make-audit-project nil))
  ([_]
   (let [project (make-project)]
     (assoc project
            :code.doc/source-namespaces (manage/source-namespaces project)
            :code.doc/coverage (manage/documented-coverage project)))))

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
    :main      {:arglists '([] [key] [key params] [key params project] [key params lookup project])
                :count 4}
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
    :summary  {:written   [:updated #(if %2 (inc %1) %1) 0]}}))

(invoke/definvoke publish
  "main publish method
 
   (publish 'hara/hara-code {})"
  {:added "3.0"}
  [:task {:template :publish
          :params {:title "PUBLISHING HTML FILES"
                   :parallel true}
          :main {:fn #'executive/render}}])

(defmethod task/task-defaults :code.doc.theme
  ([_]
   {:construct {:input    (fn [_] :list)
                :lookup   (fn [_ project]
                            (-> project :publish :sites))
                :env      make-project}
    :params    {:print {:item true
                        :result true
                        :summary true}
                :return :summary}
    :arglists '([] [site] [site params] [site params project] [site params lookup project])
    :main      {:count 4}
    :item      {:list     (fn [lookup _] (sort (keys lookup)))
                :display (fn [data] {:files (count data)})}
    :result    {:keys    {:files count}
                :columns [{:key    :key
                           :align  :left}
                          {:key    :files
                           :align  :left
                           :length 10
                           :color  #{:bold}}]}
    :summary  {:aggregate {:total [:files + 0]}}}))

(invoke/definvoke init-template
  "initialises the theme template for a given site
 
   (init-template \"hara\")"
  {:added "3.0"}
  [:task {:template :code.doc.theme
          :params {:title "INITIALISE TEMPLATE"}
          :main {:fn #'executive/init-template}}])

(invoke/definvoke deploy-template
  "deploys the theme for a given site
  
   (deploy-template \"hara\")"
  {:added "3.0"}
  [:task {:template :code.doc.theme
          :params {:title "DEPLOY TEMPLATE ASSETS"}
          :main {:fn #'executive/deploy-template}}])

(defmethod task/task-defaults :code.doc.audit
  ([_]
   {:construct {:input  (fn [_] *ns*)
                :lookup (fn [_ project] (:code.doc/coverage project))
                :env    make-audit-project}
    :params    {:print {:item true
                        :result true
                        :summary true}}
    :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
    :main      {:count 4}
    :item      {:list    (fn [_ project] (:code.doc/source-namespaces project))
                :pre     project/sym-name
                :display (fn [data]
                           (if (empty? data)
                             (res/result {:status :info
                                          :data :documented})
                             data))}
    :result    {:ignore empty?
                :keys {:count count
                       :data  identity}
                :columns [{:key    :key
                           :align  :left}
                          {:key    :count
                           :format "(%s)"
                           :length 8
                           :align  :center
                           :color  #{:bold}}
                          {:key    :data
                           :align  :left
                           :length 80
                           :color  #{:yellow}}]}
    :summary   {:aggregate {:total [:count + 0]}}}))

(invoke/definvoke missing
  "checks for namespaces not yet referenced by code.doc pages
  
   (missing)
  
   (missing '[code.doc] {:print {:result false :summary false}
                         :return :all})"
  {:added "4.1"}
  [:task {:template :code.doc.audit
          :params {:title "MISSING CODE.DOC NAMESPACES"}
          :main {:fn #'manage/missing-namespaces}}])

(comment
  ;; Currently have to change `config/code.doc.edn` manually
  (publish '[core] {:write true})
  (publish '[core])
  
  (require '[hara.deploy])
  (./code:incomplete '[hara])
  (hara.deploy/deploy '[hara] {:tag :all})
  (deploy-template [:core] {})
  (publish :all {:write true})
  
  (init-template "bolton" {:write true})
  (def lookup (-> env :publish :sites))

  (lookup :hara)
  (sort (keys lookup)))
