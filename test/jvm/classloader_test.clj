(ns jvm.classloader-test
  (:use code.test)
  (:require [jvm.classloader :refer :all]
            [jvm.classloader.common :as common]
            [jvm.artifact :as artifact]
            [std.fs :as fs]
            [clojure.java.io :as io]))

^{:refer jvm.classloader/has-url? :added "3.0"}
(fact "checks whether the classloader has the following url"

  (has-url? (fs/path "src"))
  => true)

^{:refer jvm.classloader/get-url :added "3.0"}
(fact "returns the required url"

  (get-url (fs/path "src"))
  ;;#object[java.net.URL 0x3d202d52 "file:/Users/chris/Development/hara/hara/src/"]
  => java.net.URL)

^{:refer jvm.classloader/all-urls :added "3.0"}
(fact "returns all urls contained by the loader"

  (all-urls) ^:hidden
  => sequential?)

^{:refer jvm.classloader/add-url :added "3.0"}
(fact "adds a classpath to the loader"
  ^:hidden
  
  (add-url (fs/path "path/to/somewhere")) ^:hidden
  => (throws)
  
  (has-url? (fs/path "path/to/somewhere"))
  => false

  ;;(remove-url (fs/path "path/to/somewhere"))
  )

^{:refer jvm.classloader/remove-url :added "3.0"}
(fact "removes url from classloader"
  ^:hidden

  (try
    (do (add-url (fs/path "path/to/somewhere"))
        (has-url? (fs/path "path/to/somewhere")))
    (catch Throwable t))
  => nil

  (try (remove-url (fs/path "path/to/somewhere"))
       (has-url? (fs/path "path/to/somewhere"))
       (catch Throwable t
         false))
  => false?)

^{:refer jvm.classloader/delegation :added "3.0"}
(fact "returns a list of classloaders in order of top to bottom"
   ^:hidden
  
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (delegation))
  => list?)

^{:refer jvm.classloader/classpath :added "3.0"}
(fact "returns the classpath for the loader, including parent loaders"
   ^:hidden
  
  (classpath)
  => sequential?)

^{:refer jvm.classloader/all-jars :added "3.0"}
(fact "gets all jars on the classloader"
   ^:hidden
  
  (all-jars)
  => seq?)

^{:refer jvm.classloader/all-paths :added "3.0"}
(fact "gets all paths on the classloader"
   ^:hidden
  
  (all-paths)
  => seq?)

^{:refer jvm.classloader/url-classloader :added "3.0"}
(fact "returns a `java.net.URLClassLoader` from a list of strings"
   ^:hidden
  
  (->> (url-classloader ["/dev/null/"])
       (.getURLs)
       (map str))
  => ["file:/dev/null/"])

^{:refer jvm.classloader/dynamic-classloader :added "3.0"}
(fact "creates a dynamic classloader instance"
   ^:hidden
  
  (dynamic-classloader [])
  => clojure.lang.DynamicClassLoader)

^{:refer jvm.classloader/load-class :added "3.0"}
(fact "loads class from an external source"
   ^:hidden
  
  (.getName (load-class "target/classes/test/Cat.class"
                        {:name "test.Cat"}))
  => "test.Cat")

^{:refer jvm.classloader/unload-class :added "3.0"}
(fact "unloads a class from the current namespace"
   ^:hidden
  
  (unload-class "test.Cat")
  ;; #object[java.lang.ref.SoftReference 0x10074132
  ;;         "java.lang.ref.SoftReference@10074132"]
  )

^{:refer jvm.classloader/to-bytes :added "3.0"}
(fact "opens `.class` file from an external source"
   ^:hidden
  
  (to-bytes "target/classes/test/Dog.class")
  => bytes?)

^{:refer jvm.classloader/any-load-class :added "3.0"}
(fact "loads a class, storing class into the global cache"
   ^:hidden
  
  (any-load-class test.Cat nil nil)
  => test.Cat)

^{:refer jvm.classloader/dynamic-load-bytes :added "3.0"}
(fact "loads a class from bytes"
   ^:hidden
  
  (dynamic-load-bytes (to-bytes "target/classes/test/Cat.class")
                      (dynamic-classloader)
                      {:name "test.Cat"})
  => test.Cat)

^{:refer jvm.classloader/dynamic-load-string :added "3.0"}
(comment "loads a class from a path string"
   ^:hidden
  
  (dynamic-load-string "<.m2>/org/yaml/snakeyaml/1.5/snakeyaml-1.5.jar"
                       (dynamic-classloader)
                       {:name "org.yaml.snakeyaml.Dumper"
                        :entry-path "org/yaml/snakeyaml/Dumper.class"})
  => org.yaml.snakeyaml.Dumper)

^{:refer jvm.classloader/dynamic-load-coords :added "3.0"}
(comment "loads a class from a coordinate"
   ^:hidden
  
  (.getName (dynamic-load-coords '[org.yaml/snakeyaml "1.5"]
                                 (dynamic-classloader)
                                 {:name "org.yaml.snakeyaml.Dumper"
                                  :entry-path "org/yaml/snakeyaml/Dumper.class"}))
  => "org.yaml.snakeyaml.Dumper")

(comment

  
  (dynamic-load-coords (artifact/artifact :path '[org.openjfx/javafx-base "16"])
                       
                       (dynamic-classloader)
                       {:name "javafx.util.Duration"
                        :entry-path "javafx/util/Duration.class"})
  )
