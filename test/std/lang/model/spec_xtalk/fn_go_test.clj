(ns std.lang.model.spec-xtalk.fn-go-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-go :refer :all]))

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

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-abs :added "4.1"}
(fact "go-tf-x-m-abs"
  (go-tf-x-m-abs '(x:m-abs n))
  => '(math.Abs n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-max :added "4.1"}
(fact "go-tf-x-m-max"
  (go-tf-x-m-max '(x:m-max a b))
  => '(math.Max a b))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-min :added "4.1"}
(fact "go-tf-x-m-min"
  (go-tf-x-m-min '(x:m-min a b))
  => '(math.Min a b))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-ceil :added "4.1"}
(fact "go-tf-x-m-ceil"
  (go-tf-x-m-ceil '(x:m-ceil n))
  => '(math.Ceil n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-floor :added "4.1"}
(fact "go-tf-x-m-floor"
  (go-tf-x-m-floor '(x:m-floor n))
  => '(math.Floor n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-sqrt :added "4.1"}
(fact "go-tf-x-m-sqrt"
  (go-tf-x-m-sqrt '(x:m-sqrt n))
  => '(math.Sqrt n))

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-m-pow :added "4.1"}
(fact "go-tf-x-m-pow"
  (go-tf-x-m-pow '(x:m-pow b e))
  => '(math.Pow b e))

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

^{:refer std.lang.model.spec-xtalk.fn-go/go-tf-x-arr-push :added "4.1"}
(fact "go-tf-x-arr-push"
  (go-tf-x-arr-push '(x:arr-push arr item))
  => '(:= arr (append arr item)))
