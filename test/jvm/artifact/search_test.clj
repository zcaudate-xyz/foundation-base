(ns jvm.artifact.search-test
  (:require [jvm.artifact.search :refer :all]
            [jvm.classloader :as cls]
            [std.fs.archive :as archive])
  (:use code.test))

^{:refer jvm.artifact.search/class-seq :added "3.0"}
(fact "creates a sequence of class names"

  (some #{"clojure.lang.RT"}
        (class-seq
         '[[org.clojure/clojure "1.12.0"]]))
  => "clojure.lang.RT")

^{:refer jvm.artifact.search/search-match :added "3.0"}
(fact "constructs a matching function for filtering"

  ((search-match #"hello") "hello.world")
  => true

  ((search-match java.util.List) java.util.ArrayList)
  => true)

^{:refer jvm.artifact.search/search :added "3.0"}
(fact "searches a pattern for class names"

  (->> (search [#"^java\.lang\."
                java.lang.CharSequence]
               ["java.lang.String"
                "java.util.ArrayList"])
       (map #(.getName ^Class %)))
  => ["java.lang.String"])

(comment
  (defn match-jars
  "matches jars from any representation

   (match-jars '[org.eclipse.aether/aether-api \"1.1.0\"])
   => (\"<.m2>/org/eclipse/aether/aether-api/1.1.0/aether-api-1.1.0.jar\")"
  {:added "3.0"}
  ([names] (match-jars names []))
  ([names coords]
   (let [patterns (map (fn [name]
                         (->> [name ".*"]
                              (artifact/artifact :path)
                              (re-pattern)))
                       names)]
     (-> coords
         (map #(artifact/artifact :path %))
         (filter (fn [path]
                   (some (fn [pattern]
                           (re-find pattern path))
                         patterns)))))))

  (fact:list)
  (test-jvm_artifact_search__class_seq test-jvm_artifact_search__search_match)
  (into {} (fact:get test-jvm_artifact_search__search_match))
  )
