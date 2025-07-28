(ns std.fs
  (:require [std.string :as str]
            [std.fs.attribute :as attr]
            [std.fs.api :as api]
            [std.fs.common :as common]
            [std.fs.interop]
            [std.fs.path :as path]
            [std.lib :as h])
  (:import (java.io PushbackReader InputStreamReader))
  (:refer-clojure :exclude [list resolve]))

(h/intern-all std.fs.api
              std.fs.path)

(h/intern-in attr/attributes
             attr/set-attributes
             common/option)

(defn ^String ns->file
  "converts an ns string to a file string
 
   (ns->file 'std.fs-test)
   => \"std/fs_test\""
  {:added "3.0"}
  ([ns]
   (-> (str ns)
       (.replaceAll "\\." "/")
       (.replaceAll "-" "_"))))

(defn file->ns
  "converts a file string to an ns string
 
   (file->ns  \"std/fs_test\")
   => \"std.fs-test\""
  {:added "3.0"}
  ([^String ns]
   (-> ns
       (.replaceAll "/" ".")
       (.replaceAll "_" "-"))))

(defn read-code
  "takes a file and returns a lazy seq of top-level forms
 
   (->> (read-code \"src/std/fs.clj\")
        first
        (take 2))
   => '(ns std.fs)"
  {:added "3.0"}
  ([path]
   (read-code path identity))
  ([path f]
   (with-open [reader (->> (path/input-stream (path/path path))
                           (InputStreamReader.)
                           (PushbackReader.))]
     (->> (repeatedly #(try (clojure.core/read reader)
                            (catch Throwable e)))
          (take-while identity)
          f
          (h/unlazy)))))

(defn get-namespace
  ([forms]
   (->> forms
        (filter #(-> % first (= 'ns)))
        first
        second)))

(defn file-namespace
  "reads the namespace of the given path
 
   (file-namespace \"src/code/project.clj\")
   => 'code.project"
  {:added "3.0"}
  ([path]
   (try
     (read-code path get-namespace)
     (catch Throwable t
       (h/local :println path "Cannot be loaded")))))
