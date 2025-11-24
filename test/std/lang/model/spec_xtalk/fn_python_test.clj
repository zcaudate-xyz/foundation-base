(ns std.lang.model.spec-xtalk.fn-python-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-python :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-del :added "4.0"}
(fact "deletes object"
  (l/emit-as :python [(python-tf-x-del '[_ obj])])
  => #"del")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-cat :added "4.0"}
(fact "concatenates"
  (l/emit-as :python [(python-tf-x-cat '[_ "a" "b"])])
  => #"\+")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-len :added "4.0"}
(fact "gets length"
  (l/emit-as :python [(python-tf-x-len '[_ arr])])
  => #"len")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-get-key :added "4.0"}
(fact "gets key"
  (l/emit-as :python [(python-tf-x-get-key '[_ obj key default])])
  => #"or")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-err :added "4.0"}
(fact "raises error"
  (l/emit-as :python [(python-tf-x-err '[_ "msg"])])
  => #"raise")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-eval :added "4.0"}
(fact "evals"
  (l/emit-as :python [(python-tf-x-eval '[_ "1 + 1"])])
  => #"eval")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-apply :added "4.0"}
(fact "applies"
  (l/emit-as :python [(python-tf-x-apply '[_ f args])])
  => #"\*")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-random :added "4.0"}
(fact "random"
  (l/emit-as :python [(python-tf-x-random '[_])])
  => #"random")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-print :added "4.0"}
(fact "prints"
  (l/emit-as :python [(python-tf-x-print '[_ "hello"])])
  => #"print")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :python [(python-tf-x-shell '[_ "ls" cm])])
  => #"os")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-type-native :added "4.0"}
(fact "type native"
  (l/emit-as :python [(python-tf-x-type-native '[_ obj])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-abs :added "4.0"}
(fact "math abs"
  (l/emit-as :python [(python-tf-x-m-abs '[_ 1])])
  => #"abs")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-acos :added "4.0"}
(fact "math acos"
  (l/emit-as :python [(python-tf-x-m-acos '[_ 1])])
  => #"acos")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-asin :added "4.0"}
(fact "math asin"
  (l/emit-as :python [(python-tf-x-m-asin '[_ 1])])
  => #"asin")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-atan :added "4.0"}
(fact "math atan"
  (l/emit-as :python [(python-tf-x-m-atan '[_ 1])])
  => #"atan")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-ceil :added "4.0"}
(fact "math ceil"
  (l/emit-as :python [(python-tf-x-m-ceil '[_ 1])])
  => #"ceil")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-cos :added "4.0"}
(fact "math cos"
  (l/emit-as :python [(python-tf-x-m-cos '[_ 1])])
  => #"cos")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-cosh :added "4.0"}
(fact "math cosh"
  (l/emit-as :python [(python-tf-x-m-cosh '[_ 1])])
  => #"cosh")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-exp :added "4.0"}
(fact "math exp"
  (l/emit-as :python [(python-tf-x-m-exp '[_ 1])])
  => #"exp")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-floor :added "4.0"}
(fact "math floor"
  (l/emit-as :python [(python-tf-x-m-floor '[_ 1])])
  => #"floor")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-loge :added "4.0"}
(fact "math log"
  (l/emit-as :python [(python-tf-x-m-loge '[_ 1])])
  => #"log")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-log10 :added "4.0"}
(fact "math log10"
  (l/emit-as :python [(python-tf-x-m-log10 '[_ 1])])
  => #"log10")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-max :added "4.0"}
(fact "math max"
  (l/emit-as :python [(python-tf-x-m-max '[_ 1 2])])
  => #"max")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-min :added "4.0"}
(fact "math min"
  (l/emit-as :python [(python-tf-x-m-min '[_ 1 2])])
  => #"min")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-mod :added "4.0"}
(fact "math mod"
  (l/emit-as :python [(python-tf-x-m-mod '[_ 1 2])])
  => #"%")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-quot :added "4.0"}
(fact "math quot"
  (l/emit-as :python [(python-tf-x-m-quot '[_ 1 2])])
  => #"//")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-pow :added "4.0"}
(fact "math pow"
  (l/emit-as :python [(python-tf-x-m-pow '[_ 1 2])])
  => #"\*\*")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-sin :added "4.0"}
(fact "math sin"
  (l/emit-as :python [(python-tf-x-m-sin '[_ 1])])
  => #"sin")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-sinh :added "4.0"}
(fact "math sinh"
  (l/emit-as :python [(python-tf-x-m-sinh '[_ 1])])
  => #"sinh")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-sqrt :added "4.0"}
(fact "math sqrt"
  (l/emit-as :python [(python-tf-x-m-sqrt '[_ 1])])
  => #"sqrt")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-tan :added "4.0"}
(fact "math tan"
  (l/emit-as :python [(python-tf-x-m-tan '[_ 1])])
  => #"tan")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-m-tanh :added "4.0"}
(fact "math tanh"
  (l/emit-as :python [(python-tf-x-m-tanh '[_ 1])])
  => #"tanh")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-to-string :added "4.0"}
(fact "to string"
  (l/emit-as :python [(python-tf-x-to-string '[_ x])])
  => #"str")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-to-number :added "4.0"}
(fact "to number"
  (l/emit-as :python [(python-tf-x-to-number '[_ x])])
  => #"float")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-string? :added "4.0"}
(fact "is string"
  (l/emit-as :python [(python-tf-x-is-string? '[_ x])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-number? :added "4.0"}
(fact "is number"
  (l/emit-as :python [(python-tf-x-is-number? '[_ x])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-integer? :added "4.0"}
(fact "is integer"
  (l/emit-as :python [(python-tf-x-is-integer? '[_ x])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-boolean? :added "4.0"}
(fact "is boolean"
  (l/emit-as :python [(python-tf-x-is-boolean? '[_ x])])
  => #"bool")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-function? :added "4.0"}
(fact "is function"
  (l/emit-as :python [(python-tf-x-is-function? '[_ x])])
  => #"callable")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-object? :added "4.0"}
(fact "is object"
  (l/emit-as :python [(python-tf-x-is-object? '[_ x])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-is-array? :added "4.0"}
(fact "is array"
  (l/emit-as :python [(python-tf-x-is-array? '[_ x])])
  => #"isinstance")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-lu-create :added "4.0"}
(fact "lu create"
  (l/emit-as :python [(python-tf-x-lu-create '[_])])
  => #"WeakKeyDictionary")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-lu-eq :added "4.0"}
(fact "lu eq"
  (l/emit-as :python [(python-tf-x-lu-eq '[_ a b])])
  => #"id")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-lu-get :added "4.0"}
(fact "lu get"
  (l/emit-as :python [(python-tf-x-lu-get '[_ lu k])])
  => #"get")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-lu-set :added "4.0"}
(fact "lu set"
  (l/emit-as :python [(python-tf-x-lu-set '[_ lu k v])])
  => #"id")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-lu-del :added "4.0"}
(fact "lu del"
  (l/emit-as :python [(python-tf-x-lu-del '[_ lu k])])
  => #"del")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-obj-keys :added "4.0"}
(fact "obj keys"
  (l/emit-as :python [(python-tf-x-obj-keys '[_ obj])])
  => #"keys")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-obj-vals :added "4.0"}
(fact "obj vals"
  (l/emit-as :python [(python-tf-x-obj-vals '[_ obj])])
  => #"values")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-obj-pairs :added "4.0"}
(fact "obj pairs"
  (l/emit-as :python [(python-tf-x-obj-pairs '[_ obj])])
  => #"items")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-obj-clone :added "4.0"}
(fact "obj clone"
  (l/emit-as :python [(python-tf-x-obj-clone '[_ obj])])
  => #"copy")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-clone :added "4.0"}
(fact "arr clone"
  (l/emit-as :python [(python-tf-x-arr-clone '[_ arr])])
  => #":")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-slice :added "4.0"}
(fact "arr slice"
  (l/emit-as :python [(python-tf-x-arr-slice '[_ arr 0 1])])
  => #":")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-push :added "4.0"}
(fact "arr push"
  (l/emit-as :python [(python-tf-x-arr-push '[_ arr 1])])
  => #"append")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-pop :added "4.0"}
(fact "arr pop"
  (l/emit-as :python [(python-tf-x-arr-pop '[_ arr])])
  => #"pop")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-reverse :added "4.0"}
(fact "arr reverse"
  (l/emit-as :python [(python-tf-x-arr-reverse '[_ arr])])
  => #"reversed")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-push-first :added "4.0"}
(fact "arr push first"
  (l/emit-as :python [(python-tf-x-arr-push-first '[_ arr 1])])
  => #"insert")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-pop-first :added "4.0"}
(fact "arr pop first"
  (l/emit-as :python [(python-tf-x-arr-pop-first '[_ arr])])
  => #"pop")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-insert :added "4.0"}
(fact "arr insert"
  (l/emit-as :python [(python-tf-x-arr-insert '[_ arr 0 1])])
  => #"insert")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-sort :added "4.0"}
(fact "arr sort"
  (l/emit-as :python [(python-tf-x-arr-sort '[_ arr k c])])
  => #"sort")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-arr-str-comp :added "4.0"}
(fact "arr str comp"
  (l/emit-as :python [(python-tf-x-arr-str-comp '[_ a b])])
  => #"<")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-char :added "4.0"}
(fact "str char"
  (l/emit-as :python [(python-tf-x-str-char '[_ s 0])])
  => #"ord")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-split :added "4.0"}
(fact "str split"
  (l/emit-as :python [(python-tf-x-str-split '[_ s " "])])
  => #"split")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-join :added "4.0"}
(fact "str join"
  (l/emit-as :python [(python-tf-x-str-join '[_ s arr])])
  => #"join")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-index-of :added "4.0"}
(fact "str index of"
  (l/emit-as :python [(python-tf-x-str-index-of '[_ s "a"])])
  => #"find")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-substring :added "4.0"}
(fact "str substring"
  (l/emit-as :python [(python-tf-x-str-substring '[_ s 0 1])])
  => #":")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-to-upper :added "4.0"}
(fact "str to upper"
  (l/emit-as :python [(python-tf-x-str-to-upper '[_ s])])
  => #"upper")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-to-lower :added "4.0"}
(fact "str to lower"
  (l/emit-as :python [(python-tf-x-str-to-lower '[_ s])])
  => #"lower")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-str-replace :added "4.0"}
(fact "str replace"
  (l/emit-as :python [(python-tf-x-str-replace '[_ s "a" "b"])])
  => #"replace")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-json-encode :added "4.0"}
(fact "json encode"
  (l/emit-as :python [(python-tf-x-json-encode '[_ obj])])
  => #"json")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-json-decode :added "4.0"}
(fact "json decode"
  (l/emit-as :python [(python-tf-x-json-decode '[_ s])])
  => #"json")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-return-encode :added "4.0"}
(fact "return encode"
  (l/emit-as :python [(python-tf-x-return-encode '[_ out id key])])
  => #"json.dumps")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-return-wrap :added "4.0"}
(fact "return wrap"
  (l/emit-as :python [(python-tf-x-return-wrap '[_ f encode-fn])])
  => #"try")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-return-eval :added "4.0"}
(fact "return eval"
  (l/emit-as :python [(python-tf-x-return-eval '[_ s wrap-fn])])
  => #"exec")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-socket-connect :added "4.0"}
(fact "socket connect"
  (l/emit-as :python [(python-tf-x-socket-connect '[_ host port opts])])
  => #"socket")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-socket-send :added "4.0"}
(fact "socket send"
  (l/emit-as :python [(python-tf-x-socket-send '[_ conn s])])
  => #"sendall")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-socket-close :added "4.0"}
(fact "socket close"
  (l/emit-as :python [(python-tf-x-socket-close '[_ conn])])
  => #"close")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :python [(python-tf-x-iter-from-obj '[_ obj])])
  => #"iter")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :python [(python-tf-x-iter-from-arr '[_ arr])])
  => #"iter")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :python [(python-tf-x-iter-from '[_ obj])])
  => #"iter")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :python [(python-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"next")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :python [(python-tf-x-iter-next '[_ it])])
  => #"next")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :python [(python-tf-x-iter-has? '[_ obj])])
  => #"hasattr")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :python [(python-tf-x-iter-native? '[_ it])])
  => #"hasattr")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-thread-spawn :added "4.0"}
(fact "thread spawn"
  (l/emit-as :python [(python-tf-x-thread-spawn '[_ thunk])])
  => #"threading")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-thread-join :added "4.0"}
(fact "thread join"
  (l/emit-as :python [(python-tf-x-thread-join '[_ thread])])
  => #"x:error")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :python [(python-tf-x-with-delay '[_ thunk ms])])
  => #"sleep")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-slurp :added "4.0"}
(fact "slurp"
  (comment (l/emit-as :python [(python-tf-x-slurp '[_ filename])])
           => nil?))

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-spit :added "4.0"}
(fact "spit"
  (comment (l/emit-as :python [(python-tf-x-spit '[_ filename s])])
           => nil?))

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-b64-encode :added "4.0"}
(fact "b64 encode"
  (l/emit-as :python [(python-tf-x-b64-encode '[_ obj])])
  => #"base64")

^{:refer std.lang.model.spec-xtalk.fn-python/python-tf-x-b64-decode :added "4.0"}
(fact "b64 decode"
  (l/emit-as :python [(python-tf-x-b64-decode '[_ s])])
  => #"base64")
