(ns std.lang.model.spec-xtalk.fn-scheme-test
  (:require [std.lang :as l]
             [std.lang.model.spec-scheme]
             [std.lang.model.spec-xtalk.fn-scheme :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-xtalk.fn-scheme/+scheme-promise+ :added "4.1"}
(fact "scheme async run stays local while promise ops hard-link to common-promise"
  [(get-in +scheme-promise+ [:x-async-run :macro])
   (get-in +scheme-promise+ [:x-promise :raw])
   (get-in +scheme-promise+ [:x-promise-then :raw])
   (get-in +scheme-promise+ [:x-promise-catch :raw])
   (get-in +scheme-promise+ [:x-promise-finally :raw])
   (get-in +scheme-promise+ [:x-promise-native? :raw])]
  => [#'scheme-tf-x-async-run
      'xt.lang.common-promise/promise
      'xt.lang.common-promise/promise-then
      'xt.lang.common-promise/promise-catch
      'xt.lang.common-promise/promise-finally
      'xt.lang.common-promise/promise-native?])

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-async-run :added "4.1"}
(fact "scheme async run emits a lower-level thread primitive"
  (l/emit-as :scheme [(scheme-tf-x-async-run '[_ thunk])])
  => #"thread")

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-print :added "4.1"}
(fact "prints values"
  (scheme-tf-x-print '(x:print "hello"))
  => '(display "hello"))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-len :added "4.1"}
(fact "gets length"
  (scheme-tf-x-len '(x:len arr))
  => '(cond ((vector? arr) (vector-length arr))
            ((string? arr) (string-length arr))
            ((hash? arr) (hash-count arr))
            (else (length arr))))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (scheme-tf-x-cat '(x:cat "a" "b"))
  => '(string-append "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (scheme-tf-x-apply '(x:apply f args))
  => '(apply f args))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :scheme [(scheme-tf-x-type-native '(x:type-native obj))])
  => #"procedure\\?")

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (scheme-tf-x-get-key '(x:get-key obj key fallback))
  => '(hash-ref obj key fallback))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (scheme-tf-x-has-key? '(x:has-key? obj key expected))
  => '(and (hash-has-key? obj key)
           (equal? expected (hash-ref obj key))))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (scheme-tf-x-obj-keys '(x:obj-keys obj))
  => '(hash-keys obj))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :scheme [(scheme-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"vector-ref")

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (scheme-tf-x-arr-push '(x:arr-push arr value))
  => '(vector-append arr (vector value)))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (scheme-tf-x-str-join '(x:str-join sep coll))
  => '(string-join coll sep))

^{:refer std.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (scheme-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))
