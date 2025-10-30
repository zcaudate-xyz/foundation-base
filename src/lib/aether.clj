(ns lib.aether
  (:require [std.print :as print]
            [jvm.artifact :as artifact]
            [jvm.classloader :as classloader]
            [jvm.deps :as deps]
            [std.object :as object]
            [lib.aether.base :as base]
            [lib.aether.listener :as listener]
            [lib.aether.request :as request]
            [lib.aether.result :as result]
            [std.lib :as h])
  (:import (org.eclipse.aether.graph Dependency Exclusion)
           (org.eclipse.aether RepositorySystem)))

(h/intern-in base/aether)

(defn artifact->dependency
  "converts an artifact to a dependency
 
   (artifact->dependency '[org.clojure/clojure \"1.8.0\" :scope \"compile\" :exclusions [org.asm/compile]])
   ;; #dep{:artifact \"org.clojure:clojure:jar:1.8.0\",
   ;;      :exclusions [\"org.asm:compile:jar:\"],
   ;;      :optional false, :scope \"compile\", :optional? false}
   => org.eclipse.aether.graph.Dependency"
  {:added "3.0"}
  ([artifact]
   (let [coord (cond (artifact/coord? artifact)
                     artifact

                     (map? artifact)
                     (apply vector (:name artifact) (:version artifact)
                            (mapcat identity (dissoc artifact :name :version)))

                     :else
                     (artifact/artifact :coord artifact))
         [name version & {:keys [scope exclusions]}] coord]
     (object/from-data {:artifact [name version]
                        :scope scope
                        :exclusions exclusions}
                       Dependency))))

(defn populate-artifact
  "allows coordinate to fill rest of values
   
   (populate-artifact '[midje \"1.6.3\"]
                      {:artifacts [{:extension \"pom\"
                                    :file \"midje.pom\"}
                                   {:extension \"jar\"
                                    :file \"midje.jar\"}]})
   => {:artifacts [{:extension \"pom\",
                    :file \"midje.pom\",
                    :artifact \"midje\",
                    :group \"midje\",
                    :version \"1.6.3\"}
                  {:extension \"jar\",
                    :file \"midje.jar\",
                    :artifact \"midje\",
                    :group \"midje\",
                    :version \"1.6.3\"}]}"
  {:added "3.0"}
  ([coord opts]
   (let [root (-> (artifact/artifact coord)
                  (select-keys [:artifact :group :version]))]
     opts (update-in opts [:artifacts]
                     (fn [arr] (mapv #(merge % root) arr))))))

(defn collect-dependencies
  "getting the dependencies of a repo using pom files
 
   (collect-dependencies '[prismatic/schema \"1.1.3\"] {:type :coord})
   => '[[prismatic/schema \"1.1.3\"]]"
  {:added "3.0"}
  ([coords]
   (collect-dependencies coords {}))
  ([coords opts]
   (collect-dependencies (base/aether) coords opts))
  ([{:keys [^RepositorySystem system session repositories]} coords opts]
   (binding [listener/*progress* (atom [])
             listener/*pairs* (atom {})]
     (let [opts   (h/merge-nested {:type :defalut
                                   :return :hierarchy
                                   :print {:hierarchy true
                                           :title true
                                           :timing true}}
                                  opts)
           reqm   (cond (and (sequential? coords)
                             (not (artifact/coord? coords)))
                        {:dependencies (mapv artifact->dependency coords)}

                        :else
                        {:root (artifact->dependency coords)})
           request (request/collect-request
                    (assoc reqm :repositories repositories))]
       (if (-> opts :print :title)
         (print/print-title "COLLECTING DEPENDENCIES")
         (print/print "\n"))
       (-> (.collectDependencies system session request)
           (result/return-deps @listener/*progress* opts))))))

(defn resolve-dependencies
  "resolves maven dependencies for a set of coordinates
 
   (resolve-dependencies '[prismatic/schema \"1.1.3\"] {:type :coord})
   => '[[prismatic/schema \"1.1.3\"]]
 
   (vec (sort (resolve-dependencies '[midje \"1.6.3\"] {:type :coord})))
   =>  '[[clj-time/clj-time \"0.6.0\"]
         [colorize/colorize \"0.1.1\"]
         [commons-codec/commons-codec \"1.9\"]
         [dynapath/dynapath \"0.2.0\"]
         [gui-diff/gui-diff \"0.5.0\"]
         [joda-time/joda-time \"2.2\"]
         [midje/midje \"1.6.3\"]
         [net.cgrand/parsley \"0.9.1\"]
         [net.cgrand/regex \"1.1.0\"]
         [ordered/ordered \"1.2.0\"]
         [org.clojars.trptcolin/sjacket \"0.1.3\"]
         [org.clojure/core.unify \"0.5.2\"]
         [org.clojure/math.combinatorics \"0.0.7\"]
         [org.clojure/tools.macro \"0.1.5\"]
         [org.clojure/tools.namespace \"0.2.4\"]
         [slingshot/slingshot \"0.10.3\"]
        [swiss-arrows/swiss-arrows \"1.0.0\"]
         [utilize/utilize \"0.2.3\"]]"
  {:added "3.0"}
  ([coords]
   (resolve-dependencies coords {}))
  ([coords opts]
   (resolve-dependencies (base/aether) coords opts))
  ([{:keys [^RepositorySystem system session repositories]} coords opts]
   (binding [listener/*progress* (atom [])
             listener/*pairs* (atom {})]
     (let [opts   (h/merge-nested {:type :default
                                   :return :resolved
                                   :print {:hierarchy true
                                           :title true
                                           :timing true}}
                                  opts)
           reqm   (cond (and (sequential? coords)
                             (not (artifact/coord? coords)))
                        {:dependencies (mapv artifact->dependency coords)}

                        :else
                        {:root (artifact->dependency coords)})
           request (request/dependency-request
                    (assoc reqm :repositories repositories))]
       (if (-> opts :print :title)
         (print/print-title "RESOLVING DEPENDENCIES")
         (print/print "\n"))
       (-> (.resolveDependencies system session request)
           (result/return-deps @listener/*progress* opts))))))

(defn install-artifact
  "installs artifacts to the given coordinate
 
   (install-artifact
    '[im.chit/jvm.artifact \"2.4.8\"]
   {:artifacts [{:file \"jvm.artifact-2.4.8.jar\"
                  :extension \"jar\"}
                 {:file \"jvm.artifact-2.4.8.pom\"
                  :extension \"pom\"}]})"
  {:added "3.0"}
  ([coord {:keys [artifacts] :as opts}]
   (install-artifact (base/aether) coord opts))
  ([{:keys [^RepositorySystem system session]} coord {:keys [artifacts] :as opts}]
   (binding [listener/*progress* (atom [])
             listener/*pairs* (atom {})]
     (let [opts   (h/merge-nested {:type :default
                                   :print {:title true
                                           :timing true}}
                                  opts)
           opts (populate-artifact coord opts)
           request (request/install-request opts)]
       (if (-> opts :print :title)
         (print/print-title "INSTALLING ARTIFACTS")
         (print/print "\n"))
       (-> (.install system session request)
           (result/return @listener/*progress* opts))))))

(defn deploy-artifact
  "deploys artifacts to the given coordinate
 
   (deploy-artifact
    '[hara/jvm.artifact \"2.4.8\"]
   {:artifacts [{:file \"jvm.artifact-2.4.8.jar\"
                  :extension \"jar\"}
                 {:file \"jvm.artifact-2.4.8.pom\"
                  :extension \"pom\"}
                 {:file \"jvm.artifact-2.4.8.pom.asc\"
                  :extension \"pom.asc\"}
                 {:file \"jvm.artifact-2.4.8.jar.asc\"
                  :extension \"jar.asc\"}]
     :repository {:id \"clojars\"
                  :url \"https://clojars.org/repo/\"
                  :authentication {:username \"zcaudate\"
                                   :password \"hello\"}}})"
  {:added "3.0"}
  ([coord {:keys [artifacts repository] :as opts}]
   (deploy-artifact (base/aether) coord opts))
  ([{:keys [^RepositorySystem system session]}
    coord
    {:keys [artifacts repository] :as opts}]
   (binding [listener/*progress* (atom [])
             listener/*pairs* (atom {})]
     (let [opts   (h/merge-nested {:type :default
                                   :print {:title true
                                           :timing true}}
                                  opts)
           opts    (populate-artifact coord opts)
           request (request/deploy-request opts)]
       (if (-> opts :print :title)
         (print/print-title "DEPLOYING ARTIFACTS")
         (print/print "\n"))
       (-> (.deploy system session request)
           (result/return @listener/*progress* opts))))))

(defn pull
  "resolves the coordinate from maven and loads dependency into classpath
 
   (pull '[[joda-time \"2.9\"]])"
  {:added "3.0"}
  ([coords]
   (pull coords {}))
  ([coords opts]
   (pull (base/aether opts) coords opts))
  ([{:keys [loader] :as aether} coords {:keys [keep renew nodeps] :as opts}]
   (let [loader    (or loader (classloader/dynamic-classloader))
         coords    (if (or (not (vector? coords))
                           (artifact/coord? coords))
                     [coords]
                     coords)
         coords    (if-not renew (remove #(deps/loaded-artifact? % loader) coords))
         coords    (if nodeps
                     coords
                     (resolve-dependencies aether coords {:print {:hierarchy false}}))
         unloaded  (if-not keep (deps/unload coords loader :different))
         loaded    (deps/load coords loader)]
     {:artifacts coords
      :unloaded unloaded
      :loaded loaded})))

(defn push
  "gets rid of a dependency that is not needed
 
   (push '[[joda-time \"2.9\"]])"
  {:added "3.0"}
  ([coords]
   (push coords {}))
  ([coords opts]
   (push (base/aether) coords opts))
  ([{:keys [loader] :as aether} coords {:keys [clean simulate nodeps] :as opts}]
   (let [loader  (or loader classloader/+base+)
         coords    (if (or (not (vector? coords))
                           (artifact/coord? coords))
                     [coords]
                     coords)
         coords  (if nodeps
                   coords
                   (collect-dependencies aether coords {:return :resolved
                                                        :print {:hierarchy false}}))
         unloaded (if-not simulate (deps/unload coords loader :same))
         cleaned  (mapcat (fn [coord]
                            (case clean
                              :none     nil
                              :artifact (deps/clean coord (assoc opts :full false))
                              :full     (deps/clean coord (assoc opts :full true))
                              nil))
                          coords)]
     {:artifacts coords
      :unloaded unloaded
      :cleaned cleaned})))

(defn resolve-versions
  "checks for given version of artifacts
 
   (resolve-versions '[[lein-monolith \"LATEST\"]
                       [org.clojure/clojure \"LATEST\"]])
   => (contains-in [{:group \"lein-monolith\",
                     :artifact \"lein-monolith\",
                     :version string?}
                    {:group \"org.clojure\",
                     :artifact \"clojure\",
                     :version string?}])"
  {:added "3.0"}
  ([coords]
   (resolve-versions coords {}))
  ([coords opts]
   (resolve-versions (base/aether) coords opts))
  ([{:keys [^RepositorySystem system session repositories]} coords opts]
   (binding [listener/*progress* (atom [])
             listener/*pairs* (atom {})]
     (let [opts   (h/merge-nested {:type :default
                                   :print {:title true
                                           :timing true}}
                                  opts)]
       (if (-> opts :print :title)
         (print/print-title "RESOLVE VERSIONS")
         (print/print "\n"))
       (-> (keep (fn [coord]
                   (let [request (request/version-request {:artifact (artifact/artifact coord)
                                                           :repositories repositories})]
                     (try (.resolveVersion system session request)
                          (catch Throwable t
                            (print/println "Not Resolved:" coord)))))
                 coords)
           (result/return @listener/*progress* opts))))))

(defn outdated?
  "checks if a set of artifacts are outdated
 
   (outdated? '[[binaryage/devtools \"0.9.7\"]])
   => (contains-in [['binaryage/devtools \"0.9.7\" '=> string?]])"
  {:added "3.0"}
  ([coords]
   (outdated? coords {}))
  ([coords opts]
   (outdated? (base/aether) coords opts))
  ([aether coords opts]
   (let [opts    (h/merge-nested opts {:print {:results true}})
         reps    (mapv artifact/artifact coords)
         inputs  (map #(assoc % :version "LATEST") reps)
         latest  (resolve-versions aether inputs opts)
         results (->> (mapv (fn [in new]
                              (if (not= (:version in)
                                        (:version new))
                                [(first (artifact/artifact :coord in)) (:version in)  '=> (:version new)]))
                            reps latest)
                      (filterv identity))]
     (cond-> results
       (and (seq results) (-> opts :print :results)) (doto prn)))))


(comment
  (do 
    (def +base+ (base/aether))
    (def +request+
      (request/deploy-request
        {:type :default,
         :print {:title false, :timing false},
         :artifacts
         [{:file (std.fs/path "/home/chris/Development/tahto/foundation/target/interim/xyz.zcaudate/std.object/foundation-std.object-4.0.1.jar"),
           :extension "jar",
           :artifact "std.object",
           :group "xyz.zcaudate",
           :version "4.0.1"}
          {:file
           (std.fs/path "/home/chris/Development/tahto/foundation/target/interim/xyz.zcaudate/std.object/foundation-std.object-4.0.1.pom.xml"),
           :extension "pom",
           :artifact "std.object",
           :group "xyz.zcaudate",
           :version "4.0.1"}],
         :repository
         {:id "statstrade",
          :url "https://maven.statstrade.io/",
          :authentication
          {:username "zcaudate",
           :password "maven123"}}}))
    (aget (:authentications (.&
                             (.getAuthentication (.getRepository +request+))))
          0)
    (aget (:authentications (.&
                             (.getAuthentication (.getRepository +request+))))
          1)
    (def +deploy+
      (.deploy
       (:system  +base+)
       (:session +base+)
       +request+))))
