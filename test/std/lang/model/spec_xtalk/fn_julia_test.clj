(ns std.lang.model.spec-xtalk.fn-julia-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-julia :as fn]))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-del :added "4.0"}
(fact "transforms x:del"
  (fn/julia-tf-x-del '(x:del obj))
  => '(delete! obj))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-cat :added "4.0"}
(fact "transforms x:cat"
  (fn/julia-tf-x-cat '(x:cat a b))
  => '(* a b))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-len :added "4.0"}
(fact "transforms x:len"
  (fn/julia-tf-x-len '(x:len arr))
  => '(length arr))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-get-key :added "4.0"}
(fact "transforms x:get-key"
  (fn/julia-tf-x-get-key '(x:get-key obj key default))
  => '(get obj key default))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-err :added "4.0"}
(fact "transforms x:err"
  (fn/julia-tf-x-err '(x:err msg))
  => '(error msg))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-eval :added "4.0"}
(fact "transforms x:eval"
  (fn/julia-tf-x-eval '(x:eval s))
  => '(eval (Meta.parse s)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-apply :added "4.0"}
(fact "transforms x:apply"
  (fn/julia-tf-x-apply '(x:apply f args))
  => '(f (:... args)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-random :added "4.0"}
(fact "transforms x:random"
  (fn/julia-tf-x-random '(x:random))
  => '(rand))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-print :added "4.0"}
(fact "transforms x:print"
  (fn/julia-tf-x-print '(x:print a b))
  => '(println a b))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-type-native :added "4.0"}
(fact "transforms x:type-native"
  (fn/julia-tf-x-type-native '(x:type-native obj))
  => '(string (typeof obj)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-abs :added "4.0"}
(fact "transforms x:m-abs"
  (fn/julia-tf-x-m-abs '(x:m-abs num))
  => '(abs num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-acos :added "4.0"}
(fact "transforms x:m-acos"
  (fn/julia-tf-x-m-acos '(x:m-acos num))
  => '(acos num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-asin :added "4.0"}
(fact "transforms x:m-asin"
  (fn/julia-tf-x-m-asin '(x:m-asin num))
  => '(asin num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-atan :added "4.0"}
(fact "transforms x:m-atan"
  (fn/julia-tf-x-m-atan '(x:m-atan num))
  => '(atan num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-ceil :added "4.0"}
(fact "transforms x:m-ceil"
  (fn/julia-tf-x-m-ceil '(x:m-ceil num))
  => '(ceil num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-cos :added "4.0"}
(fact "transforms x:m-cos"
  (fn/julia-tf-x-m-cos '(x:m-cos num))
  => '(cos num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-cosh :added "4.0"}
(fact "transforms x:m-cosh"
  (fn/julia-tf-x-m-cosh '(x:m-cosh num))
  => '(cosh num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-exp :added "4.0"}
(fact "transforms x:m-exp"
  (fn/julia-tf-x-m-exp '(x:m-exp num))
  => '(exp num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-floor :added "4.0"}
(fact "transforms x:m-floor"
  (fn/julia-tf-x-m-floor '(x:m-floor num))
  => '(floor num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-loge :added "4.0"}
(fact "transforms x:m-loge"
  (fn/julia-tf-x-m-loge '(x:m-loge num))
  => '(log num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-log10 :added "4.0"}
(fact "transforms x:m-log10"
  (fn/julia-tf-x-m-log10 '(x:m-log10 num))
  => '(log10 num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-max :added "4.0"}
(fact "transforms x:m-max"
  (fn/julia-tf-x-m-max '(x:m-max a b))
  => '(max a b))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-min :added "4.0"}
(fact "transforms x:m-min"
  (fn/julia-tf-x-m-min '(x:m-min a b))
  => '(min a b))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-mod :added "4.0"}
(fact "transforms x:m-mod"
  (fn/julia-tf-x-m-mod '(x:m-mod num denom))
  => '(:% num denom))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-pow :added "4.0"}
(fact "transforms x:m-pow"
  (fn/julia-tf-x-m-pow '(x:m-pow base n))
  => '(^ base n))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-quot :added "4.0"}
(fact "transforms x:m-quot"
  (fn/julia-tf-x-m-quot '(x:m-quot num denom))
  => '(div num denom))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-sin :added "4.0"}
(fact "transforms x:m-sin"
  (fn/julia-tf-x-m-sin '(x:m-sin num))
  => '(sin num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-sinh :added "4.0"}
(fact "transforms x:m-sinh"
  (fn/julia-tf-x-m-sinh '(x:m-sinh num))
  => '(sinh num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-sqrt :added "4.0"}
(fact "transforms x:m-sqrt"
  (fn/julia-tf-x-m-sqrt '(x:m-sqrt num))
  => '(sqrt num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-tan :added "4.0"}
(fact "transforms x:m-tan"
  (fn/julia-tf-x-m-tan '(x:m-tan num))
  => '(tan num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-m-tanh :added "4.0"}
(fact "transforms x:m-tanh"
  (fn/julia-tf-x-m-tanh '(x:m-tanh num))
  => '(tanh num))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-to-string :added "4.0"}
(fact "transforms x:to-string"
  (fn/julia-tf-x-to-string '(x:to-string e))
  => '(string e))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-to-number :added "4.0"}
(fact "transforms x:to-number"
  (fn/julia-tf-x-to-number '(x:to-number e))
  => '(parse Float64 e))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-string? :added "4.0"}
(fact "transforms x:is-string?"
  (fn/julia-tf-x-is-string? '(x:is-string? e))
  => '(isa e String))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-number? :added "4.0"}
(fact "transforms x:is-number?"
  (fn/julia-tf-x-is-number? '(x:is-number? e))
  => '(isa e Number))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-integer? :added "4.0"}
(fact "transforms x:is-integer?"
  (fn/julia-tf-x-is-integer? '(x:is-integer? e))
  => '(isa e Integer))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-boolean? :added "4.0"}
(fact "transforms x:is-boolean?"
  (fn/julia-tf-x-is-boolean? '(x:is-boolean? e))
  => '(isa e Bool))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-function? :added "4.0"}
(fact "transforms x:is-function?"
  (fn/julia-tf-x-is-function? '(x:is-function? e))
  => '(isa e Function))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-object? :added "4.0"}
(fact "transforms x:is-object?"
  (fn/julia-tf-x-is-object? '(x:is-object? e))
  => '(isa e Dict))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-is-array? :added "4.0"}
(fact "transforms x:is-array?"
  (fn/julia-tf-x-is-array? '(x:is-array? e))
  => '(isa e AbstractArray))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-obj-keys :added "4.0"}
(fact "transforms x:obj-keys"
  (fn/julia-tf-x-obj-keys '(x:obj-keys obj))
  => '(collect (keys obj)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-obj-vals :added "4.0"}
(fact "transforms x:obj-vals"
  (fn/julia-tf-x-obj-vals '(x:obj-vals obj))
  => '(collect (values obj)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-obj-pairs :added "4.0"}
(fact "transforms x:obj-pairs"
  (fn/julia-tf-x-obj-pairs '(x:obj-pairs obj))
  => '(collect obj))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-obj-clone :added "4.0"}
(fact "transforms x:obj-clone"
  (fn/julia-tf-x-obj-clone '(x:obj-clone obj))
  => '(copy obj))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-clone :added "4.0"}
(fact "transforms x:arr-clone"
  (fn/julia-tf-x-arr-clone '(x:arr-clone arr))
  => '(copy arr))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-slice :added "4.0"}
(fact "transforms x:arr-slice"
  (fn/julia-tf-x-arr-slice '(x:arr-slice arr start end))
  => '(getindex arr (:to (+ start 1) end)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-push :added "4.0"}
(fact "transforms x:arr-push"
  (fn/julia-tf-x-arr-push '(x:arr-push arr item))
  => '(push! arr item))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-pop :added "4.0"}
(fact "transforms x:arr-pop"
  (fn/julia-tf-x-arr-pop '(x:arr-pop arr))
  => '(pop! arr))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-reverse :added "4.0"}
(fact "transforms x:arr-reverse"
  (fn/julia-tf-x-arr-reverse '(x:arr-reverse arr))
  => '(reverse arr))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-push-first :added "4.0"}
(fact "transforms x:arr-push-first"
  (fn/julia-tf-x-arr-push-first '(x:arr-push-first arr item))
  => '(pushfirst! arr item))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-pop-first :added "4.0"}
(fact "transforms x:arr-pop-first"
  (fn/julia-tf-x-arr-pop-first '(x:arr-pop-first arr))
  => '(popfirst! arr))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-insert :added "4.0"}
(fact "transforms x:arr-insert"
  (fn/julia-tf-x-arr-insert '(x:arr-insert arr idx e))
  => '(insert! arr (+ idx 1) e))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-arr-sort :added "4.0"}
(fact "transforms x:arr-sort"
  (fn/julia-tf-x-arr-sort '(x:arr-sort arr key-fn compare-fn))
  => '(sort! arr :lt (fn [a b] (< (key-fn a) (key-fn b)))))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-char :added "4.0"}
(fact "transforms x:str-char"
  (fn/julia-tf-x-str-char '(x:str-char s i))
  => '(Int (getindex s (+ i 1))))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-split :added "4.0"}
(fact "transforms x:str-split"
  (fn/julia-tf-x-str-split '(x:str-split s tok))
  => '(split s tok))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-join :added "4.0"}
(fact "transforms x:str-join"
  (fn/julia-tf-x-str-join '(x:str-join s arr))
  => '(join arr s))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-index-of :added "4.0"}
(fact "transforms x:str-index-of"
  (fn/julia-tf-x-str-index-of '(x:str-index-of s tok))
  => '(findfirst tok s))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-substring :added "4.0"}
(fact "transforms x:str-substring"
  (fn/julia-tf-x-str-substring '(x:str-substring s start))
  => '(getindex s (:to (+ start 1) (end)))

  (fn/julia-tf-x-str-substring '(x:str-substring s start end))
  => '(getindex s (:to (+ start 1) end)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-to-upper :added "4.0"}
(fact "transforms x:str-to-upper"
  (fn/julia-tf-x-str-to-upper '(x:str-to-upper s))
  => '(uppercase s))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-to-lower :added "4.0"}
(fact "transforms x:str-to-lower"
  (fn/julia-tf-x-str-to-lower '(x:str-to-lower s))
  => '(lowercase s))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-str-replace :added "4.0"}
(fact "transforms x:str-replace"
  (fn/julia-tf-x-str-replace '(x:str-replace s tok replacement))
  => '(replace s (=> tok replacement)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-json-encode :added "4.0"}
(fact "transforms x:json-encode"
  (fn/julia-tf-x-json-encode '(x:json-encode obj))
  => '(. JSON (json obj)))

^{:refer std.lang.model.spec-xtalk.fn-julia/julia-tf-x-json-decode :added "4.0"}
(fact "transforms x:json-decode"
  (fn/julia-tf-x-json-decode '(x:json-decode s))
  => '(. JSON (parse s)))
