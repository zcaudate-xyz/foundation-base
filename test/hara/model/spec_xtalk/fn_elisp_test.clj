(ns hara.model.spec-xtalk.fn-elisp-test
  (:require [hara.lang :as l]
             [hara.model.spec-elisp]
             [hara.model.spec-xtalk.fn-elisp :refer :all])
  (:use code.test))

^{:refer hara.model.spec-xtalk.fn-elisp/+elisp-promise+ :added "4.1"}
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

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-begin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-vector-subseq :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-vector->list :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-print :added "4.1"}
(fact "prints values"
  (elisp-tf-x-print '(x:print "hello"))
  => '(progn (princ "hello") nil))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-len :added "4.1"}
(fact "gets length"
  (elisp-tf-x-len '(x:len arr))
  => '(if (vectorp arr)
        (length arr)
        (if (hash-table-p arr)
          (hash-table-count arr)
          (length arr))))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (elisp-tf-x-cat '(x:cat "a" "b"))
  => '(concat "a" "b"))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (elisp-tf-x-apply '(x:apply f args))
  => '(apply f (append args nil)))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-div :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-err :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-eval :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-random :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-now-ms :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-new :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-message :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-ex-data :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :elisp [(elisp-tf-x-type-native '(x:type-native obj))])
  => #"hash-table-p")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-global-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-global-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-global-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-to-string :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-to-number :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-string? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-number? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-integer? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-boolean? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-function? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-object? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-is-array? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-lu-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (elisp-tf-x-get-key '(x:get-key obj key fallback))
  => '(gethash key obj fallback))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-get-path :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (l/emit-as :elisp [(elisp-tf-x-has-key? '(x:has-key? obj key expected))])
  => #"gethash")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-del-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-set-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-copy-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (elisp-tf-x-obj-keys '(x:obj-keys obj))
  => '(vconcat (hash-table-keys obj)))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-vals :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-pairs :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-clone :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-assign :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :elisp [(elisp-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"aref")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-set-idx :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-clone :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-slice :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-reverse :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-concat :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (elisp-tf-x-arr-push '(x:arr-push arr value))
  => '(progn (setq arr (vconcat arr (vector value))) arr))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-pop :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-push-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-pop-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-insert :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-remove :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-assign :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-some :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-each :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-every :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-map :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-filter :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-foldl :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-foldr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-sort :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-comp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-char :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-split :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-len :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (elisp-tf-x-str-join '(x:str-join sep coll))
  => '(mapconcat (lambda (x) x) (append coll nil) sep))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-index-of :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-substring :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-upper :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-lower :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-to-fixed :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-replace :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim-left :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-trim-right :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-pad-left :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-pad-right :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-starts-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-str-ends-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-abs :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-acos :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-asin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-atan :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-max :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-min :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-mod :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-quot :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-floor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-ceil :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-cos :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-cosh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-exp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-loge :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-log10 :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sin :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sinh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-sqrt :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-tan :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-tanh :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (elisp-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-json-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-json-decode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-return-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-return-wrap :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-return-eval :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-and :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-or :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-xor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-lshift :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-bit-rshift :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from-arr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-from :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-next :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-null :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-iter-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-prototype-method :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-async-run :added "4.1"}
(fact "elisp async run emits a lower-level thread primitive"
  (l/emit-as :elisp [(elisp-tf-x-async-run '[_ thunk])])
  => #"make-thread")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-all :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-then :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-catch :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-finally :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-promise-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-with-delay :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-connect :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-send :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-socket-close :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-notify-http :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-pwd :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-shell :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-file-resolve :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-file-slurp :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-xtalk.fn-elisp/elisp-tf-x-file-spit :added "4.1"}
(fact "TODO")