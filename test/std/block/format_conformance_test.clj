(ns test.std.block.format-conformance-test
  (:use code.test)
  (:require [std.block.format :as format]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [std.lib :as h]))

(defn- cljfmt-format
  "Formats a Clojure string using cljfmt (assumes cljfmt is on classpath).
   Requires a :cljfmt alias in deps.edn pointing to cljfmt's main function."
  [code-str]
  (let [temp-file (doto (java.io.File/createTempFile "cljfmt-test" ".clj")
                    (.deleteOnExit))]
    (spit temp-file code-str)
    (let [{:keys [exit out err]} (shell/sh "clojure" "-M:cljfmt" "fix" (.getAbsolutePath temp-file))]
      (if (zero? exit)
        (slurp temp-file)
        (throw (ex-info (str "cljfmt failed: " err) {:code-str code-str :error err}))))))

(defn- compare-with-cljfmt
  "Compares the output of std.block.format with cljfmt's output."
  [code-str]
  (let [formatted-by-std-block (format/format code-str)
        formatted-by-cljfmt (cljfmt-format code-str)]
    formatted-by-std-block => formatted-by-cljfmt))

^{:refer std.block.format/format :added "4.0"}
(fact "conforms to cljfmt for simple defn"
  (compare-with-cljfmt "(defn my-fn [a b] (+ a b))"))

(fact "conforms to cljfmt for a simple list"
  (compare-with-cljfmt "(+ 1 2 3)"))

(fact "conforms to cljfmt for a nested list"
  (compare-with-cljfmt "(defn my-fn [a b] (let [x 1] (+ a b x)))"))

(fact "conforms to cljfmt for a map"
  (compare-with-cljfmt "{:a 1 :b 2 :c 3}"))

(fact "conforms to cljfmt for a vector"
  (compare-with-cljfmt "[1 2 3]"))

(fact "conforms to cljfmt for a comment"
  (compare-with-cljfmt ";; This is a comment\n(defn my-fn [] 1)"))

(fact "conforms to cljfmt for metadata"
  (compare-with-cljfmt "^:private (defn my-fn [] 1)"))
