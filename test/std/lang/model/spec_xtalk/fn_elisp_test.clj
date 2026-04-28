(ns std.lang.model.spec-xtalk.fn-elisp-test
  (:require [std.lang :as l]
            [std.lang.model.spec-elisp]
            [std.lang.model.spec-xtalk.fn-elisp :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-print :added "4.1"}
(fact "prints values"
  (elisp-tf-x-print '(x:print "hello"))
  => '(princ "hello"))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-len :added "4.1"}
(fact "gets length"
  (elisp-tf-x-len '(x:len arr))
  => '(if (hash-table-p arr)
        (hash-table-count arr)
        (length arr)))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (elisp-tf-x-cat '(x:cat "a" "b"))
  => '(concat "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-apply :added "4.1"}
(fact "applies arguments"
  (elisp-tf-x-apply '(x:apply f args))
  => '(apply f args))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-type-native :added "4.1"}
(fact "detects native type"
  (l/emit-as :elisp [(elisp-tf-x-type-native '(x:type-native obj))])
  => #"hash-table-p")

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-get-key :added "4.1"}
(fact "gets object key"
  (elisp-tf-x-get-key '(x:get-key obj key fallback))
  => '(gethash key obj fallback))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-has-key? :added "4.1"}
(fact "checks object key"
  (l/emit-as :elisp [(elisp-tf-x-has-key? '(x:has-key? obj key expected))])
  => #"gethash")

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-obj-keys :added "4.1"}
(fact "lists object keys"
  (elisp-tf-x-obj-keys '(x:obj-keys obj))
  => '(hash-table-keys obj))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-get-idx :added "4.1"}
(fact "gets array index"
  (l/emit-as :elisp [(elisp-tf-x-get-idx '(x:get-idx arr idx fallback))])
  => #"aref")

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-arr-push :added "4.1"}
(fact "pushes array values"
  (elisp-tf-x-arr-push '(x:arr-push arr value))
  => '(vconcat arr (vector value)))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (elisp-tf-x-str-join '(x:str-join sep coll))
  => '(mapconcat identity coll sep))

^{:refer std.lang.model.spec-xtalk.fn-elisp/elisp-tf-x-m-pow :added "4.1"}
(fact "powers numbers"
  (elisp-tf-x-m-pow '(x:m-pow base exp))
  => '(expt base exp))
