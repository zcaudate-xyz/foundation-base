(ns hara.model.spec-xtalk.fn-scheme-test
  (:require [hara.lang :as l]
             [hara.model.spec-scheme]
             [hara.model.spec-xtalk.fn-scheme :refer :all])
  (:use code.test))

^{:refer hara.model.spec-xtalk.fn-scheme/+scheme-promise+ :added "4.1"}
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

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-begin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-vector-slice :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-vector->list :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-if-chain :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-promise-native-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-promise-rejected-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-promise-wrap-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-print :added "4.1"}
(fact "prints values"
  (scheme-tf-x-print '(x:print "hello"))
  => '(begin (display "hello") false))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-len :added "4.1"}
(fact "gets length"
  (scheme-tf-x-len '(x:len arr))
  => '(if (vector? arr)
        (vector-length arr)
        (if (string? arr)
          (string-length arr)
          (if (hash? arr)
            (hash-count arr)
            (length arr)))))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (scheme-tf-x-cat '(x:cat "a" "b"))
  => '(string-append "a" "b"))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (scheme-tf-x-apply '(x:apply f args))
  => '(apply f (if (vector? args) (vector->list args) args)))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-div :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-err :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-eval :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-random :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-now-ms :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-new :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-message :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-ex-data :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :scheme [(scheme-tf-x-type-native '(x:type-native obj))])
  => #"procedure\\?")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-global-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-global-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-global-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-to-string :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-to-number :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-string? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-number? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-integer? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-boolean? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-function? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-object? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-is-array? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-lu-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (scheme-tf-x-get-key '(x:get-key obj key fallback))
  => '(hash-ref obj key (lambda () fallback)))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-get-path :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (scheme-tf-x-has-key? '(x:has-key? obj key expected))
  => '(and (hash-has-key? obj key)
           (equal? expected (hash-ref obj key))))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-del-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-set-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-copy-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (scheme-tf-x-obj-keys '(x:obj-keys obj))
  => '(list->vector (hash-keys obj)))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-vals :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-pairs :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-clone :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-assign :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :scheme [(scheme-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"vector-ref")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-set-idx :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-clone :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-slice :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-reverse :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-concat :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (scheme-tf-x-arr-push '(x:arr-push arr value))
  => '(begin (set! arr (vector-append arr (vector value))) arr))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-pop :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-pop-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-insert :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-remove :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-assign :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-each :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-every :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-some :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-map :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-filter :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-foldl :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-foldr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-sort :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-comp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-char :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-len :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-split :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (scheme-tf-x-str-join '(x:str-join sep coll))
  => '(string-join (if (vector? coll) (vector->list coll) coll) sep))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-index-of :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-substring :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-upper :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-lower :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-to-fixed :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-replace :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim-left :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-trim-right :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-pad-left :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-pad-right :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-starts-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-str-ends-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-abs :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-acos :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-asin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-atan :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-max :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-min :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-mod :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-quot :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-floor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-ceil :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-cos :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-cosh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-exp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-loge :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-log10 :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sinh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-sqrt :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-tan :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-tanh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (scheme-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-and :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-or :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-xor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-lshift :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-bit-rshift :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-json-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-json-decode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-return-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-return-wrap :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-return-eval :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from-arr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-from :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-next :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-null :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-iter-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-prototype-method :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-async-run :added "4.1"}
(fact "scheme async run emits a lower-level thread primitive"
  (l/emit-as :scheme [(scheme-tf-x-async-run '[_ thunk])])
  => #"thread")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-all :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-then :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-catch :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-finally :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-promise-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-with-delay :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-connect :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-send :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-socket-close :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-notify-http :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-pwd :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-shell :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-file-resolve :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-file-slurp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-scheme/scheme-tf-x-file-spit :added "4.1"}
(fact "TODO")