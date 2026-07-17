(ns hara.model.spec-xtalk.fn-dart-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.model.spec-xtalk.fn-dart :refer :all]))

(defn emit-dart [form]
  (l/emit-as :dart [form]))

^{:refer hara.model.spec-xtalk.fn-dart/dart-method0 :added "4.1"}
(fact "emits dart no-arg methods")

^{:refer hara.model.spec-xtalk.fn-dart/dart-runtime-type-string :added "4.1"}
(fact "gets dart runtime type string")

^{:refer hara.model.spec-xtalk.fn-dart/dart-is-map :added "4.1"}
(fact "checks dart map type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-map-get :added "4.1"}
(fact "gets dart map values")

^{:refer hara.model.spec-xtalk.fn-dart/dart-xt-exception? :added "4.1"}
(fact "checks dart exceptions")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-len :added "4.1"}
(fact "gets length"
  (l/emit-as :dart [(dart-tf-x-len '[_ arr])])
  => #"length")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-cat :added "4.1"}
(fact "concatenates"
  (l/emit-as :dart [(dart-tf-x-cat '[_ "a" "b"])])
  => #"\+")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-print :added "4.1"}
(fact "prints values"
  (l/emit-as :dart [(dart-tf-x-print '[_ "hello"])])
  => #"print")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (l/emit-as :dart [(dart-tf-x-arr-push '[_ arr 1])])
  => #"add")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-apply :added "4.1"}
(fact "applies function"
  (l/emit-as :dart [(dart-tf-x-apply '[_ f args])])
  => #"Function\.apply")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-now-ms :added "4.1"}
(fact "gets current time in milliseconds"
  (l/emit-as :dart [(dart-tf-x-now-ms '[_])])
  => #"DateTime\.now.*millisecondsSinceEpoch")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-random :added "4.1"}
(fact "generates random number"
  (l/emit-as :dart [(dart-tf-x-random '[_])])
  => #"math\.Random.*nextDouble")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-type-native :added "4.1"}
(fact "gets runtime type"
  (l/emit-as :dart [(dart-tf-x-type-native '[_ obj])])
  => #"runtimeType")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-del :added "4.1"}
(fact "deletes key from map"
  (l/emit-as :dart [(dart-tf-x-del '[_ obj key])])
  => #"remove")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-eval :added "4.1"}
(fact "eval not supported"
  (l/emit-as :dart [(dart-tf-x-eval '[_ s])])
  => #"eval not supported")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-has-key? :added "4.1"}
(fact "checks object key")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-del-key :added "4.1"}
(fact "deletes key from map by key"
  (emit-dart (dart-tf-x-del-key '[_ obj key]))
  => #"remove\(key\)")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-abs :added "4.1"}
(fact "wraps negative literal receivers for no-arg methods"
  (l/emit-as :dart [(dart-tf-x-m-abs '[_ -5])
                    (dart-tf-x-m-ceil '[_ -1.2])
                    (dart-tf-x-m-floor '[_ -1.2])])
  => #"(?s)\(-5\)\.abs\(\).*\(-1\.2\)\.ceil\(\).*\(-1\.2\)\.floor\(\)")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-ceil :added "4.1"}
(fact "ceiling"
  (emit-dart (dart-tf-x-m-ceil '[_ x]))
  => #"\.ceil")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-floor :added "4.1"}
(fact "floor"
  (emit-dart (dart-tf-x-m-floor '[_ x]))
  => #"\.floor")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-sin :added "4.1"}
(fact "sine"
  (l/emit-as :dart [(dart-tf-x-m-sin '[_ x])])
  => #"math\.sin")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-cos :added "4.1"}
(fact "cosine"
  (emit-dart (dart-tf-x-m-cos '[_ x]))
  => #"math\.cos")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-tan :added "4.1"}
(fact "tangent"
  (emit-dart (dart-tf-x-m-tan '[_ x]))
  => #"math\.tan")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-asin :added "4.1"}
(fact "arc sine"
  (emit-dart (dart-tf-x-m-asin '[_ x]))
  => #"math\.asin")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-acos :added "4.1"}
(fact "arc cosine"
  (emit-dart (dart-tf-x-m-acos '[_ x]))
  => #"math\.acos")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-atan :added "4.1"}
(fact "arc tangent"
  (emit-dart (dart-tf-x-m-atan '[_ x]))
  => #"math\.atan")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-sqrt :added "4.1"}
(fact "square root"
  (l/emit-as :dart [(dart-tf-x-m-sqrt '[_ x])])
  => #"math\.sqrt")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-exp :added "4.1"}
(fact "exponential"
  (emit-dart (dart-tf-x-m-exp '[_ x]))
  => #"math\.exp")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-loge :added "4.1"}
(fact "natural logarithm"
  (emit-dart (dart-tf-x-m-loge '[_ x]))
  => #"math\.log")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-max :added "4.1"}
(fact "maximum"
  (l/emit-as :dart [(dart-tf-x-m-max '[_ a b])])
  => #"math\.max")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-min :added "4.1"}
(fact "minimum"
  (emit-dart (dart-tf-x-m-min '[_ a b]))
  => #"math\.min")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-mod :added "4.1"}
(fact "modulo"
  (l/emit-as :dart [(dart-tf-x-m-mod '[_ a b])])
  => #"%")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-pow :added "4.1"}
(fact "power"
  (l/emit-as :dart [(dart-tf-x-m-pow '[_ a b])])
  => #"math\.pow")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-quot :added "4.1"}
(fact "integer quotient"
  (emit-dart (dart-tf-x-m-quot '[_ a b]))
  => #"~/")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-to-string :added "4.1"}
(fact "converts to string"
  (emit-dart (dart-tf-x-to-string '[_ x]))
  => #"toString")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-to-number :added "4.1"}
(fact "converts to number"
  (emit-dart (dart-tf-x-to-number '[_ x]))
  => #"num\.parse")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-string? :added "4.1"}
(fact "checks string type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-number? :added "4.1"}
(fact "checks number type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-function? :added "4.1"}
(fact "checks function type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-object? :added "4.1"}
(fact "checks object type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-is-array? :added "4.1"}
(fact "checks array type")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-char :added "4.1"}
(fact "gets string character")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-split :added "4.1"}
(fact "splits strings"
  (emit-dart (dart-tf-x-str-split '[_ s sep]))
  => #"split")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (emit-dart (dart-tf-x-str-join '[_ arr sep]))
  => #"join")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-index-of :added "4.1"}
(fact "finds string index"
  (emit-dart (dart-tf-x-str-index-of '[_ s sub]))
  => #"indexOf")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-last-index-of :added "4.1"}
(fact "finds last string index"
  (emit-dart (dart-tf-x-str-last-index-of '[_ s sub]))
  => #"lastIndexOf")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-substring :added "4.1"}
(fact "extracts substrings"
  (emit-dart (dart-tf-x-str-substring '[_ s start end]))
  => #"substring")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings"
  (emit-dart (dart-tf-x-str-to-upper '[_ s]))
  => #"toUpperCase")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings"
  (emit-dart (dart-tf-x-str-to-lower '[_ s]))
  => #"toLowerCase")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers with fixed precision"
  (emit-dart (dart-tf-x-str-to-fixed '[_ s n]))
  => #"toStringAsFixed")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-replace :added "4.1"}
(fact "replaces strings"
  (emit-dart (dart-tf-x-str-replace '[_ s pattern replacement]))
  => #"replaceAll")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-trim :added "4.1"}
(fact "trims strings"
  (emit-dart (dart-tf-x-str-trim '[_ s]))
  => #"trim")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-trim-left :added "4.1"}
(fact "trims strings on the left"
  (emit-dart (dart-tf-x-str-trim-left '[_ s]))
  => #"trimLeft")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-trim-right :added "4.1"}
(fact "trims strings on the right"
  (emit-dart (dart-tf-x-str-trim-right '[_ s]))
  => #"trimRight")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-starts-with? :added "4.1"}
(fact "checks string prefixes"
  (emit-dart (dart-tf-x-str-starts-with? '[_ s prefix]))
  => #"startsWith")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-ends-with? :added "4.1"}
(fact "checks string suffixes"
  (emit-dart (dart-tf-x-str-ends-with? '[_ s suffix]))
  => #"endsWith")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-includes? :added "4.1"}
(fact "checks string containment"
  (emit-dart (dart-tf-x-str-includes? '[_ s sub]))
  => #"contains")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lu-create :added "4.1"}
(fact "creates a lookup map literal"
  (emit-dart (dart-tf-x-lu-create '[_]))
  => #"\{\}")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lu-get :added "4.1"}
(fact "gets lookup table value")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lu-set :added "4.1"}
(fact "sets lookup table value")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lu-del :added "4.1"}
(fact "deletes lookup values"
  (emit-dart (dart-tf-x-lu-del '[_ lu obj]))
  => #"remove")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-pop :added "4.1"}
(fact "pops array items"
  (emit-dart (dart-tf-x-arr-pop '[_ arr]))
  => #"removeLast")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-slice :added "4.1"}
(fact "slices arrays")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-push-first :added "4.1"}
(fact "pushes array items to the front"
  (emit-dart (dart-tf-x-arr-push-first '[_ arr item]))
  => #"insert")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-pop-first :added "4.1"}
(fact "pops first item from array"
  (emit-dart (dart-tf-x-arr-pop-first '[_ arr]))
  => #"removeAt\(0\)")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-insert :added "4.1"}
(fact "inserts array items"
  (emit-dart (dart-tf-x-arr-insert '[_ arr idx e]))
  => #"insert")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-remove :added "4.1"}
(fact "removes array items"
  (emit-dart (dart-tf-x-arr-remove '[_ arr idx]))
  => #"removeAt")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays"
  (emit-dart (dart-tf-x-arr-sort '[_ arr key-fn comp-fn]))
  => #"sort")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-str-comp :added "4.1"}
(fact "compares strings")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-eq :added "4.1"}
(fact "iter equality stays as a returnable iife in function bodies"
  (let [out (l/emit-as :dart ['(fn [it0 it1 eq-fn]
                                 (return (x:iter-eq it0 it1 eq-fn)))])]
    [(boolean (re-find #"return\s+\(\(\)\s*\{" out))
     (boolean (re-find #"while\s*\(" out))
     (boolean (re-find #"dart_callback__" out))
     (boolean (re-find #"return while" out))])
  => [true true false false])

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-from :added "4.1"}
(fact "creates iterators"
  (emit-dart (dart-tf-x-iter-from '[_ x]))
  => #"iterator")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterators from arrays"
  (emit-dart (dart-tf-x-iter-from-arr '[_ arr]))
  => #"iterator")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-has? :added "4.1"}
(fact "checks iterator state")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-native? :added "4.1"}
(fact "checks native iterators")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-next :added "4.1"}
(fact "gets iterator values"
  (emit-dart (dart-tf-x-iter-next '[_ iter]))
  => #"current")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-iter-null :added "4.1"}
(fact "creates null iterators")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-prototype-create :added "4.1"}
(fact "creates prototypes")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-prototype-get :added "4.1"}
(fact "gets runtime prototypes"
  (emit-dart (dart-tf-x-prototype-get '[_ obj]))
  => #"obj\[\"_xt_proto\"\]")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-prototype-set :added "4.1"}
(fact "stores attached protocol metadata"
  (emit-dart (dart-tf-x-prototype-set '[_ obj prototype]))
  => #"obj\[\"_xt_proto\"\] = prototype")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-prototype-method :added "4.1"}
(fact "creates direct runtime method lookups"
  (emit-dart (dart-tf-x-prototype-method '[_ obj key]))
  => #"(?s)direct.*obj\[key\].*proto.*obj\[\"_xt_proto\"\].*proto.*\[key\]")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-prototype-tostring :added "4.1"}
(fact "stringifies prototypes"
  (emit-dart (dart-tf-x-prototype-tostring '[_ obj]))
  => #"toString")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-return-encode :added "4.1"}
(fact "encodes return values"
  (emit-dart (dart-tf-x-return-encode '[_ out id key]))
  => #"json\.encode")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-return-wrap :added "4.1"}
(fact "wraps return handlers"
  (emit-dart (dart-tf-x-return-wrap '[_ thunk encode-fn]))
  => #"json\.encode")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-return-eval :added "4.1"}
(fact "eval return handling is not supported"
  (emit-dart (dart-tf-x-return-eval '[_ s wrap-fn]))
  => #"eval not supported in Dart")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-with-delay :added "4.1"}
(fact "delay future"
  (l/emit-as :dart [(dart-tf-x-with-delay '[_ ms value])])
  => #"Future\.delayed.*Duration.*milliseconds")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-async-run :added "4.1"}
(fact "async run emits a native future start"
  (l/emit-as :dart [(dart-tf-x-async-run '[_ thunk])])
  => #"Future\.sync\(thunk\)")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise :added "4.1"}
(fact "transforms x:promise")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-all :added "4.1"}
(fact "transforms x:promise-all")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-then :added "4.1"}
(fact "transforms x:promise-then")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-catch :added "4.1"}
(fact "transforms x:promise-catch")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-finally :added "4.1"}
(fact "transforms x:promise-finally")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-native? :added "4.1"}
(fact "transforms x:promise-native?")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-socket-send :added "4.1"}
(fact "sends socket data")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-socket-close :added "4.1"}
(fact "closes sockets")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-notify-http :added "4.1"}
(fact "notifies via HTTP")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-pwd :added "4.1"}
(fact "gets working directory")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-file-slurp :added "4.1"}
(fact "file-slurp reads file content"
  (let [out (l/emit-as :dart [(dart-tf-x-file-slurp '[_ filename cb])])]
    [(boolean (re-find #"File\(filename\)" out))
     (boolean (re-find #"readAsString" out))
     (boolean (re-find #"catchError" out))])
  => [true true true])

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-file-spit :added "4.1"}
(fact "file-spit writes file content"
  (let [out (l/emit-as :dart [(dart-tf-x-file-spit '[_ filename s cb])])]
    [(boolean (re-find #"File\(filename\)" out))
     (boolean (re-find #"writeAsString" out))
     (boolean (re-find #"catchError" out))])
  => [true true true])

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-shell :added "4.1"}
(fact "shell uses Process.run"
  (let [out (l/emit-as :dart [(dart-tf-x-shell '[_ s root cb])])]
    [(boolean (re-find #"Process\.run" out))
     (boolean (re-find #"cd " out))
     (boolean (re-find #"stdout" out))
     (boolean (re-find #"catchError" out))])
  => [true true true true])

^{:refer hara.model.spec-xtalk.fn-dart/dart-call :added "4.1"}
(fact "emits dart calls")

^{:refer hara.model.spec-xtalk.fn-dart/dart-string-compare-expr :added "4.1"}
(fact "emits dart string comparisons")

^{:refer hara.model.spec-xtalk.fn-dart/dart-comparison-form :added "4.1"}
(fact "emits dart comparison forms")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lt :added "4.1"}
(fact "transforms x:lt")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-lte :added "4.1"}
(fact "transforms x:lte")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-gt :added "4.1"}
(fact "transforms x:gt")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-gte :added "4.1"}
(fact "transforms x:gte")

^{:refer hara.model.spec-xtalk.fn-dart/dart-json-compact-expr :added "4.1"}
(fact "emits dart compact json expressions")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-obj-keys :added "4.1"}
(fact "lists object keys")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-obj-vals :added "4.1"}
(fact "lists object values")

^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-obj-pairs :added "4.1"}
(fact "lists object pairs")


^{:refer hara.model.spec-xtalk.fn-dart/dart-tf-x-promise-new :added "4.0"}
(fact "creates a new promise via a Dart completer"
  (let [out (l/emit-as :dart [(dart-tf-x-promise-new '[_ thunk])])]
    [(boolean (re-find #"Completer<dynamic>\(\)" out))
     (boolean (re-find #"Function\.apply\(thunk" out))
     (boolean (re-find #"completer\.complete" out))
     (boolean (re-find #"completer\.completeError" out))
     (boolean (re-find #"completer\.future" out))])
  => [true true true true true])