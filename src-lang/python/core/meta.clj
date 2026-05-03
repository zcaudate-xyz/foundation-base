(ns python.core.meta
  (:require [clojure.string]
            [std.fs :as fs]
            [std.html :as html]
            [std.lib.env :as env]
            [std.lib.os :as os]))


(def +root+ "assets/python.core")

(def +html-url+ "https://docs.python.org/3.8/library/functions.html")
(def +html-path+ (str +root+ "/builtins.html"))
(def +edn-path+  (str +root+ "/builtins.edn"))

(defn fetch-html
  "fetches the doc containing builtin descriptions"
  {:added "3.0"}
  []
  (os/sh "wget" +html-url+ "-O" "builtins.html"
        {:root (str "resources/" +root+)}))

(defn get-html
  "gets from file or fetch from source"
  {:added "3.0"}
  ([]
   (or (if-let [res (env/sys:resource +html-path+)]
         (slurp res))
       (do (fetch-html)
           (get-html)))))

(defn build-props
  "helper function for individual functions"
  {:added "3.0"}
  ([node]
   (let [name (html/text (html/select node "code.sig-name"))
         args (mapv html/text (html/select node ".sig-param"))
         doc  (html/text (html/select-first node "dd"))]
     {:name name
      :args args
      :doc doc})))

(defn build-builtins
  "build builtin map from html"
  {:added "3.0"}
  ([]
   (let [html (get-html)
         outline  (html/parse (clojure.string/trim html))
         functions (html/select outline "dl.function")
         props (mapv build-props functions)]
     props)))

(defn get-builtins
  "gets the builtin map"
  {:added "3.0"}
  ([]
   (let [builtins (or (if-let [edn (env/sys:resource +edn-path+)]
                        (read-string edn))
                      (build-builtins))]
     builtins)))

(defn clean
  "cleans html and edn function"
  {:added "3.0"}
  ([]
   (fs/delete (fs/path "resources/" +html-path+))
   (fs/delete (fs/path "resources/" +edn-path+))))

(defn create
  "creates the meta descriptions for the builtins"
  {:added "3.0"}
  ([]
   (fs/create-directory "resources/assets/python.core")
   (spit (str "resources/" +html-path+)
         (fetch-html))
   (spit (str "resources/" +edn-path+)
         (build-builtins))))

(comment
  (create)
  (build-builtins)
  (get-builtins))
