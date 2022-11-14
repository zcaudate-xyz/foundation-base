(ns std.fs.path-test
  (:use code.test)
  (:require [std.fs.path :refer :all]
            [std.fs.api :as api]
            [std.fs.common :as common]
            [std.string :as str])
  (:refer-clojure :exclude [resolve]))

^{:refer std.fs.path/path.functionality :added "3.0" :adopt true}
(fact "returns a java.nio.file.Path object"

  (str (path "~"))
  => common/*home*

  (str (path "~/../shared/data"))
  => (str (->> (re-pattern common/*sep*)
               (str/split common/*home*)
               (butlast)
               (str/join "/"))
          "/shared/data")

  (str (path ["hello" "world.txt"]))
  => (str common/*cwd* "/hello/world.txt"))

^{:refer std.fs.path/normalise :added "3.0"}
(fact "creates a string that takes notice of the user home"

  (normalise ".")
  => (str common/*cwd* "/" ".")

  (normalise "~/hello/world.txt")
  => (str common/*home* "/hello/world.txt")

  (normalise "/usr/home")
  => "/usr/home")

^{:refer std.fs.path/path :added "3.0" :class [:path]}
(comment "creates a `java.nio.file.Path object"
  ^:hidden

  (path "project.clj")
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"

  (path (path "project.clj"))       ;; idempotent
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"

  (path "~")                       ;; tilda
 ;;=> #path:"/Users/chris"

  (path "src" "hara/time.clj")      ;; multiple arguments
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path ["src" "hara" "time.clj"])  ;; vector 
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path (java.io.File.              ;; java.io.File object 
         "src/hara/time.clj"))
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path (java.net.URI.              ;; java.net.URI object 
         "file:///Users/chris/Development/chit/hara/project.clj"))
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"
  )

^{:refer std.fs.path/path? :added "3.0" :class [:path]}
(fact "checks to see if the object is of type Path"

  (path? (path "/home"))
  => true)

^{:refer std.fs.path/section :added "3.0"}
(fact "path object without normalisation"

  (str (section "project.clj"))
  => "project.clj"

  (str (section "src" "hara/time.clj"))
  => "src/hara/time.clj")

^{:refer std.fs.path/to-file :added "3.0"}
(fact "creates a java.io.File object"

  (to-file (section "project.clj"))
  => (all java.io.File
          #(-> % str (= "project.clj"))))

^{:refer std.fs.path/file-name :added "3.0" :class [:path]}
(fact "returns the last section of the path"

  (str (file-name "src/hara"))
  => "hara")

^{:refer std.fs.path/file-system :added "3.0" :class [:file]}
(fact "returns the filesystem governing the path"

  (file-system ".")
  ;; #object[sun.nio.fs.MacOSXFileSystem 0x512a9870 "sun.nio.fs.MacOSXFileSystem@512a9870"]
  => java.nio.file.FileSystem)

^{:refer std.fs.path/nth-segment :added "3.0" :class [:path]}
(fact "returns the nth segment of a given path"

  (str (nth-segment "/usr/local/bin" 1))
  => "local")

^{:refer std.fs.path/segment-count :added "3.0" :class [:path]}
(fact "returns the number of segments of a given path"

  (segment-count "/usr/local/bin")
  => 3)

^{:refer std.fs.path/parent :added "3.0" :class [:path]}
(fact "returns the parent of the given path"

  (str (parent "/usr/local/bin"))
  => "/usr/local")

^{:refer std.fs.path/root :added "3.0" :class [:path]}
(fact "returns the root path"

  (str (root "/usr/local/bin"))
  => "/")

^{:refer std.fs.path/relativize :added "3.0" :class [:path]}
(fact "returns one path relative to another"

  (str (relativize "test" "src/hara"))
  => "../src/hara")

^{:refer std.fs.path/subpath :added "3.0" :class [:path]}
(fact "returns the subpath of a given path"

  (str (subpath "/usr/local/bin/hello" 1 3))
  => "local/bin")

^{:refer std.fs.path/file-suffix :added "3.0" :class [:attribute]}
(fact "encodes the type of file as a keyword"

  (file-suffix "hello.clj")
  => :clj

  (file-suffix "hello.java")
  => :java)

^{:refer std.fs.path/directory? :added "3.0" :class [:attribute]}
(fact "checks whether a file is a directory"

  (directory? "src")
  => true

  (directory? "project.clj")
  => false)

^{:refer std.fs.path/executable? :added "3.0" :class [:attribute]}
(fact "checks whether a file is executable"

  (executable? "project.clj")
  => false

  (executable? "/usr/bin/whoami")
  => true)

^{:refer std.fs.path/set-executable :added "3.0"}
(fact "sets a file to be executable")

^{:refer std.fs.path/permissions :added "3.0" :class [:attribute]}
(comment "returns the permissions for a given file"

  (permissions "src")
  => "rwxr-xr-x")

^{:refer std.fs.path/typestring :added "3.0" :class [:attribute]}
(fact "returns the shorthand string for a given entry"

  (typestring "src")
  => "d"

  (typestring "project.clj")
  => "-")

^{:refer std.fs.path/exists? :added "3.0" :class [:attribute]}
(fact "checks whether a file exists"

  (exists? "project.clj")
  => true

  (exists? "NON.EXISTENT")
  => false)

^{:refer std.fs.path/hidden? :added "3.0" :class [:attribute]}
(fact "checks whether a file is hidden"

  (hidden? ".gitignore")
  => true

  (hidden? "project.clj")
  => false)

^{:refer std.fs.path/file? :added "3.0" :class [:attribute]}
(fact "checks whether a file is not a link or directory"

  (file? "project.clj")
  => true

  (file? "src")
  => false)

^{:refer std.fs.path/link? :added "3.0" :class [:attribute]}
(fact "checks whether a file is a link"

  (link? "project.clj")
  => false

  (link? (api/create-symlink "project.lnk"
                             "project.clj"))
  => true
  (api/delete "project.lnk"))

^{:refer std.fs.path/readable? :added "3.0" :class [:attribute]}
(fact "checks whether a file is readable"

  (readable? "project.clj")
  => true)

^{:refer std.fs.path/writable? :added "3.0" :class [:attribute]}
(fact "checks whether a file is writable"

  (writable? "project.clj")
  => true)

^{:refer std.fs.path/empty-directory? :added "3.0" :class [:attribute]}
(fact "checks if a directory is empty, returns true if both are true"

  (empty-directory? ".")
  => false)

^{:refer std.fs.path/input-stream :added "3.0" :class [:file]}
(fact "opens a file as an input-stream"

  (input-stream "project.clj"))

^{:refer std.fs.path/output-stream :added "3.0" :class [:file]}
(comment "opens a file as an output-stream"

  (output-stream "project.clj"))

^{:refer std.fs.path/read-all-lines :added "3.0" :class [:file]}
(fact "opens a file and reads the contents as an array of lines"

  (read-all-lines "project.clj"))

^{:refer std.fs.path/read-all-bytes :added "3.0" :class [:file]}
(fact "opens a file and reads the contents as a byte array"

  (read-all-bytes "project.clj"))

^{:refer std.fs.path/file :added "3.0" :class [:file]}
(fact "returns the input as a file"

  (file "project.clj")
  => java.io.File)

^{:refer std.fs.path/last-modified :added "3.0"}
(fact "returns the last modified time as a long"

  (last-modified "project.clj")
  => integer?)

^{:refer std.fs.path/write-into :added "3.0" :class [:file]}
(fact "writes a stream to a path"

  (write-into "project.clj" (java.io.FileInputStream. "project.clj")
              {:options #{:replace-existing}}))

^{:refer std.fs.path/write-all-bytes :added "3.0" :class [:file]}
(comment "writes a byte-array to file"

  (write-all-bytes "hello.txt" (.getBytes "Hello World")))

(comment
  (code.manage/import))
