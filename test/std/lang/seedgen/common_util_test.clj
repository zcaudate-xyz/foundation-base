(ns std.lang.seedgen.common-util-test
  (:use code.test)
  (:require [std.lang.seedgen.common-util :refer :all]))

^{:refer std.lang.seedgen.common-util/require-alias :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-script-heads :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-root-langs :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-dispatch-map :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-normalize-runtime-lang :added "4.1"}
(fact "normalizes runtime tags to their installed language keys"
  (seedgen-normalize-runtime-lang :py)
  => :python

  (seedgen-normalize-runtime-lang :R)
  => :r

  (seedgen-normalize-runtime-lang :dt)
  => :dart)

^{:refer std.lang.seedgen.common-util/seedgen-dispatch-tag :added "4.1"}
(fact "returns the grammar dispatch tag for a runtime language"
  (seedgen-dispatch-tag :python)
  => :py

  (seedgen-dispatch-tag :r)
  => :R

  (seedgen-dispatch-tag :dart)
  => :dt)

^{:refer std.lang.seedgen.common-util/seedgen-dispatch-lang :added "4.1"}
(fact "parses dispatch symbols back to their canonical runtime language"
  (seedgen-dispatch-lang '(!.R (+ 1 2 3)))
  => :r

  (seedgen-dispatch-lang '(!.py (+ 1 2 3)))
  => :python

  (seedgen-dispatch-lang '(!.dt (+ 1 2 3)))
  => :dart)

^{:refer std.lang.seedgen.common-util/seedgen-default-runtime :added "4.1"}
(fact "returns the default runtime for generated script headers"
  (seedgen-default-runtime :dart)
  => :twostep

  (seedgen-default-runtime :r)
  => :basic)

^{:refer std.lang.seedgen.common-util/seedgen-runtime-reference-lang :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-runtime-dispatch-langs :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-fact-forms :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-lang-config :added "4.1"}
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

^{:refer std.lang.seedgen.common-util/seedgen-suppressed-langs :added "4.1"}
(fact "collects suppressed seedgen languages from metadata"
  (seedgen-suppressed-langs
   (with-meta
     '(fact "TODO")
     {:seedgen/base {:python {:suppress true}
                     :lua {:suppress false}
                     :js {:suppress true}}}))
  => #{:python :js})

^{:refer std.lang.seedgen.common-util/seedgen-coverage-langs :added "4.1"}
(fact "TODO")
