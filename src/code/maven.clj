(ns code.maven
  (:require [code.maven.command :as command]
            [code.maven.package :as package]
            [code.maven.task :as deploy.task]
            [code.maven.lein :as lein]
            [std.lib :refer [definvoke]]
            [std.task :as task]
            [lib.aether :as aether]))

(defmethod task/task-defaults :deploy.maven
  ([_]
   (merge deploy.task/+main+
          {:item      {:list     (fn [lookup _] (vec (sort (keys lookup))))
                       :display  (fn [data] (vec (sort (map :extension data))))}
           :result    {:keys    {:artifacts (fn [data]
                                              (let [{:keys [version group artifact]} (first data)
                                                    extensions (sort (map (comp str :extension) data))]
                                                [group version extensions]))}
                       :columns [{:key    :key
                                  :align  :left}
                                 {:key    :artifacts
                                  :align  :left
                                  :length 60
                                  :color  #{:bold}}
                                 {:key    :time
                                  :align  :left
                                  :length 10
                                  :format "%d ms"
                                  :color  #{:bold}}]}
           :summary  {:aggregate {}}})))

(defmethod task/task-defaults :deploy.linkage
  ([_]
   (merge deploy.task/+main+
          {:item      {:list    (fn [lookup _] (vec (sort (keys lookup))))}
           :result    {:columns [{:key    :key
                                  :align  :left}
                                 {:key    :time
                                  :align  :left
                                  :length 10
                                  :format "%d ms"
                                  :color  #{:bold}}]}
           :summary  {:aggregate {}}})))

(defmethod task/task-defaults :deploy.package
  ([_]
   (merge deploy.task/+main+
          {:item      {:list     (fn [lookup _] (vec (sort (keys lookup))))
                       :display  (juxt (comp count :files) :pom)}
           :result    {:keys    {:jar :jar
                                 :pom :pom
                                 :packaged (comp count :files)}
                       :columns [{:key    :key
                                  :align  :left}
                                 {:key    :packaged
                                  :align  :left
                                  :format "(%d)"
                                  :length 10
                                  :color  #{:bold}}
                                 {:key    :jar
                                  :align  :left
                                  :length 40
                                  :color  #{:bold}}
                                 {:key    :time
                                  :align  :left
                                  :length 10
                                  :format "%d ms"
                                  :color  #{:bold}}]}
           :summary  {:aggregate {:packaged   [:packaged + 0]}}})))

(definvoke linkage
  "creates linkages for project
 
   (linkage :all {:tag :all
                  :print {:item false :result false :summary false}})"
  {:added "3.0"}
  [:task {:template :deploy.linkage
          :params {:title "CREATES ALL LINKAGE FILES"}
          :main {:fn #'package/linkage}}])

(definvoke package
  "packages files in the interim directory
 
   (package '[xyz.zcaudate]
            {:tag :all
            :print {:item true :result false :summary false}})"
  {:added "3.0"}
  [:task {:template :deploy.package
          :params {:title "PACKAGE INTERIM FILES"
                   :parallel true}
          :main {:fn #'package/package}}])

(definvoke infer
  "infers all variables
   
   (infer '[xyz.zcaudate]
            {:tag :all
             :print {:item true :result false :summary false}})"
  {:added "4.0"}
  [:task {:template :deploy.linkage
          :params {:title "INFER INTERIM FILES"
                   :parallel true}
          :main {:fn #'package/infer}}])

(definvoke clean
  "cleans the interim directory of packages
 
   (clean :all {:tag :all
                :print {:item false :result false :summary false}})"
  {:added "3.0"}
  [:task {:template :deploy.linkage
          :params {:title "CLEAN ALL INTERIM FILES"
                   :parallel true}
          :main {:fn #'command/clean}}])

(definvoke install
  "installs packages to the local `.m2` repository
 
   (install '[xyz.zcaudate] {:tag :all :print {:item true}})
   
   (install 'xyz.zcaudate/std.lib
            {:tag :all
             :print {:item true}})"
  {:added "3.0"}
  [:task {:template :deploy.maven
          :params {:title "INSTALL PACKAGES"
                   :parallel true}
          :main {:fn #'command/install}}])

(definvoke deploy
  "deploys packages to a maven repository
 
   (deploy '[xyz.zcaudate] {:tag :all})"
  {:added "3.0"}
  [:task {:template :deploy.maven
          :params {:title "DEPLOY PACKAGES"
                   :parallel true}
          :main {:fn #'command/deploy}}])

(definvoke deploy-lein
  "deploys packages to clojars using lein
   
   (deploy-lein '[xyz.zcaudate] {:tag :all})"
  {:added "4.0"}
  [:task {:template :deploy.linkage
          :params {:title "DEPLOY USING LEIN"
                   :parallel true}
          :main {:fn #'lein/deploy-lein}}])


(comment)
