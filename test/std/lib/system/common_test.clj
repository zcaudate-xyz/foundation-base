(ns std.lib.system.common-test
  (:require [std.lib.system.common :refer :all]
            [std.protocol.component :as protocol.component]
            [std.protocol.track :as protocol.track])
  (:use code.test))

^{:refer std.lib.system.common/system? :added "3.0"}
(fact "checks if a component extends ISystem" ^:hidden

  (system? 1)
  => false)

^{:refer std.lib.system.common/primitive? :added "3.0"}
(fact "checks if a component is a primitive type" ^:hidden

  (primitive? 1) => true

  (primitive? {}) => false)
