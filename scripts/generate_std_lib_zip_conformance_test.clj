(ns generate-std-lib-zip-conformance-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn parse-args
  [args]
  (if (< (count args) 2)
    (do (println "Usage: lein run -m clojure.main/main scripts/generate_std_lib_zip_conformance_test.clj <corpus-path> <output-path>")
        (System/exit 1))
    {:corpus (first args)
     :output (second args)}))

(defn fixture-function
  [fixture]
  (-> (:id fixture)
      (str/split #"/")
      last
      (str/replace #"-\d+$" "")))

(defn escape-string
  [s]
  (str "\""
       (-> s
           (str/replace "\\" "\\\\")
           (str/replace "\"" "\\\"")
           (str/replace "\n" "\\n")
           (str/replace "\t" "\\t"))
       "\""))

(defn format-value
  [value]
  (cond
    (nil? value) "nil"
    (boolean? value) (str value)
    (number? value) (str value)
    (keyword? value) (str value)
    (symbol? value) (str value)
    (string? value) (escape-string value)
    (and (seq? value) (= 'quote (first value)))
    (str "(quote " (format-value (second value)) ")")
    (seq? value)
    (str "(" (str/join " " (map format-value value)) ")")
    (vector? value)
    (str "[" (str/join " " (map format-value value)) "]")
    (set? value)
    (str "#{" (str/join " " (map format-value value)) "}")
    (map? value)
    (str "{" (str/join " " (mapcat (fn [[k v]] [(format-value k) (format-value v)]) value)) "}")
    :else (pr-str value)))

(defn fixture-test-map
  [fixture]
  (str "  {:name \"" (:id fixture) "\"\n"
       "   :test (fn [] (evaluate-in-hara {:ns '" (:ns fixture) " :expr " (escape-string (:expr fixture)) "}))\n"
       "   :expected " (format-value (:expected fixture)) "}"))

(defn generate-test-file
  [fixtures output-path]
  (let [grouped (group-by fixture-function fixtures)
        sections (for [[function fixtures] (sort-by key grouped)]
                   (str "^{:refer std.lib.zip/" function " :added \"3.0\"}\n"
                        "(Test/run\n"
                        " [" (str/join "\n  " (map fixture-test-map fixtures)) "]\n"
                        " conformance-test-check)"))
        content (str "(ns std.lib.zip.conformance-test\n"
                     "  (:require [std.lib.zip.conformance :refer :all]\n"
                     "            [std.lib.zip]\n"
                     "            [code.test.base.process :as process]\n"
                     "            [code.test.checker.common :as checker]))\n\n"
                     "(defn conformance-test-check\n"
                     "  [test expected]\n"
                     "  (let [result (process/check test expected)]\n"
                     "    (if (not (Test/passed? result))\n"
                     "      (println :failure\n"
                     "               {:actual (Test/actual result)\n"
                     "                :expected (Test/expected result)\n"
                     "                :failures (Test/failures result)}))\n"
                     "    result))\n\n"
                     "^{:refer std.lib.zip.conformance/load-fixtures :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected true\n"
                     "   :name \"loads the shared fixture corpus\"\n"
                     "   :test (fn [] (let [fixtures (load-fixtures)]\n"
                     "                  (and (vector? fixtures)\n"
                     "                       (= \"std/lib/zip/zipper?-000\" (:id (first fixtures))))))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/evaluate-in-hara :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected false\n"
                     "   :name \"evaluates a fixture expression in the Hara runtime\"\n"
                     "   :test (fn [] (evaluate-in-hara {:ns 'std.lib.zip :expr \"(zipper? 1)\"}))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/compare-results :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected {:id \"id\" :expected 1 :hara 1 :foundation 1 :match true}\n"
                     "   :name \"reports a match when values are equal\"\n"
                     "   :test (fn [] (compare-results \"id\" 1 1 1))}\n"
                     "  {:expected {:id \"id\" :expected 1 :hara 1 :foundation 2 :match false}\n"
                     "   :name \"reports a mismatch when values differ\"\n"
                     "   :test (fn [] (compare-results \"id\" 1 1 2))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/foundation-root :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected true\n"
                     "   :name \"resolves the Foundation root to a non-empty path\"\n"
                     "   :test (fn [] (let [path (foundation-root)]\n"
                     "                  (and (string? path)\n"
                     "                       (pos? (count path)))))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/foundation-fixture-path :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected true\n"
                     "   :name \"resolves the fixture path to a non-empty path\"\n"
                     "   :test (fn [] (let [path (foundation-fixture-path)]\n"
                     "                  (and (string? path)\n"
                     "                       (pos? (count path)))))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/run-foundation :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected true\n"
                     "   :name \"invokes the Foundation runner and returns a result map\"\n"
                     "   :test (fn [] (let [results (run-foundation (foundation-fixture-path) (foundation-root))]\n"
                     "                  (and (map? results)\n"
                     "                       (not (nil? (get results \"std/lib/zip/zipper?-000\"))))))}]\n"
                     " conformance-test-check)\n\n"
                     "^{:refer std.lib.zip.conformance/run-corpus :added \"3.0\"}\n"
                     "(Test/run\n"
                     " [{:expected true\n"
                     "   :name \"reports the std.lib.zip corpus as conformant\"\n"
                     "   :test (fn [] (:conformant? (run-corpus)))}]\n"
                     " conformance-test-check)\n\n"
                     ";; Fixture-level conformance tests generated from core/spec/fixtures/std-lib-zip/conformance.edn\n"
                     ";; Regenerate with: lein run -m clojure.main/main scripts/generate_std_lib_zip_conformance_test.clj\n"
                     ";;                  ../../technology/hara/core/spec/fixtures/std-lib-zip/conformance.edn\n"
                     ";;                  ../../technology/hara/core/lib/test/std/lib/zip/conformance_test.hal\n\n"
                     (str/join "\n\n" sections)
                     "\n")]
    (spit output-path content)))

(let [{:keys [corpus output]} (parse-args *command-line-args*)]
  (-> (slurp corpus)
      (edn/read-string)
      (generate-test-file output))
  (println "Generated" output))
