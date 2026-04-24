(ns std.lang.seedgen.form-infile-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.lang.seedgen.common-util :as common]
            [std.lang.seedgen.common-infile :as common-infile]
            [std.lang.seedgen.form-infile :as form-infile]))

^{:refer std.lang.seedgen.form-infile/seedgen-langadd :added "4.1"}
(fact "adds seedgen runtimes back from the seedgen root form"
  (let [tmp (java.io.File/createTempFile "seedgen-langadd" ".clj")
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
                                        "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"runtime specific branches\"\n"
                                        "  (!.js (+ 1 2 3))\n"
                                        "  => 6)\n\n"
                                        "^{:refer xt.lang.spec-base/example.B :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"TODO\")\n")))]
    (try
      (write-sample!)
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      [(common-infile/seedgen-list 'sample.add-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.spec-base/example.A)
           common/seedgen-coverage-langs
           set)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.spec-base/example.B)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:lua :python]
      #{:js :lua :python}
      #{:js}
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.py (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.py (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.spec-base/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-langadd" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
                      "  :setup [(!.js (+ 1 2 3))\n"
                      "          (!.python (+ 1 2 3))]}\n"
                      "(fact \"runtime specific branches\"\n"
                      "  (!.js (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:lang :lua :write true} lookup project)
      [(common-infile/seedgen-list 'sample.add-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.spec-base/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:lua :python]
      #{:js :lua :python}
      "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.lua (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.lua (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-langadd" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  (!.py\n    (+ 1 2 3))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-notify" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.base-notify :as notify]\n"
                      "            [xt.lang.base-repl :as repl]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (notify/wait-on :js\n"
                      "    (repl/notify 1)))\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.base-notify :as notify]\n            [xt.lang.base-repl :as repl]))\n\n^{:seedgen/root {:all true, :langs [:python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (notify/wait-on :js\n    (repl/notify 1))\n\n  (notify/wait-on :python\n    (repl/notify 1)))\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-no-assert" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (!.js\n"
                      "    (+ 1 2 3)))\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (!.js\n    (+ 1 2 3))\n\n  (!.lua\n    (+ 1 2 3)))\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.add-test path}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "(l/script- :js {:runtime :basic})\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :no-seedgen-root})

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-a" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"multiple checks are also allowed\"\n\n"
                      "  (!.js\n"
                      "    (xt/x:apply (fn [a b c]\n"
                      "                  (return (+ a b c)))\n"
                      "                [1 2 3]))\n"
                      "  => 6\n\n"
                      "  ^{:seedgen/base {:python {:suppress true}}}\n"
                      "  (!.js\n"
                      "    (xt/x:apply (fn [a b c d]\n"
                      "                  (return (+ a b c d)))\n"
                      "                [1 2 3 4]))\n"
                      "  => 10)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"multiple checks are also allowed\"\n\n  (!.js\n    (xt/x:apply (fn [a b c]\n                  (return (+ a b c)))\n                [1 2 3]))\n  => 6\n\n  ^{:seedgen/base {:python {:suppress true}}}\n  (!.js\n    (xt/x:apply (fn [a b c d]\n                  (return (+ a b c d)))\n                [1 2 3 4]))\n  => 10\n\n  (!.py\n    (xt/x:apply (fn [a b c]\n                  (return (+ a b c)))\n                [1 2 3]))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-b" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.B :added \"4.1\"}\n"
                      "(fact \"forms can be suppressed\"\n\n"
                      "  (!.js\n"
                      "    (xt/x:apply (fn [a b c]\n"
                      "                  (return (+ a b c)))\n"
                      "                [1 2 3]))\n"
                      "  => 6\n\n"
                      "  ^{:seedgen/base {:python {:suppress true}}}\n"
                      "  (!.js\n"
                      "    (xt/x:apply (fn [a b c d]\n"
                      "                  (return (+ a b c d)))\n"
                      "                [1 2 3 4]))\n"
                      "  => 10)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.B :added \"4.1\"}\n(fact \"forms can be suppressed\"\n\n  (!.js\n    (xt/x:apply (fn [a b c]\n                  (return (+ a b c)))\n                [1 2 3]))\n  => 6\n\n  ^{:seedgen/base {:python {:suppress true}}}\n  (!.js\n    (xt/x:apply (fn [a b c d]\n                  (return (+ a b c d)))\n                [1 2 3 4]))\n  => 10\n\n  (!.lua\n    (xt/x:apply (fn [a b c]\n                  (return (+ a b c)))\n                [1 2 3]))\n  => 6\n\n  (!.lua\n    (xt/x:apply (fn [a b c d]\n                  (return (+ a b c d)))\n                [1 2 3 4]))\n  => 10\n\n  (!.py\n    (xt/x:apply (fn [a b c]\n                  (return (+ a b c)))\n                [1 2 3]))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-c" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.base-notify :as notify]\n"
                      "            [xt.lang.base-repl :as repl]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:python :lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.C :added \"4.1\"}\n"
                      "(fact \"order is important\"\n\n"
                      "  (notify/wait-on :js\n"
                      "    (repl/notify 1))\n"
                      "  => 1)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.base-notify :as notify]\n            [xt.lang.base-repl :as repl]))\n\n^{:seedgen/root {:all true, :langs [:python :lua]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.C :added \"4.1\"}\n(fact \"order is important\"\n\n  (notify/wait-on :js\n    (repl/notify 1))\n  => 1\n\n  (notify/wait-on :python\n    (repl/notify 1))\n  => 1\n\n  (notify/wait-on :lua\n    (repl/notify 1))\n  => 1)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-d" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.base-notify :as notify]\n"
                      "            [xt.lang.base-repl :as repl]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example.D :added \"4.1\"}\n"
                      "(fact \"any form is allowed with :seedgen/base meta\"\n\n"
                      "  ^{:seedgen/base true}\n"
                      "  [(!.js 1)\n"
                      "   (inc 0)\n"
                      "   (notify/wait-on :js\n"
                      "     (repl/notify 1))]\n"
                      "  => [1 1])\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.base-notify :as notify]\n            [xt.lang.base-repl :as repl]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.D :added \"4.1\"}\n(fact \"any form is allowed with :seedgen/base meta\"\n\n  ^{:seedgen/base true}\n  [(!.js 1)\n   (inc 0)\n   (notify/wait-on :js\n     (repl/notify 1))]\n  => [1 1]\n\n  [(!.lua 1)\n   (inc 0)\n   (notify/wait-on :lua\n     (repl/notify 1))]\n  => [1 1]\n\n  [(!.py 1)\n   (inc 0)\n   (notify/wait-on :python\n     (repl/notify 1))]\n  => [1 1])\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-e" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.E :added \"4.1\"}\n"
                      "(fact \"seed meta can be mixed and matched\"\n\n"
                      "  ^{:seedgen/base true}\n"
                      "  (identity (!.js 1))\n"
                      "  => 1\n\n"
                      "  ^{:seedgen/base {:all {}\n"
                      "                   :python {:suppress true}}}\n"
                      "  (identity (!.js 2))\n"
                      "  => 2)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.E :added \"4.1\"}\n(fact \"seed meta can be mixed and matched\"\n\n  ^{:seedgen/base true}\n  (identity (!.js 1))\n  => 1\n\n  ^{:seedgen/base {:all {}\n                   :python {:suppress true}}}\n  (identity (!.js 2))\n  => 2\n\n  (identity (!.lua 1))\n  => 1\n\n  (identity (!.lua 2))\n  => 2\n\n  (identity (!.py 1))\n  => 1)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-meta" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"metadata branches\"\n\n"
                      "  ^{:seedgen/base {:lua {:expect 6}}}\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"metadata branches\"\n\n  ^{:seedgen/base {:lua {:expect 6}}}\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  (!.lua\n    (+ 1 2 3))\n  => 6\n\n  (!.py\n    (+ 1 2 3))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-f-expect" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example-f :added \"4.1\"}\n"
                      "(fact \"expect can be customised\"\n\n"
                      "  ^{:seedgen/base {:lua {:expect 11}}}\n"
                      "  (!.js\n"
                      "    (xt/x:offset 10))\n"
                      "  => 10)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-f :added \"4.1\"}\n(fact \"expect can be customised\"\n\n  ^{:seedgen/base {:lua {:expect 11}}}\n  (!.js\n    (xt/x:offset 10))\n  => 10\n\n  (!.lua\n    (xt/x:offset 10))\n  => 11\n\n  (!.py\n    (xt/x:offset 10))\n  => 10)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-f-all-expect" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example-fb :added \"4.1\"}\n"
                      "(fact \"expect outcomes can default and override\"\n\n"
                      "  ^{:seedgen/base {:all {:expect 12}\n"
                      "                   :lua {:expect 11}}}\n"
                      "  (!.js\n"
                      "    (xt/x:offset 10))\n"
                      "  => 10)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-fb :added \"4.1\"}\n(fact \"expect outcomes can default and override\"\n\n  ^{:seedgen/base {:all {:expect 12}\n                   :lua {:expect 11}}}\n  (!.js\n    (xt/x:offset 10))\n  => 10\n\n  (!.lua\n    (xt/x:offset 10))\n  => 11\n\n  (!.py\n    (xt/x:offset 10))\n  => 12)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-f-input" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua :python]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/example-fa :added \"4.1\"}\n"
                      "(fact \"input can be customised\"\n\n"
                      "  ^{:seedgen/base {:lua {:input (xt/x:offset 9)}}}\n"
                      "  (!.js\n"
                      "    (xt/x:offset 10))\n"
                      "  => 10)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-fa :added \"4.1\"}\n(fact \"input can be customised\"\n\n  ^{:seedgen/base {:lua {:input (xt/x:offset 9)}}}\n  (!.js\n    (xt/x:offset 10))\n  => 10\n\n  (!.lua\n    (xt/x:offset 9))\n  => 10\n\n  (!.py\n    (xt/x:offset 10))\n  => 10)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-transform" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/x:return-eval :added \"4.1\"}\n"
                      "(fact \"transform can be customised\"\n\n"
                      "  ^{:seedgen/base {:lua {:transform {\"1 + 1\" \"return 1 + 1\"}}}}\n"
                      "  (!.js\n"
                      "    (var eval-fn\n"
                      "         (fn [s re-wrap-fn]\n"
                      "           (xt/x:return-eval s re-wrap-fn)))\n"
                      "    (eval-fn \"1 + 1\"\n"
                      "             (fn [f]\n"
                      "               (return f))))\n"
                      "  => 2)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]\n            [xt.lang.spec-base :as xt]))\n\n^{:seedgen/root {:all true, :langs [:lua]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n^{:refer xt.lang.spec-base/x:return-eval :added \"4.1\"}\n(fact \"transform can be customised\"\n\n  ^{:seedgen/base {:lua {:transform {\"1 + 1\" \"return 1 + 1\"}}}}\n  (!.js\n    (var eval-fn\n         (fn [s re-wrap-fn]\n           (xt/x:return-eval s re-wrap-fn)))\n    (eval-fn \"1 + 1\"\n             (fn [f]\n               (return f))))\n  => 2\n\n  (!.lua\n    (var eval-fn\n         (fn [s re-wrap-fn]\n           (xt/x:return-eval s re-wrap-fn)))\n    (eval-fn \"return 1 + 1\"\n             (fn [f]\n               (return f))))\n  => 2)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-transform-symbol" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/x:iter-from :added \"4.1\"}\n"
                      "(fact \"symbol transforms use block-aware replacement\"\n\n"
                      "  ^{:seedgen/base {:lua {:transform {xt/x:iter-from xt/x:iter-from-arr}}}}\n"
                      "  (!.js\n"
                      "    (var out [])\n"
                      "    (xt/for:iter [e (xt/x:iter-from [2 4 6])]\n"
                      "      (xt/x:arr-push out e))\n"
                      "    out)\n"
                      "  => [2 4 6])\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (let [output (slurp path)]
        [(str/includes? output "(xt/for:iter [e (xt/x:iter-from-arr [2 4 6])]")
         (str/includes? output "  (!.lua\n    (var out [])\n    (xt/for:iter [e (xt/x:iter-from-arr [2 4 6])]")
         (not (str/includes? output "  (!.lua (var out []) (xt/for:iter [e (xt/x:iter-from-arr [2 4 6])]"))])
      (finally
        (.delete tmp))))
  => [true true true]

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-transform-form" ".clj")
        path (.getAbsolutePath tmp)
        root (.getParent tmp)
        lookup {'sample.add-test path}
        project {:root root}]
    (try
      (spit path (str "(ns sample.add-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]\n"
                      "            [xt.lang.spec-base :as xt]))\n\n"
                      "^{:seedgen/root {:all true, :langs [:lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "^{:refer xt.lang.spec-base/x:callback :added \"4.1\"}\n"
                      "(fact \"quoted form transforms use block-aware replacement\"\n\n"
                      "  ^{:seedgen/base {:lua {:transform {'(fn [cb]\n"
                      "                                         (cb nil \"OK\"))\n"
                      "                                      '(fn [cb]\n"
                      "                                         (return nil \"OK\"))}}}}\n"
                      "  (!.js\n"
                      "     (var out nil)\n"
                      "     (var success-fn (fn [cb]\n"
                      "                       (cb nil \"OK\")))\n"
                      "     (xt/for:return [[ret err] (success-fn (xt/x:callback))]\n"
                      "       {:success (:= out ret)\n"
                      "        :error   (:= out err)})\n"
                      "     out)\n"
                      "  => \"OK\"\n\n"
                      "  ^{:seedgen/base {:lua {:transform {'(fn [cb]\n"
                      "                                         (cb \"ERR\" nil))\n"
                      "                                      '(fn [cb]\n"
                      "                                         (return \"ERR\" nil))}}}}\n"
                      "  (!.js\n"
                      "     (var out nil)\n"
                      "     (var failure-fn (fn [cb]\n"
                      "                       (cb \"ERR\" nil)))\n"
                      "     (xt/for:return [[ret err] (failure-fn (xt/x:callback))]\n"
                      "       {:success (:= out ret)\n"
                      "        :error   (:= out err)})\n"
                      "     out)\n"
                      "  => \"ERR\")\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (let [output (slurp path)]
        [(str/includes? output "(var success-fn (fn [cb] (return nil \"OK\")))")
         (str/includes? output "(var failure-fn (fn [cb] (return \"ERR\" nil)))")
         (str/includes? output "  (!.lua\n     (var out nil)\n     (var success-fn (fn [cb] (return nil \"OK\")))")
         (str/includes? output "  (!.lua\n     (var out nil)\n     (var failure-fn (fn [cb] (return \"ERR\" nil)))")
         (not (str/includes? output "  (!.lua (var out nil) (var success-fn (fn [cb] (return nil \"OK\")))"))
         (not (str/includes? output "  (!.lua (var out nil) (var failure-fn (fn [cb] (return \"ERR\" nil)))"))])
      (finally
        (.delete tmp))))
  => [true true true true true true]

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-train004-g-setup" ".clj")
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
                      "^{:refer xt.lang.spec-base/example-g :added \"4.1\"\n"
                      "  :setup [^{:seedgen/base {:lua {:input (!.lua (setup-lua))}}}\n"
                      "          (!.js (setup-js))]}\n"
                      "(fact \"setup can be customised\"\n\n"
                      "  (!.js 1)\n"
                      "  => 1)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.add-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example-g :added \"4.1\"\n  :setup [^{:seedgen/base {:lua {:input (!.lua (setup-lua))}}}\n(!.js (setup-js))\n          (!.lua (setup-lua))\n          (!.py (setup-js))]}\n(fact \"setup can be customised\"\n\n  (!.js 1)\n  => 1\n\n  (!.lua 1)\n  => 1\n\n  (!.py 1)\n  => 1)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langadd-scaffold-meta" ".clj")
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
                      "^{:refer xt.lang.spec-base/example-h :added \"4.1\"\n"
                      "  :setup [(def +s+ \"seed\")\n"
                      "          (def +out+ [\"seed\"])]\n"
                      "  :teardown [(def +done+ true)]}\n"
                      "(fact \"scaffold setup is preserved\"\n\n"
                      "  (!.js 1)\n"
                      "  => 1)\n"))
      (form-infile/seedgen-langadd 'sample.add-test {:write true} lookup project)
      (let [output (slurp path)]
        [(str/includes? output ":setup [(def +s+ \"seed\")")
         (str/includes? output "(def +out+ [\"seed\"])]")
         (str/includes? output ":teardown [(def +done+ true)]")
         (str/includes? output "(!.lua 1)\n  => 1")
         (str/includes? output "(!.py 1)\n  => 1")])
      (finally
        (.delete tmp))))
  => [true true true true true]

^{:refer std.lang.seedgen.form-infile/seedgen-langremove :added "4.1"}
(fact "purges targeted seedgen runtimes while preserving the seedgen root"
  (let [tmp (java.io.File/createTempFile "seedgen-langremove" ".clj")
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
                                        "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
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
                                        "^{:refer xt.lang.spec-base/example.B :added \"4.1\"\n"
                                        "  :setup [(!.js (+ 1 2 3))]}\n"
                                        "(fact \"TODO\")\n")))]
    (try
      (write-sample!)
      (form-infile/seedgen-langremove 'sample.purge-test {:lang :all :write true} lookup project)
      [(common-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.spec-base/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[] #{:js}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6)\n\n^{:refer xt.lang.spec-base/example.B :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))]}\n(fact \"TODO\")\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-langremove" ".clj")
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
                                        "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
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
      (form-infile/seedgen-langremove 'sample.purge-test {:lang :lua :write true} lookup project)
      [(common-infile/seedgen-list 'sample.purge-test {} lookup nil)
       (-> (common/seedgen-fact-forms path)
           (get 'xt.lang.spec-base/example.A)
           common/seedgen-coverage-langs
           set)
       (slurp path)]
      (finally
        (.delete tmp))))
  => [[:python] #{:js :python}
      "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n  :setup [(!.js (+ 1 2 3))\n          (!.python (+ 1 2 3))]}\n(fact \"runtime specific branches\"\n\n  (!.js (+ 1 2 3))\n  => 6\n\n  (!.python (+ 1 2 3))\n  => 6)\n"]

  (let [tmp (java.io.File/createTempFile "seedgen-langremove" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"runtime specific branches\"\n\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.lua (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  (!.python\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-langremove 'sample.purge-test {:lang :lua :write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"runtime specific branches\"\n\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  (!.python\n    (+ 1 2 3))\n  => 6)\n"

  (let [tmp (java.io.File/createTempFile "seedgen-langremove" ".clj")
        path (.getAbsolutePath tmp)
        lookup {'sample.purge-test path}]
    (try
      (spit path (str "(ns sample.purge-test\n"
                      "  (:use code.test)\n"
                      "  (:require [std.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n"))
      (form-infile/seedgen-langremove 'sample.purge-test {:lang [:js]} lookup nil)
      (finally
        (.delete tmp))))
  => (contains {:status :error
                :data :cannot-purge-root
                :lang :js})

  (let [tmp (java.io.File/createTempFile "seedgen-langremove-meta" ".clj")
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n"
                      "(fact \"metadata branches\"\n\n"
                      "  ^{:seedgen/base {:lua {:expect 6}}}\n"
                      "  (!.js\n"
                      "    (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  ^{:seedgen/base {:lua {:expect 6}}}\n"
                      "  (!.lua\n"
                      "    (+ 1 2 3))\n"
                      "  => 6\n\n"
                      "  ^{:seedgen/base {:lua {:expect 6}}}\n"
                      "  (!.python\n"
                      "    (+ 1 2 3))\n"
                      "  => 6)\n"))
      (form-infile/seedgen-langremove 'sample.purge-test {:lang :lua :write true} lookup project)
      (slurp path)
      (finally
        (.delete tmp))))
  => "(ns sample.purge-test\n  (:use code.test)\n  (:require [std.lang :as l]))\n\n^{:seedgen/root {:all true, :langs [:lua :python]}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :python {:runtime :basic})\n\n^{:refer xt.lang.spec-base/example.A :added \"4.1\"}\n(fact \"metadata branches\"\n\n  ^{:seedgen/base {:lua {:expect 6}}}\n  (!.js\n    (+ 1 2 3))\n  => 6\n\n  ^{:seedgen/base {:lua {:expect 6}}}\n  (!.python\n    (+ 1 2 3))\n  => 6)\n")
  )
