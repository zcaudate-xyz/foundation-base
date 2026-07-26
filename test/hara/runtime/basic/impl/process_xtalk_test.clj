tahto/runtime/basic/impl/process_xtalk_test.clj:1:(ns tahto.runtime.basic.impl.process-xtalk-test
tahto/runtime/basic/impl/process_xtalk_test.clj:2:  (:require [tahto.runtime.basic.impl.process-xtalk :refer :all])
  (:use code.test))

tahto/runtime/basic/impl/process_xtalk_test.clj:5:^{:refer tahto.runtime.basic.impl.process-xtalk/read-output :added "4.0"}
(fact "read output for scheme"

  (read-output {:out "#t"})
  => true

  (read-output {:out "#f"})
  => false

  (read-output {:err "error"})
  => (throws)

  (read-output {:out "invalid"})
  => 'invalid)

tahto/runtime/basic/impl/process_xtalk_test.clj:20:^{:refer tahto.runtime.basic.impl.process-xtalk/transform-form :added "4.0"}
(fact "transforms output from shell"

  (transform-form '((+ 1 2)) {:bulk true})
  => '((lambda [] (+ 1 2)))

  (transform-form '((+ 1 2)) {})
  => '((+ 1 2)))
