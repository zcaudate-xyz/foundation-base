tahto/model/spec_xtalk/fn_elisp_test.clj:1:(ns tahto.model.spec-xtalk.fn-elisp-test
  (:require [tahto.core :as l]
tahto/model/spec_xtalk/fn_elisp_test.clj:3:             [tahto.model.spec-elisp]
tahto/model/spec_xtalk/fn_elisp_test.clj:4:             [tahto.model.spec-xtalk.fn-elisp :refer :all])
  (:use code.test))

tahto/model/spec_xtalk/fn_elisp_test.clj:7:^{:refer tahto.model.spec-xtalk.fn-elisp/+elisp-promise+ :added "4.1"}
(fact "elisp promise helpers are exposed as local macros"
  [(get-in +elisp-promise+ [:x-async-run :macro])
   (get-in +elisp-promise+ [:x-promise :macro])
   (get-in +elisp-promise+ [:x-promise-then :macro])
   (get-in +elisp-promise+ [:x-promise-catch :macro])
   (get-in +elisp-promise+ [:x-promise-finally :macro])
   (get-in +elisp-promise+ [:x-promise-native? :macro])]
  => [#'elisp-tf-x-async-run
      #'elisp-tf-x-promise
      #'elisp-tf-x-promise-then
      #'elisp-tf-x-promise-catch
      #'elisp-tf-x-promise-finally
      #'elisp-tf-x-promise-native?])

tahto/model/spec_xtalk/fn_elisp_test.clj:22:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-begin :added "4.1"}
(fact "wraps expressions in progn")

tahto/model/spec_xtalk/fn_elisp_test.clj:25:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-vector-subseq :added "4.1"}
(fact "extracts vector subsequences")

tahto/model/spec_xtalk/fn_elisp_test.clj:28:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-vector->list :added "4.1"}
(fact "converts vectors to lists")

tahto/model/spec_xtalk/fn_elisp_test.clj:31:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-del :added "4.1"}
(fact "deletes values")

tahto/model/spec_xtalk/fn_elisp_test.clj:34:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-print :added "4.1"}
(fact "prints values"
  (elisp-tf-x-print '(x:print "hello"))
  => '(progn (princ "hello") nil))

tahto/model/spec_xtalk/fn_elisp_test.clj:39:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-len :added "4.1"}
(fact "gets length"
  (elisp-tf-x-len '(x:len arr))
  => '(if (vectorp arr)
        (length arr)
        (if (hash-table-p arr)
          (hash-table-count arr)
          (length arr))))

tahto/model/spec_xtalk/fn_elisp_test.clj:48:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (elisp-tf-x-cat '(x:cat "a" "b"))
  => '(concat "a" "b"))

tahto/model/spec_xtalk/fn_elisp_test.clj:53:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (elisp-tf-x-apply '(x:apply f args))
  => '(apply f (append args nil)))

tahto/model/spec_xtalk/fn_elisp_test.clj:58:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-div :added "4.1"}
(fact "divides values")

tahto/model/spec_xtalk/fn_elisp_test.clj:61:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-err :added "4.1"}
(fact "raises errors")

tahto/model/spec_xtalk/fn_elisp_test.clj:64:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

tahto/model/spec_xtalk/fn_elisp_test.clj:67:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-random :added "4.1"}
(fact "generates random values")

tahto/model/spec_xtalk/fn_elisp_test.clj:70:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-now-ms :added "4.1"}
(fact "gets current time in milliseconds")

tahto/model/spec_xtalk/fn_elisp_test.clj:73:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/spec_xtalk/fn_elisp_test.clj:76:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/spec_xtalk/fn_elisp_test.clj:79:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/spec_xtalk/fn_elisp_test.clj:82:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/spec_xtalk/fn_elisp_test.clj:85:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :elisp [(elisp-tf-x-type-native '(x:type-native obj))])
  => #"hash-table-p")

tahto/model/spec_xtalk/fn_elisp_test.clj:90:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-global-set :added "4.1"}
(fact "sets global variables")

tahto/model/spec_xtalk/fn_elisp_test.clj:93:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

tahto/model/spec_xtalk/fn_elisp_test.clj:96:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

tahto/model/spec_xtalk/fn_elisp_test.clj:99:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-to-string :added "4.1"}
(fact "converts to string")

tahto/model/spec_xtalk/fn_elisp_test.clj:102:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-to-number :added "4.1"}
(fact "converts to number")

tahto/model/spec_xtalk/fn_elisp_test.clj:105:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-string? :added "4.1"}
(fact "checks string type")

tahto/model/spec_xtalk/fn_elisp_test.clj:108:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-number? :added "4.1"}
(fact "checks number type")

tahto/model/spec_xtalk/fn_elisp_test.clj:111:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

tahto/model/spec_xtalk/fn_elisp_test.clj:114:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

tahto/model/spec_xtalk/fn_elisp_test.clj:117:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-function? :added "4.1"}
(fact "checks function type")

tahto/model/spec_xtalk/fn_elisp_test.clj:120:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-object? :added "4.1"}
(fact "checks object type")

tahto/model/spec_xtalk/fn_elisp_test.clj:123:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-is-array? :added "4.1"}
(fact "checks array type")

tahto/model/spec_xtalk/fn_elisp_test.clj:126:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-create :added "4.1"}
(fact "creates lookup tables")

tahto/model/spec_xtalk/fn_elisp_test.clj:129:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-eq :added "4.1"}
(fact "compares lookup tables")

tahto/model/spec_xtalk/fn_elisp_test.clj:132:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-get :added "4.1"}
(fact "gets lookup table value")

tahto/model/spec_xtalk/fn_elisp_test.clj:135:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-set :added "4.1"}
(fact "sets lookup table value")

tahto/model/spec_xtalk/fn_elisp_test.clj:138:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-del :added "4.1"}
(fact "deletes lookup table value")

tahto/model/spec_xtalk/fn_elisp_test.clj:141:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (elisp-tf-x-get-key '(x:get-key obj key fallback))
  => '(gethash key obj fallback))

tahto/model/spec_xtalk/fn_elisp_test.clj:146:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-get-path :added "4.1"}
(fact "gets nested path")

tahto/model/spec_xtalk/fn_elisp_test.clj:149:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (l/emit-as :elisp [(elisp-tf-x-has-key? '(x:has-key? obj key expected))])
  => #"gethash")

tahto/model/spec_xtalk/fn_elisp_test.clj:154:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-del-key :added "4.1"}
(fact "deletes object key")

tahto/model/spec_xtalk/fn_elisp_test.clj:157:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-set-key :added "4.1"}
(fact "sets object key")

tahto/model/spec_xtalk/fn_elisp_test.clj:160:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-copy-key :added "4.1"}
(fact "copies object key")

tahto/model/spec_xtalk/fn_elisp_test.clj:163:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (elisp-tf-x-obj-keys '(x:obj-keys obj))
  => '(vconcat (hash-table-keys obj)))

tahto/model/spec_xtalk/fn_elisp_test.clj:168:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-vals :added "4.1"}
(fact "lists object values")

tahto/model/spec_xtalk/fn_elisp_test.clj:171:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-pairs :added "4.1"}
(fact "lists object pairs")

tahto/model/spec_xtalk/fn_elisp_test.clj:174:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-clone :added "4.1"}
(fact "clones objects")

tahto/model/spec_xtalk/fn_elisp_test.clj:177:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-assign :added "4.1"}
(fact "assigns objects")

tahto/model/spec_xtalk/fn_elisp_test.clj:180:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :elisp [(elisp-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"aref")

tahto/model/spec_xtalk/fn_elisp_test.clj:185:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-set-idx :added "4.1"}
(fact "sets array index")

tahto/model/spec_xtalk/fn_elisp_test.clj:188:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-clone :added "4.1"}
(fact "clones arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:191:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-slice :added "4.1"}
(fact "slices arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:194:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-reverse :added "4.1"}
(fact "reverses arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:197:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-concat :added "4.1"}
(fact "concatenates arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:200:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (elisp-tf-x-arr-push '(x:arr-push arr value))
  => '(progn (setq arr (vconcat arr (vector value))) arr))

tahto/model/spec_xtalk/fn_elisp_test.clj:205:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-pop :added "4.1"}
(fact "pops array elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:208:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-push-first :added "4.1"}
(fact "prepends array elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:211:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-pop-first :added "4.1"}
(fact "removes first array element")

tahto/model/spec_xtalk/fn_elisp_test.clj:214:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-insert :added "4.1"}
(fact "inserts array elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:217:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-remove :added "4.1"}
(fact "removes array elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:220:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-assign :added "4.1"}
(fact "assigns array elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:223:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-some :added "4.1"}
(fact "tests some elements")

tahto/model/spec_xtalk/fn_elisp_test.clj:226:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-each :added "4.1"}
(fact "iterates over arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:229:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-every :added "4.1"}
(fact "tests every element")

tahto/model/spec_xtalk/fn_elisp_test.clj:232:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-map :added "4.1"}
(fact "maps arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:235:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-filter :added "4.1"}
(fact "filters arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:238:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-foldl :added "4.1"}
(fact "folds arrays left")

tahto/model/spec_xtalk/fn_elisp_test.clj:241:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-foldr :added "4.1"}
(fact "folds arrays right")

tahto/model/spec_xtalk/fn_elisp_test.clj:244:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:247:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-comp :added "4.1"}
(fact "compares strings")

tahto/model/spec_xtalk/fn_elisp_test.clj:250:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-char :added "4.1"}
tahto/model/spec_xtalk/fn_elisp_test.clj:251:(fact "gets string ctahtocter")

tahto/model/spec_xtalk/fn_elisp_test.clj:253:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-split :added "4.1"}
(fact "splits strings")

tahto/model/spec_xtalk/fn_elisp_test.clj:256:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-len :added "4.1"}
(fact "gets string length")

tahto/model/spec_xtalk/fn_elisp_test.clj:259:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (elisp-tf-x-str-join '(x:str-join sep coll))
  => '(mapconcat (lambda (x) x) (append coll nil) sep))

tahto/model/spec_xtalk/fn_elisp_test.clj:264:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-index-of :added "4.1"}
(fact "finds substring index")

tahto/model/spec_xtalk/fn_elisp_test.clj:267:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-substring :added "4.1"}
(fact "extracts substrings")

tahto/model/spec_xtalk/fn_elisp_test.clj:270:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings")

tahto/model/spec_xtalk/fn_elisp_test.clj:273:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings")

tahto/model/spec_xtalk/fn_elisp_test.clj:276:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers")

tahto/model/spec_xtalk/fn_elisp_test.clj:279:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

tahto/model/spec_xtalk/fn_elisp_test.clj:282:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim :added "4.1"}
(fact "trims strings")

tahto/model/spec_xtalk/fn_elisp_test.clj:285:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim-left :added "4.1"}
(fact "trims left whitespace")

tahto/model/spec_xtalk/fn_elisp_test.clj:288:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim-right :added "4.1"}
(fact "trims right whitespace")

tahto/model/spec_xtalk/fn_elisp_test.clj:291:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-pad-left :added "4.1"}
(fact "pads strings on the left")

tahto/model/spec_xtalk/fn_elisp_test.clj:294:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-pad-right :added "4.1"}
(fact "pads strings on the right")

tahto/model/spec_xtalk/fn_elisp_test.clj:297:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-starts-with :added "4.1"}
(fact "checks string prefix")

tahto/model/spec_xtalk/fn_elisp_test.clj:300:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-str-ends-with :added "4.1"}
(fact "checks string suffix")

tahto/model/spec_xtalk/fn_elisp_test.clj:303:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-abs :added "4.1"}
(fact "computes absolute value")

tahto/model/spec_xtalk/fn_elisp_test.clj:306:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-acos :added "4.1"}
(fact "computes arc cosine")

tahto/model/spec_xtalk/fn_elisp_test.clj:309:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-asin :added "4.1"}
(fact "computes arc sine")

tahto/model/spec_xtalk/fn_elisp_test.clj:312:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-atan :added "4.1"}
(fact "computes arc tangent")

tahto/model/spec_xtalk/fn_elisp_test.clj:315:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-max :added "4.1"}
(fact "computes maximum")

tahto/model/spec_xtalk/fn_elisp_test.clj:318:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-min :added "4.1"}
(fact "computes minimum")

tahto/model/spec_xtalk/fn_elisp_test.clj:321:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-mod :added "4.1"}
(fact "computes modulo")

tahto/model/spec_xtalk/fn_elisp_test.clj:324:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-quot :added "4.1"}
(fact "computes quotient")

tahto/model/spec_xtalk/fn_elisp_test.clj:327:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-floor :added "4.1"}
(fact "computes floor")

tahto/model/spec_xtalk/fn_elisp_test.clj:330:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-ceil :added "4.1"}
(fact "computes ceiling")

tahto/model/spec_xtalk/fn_elisp_test.clj:333:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-cos :added "4.1"}
(fact "computes cosine")

tahto/model/spec_xtalk/fn_elisp_test.clj:336:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

tahto/model/spec_xtalk/fn_elisp_test.clj:339:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-exp :added "4.1"}
(fact "computes exponential")

tahto/model/spec_xtalk/fn_elisp_test.clj:342:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-loge :added "4.1"}
(fact "computes natural logarithm")

tahto/model/spec_xtalk/fn_elisp_test.clj:345:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

tahto/model/spec_xtalk/fn_elisp_test.clj:348:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sin :added "4.1"}
(fact "computes sine")

tahto/model/spec_xtalk/fn_elisp_test.clj:351:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

tahto/model/spec_xtalk/fn_elisp_test.clj:354:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sqrt :added "4.1"}
(fact "computes square root")

tahto/model/spec_xtalk/fn_elisp_test.clj:357:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-tan :added "4.1"}
(fact "computes tangent")

tahto/model/spec_xtalk/fn_elisp_test.clj:360:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

tahto/model/spec_xtalk/fn_elisp_test.clj:363:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (elisp-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))

tahto/model/spec_xtalk/fn_elisp_test.clj:368:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-json-encode :added "4.1"}
(fact "encodes JSON")

tahto/model/spec_xtalk/fn_elisp_test.clj:371:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-json-decode :added "4.1"}
(fact "decodes JSON")

tahto/model/spec_xtalk/fn_elisp_test.clj:374:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-return-encode :added "4.1"}
(fact "encodes return values")

tahto/model/spec_xtalk/fn_elisp_test.clj:377:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-return-wrap :added "4.1"}
(fact "wraps return values")

tahto/model/spec_xtalk/fn_elisp_test.clj:380:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-return-eval :added "4.1"}
(fact "evaluates return values")

tahto/model/spec_xtalk/fn_elisp_test.clj:383:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-and :added "4.1"}
(fact "computes bitwise AND")

tahto/model/spec_xtalk/fn_elisp_test.clj:386:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-or :added "4.1"}
(fact "computes bitwise OR")

tahto/model/spec_xtalk/fn_elisp_test.clj:389:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-xor :added "4.1"}
(fact "computes bitwise XOR")

tahto/model/spec_xtalk/fn_elisp_test.clj:392:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-lshift :added "4.1"}
(fact "computes left shifts bits")

tahto/model/spec_xtalk/fn_elisp_test.clj:395:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-rshift :added "4.1"}
(fact "computes right shifts bits")

tahto/model/spec_xtalk/fn_elisp_test.clj:398:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterators from arrays")

tahto/model/spec_xtalk/fn_elisp_test.clj:401:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects")

tahto/model/spec_xtalk/fn_elisp_test.clj:404:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from :added "4.1"}
(fact "creates iterators")

tahto/model/spec_xtalk/fn_elisp_test.clj:407:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-next :added "4.1"}
(fact "advances iterators")

tahto/model/spec_xtalk/fn_elisp_test.clj:410:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-eq :added "4.1"}
(fact "compares iterators")

tahto/model/spec_xtalk/fn_elisp_test.clj:413:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-null :added "4.1"}
(fact "creates null iterators")

tahto/model/spec_xtalk/fn_elisp_test.clj:416:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-has? :added "4.1"}
(fact "checks iterator state")

tahto/model/spec_xtalk/fn_elisp_test.clj:419:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-native? :added "4.1"}
(fact "checks native iterators")

tahto/model/spec_xtalk/fn_elisp_test.clj:422:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-create :added "4.1"}
(fact "creates prototypes")

tahto/model/spec_xtalk/fn_elisp_test.clj:425:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-get :added "4.1"}
(fact "gets prototypes")

tahto/model/spec_xtalk/fn_elisp_test.clj:428:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-set :added "4.1"}
(fact "sets prototypes")

tahto/model/spec_xtalk/fn_elisp_test.clj:431:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-method :added "4.1"}
(fact "calls prototype methods")

tahto/model/spec_xtalk/fn_elisp_test.clj:434:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise :added "4.1"}
(fact "transforms x:promise")

tahto/model/spec_xtalk/fn_elisp_test.clj:437:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-async-run :added "4.1"}
(fact "elisp async run emits a lower-level thread primitive"
  (l/emit-as :elisp [(elisp-tf-x-async-run '[_ thunk])])
  => #"make-thread")

tahto/model/spec_xtalk/fn_elisp_test.clj:442:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-all :added "4.1"}
(fact "transforms x:promise-all")

tahto/model/spec_xtalk/fn_elisp_test.clj:445:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-then :added "4.1"}
(fact "transforms x:promise-then")

tahto/model/spec_xtalk/fn_elisp_test.clj:448:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-catch :added "4.1"}
(fact "transforms x:promise-catch")

tahto/model/spec_xtalk/fn_elisp_test.clj:451:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-finally :added "4.1"}
(fact "transforms x:promise-finally")

tahto/model/spec_xtalk/fn_elisp_test.clj:454:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-native? :added "4.1"}
(fact "transforms x:promise-native?")

tahto/model/spec_xtalk/fn_elisp_test.clj:457:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-with-delay :added "4.1"}
(fact "delays execution")

tahto/model/spec_xtalk/fn_elisp_test.clj:460:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

tahto/model/spec_xtalk/fn_elisp_test.clj:463:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-send :added "4.1"}
(fact "sends socket data")

tahto/model/spec_xtalk/fn_elisp_test.clj:466:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-close :added "4.1"}
(fact "closes sockets")

tahto/model/spec_xtalk/fn_elisp_test.clj:469:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-notify-http :added "4.1"}
(fact "notifies via HTTP")

tahto/model/spec_xtalk/fn_elisp_test.clj:472:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-pwd :added "4.1"}
(fact "gets working directory")

tahto/model/spec_xtalk/fn_elisp_test.clj:475:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-shell :added "4.1"}
(fact "runs shell commands")

tahto/model/spec_xtalk/fn_elisp_test.clj:478:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

tahto/model/spec_xtalk/fn_elisp_test.clj:481:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-file-slurp :added "4.1"}
(fact "reads file contents")

tahto/model/spec_xtalk/fn_elisp_test.clj:484:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-file-spit :added "4.1"}
(fact "writes file contents")


tahto/model/spec_xtalk/fn_elisp_test.clj:488:^{:refer tahto.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-new :added "4.1"}
(fact "transforms x:promise-new"
  (elisp-tf-x-promise-new '(x:promise-new thunk))
  => '(xt-promise-new thunk))