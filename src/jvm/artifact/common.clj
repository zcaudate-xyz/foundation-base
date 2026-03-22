(ns jvm.artifact.common
  (:require [std.string.common])
  (:import (clojure.lang Symbol)))

(defonce ^:dynamic *sep*
  (System/getProperty "file.separator"))

(defonce ^:dynamic *local-repo*
  (-> (System/getProperty "user.home")
      (str *sep* ".m2" *sep* "repository")))

(defonce ^:dynamic *java-class-path*
  (->> (std.string.common/split (System/getProperty "java.class.path") #":")
       (filter (fn [^String x] (.endsWith x ".jar")))))

(defonce ^:dynamic *java-home*
  (System/getProperty "java.home"))

(defonce ^:dynamic *java-runtime-jar*
  (str *java-home* "/lib/rt.jar"))

(defn resource-entry-symbol
  "creates a path based on a symbol
 
   (resource-entry-symbol 'code.test)
   => \"code/test.clj\"
 
   (resource-entry-symbol 'clojure.lang.AFn)
   => \"clojure/lang/AFn.class\""
  {:added "3.0"}
  ([sym]
   (let [sym-str (-> (str sym)
                     (.replaceAll "\\." *sep*)
                     (.replaceAll "-" "_"))
         f-char (-> sym-str (std.string.common/split (re-pattern *sep*)) last first)]

     (str sym-str
          (if (<= (int \A) (int f-char) (int \Z))
            ".class"
            ".clj")))))

(defn resource-entry
  "creates a entry-path based on input
 
   (resource-entry \"hello/world.txt\")
   => \"hello/world.txt\"
 
   (resource-entry 'version-clj.core)
   => \"version_clj/core.clj\"
 
   (resource-entry java.io.File)
   => \"java/io/File.class\""
  {:added "3.0"}
  ([x]
   (condp = (type x)
     String x
     Symbol (resource-entry-symbol x)
     Class (-> (.getName ^Class x)
               (.replaceAll "\\." *sep*)
               (str  ".class")))))
