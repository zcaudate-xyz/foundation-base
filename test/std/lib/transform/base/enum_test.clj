(ns std.lib.transform.base.enum-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.base.enum :refer :all])
  (:use code.test))

^{:refer std.lib.transform.base.enum/wrap-single-enum :added "3.0"}
(fact "wraps normalise with comprehension of the enum type"

  (graph/normalise {:account {:type :account.type/guest}}
                   {:schema (schema/schema {:account/type [{:type :enum
                                                            :enum {:ns :account.type
                                                                   :values #{:vip :guest}}}]})}
                   {:normalise-single [wrap-single-enum]})
  => {:account {:type :guest}}
  (graph/normalise {:account {:type :account.type/WRONG}}
                   {:schema (schema/schema {:account/type [{:type :enum
                                                            :enum {:ns :account.type
                                                                   :values #{:vip :guest}}}]})}
                   {:normalise-single [wrap-single-enum]})
  => (throws-info {:check #{:vip :guest}
                   :data :account.type/WRONG
                   :id :wrong-input}))
