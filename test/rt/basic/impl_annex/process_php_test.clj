(ns rt.basic.impl-annex.process-php-test
  (:require [rt.basic.impl-annex.process-php :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-php/default-oneshot-wrap :added "4.0"}
(fact "creates the oneshot bootstrap form"

  (default-oneshot-wrap 1)
  => string?)

^{:refer rt.basic.impl-annex.process-php/default-basic-client :added "4.0"}
(fact "creates the basic client bootstrap"

  (default-basic-client 19000)
  => string?)
