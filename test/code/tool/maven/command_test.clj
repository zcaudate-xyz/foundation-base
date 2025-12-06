(ns code.tool.maven.command-test
  (:use code.test)
  (:require [std.config :as config]
            [std.config.ext.gpg :as gpg]
            [code.tool.maven.command :refer :all]
            [code.tool.maven.package :as package]
            [code.framework.link :as linkage]
            [code.project :as project]
            [lib.aether :as aether]
            [jvm.artifact :as artifact]))

(def -key- (config/resolve [:include "config/keys/test@test.com" {:type :gpg}]))

(def -collected- (linkage/collect))

^{:refer code.tool.maven.command/sign-file :added "1.2"}
(fact "signs a file with gpg"

  (sign-file {:file "test-data/code.tool.maven/maven.edn" :extension "edn"}
             -key-)
  => {:file "test-data/code.tool.maven/maven.edn.asc"
      :extension "edn.asc"})

^{:refer code.tool.maven.command/create-digest :added "1.2"}
(fact "creates a digest given a file and a digest type"

  (create-digest "MD5"
                 "md5"
                 {:file "test-data/code.tool.maven/maven.edn"
                  :extension "edn"})
  => {:file "test-data/code.tool.maven/maven.edn.md5", :extension "edn.md5"})

^{:refer code.tool.maven.command/add-digest :added "1.2"}
(fact "adds MD5 and SHA1 digests to all artifacts"

  (add-digest [{:file "test-data/code.tool.maven/maven.edn"
                :extension "edn"}])
  => [{:file "test-data/code.tool.maven/maven.edn.md5",
       :extension "edn.md5"}
      {:file "test-data/code.tool.maven/maven.edn",
       :extension "edn"}])

^{:refer code.tool.maven.command/clean :added "3.0"}
(fact "cleans the interim directory"

  (def -args- ['xyz.zcaudate/std.object nil -collected- (project/project)])

  (apply package/package -args-)

  (apply clean -args-))

^{:refer code.tool.maven.command/prepare-artifacts :added "3.0"}
(fact "prepares the files for deployment"
  ^:hidden
  
  (prepare-artifacts {:pom "test-data/code.tool.maven/sample.pom"
                      :jar "test-data/code.tool.maven/sample.jar"
                      :interim ""}
                     {:secure true :digest true}
                     {:deploy {:signing {:key -key-}}})
  => coll?)

^{:refer code.tool.maven.command/install :added "3.0"}
(fact "installs a package to the local `.m2` repository"

  (install 'xyz.zcaudate/std.object
           {}
           -collected-
           (assoc (project/project) :version "3.0.1" :aether (aether/aether)))
  => (contains [artifact/rep? artifact/rep?]) ^:hidden
  
  (install 'xyz.zcaudate/std.object
           {:secure true :digest true}
           -collected-
           (assoc (project/project)
                  :version "3.0.1"
                  :aether (aether/aether)
                  :deploy {:signing {:key -key-}}))
  => vector?)

^{:refer code.tool.maven.command/deploy :added "3.0"}
(comment "deploys a package to a test repo"

  (deploy 'xyz.zcaudate/std.lib
          {:tag :dev}   ;;(read-config)
          -collected-
          (assoc (project/project)
                 :deploy (config/load "config/deploy.edn")
                 :aether (aether/aether))))

(comment)
