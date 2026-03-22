(ns std.lib.transform.fill-assoc-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.fill-assoc :refer :all])
  (:use code.test))

^{:refer std.lib.transform.fill-assoc/process-fill-assoc :added "3.0"}
(fact "helper function for wrap-model-fill-assoc"

  (process-fill-assoc {:age 10}
                      {:name "Chris", :age 9}
                      [:account]
                      {}
                      {:fill-assoc {:account {:age 10}}}
                      {})
  => {:name "Chris", :age #{9 10}})

^{:refer std.lib.transform.fill-assoc/wrap-model-fill-assoc :added "3.0"}
(fact "adds an additional value to the entry"

  (graph/normalise {:account/name "Chris" :account/age 9}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:fill-assoc {:account {:age 10}}}}
                   {:normalise [wrap-model-fill-assoc]})
  => {:account {:name "Chris", :age #{9 10}}})

(comment
  (./import)
  (./scaffold))
