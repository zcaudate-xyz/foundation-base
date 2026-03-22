(ns std.lib.transform.apply-test
  (:require [example.data :as examples]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.apply :as transform])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [graph/wrap-plus transform/wrap-model-pre-transform]
   :normalise-branch [graph/wrap-key-path]
   :normalise-attr   [graph/wrap-key-path]})

^{:refer std.lib.transform.apply/wrap-hash-set :added "3.0"}
(fact "allows operations to be performed on sets"

  ((transform/wrap-hash-set +) #{1 2 3} 10)
  => #{13 12 11}

  ((transform/wrap-hash-set +) 1 10)
  => 11)

^{:refer std.lib.transform.apply/process-transform :added "3.0"}
(fact "Converts one value to another either through a value or function"

  (transform/process-transform {:name "Bob"}
                               {:name "Chris"}
                               [:account]
                               {}
                               {}
                               {})
  => {:name "Bob"})

^{:refer std.lib.transform.apply/wrap-model-pre-transform :added "3.0"}
(fact "Applies a function transformation in the :pre-transform step"

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :pipeline {:pre-transform {:account {:name "Bob"}}}}
                   {:normalise [transform/wrap-model-pre-transform]})
  => {:account {:name "Bob"}}
  ^:hidden
  (graph/normalise {:account/orders #{{:number 1 :items {:name "one"}}
                                      {:number 2 :items {:name "two"}}}}
                   {:schema (schema/schema examples/account-orders-items-image)
                    :pipeline {:pre-transform {:account {:orders {:number (fn [x _] (inc x))
                                                                  :items {:name "thing"}}}}}}
                   *wrappers*)
  => {:account {:orders #{{:items {:name "thing"}, :number 2}
                          {:items {:name "thing"}, :number 3}}}}

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :name "Bob"
                    :pipeline {:pre-transform {:account {:name (fn [v _] (str v "tian"))}}}}
                   *wrappers*)
  => {:account {:name "Christian"}})

^{:refer std.lib.transform.apply/wrap-model-post-transform :added "3.0"}
(fact "applies a function transformation in the :post-transform step"

  (graph/normalise {:account/name "Chris"}
                   {:schema (schema/schema examples/account-name-age-sex)
                    :name "Bob"
                    :pipeline {:post-transform {:account {:name (fn [_ env] (:name env))}}}}
                   {:normalise [transform/wrap-model-post-transform]})
  => {:account {:name "Bob"}})

(comment
  (./import)
  (./scaffold))
