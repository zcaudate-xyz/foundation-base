(ns code.project
  (:require [std.fs :as fs]
            [code.project.common :as common]
            [code.project.lein :as lein]
            [code.project.shadow :as shadow]
            [std.lib :as h :refer [definvoke]]))

(def ^:dynamic *include* [".clj$"])

(def project-lookup
  {lein/*project-file* lein/project
   shadow/*shadow-file* shadow/project})

(defn project-file
  "returns the current project file
 
   (project-file)
   => \"project.clj\""
  {:added "3.0"}
  ([]
   (->> [lein/*project-file*
         shadow/*shadow-file*]
        (filter fs/exists?)
        (first))))

(definvoke project-map
  "returns the project map
 
   (project-map (fs/path \"project.clj\"))"
  {:added "3.0"}
  [:recent {:key str
            :compare fs/last-modified}]
  ([path]
   ((project-lookup (str (fs/file-name path)) path))))

(defn project
  "returns project options as a map
 
   (project)
   => (contains {:name symbol?
                 :dependencies vector?})"
  {:added "3.0"}
  ([] (project (project-file)))
  ([path]
   (cond (nil? path)
         (throw (ex-info "Cannot find project" {:path nil}))

         (h/input-stream? path)
         (lein/project path)

         :else
         (project-map (fs/path path)))))

(defn project-name
  "returns the name, read from the project map
 
   (project-name)
   => symbol?"
  {:added "3.0"}
  ([] (:name (project)))
  ([path]
   (:name (project path))))

(defn exclude
  "helper function for excluding certain namespaces
 
   (exclude '{lucid.legacy.analyzer :a
              lucid.legacy :a
              lib.aether :b}
            [\"lucid.legacy\"])
   => '{lib.aether :b}"
  {:added "3.0"}
  ([lookup exclusions]
   (reduce-kv (fn [out ns v]
                (let [nss (str ns)
                      exclude? (->> exclusions
                                    (map (fn [ex]
                                           (.startsWith nss ex)))
                                    (some true?))]
                  (if exclude?
                    out
                    (assoc out ns v))))
              {}

              lookup)))

(definvoke lookup-ns
  "fast lookup for all-files function
 
   (first (lookup-ns (lookup-path (h/ns-sym))))
   => 'code.project-test"
  {:added "3.0"}
  [:recent {:key str
            :compare fs/last-modified}]
  ([path]
   (let [ns (fs/file-namespace path)]
     (swap! common/*lookup* assoc ns path)
     [ns (str path)])))

(defn lookup-path
  "looks up the path given the `ns`
 
   (lookup-path (h/ns-sym))"
  {:added "3.0"}
  ([ns]
   (get @common/*lookup* ns)))

(defn all-files
  "returns all the clojure files in a directory
 
   (count (all-files [\"test\"]))
   => number?
 
   (-> (all-files [\"test\"])
       (get 'code.project-test))
   => #(.endsWith ^String % \"/test/code/project_test.clj\")"
  {:added "3.0"}
  ([] (all-files ["."]))
  ([paths] (all-files paths {}))
  ([paths opts]
   (all-files paths opts (project)))
  ([paths opts project]
   (let [filt (-> {:include *include*}
                  (merge opts)
                  (update-in [:exclude]
                             conj
                             fs/link?))
         result (->> paths
                     (map #(fs/path (:root project) %))
                     (mapcat #(fs/select % filt))
                     (pmap lookup-ns)
                     (into {}))]
     (dissoc result nil))))

(defn file-lookup
  "creates a lookup of namespaces and files in the project
 
   (-> (file-lookup (project))
       (get 'code.project))
   => #(.endsWith ^String % \"/src/code/project.clj\")"
  {:added "3.0"}
  ([] (file-lookup (project)))
  ([project]
   (all-files (concat (:source-paths project)
                      (:test-paths project))
              {}
              project)))

(defn file-suffix
  "returns the file suffix for a given type
 
   (file-suffix) => \".clj\"
 
   (file-suffix :cljs) => \".cljs\""
  {:added "3.0"}
  ([] (file-suffix common/*type*))
  ([type]
   (-> (common/type-lookup type) :extension)))

(defn test-suffix
  "returns the test suffix
 
   (test-suffix) => \"-test\""
  {:added "3.0"}
  ([] common/*test-suffix*)
  ([s] (alter-var-root #'common/*test-suffix*
                       (constantly s))))

(defn file-type
  "returns the type of file according to the suffix
 
   (file-type \"project.clj\")
   => :source
 
   (file-type \"test/code/project_test.clj\")
   => :test"
  {:added "3.0"}
  ([path]
   (cond (.endsWith (str path)
                    (str (munge (test-suffix))
                         (file-suffix)))
         :test

         :else
         :source)))

(defn sym-name
  "returns the symbol of the namespace
 
   (sym-name *ns*)
   => 'code.project-test
 
   (sym-name 'a)
   => 'a"
  {:added "3.0"}
  ([x]
   (cond (instance? clojure.lang.Namespace x)
         (.getName ^clojure.lang.Namespace x)

         (symbol? x)
         x

         :else
         (throw (ex-info "Only symbols and namespaces are supported" {:type (type x)
                                                                      :value x})))))

(defn source-ns
  "returns the source namespace
 
   (source-ns 'a) => 'a
   (source-ns 'a-test) => 'a"
  {:added "3.0"}
  ([ns]
   (let [sns (str (sym-name ns))
         suffix (test-suffix)
         sns (if (.endsWith (str sns) suffix)
               (subs sns 0 (- (count sns) (count suffix)))
               sns)]
     (symbol sns))))

(defn test-ns
  "returns the test namespace
 
   (test-ns 'a) => 'a-test
   (test-ns 'a-test) => 'a-test"
  {:added "3.0"}
  ([ns]
   (let [sns (str (sym-name ns))
         suffix (test-suffix)
         sns (if (.endsWith (str sns) suffix)
               sns
               (str sns suffix))]
     (symbol sns))))

(defmacro in-context
  "creates a local context for executing code functions
 
   (in-context ((fn [current params _ project]
                  current)))
   => 'code.project-test"
  {:added "3.0"}
  ([[func & args]]
   (let [project `(project)
         lookup  `(all-files ["src" "test"] {} ~'project)
         current `(.getName *ns*)
         params  `{}]
     (case (count args)
       0  `(let [~'project ~project]
             (~func ~current ~params ~lookup ~'project))
       1   (if (map? (first args))
             `(let [~'project ~project]
                (~func ~current ~(first args) ~lookup ~'project))
             `(let [~'project ~project]
                (~func ~(first args) ~params ~lookup ~'project)))
       2   `(let [~'project ~project]
              (~func ~@args ~lookup ~'project))))))

;; Project only options

(defn code-files
  "returns only the code files for the current project
 
   (code-files)"
  {:added "3.0"}
  ([]
   (all-files (concat (:test-paths common/+defaults+)
                      (:source-paths common/+defaults+)))))

(defn code-path
  "returns the path of the code
 
   (str (code-path (h/ns-sym) true))
   => \"test/code/project_test.clj\""
  {:added "3.0"}
  ([ns relative]
   (let [path (or (get @common/*lookup* ns)
                  (get (code-files) ns)
                  (throw (ex-info "Namespace does not exist" {:ns ns})))]
     (cond->> path
       relative (fs/relativize (fs/path "."))))))
