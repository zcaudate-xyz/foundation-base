(ns std.lib.transform.base.alias-test
  (:require [example.family :as family]
            [std.lib.schema :as schema]
            [std.lib.transform :as graph]
            [std.lib.transform.base.alias :refer :all])
  (:use code.test))

(def ^:dynamic *wrappers*
  {:normalise        [wrap-alias]})

^{:refer std.lib.transform.base.alias/find-aliases :added "3.0"}
(fact "finds necessary aliases"

  (find-aliases (:flat (schema/schema family/family-links))
                [:person/brother
                 :person/mother])
  => [[:person/brother {:ns :sibling,
                        :template {:sibling {:gender :m}}}]
      [:person/mother {:ns :parent,
                       :template {:parent {:gender :f}}}]])

^{:refer std.lib.transform.base.alias/template-alias :added "3.0"}
(fact "templates an alias, replacing symbols with random"

  (template-alias {:db/id 'hello})
  ;;{:db/id hello_141387}
  => (contains {:db/id symbol?}))

^{:refer std.lib.transform.base.alias/resolve-alias :added "3.0"}
(fact "resolves the data for the alias"

  (resolve-alias (:tree (schema/schema family/family-links))
                 {:male {:name "Chris"}}
                 [:male {:ns :person,
                         :template {:person {:gender :m}}}]
                 nil)
  => {:person {:gender :m, :name "Chris"}})

^{:refer std.lib.transform.base.alias/wrap-alias :added "3.0"}
(fact "wraps normalise to process aliases for a database schema"

  (graph/normalise {:male/name "Chris"}
                   {:schema (schema/schema family/family-links)}
                   {:normalise [wrap-alias]})
  => {:person {:gender :m, :name "Chris"}}

  (graph/normalise {:female {:parent/name "Sam"
                             :brother {:brother/name "Chris"}}}
                   {:schema (schema/schema family/family-links)})
  => {:person {:gender :f, :parent {:name "Sam"},
               :sibling {:gender :m,
                         :sibling {:gender :m,
                                   :name "Chris"}}}})

(comment
  (./import)
  (./scaffold))
