(ns lib.aether.request-test
  (:use code.test)
  (:require [lib.aether.request :refer :all]
            [std.object :as object])
  (:import (org.eclipse.aether.graph Dependency
                                     DependencyNode
                                     DefaultDependencyNode)
           (org.eclipse.aether.repository RemoteRepository
                                          RemoteRepository$Builder
                                          RepositoryPolicy)
           (org.eclipse.aether.collection CollectRequest)
           (org.eclipse.aether.deployment DeployRequest)
           (org.eclipse.aether.installation InstallRequest)
           (org.eclipse.aether.resolution ArtifactRequest
                                          DependencyRequest)))

^{:added "3.0"}
(fact "creates a `Dependency` object from map"

  (object/from-data {:artifact '[hara/hara "2.4.8"]}
                    Dependency)
  => (partial instance? Dependency)
  ;;=> #dep{:artifact "hara:hara:jar:2.4.8",
  ;;        :exclusions [],
  ;;        :optional false,
  ;;        :scope "",
  ;;        :optional? false}
  )

^{:added "3.0"}
(fact "creates a `DependencyNode` object from map"

  (object/from-data {:artifact '[hara/hara "2.4.8"]}
                    DependencyNode)
  => (partial instance? DependencyNode)
  ;;=> #dep.node {:children [],
  ;;              :relocations [],
  ;;              :repositories [],
  ;;              :managed-bits 0,
  ;;              :artifact "hara:hara:jar:2.4.8",
  ;;              :aliases [],
  ;;              :request-context "",
  ;;              :data {}}
  )

^{:refer lib.aether.request/artifact-request :added "3.0"}
(fact "creates an `ArtifactRequest` object from map"

  (artifact-request
   {:artifact "hara:hara:2.4.8"
    :repositories [{:id "clojars"
                    :authentication {:username "zcaudate"
                                     :password "hello"}
                    :url "https://clojars.org/repo/"}]})
  => (partial instance? ArtifactRequest)
  ;;=> #req.artifact{:artifact "hara:hara:jar:2.4.8",
  ;;                 :repositories [{:id "clojars",
  ;;                                 :url "https://clojars.org/repo/"
  ;;                                 :authentication {:username "zcaudate", :password "hello"}}],
  ;;                 :request-context ""}
  )

^{:refer lib.aether.request/collect-request :added "3.0"}
(fact "creates a `CollectRequest` object from map"

  (collect-request
   {:root {:artifact "hara:hara:2.4.8"}
    :repositories [{:id "clojars"
                    :url "https://clojars.org/repo/"}]})
  => (partial instance? CollectRequest)
  ;;=> #req.collect{:root {:artifact "hara:hara:jar:2.4.8",
  ;;                       :exclusions [],
  ;;                       :optional false,
  ;;                       :scope "",
  ;;                       :optional? false}
  ;;                :repositories [{:id "clojars",
  ;;                                :url "https://clojars.org/repo/"}]}
  )

^{:refer lib.aether.request/dependency-request :added "3.0"}
(fact "creates a `DependencyRequest` object from map"

  (dependency-request
   {:root {:artifact "hara:hara:2.4.8"}
    :repositories [{:id "clojars"
                    :url "https://clojars.org/repo/"}]})
  => (partial instance? DependencyRequest)
  ;;=> #req.dependency{:root {:artifact "hara:hara:jar:2.4.8",
  ;;                          :exclusions [],
  ;;                          :optional false,
  ;;                          :scope "",
  ;;                          :optional? false}
  ;;                   :repositories [{:id "clojars",
  ;;                                   :url "https://clojars.org/repo/"}]}
  )

^{:refer lib.aether.request/deploy-request :added "3.0"}
(fact "creates a `DeployRequest` object from map"

  (deploy-request
   {:artifacts [{:group "hara"
                 :artifact "std.string"
                 :version "2.4.8"
                 :extension "jar"
                 :file "hara-string.jar"}]
    :repository {:id "clojars"
                 :url "https://clojars.org/repo/"
                 :authentication {:username "zcaudate"
                                  :password "hello"}}})
  => (partial instance? DeployRequest)
  ;;=> #req.deploy{:artifacts ["hara:std.string:jar:2.4.8"]
  ;;               :repository {:id "clojars",
  ;;                            :authentication {:username "zcaudate", :password "hello"}
  ;;                            :url "https://clojars.org/repo/"}}
  )

^{:refer lib.aether.request/install-request :added "3.0"}
(fact "creates a `InstallRequest` object from map"

  (install-request
   {:artifacts [{:group "hara"
                 :artifact "std.string"
                 :version "2.4.8"
                 :extension "jar"
                 :file "hara-string.jar"}
                {:group "hara"
                 :artifact "std.string"
                 :version "2.4.8"
                 :extension "pom"
                 :file "hara-string.pom"}]})
  => (partial instance? InstallRequest)
  ;;=> #req.install{:artifacts ["hara:std.string:jar:2.4.8"
  ;;                            "hara:std.string:pom:2.4.8"]
  ;;                :metadata []}
  )

^{:refer lib.aether.request/metadata-request :added "3.0"}
(fact "constructs a metadat request"

  (metadata-request
   {:metadata   {:group "hara"
                 :artifact "std.string"
                 :version "2.4.8"}
    :repository {:id "clojars"
                 :url "https://clojars.org/repo/"
                 :authentication {:username "zcaudate"
                                  :password "hello"}}})
  => (partial instance? org.eclipse.aether.resolution.MetadataRequest))

^{:refer lib.aether.request/range-request :added "3.0"}
(fact "constructs a range request"

  (range-request {:artifact {:group "hara"
                             :artifact "std.string"
                             :version "2.4.8"}
                  :repositories [{:id "clojars"
                                  :url "https://clojars.org/repo/"
                                  :authentication {:username "zcaudate"
                                                   :password "hello"}}]})
  => (partial instance? org.eclipse.aether.resolution.VersionRangeRequest))

^{:refer lib.aether.request/version-request :added "3.0"}
(fact "constructs a version request"

  (version-request {:artifact {:group "hara"
                               :artifact "std.string"
                               :version "2.4.8"}
                    :repositories [{:id "clojars"
                                    :url "https://clojars.org/repo/"
                                    :authentication {:username "zcaudate"
                                                     :password "hello"}}]})
  => (partial instance? org.eclipse.aether.resolution.VersionRequest))
