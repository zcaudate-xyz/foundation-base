(ns std.lib.transform.base.type-check-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.base.type-check :refer :all])
  (:use code.test))

^{:refer std.lib.transform.base.type-check/wrap-single-type-check :added "3.0"}
(fact "wraps normalise to type check inputs as well as to coerce incorrect inputs"
  ^:hidden

  (comment
    (graph/normalise {:account {:age "10"}}
                     {:schema (schema/schema examples/account-name-age-sex)}
                     {:normalise-single [wrap-single-type-check]})
    => (throws-info {:type :long,
                     :data "10",
                     :id :wrong-type})

    (graph/normalise {:account {:age "10"}}
                     {:schema (schema/schema examples/account-name-age-sex)
                      :options {:use-coerce true}}
                     {:normalise-single [wrap-single-type-check]})
    => {:account {:age 10}}))
