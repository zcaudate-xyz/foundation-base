(ns hara.runtime.jep.bootstrap-test
  (:require [hara.runtime.jep.bootstrap :refer :all]
            [std.fs :as fs]
            [std.lib.os :as os])
  (:use code.test))

^{:refer hara.runtime.jep.bootstrap/bootstrap-code :added "3.0"}
(fact "creates the bootstrap code"
  (bootstrap-code)
  => vector?)

^{:refer hara.runtime.jep.bootstrap/jep-bootstrap :added "3.0"}
(fact "returns the jep runtime"
  (with-redefs [fs/create-tmpfile (fn [_] "file")
                os/sh (fn [& _] {:exit 0})
                os/sh-output (fn [_] {:exit 0 :out "path/to/jep"})]
    (jep-bootstrap))
  => "path/to/jep")

^{:refer hara.runtime.jep.bootstrap/jep-available? :added "4.1"}
(fact "checks if the jep python runtime is already available"
  (with-redefs [jep-bootstrap (fn [_] "path/to/jep")]
    (jep-available?))
  => true

  (with-redefs [jep-bootstrap (fn [_]
                                (throw (ex-info "missing" {})))]
    (jep-available?))
  => false)

^{:refer hara.runtime.jep.bootstrap/init-paths :added "3.0"}
(fact "sets the path of the jep interpreter"
  ;; Cannot mock static methods directly
  )
