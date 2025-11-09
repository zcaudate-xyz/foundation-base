(ns jvm.artifact.search-test
  (:use code.test)
  (:require [jvm.artifact.search :refer :all]
            [jvm.classloader :as cls]
            [std.fs.archive :as archive]))

^{:refer jvm.artifact.search/match-jars :added "3.0"}
(comment "matches jars from any representation"

  (match-jars '[org.eclipse.aether/aether-api "1.1.0"])
  => ("<.m2>/org/eclipse/aether/aether-api/1.1.0/aether-api-1.1.0.jar"))

^{:refer jvm.artifact.search/class-seq :added "3.0"}
(fact "creates a sequence of class names"
  ^:hidden
  
  (class-seq
   '[[org.eclipse.aether/aether-api "1.1.0"]])
  
  (class-seq
   '[[classifier4j/classifier4j "0.6"]])
  )

^{:refer jvm.artifact.search/search-match :added "3.0"}
(fact "constructs a matching function for filtering"

  ((search-match #"hello") "hello.world")
  => true

  ((search-match java.util.List) java.util.ArrayList)
  => true)

^{:refer jvm.artifact.search/search :added "3.0"}
(comment "searches a pattern for class names"

  (->> (.getURLs cls/+base+)
       (map #(-> % str (subs (count "file:"))))
       (filter #(.endsWith % "jfxrt.jar"))
       (class-seq)
       (search [#"^javafx.*[A-Za-z0-9]Builder$"])
       (take 5))
  => (javafx.animation.AnimationBuilder
      javafx.animation.FadeTransitionBuilder
      javafx.animation.FillTransitionBuilder
      javafx.animation.ParallelTransitionBuilder
      javafx.animation.PathTransitionBuilder))
