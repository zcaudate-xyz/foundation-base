tahto/model/spec_haxe_test.clj:1:(ns tahto.model.spec-haxe-test
  (:require [tahto.core :as l]
tahto/model/spec_haxe_test.clj:3:            [tahto.model.spec-haxe :as haxe]
            [std.string.prose :as prose])
  (:use code.test))

tahto/model/spec_haxe_test.clj:7:^{:refer tahto.model.spec-haxe/haxe-fn :added "4.1"}
(fact "normalizes function forms for Haxe"
  (l/emit-as
   :haxe '[(fn [x y] (return (+ x y)))])
  => "function (x, y) {\n  return x + y;\n}")

tahto/model/spec_haxe_test.clj:13:^{:refer tahto.model.spec-haxe/haxe-var :added "4.1"}
(fact "emits Haxe var declarations"
  (l/emit-as
   :haxe '[(var x 1)
           (var y 2)])
  => (prose/|
      "var x = 1"
      ""
      "var y = 2"))

tahto/model/spec_haxe_test.clj:23:^{:refer tahto.model.spec-haxe/haxe-symbol :added "4.1"}
(fact "converts hyphens to underscores"
  (l/emit-as
   :haxe '[(my-function my-arg)])
  => "my_function(my_arg)")

tahto/model/spec_haxe_test.clj:29:^{:refer tahto.model.spec-haxe/haxe-for-array :added "4.1"}
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

tahto/model/spec_haxe_test.clj:45:^{:refer tahto.model.spec-haxe/haxe-tf-x-del :added "4.1"}
(fact "emits Reflect.deleteField for x-del"
  (l/emit-as
   :haxe '[(x:del obj :foo)])
  => "Reflect.deleteField(obj,\"foo\")")

tahto/model/spec_haxe_test.clj:51:^{:refer tahto.model.spec-haxe/haxe-tf-x-cat :added "4.1"}
(fact "emits string concatenation"
  (l/emit-as
   :haxe '[(x:cat "Hello, " name "!")])
  => "\"Hello, \" + name + \"!\"")

tahto/model/spec_haxe_test.clj:57:^{:refer tahto.model.spec-haxe/haxe-tf-x-len :added "4.1"}
(fact "emits length access"
  (l/emit-as
   :haxe '[(x:len arr)])
  => "arr.length")

tahto/model/spec_haxe_test.clj:63:^{:refer tahto.model.spec-haxe/haxe-tf-x-json-encode :added "4.1"}
(fact "emits JSON encoding"
  (l/emit-as
   :haxe '[(x:json-encode {:a 1})])
  => "haxe.Json.stringify([\"a\" => 1])")

tahto/model/spec_haxe_test.clj:69:^{:refer tahto.model.spec-haxe/haxe-tf-x-type-native :added "4.1"}
(fact "emits type detection code"
  (let [out (l/emit-as
             :haxe '[(x:type-native x)])]
    (boolean (re-find #"Std.isOfType" out)))
  => true)


tahto/model/spec_haxe_test.clj:77:^{:refer tahto.model.spec-haxe/haxe-tf-x-del-key :added "4.1"}
(fact "deletes object key")

tahto/model/spec_haxe_test.clj:80:^{:refer tahto.model.spec-haxe/haxe-tf-x-get-key :added "4.1"}
(fact "gets object key")

tahto/model/spec_haxe_test.clj:83:^{:refer tahto.model.spec-haxe/haxe-tf-x-has-key? :added "4.1"}
(fact "checks object key")

tahto/model/spec_haxe_test.clj:86:^{:refer tahto.model.spec-haxe/haxe-tf-x-err :added "4.1"}
(fact "raises errors")

tahto/model/spec_haxe_test.clj:89:^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/spec_haxe_test.clj:92:^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/spec_haxe_test.clj:95:^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/spec_haxe_test.clj:98:^{:refer tahto.model.spec-haxe/haxe-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/spec_haxe_test.clj:101:^{:refer tahto.model.spec-haxe/haxe-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

tahto/model/spec_haxe_test.clj:104:^{:refer tahto.model.spec-haxe/haxe-tf-x-apply :added "4.1"}
(fact "applies arguments")

tahto/model/spec_haxe_test.clj:107:^{:refer tahto.model.spec-haxe/haxe-tf-x-random :added "4.1"}
(fact "generates random values")

tahto/model/spec_haxe_test.clj:110:^{:refer tahto.model.spec-haxe/haxe-tf-x-print :added "4.1"}
(fact "prints values")

tahto/model/spec_haxe_test.clj:113:^{:refer tahto.model.spec-haxe/haxe-tf-x-to-string :added "4.1"}
(fact "converts to string")

tahto/model/spec_haxe_test.clj:116:^{:refer tahto.model.spec-haxe/haxe-tf-x-to-number :added "4.1"}
(fact "converts to number")

tahto/model/spec_haxe_test.clj:119:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-string? :added "4.1"}
(fact "checks string type")

tahto/model/spec_haxe_test.clj:122:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-number? :added "4.1"}
(fact "checks number type")

tahto/model/spec_haxe_test.clj:125:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

tahto/model/spec_haxe_test.clj:128:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

tahto/model/spec_haxe_test.clj:131:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-function? :added "4.1"}
(fact "checks function type")

tahto/model/spec_haxe_test.clj:134:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-object? :added "4.1"}
(fact "checks object type")

tahto/model/spec_haxe_test.clj:137:^{:refer tahto.model.spec-haxe/haxe-tf-x-is-array? :added "4.1"}
(fact "checks array type")

tahto/model/spec_haxe_test.clj:140:^{:refer tahto.model.spec-haxe/haxe-tf-x-json-decode :added "4.1"}
(fact "decodes JSON")

tahto/model/spec_haxe_test.clj:143:^{:refer tahto.model.spec-haxe/haxe-tf-x-return-encode :added "4.1"}
(fact "encodes return values")

tahto/model/spec_haxe_test.clj:146:^{:refer tahto.model.spec-haxe/haxe-tf-x-return-wrap :added "4.1"}
(fact "wraps return values")

tahto/model/spec_haxe_test.clj:149:^{:refer tahto.model.spec-haxe/haxe-tf-x-return-eval :added "4.1"}
(fact "evaluates return values")

tahto/model/spec_haxe_test.clj:152:^{:refer tahto.model.spec-haxe/haxe-for-object :added "4.1"}
(fact "emits haxe object loops")

tahto/model/spec_haxe_test.clj:155:^{:refer tahto.model.spec-haxe/haxe-map-key :added "4.1"}
(fact "emits haxe map keys")
