(ns std.lang.seedgen.common-infile-test
  (:use code.test)
  (:require [code.project :as project]
            [std.lang.seedgen.common-infile :as seed-infile]))

^{:refer std.lang.seedgen.common-infile/seedgen-root :added "4.1"}
(fact "returns an explicit error result when the test file is missing"
  (project/in-context
   (seed-infile/seedgen-root 'xt.sample.train-001-test {}))
  => :js

  (project/in-context
   (seed-infile/seedgen-root 'xt.sample.missing-test {}))
  => (contains {:status :error
                :data :no-test-file}))

^{:refer std.lang.seedgen.common-infile/seedgen-list :added "4.1"}
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

^{:refer std.lang.seedgen.common-infile/seedgen-incomplete :added "4.1"}
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
                      "^{:refer xt.lang.spec-base/example.A :added \"4.1\"\n"
                      "  :setup [(!.js (+ 1 2 3))]}\n"
                      "(fact \"covered from setup\"\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.spec-base/example.B :added \"4.1\"}\n"
                      "(fact \"TODO\")\n\n"
                      "^{:refer xt.lang.spec-base/example.C :added \"4.1\"\n"
                      "  :teardown [(!.lua (+ 1 2 3))]}\n"
                      "(fact \"covered from teardown\"\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.spec-base/example.D :added \"4.1\"}\n"
                      "(fact \"covered from notify wait\"\n"
                      "  (notify/wait-on :dt\n"
                      "    (repl/notify 1))\n"
                      "  \"TODO\")\n\n"
                      "^{:refer xt.lang.spec-base/example.E :added \"4.1\"}\n"
                      "(fact \"covered from matching notify wait\"\n"
                      "  (notify/wait-on :js\n"
                      "    (repl/notify 1))\n"
                      "  \"TODO\")\n"))
      (->> (seed-infile/seedgen-incomplete 'sample.incomplete-test {} lookup nil)
           keys
           set)
      (finally
        (.delete tmp))))
  => #{'xt.lang.spec-base/example.B
       'xt.lang.spec-base/example.C
       'xt.lang.spec-base/example.D}

  (-> (project/in-context
       (seed-infile/seedgen-incomplete 'xt.sample.train-004-test {}))
      keys
      set)
  => #{})
