(ns hara.lang.model.spec-xtalk.fn-scheme-test
  (:require [hara.lang :as l]
             [hara.lang.model.spec-scheme]
             [hara.lang.model.spec-xtalk.fn-scheme :refer :all])
  (:use code.test))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/+scheme-promise+ :added "4.1"}
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

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-async-run :added "4.1"}
(fact "scheme async run emits a lower-level thread primitive"
  (l/emit-as :scheme [(scheme-tf-x-async-run '[_ thunk])])
  => #"thread")

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-print :added "4.1"}
(fact "prints values"
  (scheme-tf-x-print '(x:print "hello"))
  => '(begin (display "hello") false))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-len :added "4.1"}
(fact "gets length"
  (scheme-tf-x-len '(x:len arr))
  => '(if (vector? arr)
        (vector-length arr)
        (if (string? arr)
          (string-length arr)
          (if (hash? arr)
            (hash-count arr)
            (length arr)))))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (scheme-tf-x-cat '(x:cat "a" "b"))
  => '(string-append "a" "b"))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (scheme-tf-x-apply '(x:apply f args))
  => '(apply f (if (vector? args) (vector->list args) args)))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :scheme [(scheme-tf-x-type-native '(x:type-native obj))])
  => #"procedure\\?")

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (scheme-tf-x-get-key '(x:get-key obj key fallback))
  => '(hash-ref obj key (lambda () fallback)))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (scheme-tf-x-has-key? '(x:has-key? obj key expected))
  => '(and (hash-has-key? obj key)
           (equal? expected (hash-ref obj key))))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (scheme-tf-x-obj-keys '(x:obj-keys obj))
  => '(list->vector (hash-keys obj)))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :scheme [(scheme-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"vector-ref")

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (scheme-tf-x-arr-push '(x:arr-push arr value))
  => '(begin (set! arr (vector-append arr (vector value))) arr))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (scheme-tf-x-str-join '(x:str-join sep coll))
  => '(string-join (if (vector? coll) (vector->list coll) coll) sep))

^{:refer hara.lang.model.spec-xtalk.fn-scheme/scheme-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (scheme-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))
