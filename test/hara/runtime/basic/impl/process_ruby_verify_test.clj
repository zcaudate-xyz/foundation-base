(ns hara.runtime.basic.impl.process-ruby-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :ruby
  hara.runtime.basic.impl.process-ruby-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl.process-ruby/CANARY :added "4.0"}
(fact "starts the ruby verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/ruby]))

(fact:global
 {:skip (not (env/program-exists? "ruby"))})

^{:refer hara.runtime.basic.impl.process-ruby/!.rb :added "4.0"}
(fact "validates a simple ruby expression through the runtime"
  (do (defrun.rb test-expr []
        (+ 1 2 3))
      (string? (!.rb test-expr)))
  => true)
