(ns rt.basic.impl.process-xtalk-test
  (:use code.test)
  (:require [rt.basic.impl.process-xtalk :refer :all]
            [std.lib :as h]))

^{:refer rt.basic.impl.process-xtalk/read-output :added "4.0"}
(fact "read output for scheme"

  (read-output {:out "#t"})
  => true

  (read-output {:out "#f"})
  => false

  (read-output {:err "error"})
  => (throws)

  (read-output {:out "invalid"})
  => 'invalid)

^{:refer rt.basic.impl.process-xtalk/transform-form :added "4.0"}
(fact "transforms output from shell"

  (transform-form '((+ 1 2)) {:bulk true})
  => '((lambda [] (+ 1 2)))

  (transform-form '((+ 1 2)) {})
  => '((+ 1 2)))
