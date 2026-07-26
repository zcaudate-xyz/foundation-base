tahto/model/spec_xtalk/fn_js_test.clj:1:(ns tahto.model.spec-xtalk.fn-js-test
  (:require [tahto.core :as l]
tahto/model/spec_xtalk/fn_js_test.clj:3:            [tahto.model.spec-xtalk.fn-js :refer :all])
  (:use code.test))

(fact "supports staged value-position lowering for native type"
   (l/emit-as :js ['(fn [obj]
                     (return (x:type-native obj)))])
   => #"function \(obj\)\{[\s\S]*typeof obj[\s\S]*Array\.isArray\(obj\)"

   (l/emit-as :js ['(fn [obj f g]
                     (return (f (g (x:type-native obj)))))])
   => #"return f\(g\(\(function \(value\)\{[\s\S]*typeof value[\s\S]*\}\)\(obj\)\)\);"

   (l/emit-as :js ['x:type-native])
   => #"function \(value\)\{[\s\S]*typeof value[\s\S]*Array\.isArray\(value\)")

tahto/model/spec_xtalk/fn_js_test.clj:18:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-len :added "4.0"}
(fact "gets length"
  (l/emit-as :js [(js-tf-x-len '[_ arr])])
  => #"\.length")

tahto/model/spec_xtalk/fn_js_test.clj:23:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-cat :added "4.0"}
(fact "concatenates"
  (l/emit-as :js [(js-tf-x-cat '[_ "a" "b"])])
  => #"\+")

tahto/model/spec_xtalk/fn_js_test.clj:28:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-apply :added "4.0"}
(fact "applies function"
  (l/emit-as :js [(js-tf-x-apply '[_ f args])])
  => #"apply")

tahto/model/spec_xtalk/fn_js_test.clj:33:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-random :added "4.0"}
(fact "generates random number"
  (l/emit-as :js [(js-tf-x-random '[_])])
  => #"Math.random")

tahto/model/spec_xtalk/fn_js_test.clj:38:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-type-native :added "4.0"}
(fact "gets native type"

  (js-tf-x-type-native '[_ obj])
  => '(do (when (== obj nil) (return nil))
          (var t := (typeof obj))
          (if
              (== t "object")
            (cond
              (Array.isArray obj)
              (return "array")
              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object") (return "object") (return tn))))
            (return t))))

tahto/model/spec_xtalk/fn_js_test.clj:55:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/spec_xtalk/fn_js_test.clj:58:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/spec_xtalk/fn_js_test.clj:61:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/spec_xtalk/fn_js_test.clj:64:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/spec_xtalk/fn_js_test.clj:67:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-has-key? :added "4.1"}
(fact "has key"
  (l/emit-as :js [(js-tf-x-has-key? '[_ obj "k" nil])])
  => #"\[\"k\"\]")

tahto/model/spec_xtalk/fn_js_test.clj:72:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-m-max :added "4.0"}
(fact "gets max"
  (l/emit-as :js [(js-tf-x-m-max '[_ 1 2])])
  => #"Math.max")

tahto/model/spec_xtalk/fn_js_test.clj:77:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-m-min :added "4.0"}
(fact "gets min"
  (l/emit-as :js [(js-tf-x-m-min '[_ 1 2])])
  => #"Math.min")

tahto/model/spec_xtalk/fn_js_test.clj:82:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-m-mod :added "4.0"}
(fact "gets mod"
  (l/emit-as :js [(js-tf-x-m-mod '[_ 1 2])])
  => #"%")

tahto/model/spec_xtalk/fn_js_test.clj:87:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-m-quot :added "4.0"}
(fact "gets quotient"
  (l/emit-as :js [(js-tf-x-m-quot '[_ 1 2])])
  => #"Math.floor")

tahto/model/spec_xtalk/fn_js_test.clj:92:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-string? :added "4.0"}
(fact "checks if string"
  (l/emit-as :js [(js-tf-x-is-string? '[_ x])])
  => #"typeof")

tahto/model/spec_xtalk/fn_js_test.clj:97:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-number? :added "4.0"}
(fact "checks if number"
  (l/emit-as :js [(js-tf-x-is-number? '[_ x])])
  => #"typeof")

tahto/model/spec_xtalk/fn_js_test.clj:102:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-integer? :added "4.0"}
(fact "checks if integer"
  (l/emit-as :js [(js-tf-x-is-integer? '[_ x])])
  => #"Number.isInteger")

tahto/model/spec_xtalk/fn_js_test.clj:107:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-boolean? :added "4.0"}
(fact "checks if boolean"
  (l/emit-as :js [(js-tf-x-is-boolean? '[_ x])])
  => #"typeof")

tahto/model/spec_xtalk/fn_js_test.clj:112:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-object? :added "4.0"}
(fact "checks if object"
  (l/emit-as :js [(js-tf-x-is-object? '[_ x])])
  => #"typeof")

tahto/model/spec_xtalk/fn_js_test.clj:117:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-is-function? :added "4.0"}
(fact "checks if function"
  (l/emit-as :js [(js-tf-x-is-function? '[_ x])])
  => #"typeof")

tahto/model/spec_xtalk/fn_js_test.clj:122:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-lu-get :added "4.0"}
(fact "gets lookup"
  (l/emit-as :js [(js-tf-x-lu-get '[_ lu key])])
  => #"\.get")

tahto/model/spec_xtalk/fn_js_test.clj:127:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-lu-set :added "4.0"}
(fact "sets lookup"
  (l/emit-as :js [(js-tf-x-lu-set '[_ lu key val])])
  => #"\.set")

tahto/model/spec_xtalk/fn_js_test.clj:132:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-lu-del :added "4.0"}
(fact "deletes lookup"
  (l/emit-as :js [(js-tf-x-lu-del '[_ lu key])])
  => #"\.del")

tahto/model/spec_xtalk/fn_js_test.clj:137:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-obj-keys :added "4.0"}
(fact "gets object keys"
  (l/emit-as :js [(js-tf-x-obj-keys '[_ obj])])
  => #"Object.keys")

tahto/model/spec_xtalk/fn_js_test.clj:142:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-obj-vals :added "4.0"}
(fact "gets object values"
  (l/emit-as :js [(js-tf-x-obj-vals '[_ obj])])
  => #"Object.values")

tahto/model/spec_xtalk/fn_js_test.clj:147:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-obj-pairs :added "4.0"}
(fact "gets object pairs"
  (l/emit-as :js [(js-tf-x-obj-pairs '[_ obj])])
  => #"Object.entries")

tahto/model/spec_xtalk/fn_js_test.clj:152:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-obj-clone :added "4.0"}
(fact "clones object"
  (l/emit-as :js [(js-tf-x-obj-clone '[_ obj])])
  => #"Object.assign")

tahto/model/spec_xtalk/fn_js_test.clj:157:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-obj-assign :added "4.0"}
(fact "assigns object"
  (l/emit-as :js [(js-tf-x-obj-assign '[_ obj1 obj2])])
  => #"Object.assign")

tahto/model/spec_xtalk/fn_js_test.clj:162:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-push :added "4.0"}
(fact "pushes to array"
  (l/emit-as :js [(js-tf-x-arr-push '[_ arr 1])])
  => #"push")

tahto/model/spec_xtalk/fn_js_test.clj:167:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-pop :added "4.0"}
(fact "pops from array"
  (l/emit-as :js [(js-tf-x-arr-pop '[_ arr])])
  => #"pop")

tahto/model/spec_xtalk/fn_js_test.clj:172:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-push-first :added "4.0"}
(fact "pushes first"
  (l/emit-as :js [(js-tf-x-arr-push-first '[_ arr 1])])
  => #"unshift")

tahto/model/spec_xtalk/fn_js_test.clj:177:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-pop-first :added "4.0"}
(fact "pops first"
  (l/emit-as :js [(js-tf-x-arr-pop-first '[_ arr])])
  => #"shift")

tahto/model/spec_xtalk/fn_js_test.clj:182:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-insert :added "4.0"}
(fact "inserts into array"
  (l/emit-as :js [(js-tf-x-arr-insert '[_ arr 0 1])])
  => #"splice")

tahto/model/spec_xtalk/fn_js_test.clj:187:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-remove :added "4.0"}
(fact "removes from array"
  (l/emit-as :js [(js-tf-x-arr-remove '[_ arr 0])])
  => #"splice")

tahto/model/spec_xtalk/fn_js_test.clj:192:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-slice :added "4.0"}
(fact "slices array"
  (l/emit-as :js [(js-tf-x-arr-slice '[_ arr 0 1])])
  => #"slice")

tahto/model/spec_xtalk/fn_js_test.clj:197:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-reverse :added "4.0"}
(fact "reverses array"
  (l/emit-as :js [(js-tf-x-arr-reverse '[_ arr])])
  => #"reverse")

tahto/model/spec_xtalk/fn_js_test.clj:202:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-concat :added "4.1"}
(fact "concatenates into a new array"
  (l/emit-as :js [(js-tf-x-arr-concat '[_ arr other])])
  => #"concat")

tahto/model/spec_xtalk/fn_js_test.clj:207:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-clone :added "4.0"}
(fact "clones array"
  (l/emit-as :js [(js-tf-x-arr-clone '[_ arr])])
  => #"slice")

tahto/model/spec_xtalk/fn_js_test.clj:212:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-each :added "4.0"}
(fact "iterates array"
  (l/emit-as :js [(js-tf-x-arr-each '[_ arr f])])
  => #"forEach")

tahto/model/spec_xtalk/fn_js_test.clj:217:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-every :added "4.0"}
(fact "checks every element"
  (l/emit-as :js [(js-tf-x-arr-every '[_ arr pred])])
  => #"every")

tahto/model/spec_xtalk/fn_js_test.clj:222:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-some :added "4.0"}
(fact "checks some element"
  (l/emit-as :js [(js-tf-x-arr-some '[_ arr pred])])
  => #"some")

tahto/model/spec_xtalk/fn_js_test.clj:227:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-map :added "4.0"}
(fact "maps array"
  (l/emit-as :js [(js-tf-x-arr-map '[_ arr f])])
  => #"map")

tahto/model/spec_xtalk/fn_js_test.clj:232:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-filter :added "4.0"}
(fact "filters array"
  (l/emit-as :js [(js-tf-x-arr-filter '[_ arr pred])])
  => #"filter")

tahto/model/spec_xtalk/fn_js_test.clj:237:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-foldl :added "4.0"}
(fact "reduces from the left"
  (l/emit-as :js [(js-tf-x-arr-foldl '[_ arr f init])])
  => #"reduce")

tahto/model/spec_xtalk/fn_js_test.clj:242:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-foldr :added "4.0"}
(fact "reduces from the right"
  (l/emit-as :js [(js-tf-x-arr-foldr '[_ arr f init])])
  => #"reduceRight")

tahto/model/spec_xtalk/fn_js_test.clj:247:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-find :added "4.0"}
(fact "finds array index"
  (l/emit-as :js [(js-tf-x-arr-find '[_ arr pred])])
  => #"findIndex")

tahto/model/spec_xtalk/fn_js_test.clj:252:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-arr-sort :added "4.0"}
(fact "sorts array"
  (l/emit-as :js [(js-tf-x-arr-sort '[_ arr key-fn comp-fn])])
  => #"sort")

tahto/model/spec_xtalk/fn_js_test.clj:257:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-char :added "4.0"}
(fact "gets char"
  (l/emit-as :js [(js-tf-x-str-char '[_ s 0])])
  => #"charCodeAt")

tahto/model/spec_xtalk/fn_js_test.clj:262:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-split :added "4.0"}
(fact "splits string"
  (l/emit-as :js [(js-tf-x-str-split '[_ s " "])])
  => #"split")

tahto/model/spec_xtalk/fn_js_test.clj:267:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-join :added "4.0"}
(fact "joins string"
  (l/emit-as :js [(js-tf-x-str-join '[_ s arr])])
  => #"join")

tahto/model/spec_xtalk/fn_js_test.clj:272:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-index-of :added "4.0"}
(fact "index of"
  (l/emit-as :js [(js-tf-x-str-index-of '[_ s "a"])])
  => #"indexOf")

tahto/model/spec_xtalk/fn_js_test.clj:277:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-substring :added "4.0"}
(fact "substring"
  (l/emit-as :js [(js-tf-x-str-substring '[_ s 0 1])])
  => #"substring")

tahto/model/spec_xtalk/fn_js_test.clj:282:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-to-upper :added "4.0"}
(fact "to upper"
  (l/emit-as :js [(js-tf-x-str-to-upper '[_ s])])
  => #"toUpperCase")

tahto/model/spec_xtalk/fn_js_test.clj:287:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-to-lower :added "4.0"}
(fact "to lower"
  (l/emit-as :js [(js-tf-x-str-to-lower '[_ s])])
  => #"toLowerCase")

tahto/model/spec_xtalk/fn_js_test.clj:292:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-to-fixed :added "4.0"}
(fact "to fixed"
  (l/emit-as :js [(js-tf-x-str-to-fixed '[_ n 2])])
  => #"toFixed")

tahto/model/spec_xtalk/fn_js_test.clj:297:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-replace :added "4.0"}
(fact "replaces string"
  (l/emit-as :js [(js-tf-x-str-replace '[_ s "a" "b"])])
  => #"replace")

tahto/model/spec_xtalk/fn_js_test.clj:302:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-trim :added "4.0"}
(fact "trims string"
  (l/emit-as :js [(js-tf-x-str-trim '[_ s])])
  => #"trim")

tahto/model/spec_xtalk/fn_js_test.clj:307:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-trim-left :added "4.0"}
(fact "trims left"
  (l/emit-as :js [(js-tf-x-str-trim-left '[_ s])])
  => #"trimLeft")

tahto/model/spec_xtalk/fn_js_test.clj:312:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-trim-right :added "4.0"}
(fact "trims right"
  (l/emit-as :js [(js-tf-x-str-trim-right '[_ s])])
  => #"trimRight")

tahto/model/spec_xtalk/fn_js_test.clj:317:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-comp :added "4.0"}
(fact "compares strings"
  (l/emit-as :js [(js-tf-x-str-comp '[_ a b])])
  => #"localeCompare")

tahto/model/spec_xtalk/fn_js_test.clj:322:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-pad-left :added "4.1"}
(fact "pads strings on the left")

tahto/model/spec_xtalk/fn_js_test.clj:325:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-pad-right :added "4.1"}
(fact "pads strings on the right")

tahto/model/spec_xtalk/fn_js_test.clj:328:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-starts-with :added "4.1"}
(fact "checks string prefix")

tahto/model/spec_xtalk/fn_js_test.clj:331:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-str-ends-with :added "4.1"}
(fact "checks string suffix")

tahto/model/spec_xtalk/fn_js_test.clj:334:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-return-encode :added "4.0"}
(fact "encodes return"
  (l/emit-as :js [(js-tf-x-return-encode '[_ out id key])])
  => #"JSON.stringify")

tahto/model/spec_xtalk/fn_js_test.clj:339:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-return-wrap :added "4.0"}
(fact "wraps return"
  (l/emit-as :js [(js-tf-x-return-wrap '[_ f encode-fn])])
  => #"try")

tahto/model/spec_xtalk/fn_js_test.clj:344:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-return-eval :added "4.0"}
(fact "evals return"
  (l/emit-as :js [(js-tf-x-return-eval '[_ s wrap-fn])])
  => #"eval")

tahto/model/spec_xtalk/fn_js_test.clj:349:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-socket-connect :added "4.0"}
(fact "connects socket"
  (l/emit-as :js [(js-tf-x-socket-connect '[_ host port opts cb])])
  => #"net.Socket")

tahto/model/spec_xtalk/fn_js_test.clj:354:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-socket-send :added "4.0"}
(fact "sends socket"
  (l/emit-as :js [(js-tf-x-socket-send '[_ conn s])])
  => #"write")

tahto/model/spec_xtalk/fn_js_test.clj:359:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-socket-close :added "4.0"}
(fact "closes socket"
  (l/emit-as :js [(js-tf-x-socket-close '[_ conn])])
  => #"end")

tahto/model/spec_xtalk/fn_js_test.clj:364:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-notify-http :added "4.0"}
(fact "notify http"
  (comment
    (l/emit-as :js [(js-tf-x-notify-http '[_ host port value id key opts])])
    => #"fetch"))

tahto/model/spec_xtalk/fn_js_test.clj:370:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :js [(js-tf-x-iter-from-obj '[_ obj])])
  => #"Symbol.iterator")

tahto/model/spec_xtalk/fn_js_test.clj:375:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :js [(js-tf-x-iter-from-arr '[_ arr])])
  => #"Symbol.iterator")

tahto/model/spec_xtalk/fn_js_test.clj:380:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :js [(js-tf-x-iter-from '[_ obj])])
  => #"Symbol.iterator")

tahto/model/spec_xtalk/fn_js_test.clj:385:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :js [(js-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"next")

tahto/model/spec_xtalk/fn_js_test.clj:390:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :js [(js-tf-x-iter-next '[_ it])])
  => #"next")

tahto/model/spec_xtalk/fn_js_test.clj:395:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :js [(js-tf-x-iter-has? '[_ obj])])
  => #"Symbol.iterator")

tahto/model/spec_xtalk/fn_js_test.clj:400:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :js [(js-tf-x-iter-native? '[_ it])])
  => #"next")

tahto/model/spec_xtalk/fn_js_test.clj:405:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-async-run :added "4.1"}
(fact "async run emits a native promise start"
  (l/emit-as :js [(js-tf-x-async-run '[_ thunk])])
  => #"(?s)new Promise.*setTimeout.*Promise\.resolve.*thunk")

tahto/model/spec_xtalk/fn_js_test.clj:410:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :js [(js-tf-x-with-delay '[_ 100 thunk])])
  => #"(?s)setTimeout.*new Promise")

tahto/model/spec_xtalk/fn_js_test.clj:415:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise :added "4.1"}
(fact "transforms x:promise")

tahto/model/spec_xtalk/fn_js_test.clj:418:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-all :added "4.1"}
(fact "transforms x:promise-all")

tahto/model/spec_xtalk/fn_js_test.clj:421:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-then :added "4.1"}
(fact "transforms x:promise-then")

tahto/model/spec_xtalk/fn_js_test.clj:424:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-catch :added "4.1"}
(fact "transforms x:promise-catch")

tahto/model/spec_xtalk/fn_js_test.clj:427:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-finally :added "4.1"}
(fact "transforms x:promise-finally")

tahto/model/spec_xtalk/fn_js_test.clj:430:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-native? :added "4.1"}
(fact "transforms x:promise-native?")

tahto/model/spec_xtalk/fn_js_test.clj:433:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-pwd :added "4.1"}
(fact "gets working directory")

tahto/model/spec_xtalk/fn_js_test.clj:436:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-shell :added "4.0"}
(fact "executes shell command"
  (l/emit-as :js [(js-tf-x-shell '[_ "ls" opts cb])])
  => #"child_process"

  (l/emit-as :js [(js-tf-x-shell '[_ "ls" opts cb])])
  => #"\[\"async\"\]")

tahto/model/spec_xtalk/fn_js_test.clj:444:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

tahto/model/spec_xtalk/fn_js_test.clj:447:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-file-read :added "4.1"}
(fact "read file"
  (l/emit-as :js [(js-tf-x-file-read '[_ filename])])
  => #"readFile"

  (l/emit-as :js [(js-tf-x-file-read '[_ filename])])
  => #"promises")

tahto/model/spec_xtalk/fn_js_test.clj:455:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-file-write :added "4.1"}
(fact "write file"
  (l/emit-as :js [(js-tf-x-file-write '[_ filename content])])
  => #"writeFile"

  (l/emit-as :js [(js-tf-x-file-write '[_ filename content])])
  => #"promises")

(comment

  ;; -------
  ;; return case
  (return (x:type-native obj))

  ;;
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (return "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (return "object")
                  (return tn))))
        (return t)))
  
  ;; -------
  ;; assign case
  (var a (x:type-native obj))
  (:= a  (x:type-native obj))

  ;;
  (var a nil)
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (:= a "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (:= a "object")
                  (:= a tn))))
        (:= a t)))

  ;; -------
  ;; general usage case
  (f (g (x:type-native obj)))

  (var type-native-fn
       (fn type-native-lambda [obj]
         (when (== obj nil) (return nil))
         (var t := (typeof obj))
         (if (== t "object")
           (cond (Array.isArray obj)
                 (return "array")

                 :else
                 (do
                   (var tn := (. obj ["constructor"] ["name"]))
                   (if (== tn "Object")
                     (return "object")
                     (return tn))))
           (return t))))
  (f (g (type-native-fn obj)))

  ;; -------
  ;; standalone
  x:type-native

  ;;
  (fn type-native-lambda [obj]
    (when (== obj nil) (return nil))
    (var t := (typeof obj))
    (if (== t "object")
      (cond (Array.isArray obj)
            (return "array")

            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (return "object")
                (return tn))))
      (return t)))
  )


tahto/model/spec_xtalk/fn_js_test.clj:548:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-promise-new :added "4.0"}
(fact "creates a new promise"
  (l/emit-as :js [(js-tf-x-promise-new '[_ thunk])])
  => "new Promise(function (resolve,reject){\n  thunk(resolve,reject);\n})")

tahto/model/spec_xtalk/fn_js_test.clj:553:^{:refer tahto.model.spec-xtalk.fn-js/js-tf-x-construct :added "4.1"}
(fact "constructs dynamically through Reflect.construct"
  (js-tf-x-construct '(_ Widget args))
  => '(. Reflect (construct Widget args)))
