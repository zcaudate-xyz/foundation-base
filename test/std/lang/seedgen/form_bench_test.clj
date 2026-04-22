(ns std.lang.seedgen.form-bench-test
  (:use code.test)
  (:require [std.fs :as fs]
            [std.lang.seedgen.form-bench :as form-bench]))

^{:refer std.lang.seedgen.form-bench/seedgen-benchlist :added "4.1"}
(fact "derives bench namespaces from the seedgen root and requested runtimes"
  (let [tmp (java.io.File/createTempFile "seedgen-benchlist" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'xt.sample.multi-test path
                'sample.multi-test path}]
    (try
      (spit path (str "(ns xt.sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n"))
      (form-bench/seedgen-benchlist 'xt.sample.multi-test
                                    {}
                                    lookup
                                    nil)
      => '[xtbench.js.sample.multi-test
           xtbench.lua.sample.multi-test
           xtbench.python.sample.multi-test]

      (form-bench/seedgen-benchlist 'sample.multi-test
                                    {:rename '{sample [samplebench :lang]}
                                     :lang [:python :js]}
                                    lookup
                                    nil)
      => '[samplebench.python.multi-test
           samplebench.js.multi-test]

      (form-bench/seedgen-benchlist 'sample.missing-test
                                    {}
                                    lookup
                                    nil)
      => (contains {:status :error
                    :data :no-test-file})

      (do
        (spit path (str "(ns xt.sample.multi-test\n"
                        "  (:use code.test)\n"
                        "  (:require [std.lang :as l]))\n\n"
                        "(l/script- :js {:runtime :basic})\n"))
        (form-bench/seedgen-benchlist 'xt.sample.multi-test
                                      {}
                                      lookup
                                      nil))
      => (contains {:status :error
                    :data :no-seedgen-root})
      (finally
        (.delete tmp)))))

^{:refer std.lang.seedgen.form-bench/seedgen-benchadd :added "4.1"}
(fact "creates bench files for the requested runtimes"
  (let [root   (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd"
                                                                 (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path   (.getAbsolutePath (java.io.File. test-dir "multi_test.clj"))
        lookup {'xt.sample.multi-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n"
                      "(fact \"runtime branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python (+ 1 2 3))\n"
                      "  => 6)\n"))
      (let [output (form-bench/seedgen-benchadd 'xt.sample.multi-test
                                                {:rename '{xt [samplebench :lang]}
                                                 :lang [:python]
                                                 :write true}
                                                lookup
                                                  project)
            bench-path (str (fs/path root "test/samplebench/python/sample/multi_test.clj"))]
         [(-> output :outputs first (select-keys [:lang :ns :updated]))
          (fs/exists? bench-path)
          (let [content (slurp bench-path)]
            [(boolean (re-find #"samplebench\.python\.sample\.multi-test" content))
             (boolean (re-find #"\(l/script- :python \{:runtime :basic\}\)" content))
             (boolean (re-find #"\(!\.python \(\+ 1 2 3\)\)" content))])])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :python
       :ns 'samplebench.python.sample.multi-test
       :updated true}
      true
      [true true true]])

^{:refer std.lang.seedgen.form-bench/seedgen-benchadd :added "4.1"}
(fact "preserves fact layout when generating bench files"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-format"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "format_test.clj"))
        lookup  {'xt.sample.format-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.format-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n"
                      "(fact \"runtime branches\"\n\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-bench/seedgen-benchadd 'xt.sample.format-test
                                   {:rename '{xt [samplebench :lang]}
                                    :lang [:python]
                                    :write true}
                                   lookup
                                   project)
      (slurp (str (fs/path root "test/samplebench/python/sample/format_test.clj")))
      (finally
       (fs/delete root {:recursive true}))))
  => "(ns samplebench.python.sample.format-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n(fact \"runtime branches\"\n\n  (!.python\n    (+ 1 2 3))\n  => 6)\n")

^{:refer std.lang.seedgen.form-bench/seedgen-benchadd :added "4.1"}
(fact "generates standalone bench namespaces using the langadd runtime derivation rules"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-derived"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "derived_test.clj"))
        lookup  {'xt.sample.derived-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.derived-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.common-spec :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example-f :added \"4.1\"}\n"
                      "(fact \"expect can be customised\"\n\n"
                      "  ^{:seedgen/base {:lua {:expect 11}}}\n"
                      "  (!.js\n"
                      "    (xt/x:offset 10))\n"
                      "  => 10)\n"))
      (form-bench/seedgen-benchadd 'xt.sample.derived-test
                                   {:rename '{xt [samplebench :lang]}
                                    :lang [:lua]
                                    :write true}
                                   lookup
                                   project)
      (slurp (str (fs/path root "test/samplebench/lua/sample/derived_test.clj")))
      (finally
        (fs/delete root {:recursive true}))))
  => "(ns samplebench.lua.sample.derived-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.common-spec :as xt]))\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example-f :added \"4.1\"}\n(fact \"expect can be customised\"\n\n  (!.lua\n    (xt/x:offset 10))\n  => 11)\n")

^{:refer std.lang.seedgen.form-bench/seedgen-benchadd :added "4.1"}
(fact "renders bench setup outcomes using unified seedgen base overrides"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-setup"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "setup_test.clj"))
        lookup  {'xt.sample.setup-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.setup-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example-g :added \"4.1\"\n"
                      "  :setup [^{:seedgen/base {:lua {:input (!.lua (setup-lua))}}}\n"
                      "          (!.js (setup-js))]}\n"
                      "(fact \"setup bench outcomes\"\n\n"
                      "  (!.js 1)\n"
                      "  => 1)\n"))
      (form-bench/seedgen-benchadd 'xt.sample.setup-test
                                   {:rename '{xt [samplebench :lang]}
                                    :lang [:lua]
                                    :write true}
                                   lookup
                                   project)
      (slurp (str (fs/path root "test/samplebench/lua/sample/setup_test.clj")))
      (finally
        (fs/delete root {:recursive true}))))
  => "(ns samplebench.lua.sample.setup-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example-g :added \"4.1\"\n  :setup [(!.lua (setup-lua))]}\n(fact \"setup bench outcomes\"\n\n  (!.lua 1)\n  => 1)\n")

^{:refer std.lang.seedgen.form-bench/seedgen-benchremove :added "4.1"}
(fact "removes selected bench files while preserving other runtimes"
  (let [root   (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchremove"
                                                                 (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path   (.getAbsolutePath (java.io.File. test-dir "multi_test.clj"))
        lookup {'xt.sample.multi-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n"
                      "(fact \"runtime branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-bench/seedgen-benchadd 'xt.sample.multi-test
                                   {:rename '{xt [samplebench :lang]}
                                    :write true}
                                   lookup
                                   project)
      (let [py-path (str (fs/path root "test/samplebench/python/sample/multi_test.clj"))
            js-path (str (fs/path root "test/samplebench/js/sample/multi_test.clj"))
            output  (form-bench/seedgen-benchremove 'xt.sample.multi-test
                                                    {:rename '{xt [samplebench :lang]}
                                                     :lang [:python]
                                                     :write true}
                                                    lookup
                                                    project)]
        [(-> output :outputs first (select-keys [:lang :ns :updated :exists]))
         (fs/exists? py-path)
         (fs/exists? js-path)])
      (finally
       (fs/delete root {:recursive true}))))
  => [{:lang :python
       :ns 'samplebench.python.sample.multi-test
        :updated true
        :exists true}
        false
        true])

^{:refer std.lang.seedgen.form-bench/seedgen-benchremove :added "4.1"}
(fact "removes explicitly requested bench runtimes even when they are not present in the seed source"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchremove-explicit"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "remove_test.clj"))
        lookup  {'xt.sample.remove-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.remove-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n"))
      (let [bench-path (fs/path root "test/samplebench/lua/sample/remove_test.clj")]
        (fs/create-directory (fs/parent bench-path))
        (spit (str bench-path) "(ns samplebench.lua.sample.remove-test)\n")
        [(-> (form-bench/seedgen-benchremove 'xt.sample.remove-test
                                             {:rename '{xt [samplebench :lang]}
                                              :lang [:lua]
                                              :write true}
                                             lookup
                                             project)
             :outputs
             first
             (select-keys [:lang :ns :updated :exists]))
         (fs/exists? (str bench-path))])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :lua
       :ns 'samplebench.lua.sample.remove-test
       :updated true
       :exists true}
      false])
