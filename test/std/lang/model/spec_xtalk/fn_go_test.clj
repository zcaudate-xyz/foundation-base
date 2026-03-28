(ns std.lang.model.spec-xtalk.fn-go-test
  (:require [std.lang.model.spec-xtalk.fn-go :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-len :added "4.1"}
(fact "go-tf-x-len"
  (go-tf-x-len '(x:len arr))
  => '(len arr))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-cat :added "4.1"}
(fact "go-tf-x-cat"
  (go-tf-x-cat '(x:cat "a" "b"))
  => '(+ "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-print :added "4.1"}
(fact "go-tf-x-print"
  (go-tf-x-print '(x:print "hello"))
  => '(fmt.Println "hello"))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-err :added "4.1"}
(fact "go-tf-x-err"
  (go-tf-x-err '(x:err "error message"))
  => '(fmt.Errorf "%v" "error message"))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-type-native :added "4.1"}
(fact "go-tf-x-type-native"
  (go-tf-x-type-native '(x:type-native obj))
  => '(fmt.Sprintf "%T" obj))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-abs :added "4.1"}
(fact "go-tf-x-m-abs"
  (go-tf-x-m-abs '(x:m-abs n))
  => '(math.Abs n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-acos :added "4.1"}
(fact "go-tf-x-m-acos"
  (go-tf-x-m-acos '(x:m-acos n))
  => '(math.Acos n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-asin :added "4.1"}
(fact "go-tf-x-m-asin"
  (go-tf-x-m-asin '(x:m-asin n))
  => '(math.Asin n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-atan :added "4.1"}
(fact "go-tf-x-m-atan"
  (go-tf-x-m-atan '(x:m-atan n))
  => '(math.Atan n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-ceil :added "4.1"}
(fact "go-tf-x-m-ceil"
  (go-tf-x-m-ceil '(x:m-ceil n))
  => '(math.Ceil n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-cos :added "4.1"}
(fact "go-tf-x-m-cos"
  (go-tf-x-m-cos '(x:m-cos n))
  => '(math.Cos n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-cosh :added "4.1"}
(fact "go-tf-x-m-cosh"
  (go-tf-x-m-cosh '(x:m-cosh n))
  => '(math.Cosh n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-exp :added "4.1"}
(fact "go-tf-x-m-exp"
  (go-tf-x-m-exp '(x:m-exp n))
  => '(math.Exp n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-floor :added "4.1"}
(fact "go-tf-x-m-floor"
  (go-tf-x-m-floor '(x:m-floor n))
  => '(math.Floor n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-loge :added "4.1"}
(fact "go-tf-x-m-loge"
  (go-tf-x-m-loge '(x:m-loge n))
  => '(math.Log n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-log10 :added "4.1"}
(fact "go-tf-x-m-log10"
  (go-tf-x-m-log10 '(x:m-log10 n))
  => '(math.Log10 n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-max :added "4.1"}
(fact "go-tf-x-m-max"
  (go-tf-x-m-max '(x:m-max a b))
  => '(math.Max a b))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-min :added "4.1"}
(fact "go-tf-x-m-min"
  (go-tf-x-m-min '(x:m-min a b))
  => '(math.Min a b))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-mod :added "4.1"}
(fact "go-tf-x-m-mod"
  (go-tf-x-m-mod '(x:m-mod a b))
  => '(math.Mod a b))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-sin :added "4.1"}
(fact "go-tf-x-m-sin"
  (go-tf-x-m-sin '(x:m-sin n))
  => '(math.Sin n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-sinh :added "4.1"}
(fact "go-tf-x-m-sinh"
  (go-tf-x-m-sinh '(x:m-sinh n))
  => '(math.Sinh n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-sqrt :added "4.1"}
(fact "go-tf-x-m-sqrt"
  (go-tf-x-m-sqrt '(x:m-sqrt n))
  => '(math.Sqrt n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-pow :added "4.1"}
(fact "go-tf-x-m-pow"
  (go-tf-x-m-pow '(x:m-pow b e))
  => '(math.Pow b e))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-tan :added "4.1"}
(fact "go-tf-x-m-tan"
  (go-tf-x-m-tan '(x:m-tan n))
  => '(math.Tan n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-tanh :added "4.1"}
(fact "go-tf-x-m-tanh"
  (go-tf-x-m-tanh '(x:m-tanh n))
  => '(math.Tanh n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-to-string :added "4.1"}
(fact "go-tf-x-to-string"
  (go-tf-x-to-string '(x:to-string obj))
  => '(fmt.Sprint obj))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-to-number :added "4.1"}
(fact "go-tf-x-to-number"
  (go-tf-x-to-number '(x:to-number s))
  => '(strconv.ParseFloat s 64))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-split :added "4.1"}
(fact "go-tf-x-str-split"
  (go-tf-x-str-split '(x:str-split s sep))
  => '(strings.Split s sep))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-join :added "4.1"}
(fact "go-tf-x-str-join"
  (go-tf-x-str-join '(x:str-join sep arr))
  => '(strings.Join arr sep))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-index-of :added "4.1"}
(fact "go-tf-x-str-index-of"
  (go-tf-x-str-index-of '(x:str-index-of s sub))
  => '(strings.Index s sub))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-to-upper :added "4.1"}
(fact "go-tf-x-str-to-upper"
  (go-tf-x-str-to-upper '(x:str-to-upper s))
  => '(strings.ToUpper s))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-to-lower :added "4.1"}
(fact "go-tf-x-str-to-lower"
  (go-tf-x-str-to-lower '(x:str-to-lower s))
  => '(strings.ToLower s))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-trim :added "4.1"}
(fact "go-tf-x-str-trim"
  (go-tf-x-str-trim '(x:str-trim s))
  => '(strings.TrimSpace s))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-trim-left :added "4.1"}
(fact "go-tf-x-str-trim-left"
  (go-tf-x-str-trim-left '(x:str-trim-left s))
  => '(strings.TrimLeft s " "))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-trim-right :added "4.1"}
(fact "go-tf-x-str-trim-right"
  (go-tf-x-str-trim-right '(x:str-trim-right s))
  => '(strings.TrimRight s " "))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-str-replace :added "4.1"}
(fact "go-tf-x-str-replace"
  (go-tf-x-str-replace '(x:str-replace s tok repl))
  => '(strings.ReplaceAll s tok repl))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-arr-push :added "4.1"}
(fact "go-tf-x-arr-push"
  (go-tf-x-arr-push '(x:arr-push arr item))
  => '(:= arr (append arr item)))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-arr-pop :added "4.1"}
(fact "go-tf-x-arr-pop"
  (go-tf-x-arr-pop '(x:arr-pop arr))
  => '(:= arr (. arr (:to 0 (- (len arr) 1)))))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-arr-push-first :added "4.1"}
(fact "go-tf-x-arr-push-first"
  (go-tf-x-arr-push-first '(x:arr-push-first arr item))
  => '(:= arr (append (:vec item) arr)))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-arr-pop-first :added "4.1"}
(fact "go-tf-x-arr-pop-first"
  (go-tf-x-arr-pop-first '(x:arr-pop-first arr))
  => '(:= arr (. arr (:to 1 (len arr)))))
