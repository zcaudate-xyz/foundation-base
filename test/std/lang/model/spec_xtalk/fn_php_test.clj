(ns std.lang.model.spec-xtalk.fn-php-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-php :refer :all]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-len :added "4.1"}
(fact "php-tf-x-len"
  (php-tf-x-len '(x:len arr))
  => '(count arr))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-cat :added "4.1"}
(fact "php-tf-x-cat"
  (php-tf-x-cat '(x:cat "a" "b"))
  => '(. "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-apply :added "4.1"}
(fact "php-tf-x-apply"
  (php-tf-x-apply '(x:apply f args))
  => '(call_user_func_array f args))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-shell :added "4.1"}
(fact "php-tf-x-shell"
  (php-tf-x-shell '(x:shell "cmd" {}))
  => '(shell_exec "cmd"))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-random :added "4.1"}
(fact "php-tf-x-random"
  (php-tf-x-random '(x:random))
  => '(/ (rand 0 (getrandmax)) (getrandmax)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-type-native :added "4.1"}
(fact "php-tf-x-type-native"
  (php-tf-x-type-native '(x:type-native obj))
  => '(gettype obj))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-err :added "4.1"}
(fact "php-tf-x-err"
  (php-tf-x-err '(x:err "error"))
  => '(throw (new Exception "error")))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-eval :added "4.1"}
(fact "php-tf-x-eval"
  (php-tf-x-eval '(x:eval "code"))
  => '(eval "code"))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-print :added "4.1"}
(fact "php-tf-x-print"
  (php-tf-x-print '(x:print "hello"))
  => '(var_dump "hello"))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-now-ms :added "4.1"}
(fact "php-tf-x-now-ms"
  (php-tf-x-now-ms '(x:now-ms))
  => '(* 1000 (microtime true)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-m-max :added "4.1"}
(fact "php-tf-x-m-max"
  (php-tf-x-m-max '(x:m-max a b))
  => '(max a b))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-m-min :added "4.1"}
(fact "php-tf-x-m-min"
  (php-tf-x-m-min '(x:m-min a b))
  => '(min a b))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-m-mod :added "4.1"}
(fact "php-tf-x-m-mod"
  (php-tf-x-m-mod '(x:m-mod a b))
  => '(:% a (:- " % ") b))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-m-quot :added "4.1"}
(fact "php-tf-x-m-quot"
  (php-tf-x-m-quot '(x:m-quot a b))
  => '(floor (/ a b)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-string? :added "4.1"}
(fact "php-tf-x-is-string?"
  (php-tf-x-is-string? '(x:is-string? x))
  => '(is_string x))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-number? :added "4.1"}
(fact "php-tf-x-is-number?"
  (php-tf-x-is-number? '(x:is-number? x))
  => '(or (is_int x) (is_float x)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-integer? :added "4.1"}
(fact "php-tf-x-is-integer?"
  (php-tf-x-is-integer? '(x:is-integer? x))
  => '(is_int x))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-boolean? :added "4.1"}
(fact "php-tf-x-is-boolean?"
  (php-tf-x-is-boolean? '(x:is-boolean? x))
  => '(is_bool x))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-object? :added "4.1"}
(fact "php-tf-x-is-object?"
  (php-tf-x-is-object? '(x:is-object? x))
  => '(is_object x))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-is-array? :added "4.1"}
(fact "php-tf-x-is-array?"
  (php-tf-x-is-array? '(x:is-array? x))
  => '(is_array x))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-arr-push :added "4.1"}
(fact "php-tf-x-arr-push"
  (php-tf-x-arr-push '(x:arr-push arr item))
  => '(array_push arr item))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-arr-pop :added "4.1"}
(fact "php-tf-x-arr-pop"
  (php-tf-x-arr-pop '(x:arr-pop arr))
  => '(array_pop arr))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-arr-push-first :added "4.1"}
(fact "php-tf-x-arr-push-first"
  (php-tf-x-arr-push-first '(x:arr-push-first arr item))
  => '(array_unshift arr item))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-arr-pop-first :added "4.1"}
(fact "php-tf-x-arr-pop-first"
  (php-tf-x-arr-pop-first '(x:arr-pop-first arr))
  => '(array_shift arr))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-arr-slice :added "4.1"}
(fact "php-tf-x-arr-slice"
  (php-tf-x-arr-slice '(x:arr-slice arr start end))
  => '(array_slice arr start (- end start)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-char :added "4.1"}
(fact "php-tf-x-str-char"
  (php-tf-x-str-char '(x:str-char s i))
  => '(ord (substr s i 1)))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-split :added "4.1"}
(fact "php-tf-x-str-split"
  (php-tf-x-str-split '(x:str-split s tok))
  => '(explode tok s))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-join :added "4.1"}
(fact "php-tf-x-str-join"
  (php-tf-x-str-join '(x:str-join sep arr))
  => '(implode sep arr))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-index-of :added "4.1"}
(fact "php-tf-x-str-index-of"
  (php-tf-x-str-index-of '(x:str-index-of s tok))
  => '(strpos s tok))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-substring :added "4.1"}
(fact "php-tf-x-str-substring"
  (php-tf-x-str-substring '(x:str-substring s start len))
  => '(substr s start len))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-to-upper :added "4.1"}
(fact "php-tf-x-str-to-upper"
  (php-tf-x-str-to-upper '(x:str-to-upper s))
  => '(strtoupper s))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-to-lower :added "4.1"}
(fact "php-tf-x-str-to-lower"
  (php-tf-x-str-to-lower '(x:str-to-lower s))
  => '(strtolower s))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-replace :added "4.1"}
(fact "php-tf-x-str-replace"
  (php-tf-x-str-replace '(x:str-replace s tok rep))
  => '(str_replace tok rep s))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-str-trim :added "4.1"}
(fact "php-tf-x-str-trim"
  (php-tf-x-str-trim '(x:str-trim s))
  => '(trim s))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-return-encode :added "4.1"}
(fact "php-tf-x-return-encode"
  (php-tf-x-return-encode '(x:return-encode out id key))
  => (contains '(do (try (return (json_encode {:id id, :key key, :type "data", :value out})) (catch Exception $e (return (json_encode {:id id, :key key, :type "raw", :value (. "" out)})))))))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-return-wrap :added "4.1"}
(fact "php-tf-x-return-wrap"
  (php-tf-x-return-wrap '(x:return-wrap f enc))
  => (contains '(do (try (:= out (f)) (catch Exception $e (return (json_encode {:type "error", :value (. "" $e)})))) (return (enc out nil nil)))))

^{:refer std.lang.model.spec-xtalk.fn-php/php-tf-x-return-eval :added "4.1"}
(fact "php-tf-x-return-eval"
  (php-tf-x-return-eval '(x:return-eval s wrap))
  => (contains '(return (wrap (function [] (return (eval s)))))))
