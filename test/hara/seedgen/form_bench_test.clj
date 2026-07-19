(ns hara.seedgen.form-bench-test
  (:use code.test)
  (:require [std.fs :as fs]
            [hara.seedgen.form-bench :as form-bench]))

^{:refer hara.seedgen.form-bench/seedgen-benchlist :added "4.1"}
(fact "derives bench namespaces from the seedgen root and requested runtimes"
  (let [tmp (java.io.File/createTempFile "seedgen-benchlist" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'xt.sample.multi-test path
                'kmi.lang.multi-test path
                'sample.multi-test path}]
    (try
      (spit path (str "(ns xt.sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
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

      (form-bench/seedgen-benchlist 'kmi.lang.multi-test
                                    {:lang [:python :dart]}
                                    lookup
                                    nil)
      => '[xtbench.python.kmi.lang.multi-test
           xtbench.dart.kmi.lang.multi-test]

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
                        "  (:require [hara.lang :as l]))\n\n"
                        "(l/script- :js {:runtime :basic})\n"))
        (form-bench/seedgen-benchlist 'xt.sample.multi-test
                                      {}
                                      lookup
                                      nil))
      => (contains {:status :error
                    :data :no-seedgen-root})
      (finally
        (.delete tmp)))))

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-kmi-path}
(fact "writes KMI benches beneath xtbench without touching the canonical seed"
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-kmi"
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/kmi/lang")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "sample_test.clj"))
        source   (str "^{:no-test true}\n"
                      "(ns kmi.lang.sample-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true :langs [:python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer kmi.lang.sample/value :id kmi-value-1 :added \"4.1\"}\n"
                      "(fact \"value one\" (!.js (+ 1 2)) => 3)\n\n"
                      "^{:refer kmi.lang.sample/value :id kmi-value-2 :added \"4.1\"}\n"
                      "(fact \"value two\" (!.js (+ 2 3)) => 5)\n")
        lookup   {'kmi.lang.sample-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test-lang"]}]
    (try
      (spit path source)
      (let [output     (form-bench/seedgen-benchadd 'kmi.lang.sample-test
                                                    {:lang [:python]
                                                     :write true}
                                                    lookup
                                                    project)
            bench-path (str (fs/path root "test-lang/xtbench/python/kmi/lang/sample_test.clj"))
            bench-text (slurp bench-path)]
        [(-> output :outputs first (select-keys [:lang :ns :path :updated]))
         (= source (slurp path))
         (fs/exists? bench-path)
         (str/includes? bench-text "^{:no-test true}")
         (str/includes? bench-text "(ns xtbench.python.kmi.lang.sample-test")
         (= 2 (count (re-seq #"!\\.python" bench-text)))
         (not (str/includes? bench-text "!.js"))])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :python
       :ns 'xtbench.python.kmi.lang.sample-test
       :path "test-lang/xtbench/python/kmi/lang/sample_test.clj"
       :updated true}
      true
      true
      true
      true
      true
      true])

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"}
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
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
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

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-test-root}
(fact "writes bench files to the same test root as the source test namespace"
  (let [root     (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-test-root"
                                                                   (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/xt/sample")
                   (.mkdirs))
        path     (.getAbsolutePath (java.io.File. test-dir "multi_test.clj"))
        lookup   {'xt.sample.multi-test path}
        project  {:root (.getAbsolutePath root)
                  :test-paths ["test" "test-lang"]}]
    (try
      (spit path (str "(ns xt.sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :dart {:runtime :twostep})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"runtime branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.dart (+ 1 2 3))\n"
                      "  => 6)\n"))
      (let [output        (form-bench/seedgen-benchadd 'xt.sample.multi-test
                                                       {:lang [:dart]
                                                        :write true}
                                                       lookup
                                                       project)
            bench-path    (str (fs/path root "test-lang/xtbench/dart/sample/multi_test.clj"))
            fallback-path (str (fs/path root "test/xtbench/dart/sample/multi_test.clj"))]
        [(-> output :outputs first (select-keys [:lang :path :updated]))
         (fs/exists? bench-path)
         (fs/exists? fallback-path)])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :dart
       :path "test-lang/xtbench/dart/sample/multi_test.clj"
       :updated true}
      true
      false])

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-fact-layout}
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
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
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
  => "(ns samplebench.python.sample.format-test\n  (:use code.test)\n  (:require [hara.lang :as l]))\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"runtime branches\"\n\n  (!.python\n    (+ 1 2 3))\n  => 6)\n")

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-derived-runtime}
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
                      "  (:require [hara.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example-f :added \"4.1\"}\n"
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
  => "(ns samplebench.lua.sample.derived-test\n  (:use code.test)\n  (:require [hara.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-f :added \"4.1\"}\n(fact \"expect can be customised\"\n\n  (!.lua\n    (xt/x:offset 10))\n  => 11)\n")

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-setup-overrides}
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
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example-g :added \"4.1\"\n"
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
  => "(ns samplebench.lua.sample.setup-test\n  (:use code.test)\n  (:require [hara.lang :as l]))\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-g :added \"4.1\"\n  :setup [(!.lua (setup-lua))]}\n(fact \"setup bench outcomes\"\n\n  (!.lua 1)\n  => 1)\n")

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-global-fixtures}
(fact "renders global fact setup and teardown in bench targets"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-global"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test/sample")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "global_test.clj"))
        lookup  {'xt.sample.global-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test"]}]
    (try
      (spit path (str "(ns xt.sample.global-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:dart]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(fact:global\n"
                      " {:skip (not (env/program-exists? \"supabase\"))\n"
                      "  :setup [(!.js (setup-js))]\n"
                      "  :teardown [(!.js (teardown-js))]})\n\n"
                      "^{:refer xt.lang.spec-base/example-h :added \"4.1\"}\n"
                      "(fact \"global bench outcomes\"\n\n"
                      "  (!.js 1)\n"
                      "  => 1)\n"))
      (let [output (form-bench/seedgen-benchadd 'xt.sample.global-test
                                                {:rename '{xt [samplebench :lang]}
                                                 :lang [:dart]
                                                 :write true}
                                                lookup
                                                project)
            bench-path (str (fs/path root "test/samplebench/dart/sample/global_test.clj"))]
        [(-> output :outputs first (select-keys [:lang :ns :updated]))
         (let [content (slurp bench-path)]
           [(boolean (re-find #"\(fact:global" content))
            (boolean (re-find #":skip \(not \(env/program-exists\? \"supabase\"\)\)" content))
            (boolean (re-find #"\(!\.dt \(setup-js\)\)" content))
            (boolean (re-find #"\(!\.dt \(teardown-js\)\)" content))])])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :dart
       :ns 'samplebench.dart.sample.global-test
       :updated true}
      [true true true true]])

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-runtime-variant}
(fact "preserves the concrete runtime variant when a bench target is requested by family name"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-variant"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/xt/db/runtime")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "parity_roundtrip_test.clj"))
        lookup  {'xt.db.runtime.parity-roundtrip-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test-lang"]}]
    (try
      (spit path (str "(ns xt.db.runtime.parity-roundtrip-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true\n"
                      "                 :langs [:lua.nginx]\n"
                      "                 :js        {:extra [[js.net.conn-sqlite :as js-sqlite]]}\n"
                      "                 :lua.nginx {:extra [[lua.nginx.conn-sqlite :as lua-sqlite]]}}}\n"
                      "(l/script- :js\n"
                      "  {:runtime :basic\n"
                      "   :require [[xt.lang.spec-base :as xt]\n"
                      "             ^{:seedgen/extra true}\n"
                      "             [js.net.conn-sqlite :as js-sqlite]]})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"variant dispatch\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6)\n"))
      (let [output    (form-bench/seedgen-benchadd 'xt.db.runtime.parity-roundtrip-test
                                                   {:lang [:lua]
                                                    :write true}
                                                   lookup
                                                   project)
            entry     (-> output :outputs first)
            content   (slurp (str (fs/path root (:path entry))))]
        [(select-keys entry [:lang :runtime-lang :ns :updated])
         [(boolean (re-find #"\(l/script- :lua\.nginx" content))
          (boolean (re-find #"\(!\.lua \(\+ 1 2 3\)\)" content))
          (not (re-find #"\(!\.lua\.nginx" content))]])
      (finally
        (fs/delete root {:recursive true}))))
  => [{:lang :lua
       :runtime-lang :lua.nginx
       :ns 'xtbench.lua.db.runtime.parity-roundtrip-test
       :updated true}
      [true true true]])

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-replace-extra-requires}
(fact "replaces root-only extra requires with runtime-specific bench requires"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-extra"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/xt/db/runtime")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "parity_sqlite_test.clj"))
        lookup  {'xt.db.runtime.parity-sqlite-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test-lang"]}]
    (try
      (spit path (str "(ns xt.db.runtime.parity-sqlite-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true\n"
                      "                 :langs [:lua.nginx :dart]\n"
                      "                 :js        {:extra [[js.net.conn-sqlite :as js-sqlite]]}\n"
                      "                 :lua.nginx {:extra [[lua.nginx.conn-sqlite :as lua-sqlite]]}\n"
                      "                 :dart      {:extra [[dart.net.conn-sqlite :as dart-sqlite]]}}}\n"
                      "(l/script- :js\n"
                      "  {:runtime :basic\n"
                      "   :require [[xt.lang.spec-base :as xt]\n"
                      "             ^{:seedgen/extra true}\n"
                      "             [js.net.conn-sqlite :as js-sqlite]]})\n"))
      (let [output (form-bench/seedgen-benchadd 'xt.db.runtime.parity-sqlite-test
                                                {:lang [:lua :dart]
                                                 :write true}
                                                lookup
                                                project)
            content-by-lang
            (->> (:outputs output)
                 (map (fn [{:keys [lang path]}]
                        [lang (slurp (str (fs/path root path)))]))
                 (into {}))]
        [(:lua content-by-lang)
         (:dart content-by-lang)])
      (finally
        (fs/delete root {:recursive true}))))
  => [#"(?s)\(l/script- :(lua|lua\.nginx) .*?\[lua\.nginx\.driver-sqlite :as lua-sqlite\].*"
      #"(?s)\(l/script- :dart .*?\[dart\.lib\.driver-sqlite :as dart-sqlite\].*"])

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-remove-extra-requires}
(fact "removes seedgen extra requires from non-root bench runtimes"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-extra-remove"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/xt/db/runtime")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "parity_roundtrip_test.clj"))
        lookup  {'xt.db.runtime.parity-roundtrip-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test-lang"]}]
    (try
      (spit path (str "(ns xt.db.runtime.parity-roundtrip-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true\n"
                      "                 :langs [:lua.nginx]\n"
                      "                 :js        {:extra [[js.net.conn-sqlite :as js-sqlite]]}\n"
                      "                 :lua.nginx {:extra [[lua.nginx.conn-sqlite :as lua-sqlite]]}}}\n"
                      "(l/script- :js\n"
                      "  {:runtime :basic\n"
                      "   :require [[xt.lang.spec-base :as xt]\n"
                      "             ^{:seedgen/extra true}\n"
                      "             [js.net.conn-sqlite :as js-sqlite]]})\n"))
      (let [output (form-bench/seedgen-benchadd 'xt.db.runtime.parity-roundtrip-test
                                                {:lang [:lua]
                                                 :write true}
                                                lookup
                                                project)
            path   (-> output :outputs first :path)]
        (slurp (str (fs/path root path))))
      (finally
        (fs/delete root {:recursive true}))))
  => #(and (re-find #"\[lua\.nginx\.driver-sqlite :as lua-sqlite\]" %)
           (not (re-find #"\[js\.lib\.driver-sqlite :as js-sqlite\]" %))))

^{:refer hara.seedgen.form-bench/seedgen-benchadd :added "4.1"
  :id test-seedgen-benchadd-drop-extra-requires}
(fact "drops root-only extra requires even when the target runtime does not define a replacement"
  (let [root    (.toFile (java.nio.file.Files/createTempDirectory "seedgen-benchadd-extra-drop"
                                                                  (make-array java.nio.file.attribute.FileAttribute 0)))
        test-dir (doto (java.io.File. root "test-lang/xt/db/runtime")
                   (.mkdirs))
        path    (.getAbsolutePath (java.io.File. test-dir "parity_roundtrip_test.clj"))
        lookup  {'xt.db.runtime.parity-roundtrip-test path}
        project {:root (.getAbsolutePath root)
                 :test-paths ["test-lang"]}]
    (try
      (spit path (str "(ns xt.db.runtime.parity-roundtrip-test\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true\n"
                      "                 :langs [:ruby]\n"
                      "                 :js {:extra [[js.net.conn-sqlite :as js-sqlite]]}}}\n"
                      "(l/script- :js\n"
                      "  {:runtime :basic\n"
                      "   :require [[xt.lang.spec-base :as xt]\n"
                      "             ^{:seedgen/extra true}\n"
                      "             [js.net.conn-sqlite :as js-sqlite]]})\n"))
      (let [output (form-bench/seedgen-benchadd 'xt.db.runtime.parity-roundtrip-test
                                                {:lang [:ruby]
                                                 :write true}
                                                lookup
                                                project)
            path   (-> output :outputs first :path)]
        (slurp (str (fs/path root path))))
      (finally
        (fs/delete root {:recursive true}))))
  => #(not (re-find #"\[js\.lib\.driver-sqlite :as js-sqlite\]" %)))

^{:refer hara.seedgen.form-bench/seedgen-benchremove :added "4.1"}
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
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
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

^{:refer hara.seedgen.form-bench/seedgen-benchremove :added "4.1"
  :id test-seedgen-benchremove-explicit-runtime}
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
                      "  (:require [hara.lang :as l]))\n\n"
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
