(ns code.framework.common
  (:require [std.string :as str]
            [code.project :as project]
            [std.block :as block]
            [code.edit :as nav]))

(def ^:dynamic *path* nil)

(def ^:dynamic *test-full* false)

(def +test-vars+ #{:class :form :intro :line :ns :refer :test :unit :var
                   :let :use :replace :setup :teardown :id :check :sexp})

(defn display-entry
  "creates a map represenation of the entry
 
   (display-entry {:ns {:a {:test {}
                            :source {}}
                        :b {:test {}}
                        :c {:source {}}}})
   => {:source {:ns [:a :c]}
       :test {:ns [:a :b]}}"
  {:added "3.0"}
  ([entry]
   (reduce-kv (fn [out ns vars]
                (reduce (fn [out [name {:keys [source test]}]]
                          (cond-> out
                            source (update-in [:source ns] (fnil #(conj % name) []))
                            test   (update-in [:test ns] (fnil #(conj % name) []))))
                        out
                        (sort vars)))
              {}
              entry)))

(defrecord Entry []
  Object
  (toString [entry]
    (str "#code" (display-entry entry))))

(defmethod print-method Entry
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn entry
  "creates an entry for analysis
 
   (entry {:ns {:a {:test {}
                    :source {}}
                :b {:test {}}
                :c {:source {}}}})
   ;;#code{:source {:ns [:a :c]}, :test {:ns [:a :b]}}
   => code.framework.common.Entry"
  {:added "3.0"}
  ([m]
   (map->Entry m)))

(defn entry?
  "checks if object is an entry
 
   (entry? (entry {}))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Entry x)))

(defmulti test-frameworks
  "lists the framework that a namespace uses
 
   (test-frameworks 'code.test) => :fact
 
   (test-frameworks 'clojure.test) => :clojure"
  {:added "3.0"}
  (fn [sym] sym))

(defmethod test-frameworks :default
  [_])

(defmulti analyse-test
  "seed function for analyse-test
 
   (analyse-test :fact
                 (nav/parse-root (slurp \"test/code/framework_test.clj\")))"
  {:added "3.0"}
  (fn [type zloc] type))

(defn gather-meta
  "gets the metadata for a particular form
   (-> (nav/parse-string \"^{:refer clojure.core/+ :added \\\"1.1\\\"}\\n(fact ...)\")
       nav/down nav/right nav/down
       gather-meta)
   => '{:added \"1.1\", :ns clojure.core, :var +, :refer clojure.core/+}"
  {:added "3.0"}
  ([nav]
   (if (-> nav nav/up nav/up nav/tag (= :meta))
     (let [mta (-> nav nav/up nav/left nav/value)
           sym (:refer mta)]
       (if sym
         (assoc mta
                :ns   (symbol (str (.getNamespace ^clojure.lang.Symbol sym)))
                :var  (symbol (name sym))))))))

(defn gather-string
  "creates correctly spaced code string from normal docstring
 
   (-> (nav/parse-string \"\\\"hello\\nworld\\nalready\\\"\")
       (gather-string)
       (block/string))
   => \"\\\"hello\\n  world\\n  already\\\"\""
  {:added "3.0"}
  ([nav]
   (block/block (->> (nav/value nav)
                     (str/split-lines)
                     (map-indexed (fn [i s]
                                    (str (if (zero? i) "" "  ")
                                         (str/trim-left s))))
                     (str/join "\n")))))

(defn line-lookup
  "creates a function lookup for the project"
  {:added "3.0"}
  ([ns analysis]
   (reduce-kv (fn [out func {:keys [source test] :as  attrs}]
                (let [{:keys [row end-row]} (or (:line source)
                                                (:line test))
                      row (if (:line test) (- row 1) row)]
                  (reduce (fn [out i]
                            (assoc out i func))
                          out
                          (range row (inc end-row)))))
              {}
              (get analysis (project/source-ns ns)))))
