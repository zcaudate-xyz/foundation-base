(ns rt.jep.bootstrap-test
  (:use code.test)
  (:require [rt.jep.bootstrap :refer :all]
            [std.fs :as fs]
            [std.lib :as h]))

^{:refer rt.jep.bootstrap/bootstrap-code :added "3.0"}
(fact "creates the bootstrap code"
  (bootstrap-code)
  => vector?)

^{:refer rt.jep.bootstrap/jep-bootstrap :added "3.0"}
(fact "returns the jep runtime"
  (with-redefs [fs/create-tmpfile (fn [_] "file")
                h/sh (fn [& _] {:exit 0})
                h/sh-output (fn [_] {:exit 0 :out "path/to/jep"})]
    (jep-bootstrap))
  => "path/to/jep")

^{:refer rt.jep.bootstrap/init-paths :added "3.0"}
(fact "sets the path of the jep interpreter"
  ;; Cannot mock static methods directly
  )
