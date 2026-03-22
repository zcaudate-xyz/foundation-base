(ns std.string.path-test
  (:require [std.string.path :as path]
            [std.string.wrap :refer [wrap]])
  (:use code.test))

^{:refer std.string.path/path-join :added "3.0"}
(fact "joins a sequence of elements into a path separated value"

  (path/path-join ["a" "b" "c"])
  => "a/b/c"

  ((wrap path/path-join) '[:a :b :c] "-")
  => :a-b-c

  ((wrap path/path-join) '[a b c] '-)
  => 'a-b-c)

^{:refer std.string.path/path-split :added "3.0"}
(fact "splits a sequence of elements into a path separated value"

  (path/path-split "a/b/c/d")
  => '["a" "b" "c" "d"]

  (path/path-split "a.b.c.d" ".")
  => ["a" "b" "c" "d"]

  ((wrap path/path-split) :hello/world)
  => [:hello :world]

  ((wrap path/path-split) :hello.world ".")
  => [:hello :world])

^{:refer std.string.path/path-ns-array :added "3.0"}
(fact "returns the path vector of the string"

  (path/path-ns-array "a/b/c/d")
  => ["a" "b" "c"]

  ((wrap path/path-ns-array) (keyword "a/b/c/d"))
  => [:a :b :c])

^{:refer std.string.path/path-ns :added "3.0"}
(fact "returns the path namespace of the string"

  (path/path-ns "a/b/c/d")
  => "a/b/c"

  ((wrap path/path-ns) :a.b.c ".")
  => :a.b)

^{:refer std.string.path/path-root :added "3.0"}
(fact "returns the path root of the string"

  (path/path-root "a/b/c/d")
  => "a"

  ((wrap path/path-root) 'a.b.c ".")
  => 'a)

^{:refer std.string.path/path-stem-array :added "3.0"}
(fact "returns the path stem vector of the string"

  (path/path-stem-array "a/b/c/d")
  =>  ["b" "c" "d"]

  ((wrap path/path-stem-array) 'a.b.c.d ".")
  => '[b c d])

^{:refer std.string.path/path-stem :added "3.0"}
(fact "returns the path stem of the string"

  (path/path-stem "a/b/c/d")
  => "b/c/d"

  ((wrap path/path-stem) 'a.b.c.d ".")
  => 'b.c.d)

^{:refer std.string.path/path-val :added "3.0"}
(fact "returns the val of the string"

  (path/path-val "a/b/c/d")
  => "d"

  ((wrap path/path-val) 'a.b.c.d ".")
  => 'd)

^{:refer std.string.path/path-nth :added "3.0"}
(fact "check for the val of the string"

  (path/path-nth "a/b/c/d" 2)
  => "c")

^{:refer std.string.path/path-sub-array :added "3.0"}
(fact "returns a sub array of the path within the string"

  (path/path-sub-array "a/b/c/d" 1 2)
  => ["b" "c"]

  ((wrap path/path-sub-array) (symbol "a/b/c/d") 1 2)
  => '[b c])

^{:refer std.string.path/path-sub :added "3.0"}
(fact "returns a subsection of the path within the string"

  (path/path-sub "a/b/c/d" 1 2)
  => "b/c"

  ((wrap path/path-sub) (symbol "a/b/c/d") 1 2)
  => 'b/c)

^{:refer std.string.path/path-count :added "3.0"}
(fact "counts the number of elements in a given path"

  (path/path-count "a/b/c")
  => 3

  ((wrap path/path-count) *ns*)
  => 3)
