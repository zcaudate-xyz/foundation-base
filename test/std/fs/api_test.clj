(ns std.fs.api-test
  (:use code.test)
  (:require [std.fs.api :refer :all]
            [std.fs.path :as path])
  (:refer-clojure :exclude [list resolve]))

^{:refer std.fs.api/create-directory :added "3.0" :class [:operation]}
(fact "creates a directory on the filesystem"

  (do (create-directory "test-scratch/.hello/.world/.foo")
      (path/directory? "test-scratch/.hello/.world/.foo"))
  => true

  (delete "test-scratch/.hello"))

^{:refer std.fs.api/create-symlink :added "3.0" :class [:operation]}
(fact "creates a symlink to another file"

  (do (create-symlink "test-scratch/project.lnk" "project.clj")
      (path/link? "test-scratch/project.lnk"))
  => true

  ^:hidden
  (delete "test-scratch/project.lnk"))

^{:refer std.fs.api/create-tmpfile :added "3.0" :class [:operation]}
(fact "creates a tempory file"

  (create-tmpfile)
  ;;#file:"/var/folders/rc/4nxjl26j50gffnkgm65ll8gr0000gp/T/tmp2270822955686495575"
  => java.io.File)

^{:refer std.fs.api/create-tmpdir :added "3.0" :class [:operation]}
(fact "creates a temp directory on the filesystem"

  (create-tmpdir)
  => java.nio.file.Path)

^{:refer std.fs.api/select :added "3.0" :class [:operation]}
(fact "selects all the files in a directory"

  (->> (select "src/std/fs")
       (map #(path/relativize "src/std/fs" %))
       (map str)
       (sort))
  => (contains ["api.clj" "archive.clj" "attribute.clj" "common.clj" "interop.clj" "path.clj" "walk.clj" "watch.clj"] :in-any-order))

^{:refer std.fs.api/list :added "3.0"  :class [:operation]}
(fact "lists the files and attributes for a given directory"

  (keys (list "src/std/fs"))
  => (contains [(str (path/path "src/std/fs/api.clj"))
                (str (path/path "src/std/fs/path.clj"))] :in-any-order :gaps-ok))

^{:refer std.fs.api/copy :added "3.0" :class [:operation]}
(fact "copies all specified files from one to another"

  (copy "src" ".src" {:include [".clj"]})
  => map?

  (delete ".src"))

^{:refer std.fs.api/copy-single :added "3.0" :class [:operation]}
(fact "copies a single file to a destination"

  (copy-single "project.clj"
               "test-scratch/project.clj.bak"
               {:options #{:replace-existing}})
  => (path/path "." "test-scratch/project.clj.bak")

  (delete "test-scratch/project.clj.bak"))

^{:refer std.fs.api/copy-into :added "3.0" :class [:operation]}
(fact "copies a single file to a destination"

  (create-directory "test-scratch")
  (copy-into "src/std/fs/api.clj"
             "test-scratch/fs/api.clj")
  => vector?

  (delete "test-scratch"))

^{:refer std.fs.api/delete :added "3.0" :class [:operation]}
(fact "copies all specified files from one to another"

  (do (create-directory "test-scratch")
      (copy-single "src/std/fs/api.clj" "test-scratch/fs/api.clj")
      (delete "test-scratch" {:include ["api.clj"]}))
  => set?

  (delete "test-scratch")
  => set?)

^{:refer std.fs.api/move :added "3.0" :class [:operation]}
(fact "moves a file or directory"
  (create-directory "test-scratch/move-src")
  (spit "test-scratch/move-src/hello" "world")

  (move "test-scratch/move-src" "test-scratch/move-dest")
  => map?

  (path/exists? "test-scratch/move-dest/hello") => true
  (path/exists? "test-scratch/move-src") => false

  (delete "test-scratch/move-dest"))
