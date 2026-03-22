(ns jvm.classloader
  (:require [clojure.java.io :as io]
            [jvm.artifact :as artifact]
            [jvm.artifact.common :as base]
            [jvm.classloader.base-classloader :deps true]
            [jvm.classloader.common :as common]
            [jvm.classloader.url-classloader :deps true]
            [jvm.protocol :as protocol.classloader]
            [std.lib.invoke :as invoke]
            [std.object.query :as query])
  (:import (clojure.lang DynamicClassLoader RT) (java.net URL URLClassLoader)))

(defonce +base+ (ClassLoader/getSystemClassLoader))

(defn has-url?
  "checks whether the classloader has the following url
 
   (has-url? (fs/path \"src\"))
   => true"
  {:added "3.0"}
  ([path]
   (has-url? +base+ path))
  ([loader path]
   (protocol.classloader/-has-url? loader path)))

(defn get-url
  "returns the required url
 
   (get-url (fs/path \"src\"))
   ;;#object[java.net.URL 0x3d202d52 \"file:/Users/chris/Development/hara/hara/src/\"]
   => java.net.URL"
  {:added "3.0"}
  ([path]
   (get-url +base+ path))
  ([loader path]
   (protocol.classloader/-get-url loader path)))

(defn all-urls
  "returns all urls contained by the loader
 
   (all-urls)"
  {:added "3.0"}
  ([]
   (all-urls +base+))
  ([loader]
   (protocol.classloader/-all-urls loader)))

(defn add-url
  "adds a classpath to the loader"
  {:added "3.0"}
  ([path]
   (add-url +base+ path))
  ([loader path]
   (protocol.classloader/-add-url loader path)))

(defn remove-url
  "removes url from classloader"
  {:added "3.0"}
  ([path]
   (remove-url +base+ path))
  ([loader path]
   (protocol.classloader/-remove-url loader path)))

(defn delegation
  "returns a list of classloaders in order of top to bottom"
  {:added "3.0"}
  ([cl]
   (->> cl
        (iterate (fn [^ClassLoader cl] (.getParent cl)))
        (take-while #(satisfies? protocol.classloader/ILoader %))
        (reverse))))

(defn classpath
  "returns the classpath for the loader, including parent loaders"
  {:added "3.0"}
  ([]
   (classpath +base+))
  ([loader]
   (->> (delegation loader)
        (mapcat all-urls)
        (map #(.getFile ^java.net.URL %)))))

(defonce +clojure-jar+
  (->> (classpath)
       (filter #(.contains ^String % "/org/clojure/clojure/"))
       (first)))

(defonce +spec-jar+
  (->> (classpath)
       (filter #(.contains ^String % "/org/clojure/spec"))
       (first)))

(defn all-jars
  "gets all jars on the classloader"
  {:added "3.0"}
  ([] (all-jars +base+))
  ([loader]
   (->> (classpath loader)
        (filter #(.endsWith ^String % ".jar")))))

(defn all-paths
  "gets all paths on the classloader"
  {:added "3.0"}
  ([] (all-paths +base+))
  ([^ClassLoader loader]
   (->> (classpath loader)
        (remove #(.endsWith ^String % ".jar")))))

(defn ^java.net.URLClassLoader url-classloader
  "returns a `java.net.URLClassLoader` from a list of strings"
  {:added "3.0"}
  ([urls]
   (url-classloader urls +base+))
  ([urls parent]
   (URLClassLoader. (->> urls
                         (map common/to-url)
                         (into-array URL))
                    parent)))

(defn dynamic-classloader
  "creates a dynamic classloader instance"
  {:added "3.0"}
  ([]
   (dynamic-classloader []))
  ([urls]
   (dynamic-classloader urls +base+))
  ([urls parent]
   (DynamicClassLoader.
    (url-classloader (->> urls
                          (map common/to-url)
                          (into-array URL))
                     parent))))

(defonce ^java.util.concurrent.ConcurrentHashMap +class-cache+
  (query/apply-element clojure.lang.DynamicClassLoader "classCache" []))

(defonce ^java.lang.ref.ReferenceQueue +rq+
  (query/apply-element clojure.lang.DynamicClassLoader "rq" []))

(defn ^Class load-class
  "loads class from an external source"
  {:added "3.0"}
  ([x]
   (load-class x {}))
  ([x opts]
   (load-class x opts (dynamic-classloader)))
  ([x opts loader]
   (protocol.classloader/-load-class x loader opts)))

(defn unload-class
  "unloads a class from the current namespace"
  {:added "3.0"}
  ([name]
   (.remove +class-cache+ name) (clojure.lang.Util/clearCache +rq+ +class-cache+)))

(defmulti to-bytes
  "opens `.class` file from an external source"
  {:added "3.0"}
  (fn [x] (type x)))

(defmethod to-bytes java.io.InputStream
  ([stream]
   (let [o (java.io.ByteArrayOutputStream.)]
     (io/copy stream o)
     (.toByteArray o))))

(defmethod to-bytes String
  ([path]
   (to-bytes (io/input-stream path))))

(invoke/definvoke any-load-class
  "loads a class, storing class into the global cache"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [Class ClassLoader]}]
  ([^Class cls _ _]
   (let [ref (java.lang.ref.SoftReference. cls +rq+)]
     (.put +class-cache+ (.getName cls) ref))
   cls))

(invoke/definvoke dynamic-load-bytes
  "loads a class from bytes"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [(Class/forName "[B") DynamicClassLoader]}]
  ([^bytes bytes ^DynamicClassLoader loader {:keys [name source] :as opts}]
   (let [_   (clojure.lang.Util/clearCache +rq+ +class-cache+)
         cls (.defineClass loader name bytes source)]
     (any-load-class cls loader opts))))

(invoke/definvoke dynamic-load-string
  "loads a class from a path string"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [String DynamicClassLoader]}]
  ([^String path loader {:keys [entry-path] :as opts}]
   (cond (.endsWith path ".class")
         (-> (to-bytes path)
             (dynamic-load-bytes loader opts))

         (or (.endsWith path ".war")
             (.endsWith path ".jar"))
         (let [resource-name (base/resource-entry entry-path)
               rt    (java.util.jar.JarFile. path)
               entry  (.getEntry rt resource-name)
               stream (.getInputStream rt entry)]
           (-> stream
               (to-bytes)
               (dynamic-load-bytes loader opts))))))

(invoke/definvoke dynamic-load-coords
  "loads a class from a coordinate"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [clojure.lang.PersistentVector DynamicClassLoader]}]
  ([coordinates loader {:keys [entry-path] :as opts}]
   (dynamic-load-string (artifact/artifact :path coordinates)
                        loader
                        opts)))

