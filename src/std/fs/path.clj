(ns std.fs.path
  (:require [std.fs.common :as common]
            [std.fs.path :as path]
            [std.string :as str])
  (:import (java.io File Writer InputStream OutputStream)
           (java.nio.file Files Path Paths LinkOption OpenOption CopyOption)
           (java.nio.file.attribute PosixFilePermission PosixFilePermissions))
  (:refer-clojure :exclude [resolve]))

(def ^:private +empty-string-array+
  (make-array String 0))

(defn normalise
  "creates a string that takes notice of the user home
 
   (normalise \".\")
   => (str common/*cwd* \"/\" \".\")
 
   (normalise \"~/hello/world.txt\")
   => (str common/*home* \"/hello/world.txt\")
 
   (normalise \"/usr/home\")
   => \"/usr/home\""
  {:added "3.0"}
  ([^String s]
   (cond (= common/*os* :windows)
         (cond (not (.startsWith s common/*sep*))
               (if (= 1 (.indexOf s ":\\"))
                 s
                 (str common/*cwd* common/*sep* s))

               :else s)

         :else
         (cond (= s "~")
               common/*home*

               (.startsWith s (str "~" common/*sep*))
               (.replace s "~" ^String common/*home*)

               (not (.startsWith s common/*sep*))
               (str common/*cwd* common/*sep* s)

               :else s))))

(defn ^Path path
  "creates a `java.nio.file.Path object"
  {:added "3.0"}
  ([x]
   (cond (instance? Path x)
         x

         (string? x)
         (.normalize (Paths/get (normalise x) +empty-string-array+))

         (vector? x)
         (apply path x)

         (instance? java.net.URI x)
         (Paths/get x)

         (instance? java.net.URL x)
         (path (.getFile ^java.net.URL x))

         (instance? File x)
         (path (.toString ^File x))

         :else
         (throw (Exception. (format "Input %s is not of the correct format" x)))))
  ([s & more]
   (.normalize (Paths/get (normalise (str s)) (into-array String (map str more))))))

(defn path?
  "checks to see if the object is of type Path
 
   (path? (path \"/home\"))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Path x)))

(defn section
  "path object without normalisation
 
   (str (section \"project.clj\"))
   => \"project.clj\"
 
   (str (section \"src\" \"hara/time.clj\"))
   => \"src/hara/time.clj\""
  {:added "3.0"}
  ([s & more]
   (Paths/get s (into-array String more))))

(defmethod print-method Path
  ([^Path v ^Writer w]
   (.write w (str "#path:\"" (.toString v) "\""))))

(defmethod print-method File
  ([^File v ^Writer w]
   (.write w (str "#file:\"" (.toString v) "\""))))

(defn to-file
  "creates a java.io.File object
 
   (to-file (section \"project.clj\"))
   => (all java.io.File
           #(-> % str (= \"project.clj\")))"
  {:added "3.0"}
  ([^Path path]
   (.toFile path)))

(defn file-name
  "returns the last section of the path
 
   (str (file-name \"src/hara\"))
   => \"hara\""
  {:added "3.0"}
  ([x]
   (.getFileName (path x))))

(defn file-system
  "returns the filesystem governing the path
 
   (file-system \".\")
   ;; #object[sun.nio.fs.MacOSXFileSystem 0x512a9870 \"sun.nio.fs.MacOSXFileSystem@512a9870\"]
   => java.nio.file.FileSystem"
  {:added "3.0"}
  ([x]
   (.getFileSystem (path x))))

(defn nth-segment
  "returns the nth segment of a given path
 
   (str (nth-segment \"/usr/local/bin\" 1))
   => \"local\""
  {:added "3.0"}
  ([x i]
   (.getName (path x) i)))

(defn segment-count
  "returns the number of segments of a given path
 
   (segment-count \"/usr/local/bin\")
   => 3"
  {:added "3.0"}
  ([x]
   (.getNameCount (path x))))

(defn parent
  "returns the parent of the given path
 
   (str (parent \"/usr/local/bin\"))
   => \"/usr/local\""
  {:added "3.0"}
  ([x]
   (.getParent (path x))))

(defn root
  "returns the root path
 
   (str (root \"/usr/local/bin\"))
   => \"/\""
  {:added "3.0"}
  ([x]
   (.getRoot (path x))))

(defn relativize
  "returns one path relative to another
 
   (str (relativize \"test\" \"src/hara\"))
   => \"../src/hara\""
  {:added "3.0"}
  ([x other]
   (.relativize (path x) (path other))))

(defn subpath
  "returns the subpath of a given path
 
   (str (subpath \"/usr/local/bin/hello\" 1 3))
   => \"local/bin\""
  {:added "3.0"}
  ([x start end]
   (.subpath (path x) start end)))

(defn file-suffix
  "encodes the type of file as a keyword
 
   (file-suffix \"hello.clj\")
   => :clj
 
   (file-suffix \"hello.java\")
   => :java"
  {:added "3.0"}
  ([file]
   (-> (str file)
       (str/split #"\.")
       last
       keyword)))

(defn directory?
  "checks whether a file is a directory
 
   (directory? \"src\")
   => true
 
   (directory? \"project.clj\")
   => false"
  {:added "3.0"}
  ([path]
   (Files/isDirectory (path/path path) common/*no-follow*)))

(defn executable?
  "checks whether a file is executable
 
   (executable? \"project.clj\")
   => boolean?
 
   (executable? \"/usr/bin/whoami\")
   => true"
  {:added "3.0"}
  ([path]
   (Files/isExecutable (path/path path))))

(defn set-executable
  "sets a file to be executable"
  {:added "3.0"}
  [path]
  (let [perms (doto (Files/getPosixFilePermissions path (make-array LinkOption 0))
                (.add PosixFilePermission/OWNER_EXECUTE)
                (.add PosixFilePermission/GROUP_EXECUTE)
                (.add PosixFilePermission/OTHERS_EXECUTE))]
    (Files/setPosixFilePermissions path perms)))

(defn permissions
  "returns the permissions for a given file
 
   (permissions \"src\")
   => \"rwxr-xr-x\""
  {:added "3.0"}
  ([path]
   (-> (path/path path)
       (Files/getPosixFilePermissions  common/*no-follow*)
       (PosixFilePermissions/toString))))

(defn typestring
  "returns the shorthand string for a given entry
 
   (typestring \"src\")
   => \"d\"
 
   (typestring \"project.clj\")
   => \"-\""
  {:added "3.0"}
  ([path]
   (let [path (path/path path)]
     (cond (Files/isDirectory path (LinkOption/values))
           "d"

           (Files/isSymbolicLink path)
           "l"

           :else "-"))))

(defn exists?
  "checks whether a file exists
 
   (exists? \"project.clj\")
   => true
 
   (exists? \"NON.EXISTENT\")
   => false"
  {:added "3.0"}
  ([path]
   (Files/exists (path/path path) common/*no-follow*)))

(defn hidden?
  "checks whether a file is hidden
 
   (hidden? \".gitignore\")
   => true
 
   (hidden? \"project.clj\")
   => false"
  {:added "3.0"}
  ([path]
   (Files/isHidden (path/path path))))

(defn file?
  "checks whether a file is not a link or directory
 
   (file? \"project.clj\")
   => true
 
   (file? \"src\")
   => false"
  {:added "3.0"}
  ([path]
   (Files/isRegularFile (path/path path) common/*no-follow*)))

(defn link?
  "checks whether a file is a link
 
   (link? \"project.clj\")
   => false
 
   (link? (api/create-symlink \"project.lnk\"
                              \"project.clj\"))
   => true
   (api/delete \"project.lnk\")"
  {:added "3.0"}
  ([path]
   (Files/isSymbolicLink (path/path path))))

(defn readable?
  "checks whether a file is readable
 
   (readable? \"project.clj\")
   => true"
  {:added "3.0"}
  ([path]
   (Files/isReadable (path/path path))))

(defn writable?
  "checks whether a file is writable
 
   (writable? \"project.clj\")
   => true"
  {:added "3.0"}
  ([path]
   (Files/isWritable (path/path path))))

(defn empty-directory?
  "checks if a directory is empty, returns true if both are true
 
   (empty-directory? \".\")
   => false"
  {:added "3.0"}
  ([path]
   (let [path (path/path path)]
     (if (directory? path)
       (zero? (.count (Files/list path)))
       (throw (Exception. (str "Not a directory: " path)))))))

(defn ^InputStream input-stream
  "opens a file as an input-stream
 
   (input-stream \"project.clj\")"
  {:added "3.0"}
  ([path]
   (input-stream path {}))
  ([path opts]
   (Files/newInputStream (path/path path)
                         (->> (:options opts)
                              (mapv common/option)
                              (into-array OpenOption)))))
(defn output-stream
  "opens a file as an output-stream
 
   (output-stream \"project.clj\")"
  {:added "3.0"}
  ([path]
   (output-stream path {}))
  ([path opts]
   (Files/newOutputStream (path/path path)
                          (->> (:options opts)
                               (mapv common/option)
                               (into-array OpenOption)))))

(defn read-all-lines
  "opens a file and reads the contents as an array of lines
 
   (read-all-lines \"project.clj\")"
  {:added "3.0"}
  ([path]
   (Files/readAllLines (path/path path))))

(defn read-all-bytes
  "opens a file and reads the contents as a byte array
 
   (read-all-bytes \"project.clj\")"
  {:added "3.0"}
  ([path]
   (Files/readAllBytes (path/path path))))

(defn ^File file
  "returns the input as a file
 
   (file \"project.clj\")
   => java.io.File"
  {:added "3.0"}
  ([path]
   (path/to-file (path/path path))))

(defn last-modified
  "returns the last modified time as a long
 
   (last-modified \"project.clj\")
   => integer?"
  {:added "3.0"}
  ([path]
   (.lastModified (path/file path))))

(defn write-into
  "writes a stream to a path
 
   (write-into \"project.clj\" (java.io.FileInputStream. \"project.clj\")
               {:options #{:replace-existing}})"
  {:added "3.0"}
  ([path ^InputStream stream]
   (write-into path stream {}))
  ([path ^InputStream stream opts]
   (let [^"[Ljava.nio.file.CopyOption;" opts (->> (:options opts)
                                                  (mapv common/option)
                                                  (into-array CopyOption))]
     (Files/copy stream (path/path path) opts))))

(defn write-all-bytes
  "writes a byte-array to file
 
   (write-all-bytes \"hello.txt\" (.getBytes \"Hello World\"))"
  {:added "3.0"}
  ([path bytes]
   (write-all-bytes path bytes {}))
  ([path ^bytes bytes opts]
   (let [^"[Ljava.nio.file.OpenOption;" opts (->> (:options opts)
                                                  (mapv common/option)
                                                  (into-array OpenOption))]
     (Files/write ^Path (path/path path)
                  bytes
                  opts))))

(comment
  (root (path "."))

  (file-name (path "."))

  (parent (path "."))

  (nth-segment (path ".") 3))
