(ns std.string.path
  (:require [std.lib.foundation :as h]
            [std.string.common :as common]))

(defn path-join
  "joins a sequence of elements into a path separated value
 
   (path/path-join [\"a\" \"b\" \"c\"])
   => \"a/b/c\"
 
   ((wrap path/path-join) '[:a :b :c] \"-\")
   => :a-b-c
 
   ((wrap path/path-join) '[a b c] '-)
   => 'a-b-c"
  {:added "3.0"}
  ([arr] (path-join arr h/*sep*))
  ([arr sep]
   (if (seq arr)
     (-> (filter identity arr)
         (common/joinl sep)))))

(defn path-split
  "splits a sequence of elements into a path separated value
 
   (path/path-split \"a/b/c/d\")
   => '[\"a\" \"b\" \"c\" \"d\"]
 
   (path/path-split \"a.b.c.d\" \".\")
   => [\"a\" \"b\" \"c\" \"d\"]
 
   ((wrap path/path-split) :hello/world)
   => [:hello :world]
 
   ((wrap path/path-split) :hello.world \".\")
   => [:hello :world]"
  {:added "3.0"}
  ([s] (path-split s h/*sep*))
  ([s sep]
   (common/split s (h/re-create sep))))

(defn path-ns-array
  "returns the path vector of the string
 
   (path/path-ns-array \"a/b/c/d\")
   => [\"a\" \"b\" \"c\"]
 
   ((wrap path/path-ns-array) (keyword \"a/b/c/d\"))
   => [:a :b :c]"
  {:added "3.0"}
  ([s]
   (path-ns-array s h/*sep*))
  ([s sep]
   (or (butlast (path-split s sep)) [])))

(defn path-ns
  "returns the path namespace of the string
 
   (path/path-ns \"a/b/c/d\")
   => \"a/b/c\"
 
   ((wrap path/path-ns) :a.b.c \".\")
   => :a.b"
  {:added "3.0"}
  ([s]
   (path-ns s h/*sep*))
  ([s sep]
   (-> s
       (path-ns-array sep)
       (path-join sep))))

(defn path-root
  "returns the path root of the string
 
   (path/path-root \"a/b/c/d\")
   => \"a\"
 
   ((wrap path/path-root) 'a.b.c \".\")
   => 'a"
  {:added "3.0"}
  ([s]
   (path-root s h/*sep*))
  ([s sep]
   (first (path-ns-array s sep))))

(defn path-stem-array
  "returns the path stem vector of the string
 
   (path/path-stem-array \"a/b/c/d\")
   =>  [\"b\" \"c\" \"d\"]
 
   ((wrap path/path-stem-array) 'a.b.c.d \".\")
   => '[b c d]"
  {:added "3.0"}
  ([s]
   (path-stem-array s h/*sep*))
  ([s sep]
   (rest (path-split s sep))))

(defn path-stem
  "returns the path stem of the string
 
   (path/path-stem \"a/b/c/d\")
   => \"b/c/d\"
 
   ((wrap path/path-stem) 'a.b.c.d \".\")
   => 'b.c.d"
  {:added "3.0"}
  ([s]
   (path-stem s h/*sep*))
  ([s sep]
   (-> s
       (path-stem-array sep)
       (path-join sep))))

(defn path-val
  "returns the val of the string
 
   (path/path-val \"a/b/c/d\")
   => \"d\"
 
   ((wrap path/path-val) 'a.b.c.d \".\")
   => 'd"
  {:added "3.0"}
  ([s]
   (path-val s h/*sep*))
  ([s sep]
   (last (path-split s sep))))

(defn path-nth
  "check for the val of the string
 
   (path/path-nth \"a/b/c/d\" 2)
   => \"c\""
  {:added "3.0"}
  ([s n]
   (path-nth s n h/*sep*))
  ([s n sep]
   (nth (path-split s sep) n)))

(defn path-sub-array
  "returns a sub array of the path within the string
 
   (path/path-sub-array \"a/b/c/d\" 1 2)
   => [\"b\" \"c\"]
 
   ((wrap path/path-sub-array) (symbol \"a/b/c/d\") 1 2)
   => '[b c]"
  {:added "3.0"}
  ([s start num]
   (path-sub-array s start num h/*sep*))
  ([s start num sep]
   (->> (path-split s sep)
        (drop start)
        (take num))))

(defn path-sub
  "returns a subsection of the path within the string
 
   (path/path-sub \"a/b/c/d\" 1 2)
   => \"b/c\"
 
   ((wrap path/path-sub) (symbol \"a/b/c/d\") 1 2)
   => 'b/c"
  {:added "3.0"}
  ([s start num]
   (path-sub s start num h/*sep*))
  ([s start num sep]
   (-> (path-sub-array s start num sep)
       (path-join sep))))

(defn path-count
  "counts the number of elements in a given path
 
   (path/path-count \"a/b/c\")
   => 3
 
   ((wrap path/path-count) *ns*)
   => 3"
  {:added "3.0"}
  ([s]
   (path-count s h/*sep*))
  ([s sep]
   (count (path-split s sep))))
