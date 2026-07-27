(ns tahto.model.spec-haxe-test
  (:require [tahto.core :as l]
            [tahto.model.spec-haxe :as haxe]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer tahto.model.spec-haxe/haxe-fn :added "4.1"}
(fact "normalizes function forms for Haxe"
  (l/emit-as
   :haxe '[(fn [x y] (return (+ x y)))])
  => "function (x, y) {\n  return x + y;\n}")

^{:refer tahto.model.spec-haxe/haxe-var :added "4.1"}
(fact "emits Haxe var declarations"
  (l/emit-as
   :haxe '[(var x 1)
           (var y 2)])
  => (prose/|
      "var x = 1"
      ""
      "var y = 2"))

^{:refer tahto.model.spec-haxe/haxe-symbol :added "4.1"}
(fact "converts hyphens to underscores"
  (l/emit-as
   :haxe '[(my-function my-arg)])
  => "my_function(my_arg)")

^{:refer tahto.model.spec-haxe/haxe-for-array :added "4.1"}
(fact "emits Haxe for loops over arrays"
  (l/emit-as
   :haxe '[(var total 0)
           (for:array [n numbers]
             (:= total (+ total n)))
           (return total)])
  => (prose/|
      "var total = 0"
      ""
      "for(n in numbers){"
      "  total = (total + n);"
      "}"
      ""
      "return total"))

^{:refer tahto.model.spec-haxe/haxe-tf-x-del :added "4.1"}
(fact "emits Reflect.deleteField for x-del"
  (l/emit-as
   :haxe '[(x:del obj :foo)])
  => "Reflect.deleteField(obj,\"foo\")")

^{:refer tahto.model.spec-haxe/haxe-tf-x-cat :added "4.1"}
(fact "emits string concatenation"
  (l/emit-as
   :haxe '[(x:cat "Hello, " name "!")])
  => "\"Hello, \" + name + \"!\"")

^{:refer tahto.model.spec-haxe/haxe-tf-x-len :added "4.1"}
(fact "emits length access"
  (l/emit-as
   :haxe '[(x:len arr)])
  => "arr.length")

^{:refer tahto.model.spec-haxe/haxe-tf-x-json-encode :added "4.1"}
(fact "emits JSON encoding"
  (l/emit-as
   :haxe '[(x:json-encode {:a 1})])
  => "haxe.Json.stringify([\"a\" => 1])")

^{:refer tahto.model.spec-haxe/haxe-tf-x-type-native :added "4.1"}
(fact "emits type detection code"
  (let [out (l/emit-as
             :haxe '[(x:type-native x)])]
    (boolean (re-find #"Std.isOfType" out)))
  => true)


^{:refer tahto.model.spec-haxe/haxe-tf-x-del-key :added "4.1"}
(fact "deletes object key")

^{:refer tahto.model.spec-haxe/haxe-tf-x-get-key :added "4.1"}
(fact "gets object key")

^{:refer tahto.model.spec-haxe/haxe-tf-x-has-key? :added "4.1"}
(fact "checks object key")

^{:refer tahto.model.spec-haxe/haxe-tf-x-err :added "4.1"}
(fact "raises errors")

^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

^{:refer tahto.model.spec-haxe/haxe-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

^{:refer tahto.model.spec-haxe/haxe-tf-x-apply :added "4.1"}
(fact "applies arguments")

^{:refer tahto.model.spec-haxe/haxe-tf-x-random :added "4.1"}
(fact "generates random values")

^{:refer tahto.model.spec-haxe/haxe-tf-x-print :added "4.1"}
(fact "prints values")

^{:refer tahto.model.spec-haxe/haxe-tf-x-to-string :added "4.1"}
(fact "converts to string")

^{:refer tahto.model.spec-haxe/haxe-tf-x-to-number :added "4.1"}
(fact "converts to number")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-string? :added "4.1"}
(fact "checks string type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-number? :added "4.1"}
(fact "checks number type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-function? :added "4.1"}
(fact "checks function type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-object? :added "4.1"}
(fact "checks object type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-is-array? :added "4.1"}
(fact "checks array type")

^{:refer tahto.model.spec-haxe/haxe-tf-x-json-decode :added "4.1"}
(fact "decodes JSON")

^{:refer tahto.model.spec-haxe/haxe-tf-x-return-encode :added "4.1"}
(fact "encodes return values")

^{:refer tahto.model.spec-haxe/haxe-tf-x-return-wrap :added "4.1"}
(fact "wraps return values")

^{:refer tahto.model.spec-haxe/haxe-tf-x-return-eval :added "4.1"}
(fact "evaluates return values")

^{:refer tahto.model.spec-haxe/haxe-for-object :added "4.1"}
(fact "emits haxe object loops")

^{:refer tahto.model.spec-haxe/haxe-map-key :added "4.1"}
(fact "emits haxe map keys")
