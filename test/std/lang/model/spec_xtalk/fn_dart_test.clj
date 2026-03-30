(ns std.lang.model.spec-xtalk.fn-dart-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.fn-dart :refer :all]))

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-len :added "4.1"}
(fact "gets length"
  (l/emit-as :dart [(dart-tf-x-len '[_ arr])])
  => #"length")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cat :added "4.1"}
(fact "concatenates"
  (l/emit-as :dart [(dart-tf-x-cat '[_ "a" "b"])])
  => #"\+")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-print :added "4.1"}
(fact "prints values"
  (l/emit-as :dart [(dart-tf-x-print '[_ "hello"])])
  => #"print")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (l/emit-as :dart [(dart-tf-x-arr-push '[_ arr 1])])
  => #"add")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-apply :added "4.1"}
(fact "applies function"
  (l/emit-as :dart [(dart-tf-x-apply '[_ f args])])
  => #"Function\.apply")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-now-ms :added "4.1"}
(fact "gets current time in milliseconds"
  (l/emit-as :dart [(dart-tf-x-now-ms '[_])])
  => #"DateTime\.now.*millisecondsSinceEpoch")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-random :added "4.1"}
(fact "generates random number"
  (l/emit-as :dart [(dart-tf-x-random '[_])])
  => #"Random.*nextDouble")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-type-native :added "4.1"}
(fact "gets runtime type"
  (l/emit-as :dart [(dart-tf-x-type-native '[_ obj])])
  => #"runtimeType")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-abs :added "4.1"}
(fact "absolute value"
  (l/emit-as :dart [(dart-tf-x-m-abs '[_ x])])
  => #"\.abs")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-sin :added "4.1"}
(fact "sine"
  (l/emit-as :dart [(dart-tf-x-m-sin '[_ x])])
  => #"sin")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-sqrt :added "4.1"}
(fact "square root"
  (l/emit-as :dart [(dart-tf-x-m-sqrt '[_ x])])
  => #"sqrt")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-max :added "4.1"}
(fact "maximum"
  (l/emit-as :dart [(dart-tf-x-m-max '[_ a b])])
  => #"max")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-mod :added "4.1"}
(fact "modulo"
  (l/emit-as :dart [(dart-tf-x-m-mod '[_ a b])])
  => #"%")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-pow :added "4.1"}
(fact "power"
  (l/emit-as :dart [(dart-tf-x-m-pow '[_ a b])])
  => #"math\.pow")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-string? :added "4.1"}
(fact "checks if string"
  (l/emit-as :dart [(dart-tf-x-is-string? '[_ x])])
  => #"is\(x,String\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-del :added "4.1"}
(fact "deletes key from map"
  (l/emit-as :dart [(dart-tf-x-del '[_ obj key])])
  => #"remove")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-eval :added "4.1"}
(fact "eval not supported"
  (l/emit-as :dart [(dart-tf-x-eval '[_ s])])
  => #"eval not supported")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-has-key? :added "4.1"}
(fact "checks if key exists"
  (l/emit-as :dart [(dart-tf-x-has-key? '[_ obj key nil])])
  => #"\[\]")



^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-shell :added "4.1"}
(fact "shell not implemented"
  (l/emit-as :dart [(dart-tf-x-shell '[_ s opts])])
  => #"shell not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-b64-encode :added "4.1"}
(fact "base64 encode"
  (l/emit-as :dart [(dart-tf-x-b64-encode '[_ s])])
  => #"base64\.encode.*utf8\.encode")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-b64-decode :added "4.1"}
(fact "base64 decode"
  (l/emit-as :dart [(dart-tf-x-b64-decode '[_ s])])
  => #"utf8\.decode.*base64\.decode")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-uri-encode :added "4.1"}
(fact "uri encode"
  (l/emit-as :dart [(dart-tf-x-uri-encode '[_ s])])
  => #"Uri\.encodeComponent")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-uri-decode :added "4.1"}
(fact "uri decode"
  (l/emit-as :dart [(dart-tf-x-uri-decode '[_ s])])
  => #"Uri\.decodeComponent")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-slurp :added "4.1"}
(fact "slurp not implemented"
  (l/emit-as :dart [(dart-tf-x-slurp '[_ filename])])
  => #"slurp not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-spit :added "4.1"}
(fact "spit not implemented"
  (l/emit-as :dart [(dart-tf-x-spit '[_ filename s])])
  => #"spit not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-format :added "4.1"}
(fact "str-format not implemented"
  (l/emit-as :dart [(dart-tf-x-str-format '[_ template values])])
  => #"str-format not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-client-basic :added "4.1"}
(fact "client basic not implemented"
  (l/emit-as :dart [(dart-tf-x-client-basic '[_ host port opts cb])])
  => #"Client not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-thread-spawn :added "4.1"}
(fact "thread spawn not implemented"
  (l/emit-as :dart [(dart-tf-x-thread-spawn '[_ f])])
  => #"Thread spawn not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-with-delay :added "4.1"}
(fact "delay future"
  (l/emit-as :dart [(dart-tf-x-with-delay '[_ ms value])])
  => #"Future\.delayed.*Duration.*milliseconds")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-start-interval :added "4.1"}
(fact "start interval"
  (l/emit-as :dart [(dart-tf-x-start-interval '[_ ms f])])
  => #"Timer\.periodic.*Duration.*milliseconds")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-stop-interval :added "4.1"}
(fact "stop interval"
  (l/emit-as :dart [(dart-tf-x-stop-interval '[_ timer])])
  => #"cancel")