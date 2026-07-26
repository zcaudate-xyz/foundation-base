tahto/model/spec_xtalk/fn_scheme_test.clj:1:(ns tahto.model.spec-xtalk.fn-scheme-test
  (:require [tahto.core :as l]
tahto/model/spec_xtalk/fn_scheme_test.clj:3:             [tahto.model.spec-scheme]
tahto/model/spec_xtalk/fn_scheme_test.clj:4:             [tahto.model.spec-xtalk.fn-scheme :refer :all])
  (:use code.test))

tahto/model/spec_xtalk/fn_scheme_test.clj:7:^{:refer tahto.model.spec-xtalk.fn-scheme/+scheme-promise+ :added "4.1"}
(fact "scheme promise helpers are exposed as local macros"
  [(get-in +scheme-promise+ [:x-async-run :macro])
   (get-in +scheme-promise+ [:x-promise :macro])
   (get-in +scheme-promise+ [:x-promise-then :macro])
   (get-in +scheme-promise+ [:x-promise-catch :macro])
   (get-in +scheme-promise+ [:x-promise-finally :macro])
   (get-in +scheme-promise+ [:x-promise-native? :macro])]
  => [#'scheme-tf-x-async-run
      #'scheme-tf-x-promise
      #'scheme-tf-x-promise-then
      #'scheme-tf-x-promise-catch
      #'scheme-tf-x-promise-finally
      #'scheme-tf-x-promise-native?])

tahto/model/spec_xtalk/fn_scheme_test.clj:22:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-begin :added "4.1"}
(fact "wraps expressions in begin")

tahto/model/spec_xtalk/fn_scheme_test.clj:25:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-vector-slice :added "4.1"}
(fact "slices vectors")

tahto/model/spec_xtalk/fn_scheme_test.clj:28:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-vector->list :added "4.1"}
(fact "converts vectors to lists")

tahto/model/spec_xtalk/fn_scheme_test.clj:31:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-if-chain :added "4.1"}
(fact "builds scheme if chains")

tahto/model/spec_xtalk/fn_scheme_test.clj:34:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-promise-native-expr :added "4.1"}
(fact "handles scheme promise native expr")

tahto/model/spec_xtalk/fn_scheme_test.clj:37:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-promise-rejected-expr :added "4.1"}
(fact "handles scheme promise rejected expr")

tahto/model/spec_xtalk/fn_scheme_test.clj:40:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-promise-wrap-expr :added "4.1"}
(fact "handles scheme promise wrap expr")

tahto/model/spec_xtalk/fn_scheme_test.clj:43:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-del :added "4.1"}
(fact "deletes values")

tahto/model/spec_xtalk/fn_scheme_test.clj:46:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-print :added "4.1"}
(fact "prints values"
  (scheme-tf-x-print '(x:print "hello"))
  => '(begin (display "hello") false))

tahto/model/spec_xtalk/fn_scheme_test.clj:51:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-len :added "4.1"}
(fact "gets length"
  (scheme-tf-x-len '(x:len arr))
  => '(if (vector? arr)
        (vector-length arr)
        (if (string? arr)
          (string-length arr)
          (if (hash? arr)
            (hash-count arr)
            (length arr)))))

tahto/model/spec_xtalk/fn_scheme_test.clj:62:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (scheme-tf-x-cat '(x:cat "a" "b"))
  => '(string-append "a" "b"))

tahto/model/spec_xtalk/fn_scheme_test.clj:67:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (scheme-tf-x-apply '(x:apply f args))
  => '(apply f (if (vector? args) (vector->list args) args)))

tahto/model/spec_xtalk/fn_scheme_test.clj:72:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-div :added "4.1"}
(fact "divides values")

tahto/model/spec_xtalk/fn_scheme_test.clj:75:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-err :added "4.1"}
(fact "raises errors")

tahto/model/spec_xtalk/fn_scheme_test.clj:78:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

tahto/model/spec_xtalk/fn_scheme_test.clj:81:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-random :added "4.1"}
(fact "generates random values")

tahto/model/spec_xtalk/fn_scheme_test.clj:84:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-now-ms :added "4.1"}
(fact "gets current time in milliseconds")

tahto/model/spec_xtalk/fn_scheme_test.clj:87:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/spec_xtalk/fn_scheme_test.clj:90:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/spec_xtalk/fn_scheme_test.clj:93:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/spec_xtalk/fn_scheme_test.clj:96:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/spec_xtalk/fn_scheme_test.clj:99:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :scheme [(scheme-tf-x-type-native '(x:type-native obj))])
  => #"procedure\\?")

tahto/model/spec_xtalk/fn_scheme_test.clj:104:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-global-set :added "4.1"}
(fact "sets global variables")

tahto/model/spec_xtalk/fn_scheme_test.clj:107:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

tahto/model/spec_xtalk/fn_scheme_test.clj:110:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

tahto/model/spec_xtalk/fn_scheme_test.clj:113:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-to-string :added "4.1"}
(fact "converts to string")

tahto/model/spec_xtalk/fn_scheme_test.clj:116:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-to-number :added "4.1"}
(fact "converts to number")

tahto/model/spec_xtalk/fn_scheme_test.clj:119:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-string? :added "4.1"}
(fact "checks string type")

tahto/model/spec_xtalk/fn_scheme_test.clj:122:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-number? :added "4.1"}
(fact "checks number type")

tahto/model/spec_xtalk/fn_scheme_test.clj:125:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

tahto/model/spec_xtalk/fn_scheme_test.clj:128:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

tahto/model/spec_xtalk/fn_scheme_test.clj:131:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-function? :added "4.1"}
(fact "checks function type")

tahto/model/spec_xtalk/fn_scheme_test.clj:134:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-object? :added "4.1"}
(fact "checks object type")

tahto/model/spec_xtalk/fn_scheme_test.clj:137:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-is-array? :added "4.1"}
(fact "checks array type")

tahto/model/spec_xtalk/fn_scheme_test.clj:140:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-create :added "4.1"}
(fact "creates lookup tables")

tahto/model/spec_xtalk/fn_scheme_test.clj:143:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-get :added "4.1"}
(fact "gets lookup table value")

tahto/model/spec_xtalk/fn_scheme_test.clj:146:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-set :added "4.1"}
(fact "sets lookup table value")

tahto/model/spec_xtalk/fn_scheme_test.clj:149:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-del :added "4.1"}
(fact "deletes lookup table value")

tahto/model/spec_xtalk/fn_scheme_test.clj:152:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-eq :added "4.1"}
(fact "compares lookup tables")

tahto/model/spec_xtalk/fn_scheme_test.clj:155:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (scheme-tf-x-get-key '(x:get-key obj key fallback))
  => '(hash-ref obj key (lambda () fallback)))

tahto/model/spec_xtalk/fn_scheme_test.clj:160:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-get-path :added "4.1"}
(fact "gets nested path")

tahto/model/spec_xtalk/fn_scheme_test.clj:163:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (scheme-tf-x-has-key? '(x:has-key? obj key expected))
  => '(and (hash-has-key? obj key)
           (equal? expected (hash-ref obj key))))

tahto/model/spec_xtalk/fn_scheme_test.clj:169:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-del-key :added "4.1"}
(fact "deletes object key")

tahto/model/spec_xtalk/fn_scheme_test.clj:172:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-set-key :added "4.1"}
(fact "sets object key")

tahto/model/spec_xtalk/fn_scheme_test.clj:175:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-copy-key :added "4.1"}
(fact "copies object key")

tahto/model/spec_xtalk/fn_scheme_test.clj:178:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (scheme-tf-x-obj-keys '(x:obj-keys obj))
  => '(list->vector (hash-keys obj)))

tahto/model/spec_xtalk/fn_scheme_test.clj:183:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-vals :added "4.1"}
(fact "lists object values")

tahto/model/spec_xtalk/fn_scheme_test.clj:186:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-pairs :added "4.1"}
(fact "lists object pairs")

tahto/model/spec_xtalk/fn_scheme_test.clj:189:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-clone :added "4.1"}
(fact "clones objects")

tahto/model/spec_xtalk/fn_scheme_test.clj:192:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-assign :added "4.1"}
(fact "assigns objects")

tahto/model/spec_xtalk/fn_scheme_test.clj:195:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :scheme [(scheme-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"vector-ref")

tahto/model/spec_xtalk/fn_scheme_test.clj:200:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-set-idx :added "4.1"}
(fact "sets array index")

tahto/model/spec_xtalk/fn_scheme_test.clj:203:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-clone :added "4.1"}
(fact "clones arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:206:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-slice :added "4.1"}
(fact "slices arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:209:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-reverse :added "4.1"}
(fact "reverses arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:212:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-concat :added "4.1"}
(fact "concatenates arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:215:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (scheme-tf-x-arr-push '(x:arr-push arr value))
  => '(begin (set! arr (vector-append arr (vector value))) arr))

tahto/model/spec_xtalk/fn_scheme_test.clj:220:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-pop :added "4.1"}
(fact "pops array elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:223:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push-first :added "4.1"}
(fact "prepends array elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:226:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-pop-first :added "4.1"}
(fact "removes first array element")

tahto/model/spec_xtalk/fn_scheme_test.clj:229:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-insert :added "4.1"}
(fact "inserts array elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:232:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-remove :added "4.1"}
(fact "removes array elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:235:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-assign :added "4.1"}
(fact "assigns array elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:238:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-each :added "4.1"}
(fact "iterates over arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:241:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-every :added "4.1"}
(fact "tests every element")

tahto/model/spec_xtalk/fn_scheme_test.clj:244:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-some :added "4.1"}
(fact "tests some elements")

tahto/model/spec_xtalk/fn_scheme_test.clj:247:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-map :added "4.1"}
(fact "maps arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:250:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-filter :added "4.1"}
(fact "filters arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:253:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-foldl :added "4.1"}
(fact "folds arrays left")

tahto/model/spec_xtalk/fn_scheme_test.clj:256:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-foldr :added "4.1"}
(fact "folds arrays right")

tahto/model/spec_xtalk/fn_scheme_test.clj:259:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:262:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-comp :added "4.1"}
(fact "compares strings")

tahto/model/spec_xtalk/fn_scheme_test.clj:265:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-char :added "4.1"}
tahto/model/spec_xtalk/fn_scheme_test.clj:266:(fact "gets string ctahtocter")

tahto/model/spec_xtalk/fn_scheme_test.clj:268:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-len :added "4.1"}
(fact "gets string length")

tahto/model/spec_xtalk/fn_scheme_test.clj:271:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-split :added "4.1"}
(fact "splits strings")

tahto/model/spec_xtalk/fn_scheme_test.clj:274:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (scheme-tf-x-str-join '(x:str-join sep coll))
  => '(string-join (if (vector? coll) (vector->list coll) coll) sep))

tahto/model/spec_xtalk/fn_scheme_test.clj:279:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-index-of :added "4.1"}
(fact "finds substring index")

tahto/model/spec_xtalk/fn_scheme_test.clj:282:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-substring :added "4.1"}
(fact "extracts substrings")

tahto/model/spec_xtalk/fn_scheme_test.clj:285:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings")

tahto/model/spec_xtalk/fn_scheme_test.clj:288:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings")

tahto/model/spec_xtalk/fn_scheme_test.clj:291:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers")

tahto/model/spec_xtalk/fn_scheme_test.clj:294:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

tahto/model/spec_xtalk/fn_scheme_test.clj:297:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim :added "4.1"}
(fact "trims strings")

tahto/model/spec_xtalk/fn_scheme_test.clj:300:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim-left :added "4.1"}
(fact "trims left whitespace")

tahto/model/spec_xtalk/fn_scheme_test.clj:303:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim-right :added "4.1"}
(fact "trims right whitespace")

tahto/model/spec_xtalk/fn_scheme_test.clj:306:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-pad-left :added "4.1"}
(fact "pads strings on the left")

tahto/model/spec_xtalk/fn_scheme_test.clj:309:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-pad-right :added "4.1"}
(fact "pads strings on the right")

tahto/model/spec_xtalk/fn_scheme_test.clj:312:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-starts-with :added "4.1"}
(fact "checks string prefix")

tahto/model/spec_xtalk/fn_scheme_test.clj:315:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-str-ends-with :added "4.1"}
(fact "checks string suffix")

tahto/model/spec_xtalk/fn_scheme_test.clj:318:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-abs :added "4.1"}
(fact "computes absolute value")

tahto/model/spec_xtalk/fn_scheme_test.clj:321:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-acos :added "4.1"}
(fact "computes arc cosine")

tahto/model/spec_xtalk/fn_scheme_test.clj:324:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-asin :added "4.1"}
(fact "computes arc sine")

tahto/model/spec_xtalk/fn_scheme_test.clj:327:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-atan :added "4.1"}
(fact "computes arc tangent")

tahto/model/spec_xtalk/fn_scheme_test.clj:330:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-max :added "4.1"}
(fact "computes maximum")

tahto/model/spec_xtalk/fn_scheme_test.clj:333:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-min :added "4.1"}
(fact "computes minimum")

tahto/model/spec_xtalk/fn_scheme_test.clj:336:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-mod :added "4.1"}
(fact "computes modulo")

tahto/model/spec_xtalk/fn_scheme_test.clj:339:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-quot :added "4.1"}
(fact "computes quotient")

tahto/model/spec_xtalk/fn_scheme_test.clj:342:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-floor :added "4.1"}
(fact "computes floor")

tahto/model/spec_xtalk/fn_scheme_test.clj:345:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-ceil :added "4.1"}
(fact "computes ceiling")

tahto/model/spec_xtalk/fn_scheme_test.clj:348:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-cos :added "4.1"}
(fact "computes cosine")

tahto/model/spec_xtalk/fn_scheme_test.clj:351:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

tahto/model/spec_xtalk/fn_scheme_test.clj:354:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-exp :added "4.1"}
(fact "computes exponential")

tahto/model/spec_xtalk/fn_scheme_test.clj:357:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-loge :added "4.1"}
(fact "computes natural logarithm")

tahto/model/spec_xtalk/fn_scheme_test.clj:360:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

tahto/model/spec_xtalk/fn_scheme_test.clj:363:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sin :added "4.1"}
(fact "computes sine")

tahto/model/spec_xtalk/fn_scheme_test.clj:366:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

tahto/model/spec_xtalk/fn_scheme_test.clj:369:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sqrt :added "4.1"}
(fact "computes square root")

tahto/model/spec_xtalk/fn_scheme_test.clj:372:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-tan :added "4.1"}
(fact "computes tangent")

tahto/model/spec_xtalk/fn_scheme_test.clj:375:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

tahto/model/spec_xtalk/fn_scheme_test.clj:378:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (scheme-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))

tahto/model/spec_xtalk/fn_scheme_test.clj:383:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-and :added "4.1"}
(fact "computes bitwise AND")

tahto/model/spec_xtalk/fn_scheme_test.clj:386:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-or :added "4.1"}
(fact "computes bitwise OR")

tahto/model/spec_xtalk/fn_scheme_test.clj:389:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-xor :added "4.1"}
(fact "computes bitwise XOR")

tahto/model/spec_xtalk/fn_scheme_test.clj:392:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-lshift :added "4.1"}
(fact "computes left shifts bits")

tahto/model/spec_xtalk/fn_scheme_test.clj:395:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-rshift :added "4.1"}
(fact "computes right shifts bits")

tahto/model/spec_xtalk/fn_scheme_test.clj:398:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-json-encode :added "4.1"}
(fact "encodes JSON")

tahto/model/spec_xtalk/fn_scheme_test.clj:401:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-json-decode :added "4.1"}
(fact "decodes JSON")

tahto/model/spec_xtalk/fn_scheme_test.clj:404:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-return-encode :added "4.1"}
(fact "encodes return values")

tahto/model/spec_xtalk/fn_scheme_test.clj:407:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-return-wrap :added "4.1"}
(fact "wraps return values")

tahto/model/spec_xtalk/fn_scheme_test.clj:410:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-return-eval :added "4.1"}
(fact "evaluates return values")

tahto/model/spec_xtalk/fn_scheme_test.clj:413:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterators from arrays")

tahto/model/spec_xtalk/fn_scheme_test.clj:416:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects")

tahto/model/spec_xtalk/fn_scheme_test.clj:419:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from :added "4.1"}
(fact "creates iterators")

tahto/model/spec_xtalk/fn_scheme_test.clj:422:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-next :added "4.1"}
(fact "advances iterators")

tahto/model/spec_xtalk/fn_scheme_test.clj:425:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-eq :added "4.1"}
(fact "compares iterators")

tahto/model/spec_xtalk/fn_scheme_test.clj:428:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-null :added "4.1"}
(fact "creates null iterators")

tahto/model/spec_xtalk/fn_scheme_test.clj:431:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-has? :added "4.1"}
(fact "checks iterator state")

tahto/model/spec_xtalk/fn_scheme_test.clj:434:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-native? :added "4.1"}
(fact "checks native iterators")

tahto/model/spec_xtalk/fn_scheme_test.clj:437:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-create :added "4.1"}
(fact "creates prototypes")

tahto/model/spec_xtalk/fn_scheme_test.clj:440:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-get :added "4.1"}
(fact "gets prototypes")

tahto/model/spec_xtalk/fn_scheme_test.clj:443:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-set :added "4.1"}
(fact "sets prototypes")

tahto/model/spec_xtalk/fn_scheme_test.clj:446:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-method :added "4.1"}
(fact "calls prototype methods")

tahto/model/spec_xtalk/fn_scheme_test.clj:449:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise :added "4.1"}
(fact "transforms x:promise")

tahto/model/spec_xtalk/fn_scheme_test.clj:452:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-async-run :added "4.1"}
(fact "scheme async run emits a lower-level thread primitive"
  (l/emit-as :scheme [(scheme-tf-x-async-run '[_ thunk])])
  => #"thread")

tahto/model/spec_xtalk/fn_scheme_test.clj:457:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-all :added "4.1"}
(fact "transforms x:promise-all")

tahto/model/spec_xtalk/fn_scheme_test.clj:460:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-then :added "4.1"}
(fact "transforms x:promise-then")

tahto/model/spec_xtalk/fn_scheme_test.clj:463:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-catch :added "4.1"}
(fact "transforms x:promise-catch")

tahto/model/spec_xtalk/fn_scheme_test.clj:466:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-finally :added "4.1"}
(fact "transforms x:promise-finally")

tahto/model/spec_xtalk/fn_scheme_test.clj:469:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-native? :added "4.1"}
(fact "transforms x:promise-native?")

tahto/model/spec_xtalk/fn_scheme_test.clj:472:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-with-delay :added "4.1"}
(fact "delays execution")

tahto/model/spec_xtalk/fn_scheme_test.clj:475:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

tahto/model/spec_xtalk/fn_scheme_test.clj:478:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-send :added "4.1"}
(fact "sends socket data")

tahto/model/spec_xtalk/fn_scheme_test.clj:481:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-close :added "4.1"}
(fact "closes sockets")

tahto/model/spec_xtalk/fn_scheme_test.clj:484:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-notify-http :added "4.1"}
(fact "notifies via HTTP")

tahto/model/spec_xtalk/fn_scheme_test.clj:487:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-pwd :added "4.1"}
(fact "gets working directory")

tahto/model/spec_xtalk/fn_scheme_test.clj:490:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-shell :added "4.1"}
(fact "runs shell commands")

tahto/model/spec_xtalk/fn_scheme_test.clj:493:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

tahto/model/spec_xtalk/fn_scheme_test.clj:496:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-file-slurp :added "4.1"}
(fact "reads file contents")

tahto/model/spec_xtalk/fn_scheme_test.clj:499:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-file-spit :added "4.1"}
(fact "writes file contents")


tahto/model/spec_xtalk/fn_scheme_test.clj:503:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-new :added "4.1"}
(fact "transforms x:promise-new"
  (scheme-tf-x-promise-new '(x:promise-new thunk))
  => '(xt-promise-new thunk))

tahto/model/spec_xtalk/fn_scheme_test.clj:508:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-truthy-check :added "4.1"}
(fact "treats null and false as falsey"
  (scheme-truthy-check 'value)
  => '(if (null? value) false (if (equal? value false) false true)))

tahto/model/spec_xtalk/fn_scheme_test.clj:513:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-or :added "4.1"}
(fact "short-circuits and returns the first truthy value"
  (with-redefs [clojure.core/gensym (constantly 'v)]
    (scheme-tf-or '(or a b c)))
  => '(let [v a]
        (if (if (null? v) false (if (equal? v false) false true))
          v
          (let [v b]
            (if (if (null? v) false (if (equal? v false) false true))
              v c)))))

tahto/model/spec_xtalk/fn_scheme_test.clj:524:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf-and :added "4.1"}
(fact "short-circuits and returns the first falsey value"
  (with-redefs [clojure.core/gensym (constantly 'v)]
    (scheme-tf-and '(and a b c)))
  => '(let [v a]
        (if (if (null? v) false (if (equal? v false) false true))
          (let [v b]
            (if (if (null? v) false (if (equal? v false) false true))
              c v))
          v)))

tahto/model/spec_xtalk/fn_scheme_test.clj:535:^{:refer tahto.model.spec-xtalk.fn-scheme/scheme-tf--%%- :added "4.1"}
(fact "lowers the raw-eval marker to x:eval"
  (scheme-tf--%%- '(-%%- source)) => '(x:eval source))
