(ns std.lang.model.spec-dart-test
  (:require [std.lang :as l]
            [std.lang.model.spec-dart :as spec-dart])
  (:use code.test))

(fact "basic dart emission"
  (l/emit-as :dart ['(var greeting "hello")])
  => "var greeting = \"hello\""

  (l/emit-as :dart ['(defn hello [name] (return (x-cat "hi " name)))])
  => "hello(name) {\n  return \"hi \" + name\n}"

  (l/emit-as :dart ['(new Person name)])
  => "new Person(name)"

  (l/emit-as :dart ['{:name "hello" :count 2}])
  => "{\"name\":\"hello\",\"count\":2}")

(fact "xtalk helper rewrites for dart"
  (l/emit-as :dart ['(x-print "hello")])
  => "print(\"hello\")"

  (l/emit-as :dart ['(x-len [1 2 3])])
  => "[1,2,3].length"

  (l/emit-as :dart ['(x-arr-push items 1)])
  => "items.add(1)")

^{:refer std.lang.model.spec-dart/dart-map-key :added "4.1"}
(fact "emits map keys for dart"
  (spec-dart/dart-map-key :hello spec-dart/+grammar+ {})
  => "\"hello\""

  (spec-dart/dart-map-key '(+ a 1) spec-dart/+grammar+ {})
  => "(a + 1)")
