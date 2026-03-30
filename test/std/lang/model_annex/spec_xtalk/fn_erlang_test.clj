(ns std.lang.model-annex.spec-xtalk.fn-erlang-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-erlang :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-fn :added "4.1"}
(fact "creates an erlang function"
  (erlang-tf-x-fn '(:x-fn [x] (+ x 1)))
  => '(fn [x] (+ x 1)))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-abs :added "4.1"}
(fact "returns the absolute value"
  (erlang-tf-x-m-abs '(:x-m-abs -5))
  => '(abs -5))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-acos :added "4.1"}
(fact "returns the arccosine"
  (erlang-tf-x-m-acos '(:x-m-acos 0.5))
  => '(:call "math" "acos" 0.5))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-asin :added "4.1"}
(fact "returns the arcsine"
  (erlang-tf-x-m-asin '(:x-m-asin 0.5))
  => '(:call "math" "asin" 0.5))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-atan :added "4.1"}
(fact "returns the arctangent"
  (erlang-tf-x-m-atan '(:x-m-atan 1))
  => '(:call "math" "atan" 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-ceil :added "4.1"}
(fact "returns the ceiling"
  (erlang-tf-x-m-ceil '(:x-m-ceil 3.14))
  => '(:call "math" "ceil" 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-cos :added "4.1"}
(fact "returns the cosine"
  (erlang-tf-x-m-cos '(:x-m-cos 0))
  => '(:call "math" "cos" 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-cosh :added "4.1"}
(fact "returns the hyperbolic cosine"
  (erlang-tf-x-m-cosh '(:x-m-cosh 1))
  => '(:call "math" "cosh" 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-exp :added "4.1"}
(fact "returns e raised to the power"
  (erlang-tf-x-m-exp '(:x-m-exp 1))
  => '(:call "math" "exp" 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-floor :added "4.1"}
(fact "returns the floor"
  (erlang-tf-x-m-floor '(:x-m-floor 3.14))
  => '(:call "math" "floor" 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-loge :added "4.1"}
(fact "returns natural logarithm"
  (erlang-tf-x-m-loge '(:x-m-loge 10))
  => '(:call "math" "log" 10))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-log10 :added "4.1"}
(fact "returns base-10 logarithm"
  (erlang-tf-x-m-log10 '(:x-m-log10 100))
  => '(:call "math" "log10" 100))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-max :added "4.1"}
(fact "returns maximum value"
  (erlang-tf-x-m-max '(:x-m-max 1 2 3))
  => '(max 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-min :added "4.1"}
(fact "returns minimum value"
  (erlang-tf-x-m-min '(:x-m-min 1 2 3))
  => '(min 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-mod :added "4.1"}
(fact "returns remainder"
  (erlang-tf-x-m-mod '(:x-m-mod 10 3))
  => '(rem 10 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-pow :added "4.1"}
(fact "returns power"
  (erlang-tf-x-m-pow '(:x-m-pow 2 3))
  => '(:call "math" "pow" 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-quot :added "4.1"}
(fact "returns quotient"
  (erlang-tf-x-m-quot '(:x-m-quot 10 3))
  => '(div 10 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-sin :added "4.1"}
(fact "returns sine"
  (erlang-tf-x-m-sin '(:x-m-sin 0))
  => '(:call "math" "sin" 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-sinh :added "4.1"}
(fact "returns hyperbolic sine"
  (erlang-tf-x-m-sinh '(:x-m-sinh 1))
  => '(:call "math" "sinh" 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-sqrt :added "4.1"}
(fact "returns square root"
  (erlang-tf-x-m-sqrt '(:x-m-sqrt 16))
  => '(:call "math" "sqrt" 16))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-tan :added "4.1"}
(fact "returns tangent"
  (erlang-tf-x-m-tan '(:x-m-tan 0))
  => '(:call "math" "tan" 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-m-tanh :added "4.1"}
(fact "returns hyperbolic tangent"
  (erlang-tf-x-m-tanh '(:x-m-tanh 1))
  => '(:call "math" "tanh" 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-to-string :added "4.1"}
(fact "converts to string"
  (erlang-tf-x-to-string '(:x-to-string 123))
  => '(:call "integer_to_list" 123))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-to-number :added "4.1"}
(fact "converts to number"
  (erlang-tf-x-to-number '(:x-to-number "123"))
  => '(:call "list_to_integer" "123"))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-string? :added "4.1"}
(fact "checks if string"
  (erlang-tf-x-is-string? '(:x-is-string? x))
  => '(:call "is_list" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-number? :added "4.1"}
(fact "checks if number"
  (erlang-tf-x-is-number? '(:x-is-number? x))
  => '(:call "is_number" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-integer? :added "4.1"}
(fact "checks if integer"
  (erlang-tf-x-is-integer? '(:x-is-integer? x))
  => '(:call "is_integer" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-boolean? :added "4.1"}
(fact "checks if boolean"
  (erlang-tf-x-is-boolean? '(:x-is-boolean? x))
  => '(:call "is_boolean" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-function? :added "4.1"}
(fact "checks if function"
  (erlang-tf-x-is-function? '(:x-is-function? x))
  => '(:call "is_function" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-object? :added "4.1"}
(fact "checks if map/object"
  (erlang-tf-x-is-object? '(:x-is-object? x))
  => '(:call "is_map" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-is-array? :added "4.1"}
(fact "checks if list/array"
  (erlang-tf-x-is-array? '(:x-is-array? x))
  => '(:call "is_list" x))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-obj-keys :added "4.1"}
(fact "gets map keys"
  (erlang-tf-x-obj-keys '(:x-obj-keys obj))
  => '(:call "maps" "keys" obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-obj-vals :added "4.1"}
(fact "gets map values"
  (erlang-tf-x-obj-vals '(:x-obj-vals obj))
  => '(:call "maps" "values" obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-obj-pairs :added "4.1"}
(fact "gets map pairs"
  (erlang-tf-x-obj-pairs '(:x-obj-pairs obj))
  => '(:call "maps" "to_list" obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-obj-clone :added "4.1"}
(fact "clones map (identity for immutable)"
  (erlang-tf-x-obj-clone '(:x-obj-clone obj))
  => 'obj)

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-clone :added "4.1"}
(fact "clones list (identity for immutable)"
  (erlang-tf-x-arr-clone '(:x-arr-clone arr))
  => 'arr)

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-slice :added "4.1"}
(fact "gets sublist"
  (erlang-tf-x-arr-slice '(:x-arr-slice arr 0 5))
  => '(:call "lists" "sublist" arr (+ 0 1) (- 5 0)))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-push :added "4.1"}
(fact "appends to list"
  (erlang-tf-x-arr-push '(:x-arr-push arr item))
  => '(:call "lists" "append" arr (list item)))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-pop :added "4.1"}
(fact "removes last element"
  (erlang-tf-x-arr-pop '(:x-arr-pop arr))
  => '(:call "lists" "droplast" arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-reverse :added "4.1"}
(fact "reverses list"
  (erlang-tf-x-arr-reverse '(:x-arr-reverse arr))
  => '(:call "lists" "reverse" arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-push-first :added "4.1"}
(fact "prepends to list"
  (erlang-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(list* item arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-pop-first :added "4.1"}
(fact "removes first element"
  (erlang-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(tl arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-insert :added "4.1"}
(fact "inserts at index"
  (erlang-tf-x-arr-insert '(:x-arr-insert arr 2 item))
  => '(let [(tuple L1 L2) (:call "lists" "split" 2 arr)]
        (:call "lists" "append" L1 (:call "lists" "append" (list item) L2))))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-arr-sort :added "4.1"}
(fact "sorts list"
  (erlang-tf-x-arr-sort '(:x-arr-sort arr key-fn compare-fn))
  => '(:call "lists" "sort" arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-char :added "4.1"}
(fact "gets char at index"
  (erlang-tf-x-str-char '(:x-str-char s 0))
  => '(:call "lists" "nth" (+ 0 1) s))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-split :added "4.1"}
(fact "splits string"
  (erlang-tf-x-str-split '(:x-str-split s ","))
  => '(:call "string" "split" s "," "all"))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (erlang-tf-x-str-join '(:x-str-join "," arr))
  => '(:call "string" "join" arr ","))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-index-of :added "4.1"}
(fact "finds substring index"
  (erlang-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(:call "string" "str" s "abc"))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-substring :added "4.1"}
(fact "extracts substring"
  (erlang-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(:call "string" "slice" s 0 (- 5 0)))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-to-upper :added "4.1"}
(fact "converts to uppercase"
  (erlang-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(:call "string" "to_upper" s))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-to-lower :added "4.1"}
(fact "converts to lowercase"
  (erlang-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(:call "string" "to_lower" s))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-str-replace :added "4.1"}
(fact "replaces in string"
  (erlang-tf-x-str-replace '(:x-str-replace s "old" "new"))
  => '(:call "string" "replace" s "old" "new" "all"))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-json-encode :added "4.1"}
(fact "encodes to json"
  (erlang-tf-x-json-encode '(:x-json-encode obj))
  => '(:call "json" "encode" obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-json-decode :added "4.1"}
(fact "decodes from json"
  (erlang-tf-x-json-decode '(:x-json-decode s))
  => '(:call "json" "decode" s))

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-return-encode :added "4.1"}
(fact "encodes return value"
  (erlang-tf-x-return-encode '(:x-return-encode out id key))
  => anything)

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-return-wrap :added "4.1"}
(fact "wraps function with error handling"
  (erlang-tf-x-return-wrap '(:x-return-wrap f encode-fn))
  => anything)

^{:refer std.lang.model-annex.spec-xtalk.fn-erlang/erlang-tf-x-return-eval :added "4.1"}
(fact "evaluates with wrapper"
  (erlang-tf-x-return-eval '(:x-return-eval s wrap-fn))
  => anything)
