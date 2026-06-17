(ns hara.runtime.basic.impl_annex.process-php-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :php
  hara.runtime.basic.impl_annex.process-php-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-php/CANARY :added "4.0"}
(fact "starts the php verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/php]))

(fact:global
 {:skip (not (env/program-exists? "php"))})

^{:refer hara.runtime.basic.impl_annex.process-php/!.php :added "4.0"}
(fact "validates a simple php expression through the runtime"
  (do (defrun.php test-expr [] (+ 1 2 3))
      (string? (!.php test-expr)))
  => true)
