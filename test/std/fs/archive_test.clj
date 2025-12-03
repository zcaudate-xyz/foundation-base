(ns std.fs.archive-test
  (:use code.test)
  (:require [std.fs.archive :refer :all]
            [std.lib.bin :as binary]
            [std.fs :as fs]
            [std.protocol.archive  :as protocol.archive])
  (:refer-clojure :exclude [list remove]))

^{:refer std.fs.archive/zip-system? :added "3.0"}
(fact "checks if object is a `ZipSystem`"

  (zip-system? (open "test-scratch/hello.jar"))
  => true)

^{:refer std.fs.archive/create :added "3.0"}
(fact "creats a zip file"

  (fs/delete "test-scratch/hello.jar")

  (create "test-scratch/hello.jar")
  => zip-system?)

^{:refer std.fs.archive/open :added "3.0"}
(fact "either opens an existing archive or creates one if it doesn't exist"

  (open "test-scratch/hello.jar" {:create true})
  => zip-system?)

^{:refer std.fs.archive/open-and :added "3.0"}
(fact "helper function for opening an archive and performing a single operation"

  (->> (open-and "test-scratch/hello.jar" {:create false} #(protocol.archive/-list %))
       (map str))
  => ["/"])

^{:refer std.fs.archive/url :added "3.0"}
(fact "returns the url of the archive"

  (url (open "test-scratch/hello.jar"))
  => (str (fs/path "test-scratch/hello.jar")))

^{:refer std.fs.archive/path :added "3.0"}
(fact "returns the url of the archive"

  (-> (open "test-scratch/hello.jar")
      (path "world.java")
      (str))
  => "world.java")

^{:refer std.fs.archive/list :added "3.0"}
(fact "lists all the entries in the archive"

  (map str (list "test-scratch/hello.jar"))
  => ["/"])

^{:refer std.fs.archive/has? :added "3.0"}
(fact "checks if the archive has a particular entry"

  (has? "test-scratch/hello.jar" "world.java")
  => false)

^{:refer std.fs.archive/archive :added "3.0"}
(fact "puts files into an archive"

  (archive "test-scratch/hello.jar" "src")
  => coll?)

^{:refer std.fs.archive/extract :added "3.0"}
(fact "extracts all file from an archive"

  (extract "test-scratch/hello.jar")
  => coll? ^:hidden

  (extract "test-scratch/hello.jar" "test-scratch/output")
  
  
  (fs/delete "test-scratch/hello.jar")
  (fs/delete "test-scratch/hara")
  (fs/delete "test-scratch/output")
  (fs/delete "test-scratch/select"))

^{:refer std.fs.archive/insert :added "3.0"}
(fact "inserts a file to an entry within the archive"

  (open   "test-scratch/hello.jar" {:create true})
  (insert "test-scratch/hello.jar" "project.clj" "project.clj")
  => fs/path?)

^{:refer std.fs.archive/remove :added "3.0"}
(comment "removes an entry from the archive"

  (remove "test-scratch/hello.jar" "project.clj")
  => #{"project.clj"} ^:hidden

  (fs/delete "test-scratch/hello.jar"))

^{:refer std.fs.archive/write :added "3.0"}
(comment "writes files to an archive"

  (doto "test-scratch/hello.jar"
    (fs/delete)
    (open)
    (write "test.stuff"
           (binary/input-stream (.getBytes "Hello World"))))

  (slurp (stream (open "test-scratch/hello.jar") "test.stuff"))
  => "Hello World")

^{:refer std.fs.archive/stream :added "3.0"}
(comment "creates a stream for an entry wthin the archive"

  (do (insert "test-scratch/hello.jar" "project.clj" "project.clj")
      (slurp (stream "test-scratch/hello.jar" "project.clj")))
  => (slurp "project.clj") ^:hidden

  (fs/delete "test-scratch/hello.jar"))
