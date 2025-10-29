(ns code.maven.package-test
  (:use code.test)
  (:require [code.maven.package :refer :all]
            [code.link :as linkage]
            [std.fs :as fs]
            [code.project :as project]
            [std.html :as html]))

(def -collected- (linkage/collect))

^{:refer code.maven.package/pom-properties :added "3.0"}
(fact "creates a pom.properties file"

  (pom-properties (project/project))
  ;; "# foundation.deploy\n# Sun Oct 14 22:57:11 CST 2018\nversion=3.0.1\ngroupId=foundation\nartifactId=base"
  => string?)

^{:refer code.maven.package/coordinate->tree :added "3.0"}
(fact "creates a coordinate tree entry"

  (coordinate->tree '[xyz.zcaudate/std.lib "0.1.1"])
  => [:dependency
      [:groupId "xyz.zcaudate"]
      [:artifactId "std.lib"]
      [:version "0.1.1"]])

^{:refer code.maven.package/repository->tree :added "3.0"}
(fact "creates a repository tree entry"

  (repository->tree ["clojars" "https://clojars.org/repo"])
  => [:repository
      [:id "clojars"]
      [:url "https://clojars.org/repo"]
      [:snapshots [:enabled "true"]]
      [:releases [:enabled "true"]]])

^{:refer code.maven.package/license->tree :added "3.0"}
(fact "creates a license tree entry"

  (license->tree {:name "MIT" :url "http/mit.license"})
  => [:license
      [:name "MIT"]
      [:url "http/mit.license"]])

^{:refer code.maven.package/pom-xml :added "3.0"}
(fact "generates the XML content for a Maven Project Object Model (POM) file")
  ^:hidden

  (->> (pom-xml '{:description "task execution of and standardization",
                  :name xyz.zcaudate/std.task,
                  :artifact "std.task",
                  :group "xyz.zcaudate",
                  :version "3.0.1",
                  :dependencies [[xyz.zcaudate/std.lib "3.0.1"]]})
       (html/tree))
  => (contains
      [[:modelversion "4.0.0"]
       [:packaging "jar"]
       [:groupid "xyz.zcaudate"]
       [:artifactid "std.task"]
       [:version "3.0.1"]
       [:name "xyz.zcaudate/std.task"]
       [:description "task execution of and standardization"]
       [:dependencies
        [:dependency 
         [:groupid "xyz.zcaudate"] 
         [:artifactid "std.lib"] 
         [:version "3.0.1"]]]]
      :gaps-ok))

^{:refer code.maven.package/project-clj-content :added "4.0"}
(fact "generates the project-clj text"
  ^:hidden
  
  (project-clj-content '{:description "task execution of and standardization",
                         :name xyz.zcaudate/std.task,
                         :artifact "std.task",
                         :group "xyz.zcaudate",
                         :version "3.0.1",
                         :dependencies [[xyz.zcaudate/std.lib "3.0.1"]]})
  => string?)

^{:refer code.maven.package/generate-manifest :added "3.0"}
(fact "creates a manifest.mf file for the project"

  (generate-manifest "test-scratch" {:simulate true})
  => "MANIFEST.MF")

^{:refer code.maven.package/generate-pom :added "3.0"}
(fact "generates a pom file given an entry"
  ^:hidden
  
  (-> (generate-pom {:artifact "std.task"
                     :group "xyz.zcaudate"
                     :version "3.0.1"}
                    "test-scratch")
      (update-in [0] html/tree)) ^:hidden
  => (contains
      [(contains [:project
                  [:packaging "jar"]
                  [:groupid "xyz.zcaudate"]
                  [:artifactid "std.task"]
                  [:version "3.0.1"]]
                 :gaps-ok)
       "META-INF/maven/xyz.zcaudate/std.task/pom.xml"
       "META-INF/maven/xyz.zcaudate/std.task/pom.properties"]))

^{:refer code.maven.package/generate-project-clj :added "4.0"}
(fact "generates the project.clj")

^{:refer code.maven.package/linkage :added "3.0"}
(fact "returns the linkage for a given name"
  
  (linkage 'xyz.zcaudate/std.lib nil -collected- nil)
  => (contains {:name 'xyz.zcaudate/std.lib}))

^{:refer code.maven.package/package :added "3.0"}
(fact "returns the packaged items for a given name"

  (package 'xyz.zcaudate/std.image {} -collected-
           {:root "." :version "3.0.1"
            :license {:name "MIT License", :url "http://opensource.org/licenses/MIT"}})
  
  => (contains {:package 'xyz.zcaudate/std.image,
                :jar "xyz.zcaudate-std.image-3.0.1.jar",
                :pom "xyz.zcaudate-std.image-3.0.1.pom.xml",
                :interim (str (fs/file "./target/interim/xyz.zcaudate/std.image")),
                :files (contains ["MANIFEST.MF"
                                  "META-INF/maven/xyz.zcaudate/std.image/pom.xml"
                                  "META-INF/maven/xyz.zcaudate/std.image/pom.properties"])}))

^{:refer code.maven.package/infer :added "3.0"}
(fact "returns the infered items for a given name"

  (infer 'xyz.zcaudate/std.image nil -collected-  {:root "." :version "3.0.1"})
  => (contains {:package 'xyz.zcaudate/std.image,
                :jar "xyz.zcaudate-std.image-3.0.1.jar",
                :pom "xyz.zcaudate-std.image-3.0.1.pom.xml",
                :interim (str (fs/file "./target/interim/xyz.zcaudate/std.image"))}))

(comment
  (./code:import)
  (.replaceAll "\n\n" "\\s" ""))
