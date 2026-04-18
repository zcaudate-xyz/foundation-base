(ns
 xtbench.php.lang.common-notify-test
 (:require
  [net.http :as http]
  [std.json :as json]
  [std.lang :as l]
  [std.lang.interface.type-notify :as interface]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})
