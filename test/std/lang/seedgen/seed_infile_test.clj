(ns std.lang.seedgen.seed-infile-test
  (:use code.test)
  (:require [code.project :as project]
            [std.lang.seedgen.seed-common :as common]
            [std.lang.seedgen.seed-infile :as seed-infile]))

^{:refer std.lang.seedgen.seed-infile/seedgen-root :added "4.1"}
(fact "returns an explicit error result when the test file is missing"


  (project/in-context
   (seed-infile/seedgen-root 'xt.sample.train-001-test {}))
  => :js
  
  (project/in-context
   (seed-infile/seedgen-root 'xt.sample.missing-test {}))
  => (contains {:status :error
                :data :no-test-file}))

^{:refer std.lang.seedgen.seed-infile/seedgen-list :added "4.1"}
(fact "returns an empty list when a test file only declares the seedgen root"
  (project/in-context
   (seed-infile/seedgen-list 'xt.sample.train-001-test {}))
  => []

  (let [tmp (java.io.File/createTempFile "seedgen-infile" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.multi-test path}]
    (try
      (spit path (str "(ns sample.multi-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n"))
      (seed-infile/seedgen-list 'sample.multi-test {} lookup nil)
      (finally
        (.delete tmp))))
  => [:lua :python])

^{:refer std.lang.seedgen.seed-infile/seedgen-incomplete :added "4.1"}
(fact "reports facts that are not covered by the seedgen root language"
  (let [tmp (java.io.File/createTempFile "seedgen-incomplete" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.incomplete-test path}]
    (try
      (spit path (str "(ns sample.incomplete-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n"
                      "  :setup [(!.js (+ 1 2 3))]}\n"
                      "(fact \"covered from setup\"\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.common-spec/example.B :added \"4.1\"}\n"
                      "(fact \"TODO\")\n\n"
                      "^{:refer xt.lang.common-spec/example.C :added \"4.1\"\n"
                      "  :teardown [(!.lua (+ 1 2 3))]}\n"
                      "(fact \"covered from teardown\"\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.common-spec/example.D :added \"4.1\"}\n"
                      "(fact \"covered from notify wait\"\n"
                      "  (notify/wait-on :dt\n"
                      "    (repl/notify 1))\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.common-spec/example.E :added \"4.1\"}\n"
                      "(fact \"covered from matching notify wait\"\n"
                      "  (notify/wait-on :js\n"
                      "    (repl/notify 1))\n"
                      "  \"TODO\")\n"))
      (->> (seed-infile/seedgen-incomplete 'sample.incomplete-test {} lookup nil)
           keys
           set)
      (finally
        (.delete tmp))))
  => #{'xt.lang.common-spec/example.B
       'xt.lang.common-spec/example.C
       'xt.lang.common-spec/example.D})

^{:refer std.lang.seedgen.seed-infile/seedgen-removelang :added "4.1"}
(fact "purges targeted seedgen runtimes while preserving the seedgen root"
  (let [tmp (java.io.File/createTempFile "seedgen-removelang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.purge-test path}
        project {:root root}
        write-sample! (fn []
                        (spit path (str "(ns sample.purge-test\n"
                                        "  (:use code.test)\n"
                                        "  (:require [std.lang :as l]))\n\n"
                                        "^{:seedgen/root {:all true}}\n"
                                        "(l/script- :js {:runtime :basic})\n\n"
                                        "(l/script- :lua {:runtime :basic})\n\n"
                                        "(l/script- :python {:runtime :basic})\n\n"
                                        "^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))\n"
                                        "          (!.lua (+ 1 2 3))\n"
                                        "          (!.python (+ 1 2 3))]\n"
                                        "  :teardown [(!.lua (+ 1 2 3))]}\n"
                                        "(fact \"runtime specific branches\"\n"
                                        "  (!.js (+ 1 2 3))\n"
                                        "  => 6\n\n"
                                        "  (!.lua (+ 1 2 3))\n"
                                        "  => 6\n\n"
                                        "  (!.python (+ 1 2 3))\n"
                                        "  => 6)\n\n"
                                        "^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"TODO\")\n")))]
    (try
       (write-sample!)
       (seed-infile/seedgen-removelang 'sample.purge-test {:lang :all :write true} lookup project)
       [(seed-infile/seedgen-list 'sample.purge-test {} lookup nil)
        (-> (common/seedgen-fact-forms path)
            (get 'xt.lang.common-spec/example.A)
            common/seedgen-coverage-langs
            set)
        (slurp path)]
       (finally
         (.delete tmp))))
  => [[] #{:js}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true}}\n(l/script- :js {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-removelang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.purge-test path}
        project {:root root}
        write-sample! (fn []
                        (spit path (str "(ns sample.purge-test\n"
                                        "  (:use code.test)\n"
                                        "  (:require [std.lang :as l]))\n\n"
                                        "^{:seedgen/root {:all true}}\n"
                                        "(l/script- :js {:runtime :basic})\n\n"
                                        "(l/script- :lua {:runtime :basic})\n\n"
                                        "(l/script- :python {:runtime :basic})\n\n"
                                        "^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))\n"
                                        "          (!.lua (+ 1 2 3))\n"
                                        "          (!.python (+ 1 2 3))]}\n"
                                        "(fact \"runtime specific branches\"\n"
                                        "  (!.js (+ 1 2 3))\n"
                                        "  => 6\n\n"
                                        "  (!.lua (+ 1 2 3))\n"
                                        "  => 6\n\n"
                                        "  (!.python (+ 1 2 3))\n"
                                        "  => 6)\n")))]
    (try
       (write-sample!)
       (seed-infile/seedgen-removelang 'sample.purge-test {:lang :lua :write true} lookup project)
       [(seed-infile/seedgen-list 'sample.purge-test {} lookup nil)
        (-> (common/seedgen-fact-forms path)
            (get 'xt.lang.common-spec/example.A)
            common/seedgen-coverage-langs
            set)
        (slurp path)]
       (finally
         (.delete tmp))))
  => [[:python] #{:js :python}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"] 

  (let [tmp (java.io.File/createTempFile "seedgen-removelang" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.purge-test path}]
    (try
      (spit path (str "(ns sample.purge-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n"))
      (seed-infile/seedgen-removelang 'sample.purge-test {:lang [:js]} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :cannot-purge-root
                :lang :js}))
