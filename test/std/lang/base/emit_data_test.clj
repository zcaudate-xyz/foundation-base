(ns std.lang.base.emit-data-test
  (:use code.test)
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-data :as data :refer :all]
            [std.lang.base.grammar :as grammar]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-data/default-map-key :added "4.0"}
(fact "emits a default map key"
  ^:hidden
  
  (default-map-key 'hello
                   helper/+default+
                   {})
  => "hello")

^{:refer std.lang.base.emit-data/emit-map-key :added "4.0"}
(fact "emits the map key"
  ^:hidden
  
  (emit-map-key 'hello
                helper/+default+
                {})
  => "hello"

  (emit-map-key "hello"
                helper/+default+
                {})
  => "\"hello\""

  (emit-map-key :hello
                (merge helper/+default+
                       {:data {:map-entry  {:start ""  :end ""  :assign ":" :space "" :keyword :keyword}}})
                {})
  => ":hello")

^{:refer std.lang.base.emit-data/emit-map-entry :added "3.0"}
(fact "emits the map entry"
  ^:hidden

  (emit-map-entry [:hello "world"] helper/+default+ {})
  => "\"hello\":\"world\""
  => "'hello':'world'"

  (emit-map-entry [:hello "world"]
                  (assoc-in helper/+default+
                            [:data :map-entry :assign] " = ") {})
  => "\"hello\" = \"world\"")

^{:refer std.lang.base.emit-data/emit-singleline-array? :added "4.0"}
(fact "checks that array is all single lines"

  (emit-singleline-array? ["1" "2" "3"])
  => true

  (emit-singleline-array? ["1" "\n2" "3"])
  => false)

^{:refer std.lang.base.emit-data/emit-maybe-multibody :added "4.0"}
(fact "checks that array is all single lines"

  (emit-maybe-multibody ["1" "2"] "hello")
  => "2hello"

  (emit-maybe-multibody ["1" "2"] "\nhello")
  => "1  \n  hello")

^{:refer std.lang.base.emit-data/emit-coll-layout :added "4.0"}
(fact "constructs the collection"
  ^:hidden
  
  (emit-coll-layout :vector 2 ["1" "2" "3"] helper/+default+ {})
  => "[1,2,3]"

  (emit-coll-layout :vector 2 ["1" "\n2" "3"] helper/+default+ {})
  
  => "[\n    1,\n    \n  2,\n    3\n  ]"

  (emit-coll-layout :tuple 2 ["1" "\n2" "3"] helper/+default+ {})
  => "  (1,\n    \n  2,\n    3)")

^{:refer std.lang.base.emit-data/emit-coll :added "3.0"}
(fact "emits a collection"
  ^:hidden

  (emit-coll :vector [1 2 3] helper/+default+ {})
  => "[1,2,3]"

  (emit-coll :set [1 2 3] helper/+default+ {})
  => "(1,2,3)"
  
  (emit-coll :custom [1 2 3] (assoc-in helper/+default+
                                       [:data :custom]
                                       {:start "<" :end ">" :space ""}) {})
  => "<1,2,3>")

^{:refer std.lang.base.emit-data/emit-data-standard :added "4.0"}
(fact "emits either a custom string or default coll"
  (emit-data-standard :vector [1 2 3] helper/+default+ {})
  => "[1,2,3]")

^{:refer std.lang.base.emit-data/emit-data :added "3.0"}
(fact "main function for data forms"
  ^:hidden

  (emit-data :map {:a 1 :b 2} helper/+default+ {:map {}})
  => "{[:a 1],[:b 2]}"
  
  (emit-data :vector {:a 1 :b 2} helper/+default+ {:map {}})
  => "[[:a 1],[:b 2]]")

^{:refer std.lang.base.emit-data/emit-quote :added "4.0"}
(fact "emit quote structures"

  (emit-quote nil nil ''(1 2 3) +grammar+ {})
  => "(1,2,3)")

^{:refer std.lang.base.emit-data/emit-table-group :added "4.0"}
(fact "gets table group"
  (emit-table-group [:a 1 :b 2 :c :d])
  => [[:a 1] [:b 2] 'c 'd])

^{:refer std.lang.base.emit-data/emit-table :added "4.0"}
(fact "emit quote structures"

  (emit-table nil nil '(tab :a 1 :b 2) +grammar+ {})
  => "{\"a\":1,\"b\":2}")

^{:refer std.lang.base.emit-data/test-data-loop :adopt true :added "4.0"}
(fact "emit for data structures"

  (test-data-loop '[(+ 1 2)]
                        +grammar+
                        {})
  => "[(+ 1 2)]"

  (test-data-loop '{:a (+ 1 2)}
                        +grammar+
                        {})
  => "{[:a (+ 1 2)]}"

  (test-data-loop '#{(+ 1 2)}
                        +grammar+
                        {})
  => throws

  
  [:quote]
  (test-data-loop ''((+ A B) C)
                        +grammar+
                        {})
  => "((+ A B),C)"

  (test-data-loop ''[(+ A B) C]
                        +grammar+
                        {})
  => "(+ A B),C"
  
  [:table]
  (test-data-loop '(tab :a (+ 1 2) :b 2)
                        +grammar+
                        {})
  => "{\"a\":(+ 1 2),\"b\":2}"
  
  (test-data-loop '(tab 1 (+ 1 2) 3 4 5)
                        +grammar+
                        {})
  => "{1,(+ 1 2),3,4,5}")

^{:refer std.lang.base.test-data-emit/test-data-emit :adopt true :added "4.0"}
(fact "emit for data structures"

  (test-data-emit '[(+ 1 2)]
                    +grammar+
                    {})
  => "[1 + 2]"

  (test-data-emit '[(+ 1 2) (+ 3 4)]
                    +grammar+
                    {})
  => "[1 + 2,3 + 4]"

  (test-data-emit '[(+ 1 2) \0]
                    +grammar+
                    {})
  => "[1 + 2,]"
  

  (test-data-emit '{:a (+ 1 2)}
                    +grammar+
                    {})
  => "{\"a\":1 + 2}"

  (test-data-emit '{:a (not (+ 1 2))}
                    +grammar+
                    {})
  => "{\"a\":!(1 + 2)}"


  (test-data-emit '{(not (+ 1 2)) A}
                    +grammar+
                    {})
  => "{!(1 + 2):A}"
  
  
  [:quote]
  (test-data-emit ''((+ A B) C)
                    +grammar+
                    {})
  => "(A + B,C)"
  
  (test-data-emit ''[(+ A B) C]
                    +grammar+
                    {})
  => "A + B,C"
  
  [:table]
  (test-data-emit '(tab :a (+ 1 2) :b 2)
                    +grammar+
                    {})
  => "{\"a\":1 + 2,\"b\":2}"
  
  (test-data-emit '(tab 1 (+ 1 2) 3 4 5)
                    +grammar+
                    {})
  => "{1,1 + 2,3,4,5}")
