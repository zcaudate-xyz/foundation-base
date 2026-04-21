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
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-dispatch-lang :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-runtime-reference-lang :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-runtime-dispatch-langs :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-fact-forms :added "4.1"}
(fact "TODO")

^{:refer std.lang.seedgen.common-util/seedgen-lang-config :added "4.1"}
(fact "normalizes per-language seedgen configuration metadata"
  (seedgen-lang-config
   (with-meta
     '(fact "TODO")
     {:seedgen/lang {:python {:suppress true}
                     :lua {:suppress false}}}))
  => {:python {:suppress true}
      :lua {:suppress false}})

^{:refer std.lang.seedgen.common-util/seedgen-suppressed-langs :added "4.1"}
(fact "collects suppressed seedgen languages from metadata"
  (seedgen-suppressed-langs
   (with-meta
     '(fact "TODO")
     {:seedgen/lang {:python {:suppress true}
                     :lua {:suppress false}
                     :js {:suppress true}}}))
  => #{:python :js})

^{:refer std.lang.seedgen.common-util/seedgen-coverage-langs :added "4.1"}
(fact "TODO")
