(ns std.lib.transform.convert-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.convert :as convert])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus]
   :normalise-single [convert/wrap-single-model-convert]
   :normalise-branch [graph/wrap-key-path]
   :normalise-attr   [graph/wrap-key-path]})

^{:refer std.lib.transform.convert/wrap-single-model-convert :added "3.0"}
(fact "converts input according to model"
  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:convert {:account {:name (fn [x _] (.toLowerCase ^String x))}}}}
                   *wrappers*)
  => {:account {:name "chris"}})
