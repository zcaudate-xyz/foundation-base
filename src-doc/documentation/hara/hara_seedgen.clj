(ns documentation.hara-seedgen
  (:require [hara.seedgen.common-util :as seedgen])
  (:use code.test))

[[:hero {:title "hara.seedgen"
         :subtitle "Seed generation and xtalk test scaffolding."
         :lead "`hara.seedgen` supports generated seed files and xtalk-oriented test scaffolding for language/runtime coverage."}]]

[[:chapter {:title "Motivation"}]]
"When many target languages need similar coverage, seed generation keeps test shape consistent while letting each language specialize emission and runtime details."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Runtime language dispatch"}]]

"Seed generation starts by normalising language identifiers. `seedgen-normalize-runtime-lang` turns keywords, symbols, and strings into the canonical runtime lang, while `seedgen-dispatch-tag` and `seedgen-display-lang` produce the surface syntax tag used in generated code."

(fact "normalise runtime language identifiers"
  (seedgen/seedgen-normalize-runtime-lang "js")
  => :js

  (seedgen/seedgen-normalize-runtime-lang 'python)
  => :python

  (seedgen/seedgen-dispatch-tag :js)
  => :js

  (seedgen/seedgen-display-lang :python)
  => :python)

(fact "look up the default runtime for a language"
  (seedgen/seedgen-default-runtime :js)
  => :basic)

(fact "inspect the active dispatch map"
  (contains? (set (keys (seedgen/seedgen-dispatch-map))) "js")
  => true)

[[:section {:title "Detecting runtime usage in forms"}]]

"`hara.seedgen` inspects test forms to discover which runtimes a fact exercises. `seedgen-dispatch-lang` recognises `!.lang` calls, `seedgen-runtime-reference-lang` recognises runtime reference forms such as `notify/wait-on`, and `seedgen-runtime-dispatch-langs` collects every language found in an expression."

(fact "detect the runtime target of a form"
  (seedgen/seedgen-dispatch-lang '(!.js 1))
  => :js

  (seedgen/seedgen-runtime-reference-lang '(notify/wait-on :lua 42))
  => :lua

  (seedgen/seedgen-runtime-dispatch-langs '(+ (!.js 1) (!.lua 2)))
  => [:js :lua]

  (seedgen/seedgen-form-lang '(!.js (+ 1 2)))
  => :js)

(fact "collect languages across fact metadata"
  (seedgen/seedgen-coverage-langs
   '^{:refer sample/foo :setup [(notify/wait-on :js 42)]}
   (fact "x" (!.js 1) => 1))
  => [:js])

[[:section {:title "Seedgen configuration"}]]

"Facts and scripts can carry `:seedgen/base`, `:seedgen/lang`, `:seedgen/check`, and `:seedgen/root` metadata. `seedgen-base-config` merges this configuration, `seedgen-lang-entry` selects the entry for a specific language, and `seedgen-root-entry` reads language-specific options from the root script metadata."

(fact "merge per-language configuration from metadata"
  (seedgen/seedgen-base-config
   '^{:seedgen/base {:js {:suppress true} :all {:wrap true}}}
   (!.js 1))
  => {:js {:suppress true} :all {:wrap true}})

(fact "select a language entry from merged config"
  (seedgen/seedgen-lang-entry
   '^{:seedgen/base {:js {:suppress true} :all {:wrap true}}}
   (!.js 1)
   :js)
  => {:wrap true :suppress true})

(fact "read root script options for a language"
  (seedgen/seedgen-root-entry
   '^{:seedgen/root {:all true :python {:runtime :basic}}}
   (l/script- :js {:runtime :basic})
   :python)
  => {:runtime :basic})

(fact "find suppressed languages"
  (seedgen/seedgen-suppressed-langs
   '^{:seedgen/base {:js {:suppress true}}}
   (!.js 1))
  => #{:js})

[[:section {:title "File-level helpers"}]]

"At file scope, `seedgen-skip?` checks whether a namespace is opted out, `seedgen-root-langs` returns the root or derived runtime declarations, and `seedgen-fact-forms` collects facts keyed by their `:refer` symbol."

(fact "check whether a test file is skipped"
  (let [tmp (java.io.File/createTempFile "seedgen" ".clj")
        path (.getAbsolutePath tmp)]
    (try
      (spit tmp "^{:seedgen/skip true}\n(ns sample.test\n  (:use code.test)\n  (:require [hara.lang :as l]))\n\n(l/script- :js {:runtime :basic})\n")
      (seedgen/seedgen-skip? path)
      (finally
        (.delete tmp))))
  => true)

(fact "read root and derived runtime declarations"
  (let [tmp (java.io.File/createTempFile "seedgen" ".clj")
        path (.getAbsolutePath tmp)]
    (try
      (spit tmp "(ns sample.test\n  (:use code.test)\n  (:require [hara.lang :as l]))\n\n^{:seedgen/root {:all true}}\n(l/script- :js {:runtime :basic})\n\n(l/script- :lua {:runtime :basic})\n")
      [(seedgen/seedgen-root-langs path true)
       (seedgen/seedgen-root-langs path false)]
      (finally
        (.delete tmp))))
  => [[:js] [:lua]])

(fact "collect fact forms by their refer symbol"
  (let [tmp (java.io.File/createTempFile "seedgen" ".clj")
        path (.getAbsolutePath tmp)]
    (try
      (spit tmp "(ns sample.test\n  (:use code.test)\n  (:require [hara.lang :as l]))\n\n^{:seedgen/root {:all true}}\n(l/script- :js {:runtime :basic})\n\n^{:refer sample/foo}\n(fact \"x\" (!.js 1) => 1)\n")
      (keys (seedgen/seedgen-fact-forms path))
      (finally
        (.delete tmp))))
  => '[sample/foo])

[[:chapter {:title "API"}]]
