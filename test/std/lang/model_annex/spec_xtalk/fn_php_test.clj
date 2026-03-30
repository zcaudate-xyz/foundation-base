(ns std.lang.model-annex.spec-xtalk.fn-php-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-php :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-len :added "4.1"}
(fact "returns count of array"
  (php-tf-x-len '(:x-len arr))
  => '(count arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (php-tf-x-cat '(:x-cat "a" "b"))
  => '(. "a" "b"))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-apply :added "4.1"}
(fact "applies function to array"
  (php-tf-x-apply '(:x-apply f args))
  => '(call_user_func_array f args))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-shell :added "4.1"}
(fact "executes shell command"
  (php-tf-x-shell '(:x-shell "ls" {}))
  => '(shell_exec "ls"))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-random :added "4.1"}
(fact "generates random number"
  (php-tf-x-random '(:x-random))
  => '(/ (rand 0 (getrandmax)) (getrandmax)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-type-native :added "4.1"}
(fact "gets native type"
  (php-tf-x-type-native '(:x-type-native obj))
  => '(gettype obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-err :added "4.1"}
(fact "throws exception"
  (php-tf-x-err '(:x-err "message"))
  => '(throw (new Exception "message")))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-eval :added "4.1"}
(fact "evaluates code"
  (php-tf-x-eval '(:x-eval "code"))
  => '(eval "code"))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-print :added "4.1"}
(fact "prints values"
  (php-tf-x-print '(:x-print "hello"))
  => '(var_dump "hello"))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-now-ms :added "4.1"}
(fact "gets current time in ms"
  (php-tf-x-now-ms '(:x-now-ms))
  => '(* 1000 (microtime true)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-m-max :added "4.1"}
(fact "returns maximum"
  (php-tf-x-m-max '(:x-m-max 1 2 3))
  => '(max 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-m-min :added "4.1"}
(fact "returns minimum"
  (php-tf-x-m-min '(:x-m-min 1 2 3))
  => '(min 1 2 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-m-mod :added "4.1"}
(fact "returns modulo"
  (php-tf-x-m-mod '(:x-m-mod 10 3))
  => '(:% 10 :- " % " 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-m-quot :added "4.1"}
(fact "returns quotient"
  (php-tf-x-m-quot '(:x-m-quot 10 3))
  => '(floor (/ 10 3)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-string? :added "4.1"}
(fact "checks if string"
  (php-tf-x-is-string? '(:x-is-string? x))
  => '(is_string x))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-number? :added "4.1"}
(fact "checks if number"
  (php-tf-x-is-number? '(:x-is-number? x))
  => '(or (is_int x) (is_float x)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-integer? :added "4.1"}
(fact "checks if integer"
  (php-tf-x-is-integer? '(:x-is-integer? x))
  => '(is_int x))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-boolean? :added "4.1"}
(fact "checks if boolean"
  (php-tf-x-is-boolean? '(:x-is-boolean? x))
  => '(is_bool x))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-object? :added "4.1"}
(fact "checks if object"
  (php-tf-x-is-object? '(:x-is-object? x))
  => '(is_object x))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-is-array? :added "4.1"}
(fact "checks if array"
  (php-tf-x-is-array? '(:x-is-array? x))
  => '(is_array x))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (php-tf-x-arr-push '(:x-arr-push arr item))
  => '(array_push arr item))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-pop :added "4.1"}
(fact "pops from array"
  (php-tf-x-arr-pop '(:x-arr-pop arr))
  => '(array_pop arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-push-first :added "4.1"}
(fact "unshifts to array"
  (php-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(array_unshift arr item))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-pop-first :added "4.1"}
(fact "shifts from array"
  (php-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(array_shift arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-arr-slice :added "4.1"}
(fact "slices array"
  (php-tf-x-arr-slice '(:x-arr-slice arr 0 5))
  => '(array_slice arr 0 (- 5 0)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-char :added "4.1"}
(fact "gets char code"
  (php-tf-x-str-char '(:x-str-char s 0))
  => '(ord (substr s 0 1)))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-split :added "4.1"}
(fact "splits string"
  (php-tf-x-str-split '(:x-str-split s ","))
  => '(explode "," s))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (php-tf-x-str-join '(:x-str-join "," arr))
  => '(implode "," arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-index-of :added "4.1"}
(fact "finds substring"
  (php-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(strpos s "abc"))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-substring :added "4.1"}
(fact "extracts substring"
  (php-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(substr s 0 5))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-to-upper :added "4.1"}
(fact "converts to uppercase"
  (php-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(strtoupper s))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-to-lower :added "4.1"}
(fact "converts to lowercase"
  (php-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(strtolower s))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-replace :added "4.1"}
(fact "replaces in string"
  (php-tf-x-str-replace '(:x-str-replace s "old" "new"))
  => '(str_replace "old" "new" s))

^{:refer std.lang.model-annex.spec-xtalk.fn-php/php-tf-x-str-trim :added "4.1"}
(fact "trims string"
  (php-tf-x-str-trim '(:x-str-trim s))
  => '(trim s))
