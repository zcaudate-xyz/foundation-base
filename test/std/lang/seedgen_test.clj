(ns std.lang.seedgen-test
  (:require [std.fs :as fs]
            [std.lang.seedgen.form-bench :as form-bench]
            [std.lang.seedgen.form-infile :as form-infile]
            [std.lang.seedgen :refer :all])
  (:use code.test))

(def ^:private +seedgen-spacing-source+
  (str "(ns xt.sample.spacing-test\n"
       "  (:use code.test)\n"
       "  (:require [std.lang :as l]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example-h :added \"4.1\"\n"
       "  :setup [(def +setup-check+\n"
       "            (contains-in\n"
       "             {:setup true\n"
       "              :nested {:value 1}}))]\n"
       "  :teardown [(def +teardown-check+\n"
       "               (contains-in\n"
       "                {:teardown true\n"
       "                 :nested {:value 0}}))]}\n"
       "(fact \"metadata spacing\"\n\n"
       "  (!.js 1)\n"
       "  => 1)\n"))

(def ^:private +seedgen-setup-spacing+
  #"(?ms):setup \[\(def \+setup-check\+\n {8,}\(contains-in\n {8,}\{:setup true\n {8,}:nested \{:value 1\}\}\)\)\]")

(def ^:private +seedgen-teardown-spacing+
  #"(?ms):teardown \[\(def \+teardown-check\+\n {8,}\(contains-in\n {8,}\{:teardown true\n {8,}:nested \{:value 0\}\}\)\)\]")

(def ^:private +seedgen-suppress-source+
  (str "(ns xt.sample.suppress-test\n"
       "  (:use code.test)\n"
       "  (:require [std.lang :as l]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example-h :added \"4.1\"\n"
       "  :setup [^{:seedgen/base {:all {:suppress true}}}\n"
       "          (!.js \"suppressed-setup\")\n"
       "          (!.js \"retained-setup\")]}\n"
       "(fact \"suppressed bench setup\"\n"
       "  (!.js 1)\n"
       "  => 1)\n"))

(def ^:private +seedgen-extra-source+
  (str "(ns xt.sample.extra-test\n"
       "  (:use code.test)\n"
       "  (:require [std.lang :as l]))\n\n"
       "^{:seedgen/root {:all true\n"
       "                 :langs [:python]\n"
       "                 :python {:extra [[python.core.common-promise :as p]]}}}\n"
       "(l/script- :js\n"
       "  {:runtime :basic\n"
       "   :require [[xt.lang.spec-base :as xt]\n"
       "             [xt.lang.common-repl :as repl]]})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"adds language extras\"\n"
       "  (!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(def ^:private +seedgen-meta-shorthand-source+
  (str "(ns xt.sample.meta-test\n"
       "  (:use code.test)\n"
       "  (:require [std.lang :as l]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"preserves shorthand metadata\"\n"
       "  ^*(!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(defn- seedgen-spacing-context
  [prefix]
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "spacing_test.clj"))
        lookup   {'xt.sample.spacing-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (spit path +seedgen-spacing-source+)
    {:root root
     :path path
     :bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/dart/sample/spacing_test.clj"))
     :lookup lookup
     :project project}))

(defn- seedgen-suppress-context
  [prefix]
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "suppress_test.clj"))
        lookup   {'xt.sample.suppress-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (spit path +seedgen-suppress-source+)
    {:root root
     :path path
     :bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/dart/sample/suppress_test.clj"))
     :lookup lookup
     :project project}))

(defn- seedgen-extra-context
  [prefix]
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "extra_test.clj"))
        lookup   {'xt.sample.extra-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (spit path +seedgen-extra-source+)
    {:root root
     :path path
     :bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/python/sample/extra_test.clj"))
     :lookup lookup
     :project project}))

^{:refer std.lang.seedgen/seedgen-root :added "4.1"}
(fact "runs the public root lookup task without crashing on scalar results"
  (seedgen-root '[xt.sample])

  (seedgen-root '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))

^{:refer std.lang.seedgen/seedgen-list :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-readforms :added "4.1"}
(fact "returns summary information for public seedgen readforms analysis"
  (seedgen-readforms '[xt.sample.train-002] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items 1
                :results 1
                :total number?}))

^{:refer std.lang.seedgen/seedgen-benchlist :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-incomplete :added "4.1"}
(fact "returns summary information for incomplete seedgen tasks"
  (seedgen-incomplete '[xt.sample] {:print {:result true :summary true}})

  (seedgen-incomplete '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))

^{:refer std.lang.seedgen/seedgen-langremove :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen/seedgen-langadd :added "4.1"}
(fact "langadd preserves multiline setup and teardown indentation"
  (let [{:keys [root path lookup project]} (seedgen-spacing-context "seedgen-langadd-spacing")]
    (try
      (form-infile/seedgen-langadd 'xt.sample.spacing
                                   {:lang [:dart] :write true}
                                   lookup
                                   project)
      (let [content (slurp path)]
        [(boolean (re-find +seedgen-setup-spacing+ content))
         (boolean (re-find +seedgen-teardown-spacing+ content))
         (boolean (re-find #"\(!\.dt 1\)" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer std.lang.seedgen/seedgen-langadd :added "4.1"}
(fact "langadd applies root-script extra requires for generated languages"
  (let [{:keys [root path lookup project]} (seedgen-extra-context "seedgen-langadd-extra")]
    (try
      (form-infile/seedgen-langadd 'xt.sample.extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp path)]
        [(boolean (re-find #"\(l/script- :python" content))
         (boolean (re-find #"\[python\.core\.common-promise :as p\]" content))
         (boolean (re-find #"\(!\.py \(\+ 1 2 3\)\)" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd summary reports generated function refs while counting target namespaces"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-summary"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "summary_test.clj"))
        lookup  {'xt.sample.summary-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.summary-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"summary branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6)\n"))
      (let [output (#'std.lang.seedgen/seedgen-benchadd-summary
                    'xt.sample.summary-test
                    {:rename '{xt [samplebench :lang]}
                     :lang [:python]
                     :write true}
                    lookup
                    project)]
        [(:functions output)
         (:new output)])
      (finally
        (fs/delete root {:recursive true}))))
  => ['[example.A]
      '[samplebench.python.sample.summary-test]])

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd preserves multiline setup and teardown indentation"
  (let [{:keys [root bench-path lookup project]} (seedgen-spacing-context "seedgen-benchadd-spacing")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.spacing
                                   {:lang [:dart] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find +seedgen-setup-spacing+ content))
         (boolean (re-find +seedgen-teardown-spacing+ content))
         (boolean (re-find #"\(!\.dt 1\)" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd applies root-script extra requires for generated bench namespaces"
  (let [{:keys [root bench-path lookup project]} (seedgen-extra-context "seedgen-benchadd-extra")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find #"\(l/script- :python" content))
         (boolean (re-find #"\[python\.core\.common-promise :as p\]" content))
         (boolean (re-find #"\(!\.py \(\+ 1 2 3\)\)" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd skips suppressed setup items when generating bench files"
  (let [{:keys [root bench-path lookup project]} (seedgen-suppress-context "seedgen-benchadd-suppress")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.suppress
                                   {:lang [:dart] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find #"suppressed-setup" content))
         (boolean (re-find #"retained-setup" content))
         (boolean (re-find #"\(!\.dt 1\)" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [false true true])

^{:refer std.lang.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd preserves shorthand metadata on generated runtime checks"
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-meta"
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "meta_test.clj"))
        bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/dart/sample/meta_test.clj"))
        lookup   {'xt.sample.meta-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (try
      (spit path +seedgen-meta-shorthand-source+)
      (form-bench/seedgen-benchadd 'xt.sample.meta
                                   {:lang [:dart] :write true}
                                   lookup
                                   project)
      (slurp bench-path)
      (finally
        (fs/delete root {:recursive true}))))
  => #"(?ms)\^\*\(!\.dt \(\+ 1 2 3\)\)")

^{:refer std.lang.seedgen/seedgen-benchremove :added "4.1"}
(fact "TODO")
