(ns std.fs.common
  (:require [clojure.string]
            [std.lib.enum :as enum])
  (:import (java.util.regex Pattern) (java.nio.charset Charset) (java.nio.file AccessMode FileVisitOption FileVisitResult LinkOption StandardCopyOption StandardOpenOption)))

;;
;; SYSTEM
;;  

(def ^:dynamic *cwd* (.getCanonicalPath (java.io.File. ".")))

(def ^:dynamic *sep* (System/getProperty "file.separator"))

(def ^:dynamic *os*
  (let [os (.toLowerCase (System/getProperty "os.name"))]
    (cond (>= (.indexOf os "mac") 0)
          :osx

          (>= (.indexOf os "win") 0)
          :windows

          (>= (.indexOf os "nux") 0)
          :linux

          :else :other)))

(def ^:dynamic *home* (System/getProperty "user.home"))

(def ^:dynamic *tmp-dir (System/getProperty "java.io.tmpdir"))

(def ^:dynamic *no-follow* (LinkOption/values))

(def ^:dynamic *system*
  (if (.startsWith (System/getProperty "os.name") "Windows")
    :dos
    :unix))

;;
;; OPTIONS
;;

(defonce +access-modes+ (enum/enum-map> AccessMode))

(defonce +copy-options+ (enum/enum-map> StandardCopyOption))

(defonce +file-visit-options+ (enum/enum-map> FileVisitOption))

(defonce +file-visit-results+ (enum/enum-map> FileVisitResult))

(defonce +link-options+ (enum/enum-map> LinkOption))

(defonce +open-options+ (enum/enum-map> StandardOpenOption))

(defonce +all-options+
  (merge +copy-options+
         +file-visit-options+
         +file-visit-results+
         +link-options+
         +open-options+))

(defn option
  "shows all options for file operations
 
   (option)
   => (contains [:atomic-move :create-new
                 :skip-siblings :read :continue
                 :create :terminate :copy-attributes
                 :append :truncate-existing :sync
                 :follow-links :delete-on-close :write
                 :dsync :replace-existing :sparse
                 :nofollow-links :skip-subtree])
 
   (option :read)
   => java.nio.file.StandardOpenOption/READ"
  {:added "3.0"}
  ([] (keys +all-options+))
  ([k]
   (+all-options+ k)))

;;
;; PERMISSION
;;

(defn pattern
  "takes a string as turns it into a regex pattern
 
   (pattern \".clj\")
   => #\"\\Q.\\Eclj\"
 
   (pattern \"src/*\")
   => #\"src/.+\""
  {:added "3.0"}
  ([s]
   (-> s
       (clojure.string/replace #"\." "\\\\\\Q.\\\\\\E")
       (clojure.string/replace #"\*" ".+")
       (re-pattern))))

(defn tag-filter
  "adds a tag to the filter to identify the type
 
   (tag-filter {:pattern #\"hello\"})
   => (just {:tag :pattern
             :pattern #\"hello\"})"
  {:added "3.0"}
  ([m]
   (let [tag (first (keys m))]
     (assoc m :tag tag))))

(defn characterise-filter
  "characterises a filter based on type
 
   (characterise-filter \"src\")
   => (just {:tag :pattern :pattern #\"src\"})
 
   (characterise-filter (fn [_] nil))
   => (just {:tag :fn :fn fn?})"
  {:added "3.0"}
  ([ft]
   (tag-filter
    (cond (map? ft)
          ft

          (string? ft)
          {:pattern (pattern ft)}

          (instance? Pattern ft)
          {:pattern ft}

          (fn? ft)
          {:fn ft}

          :else
          (throw (Exception. (str "Cannot process " ft)))))))
