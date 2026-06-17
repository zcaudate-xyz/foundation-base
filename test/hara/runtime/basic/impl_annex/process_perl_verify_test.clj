(ns hara.runtime.basic.impl_annex.process-perl-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :perl
  hara.runtime.basic.impl_annex.process-perl-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-perl/CANARY :added "4.0"}
(fact "starts the perl verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/perl]))

(fact:global
 {:skip (not (env/program-exists? "perl"))})

^{:refer hara.runtime.basic.impl_annex.process-perl/!.pl :added "4.0"}
(fact "validates a simple perl expression through the runtime"
  (do (defrun.pl test-expr [] (+ 1 2 3))
      (string? (!.pl test-expr)))
  => true)
