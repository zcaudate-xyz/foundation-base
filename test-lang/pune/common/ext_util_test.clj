(ns pune.common.ext-util-test
  (:use code.test)
  (:require [pune.common.ext-util :refer :all]))

^{:refer pune.common.ext-util/useResendDelay :added "0.1"}
(fact "creates a resend delay")

^{:refer pune.common.ext-util/useGroupKey :added "0.1"}
(fact "use group key, for auth routes")

