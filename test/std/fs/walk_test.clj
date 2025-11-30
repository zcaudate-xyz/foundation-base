(ns std.fs.walk-test
  (:use code.test)
  (:require [std.fs.walk :refer :all]
            [std.fs.path :as path])
  (:import (java.nio.file.attribute BasicFileAttributes)
           (java.nio.file FileVisitResult)))

^{:refer std.fs.walk/match-single :added "3.0"}
(fact "matches according to the defined filter"

  (match-single {:root (path/path ".")
                 :path (path/path "src/hara/test.clj")}
                {:tag :pattern
                 :pattern #"src"})
  => true

  (match-single {:root (path/path "src")
                 :path (path/path "src/hara/test.clj")}
                {:tag :pattern
                 :pattern #"src"})
  => false

  (match-single {:path (path/path "src/hara/test.clj")}
                {:tag :fn
                 :fn (fn [m]
                       (re-find #"hara" (str m)))})
  => true)

^{:refer std.fs.walk/match-filter :added "3.0"}
(fact "matches according to many filters"

  (match-filter {})
  => true

  (match-filter {:root (path/path "")
                 :path (path/path "src/hara/test.clj")
                 :include [{:tag :pattern
                            :pattern #"test"}]})
  => true

  (match-filter {:root (path/path "")
                 :path (path/path "src/hara/test.clj")
                 :exclude [{:tag :pattern
                            :pattern #"test"}]})
  => false)

^{:refer std.fs.walk/visit-directory-pre :added "3.0"}
(fact "helper function, triggers before visiting a directory"
  (visit-directory-pre {:root (path/path ".")
                        :path (path/path "src")
                        :directory {:pre (constantly :continue)}
                        :accumulate #{}})
  => FileVisitResult/CONTINUE)

^{:refer std.fs.walk/visit-directory-post :added "3.0"}
(fact "helper function, triggers after visiting a directory"
  (visit-directory-post {})
  => FileVisitResult/CONTINUE)

^{:refer std.fs.walk/visit-file :added "3.0"}
(fact "helper function, triggers on visiting a file"
  (visit-file {:path (path/path "project.clj")
               :file (constantly :continue)
               :accumulate #{}})
  => FileVisitResult/CONTINUE)

^{:refer std.fs.walk/visit-file-failed :added "3.0"}
(fact "helper function, triggers on after a file cannot be visited"
  (visit-file-failed {})
  => FileVisitResult/CONTINUE)

^{:refer std.fs.walk/visitor :added "3.0"}
(fact "contructs the clojure wrapper for `java.nio.file.FileVisitor`"
  (visitor {})
  => java.nio.file.FileVisitor)

^{:refer std.fs.walk/walk :added "3.0"}
(fact "visits files based on a directory"

  (walk "src" {:accumulate #{:directories}})
  => vector?

  (walk "src" {:accumulator (atom {})
               :accumulate  #{}
               :file (fn [{:keys [path attrs accumulator]}]
                       (swap! accumulator
                              assoc
                              (str path)
                              (.toMillis (.lastAccessTime ^BasicFileAttributes attrs))))})
  => map?)
