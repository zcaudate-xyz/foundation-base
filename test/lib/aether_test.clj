(ns lib.aether-test
  (:require [lib.aether :refer :all])
  (:use code.test))

^{:refer lib.aether/artifact->dependency :added "3.0"}
(fact "converts an artifact to a dependency"

  (artifact->dependency '[org.clojure/clojure "1.8.0" :scope "compile" :exclusions [org.asm/compile]])
  ;; #dep{:artifact "org.clojure:clojure:jar:1.8.0",
  ;;      :exclusions ["org.asm:compile:jar:"],
  ;;      :optional false, :scope "compile", :optional? false}
  => org.eclipse.aether.graph.Dependency)

^{:refer lib.aether/populate-artifact :added "3.0"}
(fact "allows coordinate to fill rest of values"
  
  (populate-artifact '[midje "1.6.3"]
                     {:artifacts [{:extension "pom"
                                   :file "midje.pom"}
                                  {:extension "jar"
                                   :file "midje.jar"}]})
  => {:artifacts [{:extension "pom",
                   :file "midje.pom",
                   :artifact "midje",
                   :group "midje",
                   :version "1.6.3"}
                  {:extension "jar",
                   :file "midje.jar",
                   :artifact "midje",
                   :group "midje",
                   :version "1.6.3"}]})

^{:refer lib.aether/collect-dependencies :added "3.0"}
(fact "getting the dependencies of a repo using pom files"

  (collect-dependencies '[prismatic/schema "1.1.3"] {:type :coord})
  => '[[prismatic/schema "1.1.3"]])

^{:refer lib.aether/resolve-dependencies :added "3.0"}
(fact "resolves maven dependencies for a set of coordinates"

  (resolve-dependencies '[prismatic/schema "1.1.3"] {:type :coord})
  => '[[prismatic/schema "1.1.3"]]

  (vec (sort (resolve-dependencies '[midje "1.6.3"] {:type :coord})))
  =>  '[[clj-time/clj-time "0.6.0"]
        [colorize/colorize "0.1.1"]
        [commons-codec/commons-codec "1.9"]
        [dynapath/dynapath "0.2.0"]
        [gui-diff/gui-diff "0.5.0"]
        [joda-time/joda-time "2.2"]
        [midje/midje "1.6.3"]
        [net.cgrand/parsley "0.9.1"]
        [net.cgrand/regex "1.1.0"]
        [ordered/ordered "1.2.0"]
        [org.clojars.trptcolin/sjacket "0.1.3"]
        [org.clojure/core.unify "0.5.2"]
        [org.clojure/math.combinatorics "0.0.7"]
        [org.clojure/tools.macro "0.1.5"]
        [org.clojure/tools.namespace "0.2.4"]
        [slingshot/slingshot "0.10.3"]
        [swiss-arrows/swiss-arrows "1.0.0"]
        [utilize/utilize "0.2.3"]])

^{:refer lib.aether/install-artifact :added "3.0"}
(comment "installs artifacts to the given coordinate"

  (install-artifact
   '[im.chit/jvm.artifact "2.4.8"]
   {:artifacts [{:file "jvm.artifact-2.4.8.jar"
                 :extension "jar"}
                {:file "jvm.artifact-2.4.8.pom"
                 :extension "pom"}]}))

^{:refer lib.aether/deploy-artifact :added "3.0"}
(comment "deploys artifacts to the given coordinate"

  (deploy-artifact
   '[hara/jvm.artifact "2.4.8"]
   {:artifacts [{:file "jvm.artifact-2.4.8.jar"
                 :extension "jar"}
                {:file "jvm.artifact-2.4.8.pom"
                 :extension "pom"}
                {:file "jvm.artifact-2.4.8.pom.asc"
                 :extension "pom.asc"}
                {:file "jvm.artifact-2.4.8.jar.asc"
                 :extension "jar.asc"}]
    :repository {:id "clojars"
                 :url "https://clojars.org/repo/"
                 :authentication {:username "zcaudate"
                                  :password "hello"}}}))

^{:refer lib.aether/pull :added "3.0"}
(fact "resolves the coordinate from maven and loads dependency into classpath"

  (pull '[[joda-time "2.9"]]) ^:hidden
  => (contains {:artifacts sequential?
                :unloaded sequential?
                :loaded sequential?}))

^{:refer lib.aether/push :added "3.0"}
(fact "gets rid of a dependency that is not needed"

  (push '[[joda-time "2.9"]]) ^:hidden
  => (contains {:artifacts sequential?
                :unloaded sequential?
                :cleaned sequential?})

  (pull '[[joda-time "2.10"]]))

^{:refer lib.aether/resolve-versions :added "3.0"}
(fact "checks for given version of artifacts"

  (resolve-versions '[[lein-monolith "LATEST"]
                      [org.clojure/clojure "LATEST"]])
  => (contains-in [{:group "lein-monolith",
                    :artifact "lein-monolith",
                    :version string?}
                   {:group "org.clojure",
                    :artifact "clojure",
                    :version string?}]))

^{:refer lib.aether/outdated? :added "3.0"}
(fact "checks if a set of artifacts are outdated"

  (outdated? '[[binaryage/devtools "0.9.7"]])
  => (contains-in [['binaryage/devtools "0.9.7" '=> string?]]))

(comment
  (jvm.artifact/loaded-artifact? '[midje "1.9.2"]))

(comment
  (install-artifact
   '[hara/hara.stuff "2.4.10"]
   {:artifacts [{:file "project.clj"
                 :extension "project"}
                {:file "README.md"
                 :extension "readme"}]})

  (deploy-artifact
   '[zaudate/hara.stuff "2.4.10"]
   {:artifacts [{:file "project.clj"
                 :extension "project"}
                {:file "README.md"
                 :extension "readme"}]
    :repository {:id "hara"
                 :url "https://maven.hara.io"
                 :authentication
                 {:username "hara"
                  :password "hara"}}}))
