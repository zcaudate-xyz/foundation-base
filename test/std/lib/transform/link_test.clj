(ns std.lib.transform.link-test
  (:use code.test)
  (:require [std.lib.transform.link :refer :all]
            [std.lib.transform :as graph]))

^{:refer std.lib.transform.link/wrap-link-current :added "3.0"}
(fact "adds the current ref to `:link :current`"
  (resolve 'wrap-link-current) => var?)

^{:refer std.lib.transform.link/wrap-link-attr :added "3.0"}
(fact "adds the parent link `:id` of the ref"
  (resolve 'wrap-link-attr) => var?)

^{:refer std.lib.transform.link/wrap-link-parent :added "0.1"}
(fact "adding parent to current data"
  (resolve 'wrap-link-parent) => var?)
