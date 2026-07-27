(ns std.object.element.impl.multi-test
  (:require [std.lib.version :as version]
            [std.object.element.impl.multi :as multi]
            [std.object.query :as query])
  (:use code.test)
  (:refer-clojure :exclude [chars]))

(defn chars [s]
  (-> (map byte s)
      (byte-array)))

^{:refer std.object.element.impl.multi/get-name :added "3.0"}
(fact "all elements in the array have to have the same name"
  (multi/get-name [{:name "a"} {:name "b"}])
  => (throws)

  (multi/get-name [{:name "a"} {:name "a"}])
  => "a")

^{:refer std.object.element.impl.multi/to-element-array :added "3.0"}
(fact "converts a three layer map to a flat sequence of values"

  (multi/to-element-array {:a {:b {:c 1
                                   :d 2}}})
  => [1 2])

^{:refer std.object.element.impl.multi/multi-element :added "3.0"}
(fact "combining elements together into one"

  (->> (query/query-class clojure.lang.PersistentVector ["create"])
       (multi/multi-element {}))
  ;;=> #[create :: ([java.util.List]), ... ([java.lang.Iterable])]
  )

^{:refer std.object.element.impl.multi/to-element-map-path :added "3.0"}
(fact "creates a map path for the element"

  (-> (query/query-class String ["charAt" :#])
      (multi/to-element-map-path))
  => [:method 2 [java.lang.String Integer/TYPE]])

^{:refer std.object.element.impl.multi/elegible-candidates :added "3.0"}
(fact "finds elegible candidates based upon argument list"

  (-> (query/query-class clojure.lang.PersistentVector ["create" :#])
      (get-in [:lookup :method 1])
      (multi/elegible-candidates [java.util.List]))
  ;;=> (#[create :: (java.util.List) -> clojure.lang.PersistentVector]
  ;;    #[create :: (java.lang.Iterable) -> clojure.lang.PersistentVector])
  )

^{:refer std.object.element.impl.multi/find-method-candidate :added "3.0"}
(fact "creates a map path for the element"
  (-> (query/query-class clojure.lang.PersistentVector ["create" :#])
      (multi/find-method-candidate [java.util.List]))
  ;; #[create :: (java.util.List) -> clojure.lang.PersistentVector]
  => std.object.element.common.Element)

^{:refer std.object.element.impl.multi/find-field-candidate :added "3.0"}
(fact "finds best field candidate for the element"

  (-> (query/query-class String ["value" :#])
      (multi/find-field-candidate [(type (chars "a"))]))
  ;; #[value :: (java.lang.String) | byte[]]
  => (any std.object.element.common.Element
          nil))

^{:refer std.object.element.impl.multi/find-candidate :added "3.0"}
(fact "finds best element within the multi, methods then fields"

  (-> (query/query-class String ["value" :#])
      (multi/find-candidate [String (type (chars "a"))]))
  ;; #[value :: (java.lang.String) | byte[]]
  => (any std.object.element.common.Element
          nil))
