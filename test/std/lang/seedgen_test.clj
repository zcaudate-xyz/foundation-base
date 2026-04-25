(ns std.lang.seedgen-test
  (:require [std.fs :as fs]
            [std.lang.seedgen :refer :all])
  (:use code.test))

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
(fact "TODO")

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

^{:refer std.lang.seedgen/seedgen-benchremove :added "4.1"}
(fact "TODO")
