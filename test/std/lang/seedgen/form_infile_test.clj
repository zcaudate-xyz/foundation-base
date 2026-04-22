(ns std.lang.seedgen.form-infile-test
  (:use code.test)
  (:require [std.lang.seedgen.common-util :as common]
            [std.lang.seedgen.common-infile :as common-infile]
            [std.lang.seedgen.form-infile :as form-infile]))

^{:refer std.lang.seedgen.form-infile/seedgen-addlang :added "4.1"}
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
      (form-infile/seedgen-addlang 'sample.add-test {:write true} lookup project)
      [(common-infile/seedgen-list 'sample.add-test {} lookup nil)
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
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

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
      (form-infile/seedgen-addlang 'sample.add-test {:lang :lua :write true} lookup project)
      [(common-infile/seedgen-list 'sample.add-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:lua :python]
      #{:js :lua :python}
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-addlang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-addlang 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  (!.python\n    (+ 1 2 3))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-addlang" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.add-test path}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "(l/script- :js {:runtime :basic})\n"))
      (form-infile/seedgen-addlang 'sample.add-test {:write true} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :no-seedgen-root}))

^{:refer std.lang.seedgen.form-infile/seedgen-removelang :added "4.1"}
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
      (form-infile/seedgen-removelang 'sample.purge-test {:lang :all :write true} lookup project)
      [(common-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[] #{:js}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.common-spec/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

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
      (form-infile/seedgen-removelang 'sample.purge-test {:lang :lua :write true} lookup project)
      [(common-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.common-spec/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:python] #{:js :python}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-removelang" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.purge-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.purge-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n\n"
                      "(l/script- :python {:runtime :basic})\n\n"
                      "^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.lua (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-removelang 'sample.purge-test {:lang :lua :write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.common-spec/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  (!.python\n    (+ 1 2 3))\n  => 6)\n"

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
      (form-infile/seedgen-removelang 'sample.purge-test {:lang [:js]} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :cannot-purge-root
                :lang :js}))
