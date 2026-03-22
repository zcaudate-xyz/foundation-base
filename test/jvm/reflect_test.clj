(ns jvm.reflect-test
  (:require [jvm.reflect :refer :all]
            [std.object.query :as query])
  (:use code.test)
  (:refer-clojure :exclude [.% .%> .? .?* .?> .* .*> .&]))

^{:refer jvm.reflect/.% :added "3.0"}
(fact "Lists class information"
  (.% String)
  => (contains {:modifiers #{:instance :public :final :class},
                :name "java.lang.String"}))

^{:refer jvm.reflect/print-hierarchy :added "3.0"}
(fact "helper function to `.%>`")

^{:refer jvm.reflect/.%> :added "3.0"}
(fact "Lists the class and interface hierarchy for the class"

  (.%> String)
  => [java.lang.String
      [java.lang.Object
       #{java.lang.constant.ConstantDesc
         java.io.Serializable
         java.lang.Comparable
         java.lang.constant.Constable
         java.lang.CharSequence}]])

^{:refer jvm.reflect/.& :added "3.0"}
(fact "pretty prints  the data representation of a value"

  (.& {:a 1 :b 2 :c 3}))

^{:refer jvm.reflect/query-printed :added "3.0"}
(fact "prints the output on the result to display"

  (query-printed "CLASS" query/query-class String [#"^c" :name])

  (query-printed "CLASS" query/query-class String [#"^c"]))

^{:refer jvm.reflect/.? :added "3.0"}
(fact "queries the java view of the class declaration"

  (.? String  #"^c" :name)
  => (contains ["charAt"])
  ;;["charAt" "chars" "checkBoundsBeginEnd"
  ;; "checkBoundsOffCount" "checkIndex" "checkOffset"
  ;; "codePointAt" "codePointBefore" "codePointCount" "codePoints"
  ;; "coder" "compareTo" "compareToIgnoreCase" "concat" "contains"
  ;; "contentEquals" "copyValueOf"]
  )

^{:refer jvm.reflect/.?> :added "3.0"}
(fact "queries the the class hierarchy"

  (.?> String  #"^to"))

^{:refer jvm.reflect/.?* :added "3.0"}
(fact "queries the the class hierarchy"

  (.?* String  #"^to"))

^{:refer jvm.reflect/.* :added "3.0"}
(fact "lists what methods could be applied to a particular instance"

  (.* "abc" #"^to")

  (.* String :name #"^to")
  => (contains ["toString"]))

^{:refer jvm.reflect/.*> :added "3.0"}
(fact "lists what methods could be applied to a particular instance"

  (.*> "abc" #"^to")

  (.*> String :name #"^to")
  => (contains ["toString"]))

^{:refer jvm.reflect/.> :added "3.0"}
(fact "Threads the first input into the rest of the functions. Same as `->` but
   allows access to private fields using both `:keyword` and `.symbol` lookup:"

  (.> "abcd" :value String.) => "abcd"

  (.> "abcd" .value String.) => "abcd"

  (comment "BROKEN"
    (def -b- (byte-array (.getBytes "world")))
    (let [a  "hello"
          _  (.> "hello" (.value -b-))]
      a)
    => "world"))
