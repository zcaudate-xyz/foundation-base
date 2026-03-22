(ns std.lib.transform.validate-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.validate :as validate])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus]
   :normalise-single [validate/wrap-single-model-validate]
   :normalise-branch [graph/wrap-key-path]
   :normalise-attr   [graph/wrap-key-path]})

^{:refer std.lib.transform.validate/wrap-single-model-validate :added "3.0"}
(fact "validates input according to model"

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:validate {:account {:name (fn [x _] (number? x))}}}}
                   *wrappers*)
  => (throws-info {:id :not-validated :nsv [:account :name]})

  (graph/normalise {:account/name "Bob"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:validate {:account {:name (fn [x _] (= x "Bob"))}}}}
                   *wrappers*)
  => {:account {:name "Bob"}})
