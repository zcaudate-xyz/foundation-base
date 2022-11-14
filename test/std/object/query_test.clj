(ns std.object.query-test
  (:use code.test)
  (:require [std.object.query :refer :all]
            [std.lib.version :as version]))

(def -world-array- (byte-array (map byte "world")))

^{:refer std.object.query/all-class-members :added "3.0"}
(fact "returns the raw reflected methods, fields and constructors"

  (all-class-members String))

^{:refer std.object.query/all-class-elements :added "3.0"}
(fact "returns elements "

  (all-class-elements String))

^{:refer std.object.query/select-class-elements :added "3.0"}
(fact "returns the processed reflected methods, fields and constructors"

  (select-class-elements String [#"^c" :name]))

^{:refer std.object.query/query-class :added "3.0"}
(fact "queries the java view of the class declaration"

  (query-class String [#"^c" :name])
  ;;=> ["charAt" "checkBounds" "codePointAt" "codePointBefore"
  ;;    "codePointCount" "compareTo" "compareToIgnoreCase"
  ;;    "concat" "contains" "contentEquals" "copyValueOf"]
  )

^{:refer std.object.query/select-supers-elements :added "3.0"}
(fact "returns the elements related to the type's super classes"

  (select-supers-elements String []))

^{:refer std.object.query/query-supers :added "3.0"}
(fact "returns all elements associated with the context class's super"

  (query-supers "122" []))

^{:refer std.object.query/query-hierarchy :added "3.0"}
(fact "lists what methods could be applied to a particular instance"

  (query-hierarchy String [:name #"^to"])
  => ["toCharArray" "toLowerCase" "toString" "toUpperCase"])

^{:refer std.object.query/all-instance-elements :added "3.0"}
(fact "returns the hierarchy of elements corresponding to a class"

  (all-instance-elements String nil))

^{:refer std.object.query/select-instance-elements :added "3.0"}
(fact "returns the hierarchy of elements corresponding to a class"

  (select-instance-elements String nil [#"^c" :name]))

^{:refer std.object.query/query-instance :added "3.0"}
(fact "lists what class methods could be applied to a particular instance"

  (query-instance "abc" [:name #"^to"])
  => ["toCharArray" "toLowerCase" "toString" "toUpperCase"]

  (query-instance String [:name #"^to"])
  => (contains ["toString"]))

^{:refer std.object.query/query-instance-hierarchy :added "3.0"}
(fact "lists what methods could be applied to a particular instance. includes all super class methods"

  (query-instance-hierarchy String [:name #"^to"])
  => ["toCharArray" "toLowerCase" "toString" "toUpperCase"])

^{:refer std.object.query/apply-element :added "3.0"}
(fact "apply the class element to arguments"

  (->> (apply-element "123" "value" [])
       (map char))
  => [\1 \2 \3])

^{:refer std.object.query/delegate :added "3.0"}
(fact "Allow transparent field access and manipulation to the underlying object."

  (def -a- "hello")
  (def -*a-  (delegate -a-))
  (def -world-array- (.getBytes "world"))

  (mapv char (-*a- :value)) => [\h \e \l \l \o]

  (-*a- :value -world-array-)
  (String. ^bytes (-*a- :value)) => "world"
  -a- => "world")

^{:refer std.object.query/invoke-intern-element :added "3.0"}
(fact "creates the form for `element` for definvoke"

  (invoke-intern-element :element '-foo- {:class String
                                          :selector ["charAt"]} nil) ^:hidden
  ;;   '(clojure.core/let [elem ((clojure.core/get std.object.query/+query-functions+
  ;;                                                (clojure.core/or nil :class))
  ;;                              java.lang.String
  ;;                              (clojure.core/cons :merge ["charAt"]))
  ;;                        arglists (std.object.element/element-params elem)]
  ;;       (clojure.core/doto (def -foo- elem)
  ;;         (clojure.core/alter-meta! clojure.core/merge {:arglists arglists} {})))
  => seq?)

(comment
  (./ns:reset '[hara])
  (code.manage/import))


