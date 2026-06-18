(ns hara.seedgen.common-util-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.seedgen.common-util :refer :all]
            [std.fs :as fs]))

^{:refer hara.seedgen.common-util/require-alias :added "4.1"}
(fact "extracts the :as alias for a require target"
  (require-alias '[hara.lang :as l] 'hara.lang) => 'l
  (require-alias '[hara.lang] 'hara.lang) => nil
  (require-alias '[clojure.string :as str] 'hara.lang) => nil
  (require-alias 'hara.lang 'hara.lang) => nil)

^{:refer hara.seedgen.common-util/seedgen-script-heads :added "4.1"}
(fact "discovers script- heads from a namespace form"
  (seedgen-script-heads '(ns sample
                           (:require [hara.lang :as l])))
  => #{'hara.lang/script- 'l/script-}

  (seedgen-script-heads '(ns sample)) => #{'hara.lang/script-})

^{:refer hara.seedgen.common-util/seedgen-root-langs :added "4.1"}
(fact "collects root or derived runtime languages from a test file"
  (let [tmp (java.io.File/createTempFile "seedgen-root-langs" ".clj")
        path (.getAbsolutePath tmp)]
    (try
      (spit path (str "(ns sample\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:seedgen/root {:all true :langs [:js :lua]}}\n"
                      "(l/script- :js {:runtime :basic})\n\n"
                      "(l/script- :lua {:runtime :basic})\n"))
      [(seedgen-root-langs path true)
       (seedgen-root-langs path false)]
      (finally (.delete tmp))))
  => [[:js] [:lua]])

^{:refer hara.seedgen.common-util/seedgen-dispatch-map :added "4.1"}
(fact "maps dispatch tags to canonical runtime languages"
  (seedgen-dispatch-map) => (fn [m]
                              (and (map? m)
                                   (= :js (get m "js"))
                                   (= :python (get m "py"))
                                   (= :dart (get m "dt")))))

^{:refer hara.seedgen.common-util/seedgen-dispatch-tag-map :added "4.1"}
(fact "maps canonical runtime languages back to dispatch tags"
  (seedgen-dispatch-tag-map) => (fn [m]
                                  (and (map? m)
                                       (= :js (get m :js))
                                       (= :py (get m :python))
                                       (= :dt (get m :dart)))))

^{:refer hara.seedgen.common-util/seedgen-normalize-runtime-lang :added "4.1"}
(fact "normalizes runtime tags to their installed language keys"
  (seedgen-normalize-runtime-lang :py) => :python
  (seedgen-normalize-runtime-lang :R) => :r
  (seedgen-normalize-runtime-lang :dt) => :dart)

^{:refer hara.seedgen.common-util/seedgen-dispatch-tag :added "4.1"}
(fact "returns the grammar dispatch tag for a runtime language"
  (seedgen-dispatch-tag :python) => :py
  (seedgen-dispatch-tag :r) => :R
  (seedgen-dispatch-tag :dart) => :dt)

^{:refer hara.seedgen.common-util/seedgen-default-runtime :added "4.1"}
(fact "returns the default runtime for generated script headers"
  (seedgen-default-runtime :dart) => :twostep
  (seedgen-default-runtime :r) => :basic)

^{:refer hara.seedgen.common-util/seedgen-dispatch-lang :added "4.1"}
(fact "parses dispatch symbols back to their canonical runtime language"
  (seedgen-dispatch-lang '(!.R (+ 1 2 3))) => :r
  (seedgen-dispatch-lang '(!.py (+ 1 2 3))) => :python
  (seedgen-dispatch-lang '(!.dt (+ 1 2 3))) => :dart)

^{:refer hara.seedgen.common-util/seedgen-runtime-reference-lang :added "4.1"}
(fact "extracts the language from runtime reference forms"
  (seedgen-runtime-reference-lang '(l/rt :js)) => :js
  (seedgen-runtime-reference-lang '(notify/wait-on :python)) => :python
  (seedgen-runtime-reference-lang '(notify/wait-on [:lua 100])) => :lua
  (seedgen-runtime-reference-lang '(+ 1 2 3)) => nil)

^{:refer hara.seedgen.common-util/seedgen-runtime-dispatch-langs :added "4.1"}
(fact "collects all runtime languages used in a form"
  (seedgen-runtime-dispatch-langs '(!.js 1)) => [:js]
  (seedgen-runtime-dispatch-langs '[(!.js 1) (!.py 2)]) => [:js :python]
  (seedgen-runtime-dispatch-langs '[(l/rt :lua)]) => [:lua]
  (seedgen-runtime-dispatch-langs '(+ 1 2 3)) => [])

^{:refer hara.seedgen.common-util/seedgen-form-lang :added "4.1"}
(fact "returns the single language used by a form, if unambiguous"
  (seedgen-form-lang '(!.js 1)) => :js
  (seedgen-form-lang '[(!.js 1) (!.py 2)]) => nil
  (seedgen-form-lang '(+ 1 2 3)) => nil)

^{:refer hara.seedgen.common-util/seedgen-fact-forms :added "4.1"}
(fact "extracts fact forms indexed by their :refer symbol"
  (let [tmp (java.io.File/createTempFile "seedgen-fact-forms" ".clj")
        path (.getAbsolutePath tmp)]
    (try
      (spit path (str "(ns sample\n"
                      "  (:use code.test)\n"
                      "  (:require [hara.lang :as l]))\n\n"
                      "^{:refer sample/hello :added \"4.1\"}\n"
                      "(fact \"hello\"\n"
                      "  1 => 1)\n\n"
                      "^{:refer sample/world :added \"4.1\"}\n"
                      "(fact \"world\"\n"
                      "  2 => 2)\n"))
      (let [forms (seedgen-fact-forms path)]
        [(set (keys forms))
         (map? forms)
         (= 'fact (first (get forms 'sample/hello)))
         (= 'fact (first (get forms 'sample/world)))])
      (finally (.delete tmp))))
  => [#{ 'sample/hello 'sample/world} true true true])

^{:refer hara.seedgen.common-util/seedgen-base-config :added "4.1"}
(fact "normalizes seedgen base metadata into a per-language config map"
  (seedgen-base-config (with-meta
                         '(fact "TODO")
                         {:seedgen/base {:python {:suppress true}
                                         :lua {:expect 6}}}))
  => {:python {:suppress true}
      :lua {:expect 6}}

  (seedgen-base-config (with-meta
                         '(fact "TODO")
                         {:seedgen/base true}))
  => {:all {}}

  (seedgen-base-config '(fact "TODO")) => nil)

^{:refer hara.seedgen.common-util/seedgen-lang-config :added "4.1"}
(fact "normalizes unified seedgen base metadata and keeps legacy aliases compatible"
  (seedgen-lang-config
   (with-meta
     '(fact "TODO")
     {:seedgen/base {:python {:suppress true}
                     :lua {:expect 6}}}))
  => {:python {:suppress true}
      :lua {:expect 6}}

  (seedgen-lang-config
   (with-meta
     '(fact "TODO")
     {:seedgen/lang {:python {:suppress true}}
      :seedgen/check {:lua {:expect 6}}}))
  => {:python {:suppress true}
      :lua {:expect 6}})

^{:refer hara.seedgen.common-util/seedgen-lang-entry :added "4.1"}
(fact "returns the merged config entry for a specific language"
  (seedgen-lang-entry (with-meta
                        '(fact "TODO")
                        {:seedgen/base {:all {:timeout 1000}
                                        :python {:suppress true}
                                        :lua {:expect 6}}})
                      :python)
  => {:timeout 1000 :suppress true}

  (seedgen-lang-entry (with-meta
                        '(fact "TODO")
                        {:seedgen/base {:all {:timeout 1000}
                                        :python {:suppress true}}})
                      :lua)
  => {:timeout 1000})

^{:refer hara.seedgen.common-util/seedgen-root-entry :added "4.1"}
(fact "returns the merged config entry from the seedgen root form"
  (let [root-form (with-meta
                    '(l/script- :js {:runtime :basic})
                    {:seedgen/root {:all {:timeout 1000}
                                    :python {:extra [['foo :as 'f]]}}})]
    (seedgen-root-entry root-form :python)
    => {:timeout 1000 :extra [['foo :as 'f]]}

    (seedgen-root-entry root-form :js)
    => {:timeout 1000}))

^{:refer hara.seedgen.common-util/seedgen-suppressed-langs :added "4.1"}
(fact "collects suppressed seedgen languages from metadata"
  (seedgen-suppressed-langs
   (with-meta
     '(fact "TODO")
     {:seedgen/base {:python {:suppress true}
                     :lua {:suppress false}
                     :js {:suppress true}}}))
  => #{:python :js})

^{:refer hara.seedgen.common-util/seedgen-coverage-langs :added "4.1"}
(fact "collects runtime languages used in a fact and its setup/teardown"
  (seedgen-coverage-langs
   (with-meta
     '(fact "hello"
        (!.js 1) => 1)
     {:setup ['(!.lua (setup))]
      :teardown ['(!.py (teardown))]}))
  => [:js :lua :python])

^{:refer hara.seedgen.common-util/seedgen-display-lang :added "4.1"}
(fact "returns the display/script language for a canonical language"
  (seedgen-display-lang :python) => :python
  (seedgen-display-lang :r) => :r
  (seedgen-display-lang :dart) => :dart
  (seedgen-display-lang :ruby) => :ruby)

^{:refer hara.seedgen.common-util/seedgen-skip? :added "4.1"}
(fact "returns true when a test file is marked ^{:seedgen/skip true}"
  (let [skip-tmp (java.io.File/createTempFile "seedgen-skip" ".clj")
        keep-tmp (java.io.File/createTempFile "seedgen-keep" ".clj")]
    (try
      (spit (.getAbsolutePath skip-tmp)
            (str "^{:seedgen/skip true}\n"
                 "(ns sample.skip-test\n"
                 "  (:use code.test))\n"))
      (spit (.getAbsolutePath keep-tmp)
            (str "(ns sample.keep-test\n"
                 "  (:use code.test))\n"))
      [(seedgen-skip? (.getAbsolutePath skip-tmp))
       (seedgen-skip? (.getAbsolutePath keep-tmp))]
      (finally
        (.delete skip-tmp)
        (.delete keep-tmp))))
  => [true false])
