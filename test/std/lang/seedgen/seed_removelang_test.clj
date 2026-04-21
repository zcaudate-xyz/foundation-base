(ns std.lang.seedgen.seed-removelang-test
  (:use code.test)
  (:require [std.lang.seedgen.seed-common :as common]
            [std.lang.seedgen.seed-infile :as seed-infile]
            [std.lang.seedgen.seed-removelang :as seed-removelang]))

^{:refer std.lang.seedgen.seed-removelang/seedgen-removelang :added "4.1"}
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
      (seed-removelang/seedgen-removelang 'sample.purge-test {:lang :all :write true} lookup project)
      [(seed-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[] #{:js}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

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
      (seed-removelang/seedgen-removelang 'sample.purge-test {:lang :lua :write true} lookup project)
      [(seed-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:python] #{:js :python}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

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
      (seed-removelang/seedgen-removelang 'sample.purge-test {:lang [:js]} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :cannot-purge-root
                :lang :js}))
