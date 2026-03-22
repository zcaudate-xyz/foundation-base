(ns std.lib.transform.fill-empty-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.fill-empty :refer :all])
  (:use code.test))

^{:refer std.lib.transform.fill-empty/process-fill-empty :added "3.0"}
(fact "helper functio for wrap-model-fill-empty"

  (process-fill-empty {:age 10}
                      {:name "Chris"}
                      [:account]
                      {}
                      (-> (schema/schema examples/account-name-age-sex)
                          :tree
                          :person)
                      {})
  => {:name "Chris", :age 10})

^{:refer std.lib.transform.fill-empty/wrap-model-fill-empty :added "3.0"}
(fact "fill empty entry with either value or function"

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:fill-empty {:account {:age 10}}}}
                   {:normalise [wrap-model-fill-empty]})
  => {:account {:name "Chris", :age 10}}

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:fill-empty {:account {:age (fn [_ datasource]
                                                             (:age datasource))}}}
                    :age 10}
                   {:normalise [wrap-model-fill-empty]})
  => {:account {:name "Chris", :age 10}}

  (graph/normalise {:account/name "Chris" :account/age 9}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:fill-empty {:account {:age 10}}}}
                   {:normalise [wrap-model-fill-empty]})
  => {:account {:name "Chris" :age 9}})

(comment
  (./import)
  (./scaffold))
