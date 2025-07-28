(ns std.fs-test
  (:use code.test)
  (:require [std.fs :refer :all])
  (:refer-clojure :exclude [list resolve]))

^{:refer clojure.core/slurp :added "3.0" :adopt true :class [:file]}
(fact "able to slurp a path"
  (slurp (path "project.clj"))
  => (slurp "project.clj"))

^{:refer std.fs/ns->file :added "3.0" :class [:file]}
(fact "converts an ns string to a file string"

  (ns->file 'std.fs-test)
  => "std/fs_test")

^{:refer std.fs/file->ns :added "3.0" :class [:file]}
(fact "converts a file string to an ns string"

  (file->ns  "std/fs_test")
  => "std.fs-test")

^{:refer std.fs/read-code :added "3.0" :class [:file]}
(fact "takes a file and returns a lazy seq of top-level forms"

  (->> (read-code "src/std/fs.clj")
       first
       (take 2))
  => '(ns std.fs))

^{:refer std.fs/file-namespace :added "3.0"}
(fact "reads the namespace of the given path"

  (file-namespace "src/std/fs.clj")
  => 'std.fs)
