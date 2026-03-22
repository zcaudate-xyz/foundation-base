(ns std.lib.transform.base.keyword-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.base.keyword :refer :all])
  (:use code.test))

^{:refer std.lib.transform.base.keyword/wrap-single-keyword :added "3.0"}
(fact "removes the keyword namespace if there is one"

  (graph/normalise {:account {:type :account.type/vip}}
                   {:schema (schema/schema {:account/type [{:type :keyword
                                                            :keyword {:ns :account.type}}]})}
                   {:normalise-single [wrap-single-keyword]})
  => {:account {:type :vip}})
