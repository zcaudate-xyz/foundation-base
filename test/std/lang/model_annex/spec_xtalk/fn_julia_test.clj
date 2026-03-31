(ns std.lang.model-annex.spec-xtalk.fn-julia-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-julia :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-del :added "4.1"}
(fact "deletes an element from a collection"
  (julia-tf-x-del '(:x-del obj))
  => '(delete! obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-cat :added "4.1"}
(fact "concatenates strings or arrays"
  (julia-tf-x-cat '(:x-cat "a" "b"))
  => '(* "a" "b")

  (julia-tf-x-cat '(:x-cat "a" "b" "c"))
  => '(* "a" "b" "c"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-len :added "4.1"}
(fact "returns the length of an array or string"
  (julia-tf-x-len '(:x-len arr))
  => '(length arr)

  (julia-tf-x-len '(:x-len "hello"))
  => '(length "hello"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-get-key :added "4.1"}
(fact "gets a value from a dict with a default"
  (julia-tf-x-get-key '(:x-get-key obj key default))
  => '(get obj key default))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-err :added "4.1"}
(fact "throws an error with a message"
  (julia-tf-x-err '(:x-err "message"))
  => '(error "message"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-eval :added "4.1"}
(fact "evaluates a string as Julia code"
  (julia-tf-x-eval '(:x-eval "1 + 1"))
  => '(eval (Meta.parse "1 + 1")))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-apply :added "4.1"}
(fact "applies a function to a list of arguments"
  (julia-tf-x-apply '(:x-apply f args))
  => '(f (:... args)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-random :added "4.1"}
(fact "generates a random number"
  (julia-tf-x-random '(:x-random))
  => '(rand))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-print :added "4.1"}
(fact "prints values to stdout"
  (julia-tf-x-print '(:x-print "hello"))
  => '(println "hello")

  (julia-tf-x-print '(:x-print "a" "b"))
  => '(println "a" "b"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-type-native :added "4.1"}
(fact "returns the native type of an object as a string"
  (julia-tf-x-type-native '(:x-type-native obj))
  => '(string (typeof obj)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-abs :added "4.1"}
(fact "returns the absolute value of a number"
  (julia-tf-x-m-abs '(:x-m-abs -5))
  => '(abs -5))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-acos :added "4.1"}
(fact "returns the arccosine of a number"
  (julia-tf-x-m-acos '(:x-m-acos 0.5))
  => '(acos 0.5))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-asin :added "4.1"}
(fact "returns the arcsine of a number"
  (julia-tf-x-m-asin '(:x-m-asin 0.5))
  => '(asin 0.5))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-atan :added "4.1"}
(fact "returns the arctangent of a number"
  (julia-tf-x-m-atan '(:x-m-atan 1))
  => '(atan 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-ceil :added "4.1"}
(fact "returns the ceiling of a number"
  (julia-tf-x-m-ceil '(:x-m-ceil 3.14))
  => '(ceil 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-cos :added "4.1"}
(fact "returns the cosine of a number"
  (julia-tf-x-m-cos '(:x-m-cos 0))
  => '(cos 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-cosh :added "4.1"}
(fact "returns the hyperbolic cosine of a number"
  (julia-tf-x-m-cosh '(:x-m-cosh 1))
  => '(cosh 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-exp :added "4.1"}
(fact "returns e raised to the power of a number"
  (julia-tf-x-m-exp '(:x-m-exp 1))
  => '(exp 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-floor :added "4.1"}
(fact "returns the floor of a number"
  (julia-tf-x-m-floor '(:x-m-floor 3.14))
  => '(floor 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-loge :added "4.1"}
(fact "returns the natural logarithm of a number"
  (julia-tf-x-m-loge '(:x-m-loge 10))
  => '(log 10))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-log10 :added "4.1"}
(fact "returns the base-10 logarithm of a number"
  (julia-tf-x-m-log10 '(:x-m-log10 100))
  => '(log10 100))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-max :added "4.1"}
(fact "returns the maximum of two or more numbers"
  (julia-tf-x-m-max '(:x-m-max 1 2))
  => '(max 1 2)

  (julia-tf-x-m-max '(:x-m-max 1 2 3))
  => '(max 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-min :added "4.1"}
(fact "returns the minimum of two or more numbers"
  (julia-tf-x-m-min '(:x-m-min 1 2))
  => '(min 1 2)

  (julia-tf-x-m-min '(:x-m-min 1 2 3))
  => '(min 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-mod :added "4.1"}
(fact "returns the modulus of two numbers"
  (julia-tf-x-m-mod '(:x-m-mod 10 3))
  => '(:% 10 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-pow :added "4.1"}
(fact "returns base raised to the power of n"
  (julia-tf-x-m-pow '(:x-m-pow 2 3))
  => (list (symbol "^") 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-quot :added "4.1"}
(fact "returns the quotient of two numbers"
  (julia-tf-x-m-quot '(:x-m-quot 10 3))
  => '(div 10 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-sin :added "4.1"}
(fact "returns the sine of a number"
  (julia-tf-x-m-sin '(:x-m-sin 0))
  => '(sin 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-sinh :added "4.1"}
(fact "returns the hyperbolic sine of a number"
  (julia-tf-x-m-sinh '(:x-m-sinh 1))
  => '(sinh 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-sqrt :added "4.1"}
(fact "returns the square root of a number"
  (julia-tf-x-m-sqrt '(:x-m-sqrt 16))
  => '(sqrt 16))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-tan :added "4.1"}
(fact "returns the tangent of a number"
  (julia-tf-x-m-tan '(:x-m-tan 0))
  => '(tan 0))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-tanh :added "4.1"}
(fact "returns the hyperbolic tangent of a number"
  (julia-tf-x-m-tanh '(:x-m-tanh 1))
  => '(tanh 1))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-to-string :added "4.1"}
(fact "converts a value to a string"
  (julia-tf-x-to-string '(:x-to-string 123))
  => '(string 123))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-to-number :added "4.1"}
(fact "converts a string to a number"
  (julia-tf-x-to-number '(:x-to-number "123.45"))
  => '(parse Float64 "123.45"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-string? :added "4.1"}
(fact "checks if a value is a string"
  (julia-tf-x-is-string? '(:x-is-string? x))
  => '(isa x String))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-number? :added "4.1"}
(fact "checks if a value is a number"
  (julia-tf-x-is-number? '(:x-is-number? x))
  => '(isa x Number))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-integer? :added "4.1"}
(fact "checks if a value is an integer"
  (julia-tf-x-is-integer? '(:x-is-integer? x))
  => '(isa x Integer))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-boolean? :added "4.1"}
(fact "checks if a value is a boolean"
  (julia-tf-x-is-boolean? '(:x-is-boolean? x))
  => '(isa x Bool))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-function? :added "4.1"}
(fact "checks if a value is a function"
  (julia-tf-x-is-function? '(:x-is-function? x))
  => '(isa x Function))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-object? :added "4.1"}
(fact "checks if a value is a dict/object"
  (julia-tf-x-is-object? '(:x-is-object? x))
  => '(isa x Dict))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-array? :added "4.1"}
(fact "checks if a value is an array"
  (julia-tf-x-is-array? '(:x-is-array? x))
  => '(isa x AbstractArray))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-obj-keys :added "4.1"}
(fact "returns the keys of a dict as an array"
  (julia-tf-x-obj-keys '(:x-obj-keys obj))
  => '(collect (keys obj)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-obj-vals :added "4.1"}
(fact "returns the values of a dict as an array"
  (julia-tf-x-obj-vals '(:x-obj-vals obj))
  => '(collect (values obj)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-obj-pairs :added "4.1"}
(fact "returns the key-value pairs of a dict"
  (julia-tf-x-obj-pairs '(:x-obj-pairs obj))
  => '(collect obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-obj-clone :added "4.1"}
(fact "clones a dict"
  (julia-tf-x-obj-clone '(:x-obj-clone obj))
  => '(copy obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-clone :added "4.1"}
(fact "clones an array"
  (julia-tf-x-arr-clone '(:x-arr-clone arr))
  => '(copy arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-slice :added "4.1"}
(fact "returns a slice of an array"
  (julia-tf-x-arr-slice '(:x-arr-slice arr 0 5))
  => '(getindex arr (:to (+ 0 1) 5)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-push :added "4.1"}
(fact "pushes an item to the end of an array"
  (julia-tf-x-arr-push '(:x-arr-push arr item))
  => '(push! arr item))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-pop :added "4.1"}
(fact "pops an item from the end of an array"
  (julia-tf-x-arr-pop '(:x-arr-pop arr))
  => '(pop! arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-reverse :added "4.1"}
(fact "reverses an array"
  (julia-tf-x-arr-reverse '(:x-arr-reverse arr))
  => '(reverse arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-push-first :added "4.1"}
(fact "pushes an item to the beginning of an array"
  (julia-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(pushfirst! arr item))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-pop-first :added "4.1"}
(fact "pops an item from the beginning of an array"
  (julia-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(popfirst! arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-insert :added "4.1"}
(fact "inserts an element at a specific index"
  (julia-tf-x-arr-insert '(:x-arr-insert arr 2 item))
  => '(insert! arr (+ 2 1) item))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-sort :added "4.1"}
(fact "sorts an array with optional key function"
  (julia-tf-x-arr-sort '(:x-arr-sort arr key-fn compare-fn))
  => '(sort! arr :lt (fn [a b] (< (key-fn a) (key-fn b)))))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-char :added "4.1"}
(fact "returns the char code at an index"
  (julia-tf-x-str-char '(:x-str-char s 0))
  => '(Int (getindex s (+ 0 1))))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-split :added "4.1"}
(fact "splits a string by a delimiter"
  (julia-tf-x-str-split '(:x-str-split s ","))
  => '(split s ","))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-join :added "4.1"}
(fact "joins an array with a delimiter"
  (julia-tf-x-str-join '(:x-str-join "," arr))
  => '(join arr ","))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-index-of :added "4.1"}
(fact "finds the index of a substring"
  (julia-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(findfirst "abc" s))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-substring :added "4.1"}
(fact "extracts a substring"
  (julia-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(getindex s (:to (+ 0 1) 5)))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-to-upper :added "4.1"}
(fact "converts a string to uppercase"
  (julia-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(uppercase s))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-to-lower :added "4.1"}
(fact "converts a string to lowercase"
  (julia-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(lowercase s))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-replace :added "4.1"}
(fact "replaces occurrences in a string"
  (julia-tf-x-str-replace '(:x-str-replace s "old" "new"))
  => '(replace s (=> "old" "new")))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-json-encode :added "4.1"}
(fact "encodes an object to JSON"
  (julia-tf-x-json-encode '(:x-json-encode obj))
  => '(JSON.json obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-json-decode :added "4.1"}
(fact "decodes a JSON string"
  (julia-tf-x-json-decode '(:x-json-decode s))
  => '(JSON.parse s))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-encode :added "4.1"}
(fact "encodes a return value with id and key"
  (julia-tf-x-return-encode '(:x-return-encode out id key))
  => '(JSON.json {:id id
                  :key key
                  :type "data"
                  :value out}))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-wrap :added "4.1"}
(fact "wraps a function with error handling"
  (julia-tf-x-return-wrap '(:x-return-wrap f encode-fn))
  => '(encode-fn (f) nil nil))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-eval :added "4.1"}
(fact "evaluates code with a wrapper function"
  (julia-tf-x-return-eval '(:x-return-eval s wrap-fn))
  => '(wrap-fn (fn [] (include_string Main s))))
