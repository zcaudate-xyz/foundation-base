(ns std.lib.transform.ignore-test
  (:use code.test)
  (:require [std.lib.transform :as graph]
            [std.lib.transform.ignore :as ignore]
            [std.lib.schema :as schema]
            [example.data :as examples]))

^{:refer std.lib.transform.ignore/wrap-nil-model-ignore :added "3.0"}
(fact "wraps the normalise-nil function such that any unknown keys are ignored"
  (graph/normalise {:account {:name "Chris"
                              :age 10
                              :parents ["henry" "sally"]}}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:ignore {:account {:parents :checked}}}}
                   {:normalise-nil [ignore/wrap-nil-model-ignore]})
  => {:account {:name "Chris"
                :age 10
                :parents ["henry" "sally"]}}
  ^:hidden
  (graph/normalise {:account {:name "Chris"
                              :age 10
                              :parents ["henry" "sally"]}}
                   {:schema (schema/schema examples/account-name-age-sex)}
                   {:normalise-branch [graph/wrap-key-path]})
  => (throws-info {:key-path [:account]
                   :nsv [:account :parents]
                   :id :no-schema}))
