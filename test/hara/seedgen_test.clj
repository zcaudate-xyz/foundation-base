(ns hara.seedgen-test
  (:require [code.project :as project]
            [std.fs :as fs]
            [hara.seedgen.form-bench :as form-bench]
            [hara.seedgen.form-infile :as form-infile]
            [hara.seedgen :refer :all])
  (:use code.test))

(def ^:private +seedgen-spacing-source+
  (str "(ns xt.sample.spacing-test\n"
       "  (:use code.test)\n"
       "  (:require [hara.lang :as l]))\n\n"
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
       "  (:require [hara.lang :as l]))\n\n"
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
       "  (:require [hara.lang :as l]))\n\n"
       "^{:seedgen/root {:all true\n"
       "                 :langs [:js :lua :python]\n"
       "                 :python {:extra [[xt.lang.common-promise :as p]]}}}\n"
       "(l/script- :js\n"
       "  {:runtime :basic\n"
       "   :require [[xt.lang.spec-base :as xt]\n"
       "             [xt.lang.common-repl :as repl]]})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"adds language extras\"\n"
       "  (!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(def ^:private +seedgen-extra-require-format+
  #"(?ms):require \[\[xt\.lang\.spec-base :as xt\]\n\s+\[xt\.lang\.common-repl :as repl\]\n\s+\[python\.core\.common-promise :as p\]\]")

(def ^:private +seedgen-script-extra-source+
  (str "(ns xt.sample.script-extra-test\n"
       "  (:use code.test)\n"
       "  (:require [hara.lang :as l]))\n\n"
       "^{:seedgen/root {:all true\n"
       "                 :langs [:python]\n"
       "                 :python {:extra [[python.lib.driver-sqlite :as py-sqlite]]}}}\n"
       "(l/script- :js\n"
       "  {:runtime :basic\n"
       "   :require [[xt.lang.spec-base :as xt]\n"
       "             [xt.lang.common-repl :as repl]\n"
       "             ^{:seedgen/extra true}\n"
       "             [js.lib.driver-sqlite :as js-sqlite]]})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"drops inline script extras for other languages\"\n"
       "  (!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(def ^:private +seedgen-meta-shorthand-source+
  (str "(ns xt.sample.meta-test\n"
       "  (:use code.test)\n"
       "  (:require [hara.lang :as l]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"preserves shorthand metadata\"\n"
       "  ^*(!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(def ^:private +seedgen-wait-source+
  (str "(ns xt.sample.wait-test\n"
       "  (:use code.test)\n"
       "  (:require [hara.lang :as l]\n"
       "            [xt.lang.common-notify :as notify]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"removes wrapped runtime checks\"\n"
       "  (notify/wait-on :js\n"
       "    42)\n"
       "  => 42)\n"))

(def ^:private +seedgen-selector-source+
  "(ns xt.sample.selector)\n")

(def ^:private +seedgen-selector-test-source+
  (str "(ns xt.sample.selector-test\n"
       "  (:use code.test)\n"
       "  (:require [hara.lang :as l]))\n\n"
       "^{:seedgen/root {:all true}}\n"
       "(l/script- :js {:runtime :basic})\n\n"
       "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
       "(fact \"selector sample\"\n"
       "  (!.js (+ 1 2 3))\n"
       "  => 6)\n"))

(def ^:private +seedgen-quiet-print+
  {:function false
   :item false
   :result false
   :summary false})

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

(defn- seedgen-script-extra-context
  [prefix]
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "script_extra_test.clj"))
        lookup   {'xt.sample.script-extra-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (spit path +seedgen-script-extra-source+)
    {:root root
     :path path
     :bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/python/sample/script_extra_test.clj"))
     :lookup lookup
     :project project}))

(defn- seedgen-wait-context
  [prefix]
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "wait_test.clj"))
        lookup   {'xt.sample.wait-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test"]}]
    (spit path +seedgen-wait-source+)
    {:root root
     :path path
     :lookup lookup
     :project project}))

(defn- seedgen-selector-context
  [prefix]
  (let [root      (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                    (make-array java.nio.file.attribute.FileAttribute 0)))
        src-dir   (doto (java.io.File. root "src/xt/sample")
                    (.mkdirs))
        test-dir  (doto (java.io.File. root "test/xt/sample")
                    (.mkdirs))
        src-path  (.getAbsolutePath (java.io.File. src-dir "selector.clj"))
        test-path (.getAbsolutePath (java.io.File. test-dir "selector_test.clj"))
        lookup    {'xt.sample.selector src-path
                   'xt.sample.selector-test test-path}
        project   {:root (.getAbsolutePath root)
                   :source-paths ["src"]
                   :test-paths ["test"]}]
    (spit src-path +seedgen-selector-source+)
    (spit test-path +seedgen-selector-test-source+)
    {:root root
     :src-path src-path
     :test-path test-path
     :lookup lookup
     :project project}))

(defn- seedgen-transform-context
  [prefix]
  (let [root      (.toFile (java.nio.file.Files/createTempDirectory prefix
                                                                    (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir  (doto (java.io.File. root "test/xt/sample")
                    (.mkdirs))
        path      (.getAbsolutePath (java.io.File. test-dir "transform_test.clj"))
        bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/lua/sample/transform_test.clj"))
        lookup    {'xt.sample.transform-test path}
        project   {:root (.getAbsolutePath root)
                   :test-paths ["test"]}]
    (spit path (str "(ns xt.sample.transform-test\n"
                    "  (:use code.test)\n"
                    "  (:require [hara.lang :as l]))\n\n"
                    "^{:seedgen/root {:all true :langs [:lua]}}\n"
                    "(l/script- :js {:runtime :basic})\n\n"
                    "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
                    "  :setup [(def +out+\n"
                    "            {:value 1\n"
                    "             :nested [1 2 3]})]}\n"
                    "(fact \"applies self-referential transforms once\"\n"
                    "  ^{:seedgen/base {:lua {:transform {+out+ (l/as-lua +out+)}}}}\n"
                    "  (!.js +out+)\n"
                    "  => +out+)\n"))
    {:root root
     :path path
     :bench-path bench-path
     :lookup lookup
     :project project}))

^{:refer hara.seedgen/seedgen-root :added "4.1"}
(fact "runs the public root lookup task without crashing on scalar results"
  (seedgen-root '[xt.sample])

  (seedgen-root '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))

^{:refer hara.seedgen/seedgen-list :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen/seedgen-readforms :added "4.1"}
(fact "returns summary information for public seedgen readforms analysis"
  (seedgen-readforms '[xt.sample.train-002] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items 1
                :results 1
                :total number?}))

^{:refer hara.seedgen/seedgen-benchlist :added "4.1"}
(fact "TODO")

^{:refer hara.seedgen/seedgen-incomplete :added "4.1"}
(fact "returns summary information for incomplete seedgen tasks"
  (seedgen-incomplete '[xt.sample] {:print {:result true :summary true}})

  (seedgen-incomplete '[xt.sample] {:return :summary})
  => (contains {:errors 0
                :warnings 0
                :items number?
                :results number?
                :total number?}))

^{:refer hara.seedgen/seedgen-langremove :added "4.1"}
(fact "langremove removes wrapped runtime checks added by langadd"
  (let [{:keys [root path lookup project]} (seedgen-wait-context "seedgen-langremove-wait")]
    (try
      (form-infile/seedgen-langadd 'xt.sample.wait
                                   {:lang [:python :lua] :write true}
                                   lookup
                                   project)
      (form-infile/seedgen-langremove 'xt.sample.wait
                                      {:lang [:python :lua] :write true}
                                      lookup
                                      project)
      (let [content (slurp path)]
        [(boolean (re-find #"\(l/script- :js" content))
         (boolean (re-find #"notify/wait-on :js" content))
         (boolean (re-find #":langs" content))
         (boolean (re-find #"\(l/script- :python" content))
         (boolean (re-find #"\(l/script- :lua" content))
         (boolean (re-find #"notify/wait-on :python" content))
         (boolean (re-find #"notify/wait-on :lua" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [true true false false false false false])

^{:refer hara.seedgen/seedgen-langadd :added "4.1"}
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

^{:refer hara.seedgen/seedgen-langadd :added "4.1"}
(fact "langadd applies root-script extra requires for generated languages"
  (let [{:keys [root path lookup project]} (seedgen-extra-context "seedgen-langadd-extra")]
    (try
      (form-infile/seedgen-langadd 'xt.sample.extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp path)]
        [(boolean (re-find #"\(l/script- :python" content))
         (boolean (re-find +seedgen-extra-require-format+ content))
         (boolean (re-find #"\(!\.py \(\+ 1 2 3\)\)" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer hara.seedgen/seedgen-langadd :added "4.1"}
(fact "langadd drops inline script extras from non-target runtimes"
  (let [{:keys [root path lookup project]} (seedgen-script-extra-context "seedgen-langadd-script-extra")]
    (try
      (form-infile/seedgen-langadd 'xt.sample.script-extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp path)]
        [(count (re-seq #"\(l/script- :python" content))
         (count (re-seq #"\[python\.lib\.driver-sqlite :as py-sqlite\]" content))
         (count (re-seq #"\[js\.lib\.driver-sqlite :as js-sqlite\]" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [1 1 1])

^{:refer hara.seedgen/seedgen-langadd :added "4.1"}
(fact "bulk seedgen tasks only return test namespaces"
  (let [{:keys [root lookup project]} (seedgen-selector-context "seedgen-task-selector")]
    (try
      (let [langadd-keys (-> (seedgen-langadd '[xt.sample.selector]
                                              {:lang [:dart]
                                               :return :results
                                               :print +seedgen-quiet-print+}
                                              lookup
                                              project)
                             keys
                             sort
                             vec)
            benchadd-keys (-> (seedgen-benchadd '[xt.sample.selector]
                                                {:lang [:dart]
                                                 :rename '{xt [xtbench :lang]}
                                                 :return :results
                                                 :print +seedgen-quiet-print+}
                                                lookup
                                                project)
                              keys
                              sort
                              vec)
            _ (form-infile/seedgen-langadd 'xt.sample.selector
                                           {:lang [:dart] :write true}
                                           lookup
                                           project)
            _ (form-bench/seedgen-benchadd 'xt.sample.selector
                                           {:lang [:dart]
                                            :rename '{xt [xtbench :lang]}
                                            :write true}
                                           lookup
                                           project)]
        [langadd-keys
         benchadd-keys
         (-> (seedgen-langremove '[xt.sample.selector]
                                 {:lang [:dart]
                                  :return :results
                                  :print +seedgen-quiet-print+}
                                 lookup
                                 project)
             keys
             sort
             vec)
         (-> (seedgen-benchremove '[xt.sample.selector]
                                  {:lang [:dart]
                                   :rename '{xt [xtbench :lang]}
                                   :return :results
                                   :print +seedgen-quiet-print+}
                                  lookup
                                  project)
             keys
             sort
             vec)])
      (finally
        (fs/delete root {:recursive true}))))
  => ['[xt.sample.selector-test]
      '[xt.sample.selector-test]
      '[xt.sample.selector-test]
      '[xt.sample.selector-test]])

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
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
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"summary branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6)\n"))
      (let [output (#'hara.seedgen/seedgen-benchadd-summary
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

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
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

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd applies root-script extra requires for generated bench namespaces"
  (let [{:keys [root bench-path lookup project]} (seedgen-extra-context "seedgen-benchadd-extra")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find #"\(l/script- :python" content))
         (boolean (re-find +seedgen-extra-require-format+ content))
         (boolean (re-find #"\(!\.py \(\+ 1 2 3\)\)" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [true true true])

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd recognizes `:ref` metadata and applies target transforms"
  (let [root      (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-ref"
                                                                    (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir  (doto (java.io.File. root "test/xt/sample")
                    (.mkdirs))
        path      (.getAbsolutePath (java.io.File. test-dir "ref_test.clj"))
        bench-path (.getAbsolutePath (java.io.File. root "test/xtbench/python/sample/ref_test.clj"))
        lookup    {'xt.sample.ref-test path}
        project   {:root (.getAbsolutePath root)
                   :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.ref-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true\n"
                      "                 :langs [:python]\n"
                      "                 :js {:extra [[js.lib.driver-sqlite :as js-sqlite]]}\n"
                      "                 :python {:extra [[python.lib.driver-sqlite :as py-sqlite]]}}}\n"
                      "(l/script- :js\n"
                      "  {:runtime :basic\n"
                      "   :require [[xt.protocol.impl.connection-sql :as dbsql]\n"
                      "             ^{:seedgen/extra true}\n"
                      "             [js.lib.driver-sqlite :as js-sqlite]]})\n\n"
                      "^{:ref xt.sample.ref/driver :added \"4.1\"}\n"
                      "(fact \"applies transforms for :ref metadata\"\n"
                      "  ^{:seedgen/base {:python {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}}}\n"
                      "  (dbsql/connect (js-sqlite/driver) {})\n"
                      "  => nil)\n"))
      (form-bench/seedgen-benchadd 'xt.sample.ref
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find #"\^\{:ref xt\.sample\.ref/driver :added \"4\.1\"\}" content))
         (boolean (re-find #"\(dbsql/connect \(py-sqlite/driver\) \{\}\)" content))
         (boolean (re-find #"\(js-sqlite/driver\)" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [true true false])

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd drops inline script extras from non-target runtimes"
  (let [{:keys [root bench-path lookup project]} (seedgen-script-extra-context "seedgen-benchadd-script-extra")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.script-extra
                                   {:lang [:python] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(count (re-seq #"\(l/script- :python" content))
         (count (re-seq #"\[python\.lib\.driver-sqlite :as py-sqlite\]" content))
         (count (re-seq #"\[js\.lib\.driver-sqlite :as js-sqlite\]" content))])
      (finally
         (fs/delete root {:recursive true}))))
  => [1 1 0])

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
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

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
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

^{:refer hara.seedgen.form-infile/apply-item-transform-form :added "4.1"}
(fact "form transforms rewrite each original match once while preserving layout"
  (#'hara.seedgen.form-infile/apply-item-transform-form
   "(fact \"single-pass\"\n  (!.js\n    +out+)\n  => +out+)"
   (symbol "+out+")
   '(l/as-lua +out+))
  => "(fact \"single-pass\"\n  (!.js\n    (l/as-lua +out+))\n  => (l/as-lua +out+))")

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd applies self-referential transforms once"
  (let [{:keys [root bench-path lookup project]} (seedgen-transform-context "seedgen-benchadd-transform")]
    (try
      (form-bench/seedgen-benchadd 'xt.sample.transform
                                   {:lang [:lua] :write true}
                                   lookup
                                   project)
      (let [content (slurp bench-path)]
        [(boolean (re-find #":setup \[\(def \+out\+\n\s+\{:value 1\n\s+:nested \[1 2 3\]\}\)\]" content))
         (boolean (re-find #"=> \(l/as-lua \+out\+\)" content))
         (boolean (re-find #"\(l/as-lua \(l/as-lua \+out\+\)\)" content))])
      (finally
        (fs/delete root {:recursive true}))))
  => [true true false])

^{:refer hara.seedgen/seedgen-benchadd :added "4.1"}
(fact "benchadd keeps all xt.db.text.sql-call call facts"
  (let [proj   (project/project)
        lookup (project/file-lookup proj)
        output (seedgen-benchadd '[xt.db.text.sql-call]
                                 {:lang [:lua :python :dart]
                                  :return :results
                                  :print +seedgen-quiet-print+}
                                 lookup
                                 proj)
        result (get output 'xt.db.text.sql-call-test)]
    [(:functions result)
     (mapv :lang (:outputs result))
     (mapv :ns (:outputs result))
     (count (:outputs result))])
  => '[[call-api call-format-input call-format-query call-raw decode-return]
       [:lua :python :dart]
       [xtbench.lua.db.text.sql-call-test
        xtbench.python.db.text.sql-call-test
        xtbench.dart.db.text.sql-call-test]
       3])

^{:refer hara.seedgen/seedgen-benchremove :added "4.1"}
(fact "TODO")
