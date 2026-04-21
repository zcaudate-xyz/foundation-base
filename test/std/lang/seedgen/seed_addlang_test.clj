(ns std.lang.seedgen.seed-addlang-test
  (:use code.test)
  (:require [std.lang.seedgen.seed-addlang :as seed-addlang]
            [std.lang.seedgen.seed-common :as common]
            [std.lang.seedgen.seed-infile :as seed-infile]))

^{:refer std.lang.seedgen.seed-addlang/seedgen-addlang :added "4.1"}
(fact "adds seedgen runtimes back from the seedgen root form"
  (let [tmp (java.io.File/createTempFile "seedgen-addlang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}
        write-sample! (fn []
                        (spit path (str "(ns sample.add-test\n"
                                        "  (:use code.test)\n"
                                        "  (:require [std.lang :as l]))\n\n"
                                        "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                                        "(l/script- :js {:runtime :basic})\n\n"
                                        "^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"runtime specific branches\"\n"
                                        "  (!.js (+ 1 2 3))\n"
                                        "  => 6)\n\n"
                                        "^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"TODO\")\n")))]
    (try
      (write-sample!)
      (seed-addlang/seedgen-addlang 'sample.add-test {:write true} lookup project)
      [(seed-infile/seedgen-list 'sample.add-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.B)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:lua :python]
      #{:js :lua :python}
      #{:js}
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-addlang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n"
                      "  :setup [(!.js (+ 1 2 3))\n"
                      "          (!.python (+ 1 2 3))]}\n"
                      "(fact \"runtime specific branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python (+ 1 2 3))\n"
                      "  => 6)\n"))
      (seed-addlang/seedgen-addlang 'sample.add-test {:lang :lua :write true} lookup project)
      [(seed-infile/seedgen-list 'sample.add-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:lua :python]
      #{:js :lua :python}
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-addlang" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.add-test path}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "(l/script- :js {:runtime :basic})\n"))
      (seed-addlang/seedgen-addlang 'sample.add-test {:write true} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :no-seedgen-root}))
