(ns hara.model.spec-haxe-test
  (:require [hara.lang :as l]
            [hara.model.spec-haxe :as haxe]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer hara.model.spec-haxe/haxe-fn :added "4.1"}
(fact "normalizes function forms for Haxe"
  (l/emit-as
   :haxe '[(fn [x y] (return (+ x y)))])
  => "function (x, y) {\n  return x + y;\n}")

^{:refer hara.model.spec-haxe/haxe-var :added "4.1"}
(fact "emits Haxe var declarations"
  (l/emit-as
   :haxe '[(var x 1)
           (var y 2)])
  => (prose/|
      "var x = 1"
      ""
      "var y = 2"))

^{:refer hara.model.spec-haxe/haxe-symbol :added "4.1"}
(fact "converts hyphens to underscores"
  (l/emit-as
   :haxe '[(my-function my-arg)])
  => "my_function(my_arg)")

^{:refer hara.model.spec-haxe/haxe-for-array :added "4.1"}
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

^{:refer hara.model.spec-haxe/haxe-tf-x-del :added "4.1"}
(fact "emits Reflect.deleteField for x-del"
  (l/emit-as
   :haxe '[(x:del obj :foo)])
  => "Reflect.deleteField(obj,\"foo\")")

^{:refer hara.model.spec-haxe/haxe-tf-x-cat :added "4.1"}
(fact "emits string concatenation"
  (l/emit-as
   :haxe '[(x:cat "Hello, " name "!")])
  => "\"Hello, \" + name + \"!\"")

^{:refer hara.model.spec-haxe/haxe-tf-x-len :added "4.1"}
(fact "emits length access"
  (l/emit-as
   :haxe '[(x:len arr)])
  => "arr.length")

^{:refer hara.model.spec-haxe/haxe-tf-x-json-encode :added "4.1"}
(fact "emits JSON encoding"
  (l/emit-as
   :haxe '[(x:json-encode {:a 1})])
  => "haxe.Json.stringify([\"a\" => 1])")

^{:refer hara.model.spec-haxe/haxe-tf-x-type-native :added "4.1"}
(fact "emits type detection code"
  (let [out (l/emit-as
             :haxe '[(x:type-native x)])]
    (boolean (re-find #"Std.isOfType" out)))
  => true)
