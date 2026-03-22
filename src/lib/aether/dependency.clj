(ns lib.aether.dependency
  (:require [jvm.artifact :as jvm.artifact]
            [jvm.protocol :as protocol.classloader]
            [lib.aether.artifact :as artifact]
            [std.lib.invoke :as invoke]
            [std.object :as object])
  (:import (org.eclipse.aether.graph DefaultDependencyNode Dependency DependencyNode Exclusion) (org.eclipse.aether.artifact Artifact DefaultArtifact)))

(invoke/definvoke rep-exclusion
  "creates a rep from an exclusion
 
   (str (rep-exclusion (artifact-exclusion \"hara:hara\")))
   => \"hara:hara:jar:\""
  {:added "3.0"}
  [:method {:multi protocol.classloader/-rep
            :val   Exclusion}]
  ([^Exclusion exclusion]
   (jvm.artifact/->Rep (.getGroupId exclusion)
                       (.getArtifactId exclusion)
                       (.getExtension exclusion)
                       (.getClassifier exclusion)
                       nil
                       nil
                       nil
                       nil
                       nil)))

(invoke/definvoke artifact-exclusion
  "creates an artifact exclusion
 
   (artifact-exclusion \"hara:hara:jar:2.8.4\")
   => Exclusion"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-artifact
            :val   :eclipse.exclusion}]
  ([x]
   (artifact-exclusion nil x))
  ([_ x]
   (let [{:keys [group artifact classifier extension]}
         (jvm.artifact/rep x)]
     (Exclusion. group artifact classifier extension))))

(object/map-like
 Exclusion
 {:tag "exclusion"
  :read {:to-string    (fn [artifact]
                         (jvm.artifact/artifact :string artifact))
         :to-map       (fn [artifact]
                         (into {} (jvm.artifact/rep artifact)))}
  :write {:from-map    (fn [m]
                         (artifact-exclusion m))
          :from-string (fn [m]
                         (artifact-exclusion m))}})

(def artifact-map
  {:artifact
   {:type java.lang.Object
    :fn (fn [^DependencyNode req artifact]
          (.setArtifact req (artifact/artifact-eclipse artifact)))}})

(object/map-like

 Dependency
 {:tag "dep"
  :read :class
  :write {:construct {:fn (fn [artifact scope optional exclusions]
                            (Dependency. (artifact/artifact-eclipse artifact)
                                         (or scope "")
                                         (or optional false)
                                         (mapv artifact-exclusion
                                               (or exclusions []))))
                      :params [:artifact :scope :optional :exclusions]}
          :methods
          (-> (object/write-setters Dependency)
              (merge {:artifact
                      {:type java.lang.Object
                       :fn (fn [^Dependency req artifact]
                             (.setArtifact req (artifact/artifact-eclipse artifact)))}
                      :exclusions
                      {:type java.util.List
                       :fn (fn [^Dependency req exclusions]
                             (.setExclusions req (mapv artifact-exclusion exclusions)))}}))}}

 DependencyNode
 {:tag   "dep.node"
  :read  :class
  :write {:construct {:fn (fn [artifact]
                            (DefaultDependencyNode.
                             ^DefaultArtifact (artifact/artifact-eclipse artifact)))
                      :params [:artifact]}
          :methods
          (-> (object/write-setters DefaultDependencyNode)
              (merge artifact-map))}}

 DefaultDependencyNode
 {:tag   "dep.node"
  :read  :class
  :write {:construct {:fn (fn [artifact]
                            (DefaultDependencyNode.
                             ^DefaultArtifact (artifact/artifact-eclipse artifact)))
                      :params [:artifact]}
          :methods
          (-> (object/write-setters DefaultDependencyNode)
              (merge artifact-map))}})
