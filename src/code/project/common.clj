(ns code.project.common)

(def ^:dynamic *type* :clj)

(def ^:dynamic *test-suffix* "-test")

(def +defaults+ {:source-paths     ["src"]
                 :test-paths       ["test"]
                 :resource-paths   ["resources"]
                 :target-dir       "target"
                 :java-output-path "target/classes"})

(def type-lookup {:clj  {:extension ".clj"}
                  :cljs {:extension ".cljs"}})

(defn artifact
  "returns the artifact map given a symbol
 
   (artifact 'tahto/tahto)
   => '{:name tahto/tahto, :artifact \"tahto\", :group \"tahto\"}"
  {:added "3.0"}
  ([full]
   (let [group    (or (namespace full)
                      (str full))
         artifact (name full)]
     {:name full
      :artifact artifact
      :group group})))

(defonce ^:dynamic *memory* (atom {}))

(defonce ^:dynamic *lookup* (atom {}))
