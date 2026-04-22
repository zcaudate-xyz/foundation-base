(ns std.lang.model.spec-xtalk.fn-dart-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.fn-dart :refer :all]))

(defn emit-dart [form]
  (l/emit-as :dart [form]))

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
  => #"math\.Random.*nextDouble")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-type-native :added "4.1"}
(fact "gets runtime type"
  (l/emit-as :dart [(dart-tf-x-type-native '[_ obj])])
  => #"runtimeType")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-abs :added "4.1"}
(fact "absolute value"
  (l/emit-as :dart [(dart-tf-x-m-abs '[_ x])])
  => #"\.abs")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-abs :added "4.1"}
(fact "wraps negative literal receivers for no-arg methods"
  (l/emit-as :dart [(dart-tf-x-m-abs '[_ -5])
                    (dart-tf-x-m-ceil '[_ -1.2])
                    (dart-tf-x-m-floor '[_ -1.2])])
  => #"(?s)\(-5\)\.abs\(\).*\(-1\.2\)\.ceil\(\).*\(-1\.2\)\.floor\(\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-sin :added "4.1"}
(fact "sine"
  (l/emit-as :dart [(dart-tf-x-m-sin '[_ x])])
  => #"math\.sin")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-sqrt :added "4.1"}
(fact "square root"
  (l/emit-as :dart [(dart-tf-x-m-sqrt '[_ x])])
  => #"math\.sqrt")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-max :added "4.1"}
(fact "maximum"
  (l/emit-as :dart [(dart-tf-x-m-max '[_ a b])])
  => #"math\.max")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-mod :added "4.1"}
(fact "modulo"
  (l/emit-as :dart [(dart-tf-x-m-mod '[_ a b])])
  => #"%")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-pow :added "4.1"}
(fact "power"
  (l/emit-as :dart [(dart-tf-x-m-pow '[_ a b])])
  => #"math\.pow")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-pop-first :added "4.1"}
(fact "pops first item from array"
  (emit-dart (dart-tf-x-arr-pop-first '[_ arr]))
  => #"removeAt\(0\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-del-key :added "4.1"}
(fact "deletes key from map by key"
  (emit-dart (dart-tf-x-del-key '[_ obj key]))
  => #"remove\(key\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-lu-create :added "4.1"}
(fact "creates a lookup map literal"
  (emit-dart (dart-tf-x-lu-create '[_]))
  => #"\{\}")

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

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-slurp-file :added "4.1"}
(fact "slurp-file not implemented"
  (l/emit-as :dart [(dart-tf-x-slurp-file '[_ filename opts cb])])
  => #"slurp-file not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-spit-file :added "4.1"}
(fact "spit-file not implemented"
  (l/emit-as :dart [(dart-tf-x-spit-file '[_ filename s opts cb])])
  => #"spit-file not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-debug-client-basic :added "4.1"}
(fact "client basic not implemented"
  (l/emit-as :dart [(dart-tf-x-debug-client-basic '[_ host port opts cb])])
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

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-ceil :added "4.1"}
(fact "ceiling"
  (emit-dart (dart-tf-x-m-ceil '[_ x]))
  => #"\.ceil")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-floor :added "4.1"}
(fact "floor"
  (emit-dart (dart-tf-x-m-floor '[_ x]))
  => #"\.floor")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-cos :added "4.1"}
(fact "cosine"
  (emit-dart (dart-tf-x-m-cos '[_ x]))
  => #"math\.cos")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-tan :added "4.1"}
(fact "tangent"
  (emit-dart (dart-tf-x-m-tan '[_ x]))
  => #"math\.tan")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-asin :added "4.1"}
(fact "arc sine"
  (emit-dart (dart-tf-x-m-asin '[_ x]))
  => #"math\.asin")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-acos :added "4.1"}
(fact "arc cosine"
  (emit-dart (dart-tf-x-m-acos '[_ x]))
  => #"math\.acos")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-atan :added "4.1"}
(fact "arc tangent"
  (emit-dart (dart-tf-x-m-atan '[_ x]))
  => #"math\.atan")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-exp :added "4.1"}
(fact "exponential"
  (emit-dart (dart-tf-x-m-exp '[_ x]))
  => #"math\.exp")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-loge :added "4.1"}
(fact "natural logarithm"
  (emit-dart (dart-tf-x-m-loge '[_ x]))
  => #"math\.log")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-log10 :added "4.1"}
(fact "base-10 logarithm"
  (emit-dart (dart-tf-x-m-log10 '[_ x]))
  => #"math\.log10")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-min :added "4.1"}
(fact "minimum"
  (emit-dart (dart-tf-x-m-min '[_ a b]))
  => #"math\.min")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-quot :added "4.1"}
(fact "integer quotient"
  (emit-dart (dart-tf-x-m-quot '[_ a b]))
  => #"~/")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-cosh :added "4.1"}
(fact "hyperbolic cosine"
  (emit-dart (dart-tf-x-m-cosh '[_ x]))
  => #"cosh")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-sinh :added "4.1"}
(fact "hyperbolic sine"
  (emit-dart (dart-tf-x-m-sinh '[_ x]))
  => #"sinh")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-m-tanh :added "4.1"}
(fact "hyperbolic tangent"
  (emit-dart (dart-tf-x-m-tanh '[_ x]))
  => #"tanh")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-to-string :added "4.1"}
(fact "converts to string"
  (emit-dart (dart-tf-x-to-string '[_ x]))
  => #"toString")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-to-number :added "4.1"}
(fact "converts to number"
  (emit-dart (dart-tf-x-to-number '[_ x]))
  => #"num\.parse")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-number? :added "4.1"}
(fact "checks if number"
  (emit-dart (dart-tf-x-is-number? '[_ x]))
  => #"is\(x,num\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-integer? :added "4.1"}
(fact "checks if integer"
  (emit-dart (dart-tf-x-is-integer? '[_ x]))
  => #"is\(x,int\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-boolean? :added "4.1"}
(fact "checks if boolean"
  (emit-dart (dart-tf-x-is-boolean? '[_ x]))
  => #"is\(x,bool\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-function? :added "4.1"}
(fact "checks if function"
  (emit-dart (dart-tf-x-is-function? '[_ x]))
  => #"is\(x,Function\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-object? :added "4.1"}
(fact "checks if object"
  (emit-dart (dart-tf-x-is-object? '[_ x]))
  => #"Object.*List.*Function")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-is-array? :added "4.1"}
(fact "checks if array"
  (emit-dart (dart-tf-x-is-array? '[_ x]))
  => #"is\(x,List\)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-char :added "4.1"}
(fact "gets string character"
  (dart-tf-x-str-char '[_ s i])
  => '(. s ([] i)))

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-split :added "4.1"}
(fact "splits strings"
  (emit-dart (dart-tf-x-str-split '[_ s sep]))
  => #"split")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (emit-dart (dart-tf-x-str-join '[_ arr sep]))
  => #"join")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-index-of :added "4.1"}
(fact "finds string index"
  (emit-dart (dart-tf-x-str-index-of '[_ s sub]))
  => #"indexOf")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-last-index-of :added "4.1"}
(fact "finds last string index"
  (emit-dart (dart-tf-x-str-last-index-of '[_ s sub]))
  => #"lastIndexOf")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-substring :added "4.1"}
(fact "extracts substrings"
  (emit-dart (dart-tf-x-str-substring '[_ s start end]))
  => #"substring")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings"
  (emit-dart (dart-tf-x-str-to-upper '[_ s]))
  => #"toUpperCase")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings"
  (emit-dart (dart-tf-x-str-to-lower '[_ s]))
  => #"toLowerCase")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers with fixed precision"
  (emit-dart (dart-tf-x-str-to-fixed '[_ s n]))
  => #"toStringAsFixed")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-replace :added "4.1"}
(fact "replaces strings"
  (emit-dart (dart-tf-x-str-replace '[_ s pattern replacement]))
  => #"replaceAll")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-trim :added "4.1"}
(fact "trims strings"
  (emit-dart (dart-tf-x-str-trim '[_ s]))
  => #"trim")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-trim-left :added "4.1"}
(fact "trims strings on the left"
  (emit-dart (dart-tf-x-str-trim-left '[_ s]))
  => #"trimLeft")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-trim-right :added "4.1"}
(fact "trims strings on the right"
  (emit-dart (dart-tf-x-str-trim-right '[_ s]))
  => #"trimRight")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-starts-with? :added "4.1"}
(fact "checks string prefixes"
  (emit-dart (dart-tf-x-str-starts-with? '[_ s prefix]))
  => #"startsWith")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-ends-with? :added "4.1"}
(fact "checks string suffixes"
  (emit-dart (dart-tf-x-str-ends-with? '[_ s suffix]))
  => #"endsWith")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-str-includes? :added "4.1"}
(fact "checks string containment"
  (emit-dart (dart-tf-x-str-includes? '[_ s sub]))
  => #"contains")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-lu-get :added "4.1"}
(fact "gets lookup values"
  (emit-dart (dart-tf-x-lu-get '[_ lu obj]))
  => "[](lu,obj)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-lu-set :added "4.1"}
(fact "sets lookup values"
  (emit-dart (dart-tf-x-lu-set '[_ lu obj gid]))
  => "[](lu,obj) = gid")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-lu-del :added "4.1"}
(fact "deletes lookup values"
  (emit-dart (dart-tf-x-lu-del '[_ lu obj]))
  => #"remove")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-pop :added "4.1"}
(fact "pops array items"
  (emit-dart (dart-tf-x-arr-pop '[_ arr]))
  => #"removeLast")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-push-first :added "4.1"}
(fact "pushes array items to the front"
  (emit-dart (dart-tf-x-arr-push-first '[_ arr item]))
  => #"insert")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-pop-first :added "4.1"}
(fact "pops first array items"
  (dart-tf-x-arr-pop-first '[_ arr])
  => '(. arr removeAt 0))

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-insert :added "4.1"}
(fact "inserts array items"
  (emit-dart (dart-tf-x-arr-insert '[_ arr idx e]))
  => #"insert")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-remove :added "4.1"}
(fact "removes array items"
  (emit-dart (dart-tf-x-arr-remove '[_ arr idx]))
  => #"removeAt")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays"
  (emit-dart (dart-tf-x-arr-sort '[_ arr key-fn comp-fn]))
  => #"sort")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-arr-str-comp :added "4.1"}
(fact "compares array items by string value"
  (emit-dart (dart-tf-x-arr-str-comp '[_ a b]))
  => #"compareTo")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache :added "4.1"}
(fact "creates caches"
  (emit-dart (dart-tf-x-cache '[_ name]))
  => #"new Map")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-list :added "4.1"}
(fact "lists cache keys"
  (emit-dart (dart-tf-x-cache-list '[_ cache]))
  => #"keys.*toList")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-flush :added "4.1"}
(fact "flushes caches"
  (emit-dart (dart-tf-x-cache-flush '[_ cache]))
  => #"clear")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-get :added "4.1"}
(fact "gets cached values"
  (emit-dart (dart-tf-x-cache-get '[_ cache key]))
  => "[](cache,key)")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-set :added "4.1"}
(fact "sets cached values"
  (emit-dart (dart-tf-x-cache-set '[_ cache key val]))
  => "[](cache,key) = val")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-del :added "4.1"}
(fact "deletes cached values"
  (emit-dart (dart-tf-x-cache-del '[_ cache key]))
  => #"remove")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-cache-incr :added "4.1"}
(fact "increments cached values"
  (emit-dart (dart-tf-x-cache-incr '[_ cache key num]))
  => #"(?s)int\.parse.*curr")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-eq :added "4.1"}
(fact "compares iterators"
  (emit-dart (dart-tf-x-iter-eq '[_ a b]))
  => #"==")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-from :added "4.1"}
(fact "creates iterators"
  (emit-dart (dart-tf-x-iter-from '[_ x]))
  => #"iterator")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterators from arrays"
  (emit-dart (dart-tf-x-iter-from-arr '[_ arr]))
  => #"iterator")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects"
  (emit-dart (dart-tf-x-iter-from-obj '[_ obj]))
  => #"entries.*iterator")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-has? :added "4.1"}
(fact "checks iterator availability"
  (emit-dart (dart-tf-x-iter-has? '[_ iter]))
  => #"moveNext")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-native? :added "4.1"}
(fact "marks iterators as native"
  (emit-dart (dart-tf-x-iter-native? '[_ iter]))
  => #"true")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-next :added "4.1"}
(fact "gets iterator values"
  (emit-dart (dart-tf-x-iter-next '[_ iter]))
  => #"current")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-iter-null :added "4.1"}
(fact "returns null iterators"
  (emit-dart (dart-tf-x-iter-null '[_]))
  => #"null")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-debug-client-ws :added "4.1"}
(fact "websocket clients are not implemented"
  (emit-dart (dart-tf-x-debug-client-ws '[_ host port opts cb]))
  => #"WebSocket client not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-server-basic :added "4.1"}
(fact "basic servers are not implemented"
  (emit-dart (dart-tf-x-server-basic '[_ port opts cb]))
  => #"Server not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-server-ws :added "4.1"}
(fact "websocket servers are not implemented"
  (emit-dart (dart-tf-x-server-ws '[_ port opts cb]))
  => #"WebSocket server not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-ws-connect :added "4.1"}
(fact "websocket connect is not implemented"
  (emit-dart (dart-tf-x-ws-connect '[_ host port opts cb]))
  => #"WebSocket connect not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-ws-send :added "4.1"}
(fact "websocket send is not implemented"
  (emit-dart (dart-tf-x-ws-send '[_ conn s]))
  => #"WebSocket send not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-ws-close :added "4.1"}
(fact "websocket close is not implemented"
  (emit-dart (dart-tf-x-ws-close '[_ conn]))
  => #"WebSocket close not implemented")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-proto-get :added "4.1"}
(fact "gets runtime prototypes"
  (emit-dart (dart-tf-x-proto-get '[_ obj]))
  => #"runtimeType")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-proto-set :added "4.1"}
(fact "setting prototypes is not supported"
  (emit-dart (dart-tf-x-proto-set '[_ obj prototype]))
  => #"Proto set not supported in Dart")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-proto-tostring :added "4.1"}
(fact "stringifies prototypes"
  (emit-dart (dart-tf-x-proto-tostring '[_ obj]))
  => #"toString")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-return-encode :added "4.1"}
(fact "encodes return values"
  (emit-dart (dart-tf-x-return-encode '[_ out id key]))
  => #"json\.encode")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-return-wrap :added "4.1"}
(fact "wraps return handlers"
  (emit-dart (dart-tf-x-return-wrap '[_ thunk encode-fn]))
  => #"json\.encode")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-return-eval :added "4.1"}
(fact "eval return handling is not supported"
  (emit-dart (dart-tf-x-return-eval '[_ s wrap-fn]))
  => #"eval not supported in Dart")

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-thread-join :added "4.1"}
(fact "thread join is not implemented"
  (emit-dart (dart-tf-x-thread-join '[_ thread]))
  => #"Thread join not implemented in Dart")
