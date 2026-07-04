(ns indigo.server.api-browser-test
  (:require [indigo.server.api-browser :refer :all]
            [code.project :as project]
            [hara.lang :as l]
            [clojure.string :as str])
  (:use code.test))

^{:refer indigo.server.api-browser/list-namespaces :added "4.0"}
(fact "returns a sorted sequence of namespace strings for a language"
  (require 'js.core)
  (let [namespaces (list-namespaces :js)]
    (and (coll? namespaces)
         (every? string? namespaces)
         (boolean (some #{"js.core"} namespaces))))
  => true)

^{:refer indigo.server.api-browser/list-components :added "4.0"}
(fact "returns component metadata for a loaded language namespace"
  (require 'js.core)
  (let [components (list-components :js "js.core")]
    (and (coll? components)
         (every? map? components)
         (pos? (count components))))
  => true)

^{:refer indigo.server.api-browser/get-any-entry :added "4.1"}
(fact "retrieves a book entry for an existing component"
  (require 'js.core)
  (let [book  (l/get-book (l/default-library) :js)
        entry (get-any-entry book "js.core" "AggregateError")]
    (and (some? entry)
         (map? entry)))
  => true)

^{:refer indigo.server.api-browser/get-component :added "4.0"}
(fact "returns source string for an existing component"
  (require 'js.core)
  (get-component :js "js.core" "AggregateError")
  => string?)

^{:refer indigo.server.api-browser/scan-namespaces :added "4.1"}
(fact "returns a map of languages to namespace strings"
  (let [result (scan-namespaces)]
    (and (map? result)
         (every? keyword? (keys result))
         (every? coll? (vals result))
         (every? (partial every? string?) (vals result))))
  => true)

^{:refer indigo.server.api-browser/list-clj-namespaces :added "4.1"}
(fact "returns a sorted vector of clojure namespace strings"
  (let [namespaces (list-clj-namespaces)]
    (and (vector? namespaces)
         (every? string? namespaces)
         (= namespaces (sort namespaces))))
  => true)

^{:refer indigo.server.api-browser/list-clj-vars :added "4.1"}
(fact "lists public vars for a known namespace"
  (let [vars (list-clj-vars "clojure.core")]
    (and (coll? vars)
         (every? string? vars)
         (= vars (sort vars))
         (boolean (some #{"+" "map"} vars))))
  => true)

^{:refer indigo.server.api-browser/get-clj-var-source :added "4.1"}
(fact "returns source for a known clojure var"
  (get-clj-var-source "clojure.core" "+")
  => string?

  (str/includes? (get-clj-var-source "clojure.core" "+") "defn")
  => true)

^{:refer indigo.server.api-browser/get-namespace-source :added "4.1"}
(fact "returns source for a project namespace"
  (get-namespace-source "indigo.server.api-browser")
  => string?

  (str/includes? (get-namespace-source "indigo.server.api-browser")
                 "defn list-namespaces")
  => true)

^{:refer indigo.server.api-browser/list-test-namespaces :added "4.1"}
(fact "lists registered test namespaces"
  (let [tests (list-test-namespaces)]
    (and (coll? tests)
         (every? string? tests)))
  => true)

^{:refer indigo.server.api-browser/list-test-facts :added "4.1"}
(fact "lists facts for the current test namespace"
  (let [facts (list-test-facts "indigo.server.api-browser-test")]
    (and (coll? facts)
         (every? string? facts)))
  => true)

^{:refer indigo.server.api-browser/get-test-fact-source :added "4.1"}
(fact "returns a string for a fact source lookup"
  (get-test-fact-source "indigo.server.api-browser-test" "dummy-fact")
  => string?)

^{:refer indigo.server.api-browser/list-tests-for-var :added "4.1"}
(fact "returns a collection when looking up tests for a var"
  (list-tests-for-var "indigo.server.api-browser" "list-namespaces")
  => coll?)

^{:refer indigo.server.api-browser/list-libraries :added "4.0"}
(fact "returns a sequence of library names"
  (let [libs (list-libraries)]
    (and (coll? libs)
         (every? string? libs)))
  => true)

^{:refer indigo.server.api-browser/component-metadata :added "4.0"}
(fact "returns metadata map for a component"
  (require 'js.core)
  (component-metadata :js "js.core" "AggregateError")
  => map?)

^{:refer indigo.server.api-browser/component-preview :added "4.0"}
(fact "returns same source as get-component"
  (require 'js.core)
  (= (component-preview :js "js.core" "AggregateError")
     (get-component :js "js.core" "AggregateError"))
  => true)

^{:refer indigo.server.api-browser/emit-component :added "4.1"}
(fact "emits component source as target language"
  (require 'js.core)
  (emit-component :js "js.core" "AggregateError")
  => string?)

^{:refer indigo.server.api-browser/search-components :added "4.0"}
(fact "searches components matching a query"
  (require 'js.core)
  (let [results (search-components :js "trace")]
    (and (vector? results)
         (every? map? results)
         (boolean (some #(= "trace-data" (:component %)) results))))
  => true)

^{:refer indigo.server.api-browser/save-namespace-source :added "4.1"}
(fact "writes source to the registered namespace file"
  (let [tmp     (java.io.File/createTempFile "api-browser-save" ".clj")
        content ";; saved content"]
    (try
      (with-redefs [project/all-files (fn [& _] {'test.api-browser-save (.getAbsolutePath tmp)})
                    project/project   (constantly {:source-paths ["src"]
                                                   :test-paths   ["test"]})]
        (save-namespace-source "test.api-browser-save" content))
      (= (slurp tmp) content)
      (finally
        (.delete tmp))))
  => true)

^{:refer indigo.server.api-browser/get-completions :added "4.1"}
(fact "returns completion suggestions for a prefix"
  (let [completions (get-completions "clojure.core" "map")]
    (and (seq? completions)
         (every? string? completions)
         (every? #(str/starts-with? % "map") completions)))
  => true)

^{:refer indigo.server.api-browser/scaffold-test :added "4.1"}
(fact "returns a status map for scaffolding"
  (let [result (scaffold-test "nonexistent.namespace.12345")]
    (and (map? result)
         (contains? #{"ok" "error"} (:status result))))
  => true)

^{:refer indigo.server.api-browser/get-doc-path :added "4.1"}
(fact "returns a map indicating whether a doc path was found"
  (get-doc-path "code.doc")
  => map?

  (contains? (get-doc-path "code.doc") :found)
  => true)

^{:refer indigo.server.api-browser/get-file-content :added "4.1"}
(fact "reads file content, handles empty and missing paths"
  (let [tmp (java.io.File/createTempFile "api-browser-content" ".txt")]
    (try
      (spit tmp "hello world")
      (and (= (get-file-content (.getAbsolutePath tmp)) "hello world")
           (= (get-file-content "") ";; File path is empty")
           (= (get-file-content "/nonexistent/path/abc.txt")
              ";; File not found: /nonexistent/path/abc.txt"))
      (finally
        (.delete tmp))))
  => true)

^{:refer indigo.server.api-browser/resolve-paths :added "4.1"}
(fact "resolves an existing path or namespace to File objects"
  (let [tmp-file (java.io.File/createTempFile "api-browser-resolve" ".clj")
        tmp-test (java.io.File/createTempFile "api-browser-resolve-test" ".clj")
        ns-sym   'test.api-browser-resolve
        test-sym 'test.api-browser-resolve-test]
    (try
      (with-redefs [project/all-files (fn [& _] {ns-sym   (.getAbsolutePath tmp-file)
                                                 test-sym (.getAbsolutePath tmp-test)})
                    project/project   (constantly {:source-paths ["src"]
                                                   :test-paths   ["test"]})]
        (and (= (count (resolve-paths (.getAbsolutePath tmp-file))) 1)
             (every? #(instance? java.io.File %) (resolve-paths (.getAbsolutePath tmp-file)))
             (= 2 (count (resolve-paths "test.api-browser-resolve")))
             (every? #(instance? java.io.File %) (resolve-paths "test.api-browser-resolve"))
             (empty? (resolve-paths "nonexistent.namespace.12345"))))
      (finally
        (.delete tmp-file)
        (.delete tmp-test))))
  => true)

^{:refer indigo.server.api-browser/delete-path :added "4.1"}
(fact "deletes registered file and directory paths"
  (let [tmp-file (java.io.File/createTempFile "api-browser-delete" ".txt")
        tmp-dir  (doto (java.io.File/createTempFile "api-browser-delete" ".dir")
                   (.delete)
                   (.mkdir))]
    (try
      (with-redefs [project/all-files (fn [& _] {'test.api-browser-delete-file (.getAbsolutePath tmp-file)
                                                 'test.api-browser-delete-dir  (.getAbsolutePath tmp-dir)})
                    project/project   (constantly {:source-paths ["src"]
                                                   :test-paths   ["test"]})]
        (and (= "ok" (:status (delete-path "test.api-browser-delete-file")))
             (not (.exists tmp-file))
             (= "ok" (:status (delete-path "test.api-browser-delete-dir")))
             (not (.exists tmp-dir))))
      (finally
        (when (.exists tmp-file) (.delete tmp-file))
        (when (.exists tmp-dir) (.delete tmp-dir)))))
  => true)

^{:refer indigo.server.api-browser/get-namespace-entries :added "4.1"}
(fact "returns a map with entries for a namespace"
  (get-namespace-entries "indigo.server.api-browser")
  => map?)
