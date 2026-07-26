tahto/model/spec_xtalk/fn_python_test.clj:1:(ns tahto.model.spec-xtalk.fn-python-test
  (:require [tahto.core :as l]
            [xt.lang.common-promise]
tahto/model/spec_xtalk/fn_python_test.clj:4:            [tahto.model.spec-xtalk.fn-python :refer :all])
  (:use code.test))

tahto/model/spec_xtalk/fn_python_test.clj:7:^{:refer tahto.model.spec-xtalk.fn-python/+python-promise+ :added "4.1"}
(fact "async run emits a host thread start"
  (l/emit-as :python [(python-tf-x-async-run '[_ thunk])])
  => #"(?s)threading.*Thread.*target")

tahto/model/spec_xtalk/fn_python_test.clj:12:^{:refer tahto.model.spec-xtalk.fn-python/+python-iter :added "4.1"}
(fact "iter null"
  (l/emit-as :python [(:default (:x-iter-null +python-iter+))])
  => #"iter")

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "normalizes thrown values and catch bindings"
  (let [out (l/emit-as :python ['(do:>
                                  (try
                                    (throw "boom")
                                    (catch err
                                      (return err))))])]
    [(boolean (re-find #"(?s)raise .*Exception" out))
     (boolean (re-find #"except Exception as [^:]+:" out))])
  => [true true])

tahto/model/spec_xtalk/fn_python_test.clj:28:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-del :added "4.0"}
(fact "deletes object"
  (l/emit-as :python [(python-tf-x-del '[_ obj])])
  => #"del")

tahto/model/spec_xtalk/fn_python_test.clj:33:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-cat :added "4.0"}
(fact "concatenates"
  (l/emit-as :python [(python-tf-x-cat '[_ "a" "b"])])
  => #"\+")

tahto/model/spec_xtalk/fn_python_test.clj:38:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-len :added "4.0"}
(fact "gets length"
  (l/emit-as :python [(python-tf-x-len '[_ arr])])
  => #"len")

tahto/model/spec_xtalk/fn_python_test.clj:43:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-get-key :added "4.0"}
(fact "gets key"
  (python-tf-x-get-key '[_ obj key default])
  => '(or (. obj (get key)) default))

tahto/model/spec_xtalk/fn_python_test.clj:48:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-has-key? :added "4.1"}
(fact "has key"
  (python-tf-x-has-key? '[_ obj key nil])
  => '(not= nil (. obj (get key))))

tahto/model/spec_xtalk/fn_python_test.clj:53:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-err :added "4.0"}
(fact "raises error"
  (python-tf-x-err '[_ "msg"])
  => '(throw (Exception "msg")))

tahto/model/spec_xtalk/fn_python_test.clj:58:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/spec_xtalk/fn_python_test.clj:61:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/spec_xtalk/fn_python_test.clj:64:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/spec_xtalk/fn_python_test.clj:67:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/spec_xtalk/fn_python_test.clj:70:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-eval :added "4.0"}
(fact "evals"
  (l/emit-as :python [(python-tf-x-eval '[_ "1 + 1"])])
  => #"eval")

tahto/model/spec_xtalk/fn_python_test.clj:75:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-apply :added "4.0"}
(fact "applies"
  (l/emit-as :python [(python-tf-x-apply '[_ f args])])
  => #"\*")

tahto/model/spec_xtalk/fn_python_test.clj:80:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-random :added "4.0"}
(fact "random"
  (l/emit-as :python [(python-tf-x-random '[_])])
  => #"random")

tahto/model/spec_xtalk/fn_python_test.clj:85:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-print :added "4.0"}
(fact "prints"
  (l/emit-as :python [(python-tf-x-print '[_ "hello"])])
  => #"print")

tahto/model/spec_xtalk/fn_python_test.clj:90:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-type-native :added "4.0"}
(fact "type native"
  (l/emit-as :python [(python-tf-x-type-native '[_ obj])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:95:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-abs :added "4.0"}
(fact "math abs"
  (l/emit-as :python [(python-tf-x-m-abs '[_ 1])])
  => #"abs")

tahto/model/spec_xtalk/fn_python_test.clj:100:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-acos :added "4.0"}
(fact "math acos"
  (l/emit-as :python [(python-tf-x-m-acos '[_ 1])])
  => #"acos")

tahto/model/spec_xtalk/fn_python_test.clj:105:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-asin :added "4.0"}
(fact "math asin"
  (l/emit-as :python [(python-tf-x-m-asin '[_ 1])])
  => #"asin")

tahto/model/spec_xtalk/fn_python_test.clj:110:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-atan :added "4.0"}
(fact "math atan"
  (l/emit-as :python [(python-tf-x-m-atan '[_ 1])])
  => #"atan")

tahto/model/spec_xtalk/fn_python_test.clj:115:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-ceil :added "4.0"}
(fact "math ceil"
  (l/emit-as :python [(python-tf-x-m-ceil '[_ 1])])
  => #"ceil")

tahto/model/spec_xtalk/fn_python_test.clj:120:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-cos :added "4.0"}
(fact "math cos"
  (l/emit-as :python [(python-tf-x-m-cos '[_ 1])])
  => #"cos")

tahto/model/spec_xtalk/fn_python_test.clj:125:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-cosh :added "4.0"}
(fact "math cosh"
  (l/emit-as :python [(python-tf-x-m-cosh '[_ 1])])
  => #"cosh")

tahto/model/spec_xtalk/fn_python_test.clj:130:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-exp :added "4.0"}
(fact "math exp"
  (l/emit-as :python [(python-tf-x-m-exp '[_ 1])])
  => #"exp")

tahto/model/spec_xtalk/fn_python_test.clj:135:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-floor :added "4.0"}
(fact "math floor"
  (l/emit-as :python [(python-tf-x-m-floor '[_ 1])])
  => #"floor")

tahto/model/spec_xtalk/fn_python_test.clj:140:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-loge :added "4.0"}
(fact "math log"
  (l/emit-as :python [(python-tf-x-m-loge '[_ 1])])
  => #"log")

tahto/model/spec_xtalk/fn_python_test.clj:145:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-log10 :added "4.0"}
(fact "math log10"
  (l/emit-as :python [(python-tf-x-m-log10 '[_ 1])])
  => #"log10")

tahto/model/spec_xtalk/fn_python_test.clj:150:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-max :added "4.0"}
(fact "math max"
  (l/emit-as :python [(python-tf-x-m-max '[_ 1 2])])
  => #"max")

tahto/model/spec_xtalk/fn_python_test.clj:155:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-min :added "4.0"}
(fact "math min"
  (l/emit-as :python [(python-tf-x-m-min '[_ 1 2])])
  => #"min")

tahto/model/spec_xtalk/fn_python_test.clj:160:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-mod :added "4.0"}
(fact "math mod"
  (l/emit-as :python [(python-tf-x-m-mod '[_ 1 2])])
  => #"%")

tahto/model/spec_xtalk/fn_python_test.clj:165:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-quot :added "4.0"}
(fact "math quot"
  (l/emit-as :python [(python-tf-x-m-quot '[_ 1 2])])
  => #"//")

tahto/model/spec_xtalk/fn_python_test.clj:170:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-pow :added "4.0"}
(fact "math pow"
  (python-tf-x-m-pow '[_ 1 2])
  => '(pow 1 2))

tahto/model/spec_xtalk/fn_python_test.clj:175:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-sin :added "4.0"}
(fact "math sin"
  (l/emit-as :python [(python-tf-x-m-sin '[_ 1])])
  => #"sin")

tahto/model/spec_xtalk/fn_python_test.clj:180:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-sinh :added "4.0"}
(fact "math sinh"
  (l/emit-as :python [(python-tf-x-m-sinh '[_ 1])])
  => #"sinh")

tahto/model/spec_xtalk/fn_python_test.clj:185:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-sqrt :added "4.0"}
(fact "math sqrt"
  (l/emit-as :python [(python-tf-x-m-sqrt '[_ 1])])
  => #"sqrt")

tahto/model/spec_xtalk/fn_python_test.clj:190:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-tan :added "4.0"}
(fact "math tan"
  (l/emit-as :python [(python-tf-x-m-tan '[_ 1])])
  => #"tan")

tahto/model/spec_xtalk/fn_python_test.clj:195:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-m-tanh :added "4.0"}
(fact "math tanh"
  (l/emit-as :python [(python-tf-x-m-tanh '[_ 1])])
  => #"tanh")

tahto/model/spec_xtalk/fn_python_test.clj:200:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-to-string :added "4.0"}
(fact "to string"
  (l/emit-as :python [(python-tf-x-to-string '[_ x])])
  => #"str")

tahto/model/spec_xtalk/fn_python_test.clj:205:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-to-number :added "4.0"}
(fact "to number"
  (l/emit-as :python [(python-tf-x-to-number '[_ x])])
  => #"float")

tahto/model/spec_xtalk/fn_python_test.clj:210:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-string? :added "4.0"}
(fact "is string"
  (l/emit-as :python [(python-tf-x-is-string? '[_ x])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:215:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-number? :added "4.0"}
(fact "is number"
  (l/emit-as :python [(python-tf-x-is-number? '[_ x])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:220:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-integer? :added "4.0"}
(fact "is integer"
  (l/emit-as :python [(python-tf-x-is-integer? '[_ x])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:225:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-boolean? :added "4.0"}
(fact "is boolean"
  (l/emit-as :python [(python-tf-x-is-boolean? '[_ x])])
  => #"bool")

tahto/model/spec_xtalk/fn_python_test.clj:230:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-function? :added "4.0"}
(fact "is function"
  (l/emit-as :python [(python-tf-x-is-function? '[_ x])])
  => #"callable")

tahto/model/spec_xtalk/fn_python_test.clj:235:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-object? :added "4.0"}
(fact "is object"
  (l/emit-as :python [(python-tf-x-is-object? '[_ x])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:240:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-is-array? :added "4.0"}
(fact "is array"
  (l/emit-as :python [(python-tf-x-is-array? '[_ x])])
  => #"isinstance")

tahto/model/spec_xtalk/fn_python_test.clj:245:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-lu-create :added "4.0"}
(fact "lu create"
  (l/emit-as :python [(python-tf-x-lu-create '[_])])
  => #"\{\}")

tahto/model/spec_xtalk/fn_python_test.clj:250:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-lu-eq :added "4.0"}
(fact "lu eq"
  (l/emit-as :python [(python-tf-x-lu-eq '[_ a b])])
  => #"id")

tahto/model/spec_xtalk/fn_python_test.clj:255:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-lu-get :added "4.0"}
(fact "lu get"
  (l/emit-as :python [(python-tf-x-lu-get '[_ lu k])])
  => #"get")

tahto/model/spec_xtalk/fn_python_test.clj:260:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-lu-set :added "4.0"}
(fact "lu set"
  (l/emit-as :python [(python-tf-x-lu-set '[_ lu k v])])
  => #"id")

tahto/model/spec_xtalk/fn_python_test.clj:265:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-lu-del :added "4.0"}
(fact "lu del"
  (l/emit-as :python [(python-tf-x-lu-del '[_ lu k])])
  => #"del")

tahto/model/spec_xtalk/fn_python_test.clj:270:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-obj-keys :added "4.0"}
(fact "obj keys"
  (l/emit-as :python [(python-tf-x-obj-keys '[_ obj])])
  => #"keys")

tahto/model/spec_xtalk/fn_python_test.clj:275:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-obj-vals :added "4.0"}
(fact "obj vals"
  (l/emit-as :python [(python-tf-x-obj-vals '[_ obj])])
  => #"values")

tahto/model/spec_xtalk/fn_python_test.clj:280:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-obj-pairs :added "4.0"}
(fact "obj pairs"
  (l/emit-as :python [(python-tf-x-obj-pairs '[_ obj])])
  => #"items")

tahto/model/spec_xtalk/fn_python_test.clj:285:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-obj-clone :added "4.0"}
(fact "obj clone"
  (l/emit-as :python [(python-tf-x-obj-clone '[_ obj])])
  => #"copy")

tahto/model/spec_xtalk/fn_python_test.clj:290:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-clone :added "4.0"}
(fact "arr clone"
  (l/emit-as :python [(python-tf-x-arr-clone '[_ arr])])
  => #":")

tahto/model/spec_xtalk/fn_python_test.clj:295:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-slice :added "4.0"}
(fact "arr slice"
  (l/emit-as :python [(python-tf-x-arr-slice '[_ arr 0 1])])
  => #":")

tahto/model/spec_xtalk/fn_python_test.clj:300:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-push :added "4.0"}
(fact "arr push"
  (l/emit-as :python [(python-tf-x-arr-push '[_ arr 1])])
  => #"append")

tahto/model/spec_xtalk/fn_python_test.clj:305:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-pop :added "4.0"}
(fact "arr pop"
  (l/emit-as :python [(python-tf-x-arr-pop '[_ arr])])
  => #"pop")

tahto/model/spec_xtalk/fn_python_test.clj:310:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-reverse :added "4.0"}
(fact "arr reverse"
  (l/emit-as :python [(python-tf-x-arr-reverse '[_ arr])])
  => #"reversed")

tahto/model/spec_xtalk/fn_python_test.clj:315:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-push-first :added "4.0"}
(fact "arr push first"
  (l/emit-as :python [(python-tf-x-arr-push-first '[_ arr 1])])
  => #"insert")

tahto/model/spec_xtalk/fn_python_test.clj:320:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-pop-first :added "4.0"}
(fact "arr pop first"
  (l/emit-as :python [(python-tf-x-arr-pop-first '[_ arr])])
  => #"pop")

tahto/model/spec_xtalk/fn_python_test.clj:325:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-insert :added "4.0"}
(fact "arr insert"
  (l/emit-as :python [(python-tf-x-arr-insert '[_ arr 0 1])])
  => #"insert")

tahto/model/spec_xtalk/fn_python_test.clj:330:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-remove :added "4.1"}
(fact "removes array elements")

tahto/model/spec_xtalk/fn_python_test.clj:333:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-arr-sort :added "4.0"}
(fact "arr sort"
  (l/emit-as :python [(python-tf-x-arr-sort '[_ arr k c])])
  => #"sort")

tahto/model/spec_xtalk/fn_python_test.clj:338:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-comp :added "4.0"}
(fact "arr str comp"
  (l/emit-as :python [(python-tf-x-str-comp '[_ a b])])
  => #"<")

tahto/model/spec_xtalk/fn_python_test.clj:343:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-arr-every :added "4.1"}
(fact "transforms arr-every forms")

tahto/model/spec_xtalk/fn_python_test.clj:346:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-arr-some :added "4.1"}
(fact "transforms arr-some forms")

tahto/model/spec_xtalk/fn_python_test.clj:349:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-char :added "4.0"}
(fact "str char"
  (l/emit-as :python [(python-tf-x-str-char '[_ s 0])])
  => #"ord")

tahto/model/spec_xtalk/fn_python_test.clj:354:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-split :added "4.0"}
(fact "str split"
  (l/emit-as :python [(python-tf-x-str-split '[_ s " "])])
  => #"split")

tahto/model/spec_xtalk/fn_python_test.clj:359:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-join :added "4.0"}
(fact "str join"
  (l/emit-as :python [(python-tf-x-str-join '[_ s arr])])
  => #"join")

tahto/model/spec_xtalk/fn_python_test.clj:364:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-index-of :added "4.0"}
(fact "str index of"
  (l/emit-as :python [(python-tf-x-str-index-of '[_ s "a"])])
  => #"find")

tahto/model/spec_xtalk/fn_python_test.clj:369:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers")

tahto/model/spec_xtalk/fn_python_test.clj:372:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-substring :added "4.0"}
(fact "str substring"
  (l/emit-as :python [(python-tf-x-str-substring '[_ s 0 1])])
  => #":")

tahto/model/spec_xtalk/fn_python_test.clj:377:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-to-upper :added "4.0"}
(fact "str to upper"
  (l/emit-as :python [(python-tf-x-str-to-upper '[_ s])])
  => #"upper")

tahto/model/spec_xtalk/fn_python_test.clj:382:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-to-lower :added "4.0"}
(fact "str to lower"
  (l/emit-as :python [(python-tf-x-str-to-lower '[_ s])])
  => #"lower")

tahto/model/spec_xtalk/fn_python_test.clj:387:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-replace :added "4.0"}
(fact "str replace"
  (l/emit-as :python [(python-tf-x-str-replace '[_ s "a" "b"])])
  => #"replace")

tahto/model/spec_xtalk/fn_python_test.clj:392:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-trim :added "4.1"}
(fact "trims strings")

tahto/model/spec_xtalk/fn_python_test.clj:395:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-trim-left :added "4.1"}
(fact "trims left whitespace")

tahto/model/spec_xtalk/fn_python_test.clj:398:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-trim-right :added "4.1"}
(fact "trims right whitespace")

tahto/model/spec_xtalk/fn_python_test.clj:401:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-pad-left :added "4.1"}
(fact "pads strings on the left")

tahto/model/spec_xtalk/fn_python_test.clj:404:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-str-pad-right :added "4.1"}
(fact "pads strings on the right")

tahto/model/spec_xtalk/fn_python_test.clj:407:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-json-encode :added "4.0"}
(fact "json encode"
  (l/emit-as :python [(python-tf-x-json-encode '[_ obj])])
  => #"json")

tahto/model/spec_xtalk/fn_python_test.clj:412:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-json-decode :added "4.0"}
(fact "json decode"
  (l/emit-as :python [(python-tf-x-json-decode '[_ s])])
  => #"json")

tahto/model/spec_xtalk/fn_python_test.clj:417:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-return-encode :added "4.0"}
(fact "return encode"
  (l/emit-as :python [(python-tf-x-return-encode '[_ out id key])])
  => #"json.dumps")

tahto/model/spec_xtalk/fn_python_test.clj:422:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-return-wrap :added "4.0"}
(fact "return wrap"
  (l/emit-as :python [(python-tf-x-return-wrap '[_ f encode-fn])])
  => #"try")

tahto/model/spec_xtalk/fn_python_test.clj:427:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-return-eval :added "4.0"}
(fact "return eval"
  (l/emit-as :python [(python-tf-x-return-eval '[_ s wrap-fn])])
  => #"exec")

tahto/model/spec_xtalk/fn_python_test.clj:432:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-socket-connect :added "4.0"}
(fact "socket connect"
  (let [out (l/emit-as :python [(python-tf-x-socket-connect '[_ host port opts cb])])]
    [(boolean (re-find #"conn = socket\.socket\(\)" out))
     (boolean (re-find #"conn\.connect\(\(host,port\)\)" out))
     (boolean (re-find #"except Exception as e:" out))
     (boolean (re-find #"return cb\(e,None\)" out))
     (boolean (re-find #"(?s)conn\.connect\(\(host,port\)\)\s+except Exception as e:\s+return cb\(e,None\)\s+return cb\(None,conn\)" out))])
  => [true true true true true])

tahto/model/spec_xtalk/fn_python_test.clj:442:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-socket-send :added "4.0"}
(fact "socket send"
  (l/emit-as :python [(python-tf-x-socket-send '[_ conn s])])
  => #"sendall")

tahto/model/spec_xtalk/fn_python_test.clj:447:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-socket-close :added "4.0"}
(fact "socket close"
  (l/emit-as :python [(python-tf-x-socket-close '[_ conn])])
  => #"close")

tahto/model/spec_xtalk/fn_python_test.clj:452:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :python [(python-tf-x-iter-from-obj '[_ obj])])
  => #"iter")

tahto/model/spec_xtalk/fn_python_test.clj:457:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :python [(python-tf-x-iter-from-arr '[_ arr])])
  => #"iter")

tahto/model/spec_xtalk/fn_python_test.clj:462:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :python [(python-tf-x-iter-from '[_ obj])])
  => #"iter")

tahto/model/spec_xtalk/fn_python_test.clj:467:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :python [(python-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"next")

tahto/model/spec_xtalk/fn_python_test.clj:472:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :python [(python-tf-x-iter-next '[_ it])])
  => #"next")

tahto/model/spec_xtalk/fn_python_test.clj:477:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :python [(python-tf-x-iter-has? '[_ obj])])
  => #"hasattr")

tahto/model/spec_xtalk/fn_python_test.clj:482:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :python [(python-tf-x-iter-native? '[_ it])])
  => #"hasattr")

tahto/model/spec_xtalk/fn_python_test.clj:487:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")

tahto/model/spec_xtalk/fn_python_test.clj:490:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :python [(python-tf-x-with-delay '[_ ms thunk])])
  => #"sleep")

tahto/model/spec_xtalk/fn_python_test.clj:495:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-pwd :added "4.1"}
(fact "gets working directory")

tahto/model/spec_xtalk/fn_python_test.clj:498:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :python [(python-tf-x-shell '[_ "ls" cm])])
  => #"subprocess")

tahto/model/spec_xtalk/fn_python_test.clj:503:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

tahto/model/spec_xtalk/fn_python_test.clj:506:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-file-slurp :added "4.1"}
(fact "slurp file"
  (comment (l/emit-as :python [(python-tf-x-file-slurp '[_ filename opts cb])])
            => nil?))

tahto/model/spec_xtalk/fn_python_test.clj:511:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-file-spit :added "4.1"}
(fact "spit file"
  (comment (l/emit-as :python [(python-tf-x-file-spit '[_ filename s opts cb])])
            => nil?))

tahto/model/spec_xtalk/fn_python_test.clj:516:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-del-key :added "4.1"}
(fact "deletes object key")


tahto/model/spec_xtalk/fn_python_test.clj:520:^{:refer tahto.model.spec-xtalk.fn-python/python-tf-x-construct :added "4.1"}
(fact "constructs with splatted arguments"
  (python-tf-x-construct '(_ Widget args))
  => '(Widget (:* args)))
