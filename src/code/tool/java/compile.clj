(ns code.tool.java.compile
  (:require [std.lib.sort :as sort]
            [std.fs :as fs]
            [code.project :as project]
            [jvm.classloader :as classloader]
            [std.string :as str]
            [std.lib :as h])
  (:import (java.util Arrays)
           (java.net URI)
           (java.io ByteArrayOutputStream)
           (javax.tools Diagnostic$Kind
                        Diagnostic
                        DiagnosticCollector
                        ForwardingJavaFileManager
                        JavaFileObject$Kind
                        SimpleJavaFileObject
                        ToolProvider)
           (org.objectweb.asm ClassReader))
  (:refer-clojure :exclude [supers]))

(defn path->class
  "Creates a class symbol from a file
 
   (path->class \"test/Dog.java\")
   => 'test.Dog
 
   (path->class \"test/Cat.class\")
   => 'test.Cat"
  {:added "3.0"}
  ([path]
   (-> (str path)
       (.replaceAll ".java" "")
       (.replaceAll ".class" "")
       (.replaceAll "/" ".")
       symbol)))

(defn class->path
  "creates a file path from a class
 
   (class->path test.Dog)
   => \"test/Dog.class\"
 
   (class->path 'test.Cat)
   => \"test/Cat.class\""
  {:added "3.0"}
  ([cls]
   (-> (str/to-string cls)
       (.replaceAll "\\." "/")
       (str ".class"))))

(defn java-sources
  "lists source classes in a project
 
   (->> (java-sources (project/project))
        (keys)
        (filter (comp #(.startsWith % \"test\") str))
        (sort))
   => (contains '[test.Cat test.Dog test.DogBuilder
                  test.Person test.PersonBuilder test.Pet])"
  {:added "3.0"}
  ([{dirs :java-source-paths}]
   (->> (mapcat (fn [dir]
                  (->> (fs/select dir {:include [".java"]})
                       (map (juxt #(->> %
                                        (fs/relativize (fs/path dir))
                                        path->class)
                                  identity))))
                dirs)
        (into {}))))

(defn class-object
  "creates a class object for use with compiler
 
   (class-object \"test.Cat\"
                 (fs/read-all-bytes \"target/classes/test/Cat.class\"))
   => javax.tools.SimpleJavaFileObject"
  {:added "3.0"}
  ([class-name baos]
   (proxy [SimpleJavaFileObject]
          [(URI/create (str "string:///"
                            (.replace ^String class-name "." "/")
                            (.extension JavaFileObject$Kind/CLASS)))
           JavaFileObject$Kind/CLASS]
     (openOutputStream [] baos))))

(defn class-manager
  "creates a `ForwardingJavaFileManager`
 
   (let [compiler  (javax.tools.ToolProvider/getSystemJavaCompiler)
         collector (javax.tools.DiagnosticCollector.)
         manager   (.getStandardFileManager compiler collector nil nil)
         cache     (atom {})]
     (class-manager manager cache))
   => javax.tools.ForwardingJavaFileManager"
  {:added "3.0"}
  ([manager cache]
   (proxy [ForwardingJavaFileManager] [manager]
     (getJavaFileForOutput [location class-name kind sibling]
       (cond (= kind JavaFileObject$Kind/CLASS)
             (let [baos (ByteArrayOutputStream.)
                   _ (swap! cache assoc class-name baos)]
               (class-object class-name baos))

             :else
             (throw (ex-info "Expects bytecode output" {:location location
                                                        :class-name class-name
                                                        :kind kind
                                                        :sibling sibling})))))))

(defn supers
  "finds supers of a class given it's bytecode
 
   (supers (fs/read-all-bytes \"target/classes/test/Cat.class\"))
   => #{\"java.lang.Object\" \"test.Pet\"}"
  {:added "3.0"}
  ([^bytes bytecode]
   (let [cls (ClassReader. bytecode)]
     (->> (apply list
                 (.getSuperName  cls)
                 (.getInterfaces cls))
          (map (fn [^String s] (.replace s "/" ".")))
          set))))

(defn javac-output
  "displays output of compilation"
  {:added "3.0"}
  ([^DiagnosticCollector collector]
   (doseq [^Diagnostic d (.getDiagnostics collector)]
     (h/explode
      (if (.getSource d)
        (println
         (format "%s: %s, line %d: %s\n"
                 (.toString (.getKind d))
                 ;;(.getName ^Class (.getSource d))
                 (.getLineNumber d)
                 (.getMessage d nil))))))))

(defn javac-process
  "processes compilation, either reloading 
 
   (javac-process {\"test.Pet\" (fs/read-all-bytes \"target/classes/test/Pet.class\")
                   \"test.Cat\" (fs/read-all-bytes \"target/classes/test/Cat.class\")}
                 {:output \"target/classes\"
                   :reload false})"
  {:added "3.0"}
  ([bytes-lu {:keys [reload output] :as opts}]
   (let [out (when output
               {:output (h/map-entries (fn [[cls bytes]]
                                         (let [path (fs/path output (class->path cls))
                                               _    (fs/create-directory (fs/parent path))]
                                           [cls (fs/write-all-bytes path bytes)]))
                                       bytes-lu)})
         import (when reload
                  (let [supers-lu (h/map-vals (fn [bytes]
                                                (->> (supers bytes)
                                                     (filter bytes-lu)
                                                     set))
                                              bytes-lu)
                        order (sort/topological-sort supers-lu)
                        _     (doseq [cls (reverse order)]
                                (classloader/unload-class cls))]
                    {:order order
                     :reload (->> order
                                  (map (fn [cls]
                                         [cls (classloader/load-class (bytes-lu cls)
                                                                      {:name cls})]))
                                  (into {}))}))]
     (merge out import))))

(defn javac
  "compiles classes using the built-in compiler
 
   (javac '[test]
          {:output \"target/classes\"
           :reload false})
   ;;=> outputs `.class` files in target directory"
  {:added "3.0"}
  ([]
   (javac nil))
  ([entry]
   (let [[packages entry] (cond (nil? entry)
                                [nil {}]

                                (vector? entry)
                                [entry {}]

                                (map? entry)
                                [nil entry]

                                :else (throw (ex-info "Invalid input:" {:input entry})))]
     (javac packages entry)))
  ([packages {:keys [output reload]
              :or   {reload false} :as opts}]
   (let [project   (project/project)
         sources   (java-sources project)
         output    (if (contains? opts :output)
                     output
                     (or (:java-output-path project)
                         "target/classes"))]
     (javac packages (assoc opts :reload reload :output output) sources)))
  ([packages {:keys [reload output] :as opts} sources]
   (let [sources   (cond->> sources
                     packages (filter (fn [[k v]]
                                        (some #(.startsWith (str k) (str %))
                                              packages))))
         compiler  (ToolProvider/getSystemJavaCompiler)
         collector (DiagnosticCollector.)
         manager   (.getStandardFileManager compiler collector nil nil)
         cache     (atom {})
         cls-manager  (class-manager manager cache)
         arr       (->> (vals sources)
                        (map #(.toFile ^java.nio.file.Path %))
                        (into-array)
                        (Arrays/asList)
                        (.getJavaFileObjectsFromFiles manager))
         success?  (.call (.getTask compiler
                                    *err*
                                    cls-manager
                                    collector
                                    (Arrays/asList (make-array String 0))
                                    nil
                                    arr))
         _         (javac-output collector)]
     (if success?
       (javac-process (h/map-vals #(.toByteArray ^ByteArrayOutputStream %) @cache) opts)
       :not-compiled))))

(comment
  (javac '[hara.lib])
  (javac '[hara.lang]))
